import time
from typing import Dict, List
import threading
import json
import queue
import grequests
import sys
import simplejson
import csv_parser
import os


def get_csv_parser(sourcename: str, data_prefix):
    '''负责向队列里feed数据
    '''
    print('job_task:', sourcename)
    # share_queue.put('test:{}'.format(sourcename))

    filename = sourcename.split('.')[0]
    parser = None

    if filename in ['车道牌识数据utf8']:
        parser = csv_parser.CSV_parser(
            os.path.join(data_prefix, sourcename), '识别时间', "车道编码")
    elif filename in ['门架牌识数据utf8']:
        parser = csv_parser.CSV_parser(
            os.path.join(data_prefix, sourcename), '门架后台匹配时间',"门架编号")
    elif filename in ['出口车道数据']:
        parser = csv_parser.CSV_parser(
            os.path.join(data_prefix, sourcename), '压线圈时间', "出口车道HEX编码")
    elif filename in ['入口车道数据']:
        parser = csv_parser.CSV_parser(
            os.path.join(data_prefix, sourcename), '压线圈时间', "入口车道HEX编码")
    elif filename in ['门架计费扣费数据utf8']:
        parser = csv_parser.CSV_parser(
            os.path.join(data_prefix, sourcename), '计费交易时间',"门架HEX值")
    return parser


def main(data_source):
    """
    """

    http_sender = list(data_source['process'])
    parsers: Dict[str, csv_parser.CSV_parser] = {}
    for index in range(len(http_sender)):
        sourcename = http_sender[index]
        parsers[sourcename] = get_csv_parser(sourcename, data_source['prefix'])
    time_start = 0
    for parser in parsers.values():
        if time_start == 0:
            time_start = parser.data["timestamp"].iloc[0]
        else:
            if time_start > parser.data["timestamp"].iloc[0]:
                time_start = parser.data["timestamp"].iloc[0]  # 获取较小值
    for sourcename, parser in parsers.items():
        parser.generate_jmeter_txt_v2(
           data_source['savedir'], sourcename, "file-{}.txt".format(sourcename.split('.')[0]), time_start)
        #先把timestamp列删除，防止保存为json文件
        parser.data.drop(columns="timestamp",axis=1,inplace=True)
        parser.csvtojsonfiles(data_source['savedir'],sourcename)
        #parser.testtocsv(sourcename,'2020-06-16 13:05:00')
if __name__ == '__main__':
    data_source = {
        "prefix": "../datas/",
        "process": {
            "车道牌识数据utf8.csv": 1,
            "出口车道数据.csv": 1,
            "门架计费扣费数据utf8.csv": 1,
            "门架牌识数据utf8.csv": 1,
            "入口车道数据.csv": 1
        },  # 每个csv数据源的并发进程数目控制,模拟不同的地方同时上传数据
        "method": "POST",
        "savedir": '../cleaneddatas',
        "url": {
            "车道牌识数据utf8.csv": "http://127.0.0.1:8000/upload-json",
            "出口车道数据.csv": "http://127.0.0.1:8000/upload-json",
            "门架计费扣费数据utf8.csv": "http://127.0.0.1:8000/upload-json",
            "门架牌识数据utf8.csv": "http://127.0.0.1:8000/upload-json",
            "入口车道数据.csv": "http://127.0.0.1:8000/upload-json"
        },  # 每个csv数据源的上传的数据源应该被发往哪一个url
        "headers": {
            "Content-Type": "application/json",
        },
        "ConcurrentNumber": 200,  # 每个进程每次http请求的并发数目
        "scale": 0.1,  # 相邻两次并发请求之间的间隔缩放比例
    }

    main(data_source)
