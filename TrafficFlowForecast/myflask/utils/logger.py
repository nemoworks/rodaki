import logging
import os
import sys
import pathlib
import yaml
from logging.handlers import TimedRotatingFileHandler
def get_flask_server_params():
    '''
    Returns connection parameters of the Flask application
    :return: Tripple of server name, server port and debug settings
    '''
    with open('myflask/config/config.yaml') as f:
        flask_config = yaml.load(f)
        server_host = flask_config['flask'].get('server_host', '0.0.0.0')
        server_port = flask_config['flask'].get('server_port', 5000)
        flask_debug = flask_config['flask'].get('debug', True)
        return server_host, server_port, flask_debug, flask_config
_,_,_,flaskconfig=get_flask_server_params()
def get_logger(log_dir, name, log_filename='info', level=logging.INFO):
    logger = logging.getLogger(name)
    logger.setLevel(level)
    # Add file handler and stdout handler
    formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
    logfile=pathlib.Path(log_dir)
    logfile.mkdir(exist_ok=True)
    fileTimeHandler = TimedRotatingFileHandler(
        filename=os.path.join(log_dir, log_filename),
        when=flaskconfig['flask']['log_when'],
        interval=flaskconfig['flask']['interval'],
        backupCount=flaskconfig['flask']['backupCount'])

    fileTimeHandler.suffix = "%Y-%m-%d_%H-%M-%S.log"  #设置 切分后日志文件名的时间格式 默认 filename+"." + suffix 如果需要更改需要改logging 源码
    #file_handler = logging.FileHandler(os.path.join(log_dir, log_filename))
    #file_handler.setFormatter(formatter)
    # Add console handler.
    # console_formatter = logging.Formatter('%(asctime)s - %(levelname)s - %(message)s')
    # console_handler = logging.StreamHandler(sys.stdout)
    # console_handler.setFormatter(console_formatter)
    fileTimeHandler.setFormatter(formatter)
    logger.addHandler(fileTimeHandler)
    #logger.addHandler(file_handler)
    #logger.addHandler(console_handler)
    # Add google cloud log handler
    logger.info('Log directory: %s', log_dir)
    return logger

logger=get_logger(
    flaskconfig['flask']['log_dir'],
    flaskconfig['beam_pipline']['pipeline_name'],level=flaskconfig['flask']['logging_level'])
