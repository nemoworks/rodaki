import tensorflow as tf
import os
#import absl.logging as logging
import numpy as np
import yaml
import pandas as pd
from myflask.es_op.data import writePredictDataFrame, getVolumeFromRealIndex, getVolumeFromRealIndex_with_sensors, writeTrueDataFrame
from myflask.utils.util import generate_serving_input, model_evaluate_group, get_same_column_and_index, check_MAPE_evaluate
import myflask.utils.logger as logging
import shutil
from myflask.beam_pipline.create_pipline import _create_pipeline
from tfx.orchestration.beam.beam_dag_runner import BeamDagRunner


def re_execute_pipline(flask_config):
    _pipeline_name = flask_config['beam_pipline']['pipeline_name']
    _beam_pipeline_args = [
        '--direct_running_mode=multi_processing',
        # 0 means auto-detect based on on the number of CPUs available
        # during execution time.
        '--direct_num_workers=0',
    ]
    project_root = './'
    _module_file = os.path.join(
        project_root, 'myflask/beam_pipline/trainer_module.py')
    _pipeline_root = os.path.join(project_root, 'piplines', _pipeline_name)
    _metadata_path = os.path.join(project_root, 'metadata', _pipeline_name,
                                  'metadata.db')

    # try:
    #     shutil.rmtree(_pipeline_root)
    # except FileNotFoundError:
    #     print('本来不存在，不需要删除')
    # try:
    #     os.remove(_metadata_path)
    # except FileNotFoundError:
    #     print('本来不存在，不需要删除')

    # with app.context():
    #    print(current_app.config['flask_config'])

    try:
        BeamDagRunner().run(
        _create_pipeline(
            pipeline_name=_pipeline_name,
            pipeline_root=_pipeline_root,
            metadata_path=_metadata_path,
            module_file=_module_file,
            beam_pipeline_args=_beam_pipeline_args,
            param=flask_config))
    except Exception as e:
        logging.logger.error(e)

class Predictor:
    def __init__(self, serving_dir, max_keep, model_config):
        if serving_dir is not None and not os.path.exists(serving_dir):
            os.mkdir(serving_dir)
        self.max_keep = max_keep
        self.serving_dir = serving_dir
        self.update_current_serving_model()
        self.sess_config = tf.compat.v1.ConfigProto()
        if not model_config['use_gpu'] or not tf.test.is_gpu_available():
            self.sess_config = tf.compat.v1.ConfigProto(device_count={'GPU': 0},
                                                        intra_op_parallelism_threads=model_config.get(
                                                            'intra_op_parallelism_threads', 5),
                                                        inter_op_parallelism_threads=model_config.get('inter_op_parallelism_threads', 5))
        self.sess_config.gpu_options.allow_growth = True

    def predict(self, es_config, model_config, debug=False):
        if self.current_serving_model is None:
            logging.logger.warning('当前预测器没有使用serving model，因此跳过')
            return
        saved_model_dir = self.current_serving_model
        logging.logger.info(saved_model_dir)
        # (saved_model_dir)
        # 读取hex与经纬度的字典
        # iddict = yaml.load(
        #     open(os.path.join(saved_model_dir, 'assets.extra', 'hex_id.yaml')))
        sensor_hexs = yaml.load(
            open(os.path.join(saved_model_dir, 'assets.extra', 'sensor_ids.yaml')))
        # 升序排下
        sensor_hexs.sort()

        model_final = yaml.load(
            open(os.path.join(saved_model_dir, 'assets.extra', 'model_final.yaml')))
        predict_fn = tf.contrib.predictor.from_saved_model(
            export_dir=saved_model_dir,
            config=self.sess_config,
        )
        seq_len = model_final['model']['seq_len']
        horizon = model_final['model']['horizon']
        num_nodes = model_final['model']['num_nodes']
        input_dim = model_final['model']['input_dim']
        # 取时间,为程序运行当天的0点
        timenow = pd.Timestamp.now().date()
        # 查询起始时间
        gte = pd.Timestamp(timenow)-pd.Timedelta(seq_len, 'h')
        # 查询截止时间，程序运行当天的0点
        lte = pd.Timestamp(timenow)
        if debug:
            gte = pd.Timestamp('2020-09-13 00:00:00')
            lte = gte+pd.Timedelta(seq_len, 'h')

        data = getVolumeFromRealIndex_with_sensors(
            es_config["host"], es_config['real_index_name'], es_config["port"], sensor_hexs, gte, lte)
        table = pd.DataFrame(
            data, columns=["timestamp", "name", "coordinates", 'value', 'directionflag'])
        table['timestamp'] = pd.to_datetime(
            table['timestamp'], unit='ms', utc=True).dt.tz_convert('Asia/Shanghai')
        if len(table) == 0:
            logging.logger.warning('数据量为0，请重新选择时间范围，这次不做预测')
            raise AssertionError
        # row_dataframe.drop(row_dataframe[row_dataframe['备注']=='非一二类流水,仅备份'].index,inplace=True)

        # print(row_dataframe['计费交易时间'].unique())
        #assert 0>0,'用来预测的历史数据为空！'
        DataFrameGrouptemp = table.groupby(['name'])
        # print('当前分组的hex值：'+','.join(list(DataFrameGrouptemp.groups.keys())))
        hex_array_list = {}
        iddict = {}  # hex：经纬度
        hex_direction = {}  # hex:上下行标志
        # 将分组的结果按照时间排序，便于后续的时间段流量计算
        for i in DataFrameGrouptemp.groups.keys():
            hex_array_list[i] = DataFrameGrouptemp.get_group(
                i).sort_values('timestamp')
            iddict[i] = hex_array_list[i]["coordinates"].values[-1]  # 直接取最新的
            # 直接取最新的
            hex_direction = hex_array_list[i]["directionflag"].values[-1]
            # print(hex_array_list[i]['计费交易时间'].unique())
        #assert 0>0,'用来预测的历史数据为空！'
        speed_per_60_min = {}  # 用字典记录
        for (key, dataframe) in hex_array_list.items():
            # print('当前正在处理：'+key)
            dataframe = dataframe.groupby('timestamp').sum()
            speed_per_60_min[key] = pd.DataFrame(data=dataframe['value'].values,
                                                 index=dataframe.index,
                                                 columns=[key])
        dataframe = pd.concat(
            [v for (k, v) in speed_per_60_min.items()], axis=1)
        dataframe.index.name = None
        # print(dataframe)
        # 这一步是为了防止这段历史数据里，存在所有门架在中间某个小时nan的情况,这样的话，index就会缺失一个
        b = pd.DataFrame(index=pd.date_range(
            start=gte, periods=seq_len, freq='h', tz="Asia/Shanghai"))
        dataframe = pd.concat([b, dataframe], axis=1)
        # print(dataframe)
        # 将缺失值用前后两个时间的数据的平均值做填充
        # dataframe=((dataframe.ffill()+dataframe.bfill())/2).bfill().ffill()
        # 将缺失值用0填充
        # dataframe.fillna(0.0,replace=True)
        # print(dataframe)
        # 防止预测取的历史数据里与当初数据集里的graph_sensor_ids_data.txt里的门架不一致（缺少门架）
        dataframenew = pd.DataFrame(index=dataframe.index, columns=sensor_hexs)
        for i in dataframe.columns:
            dataframenew[i] = dataframe[i].values
        # 将缺失值用0填充
        #dataframenew.fillna(0.0, inplace=True)
        # 将缺失值用前后两个时间的数据的平均值做填充
        dataframenew = (
            (dataframenew.ffill()+dataframenew.bfill())/2).bfill().ffill()
        # logging.logger.info(dataframenew)
        #assert len(dataframenew)==seq_len, '预测时的历史数据长度不等于seq_len'
        #assert ''.join(dataframenew.columns)==''.join(sensor_hexs), '这段时间里门架与数据集里的门架不一致'
        if len(dataframenew) != seq_len or ''.join(dataframenew.columns) != ''.join(sensor_hexs):
            logging.logger.error('预测时的历史数据长度不等于seq_len or 这段时间里门架与数据集里的门架不一致')
            return
        # print(dataframe)
        # 预测的时间索引
        next_indexes = pd.date_range(start=dataframenew.index[-1],
                                     freq='1h',
                                     periods=horizon+1,
                                     # tz='Asia/Shanghai',
                                     closed='right'
                                     )
        # logging.logger.info(dataframenew)
        # print(next_indexes)
        inputs = generate_serving_input(dataframenew)

        predict = (predict_fn({'input': inputs})['predictions'])
        # logging.logger.info(predict)
        predict_dataframe = pd.DataFrame(
            index=next_indexes, data=predict, columns=dataframenew.columns)
        # 将预测为负数的位置变为0
        predict_dataframe[predict_dataframe < 0.0] = 0.0
        predict_dataframe = predict_dataframe.astype('int')
        logging.logger.info("完成一次预测")
        writePredictDataFrame(
            predict_dataframe, es_config, sensor_hexs, iddict, hex_direction)

    def update_current_serving_model(self):
        dirs = os.listdir(path=self.serving_dir)
        if len(dirs) == 0:
            logging.logger.warning('当前还没有模型')
            self.current_serving_model = None
            return
        dirs.sort(reverse=True)
        logging.logger.info(dirs)
        self.current_serving_model = os.path.join(self.serving_dir, dirs[0])

    def update_serving_dir(self, serving_dir):
        self.serving_dir = serving_dir

    def update_max_keep(self, max_keep):
        self.max_keep = max_keep

    def delete_extra_model(self):
        dirs = os.listdir(path=self.serving_dir)
        if len(dirs) == 0:
            logging.logger.warning('当前还没有模型')
            return
        dirs.sort()
        if len(dirs) > self.max_keep:
            logging.logger.warning('模型超过最大数目,删除')
            for i in range(0, len(dirs)-self.max_keep):
                shutil.rmtree(os.path.join(self.serving_dir, dirs[i]))

    def current_model_evaluate(self, es_config, model_config, flask_config, debug=False):
        if self.current_serving_model is None:
            logging.logger.warning('当前预测器没有使用serving model，因此跳过验证，直接开始训练')
            re_execute_pipline(flask_config)
            self.delete_extra_model()
            self.update_current_serving_model()
            return
        saved_model_dir = self.current_serving_model
        logging.logger.info(saved_model_dir)
        # (saved_model_dir)
        # 读取hex与编号的字典
        iddict = yaml.load(
            open(os.path.join(saved_model_dir, 'assets.extra', 'hex_id.yaml')))
        sensor_hexs = yaml.load(
            open(os.path.join(saved_model_dir, 'assets.extra', 'sensor_ids.yaml')))
        # 取时间,为程序运行当天的0点
        timenow = pd.Timestamp.now().date()
        # 查询起始时间
        gte = pd.Timestamp(
            timenow)-pd.Timedelta(model_config['evaluate_interval'], 'day')
        # 查询截止时间，程序运行当天的0点
        lte = pd.Timestamp(timenow)
        if debug:
            # debug是每一天去验证一次
            gte = pd.Timestamp('2020-09-13 00:00:00')
            lte = gte+pd.Timedelta(24, 'h')
        # 取时间,为程序运行当天的0点
        # timenow=pd.Timestamp.now().date()
        #timenow = pd.Timestamp('2020-09-13 00:00:00').date()
        #timenow=pd.Timestamp('2020-07-11 22:00:00')
        # 查询起始时间
        # gte=pd.Timestamp(timenow)-pd.Timedelta(seq_len,'h')
        #gte=pd.Timestamp('2020-09-13 00:00:00')
        #gte = pd.Timestamp(timenow)
        # 查询截止时间，程序运行当天的0点
        #lte = gte+pd.Timedelta(24, 'h')
        # lte=pd.Timestamp(timenow)
        #lte=pd.Timestamp('2020-09-13 01:00:00')
        realdata = getVolumeFromRealIndex_with_sensors(
            es_config['host'], es_config['real_index_name'], es_config['port'], sensor_hexs, gte, lte)
        if(len(realdata)) == 0:
            logging.logger.info('暂时还没有真实流量')
            return
        row_real_dataframe = pd.DataFrame(
            realdata, columns=["timestamp", "name", "coordinates", 'value'])
        predictdata = getVolumeFromRealIndex_with_sensors(
            es_config['host'], es_config['predict_index_name'], es_config['port'], sensor_hexs, gte, lte)
        if(len(predictdata)) == 0:
            return
        row_predict_dataframe = pd.DataFrame(
            predictdata, columns=["timestamp", "name", "coordinates", 'value'])
        logging.logger.info('row_real_dataframe'+str(row_real_dataframe))
        logging.logger.info('row_predict_dataframe'+str(row_predict_dataframe))
        real_dataframe = model_evaluate_group(row_real_dataframe)
        predict_dataframe = model_evaluate_group(row_predict_dataframe)
        logging.logger.info(real_dataframe)
        logging.logger.info(predict_dataframe)

        # 然后取real_dataframe与predict_dataframe相同的列名与时间重叠，防止节点不一致
        real_dataframe, predict_dataframe = get_same_column_and_index(
            real_dataframe, predict_dataframe)
        if len(real_dataframe) == 0 or len(predict_dataframe) == 0:
            return
        logging.logger.info(real_dataframe)
        logging.logger.info(predict_dataframe)
        if model_config['evaluate_metrics'] == 'MAPE':
            if check_MAPE_evaluate(
                real_dataframe,
                predict_dataframe,
                model_config['evaluate_metrics_threshold'],
                model_config['evaluate_metrics_type']
            ):
                return
            else:  # 需要重新执行pipline
                if not debug:
                    re_execute_pipline(flask_config)
                    self.delete_extra_model()
                    self.update_current_serving_model()
                pass
