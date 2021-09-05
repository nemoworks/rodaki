from elasticsearch import Elasticsearch


def check_transform(transform_id, host, port):
    es = Elasticsearch([{'host': host, 'port': port}])
    # 查询一个transform是否存在
    try:
        req = es.transform.get_transform(transform_id=transform_id)
        # print(req)
        es.close()
        if req['count'] == 1:
            return True  # 此transform存在
        else:
            return False
    except:
        es.close()
        return False


def get_tranform_status(transform_id, host, port):
    '''
    若transform状态为stop，返回False、
    否则True
    404不存在也返回True
    '''
    es = Elasticsearch([{'host': host, 'port': port}])
    req = es.transform.get_transform_stats(transform_id=transform_id)
    # print(req)
    es.close()
    if req['count'] == 0:
        print('不存在')
        return True
    if 'stop' in req['transforms'][0]['state']:
        return False
    else:
        return True


def start_transform(transform_id, host, port):
    es = Elasticsearch([{'host': host, 'port': port}])
    try:
        req = es.transform.start_transform(transform_id=transform_id)
        print(req)
        es.close()
        if req['acknowledged']:
            return True
        else:
            return False
    except:  # 已经是started状态了
        return True


def stop_transform(transform_id, host, port):
    es = Elasticsearch([{'host': host, 'port': port}])
    try:
        req = es.transform.stop_transform(
            transform_id=transform_id, params={
                "force": "true"
            })
    except:
        print('404')
        es.close()
        return True
    # print(req)
    es.close()
    if req['acknowledged']:
        return True
    else:
        return False


def create_transform(transform_id, body, host, port):
    es = Elasticsearch([{'host': host, 'port': port}])
    try:
        req = es.transform.put_transform(transform_id=transform_id,
                                         body=body
                                         )
        if req['acknowledged']:
            es.close()
            return True
        else:
            return False

    except:
        es.close()
        return True


def delete_transform(transform_id, host, port):
    es = Elasticsearch([{'host': host, 'port': port}])
    try:
        req = es.transform.delete_transform(transform_id=transform_id, params={
            "force": "true"
        })
    except:
        print('404')
        es.close()
        return True
    print(req)
    es.close()
    if req['acknowledged']:
        return True
    else:
        return False
