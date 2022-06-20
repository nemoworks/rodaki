package com.nju.ics.utils;

import java.io.IOException;
import java.util.Properties;

import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class ConfigureENV {
    public static final String CHECKPOINTING_OPTION = "checkpointing";
    public static final String OPERATOR_CHAINING_OPTION = "chaining";
    public static final String PARALLELISM = "parallelism";
    public static final String EVENTTIMEOPTION="event-time";
    public static Properties prop;

    public static Properties initConfiguration(String url) {

        Properties prop = new Properties();
        try {
            prop.load(ConfigureENV.class.getResourceAsStream(url));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ConfigureENV.prop = prop;
        return prop;
    }

    public static void configureEnvironment(final ParameterTool params, StreamExecutionEnvironment env) {
        env.setStateBackend(new HashMapStateBackend());
        env.getCheckpointConfig().setCheckpointStorage(ConfigureENV.prop.getProperty("flink.state.checkpoints.dir"));
        boolean checkpointingEnabled = params.has(CHECKPOINTING_OPTION);
        boolean enableChaining = params.has(OPERATOR_CHAINING_OPTION);
        int parallelism = params.getInt(PARALLELISM,
                Integer.parseInt(ConfigureENV.prop.getProperty("flink.job.parallelism")));
        env.setParallelism(parallelism);
        if (checkpointingEnabled) {
            env.enableCheckpointing(120000);// 设置checkpoint的保存间隔，这里是120秒
            // Checkpoint 必须在2分钟内完成，否则就会被抛弃
            env.getCheckpointConfig().setCheckpointTimeout(120000 * 10);
            // 同一时间只允许一个 checkpoint 进行
            env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);
            env.getCheckpointConfig().setMinPauseBetweenCheckpoints(120000);
            // enables the experimental unaligned checkpoints
            // env.getCheckpointConfig().enableUnalignedCheckpoints();
        }

        if (!enableChaining) {
            // disabling Operator chaining to make it easier to follow the Job in the WebUI
            env.disableOperatorChaining();
        }
    }
}
