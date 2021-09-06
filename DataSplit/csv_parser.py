import pandas as pd
import numpy as np
import os
import simplejson
from rich.progress import track


class CSV_parser():
    def __init__(self, data_source, time_col, groupby_name=None) -> None:
        print('start read csv:', data_source)
        self.data: pd.DataFrame = pd.read_csv(data_source, low_memory=False)
        print('finished')
        self.time_col = time_col
        self.groupby_names = None
        self.groupby_col = groupby_name
        self.clean(groupby_name)

    def clean(self, groupby_name):
        '''清洗数据
        '''
        print('start clean')
        int_cols = []
        float_cols = []
        str_col = []
        for col in self.data.columns:
            if self.data[col].dtype == np.int64:
                int_cols.append(col)
            elif self.data[col].dtype == np.floating:
                float_cols.append(col)
            else:
                str_col.append(col)
        drops = []
        # 获取时间无法解析的行
        for index, row in self.data.iterrows():
            try:
                int(pd.Timestamp(row[self.time_col]).timestamp())
            except:
                drops.append(index)
        self.data.drop(index=drops, inplace=True)
        # self.data[self.time_col] = self.data[self.time_col].apply(
        #     lambda x: int(pd.Timestamp(x).timestamp()))  # 单位：s
        # 不要改变原有的数据
        self.data['timestamp'] = self.data[self.time_col].apply(
            lambda x: int(pd.Timestamp(x).timestamp()))  # 单位：s
        self.data.sort_values(by='timestamp', inplace=True)
        self.data.reset_index(drop=True, inplace=True)
        print(self.data[groupby_name].isna().sum())
        self.data[groupby_name].fillna("NAN")  # 将为空的列用字符串填充
        #self.groupby_names = self.data.groupby(groupby_name, sort=False)
        self.groupby_names = self.data[groupby_name].unique().tolist()

        print("站点数目：", len(self.groupby_names))
        # for col in self.data.columns:
        #     if col in int_cols:
        #          self.data[col].fillna(value=0)
        # for col in self.data.columns:
        #     if self.data[col].dtype==np.int64:
        #         int_cols.append(col)
        #     elif self.data[col].dtype==np.floating:
        #         float_cols.append(col)
        #     else:
        #         str_col.append(col)
        print('clean finished')

    def csvtojsonfiles(self, datadir, sourcename):
        os.makedirs(os.path.join(
            datadir, sourcename.split('.')[0]), exist_ok=True)
        iterer = self.data.iterrows()
        for step in track(range(len(self.data))):
            index, row = next(iterer)
            simplejson.dump(row.to_dict(), open(os.path.join(
                datadir, sourcename.split('.')[0], '{}.json'.format(index)), 'w'), ensure_ascii=False,
                ignore_nan=True)

    # def csvtojsonfiles_v2(self, datadir, sourcename):
    #     os.makedirs(os.path.join(
    #         datadir, sourcename.split('.')[0]), exist_ok=True)
    #     iterer = self.data.iterrows()
    #     for step in track(range(len(self.data))):
    #         index, row = next(iterer)
    #         simplejson.dump(row.to_dict(), open(os.path.join(
    #             datadir, sourcename.split('.')[0], '{}.json'.format(index)), 'w'), ensure_ascii=False,
    #             ignore_nan=True)

    def generate_jmeter_txt(self, datadir, data_source, save_path, global_fast_timestamp,):
        os.makedirs(os.path.join(
            datadir), exist_ok=True)
        #last_timastamp = global_fast_timestamp
        batch_time = self.data[self.time_col].iloc[0]
        delay = batch_time-global_fast_timestamp
        txt_data = []
        index = 0
        for timestamp in self.data[self.time_col]:
            txt_data.append({
                'delay': timestamp-global_fast_timestamp,
                'file': os.path.join(datadir, data_source.split('.')[0], '{}.json'.format(index))
            })
            index += 1
        pd.DataFrame(data=txt_data, columns=['delay', 'file']).to_csv(
            os.path.join(datadir, save_path), index=False)

    def generate_jmeter_txt_v2(self, datadir, data_source, save_path, global_fast_timestamp,):

        os.makedirs(os.path.join(
            datadir, 'threadtxts', data_source.split('.')[0]), exist_ok=True)

        fileidx = 0
        txt_datas = {}
        index = 0
        for groupbyname in self.groupby_names:
            txt_datas[groupbyname] = []
        for index, row in self.data[["timestamp", self.groupby_col]].iterrows():
            txt_datas[row[self.groupby_col]].append({
                'delay': row["timestamp"]-global_fast_timestamp,
                'file': os.path.join(datadir, data_source.split('.')[0], '{}.json'.format(index))
            })
            index += 1
        threadidx = 1
        for key, txt_data in txt_datas.items():
            pd.DataFrame(data=txt_data, columns=['delay', 'file']).to_csv(
                os.path.join(datadir, 'threadtxts', data_source.split('.')[0], "{}.txt".format(threadidx)), index=False)
            threadidx += 1

    def testtocsv(self, data_source, timeend):
        # print(self.data[self.time_col])
        self.data[self.data[self.time_col] <= pd.Timestamp(timeend)].to_csv(
            "{}".format(data_source), index=False)
