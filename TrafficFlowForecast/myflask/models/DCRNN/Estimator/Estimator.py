import tensorflow as tf
import pandas as pd
import numpy as np
import sys
import os
import time 
import argparse
from myflask.models.DCRNN.model.dcrnn_model import DCRNNModel
from myflask.models.DCRNN.lib.AMSGrad import AMSGrad
import yaml
from myflask.models.DCRNN.lib.utils import load_graph_data,StandardScaler
from myflask.models.DCRNN.Estimator.utils import load_dataset
import myflask.models.DCRNN.lib.metrics as metrics
# input_fn for training, convertion of dataframe to dataset
def get_variable(scope,name,value=None,dtype=tf.float32,create=False):
    with tf.compat.v1.variable_scope(scope,reuse=tf.compat.v1.AUTO_REUSE):
        if create:
            return tf.compat.v1.get_variable(
                name=name,
                trainable=False,
                initializer=value,
            )
        else:
            return tf.compat.v1.get_variable(
                name=name,
                shape=(),
                trainable=False,
            )
def transer_x(x,scaler_mean,scaler_std):
    #print(x.shape)
    return tf.concat([(x[...,:1]- scaler_mean) / scaler_std,x[...,1:]],axis=-1)
def transer_x_y(x,y,scaler_mean,scaler_std):
    #print(x.shape)
    #print(tf.concat([(x[...,:1]- scaler_mean) / scaler_std,x[...,1:]],axis=-1))
    return tf.concat([(x[...,:1]- scaler_mean) / scaler_std,x[...,1:]],axis=-1),\
            tf.concat([(y[...,:1]- scaler_mean) / scaler_std,y[...,1:]],axis=-1)
def serving_input_receiver_fn():
    """Serving input_fn that builds features from placeholders

    Returns
    -------
    tf.estimator.export.ServingInputReceiver
    """
    #输入是dataframe的value加上时间信息
    input_ph = tf.placeholder(dtype=tf.float32, shape=[None,None,None,2], name='input')
    receiver_tensors = {'input': input_ph}
    assert len(input_ph.shape)==4,'输入的维度不正确'
    scaler_mean=get_variable('scaler',name='scaler_mean')
    scaler_std=get_variable('scaler',name='scaler_std')
    feature=transer_x(input_ph,scaler_mean,scaler_std)
    return tf.estimator.export.TensorServingInputReceiver(feature, receiver_tensors)

def train_input_fn(features, labels, batch_size, repeat_count,schema):
    print('train_input_fn',features.shape,labels.shape)
    #samples=get_variable('samplemsg',name='num',value=features.shape[0],create=True)
    scaler_mean=get_variable('scaler',name='scaler_mean',value=tf.reduce_mean(features[...,0]),dtype=tf.float32 if schema=='float32' else features.dtype,create=True)
    scaler_std=get_variable('scaler',name='scaler_std',value=tf.math.reduce_std(features[...,0]),dtype=tf.float32 if schema=='float32' else features.dtype,create=True)
    features=transer_x(features,scaler_mean,scaler_std)
    labels=transer_x(labels,scaler_mean,scaler_std)
   
    dataset = tf.data.Dataset.from_tensor_slices((features, labels))

    dataset = dataset.shuffle(batch_size*2+1).repeat(repeat_count).batch(batch_size,drop_remainder=True)
    return dataset

# input_fn for evaluation and predicitions (labels can be null)
def eval_input_fn(features, labels, batch_size):
    
    scaler_mean=get_variable('scaler',name='scaler_mean')
    scaler_std=get_variable('scaler',name='scaler_std')
    if labels is None:
        #features[...,0]=(features[...,0] - scaler_mean) / scaler_std
        inputs = features
        dataset = tf.data.Dataset.from_tensor_slices(inputs)
        dataset=dataset.map(lambda x:transer_x(x,scaler_mean,scaler_std))
        #print('vnsdklvndlvhndl')
    else:
        #features[...,0]=(features[...,0] - scaler_mean) / scaler_std
        #labels[...,0]=(labels[...,0] - scaler_mean) / scaler_std
        inputs = (features, labels)
        dataset = tf.data.Dataset.from_tensor_slices(inputs)
        dataset=dataset.map(lambda x,y:transer_x_y(x,y,scaler_mean,scaler_std))
        #print('djkvskjvbjs')
    
    assert batch_size is not None, "batch_size must not be None"

    dataset = dataset.batch(batch_size,drop_remainder=True)
    return dataset
def model_fn(features, labels, mode, params):
    if mode == tf.estimator.ModeKeys.PREDICT:
        tf.logging.info("my_model_fn: PREDICT, {}".format(mode))
    elif mode == tf.estimator.ModeKeys.EVAL:
        tf.logging.info("my_model_fn: EVAL, {}".format(mode))
    elif mode == tf.estimator.ModeKeys.TRAIN:
        tf.logging.info("my_model_fn: TRAIN, {}".format(mode))

    #print(features.dtype)
    #batch_size=features.get_shape()[0].value
    scaler_mean=get_variable('scaler',name='scaler_mean')
    scaler_std=get_variable('scaler',name='scaler_std')
    
    #print(params['scaler'].mean)
    is_training = (mode == tf.estimator.ModeKeys.TRAIN)
    if mode == tf.estimator.ModeKeys.TRAIN:

        #scaler_mean.assign(params['scaler'].mean)
        #scaler_std.assign(params['scaler'].std)
        batch_size=params['data']['train_batch_size']
        #print(params['adj_mx'])
        #adj_mx=get_variable('adj_mx',name='value',value=params['adj_mx'],create=True) 
    elif mode == tf.estimator.ModeKeys.EVAL:
    #     model=DCRNNModel(is_training=is_training,
    #                     inputs=features,
    #                     labels=labels,
    #                     batch_size=params['data']['test_batch_size'],
    #                     adj_mx=params['adj_mx'], **params['model'])
        batch_size=params['data']['test_batch_size']
        #features=tf.concat([(features[...:1]- scaler_mean) / scaler_std,features[...,1:]],axis=-1)
        #labels=tf.concat([(labels[...:1]- scaler_mean) / scaler_std,labels[...,1:]],axis=-1)
    else:
    #     model=DCRNNModel(is_training=is_training,
    #                     inputs=features,
    #                     labels=features,
    #                     batch_size=params['data']['predict_batch_size'],
    #                     adj_mx=params['adj_mx'], **params['model'])
        batch_size=params['data']['predict_batch_size']
    model=DCRNNModel(is_training=is_training,
                        inputs=features,
                        labels=labels if mode != tf.estimator.ModeKeys.PREDICT else features,
                        batch_size=batch_size,
                        adj_mx=params['adj_mx'] , **params['model'])
    #model._inputs=features
    #model._labels=labels
    #predictions:(batchsize,horizon,num nodes,output_dim)
    
    predictions=model.outputs
    if mode == tf.estimator.ModeKeys.PREDICT:
        predictions = {
            'predictions': tf.squeeze(
                    (predictions * scaler_std) + scaler_mean, 
                    axis=(0,3)
                    )
        }
        return tf.estimator.EstimatorSpec(mode, predictions=predictions)
    
    #for i in predictions
    #labels=scaler.inverse_transform(labels[...,:1])
    #predictions=scaler.inverse_transform(predictions)
    labels=(labels[...,:1] * scaler_std) + scaler_mean
    predictions=(predictions * scaler_std) + scaler_mean
    #loss = tf.metrics.mean_absolute_error(labels, predictions)[0]#mae loss
    #rmse_loss=tf.metrics.root_mean_squared_error(labels, predictions)
    #mae_loss=tf.metrics.mean_absolute_error(labels, predictions)
    #mape_loss=tf.keras.losses.MAPE(labels, predictions)
    loss = metrics.masked_mae_tf(predictions,labels ,0.)
    copyloss = tf.identity(loss, name="loss")
    rmse_loss=tf.metrics.root_mean_squared_error(labels, predictions)
    mae_loss=tf.metrics.mean_absolute_error(labels, predictions)
    #mape_loss,updateop1=tf.metrics.mean_absolute_error(labels, predictions)
    mape_loss=metrics.masked_mape_tf(predictions,labels,0.)
    #mape_loss=tf.reduce_mean(tf.divide(mape_loss,labels + 1e-10))
    eval_metric_ops = {
        "rmse":rmse_loss ,
        "mae": mae_loss,
        "mape":mape_loss
    }
    #tf.summary.scalar('loss(mae)', loss)
    #tf.summary.scalar('mae', mae_loss)
    #tf.summary.scalar('rmse', rmse_loss)
    #tf.summary.scalar('mape', mape_loss)
    if mode == tf.estimator.ModeKeys.EVAL:
        return tf.estimator.EstimatorSpec(
            mode, loss=loss, eval_metric_ops=eval_metric_ops)
    # Configure optimizer
    optimizer_name = params['train'].get('optimizer', 'adam').lower()
    epsilon = float(params['train'].get('epsilon', 1e-3))
    #lr=tf.compat.v1.train.exponential_decay(
    #    float(params['train'].get('base_lr', 1e-3)),
    #    tf.train.get_global_step(),
    #    )
    global_step = tf.train.get_or_create_global_step()
    #samples=get_variable('samplemsg',name='num')
    lr=tf.get_variable(
        'learning_rate', 
        shape=(), 
        initializer=tf.constant_initializer(
            float(params['train'].get('base_lr', 1e-2)
            )
        ),
        trainable=False)
    #lr=float(params['train'].get('base_lr', 1e-3))
    optimizer = tf.train.AdamOptimizer(lr, epsilon=epsilon)
    #optimizer = tf.contrib.estimator.TowerOptimizer(optimizer)
    if optimizer_name == 'sgd':
        optimizer = tf.train.GradientDescentOptimizer(lr, )
    elif optimizer_name == 'amsgrad':
        optimizer = AMSGrad(lr, epsilon=epsilon)
    
    tvars = tf.trainable_variables()
    grads = tf.gradients(loss, tvars)
    max_grad_norm = params['train'].get('max_grad_norm', 1.)
    grads, _ = tf.clip_by_global_norm(grads, max_grad_norm)
    
    train_op = optimizer.apply_gradients(zip(grads, tvars), global_step=global_step, name='train_op')
    #train_op=optimizer.minimize(loss, global_step=tf.train.get_global_step())
    return tf.estimator.EstimatorSpec(mode, loss=loss, train_op=train_op)
    # training op
def create_estimator(run_config, hparams):
    estimator = tf.estimator.Estimator(model_fn=model_fn, 
                                  params=hparams, 
                                  #model_dir='piplines/traffic_util/Trainer/model_run/28/serving_model_dir',
                                  #warm_start_from='piplines/traffic_util/Trainer/model_run/28/serving_model_dir',
                                  config=run_config)
                                #)
    #print(estimator.get_variable_value('signal_early_stopping/STOP'))
    #assert 0>1
    print("")
    print("Estimator Type: {}".format(type(estimator)))
    print("")

    return estimator
def get_run_config(model_dir,use_gpu,model_config,keep_checkpoint_max=10,save_checkpoints_steps=None):
    sess_config = tf.compat.v1.ConfigProto(allow_soft_placement=True)
    if not use_gpu or not tf.test.is_gpu_available():
        sess_config = tf.compat.v1.ConfigProto(
            device_count={'GPU': 0},
            intra_op_parallelism_threads=model_config.get('intra_op_parallelism_threads',5),
            inter_op_parallelism_threads=model_config.get('inter_op_parallelism_threads',5)
        )
    sess_config.gpu_options.allow_growth = True
    dist_strategy = tf.distribute.MirroredStrategy()
    run_config=tf.estimator.RunConfig(
        model_dir=model_dir,
        session_config=sess_config,
        keep_checkpoint_max=keep_checkpoint_max,
        save_checkpoints_steps=save_checkpoints_steps,
        train_distribute=dist_strategy
    )
    return run_config
def main(args):
    with open(args.config_filename) as f:
        supervisor_config = yaml.load(f)


        graph_pkl_filename = supervisor_config['data'].get('graph_pkl_filename')
        sensor_ids, sensor_id_to_ind, adj_mx = load_graph_data(graph_pkl_filename)
        train_batch_size=supervisor_config['data'].get('train_batch_size',16)
        test_batch_size=supervisor_config['data'].get('test_batch_size',16)
        predict_batch_size=supervisor_config['data'].get('predict_batch_size',1)
        epochs=supervisor_config['train'].get('epochs',100)
        if args.mode=='train':
            model_dir=os.path.join(supervisor_config['base_dir'],time.strftime('%Y%m%d%H%M%S'))
        else:
            model_dir=os.path.join(supervisor_config['base_dir'])
            model_dir=os.path.join(model_dir,'20201020095317')

        run_config=get_run_config(model_dir,supervisor_config['use_gpu'])
        data,scaler=load_dataset(supervisor_config['data']['dataset_dir'],train_batch_size,test_batch_size)
        #print(scaler.std,scaler.mean)

        supervisor_config['scaler']=scaler
        supervisor_config['adj_mx']=adj_mx
        estimator=create_estimator(
            run_config=run_config,
            hparams=supervisor_config
        )
        if args.mode=='train':
            #train
            estimator.train(
                input_fn=lambda: train_input_fn(data['x_train'], data['y_train'], train_batch_size, epochs,scaler.mean,
                scaler.std
                ))
            #print(estimator.get_variable_value('scaler/scaler_mean'))
        elif args.mode=='test':
            #evaluate
            #print(estimator.get_variable_value('scaler/scaler_mean'))
            evaluate_result = estimator.evaluate(
                input_fn=lambda: eval_input_fn(data['x_train'], data['y_train'],test_batch_size))
            #print(estimator.get_variable_value('scaler/scaler_mean'))
            print("Evaluation results")

            for key in evaluate_result:
                print("   {}, was: {}".format(key, evaluate_result[key]))
        elif args.mode=='predict':
            #print(estimator.get_variable_value('scaler/scaler_mean'))
            #print(estimator.get_variable_value('scaler/scaler_std'))
            #print(data['x_train'][:1,...])
            predict_results=estimator.predict(
                input_fn=lambda: eval_input_fn(data['x_train'][:1,...], None,predict_batch_size),
                yield_single_examples=False
                )
            print(next(predict_results))
        elif args.mode=='export':
            export_dir='saved_model'
            current_dir=estimator.export_saved_model(export_dir, serving_input_receiver_fn)
            print('export to:',current_dir)
        elif args.mode=='serving':
            export_dir = 'saved_model'
            saved_model_dir = export_dir + "/" + os.listdir(path=export_dir)[-1] 
            print(saved_model_dir)
            predict_fn=tf.contrib.predictor.from_saved_model(
                export_dir = saved_model_dir,
            )
            #print(data['x_train'][:1,...])
            print(predict_fn({'input':data['x_train'][:1,...]})['predictions'])

            
if __name__=='__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--config_filename', default=None, type=str,
                        help='Configuration filename for restoring the model.')
    parser.add_argument('--mode', default='train', type=str,
                        help='')
    args = parser.parse_args()
    main(args)