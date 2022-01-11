package com.nju.ics.Funcs;

import com.alibaba.fastjson.JSONObject;
import com.nju.ics.Utils.DataSourceJudge;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

public class RawDatastreamPartitionProcess extends ProcessFunction<JSONObject, JSONObject> {
    private OutputTag<JSONObject> EntryStream;
    private OutputTag<JSONObject> GantryStream;
    private OutputTag<JSONObject> ExitStream;
    @Override
    public void open(Configuration parameters) throws Exception {
        // TODO Auto-generated method stub
        super.open(parameters);
        EntryStream=new OutputTag<JSONObject>("EntryStream"){};
		GantryStream=new OutputTag<JSONObject>("GantryStream"){};
		ExitStream=new OutputTag<JSONObject>("ExitStream"){};
    }
    @Override
    public void processElement(JSONObject value, Context ctx, Collector out) throws Exception {
        // TODO Auto-generated method stub
        switch (value.getIntValue(DataSourceJudge.sourceKey)) {
            case DataSourceJudge.entryLane:
                ctx.output(EntryStream, value);
                break;
            case DataSourceJudge.gantryCharge:
                ctx.output(GantryStream, value);
                break;
            case DataSourceJudge.exitLane:
                ctx.output(ExitStream, value);
                break;
            default:
                break;
        }

    }

}
