from myflask.es_op.data import getVolumeFromRealIndex_with_sensors
import pandas as pd
import numpy as np
 # 取时间,为程序运行当天的0点
timenow = pd.Timestamp.now().date()
# 查询起始时间
gte = pd.Timestamp('2020-11-10 00:00:00')
# 查询截止时间，程序运行当天的0点
lte = pd.Timestamp(timenow)
#读取hex与编号的字典
iddict=np.load('jupyters/results/hex_id.npy',allow_pickle=True).tolist()
sensor_hexs=list(iddict.keys())
predictdata = getVolumeFromRealIndex_with_sensors(
            '127.0.0.1','jxflask_predictvolume' ,32769 , sensor_hexs, gte, lte)
table = pd.DataFrame(
            predictdata, columns=["timestamp", "name", "coordinates", 'value'])
table['timestamp'] = pd.to_datetime(
    table['timestamp'], unit='ms', utc=True).dt.tz_convert('Asia/Shanghai')
DataFrameGrouptemp = table.groupby(['name'])
hex_array_list = {}
# 将分组的结果按照时间排序，便于后续的时间段流量计算
for i in DataFrameGrouptemp.groups.keys():
    hex_array_list[i] = DataFrameGrouptemp.get_group(
        i).sort_values('timestamp')
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
dataframe.to_csv('jupyters/results/predict_dataframe.csv')
# print(dataframe)
# 这一步是为了防止这段历史数据里，存在所有门架在中间某个小时nan的情况,这样的话，index就会缺失一个
# b = pd.DataFrame(index=pd.date_range(
#     start=gte,end=lte, freq='h', tz="Asia/Shanghai"))
# dataframe = pd.concat([b, dataframe], axis=1)
# dataframenew = pd.DataFrame(index=dataframe.index, columns=sensor_hexs)
# for i in dataframe.columns:
#     dataframenew[i] = dataframe[i].values
# # 将缺失值用0填充
# #dataframenew.fillna(0.0, inplace=True)
# # 将缺失值用前后两个时间的数据的平均值做填充
# dataframenew = (
#     (dataframenew.ffill()+dataframenew.bfill())/2).bfill().ffill()
# dataframenew.to_csv('jupyters/results/predict_dataframe.csv')