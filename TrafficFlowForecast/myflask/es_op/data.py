from datetime import datetime
from elasticsearch import Elasticsearch
import pandas as pd
import numpy as np
import os
from myflask.utils.showProcess import ShowProcess
import myflask.es_op.field as field
import myflask.utils.logger as logging
import datetime
from elasticsearch import helpers
epoch = datetime.datetime.utcfromtimestamp(0,)


def unix_time_millis(dt):
    return int((dt - epoch).total_seconds() * 1000)


def writeTrueDataFrame(true_dataframe, es_config, sensor_hexs, iddict):
    # es = Elasticsearch('localhost:'+str(args.es_port))
    es = Elasticsearch([{'host': es_config['host'], 'port':es_config['port']}])
    # 读取门架经纬度
    location = pd.read_excel(os.path.join(
        'myflask/assets', '门架经纬度.xlsx'), index_col=0)
    location.set_index(['收费门架编号'], inplace=True)
    # print(location.index)

    # 先把与真实数据在时间上有重复的数据删除
    es.delete_by_query(index=es_config['real_index_name'],
                       body={
        "query": {
            'bool': {
                "must": [
                    {
                        'terms': {
                            'name': sensor_hexs,
                        }
                    }

                ],
                "filter": [
                    {
                        "range": {
                            'timestamp': {
                                'gte': true_dataframe.index[0].tz_localize('Asia/Shanghai'),
                                'lte':true_dataframe.index[-1].tz_localize('Asia/Shanghai')
                            }
                        }
                    }
                ],
            }
        }
    })
    # 添加数据（document）
    # process_bar = ShowProcess(len(predict_dataframe),'预测数据写入es完成' )
    for index, row in true_dataframe.iterrows():
        # print('进度：{}/{}'.format(cur,num))
        # print(index)
        # process_bar.show_process()
        for col in true_dataframe.columns:

            es.index(index=es_config['real_index_name'],
                     body={"value": row[col],
                           'name': col,
                           'coordinates': [location.loc[iddict[col]]['经度'], location.loc[iddict[col]]['纬度']],
                           "timestamp": index.tz_localize('Asia/Shanghai').to_pydatetime()}
                     )
        # cur=cur+1
    # 手动刷新
    # process_bar.close()
    try:
        es.indices.refresh(index=es_config['real_index_name'])
    except:
        es.close()
        logging.logger.error('es index刷新失败')
        return
    # print('es数据写入完成')
    es.close()


def writePredictDataFrame(predict_dataframe, es_config, sensor_hexs, iddict, hex_direction:dict={}):
    # es = Elasticsearch('localhost:'+str(args.es_port))
    # 因为传过来的是float类型的dataframe，这里取整
    predict_dataframe = predict_dataframe.round(0)
    predict_dataframe = predict_dataframe.astype('int')
    es = Elasticsearch([{'host': es_config['host'], 'port':es_config['port']}])
    # 读取门架经纬度
    # location = pd.read_excel(os.path.join(
    #     'myflask/assets', '门架经纬度.xlsx'), index_col=0)
    # location.set_index(['收费门架编号'], inplace=True)
    # print(location.index)

    # 先把与预测结果在时间上有重复的预测结果删除
    es.delete_by_query(index=es_config['predict_index_name'],
                       body={
        "query": {
            'bool': {
                "must": [
                    {
                        'terms': {
                            'name': sensor_hexs,
                        }
                    }

                ],
                "filter": [
                    {
                        "range": {
                            'timestamp': {
                                'gte': predict_dataframe.index[0],
                                'lte':predict_dataframe.index[-1]
                            }
                        }
                    }
                ],
            }
        }
    })
    es.indices.refresh(index=es_config['predict_index_name'])
    maxnum = 200  # 一次最多写两百个
    tempdata = []
    # 添加数据（document）
    # process_bar = ShowProcess(len(predict_dataframe),'预测数据写入es完成' )
    for index, row in predict_dataframe.iterrows():
        # print('进度：{}/{}'.format(cur,num))
        # print(index)
        # process_bar.show_process()
        for col in predict_dataframe.columns:
            tempdata.append({
                "_index": es_config['predict_index_name'],
                "value": row[col],
                'name': col,
                # 经度，纬度
                'coordinates': [iddict[col][0], iddict[col][1]],
                "timestamp": (unix_time_millis(index.tz_convert("UTC").tz_localize(None).to_pydatetime())),
                'directionflag':hex_direction.get(col,1)
                })
            if(len(tempdata) == maxnum):
                logging.logger.info("写入 {} 个预测数据".format(maxnum))
                helpers.bulk(client=es, actions=tempdata, refresh=True)
                tempdata = []
        # cur=cur+1
    helpers.bulk(client=es, actions=tempdata, refresh=True)
    # 手动刷新
    # process_bar.close()
    try:
        es.indices.refresh(index=es_config['predict_index_name'])
    except:
        es.close()
        logging.logger.error('es index刷新失败')
        return
    # print('es数据写入完成')
    es.close()


def getVolumeFromRealIndex_with_sensors(host, index_name, port, sensor_hexs, gte=None, lte=None):
    es = Elasticsearch([{'host': host, 'port': port}])
    lte = lte-pd.Timedelta(1, 's')  # 往前一秒，因为es的时间范围是左右闭区间,这里减1s
    req = es.search(index=index_name,
                    body={
                        'size': 2000,
                        "sort": [
                        ],

                        'query': {
                            'bool': {
                                "must": [
                                    {
                                        'terms': {
                                            'name': sensor_hexs,
                                        }
                                    }
                                ],
                                'must_not': [

                                ],
                                "filter": [
                                    {
                                        "range": {
                                            'timestamp': {
                                                "gte": gte.tz_localize('Asia/Shanghai'),
                                                "lte": lte.tz_localize('Asia/Shanghai')
                                            }
                                        }
                                    }
                                ],
                                "should": [],
                            }
                        }


                    },
                    scroll='1m'
                    )
    data = req['hits']['hits']
    while len(req['hits']['hits']):
        # logging.logger.info(len(req['hits']['hits']))
        req = es.scroll(scroll_id=req['_scroll_id'], scroll='1m')
        # print(req)
        data.extend(req['hits']['hits'])
    data = list(map(lambda x: {**x['_source']}, data))
    es.close()
    # print(data)
    return data


def getVolumeFromRealIndex(host, index_name, port,  gte=None, lte=None):
    es = Elasticsearch([{'host': host, 'port': port}])
    lte = lte-pd.Timedelta(1, 's')  # 往前一秒，因为es的时间范围是左右闭区间,这里减1s
    req = es.search(index=index_name,
                    body={
                        'size': 2000,  # 每次最多拿2000个
                        "sort": [
                        ],

                        'query': {
                            'bool': {
                                "must": [

                                ],
                                'must_not': [

                                ],
                                "filter": [
                                    {
                                        "range": {
                                            'timestamp': {
                                                "gte": gte.tz_localize('Asia/Shanghai'),
                                                "lte": lte.tz_localize('Asia/Shanghai')
                                            }
                                        }
                                    }
                                ],
                                "should": [],
                            }
                        }


                    },
                    scroll='1m'
                    )
    data = req['hits']['hits']
    while len(req['hits']['hits']):
        # logging.logger.info(len(req['hits']['hits']))
        req = es.scroll(scroll_id=req['_scroll_id'], scroll='1m')
        # print(req)
        data.extend(req['hits']['hits'])
    data = list(map(lambda x: {**x['_source']}, data))
    es.close()
    # print(data)
    return data


def getVolumeFromES(host, index_name, port, gte=None, lte=None):
    es = Elasticsearch([{'host': host, 'port': port}])
    # cols=','.join(usecols)
    # print(cols)
    # 取时间,为程序运行当天的0点
    # timenow=pd.Timestamp.now().date()
    timenow = pd.Timestamp('2020-07-21')
    # print(timenow)
    if lte == '':  # 查询截止时间
        lte = pd.Timestamp(timenow)
    else:
        lte = pd.Timestamp(lte)
    lte = lte-pd.Timedelta(1, 's')  # 往前一秒，因为es的时间范围是左右闭区间,这里减1s
    if gte == '':  # 查询起始时间
        gte = pd.Timestamp(lte-pd.Timedelta(30, 'd'))+pd.Timedelta(1, 's')
    else:
        gte = pd.Timestamp(gte)

    req = es.search(index=index_name,
                    body={
                        'size': 0,
                        "sort": [
                        ],
                        'aggs': {
                            "my_buckets": {
                                "composite": {
                                    "size": 1000,
                                    "sources": [
                                        {"计费交易时间": {"date_histogram": {
                                            "field": field.transTime, "calendar_interval": "1h", }}},
                                        {"门架HEX值": {
                                            "terms": {"field": field.gantryHex, }}},
                                        {"门架编号": {"terms": {"field": field.gantryId, }}}
                                    ]
                                }
                            }
                        },
                        'query': {
                            'bool': {
                                "must": [

                                ],
                                'must_not': [
                                    {  # 这里的要在正式环境里删除
                                        # 'match':{
                                        #     '备注':'仅备份'
                                        # }
                                    }
                                ],
                                "filter": [

                                    {
                                        "range": {
                                            field.transTime: {
                                                "gte": gte,
                                                "lte": lte
                                            }
                                        }
                                    }
                                ],
                                "should": [],
                            }
                        }


                    },
                    )
    data = req['aggregations']['my_buckets']['buckets']
    while req['aggregations']['my_buckets'].get('after_key'):
        # logging.logger.info(req['aggregations']['my_buckets']['after_key'])
        # print(req['aggregations']['my_buckets']['after_key'])
        req = es.search(index=index_name,
                        body={
                            'size': 0,
                            "sort": [
                            ],
                            'aggs': {
                                "my_buckets": {
                                    "composite": {
                                        "size": 1000,
                                        "sources": [
                                            {"计费交易时间": {"date_histogram": {
                                                "field": field.transTime, "calendar_interval": "1h", }}},
                                            {"门架HEX值": {
                                                "terms": {"field": field.gantryHex, }}},
                                            {"门架编号": {
                                                "terms": {"field": field.gantryId, }}}
                                        ],
                                        "after": req['aggregations']['my_buckets']['after_key']
                                    }
                                }
                            },
                            'query': {
                                'bool': {
                                    "must": [

                                    ],
                                    'must_not': [
                                        {
                                            'match': {
                                                '备注': '仅备份'
                                            }
                                        }
                                    ],
                                    "filter": [

                                        {
                                            "range": {
                                                field.transTime: {
                                                    "gte": gte,
                                                    "lte": lte
                                                }
                                            }
                                        }
                                    ],
                                    "should": [],
                                }
                            }


                        },
                        )
        # print(req)
        data.extend(req['aggregations']['my_buckets']['buckets'])
    data = list(map(lambda x: {**x['key'], 'doc_count': x['doc_count']}, data))
    es.close()
    # print(data)
    return data


def getVolumeFromES_sensor(host, index_name, port, sensor_ids, gte, lte):
    es = Elasticsearch([{'host': host, 'port': port}])
    if gte is None or lte is None:
        logging.logger.error('预测时获取历史数据的时间不能为None')
        raise AssertionError
    lte = lte-pd.Timedelta(1, 's')

    req = es.search(index=index_name,
                    body={
                        'size': 0,
                        "sort": [
                        ],
                        'aggs': {
                            "my_buckets": {
                                "composite": {
                                    "size": 1000,
                                    "sources": [
                                        {"计费交易时间": {"date_histogram": {
                                            "field": field.transTime, "calendar_interval": "1h", }}},
                                        {"门架HEX值": {
                                            "terms": {"field": field.gantryHex, }}},
                                        {"门架编号": {"terms": {"field": field.gantryId, }}}
                                    ]
                                }
                            }
                        },
                        'query': {
                            'bool': {
                                "must": [
                                    {
                                        'terms': {
                                            field.gantryHex: sensor_ids,
                                        }
                                    }
                                ],
                                'must_not': [
                                    {  # 这里的要在正式环境里删除
                                        'match': {
                                            '备注': '仅备份'
                                        }
                                    }
                                ],
                                "filter": [

                                    {
                                        "range": {
                                            field.transTime: {
                                                "gte": gte,
                                                "lte": lte
                                            }
                                        }
                                    }
                                ],
                                "should": [],
                            }
                        }


                    },
                    )
    data = req['aggregations']['my_buckets']['buckets']
    while req['aggregations']['my_buckets'].get('after_key'):
        print(req['aggregations']['my_buckets']['after_key'])
        # print(req['aggregations']['my_buckets']['after_key'])
        req = es.search(index=index_name,
                        body={
                            'size': 0,
                            "sort": [
                            ],
                            'aggs': {
                                "my_buckets": {
                                    "composite": {
                                        "size": 1000,
                                        "sources": [
                                            {"计费交易时间": {"date_histogram": {
                                                "field": field.transTime, "calendar_interval": "1h", }}},
                                            {"门架HEX值": {
                                                "terms": {"field": field.gantryHex, }}},
                                            {"门架编号": {
                                                "terms": {"field": field.gantryId, }}}
                                        ],
                                        "after": req['aggregations']['my_buckets']['after_key']
                                    }
                                }
                            },
                            'query': {
                                'bool': {
                                    "must": [
                                        {
                                            'terms': {
                                                field.gantryHex: sensor_ids,
                                            }
                                        }
                                    ],
                                    'must_not': [
                                        {  # 这里的要在正式环境里删除
                                            'match': {
                                                '备注': '仅备份'
                                            }
                                        }
                                    ],
                                    "filter": [

                                        {
                                            "range": {
                                                field.transTime: {
                                                    "gte": gte,
                                                    "lte": lte
                                                }
                                            }
                                        }
                                    ],
                                    "should": [],
                                }
                            }


                        },
                        )
        # print(req)
        data.extend(req['aggregations']['my_buckets']['buckets'])
    data = list(map(lambda x: {**x['key'], 'doc_count': x['doc_count']}, data))
    es.close()
    return data


def getDistanceResultFromES(host, index_name, port, sensors, gte=None, lte=None):
    # es=Elasticsearch([{'host':'127.0.0.1','port':port}])
    es = Elasticsearch([{'host': host, 'port': port}])
    # cols=','.join(usecols)
    # print(cols)
    lte = lte-pd.Timedelta(1, 's')
    # logging.logger.info(gte, lte)
    req = es.search(index=index_name,
                    body={
                        'size': 0,
                        "sort": [
                        ],
                        'aggs': {
                            "my_buckets": {
                                "composite": {
                                    "size": 1000,
                                    "sources": [
                                        {"门架HEX值": {
                                            "terms": {"field": field.gantryHex, }}},
                                        {"上个门架hex编码": {
                                            "terms": {"field": field.lastGantryHex, }}}
                                    ]
                                }
                            }
                        },
                        'query': {
                            'bool': {
                                "must": [
                                    {
                                        "exists": {
                                            "field": field.lastGantryHex
                                        },

                                    },
                                    {
                                        'terms': {
                                            field.lastGantryHex: sensors,
                                        }
                                    }

                                ],
                                'must_not': [
                                    # {  # 这里的要在正式环境里删除
                                    #     # 'match': {
                                    #     #     '备注': '仅备份'
                                    #     # }
                                    # }
                                ],
                                "filter": [

                                    {
                                        "range": {
                                            field.transTime: {
                                                "gte": gte,
                                                "lte": lte
                                            }
                                        }
                                    }
                                ],
                                "should": [],
                            }
                        }


                    },
                    )
    data = req['aggregations']['my_buckets']['buckets']
    while req['aggregations']['my_buckets'].get('after_key'):
        # print(req['aggregations']['my_buckets']['after_key'])
        # print(req['aggregations']['my_buckets']['after_key'])
        req = es.search(index=index_name,
                        body={
                            'size': 0,
                            "sort": [
                            ],

                            'aggs': {
                                "my_buckets": {
                                    "composite": {
                                        "size": 1000,
                                        "sources": [
                                            {"门架HEX值": {
                                                "terms": {"field": field.gantryHex, }}},
                                            {"上个门架hex编码": {
                                                "terms": {"field": field.lastGantryHex, }}}
                                        ],
                                        "after": req['aggregations']['my_buckets']['after_key']
                                    }
                                }
                            },
                            'query': {
                                'bool': {
                                    "must": [
                                        {
                                            "exists": {
                                                "field": field.lastGantryHex
                                            }
                                        },
                                        {
                                            'terms': {
                                                field.lastGantryHex: sensors,
                                            }
                                        }

                                    ],
                                    'must_not': [
                                        # {  # 这里的要在正式环境里删除
                                        #     # 'match': {
                                        #     #     '备注': '仅备份'
                                        #     # }
                                        # }
                                    ],
                                    "filter": [

                                        {
                                            "range": {
                                                field.transTime: {
                                                    "gte": gte,
                                                    "lte": lte
                                                }
                                            }
                                        }
                                    ],
                                    "should": [],
                                }
                            }


                        },
                        )
        # print(req)
        data.extend(req['aggregations']['my_buckets']['buckets'])
    data = list(map(lambda x: {**x['key'], 'doc_count': x['doc_count']}, data))
    es.close()
    return data


def cal_distance_es(sensors, gte, lte, index_name, host, port):
    # usecols=['门架编号','门架HEX值','计费交易时间','计费车牌号','计费车型','上个门架hex编码','通过上个门架的时间']
    # usecols=['门架编号','门架HEX值','计费交易时间','备注','上个门架hex编码']
    # port=9200
    # index_name='row_total_data'
    # usecols=['门架HEX值','计费交易时间','备注','上个门架hex编码']
    # gte=pd.Timestamp('2020-07-11 0:00:00')
    # lte=pd.Timestamp('2020-07-12 0:00:00')-pd.Timedelta(1,'s')
    data_list = getDistanceResultFromES(
        host, index_name, port, sensors, gte, lte)
    sensor_id_to_ind = {}
    for i, sensor_id in enumerate(sensors):
        sensor_id_to_ind[sensor_id] = i
    data_part_related = pd.DataFrame(
        data_list, columns=['门架HEX值', '上个门架hex编码', 'doc_count'])
    # assert len(data_part_related)>0,'数据量为0，请重新选择时间范围!'
    hex_group = data_part_related.groupby(['上个门架hex编码'], sort=False)
    hex_group_indexes = list(hex_group.groups.keys())
    dist_mx = np.eye(len(sensors), dtype=np.float32)  # 生成一个对角矩阵
    for index, hex_id in enumerate(hex_group_indexes):
        value_count = pd.Series(index=hex_group.get_group(
            hex_id)['门架HEX值'].values, data=hex_group.get_group(hex_id)['doc_count'].values)
        value_count_sum = hex_group.get_group(hex_id)['doc_count'].sum()
        rate_dict = (value_count/value_count.sum()).astype('float32').to_dict()
        for (key, value) in rate_dict.items():
            # logging.logger.info('{}-{}:{}'.format(hex_id, key, value))
            # 防止出现hex不是sensor_id_to_ind里面的，因为上个门架hex编码这一字段肯定在里面
            # 若key==hex_id，也要忽略,这样即使没有数据，也能得到一个对角矩阵
            if sensor_id_to_ind.get(key) is None or key == hex_id:
                continue
            dist_mx[sensor_id_to_ind[hex_id], sensor_id_to_ind[key]] = value
    return sensors, sensor_id_to_ind, pd.DataFrame(dist_mx, columns=sensors, index=sensors).values
