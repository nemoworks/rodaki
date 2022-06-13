package com.nju.ics.Funcs;

import com.nju.ics.Models.TimerRecord;
import com.nju.ics.RawType.MultiPassIdVehicle;

import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

/**
 * 
 * 同时在途
 */
public class MultiPassid extends KeyedProcessFunction<String, TimerRecord, MultiPassIdVehicle> {
    private ValueState<String> validPassid;
    StateTtlConfig ttlConfig = StateTtlConfig.newBuilder(Time.hours(5l))
            .setUpdateType(StateTtlConfig.UpdateType.OnReadAndWrite)
            .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired).build();
    ValueStateDescriptor<String> validPassidDescriptor = new ValueStateDescriptor<String>("validPassid", String.class);

    @Override
    public void processElement(TimerRecord value,
            KeyedProcessFunction<String, TimerRecord, MultiPassIdVehicle>.Context ctx,
            Collector<MultiPassIdVehicle> out)
            throws Exception {
        // TODO Auto-generated method stub
        if (!this.isRecordValid(value)) {
            return;
        }
        // 判断是否是省界出口、虚门架、出口记录
        if (value.getORIGINALFLAG() == 2
                || value.getPROVINCEBOUND() == 2 || value.getFLOWTYPE() == 3) {
            // 与这辆车的合法passid做比较,如果出去时的passid与之前的不一样，认为这个车的通行介质变了
            if (validPassid.value() != null && !validPassid.value().equals(value.getPASSID())) {
                out.collect(new MultiPassIdVehicle(value.getVEHICLEID(), value.getSTATIONID(), value.getFLOWTYPE(),
                        value.getPASSID(), validPassid.value(), ctx.timestamp()));
            }
            validPassid.clear();
        } else if (value.getFLOWTYPE() == 1 || value.getPROVINCEBOUND() == 1) {// 入口、省界入口
            
            validPassid.update(value.getPASSID());
        }

    }

    @Override
    public void open(Configuration parameters) throws Exception {
        // TODO Auto-generated method stub
        super.open(parameters);
        //validPassidDescriptor.enableTimeToLive(ttlConfig);
        validPassid = getRuntimeContext().getState(validPassidDescriptor);
    }

    public boolean isRecordValid(TimerRecord value) {
        if (value.getPASSID().startsWith("000000")) {
            return false;
        }
        if (value.getVEHICLEID().startsWith("默") || value.getVEHICLEID().startsWith("0")) {
            return false;
        }

        return true;
    }
}
