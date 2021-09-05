from elasticsearch import Elasticsearch


def check_index_by_name(index_name, host, port):
    es = Elasticsearch([{'host': host, 'port': port}])
    # 查询一个index是否存在
    if es.indices.exists(index=index_name):
        print(index_name, "已存在于es里")
        es.close()
        return True
    else:
        es.close()
        return False


def delete_index_by_name(index_name, host, port):
    es = Elasticsearch([{'host': host, 'port': port}])
    try:
        print('es进行删除index:', index_name)
        es.indices.delete(index=index_name)
        # es.indices.refresh(index=index_name)
        print('es成功删除index:', index_name)
    except:
        print('删除失败')
    es.close()


def create_index_by_name(index_name, host, port):
    # 创建一个index
    es = Elasticsearch([{'host': host, 'port': port}])
    try:
        print('开始新建index')
        es.indices.create(index=index_name)
        es.indices.put_mapping(index=index_name,
                               body={'properties': {
                                   'coordinates': {'type': 'geo_point'},
                                   'timestamp': {'type': 'date'},
                                   'value': {'type': 'short'},
                                   'directionflag': {'type': 'short'},
                                   'name': {'type': 'keyword'}
                               }})
        # 添加一个空的，kibana要求index不为空
        es.index(index=index_name,
                 body={"value": None,
                       'name': None,
                       'coordinates': None,
                       "timestamp": None,
                       'directionflag':None}
                 )
        es.indices.refresh(index=index_name)
    except:
        print('创建失败')
    es.close()


def create_raw_index_by_name(index_name, host, port, pipeline):
    # 创建一个index
    es = Elasticsearch([{'host': host, 'port': port}])
    try:
        print('开始新建index')
        es.indices.create(index=index_name, body={

            "settings": {
                "index": {
                    "final_pipeline": pipeline,
                }
            }

        })
        es.indices.put_mapping(index=index_name,
                               body={
                                   'properties': {
                                       'gantryHex': {'type': 'keyword'},
                                       'gantryId': {'type': 'keyword'},
                                       'geo': {'type': 'float'},
                                       'lastGantryHex': {'type': 'keyword'},
                                       'transTime': {"type": "date"},
                                       'vehiclePlate': {
                                           "type": "keyword"
                                       }
                                   }
                               })
        es.indices.refresh(index=index_name)
    except:
        print('创建失败')
    es.close()
