from elasticsearch import Elasticsearch
#from elasticsearch.exceptions import NotFoundError,ConnectionError


def check_pipline(pipline_id, host, port):
    es = Elasticsearch([{'host': host, 'port': port}])

    try:
        res = es.ingest.get_pipeline(id=pipline_id)
        es.close()
        # print(res)
        return True
    except:
        # print('404')
        es.close()
        return False


def create_pipline(pipline_id, host, port):
    es = Elasticsearch([{'host': host, 'port': port}])
    body = {
        "description": "add a field for ingest timestamp",
        "processors": [
            {
                "set": {
                    "field": "received",
                    "value": r"{{_ingest.timestamp}}"
                }
            }
        ]

    }
    try:
        res = es.ingest.put_pipeline(pipline_id, body)
        # print(res)
        es.close()
        if res:
            return True
        else:
            return False
    except:
        es.close()
        return False


def delete_pipline(pipline_id, host, port):
    es = Elasticsearch([{'host': host, 'port': port}])
    try:
        res = es.ingest.delete_pipeline(pipline_id)
        # print(res)
        es.close()
        return True
    except:
        es.close()
        return True
    # print(res)
    es.close()
    if res['acknowledged']:
        return True
    else:
        return False
