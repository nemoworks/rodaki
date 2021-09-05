from tfx.dsl.component.experimental.annotations import OutputDict
from tfx.dsl.component.experimental.annotations import InputArtifact
from tfx.dsl.component.experimental.annotations import OutputArtifact
from tfx.dsl.component.experimental.annotations import Parameter
from tfx.dsl.component.experimental.decorators import component
from tfx.types.standard_artifacts import Examples
from myflask.es_op.data import getVolumeFromRealIndex  # , writeTrueDataFrame
import pandas as pd
import os
import json
import yaml
import numpy as np
from pathlib import Path
from myflask.utils.util import gen_adj_mx
import myflask.utils.logger as logging
import myflask.es_op.field as field


@component
def MyExampleGen(
    examples: OutputArtifact[Examples],
    param: Parameter[str],
    gte: Parameter[str],
    lte: Parameter[str],
    debug: Parameter[str] = 'False'
) -> OutputDict():
    
        
    param = json.loads(param)
    # 取时间,为程序运行当天的0点
    timenow=pd.Timestamp.now().date()
    #timenow = pd.Timestamp('2020-09-14 00:00:00').date()
    #timenow=pd.Timestamp('2020-07-11 22:00:00')
    # 查询起始时间
    #gte=pd.Timestamp(timenow)-pd.Timedelta(seq_len,'h')
    #gte = pd.Timestamp('2020-09-13 00:00:00')
    #gte = pd.Timestamp(timenow)
    # 查询截止时间，程序运行当天的0点
    #lte = gte+pd.Timedelta(3, 'h')
    # lte=pd.Timestamp(timenow)
    #lte=pd.Timestamp('2020-09-13 01:00:00')
    # print(timenow)
    if lte == '':  # 查询截止时间
        lte = pd.Timestamp(timenow)
    else:
        lte = pd.Timestamp(lte)
    # # lte = lte-pd.Timedelta(1, 's')  # 往前一秒，因为es的时间范围是左右闭区间,这里减1s
    if gte == '':  # 查询起始时间
        gte = pd.Timestamp(lte-pd.Timedelta(30, 'd'))
    else:
        gte = pd.Timestamp(gte)
    if(debug == 'True'):  # 直接测试后面的内容
        gte = pd.Timestamp('2020-11-10 00:00:00')
        lte = gte+pd.Timedelta(20, 'd')
    data = getVolumeFromRealIndex(
        param['elasticsearch']["host"], param['elasticsearch']['real_index_name'], param['elasticsearch']["port"], gte, lte)
    table = pd.DataFrame(
        data, columns=["timestamp", "name", "coordinates", 'value'])
    table['timestamp'] = pd.to_datetime(
        table['timestamp'], unit='ms', utc=True).dt.tz_convert('Asia/Shanghai')
    if len(table) == 0:
        logging.logger.warning('数据量为0，请重新选择时间范围，这次不做预测')
        raise AssertionError
    # table.to_csv(os.path.join(Outputs.uri,'test.csv'),index=False)
    # print(table)
    DataFrameGrouptemp = table.groupby(['name'])
    # 首先建立hex值到经纬度的字典
    iddict_keys = DataFrameGrouptemp.groups.keys()
    iddict = {}
    # print(len(iddict_keys))
    # print(len(set(iddict_keys)))
    # for _hex in list(iddict_keys):
    #     # print(_hex)
    #     # print(DataFrameGrouptemp.get_group(_hex)["coordinates"].values[0])
    #     iddict[_hex] = DataFrameGrouptemp.get_group(
    #         _hex)["coordinates"].values[0]
   

    # 由于存在多个编号的门架共享一个hex值的情况，故按照hex进行分组

    hex_array_list = {}
    # 将分组的结果按照时间排序，便于后续的时间段流量计算
    for i in iddict_keys:
        hex_array_list[i] = DataFrameGrouptemp.get_group(
            i).sort_values('timestamp')
        iddict[i]=hex_array_list[i]["coordinates"].values[-1]#直接取最新的
        # print(hex_array_list[i]['计费交易时间'].unique())
    with open(os.path.join(examples.uri, 'hex_id.yaml'), mode='w') as f:
        yaml.dump(iddict, f, default_flow_style=False)
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
        start=dataframe.index[0], end=dataframe.index[-1], freq='h', tz="Asia/Shanghai"))
    dataframe = pd.concat([b, dataframe], axis=1)
    # print(dataframe)
    # 将缺失值用前后两个时间的数据的平均值做填充
    dataframe=((dataframe.ffill()+dataframe.bfill())/2).bfill().ffill()
    # 将缺失值用0填充
    #dataframe.fillna(0.0, inplace=True)
    dataframe = dataframe.astype('int')

    dataframe.to_csv(os.path.join(examples.uri, 'final_speed_per_60min.csv'))
    dataframe.to_hdf(os.path.join(
        examples.uri, 'final_speed_per_60min.h5'), key='data', mode='w')
    # writeTrueDataFrame(data, param['elasticsearch'],
    #                    list(data.columns), iddict)
    # 再来生成adj_mx_mydata.pkl文件
    # 取时间,为data的起点时间
    # timenow=pd.Timestamp.now().date()
    # print(dataframe)
    gtetime = dataframe.index[0]
    # print(data.index)
    gtetime = pd.Timestamp(gtetime)
    # print(gtetime)
    # 查询截止时间，往后一天
    ltetime = gtetime+pd.Timedelta(24, 'h')
    gen_adj_mx(es_index=param['elasticsearch']['row_records_index'], host=param['elasticsearch']["host"], port=param['elasticsearch']["port"], gte=gtetime,
               lte=ltetime, sensors=dataframe.columns, outputdir=examples.uri)

    # 把门架的id写进去
    # with open(os.path.join(param['flask']['output'],'graph_sensor_ids.txt'),'w') as f:
    #    f.writelines(','.join(sensors))
    #    f.close()
    return {
    }
