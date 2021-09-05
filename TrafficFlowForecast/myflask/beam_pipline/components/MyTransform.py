from tfx.dsl.component.experimental.annotations import OutputDict
from tfx.dsl.component.experimental.annotations import InputArtifact
from tfx.dsl.component.experimental.annotations import OutputArtifact
from tfx.dsl.component.experimental.annotations import Parameter
from tfx.dsl.component.experimental.decorators import component
from tfx.types.standard_artifacts import Examples
from tfx.types.standard_artifacts import Schema
#from tfx.types.standard_artifacts import 
#from myflask.es_op.data import getVolumeFromES
import pandas as pd
import os
import json
import yaml
import numpy as np
from pathlib import Path
from myflask.utils.util import generate_train_test

import shutil
@component
def MyTransform(
    examples:InputArtifact[Examples],
    schema:InputArtifact[Schema],
    transformed_examples:OutputArtifact[Examples],
    modelparam_path:Parameter[str]#存放模型配置{}_config.yaml的路径，需要划分训练集、验证集,读取data、model
    )->OutputDict(shape=str):
    
    #获取车流量速度的csv文件路径
    speed_dataframe_uri=os.path.join(examples.uri,'final_speed_per_60min.h5')
    datatype=schema.get_string_custom_property('type')
    xshape,yshape,modelconfig=generate_train_test(speed_dataframe_uri,modelparam_path,transformed_examples.uri,datatype)
    
    #将adj_mx.pkl文件拷贝过来，供后面的trainer使用
    shutil.copy(os.path.join(examples.uri,'adj_mx.pkl'),transformed_examples.uri)
    #hex_id.yaml文件拷贝过来，供后面的trainer使用
    shutil.copy(os.path.join(examples.uri,'hex_id.yaml'),transformed_examples.uri)
    #将sensor_ids.yaml文件拷贝过来，供后面的trainer使用
    shutil.copy(os.path.join(examples.uri,'sensor_ids.yaml'),transformed_examples.uri)
    #生成model.yaml
    with open(os.path.join(transformed_examples.uri,'model_final.yaml'),'w') as f:
        yaml.dump(modelconfig,f,default_flow_style=False)
    return {
        'shape':json.dumps(xshape)+json.dumps(yshape)
    }
