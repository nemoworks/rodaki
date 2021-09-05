from tfx.dsl.component.experimental.annotations import OutputDict
from tfx.dsl.component.experimental.annotations import InputArtifact
from tfx.dsl.component.experimental.annotations import OutputArtifact
from tfx.dsl.component.experimental.annotations import Parameter
from tfx.dsl.component.experimental.decorators import component
from tfx.types.standard_artifacts import Examples
from tfx.types.standard_artifacts import Schema
from myflask.es_op.data import getVolumeFromES
import pandas as pd
import os
import json
import numpy as np
from pathlib import Path
@component
def MySchema(
    examples:InputArtifact[Examples],
    schema:OutputArtifact[Schema],
    debug:Parameter[str]='False'
    )->OutputDict():
    
    schema.set_string_custom_property('type','float32')
    if(debug=='True'):#直接测试后面的内容
        pass
    return {
        
    }