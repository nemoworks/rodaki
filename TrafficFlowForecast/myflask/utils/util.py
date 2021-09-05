import datetime
import yaml
import pandas as pd
from myflask.es_op.data import cal_distance_es
import os
import pickle
import numpy as np
import shutil
import myflask.utils.logger as logging
normalized_k = 0.1


def get_flask_server_params():
    '''
    Returns connection parameters of the Flask application
    :return: Tripple of server name, server port and debug settings
    '''
    with open('myflask/config/config.yaml') as f:
        flask_config = yaml.load(f)
        server_host = flask_config['flask'].get('server_host', '0.0.0.0')
        server_port = flask_config['flask'].get('server_port', 5000)
        flask_debug = flask_config['flask'].get('debug', True)
        return server_host, server_port, flask_debug, flask_config
    # server_name = utils.get_env_var_setting('FLASK_SERVER_NAME', settings.DEFAULT_FLASK_SERVER_NAME)
    # server_port = int(utils.get_env_var_setting('FLASK_SERVER_PORT', settings.DEFAULT_FLASK_SERVER_PORT))
    # flask_debug = utils.get_env_var_setting('FLASK_DEBUG', settings.DEFAULT_FLASK_DEBUG)

    # flask_debug = True if flask_debug == '1' else False


def gen_adj_mx(es_index, host, port, gte, lte, sensors, outputdir):
    # 先获取每个hex到其他hex的车辆通过信息
    sensor_ids, sensor_id_to_ind, adj_mx = cal_distance_es(
        list(sensors), gte, lte, es_index, host, port)
    adj_mx[adj_mx < normalized_k] = 0
    # print(adj_mx)
    adj_mx = adj_mx.astype('float32')
    # print(adj_mx.dtype)
    # Save to pickle file.
    with open(os.path.join(outputdir, 'adj_mx.pkl'), 'wb') as f:
        pickle.dump([sensor_ids, sensor_id_to_ind, adj_mx], f, protocol=2)
    with open(os.path.join(outputdir, 'sensor_ids.yaml'), 'w') as f:
        yaml.dump(sensor_ids, f, default_flow_style=False)


def generate_graph_seq2seq_io_data(
        df, x_offsets, y_offsets, add_time_in_day=True, add_day_in_week=False, scaler=None
):
    """
    Generate samples from
    :param df:
    :param x_offsets:
    :param y_offsets:
    :param add_time_in_day:
    :param add_day_in_week:
    :param scaler:
    :return:
    # x: (epoch_size, input_length, num_nodes, input_dim)
    # y: (epoch_size, output_length, num_nodes, output_dim)
    """

    num_samples, num_nodes = df.shape
    data = np.expand_dims(df.values, axis=-1)
    data_list = [data]
    if add_time_in_day:
        time_ind = (df.index.values.astype('datetime64') -
                    df.index.values.astype("datetime64[D]")) / np.timedelta64(1, "D")
        time_in_day = np.tile(time_ind, [1, num_nodes, 1]).transpose((2, 1, 0))
        data_list.append(time_in_day)
    if add_day_in_week:
        day_in_week = np.zeros(shape=(num_samples, num_nodes, 7))
        day_in_week[np.arange(num_samples), :, df.index.dayofweek] = 1
        data_list.append(day_in_week)

    data = np.concatenate(data_list, axis=-1)
    # epoch_len = num_samples + min(x_offsets) - max(y_offsets)
    x, y = [], []
    # t is the index of the last observation.
    min_t = abs(min(x_offsets))
    max_t = abs(num_samples - abs(max(y_offsets)))  # Exclusive
    for t in range(min_t, max_t):
        x_t = data[t + x_offsets, ...]
        y_t = data[t + y_offsets, ...]
        x.append(x_t)
        y.append(y_t)
    x = np.stack(x, axis=0)
    y = np.stack(y, axis=0)
    return x, y


def generate_train_test(speed_dataframe_uri, modelparam_path, transformed_examples_uri, datatype):
    df = pd.read_hdf(speed_dataframe_uri)

    modelparam = yaml.load(open(modelparam_path))
    logging.logger.info(modelparam)
    # 0 is the latest observed sample.
    x_offsets = np.sort(
        # np.concatenate(([-week_size + 1, -day_size + 1], np.arange(-11, 1, 1)))
        # np.concatenate((np.arange(-11, 1, 1),))
        np.concatenate((np.arange(-(modelparam['model']['seq_len']-1), 1, 1),))
    )
    # Predict the next one hour
    # y_offsets = np.sort(np.arange(1, 13, 1))
    y_offsets = np.sort(np.arange(1, (modelparam['model']['horizon']+1), 1))
    if len(df) < (modelparam['model']['seq_len']+modelparam['model']['horizon']):
        logging.logger.error(
            '数据的时间范围过小，请尝试增加数据集的时间范围，或者减小last_hours、predict_hours')
        raise AssertionError
    # x: (num_samples, input_length, num_nodes, input_dim)
    # y: (num_samples, output_length, num_nodes, input_dim)
    x, y = generate_graph_seq2seq_io_data(
        df,
        x_offsets=x_offsets,
        y_offsets=y_offsets,
        add_time_in_day=True,
        add_day_in_week=False,
    )

    #logging.logger.info("x shape: ", x.shape, ", y shape: ", y.shape)

    # num_test = 6831, using the last 6831 examples as testing.
    # for the rest: 7/8 is used for training, and 1/8 is used for validation.
    num_samples = x.shape[0]
    train_rate = modelparam['data']['train'] / \
        (modelparam['data']['test']+modelparam['data']['train'])
    # train_rate=1.0-test_rate
    num_train = round(
        num_samples * train_rate) if round(num_samples * train_rate) > 0 else 1
    num_test = num_samples-num_train if num_samples-num_train > 0 else 1
    modelparam['model']['num_nodes'] = x.shape[2]
    modelparam['model']['input_dim'] = x.shape[3]
    # train
    x_train, y_train = x[:num_train], y[:num_train]
    # test
    x_test, y_test = x[-num_test:], y[-num_test:]

    for cat in ["train", "test"]:
        _x, _y = locals()["x_" + cat], locals()["y_" + cat]
        #logging.logger.info(cat, "x: ", _x.shape, "y:", _y.shape)
        np.savez_compressed(
            os.path.join(transformed_examples_uri, "%s.npz" % cat),
            x=_x,
            y=_y,
        )
    logging.logger.info(modelparam)
    with open(os.path.join(transformed_examples_uri, 'model.yaml'), 'w') as f:
        yaml.dump(modelparam, f, default_flow_style=False)
    return x.shape, y.shape, {'model': modelparam['model'], 'data': modelparam['data']}


def load_dataset(dataset_uri, batch_size, schema, pad_with_last_sample=True):
    data = {}
    cat_data = np.load(dataset_uri)
    data['x'] = cat_data['x'].astype(schema)
    data['y'] = cat_data['y'].astype(schema)
    if pad_with_last_sample:
        num_padding = (batch_size - (len(data['x']) % batch_size)) % batch_size
        x_padding = np.repeat(data['x'][-1:], num_padding, axis=0)
        y_padding = np.repeat(data['y'][-1:], num_padding, axis=0)
        data['x'] = np.concatenate([data['x'], x_padding], axis=0)
        data['y'] = np.concatenate([data['y'], y_padding], axis=0)
    #logging.logger.info(data['x'].shape, data['y'].shape)
    return data


def copy_dir(src, dst):
    if os.path.exists(dst):
        os.removedirs(dst)
    shutil.copytree(src, dst)
    return


def generate_serving_input(history, add_time_in_day=True, add_day_in_week=False):
    num_samples, num_nodes = history.shape
    data = np.expand_dims(history.values, axis=-1)
    data_list = [data]
    if add_time_in_day:
        time_ind = (history.index.values.astype('datetime64') -
                    history.index.values.astype("datetime64[D]")) / np.timedelta64(1, "D")
        time_in_day = np.tile(time_ind, [1, num_nodes, 1]).transpose((2, 1, 0))
        data_list.append(time_in_day)
    if add_day_in_week:
        day_in_week = np.zeros(shape=(num_samples, num_nodes, 7))
        day_in_week[np.arange(num_samples), :, history.index.dayofweek] = 1
        data_list.append(day_in_week)
    data = np.concatenate(data_list, axis=-1)
    # print(data)
    return np.array([data])


def check_serving_dir(serving_dir):
    # 存在serving dir并且里面有至少一个目录,就说明已经至少有一个可以部署的模型了
    if os.path.exists(serving_dir) and len(os.listdir(serving_dir)) > 0:
        return True
    return False


def model_evaluate_group(dataframe):
    dataframe['timestamp'] = pd.to_datetime(
        dataframe['timestamp'], unit='ms', utc=True).dt.tz_convert('Asia/Shanghai')
    # 按照hex进行分组
    DataFrameGrouptemp = dataframe.groupby(['name'])
    # print('当前分组的hex值：'+','.join(list(DataFrameGrouptemp.groups.keys())))
    hex_array_list = {}
    # 将分组的结果按照时间排序，便于后续的时间段流量计算
    for i in DataFrameGrouptemp.groups.keys():
        hex_array_list[i] = DataFrameGrouptemp.get_group(
            i).sort_values('timestamp')
    speed_per_60_min = {}  # 用字典记录
    for (key, dataframe) in hex_array_list.items():
        dataframe = dataframe.groupby('timestamp').mean()
        speed_per_60_min[key] = pd.DataFrame(data=dataframe['value'].values,
                                             index=dataframe.index,
                                             columns=[key])
    data = pd.concat([v for (k, v) in speed_per_60_min.items()], axis=1)
    # data.index.name=None
    # print(data.index.tz)
    # data.index=data.index.tz_convert(None)
    # 这一步是为了防止这段历史数据里，存在所有门架在中间某个小时nan的情况
    b = pd.DataFrame(index=pd.date_range(
        start=data.index[0], end=data.index[-1], freq='h', tz='Asia/Shanghai'))
    data = pd.concat([b, data], axis=1)
    # 将缺失值用前后两个时间的数据的平均值做填充
    # data=((data.ffill()+data.bfill())/2).bfill().ffill()
    # 将缺失值用0填充
    # data.fillna(0.0,inplace=True)
    return data


def get_same_column_and_index(real_dataframe, predict_dataframe):
    real_cols, predict_cols = list(
        real_dataframe.columns), list(predict_dataframe.columns)
    select_cols = list(set(real_cols).intersection(
        set(predict_cols)))  # 列用集合的取交集方法
    gte = max(real_dataframe.index[0], predict_dataframe.index[0])  # 起始时间
    lte = min(real_dataframe.index[-1], predict_dataframe.index[-1])  # 结束时间
    real_dataframe, predict_dataframe = real_dataframe[gte:
                                                       lte][select_cols], predict_dataframe[gte:lte][select_cols]
    return real_dataframe, predict_dataframe


def check_MAPE_evaluate(real_dataframe, predict_dataframe, evaluate_metrics_threshold=0.3, evaluate_metrics_type='lower'):
    realvalues, predictvalues = real_dataframe.values, predict_dataframe.values
    mask = ~np.isnan(realvalues)
    mask = mask.astype('float32')
    mask /= np.mean(mask)
    mape = np.abs(np.divide(np.subtract(
        predictvalues, realvalues).astype('float32'), realvalues + 1e-10))
    mape = np.nan_to_num(mask * mape)
    mape = np.mean(mape)
    logging.logger.info("MAPE:{}".format(mape))
    if evaluate_metrics_type == 'lower':
        return mape < evaluate_metrics_threshold
    else:
        return mape > evaluate_metrics_threshold
