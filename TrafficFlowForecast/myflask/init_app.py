import yaml
import os
from .es_op.es_init import es_init
from .kibana_op.kibana_init import kibana_init
from myflask.beam_pipline.create_pipline import _create_pipeline
from tfx.orchestration.beam.beam_dag_runner import BeamDagRunner
import shutil
from myflask.utils.util import check_serving_dir
from myflask.utils.util import logging
from myflask.serving.Predictor import Predictor

def initialize_app(flask_config):
    # 首先创建es里的两个index
    # print(flask_config)
    es_init(flask_config)
    # 然后创建kibana里的两个index pattern与map
    kibana_init(flask_config)


def execute_pipline(flask_config,predictor:Predictor=None):
    _pipeline_name = flask_config['beam_pipline']['pipeline_name']
    _beam_pipeline_args = [
        '--direct_running_mode=multi_processing',
        # 0 means auto-detect based on on the number of CPUs available
        # during execution time.
        '--direct_num_workers=0',
    ]
    project_root = './'
    _module_file = os.path.join(
        project_root, 'myflask/beam_pipline/trainer_module.py')
    _pipeline_root = os.path.join(project_root, 'piplines', _pipeline_name)
    _metadata_path = os.path.join(project_root, 'metadata', _pipeline_name,
                                  'metadata.db')

    # try:
    #     shutil.rmtree(_pipeline_root)
    # except FileNotFoundError:
    #     print('本来不存在，不需要删除')
    # try:
    #     os.remove(_metadata_path)
    # except FileNotFoundError:
    #     print('本来不存在，不需要删除')

    # with app.context():
    #    print(current_app.config['flask_config'])
    if not check_serving_dir(flask_config['serving']['models_dir']):
        logging.logger.info("没有可用的模型文件，需要执行一次训练任务")
        try:
            BeamDagRunner().run(
                _create_pipeline(
                    pipeline_name=_pipeline_name,
                    pipeline_root=_pipeline_root,
                    metadata_path=_metadata_path,
                    module_file=_module_file,
                    beam_pipeline_args=_beam_pipeline_args,
                    param=flask_config))
        except Exception as e:
            logging.logger.error(e)
        if predictor:
            predictor.delete_extra_model()
            predictor.update_current_serving_model()
def execute_pipline_test(flask_config):
    _pipeline_name = flask_config['beam_pipline']['pipeline_name']
    _beam_pipeline_args = [
        '--direct_running_mode=multi_processing',
        # 0 means auto-detect based on on the number of CPUs available
        # during execution time.
        '--direct_num_workers=0',
    ]
    project_root = './'
    _module_file = os.path.join(
        project_root, 'myflask/beam_pipline/trainer_module.py')
    _pipeline_root = os.path.join(project_root, 'piplines', _pipeline_name)
    _metadata_path = os.path.join(project_root, 'metadata', _pipeline_name,
                                  'metadata.db')

    # try:
    #     shutil.rmtree(_pipeline_root)
    # except FileNotFoundError:
    #     print('本来不存在，不需要删除')
    # try:
    #     os.remove(_metadata_path)
    # except FileNotFoundError:
    #     print('本来不存在，不需要删除')

    # with app.context():
    #    print(current_app.config['flask_config'])
    #if not check_serving_dir(flask_config['serving']['models_dir']):
    #    logging.logger.info("没有可用的模型文件，需要执行一次训练任务")
    BeamDagRunner().run(
        _create_pipeline(
            pipeline_name=_pipeline_name,
            pipeline_root=_pipeline_root,
            metadata_path=_metadata_path,
            module_file=_module_file,
            beam_pipeline_args=_beam_pipeline_args,
            param=flask_config))
        #task.predictor.delete_extra_model()
        #task.predictor.update_current_serving_model()
