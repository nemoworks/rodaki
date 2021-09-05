from myflask.task import register_predictor, register_evalutor, init_predictor
import time
import os
import absl
from myflask.init_app import initialize_app, execute_pipline
from myflask.utils.util import get_flask_server_params
from apscheduler.schedulers.blocking import BlockingScheduler
from datetime import datetime
server_name, server_port, flask_debug, flask_config = get_flask_server_params()
absl.logging.set_verbosity(absl.logging.INFO)


if __name__ == '__main__':
    predictor = init_predictor()
    # 初始化es与kibana
    initialize_app(flask_config)
    # 执行一次pipline ,如果没有可以用的serving_model的话
    execute_pipline(flask_config, predictor)
    # 注册预测器，每天定时运行一次
    scheduler = BlockingScheduler()

    # 每天的0点0分。预测一次
    scheduler.add_job(register_predictor, 'cron', id='predictor', hour=0, minute=0, max_instances=1, args=[predictor,
                                                                                                           flask_config['elasticsearch'], flask_config['model'], flask_config['flask']['debug']])
    # scheduler.add_job(register_predictor, 'interval', id='predictor', seconds=10,
    #                   max_instances=5, args=[flask_config['elasticsearch'], flask_config['model']])
    # scheduler.add_job(register_evalutor, 'interval', id='evalutor', seconds=15,
    #                   max_instances=2, args=[flask_config['elasticsearch'], flask_config['model'], flask_config])
    # 每月1号的0点0分。进行模型验证一次
    scheduler.add_job(register_evalutor, 'cron', id='evalutor', day='1', hour='0', minute=10,
                      max_instances=2, args=[predictor, flask_config['elasticsearch'], flask_config['model'], flask_config, flask_config['flask']['debug']])
    try:
        scheduler.start()
    except (KeyboardInterrupt, SystemExit):
        pass
