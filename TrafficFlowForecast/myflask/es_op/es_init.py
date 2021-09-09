from .index_ops import check_index_by_name, create_index_by_name, delete_index_by_name, create_raw_index_by_name
from .transform_ops import *
from .pipline_ops import *
import myflask.es_op.field as field
from myflask.utils.logger import logger
import sys


def es_init(flask_config):
    real_index_name = flask_config['elasticsearch']['real_index_name']
    predict_index_name = flask_config['elasticsearch']['predict_index_name']
    row_records_index = flask_config['elasticsearch']['row_records_index']
    host = flask_config['elasticsearch']['host']
    port = flask_config['elasticsearch']['port']
    delete_row_records_index_if_exist = flask_config['elasticsearch']['delete_row_records_index_if_exist']
    delete_real_index_if_exist = flask_config['elasticsearch']['delete_real_index_if_exist']
    delete_predict_index_if_exist = flask_config['elasticsearch']['delete_predict_index_if_exist']
    pipeline_id = flask_config['elasticsearch']['pipeline_id']

    es_init_pipline(flask_config)
    # 存放收费记录的index
    if(check_index_by_name(row_records_index, host, port)):
        # 说明存在了，来考虑是否要删除
        if delete_row_records_index_if_exist:
            print('删除es index:', row_records_index)
            delete_index_by_name(row_records_index, host, port)
            print("现在来重新创建index")
            create_raw_index_by_name(
                row_records_index, host, port, pipeline_id)
        else:
            print(row_records_index, '已经存在，但是没有删除')
    else:
        # 说明不存在，现在需要创建
        print('本不存在 index:', row_records_index)
        create_raw_index_by_name(row_records_index, host, port, pipeline_id)
    # 存放真实车流量的index
    if(check_index_by_name(real_index_name, host, port)):
        # 说明存在了，来考虑是否要删除
        if delete_real_index_if_exist:
            print('删除es index:', real_index_name)
            delete_index_by_name(real_index_name, host, port)
            print("现在来重新创建index")
            create_index_by_name(real_index_name, host, port)
        else:
            print(real_index_name, '已经存在，但是没有删除')
    else:
        # 说明不存在，现在需要创建
        print('本不存在 index:', real_index_name)
        create_index_by_name(real_index_name, host, port)
    # 存放预测车流量的index
    if(check_index_by_name(predict_index_name, host, port)):
        # 说明存在了，来考虑是否要删除
        if delete_predict_index_if_exist:
            print('删除es index:', predict_index_name)
            delete_index_by_name(predict_index_name, host, port)
            print("现在来重新创建index")
            create_index_by_name(predict_index_name, host, port)
        else:
            print(predict_index_name, '已经存在，但是没有删除')
    else:
        # 说明不存在，现在需要创建
        print('本不存在 index:', predict_index_name)
        create_index_by_name(predict_index_name, host, port)
    #真实车流量transfomer
    es_init_transform(flask_config)


def es_init_data_stream(flask_config):
    real_index_name = flask_config['elasticsearch']['real_index_name']
    predict_index_name = flask_config['elasticsearch']['predict_index_name']
    host = flask_config['elasticsearch']['host']
    port = flask_config['elasticsearch']['port']
    delete_real_index_if_exist = flask_config['elasticsearch']['delete_real_index_if_exist']
    delete_predict_index_if_exist = flask_config['elasticsearch']['delete_predict_index_if_exist']


def es_init_pipline(flask_config):
    host = flask_config['elasticsearch']['host']
    port = flask_config['elasticsearch']['port']
    pipeline_id = flask_config['elasticsearch']['pipeline_id']
    delete_pipeline_if_exist = flask_config['elasticsearch']['delete_pipeline_if_exist']
    if not check_pipline(pipeline_id, host, port):  # 不存在
        logger.info("开始创建pipline")
        if create_pipline(pipeline_id, host, port):
            logger.info("pipline创建成功")
        else:
            logger.error("pipline创建失败")
            sys.exit(1)
    else:
        if delete_pipeline_if_exist:
            logger.info("开始删除pipline，再重建")
            if delete_pipline(pipeline_id, host, port):
                logger.info("成功删除pipline，再重建")
                if create_pipline(pipeline_id, host, port):
                    logger.info("pipline创建成功")
                else:
                    logger.error("pipline创建失败")
                    sys.exit(1)
            else:
                logger.error("删除pipline失败")


def es_init_transform(flask_config):
    real_index_name = flask_config['elasticsearch']['real_index_name']
    row_records_index = flask_config['elasticsearch']['row_records_index']
    host = flask_config['elasticsearch']['host']
    port = flask_config['elasticsearch']['port']
    transform_id = flask_config['elasticsearch']['transform_id']
    transform_frequency = flask_config['elasticsearch']['transform_frequency']
    transform_delay = flask_config['elasticsearch']['transform_delay']
    delete_transform_if_exist = flask_config['elasticsearch']['delete_transform_if_exist']
    body = {
        "source": {
            "index": [
                row_records_index
            ],
            "query": {
                'bool': {
                    "must": [
                        {
                            "exists": {
                                "field": field.gantryHex
                            },

                        },
                        {
                            "exists": {
                                "field": field.transTime
                            },

                        },
                        {
                            "exists": {
                                "field": field.geo
                            },

                        },
                    ],
                }

            }
            # "query": {
            #     "bool": {
            #         "must_not": [
            #             # {#生产环境删除
            #             #     "match": {
            #             #     "备注": "仅备份"
            #             #     }
            #             # }
            #         ]
            #     }
            # }
        },
        'frequency': transform_frequency,
        "dest": {
            "index": real_index_name
        },
        "sync": {
            "time": {
                "field": 'received',
                # "field": field.transTime,
                "delay": transform_delay
            }
        },
        "pivot": {
            "group_by": {
                "timestamp": {
                    # "date_histogram": {
                    #     "field": field.transTime,
                    #     "calendar_interval": "1h",
                    #     "time_zone": "+08:00"
                    #     }
                    "terms": {
                        "script": {
                            "source": """
                                    ZonedDateTime date=doc['transTime'].value ;
                                    LocalDateTime local=date.toLocalDateTime();
                                    date=ZonedDateTime.of(local,ZoneId.of('Asia/Shanghai'));
                                    long millis=date.toInstant().toEpochMilli();
                                    long interval= Duration.ofHours(1).toMillis();
                                    return (Math.floor(millis / interval) * interval).longValue()
                                    """,
                            "params": {
                                "calendar_interval": "1h"
                            }
                        }

                    }
                },
                "name": {
                    "terms": {
                        "field": field.gantryHex
                    }
                }
            },
            "aggregations": {
                "totalcount": {  # 进行某一hex的下有多个门架id的情况的矫正
                    "value_count": {  # 计算这个hex下的记录数目
                        "field": field.gantryHex
                    }
                },
                "idcount": {  # 统计这个hex下有多少个不同门架id
                    "cardinality": {
                        "field": field.gantryId
                    }
                },
                "directionflag": {  # 门架的上下行标志，1或者2
                   "scripted_metric": {
                        "init_script": "state.last_value =1",
                        "map_script": "state.last_value = Short.parseShort(doc['gantryId'].value.charAt(14).toString());",
                        "combine_script": "return state",
                        "reduce_script": " def last_value = 1;for (s in states){last_value = s.last_value};return last_value"
                     }
                },
                "value": {
                    "bucket_script": {
                        "buckets_path": {
                            "totalcount": "totalcount.value",
                            "idcount": "idcount.value"
                        },
                        "script": "(short)(params.totalcount / params.idcount)"
                    }
                },
                "coordinates": {  # 虽然webflux那边是经度在前，纬度在后；但是这里取出来是经度在后，就要颠倒一下
                    "scripted_metric": {
                        "init_script": "state.a=0.0;state.b=0.0",
                        "map_script": "state.a=doc['geo'][1].floatValue();state.b=doc['geo'][0].floatValue()",
                        "combine_script": "float []profit =new float[2]; profit[0]=state.a;profit[1]=state.b;return profit",
                        "reduce_script": "float []profit =new float[2];profit[0]=0; profit[1]=0;for(s in states){profit=s}return  profit                            "
                    }
                }

            }
        },
    }
    if not check_transform(transform_id, host, port):  # 不存在
        logger.info("开始创建transform")
        if create_transform(transform_id, body, host, port):
            logger.info("transform创建成功")
            logger.info("启动transform")
            start_transform(transform_id, host, port)
        else:
            logger.error("transform创建失败")
            sys.exit(1)
    else:
        logger.info("transform已经存在")
        if delete_transform_if_exist:
            logger.info("开始删除transform，再重建")
            stop_transform(transform_id, host, port)
            if delete_transform(transform_id, host, port):
                logger.info("成功删除transform，再重建")
                if create_transform(transform_id, body, host, port):
                    logger.info("transform创建成功")
                    logger.info("启动transform")
                    start_transform(transform_id, host, port)
                else:
                    logger.error("transform创建失败")
                    sys.exit(1)
            else:
                logger.error("删除transform失败")
        else:
            logger.info("transform已经存在,选择不重建")
            start_transform(transform_id, host, port)
