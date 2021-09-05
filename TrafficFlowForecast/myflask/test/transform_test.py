from myflask.es_op.transform_ops import delete_transform,check_transform,get_tranform_status,start_transform,stop_transform,create_transform
#print(check_transform('test','127.0.0.1',9200))
print(get_tranform_status('test1','127.0.0.1',9200))
#stop_transform('test','127.0.0.1',9200)
#start_transform('test','127.0.0.1',9200)
#print(get_tranform_status('test','127.0.0.1',9200))
# if not check_transform('test1','127.0.0.1',9200):
#     create_transform('test1',body={
#         "source": {
#             "index": [
#             "row_total_data"
#             ],
#             "query": {
#                 "bool": {
#                     "must_not": [
#                     {
#                         "match": {
#                         "备注": "仅备份"
#                         }
#                     }
#                     ]
#                 }
#             }
#         },
#         'frequency':'1h',
#         "dest": {
#             "index": "transform_test"
#         },
#         "sync": {
#             "time": {
#                 "field": "计费交易时间",
#                 "delay": "60s"
#             }
#         },
#         "pivot": {
#             "group_by": {
#                 "计费交易时间": {
#                     "date_histogram": {
#                     "field": "计费交易时间",
#                     "calendar_interval": "1h"
#                     }
#                 },
#                 "门架HEX值.keyword": {
#                     "terms": {
#                     "field": "门架HEX值.keyword"
#                     }
#                 }
#             },
#             "aggregations": {
#                 "value": {
#                     "value_count": {
#                         "field": "计费车牌号.keyword"
#                     }
#                 }
#             }
#         },
#     },host='127.0.0.1',port=9200)
#     start_transform('test1','127.0.0.1',9200)
# create_transform('test1',body={
#         "source": {
#             "index": [
#             "row_total_data"
#             ],
#             "query": {
#                 "bool": {
#                     "must_not": [
#                     {
#                         "match": {
#                         "备注": "仅备份"
#                         }
#                     }
#                     ]
#                 }
#             }
#         },
#         'frequency':'1h',
#         "dest": {
#             "index": "transform_test"
#         },
#         "sync": {
#             "time": {
#                 "field": "计费交易时间",
#                 "delay": "60s"
#             }
#         },
#         "pivot": {
#             "group_by": {
#                 "计费交易时间": {
#                     "date_histogram": {
#                     "field": "计费交易时间",
#                     "calendar_interval": "1h"
#                     }
#                 },
#                 "门架HEX值.keyword": {
#                     "terms": {
#                     "field": "门架HEX值.keyword"
#                     }
#                 }
#             },
#             "aggregations": {
#                 "value": {
#                     "value_count": {
#                         "field": "计费车牌号.keyword"
#                     }
#                 }
#             }
#         },
#     },host='127.0.0.1',port=9200)
# if stop_transform('test1','127.0.0.1',9200):
#     delete_transform('test1','127.0.0.1',9200)