import requests
import numpy as np
create_headers = {'content-type': "application/json", 'kbn-xsrf': 'true'}
delete_headers = {'kbn-xsrf': 'true'}
map_url = 'http://{}:{}/api/saved_objects/map/{}'
map_layer_url = 'http://{}:{}/api/saved_objects/map/{}?overwrite=true'


def check_map(map_name, host, port):
    response = requests.get(url=map_url.format(host, port, map_name))
    if(response.status_code == 200):
        return True
    elif(response.status_code == 404):
        return False
    else:
        return False


def create_map(map_name, host, port):
    response = requests.post(
        url=map_url.format(host, port, map_name),
        json={"attributes": {"title": map_name}},
        headers=create_headers
    )
    if(response.status_code == 200):  # 创建成功
        return True
    elif(response.status_code == 409):  # 已存在
        return False
    else:
        return False


def delete_map(map_name, host, port):
    response = requests.delete(
        url=map_url.format(host, port, map_name),
        headers=delete_headers)
    if(response.status_code == 200):
        return True
    elif(response.status_code == 404):
        return False
    else:
        return False


def add_layer(map_name, map_title, host, port, predict_index_name, real_index_name):
    attr = np.load('myflask/assets/attr.npy', allow_pickle=True).tolist()
    attr['title'] = map_title
    # print(attr)
    ref = np.load('myflask/assets/ref.npy', allow_pickle=True).tolist()
    ref[0]['id'] = predict_index_name
    ref[1]['id'] = real_index_name
    ref[2]['id'] = real_index_name
    ref[3]['id'] = predict_index_name
    # print(ref)
    response = requests.put(url=map_layer_url.format(host, port, map_name),
                            json={"attributes": attr,
                                  'references': ref
                                  },
                            headers=create_headers)
    if(response.status_code == 200):
        return True
    else:
        return False
