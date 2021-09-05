from .index_ops import check_index_pattern_name, delete_index_pattern_name, create_index_pattern_name
from .map_ops import check_map, create_map, delete_map, add_layer


def init_index(flask_config):
    host = flask_config['kibana']['host']
    port = flask_config['kibana']['port']
    # kibana中与es真实车流量数据建立的index名称
    real_index_name = flask_config['kibana']['real_index_name']
    es_real_index_name = flask_config['elasticsearch']['real_index_name']
    assert real_index_name == es_real_index_name, 'kibana与es的index名称不一致'
    # 当real_index_name已经存在了，是否删除重建
    delete_real_index_if_exist = flask_config['kibana']['delete_real_index_if_exist']
    # kibana中与es预测车流量数据建立的index名称
    predict_index_name = flask_config['kibana']['predict_index_name']
    es_predict_index_name = flask_config['elasticsearch']['predict_index_name']
    assert predict_index_name == es_predict_index_name, 'kibana与es的index名称不一致'
    # 当predict_index_name已经存在了，是否删除重建
    delete_predict_index_if_exist = flask_config['kibana']['delete_predict_index_if_exist']
    # 匹配真实车流量的index pattern
    if(check_index_pattern_name(real_index_name, host, port)):
        # 说明存在了，来考虑是否要删除
        if delete_real_index_if_exist:
            print('删除kibna index pattern:', real_index_name)
            if delete_index_pattern_name(real_index_name, host, port):
                print('删除成功')
            else:
                print('删除失败')
            print("现在来重新创建index pattern")
            if create_index_pattern_name(real_index_name, host, port, es_real_index_name):
                print(real_index_name, '创建成功')
            else:
                print(real_index_name, '创建失败')
        else:
            print(real_index_name, 'index pattern已经存在，但是没有删除')
    else:
        # 说明不存在，现在需要创建
        print('本不存在 index pattern:', real_index_name)
        if create_index_pattern_name(real_index_name, host, port, es_real_index_name):
            print(real_index_name, '创建成功')
        else:
            print(real_index_name, '创建失败')
    # 匹配预测车流量的index pattern
    if(check_index_pattern_name(predict_index_name, host, port)):
        # 说明存在了，来考虑是否要删除
        if delete_predict_index_if_exist:
            print('删除kibna index pattern:', predict_index_name)
            if delete_index_pattern_name(predict_index_name, host, port):
                print('删除成功')
            else:
                print('删除失败')
            print("现在来重新创建index pattern")
            if create_index_pattern_name(predict_index_name, host, port, es_predict_index_name):
                print(predict_index_name, '创建成功')
            else:
                print(predict_index_name, '创建失败')
        else:
            print(predict_index_name, 'index pattern已经存在，但是没有删除')
    else:
        # 说明不存在，现在需要创建
        print('本不存在 index pattern:', predict_index_name)
        if create_index_pattern_name(predict_index_name, host, port, es_predict_index_name):
            print(predict_index_name, '创建成功')
        else:
            print(predict_index_name, '创建失败')


def init_map(flask_config):
    host = flask_config['kibana']['host']
    port = flask_config['kibana']['port']
    map_name = flask_config['kibana']['map_name']
    # kibana中与es真实车流量数据建立的index名称
    real_index_name = flask_config['kibana']['real_index_name']
    # kibana中与es预测车流量数据建立的index名称
    predict_index_name = flask_config['kibana']['predict_index_name']
    # 当地图已经存在了，是否删除重建
    delete_map_if_exist = flask_config['kibana']['delete_map_if_exist']
    map_title = flask_config['kibana']['map_title']
    if check_map(map_name, host, port):
        # 说明存在了，来考虑是否要删除
        if delete_map_if_exist:
            print('删除kibna map:', map_name)
            if delete_map(map_name, host, port):
                print('map:', map_name, '删除成功')
            else:
                print('map:', map_name, '删除失败')
            print("现在来重新创建map")
            if create_map(map_name, host, port):
                print(map_name, '创建成功')
                # 添加地图数据
                if add_layer(map_name, map_title, host, port, predict_index_name, real_index_name):
                    print('map:', map_name, '添加layer成功')
                else:
                    print('map:', map_name, '添加layer失败')
            else:
                print(map_name, '创建失败')

        else:
            print(map_name, 'map 已经存在，无需删除')
    else:
        # 说明不存在，现在需要创建
        print('本不存在 map:', map_name)
        if create_map(map_name, host, port):
            print(map_name, '创建成功')
            # 添加地图数据
            if add_layer(map_name, map_title, host, port, predict_index_name, real_index_name):
                print('map:', map_name, '添加layer成功')
            else:
                print('map:', map_name, '添加layer失败')
        else:
            print(map_name, '创建失败')


def kibana_init(flask_config):
    init_index(flask_config)
    init_map(flask_config)
