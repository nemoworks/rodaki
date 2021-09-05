
import requests
import json

create_headers = {'content-type': "application/json", 'kbn-xsrf': 'true'}
delete_headers = {'kbn-xsrf': 'true'}
index_pattern_url = 'http://{}:{}/api/saved_objects/index-pattern/{}'
map_url = 'http://{}:{}/api/saved_objects/map/{}'


def check_index_pattern_name(index_pattern_name, host, port):
    response = requests.get(url=index_pattern_url.format(
        host, port, index_pattern_name))
    if(response.status_code == 200):
        return True
    elif(response.status_code == 404):
        return False
    else:
        return False


def create_index_pattern_name(index_pattern_name, host, port, es_index):
    response = requests.post(
        url=index_pattern_url.format(host, port, es_index),
        json={"attributes": {"title": index_pattern_name, "timeFieldName": "timestamp"}
              },
        headers=create_headers
    )
    if(response.status_code == 200):  # 创建成功
        return True
    elif(response.status_code == 409):  # 已存在
        return False
    return False


def delete_index_pattern_name(index_pattern_name, host, port):
    response = requests.delete(
        url=index_pattern_url.format(host, port, index_pattern_name),
        headers=delete_headers)
    if(response.status_code == 200):
        return True
    elif(response.status_code == 404):
        return False
    return False
