# Lint as: python2, python3
# Copyright 2019 Google LLC. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
"""Python source file include taxi pipeline functions and necesasry utils.
For a TFX pipeline to successfully run, a preprocessing_fn and a
trainer_fn function needs to be provided. This file contains both.
"""

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function
import os
from typing import List, Text
import yaml
import tensorflow as tf
from tensorflow.python.framework import ops
import tensorflow_model_analysis as tfma
import tensorflow_transform as tft
from tensorflow_transform.tf_metadata import schema_utils
from tfx.components.trainer.fn_args_utils import DataAccessor
from tfx_bsl.tfxio import dataset_options
from myflask.models.DCRNN.Estimator.Estimator import create_estimator
from myflask.models.DCRNN.Estimator.Estimator import train_input_fn, eval_input_fn, serving_input_receiver_fn
from myflask.models.DCRNN.Estimator.Estimator import get_run_config, get_variable, transer_x_y
from myflask.models.DCRNN.lib.utils import load_graph_data
from myflask.utils.util import load_dataset
import numpy as np
import myflask.utils.logger as logging
# TFX will call this function


class lrdelayHook(tf.train.SessionRunHook):
    def __init__(self, samplesnum, epoch_delaylist, min_lr, trainbatchsize, base_lr, lr_decay_ratio, patience=20):

        self.steps = 0
        self.steps_per_epoch = round(samplesnum/trainbatchsize)
        #self.samplesnum = samplesnum
        self.epoch_delaylist = np.array(epoch_delaylist)
        self.min_lr = min_lr
        self.base_lr = base_lr
        #self.trainbatchsize = trainbatchsize
        self.lrtensor = None
        self.lrtensor_placeholder = None
        self.patience = patience
        self.lr_decay_ratio = lr_decay_ratio
        self.update_op = None
        self.min_train_loss = float('inf')
        self.wait = 0
        self.sum_loss_per_epoch = 0.0
        self.sum_step = 0

    def begin(self):
        self.lrtensor_placeholder = tf.placeholder(
            tf.float32, shape=(), name='lrplaceholder')
        top = tf.get_variable_scope()
        with tf.compat.v1.variable_scope(top, reuse=tf.compat.v1.AUTO_REUSE):
            self.lrtensor = tf.get_variable('learning_rate')
            # logging.logger.error(self.lrtensor)
        self._global_step_tensor = tf.train.get_or_create_global_step()  # global step
        self.update_op = tf.assign(self.lrtensor, self.lrtensor_placeholder)
        self.new_lr = self.base_lr
        self.element = ops.get_default_graph(
        ).get_operation_by_name('loss').outputs[0]
    # def begin(self):
    #   top=tf.get_variable_scope()
    #   with tf.compat.v1.variable_scope(top,reuse=tf.                                                                                                                                compat.v1.AUTO_REUSE):
    #     self.lrtensor=tf.get_variable('learning_rate')

    def before_run(self, run_context):

        # top=tf.get_variable_scope()
        # with tf.compat.v1.variable_scope(top,reuse=tf.compat.v1.AUTO_REUSE):
        #   self.lrtensor=tf.get_variable('learning_rate')
        return tf.train.SessionRunArgs(
            [
                self.update_op,  # Asks for global step value.
                self._global_step_tensor,
                self.element
            ],
            feed_dict={self.lrtensor_placeholder: self.new_lr})  # Sets learning rate

    def after_run(self, run_context, run_values):
        self.steps = run_values.results[1]
        self._epoch = int(self.steps/self.steps_per_epoch)
        self.new_lr = max(self.min_lr, self.base_lr * (self.lr_decay_ratio **
                                                       np.sum(self._epoch >= self.epoch_delaylist)))
        # logging.logger.error(self.new_lr)
        if self.sum_step < self.steps_per_epoch:  # 将当前epoch的每个step的loss加起来
            self.sum_loss_per_epoch += run_values.results[2]
            self.sum_step += 1
        else:  # 说明当前已经加完了一个epoch
            if self.min_train_loss >= (self.sum_loss_per_epoch/self.steps_per_epoch):
                self.wait = 0
                self.min_val_loss = self.sum_loss_per_epoch/self.steps_per_epoch
            else:
                self.wait += 1
                if self.wait > self.patience:
                    logging.logger.warning(
                        'Early stopping at epoch: %d' % self._epoch)
                    run_context.request_stop()
            self.sum_step = 1
            self.sum_loss_per_epoch = run_values.results[2]


def _eval_input_receiver_fn(example_uri, schema):
    """Build everything needed for the tf-model-analysis to run the model.
    Args:
      tf_transform_output: A TFTransformOutput.
      schema: the schema of the input data.
    Returns:
      EvalInputReceiver function, which contains:
        - Tensorflow graph which parses raw untransformed features, applies the
          tf-transform preprocessing operators.
        - Set of raw, untransformed features.
        - Label against which predictions will be compared.
    """

    input_ph = tf.placeholder(dtype=tf.string, shape=(), name='input')

    # The key name MUST be 'examples'.
    receiver_tensors = {'examples': input_ph}
    features = load_dataset(os.path.join(input_ph, 'test.npz'))
    scaler_mean = get_variable('scaler', name='scaler_mean')
    scaler_std = get_variable('scaler', name='scaler_std')
    dataset = tf.data.Dataset.from_tensor_slices(
        (features['x'], features['y']))
    dataset = dataset.map(lambda x, y: transer_x_y(
        x, y, scaler_mean, scaler_std))
    return tfma.export.EvalInputReceiver(
        features=features['x'],
        receiver_tensors=receiver_tensors,
        iterator_initializer=dataset)


def trainer_fn(trainer_fn_args, schema):
    """Build the estimator using the high level API.
    Args:
      trainer_fn_args: Holds args used to train the model as name/value pairs.
      schema: Holds the schema of the training examples.
    Returns:
      A dict of the following:
        - estimator: The estimator that will be used for training and eval.
        - train_spec: Spec for training.
        - eval_spec: Spec for eval.
        - eval_input_receiver_fn: Input function for eval.
    """
    # Number of nodes in the first layer of the DNN
    logging.logger.info(trainer_fn_args)
    logging.logger.info(schema)
    supervisor_config = yaml.load(
        open(os.path.join(trainer_fn_args.transformed_examples_dir, 'model.yaml')))
    flask_config = trainer_fn_args.custom_config
    graph_pkl_filename = os.path.join(
        trainer_fn_args.transformed_examples_dir, 'adj_mx.pkl')
    sensor_ids, sensor_id_to_ind, adj_mx = load_graph_data(graph_pkl_filename)
    train_batch_size = supervisor_config['data'].get('train_batch_size', 16)
    test_batch_size = supervisor_config['data'].get('test_batch_size', 16)
    predict_batch_size = supervisor_config['data'].get('predict_batch_size', 1)
    epochs = supervisor_config['train'].get('epochs', 200)

    train_data = load_dataset(os.path.join(
        trainer_fn_args.transformed_examples_dir, 'train.npz'), train_batch_size, schema)
    test_data = load_dataset(os.path.join(
        trainer_fn_args.transformed_examples_dir, 'test.npz'), test_batch_size, schema)
    sample_num = train_data['x'].shape[0]

    def train_input(): return train_input_fn(
        train_data['x'], train_data['y'], train_batch_size, epochs, schema)
    def eval_input(): return eval_input_fn(
        test_data['x'], test_data['y'], test_batch_size)

    def serving_receiver(): return serving_input_receiver_fn()

    exporter = tf.estimator.FinalExporter(
        flask_config['beam_pipline']['pipeline_name'], serving_receiver)

    # Keep multiple checkpoint files for distributed training, note that
    # keep_max_checkpoint should be greater or equal to the number of replicas to
    # avoid race condition.
    run_config = get_run_config(
        model_dir=trainer_fn_args.serving_model_dir,
        use_gpu=flask_config['model']['use_gpu'],
        model_config=flask_config['model'],
        keep_checkpoint_max=flask_config['model']['keep_checkpoint_max'],
        save_checkpoints_steps= round(sample_num/train_batch_size)
    )
    warm_start_from = trainer_fn_args.base_model
    supervisor_config['adj_mx'] = adj_mx

    estimator = create_estimator(run_config, hparams=supervisor_config)
    # 提前终止step=sample_num*patience/train_batch_size最少要执行10个epoch才考虑
    early_stopping = tf.estimator.experimental.stop_if_no_decrease_hook(
        estimator,
        metric_name='mape',
        max_steps_without_decrease=round(
            sample_num*supervisor_config['train']['patience']/train_batch_size),
        # max_steps_without_decrease=100,
        min_steps=round(10*sample_num/train_batch_size),
        run_every_secs=None,
        run_every_steps=int((sample_num/train_batch_size)),
        # eval_dir=trainer_fn_args.serving_model_dir
    )
    evaluator = tf.estimator.experimental.InMemoryEvaluatorHook(
        estimator, eval_input, every_n_iter=round(sample_num/train_batch_size))  # supervisor_config['train']['test_every_n_epochs']*
    # 将model config中的epoch delay转为steps delay

    # lr_delay_steps=[]
    train_spec = tf.estimator.TrainSpec(  # pylint: disable=g-long-lambda
        train_input,
        max_steps=trainer_fn_args.train_steps,
        hooks=[lrdelayHook(sample_num,
                           supervisor_config['train']['steps'],
                           supervisor_config['train']['min_learning_rate'],
                           train_batch_size,
                           supervisor_config['train']['base_lr'],
                           supervisor_config['train']['lr_decay_ratio'],
                           patience=supervisor_config['train']['patience']
                           ),
               evaluator,])

    eval_spec = tf.estimator.EvalSpec(
        eval_input,
        steps=trainer_fn_args.eval_steps,
        exporters=[exporter],
        start_delay_secs=1,
        throttle_secs=1,
        name=flask_config['beam_pipline']['pipeline_name'])
    # Create an input receiver for TFMA processing
    # receiver_fn=lambda:_eval_input_receiver_fn()
    return {
        'estimator': estimator,
        'train_spec': train_spec,
        'eval_spec': eval_spec,
        'serving_receiver_fn': serving_receiver
        # 'eval_input_receiver_fn': receiver_fn
    }
