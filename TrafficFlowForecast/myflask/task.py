from myflask.serving.Predictor import Predictor
from myflask.utils.util import get_flask_server_params



def init_predictor():
    server_name, server_port, flask_debug, flask_config = get_flask_server_params()
    return Predictor(
        flask_config['serving']['models_dir'], flask_config['serving']['max_keep'], flask_config['model'])


def register_predictor(predictor:Predictor,es_config, model_config, flask_debug):
    predictor.predict(es_config, model_config, flask_debug)


def register_evalutor(predictor:Predictor,es_config, model_config, flask_config, flask_debug):
    predictor.current_model_evaluate(
        es_config, model_config, flask_config, flask_debug)
