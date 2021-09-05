from myflask.serving.predictor import test
from myflask.utils.util import  get_flask_server_params
import absl
absl.logging.set_verbosity(absl.logging.INFO)
server_name, server_port, flask_debug,flask_config = get_flask_server_params()
test(flask_config['serving']['models_dir'],flask_config['elasticsearch'],flask_config['model'])