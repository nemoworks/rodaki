import os
from typing import List, Text, Dict
import json
import absl
import tensorflow_model_analysis as tfma

from myflask.beam_pipline.components.MyExampleGen import MyExampleGen
from myflask.beam_pipline.components.MySchema import MySchema
from myflask.beam_pipline.components.MyTransform import MyTransform
from myflask.beam_pipline.components.MyTrainerExcutor import Executor
from myflask.beam_pipline.components.MyPusher import MyPusher
from tfx.components import Evaluator

from tfx.components import ResolverNode
from tfx.components import Trainer


from tfx.dsl.experimental import latest_blessed_model_resolver
from tfx.orchestration import metadata
from tfx.orchestration import pipeline
from tfx.orchestration.beam.beam_dag_runner import BeamDagRunner
from tfx.proto import pusher_pb2
from tfx.proto import trainer_pb2
from tfx.types import Channel
from tfx.types.standard_artifacts import Model
from tfx.types.standard_artifacts import ModelBlessing
from tfx.utils.dsl_utils import external_input
from tfx.components.base import executor_spec


def _create_pipeline(pipeline_name: Text = None, pipeline_root: Text = None, data_root: Text = None,
                     module_file: Text = None, serving_model_dir: Text = None,
                     metadata_path: Text = None,
                     beam_pipeline_args: List[Text] = None, param: Dict = None) -> pipeline.Pipeline:

    myExampleGen = MyExampleGen(
        param=json.dumps(param),
        gte='',
        lte='',
        debug=str(param['flask']['debug']),
    )
    mySchema = MySchema(
        examples=myExampleGen.outputs['examples'],
        debug=str(param['flask']['debug']),
    )
    myTransform = MyTransform(
        examples=myExampleGen.outputs['examples'],
        schema=mySchema.outputs['schema'],
        modelparam_path='myflask/config/{}_config.yaml'.format(
            param['model']['name'])
    )
    myTrainer = Trainer(
        examples=myTransform.outputs['transformed_examples'],
        custom_executor_spec=executor_spec.ExecutorClassSpec(Executor),
        schema=mySchema.outputs['schema'],
        module_file=module_file,
        train_args=trainer_pb2.TrainArgs(),
        eval_args=trainer_pb2.EvalArgs(num_steps=10),
        custom_config=param
    )
    myPusher = MyPusher(
        model=myTrainer.outputs['model'],
        push_destination=param['serving']['models_dir']
    )
    # Uses user-provided Python function that implements a model using TF-Learn.
    # trainer = Trainer(
    #   module_file=module_file,
    #   examples=transform.outputs['transformed_examples'],
    #   schema=schema_gen.outputs['schema'],
    #   transform_graph=transform.outputs['transform_graph'],
    #   train_args=trainer_pb2.TrainArgs(num_steps=10000),
    #   eval_args=trainer_pb2.EvalArgs(num_steps=5000))

    return pipeline.Pipeline(
        pipeline_name=pipeline_name,
        pipeline_root=pipeline_root,
        components=[
            myExampleGen,
            mySchema,
            myTransform,
            myTrainer,
            myPusher
        ],
        enable_cache=False,
        metadata_connection_config=metadata.sqlite_metadata_connection_config(
            metadata_path),
        beam_pipeline_args=beam_pipeline_args)
