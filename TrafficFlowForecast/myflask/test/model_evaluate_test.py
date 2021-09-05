from myflask.task import register_evalutor
from myflask.utils.util import  get_flask_server_params
erver_name, server_port, flask_debug,flask_config = get_flask_server_params()
register_evalutor(flask_config['elasticsearch'],flask_config['model'],flask_config)