package cn.edu.nju.ics.rodaki.pipelinethread;


import cn.edu.nju.ics.rodaki.mongodblistener.MongodbListener;



public class MongodbListenerThread implements Runnable{

    MongodbListener listener;

    public MongodbListenerThread(MongodbListener listener) {
        this.listener = listener;

    }

    @Override
    public void run() {
        listener.startListen();

    }
}
