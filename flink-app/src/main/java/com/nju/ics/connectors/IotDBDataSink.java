package com.nju.ics.connectors;

import org.apache.commons.compress.utils.Lists;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.iotdb.flink.DefaultIoTSerializationSchema;
import org.apache.iotdb.flink.IoTDBSink;
import org.apache.iotdb.flink.IoTSerializationSchema;

import org.apache.iotdb.flink.options.IoTDBSinkOptions;

import com.nju.ics.modelextractors.GeneralExtractor;
import com.nju.ics.utils.ConfigureENV;

public class IotDBDataSink {
    public static CustomIoTDBSink generateIoTDBSink() {
        IoTDBSinkOptions options = new IoTDBSinkOptions();
        options.setHost(ConfigureENV.prop.getProperty("IotDB.host"));
        options.setPort(Integer.parseInt(ConfigureENV.prop.getProperty("IotDB.port")));
        options.setUser(ConfigureENV.prop.getProperty("IotDB.user"));
        options.setPassword(ConfigureENV.prop.getProperty("IotDB.password"));
        options.setStorageGroup(ConfigureENV.prop.getProperty("IotDB.StorageGroup"));
        IoTSerializationSchema serializationSchema = new DefaultIoTSerializationSchema();
        // modelGenarator.registerTimeseries(ConfigureENV.prop.getProperty("IotDB.StorageGroup"));
        options.setTimeseriesOptionList(Lists.newArrayList());
        CustomIoTDBSink ioTDBSink = new CustomIoTDBSink(options, serializationSchema)
                // enable batching
                .withBatchSize(Integer.parseInt(ConfigureENV.prop.getProperty("IotDB.BatchSize")))
                // how many connectons to the server will be created for each parallelism
                .withSessionPoolSize(Integer.parseInt(ConfigureENV.prop.getProperty("IotDB.SessionPoolSize")));
        
        return ioTDBSink;
    }
}
