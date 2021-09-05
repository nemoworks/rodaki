from tfx.dsl.component.experimental.annotations import OutputDict
from tfx.dsl.component.experimental.annotations import InputArtifact
from tfx.dsl.component.experimental.annotations import OutputArtifact
from tfx.dsl.component.experimental.annotations import Parameter
from tfx.dsl.component.experimental.decorators import component
from tfx.types.standard_artifacts import Model
import os
import shutil
@component
def MyPusher(
    model:InputArtifact[Model],
    push_destination:Parameter[str])->OutputDict():
    src=os.path.join(model.uri,'serving_model_dir')
    if not os.path.exists(push_destination):
        os.mkdir(push_destination)
    src_folders = os.listdir(src)
    for folder_name in src_folders:
        full_file_name = os.path.join(src, folder_name)
        if os.path.isdir(full_file_name):
            shutil.copytree(full_file_name, os.path.join(push_destination,folder_name))
