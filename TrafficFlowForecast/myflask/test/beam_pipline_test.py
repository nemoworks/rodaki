from myflask.init_app import  execute_pipline_test
from myflask.utils.util import get_flask_server_params
import absl
server_name, server_port, flask_debug, flask_config = get_flask_server_params()
absl.logging.set_verbosity(absl.logging.INFO)
if __name__ == '__main__':
    # 执行一次pipline ,如果没有可以用的serving_model的话
    execute_pipline_test(flask_config)