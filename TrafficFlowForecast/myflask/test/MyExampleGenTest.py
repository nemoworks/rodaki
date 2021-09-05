from myflask.es_op.data import getVolumeFromRealIndex  # , writeTrueDataFrame
import pandas as pd
import os
import json
import yaml
import numpy as np
from pathlib import Path
from myflask.utils.util import get_flask_server_params
import pytest
from datetime import timezone, timedelta


def test_getVolumeFromRealIndex():
    _, _, _, param = get_flask_server_params()

    timenow = pd.Timestamp('2020-09-13 00:00:00').date()
    #timenow=pd.Timestamp('2020-07-11 22:00:00')
    # 查询起始时间
    # gte=pd.Timestamp(timenow)-pd.Timedelta(seq_len,'h')
    #gte=pd.Timestamp('2020-09-13 00:00:00')
    gte = pd.Timestamp(timenow)
    # 查询截止时间，程序运行当天的0点
    lte = gte+pd.Timedelta(3, 'h')
    # lte=pd.Timestamp(timenow)
    #lte=pd.Timestamp('2020-09-13 01:00:00')
    data = getVolumeFromRealIndex(
        param['elasticsearch']["host"], param['elasticsearch']['real_index_name'], param['elasticsearch']["port"], gte, lte)
    table = pd.DataFrame(
        data, columns=["timestamp", "name", "coordinates", 'value'])
    table['timestamp'] = pd.to_datetime(
        table['timestamp'], unit='ms', utc=True).dt.tz_convert('Asia/Shanghai')
    if len(table) == 0:
        #logging.logger.warning('数据量为0，请重新选择时间范围，这次不做预测')
        raise AssertionError
    # table.to_csv(os.path.join(Outputs.uri,'test.csv'),index=False)
    print(table)
    DataFrameGrouptemp = table.groupby(['name'])
    # 首先建立hex值到经纬度的字典
    iddict_keys = DataFrameGrouptemp.groups.keys()
    iddict = {}
    # print(len(iddict_keys))
    # print(len(set(iddict_keys)))
    for _hex in list(iddict_keys):
        # print(_hex)
        # print(DataFrameGrouptemp.get_group(_hex)["coordinates"].values[0])
        iddict[_hex] = DataFrameGrouptemp.get_group(
            _hex)["coordinates"].values[0]

    # print(iddict)

    hex_array_list = {}
    # 将分组的结果按照时间排序，便于后续的时间段流量计算
    for i in iddict_keys:
        hex_array_list[i] = DataFrameGrouptemp.get_group(
            i).sort_values('timestamp')
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
        start=dataframe.index[0], end=dataframe.index[-1], freq='h', tz="Asia/Shanghai"))
    dataframe = pd.concat([b, dataframe], axis=1)
    # print(dataframe)
    # 将缺失值用前后两个时间的数据的平均值做填充
    # dataframe=((dataframe.ffill()+dataframe.bfill())/2).bfill().ffill()
    # 将缺失值用0填充
    dataframe.fillna(0.0, inplace=True)
    dataframe = dataframe.astype('int')
    # print(dataframe)


test_getVolumeFromRealIndex()
