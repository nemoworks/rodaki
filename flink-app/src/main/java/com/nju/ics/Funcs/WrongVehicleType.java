package com.nju.ics.Funcs;

import com.nju.ics.Models.ExitPaymentRecord;
import com.nju.ics.Models.TimerRecord;
import com.nju.ics.RawType.AbnormalVehicle;

import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

public class WrongVehicleType extends KeyedProcessFunction<String, ExitPaymentRecord, AbnormalVehicle> {
    private ValueState<Integer> VehicleType;
    ValueStateDescriptor<Integer> passid2vehicleMapStateDescriptor = new ValueStateDescriptor<Integer>(
            "vehicletype", Integer.class);

    @Override
    public void open(Configuration parameters) throws Exception {
        // TODO Auto-generated method stub
        super.open(parameters);
        VehicleType = getRuntimeContext().getState(passid2vehicleMapStateDescriptor);

    }

    @Override
    public void processElement(ExitPaymentRecord value,
            KeyedProcessFunction<String, ExitPaymentRecord, AbnormalVehicle>.Context ctx,
            Collector<AbnormalVehicle> out) throws Exception {
        // TODO Auto-generated method stub

        if (value.getTRANSPAYTYPE() == 1) {
            // 不是人工车道，比对数据库
            if (VehicleType.value() != null && VehicleType.value().intValue() != value.getVEHICLETYPE()) {
                out.collect(new AbnormalVehicle(value.getVEHICLEID(), value.getPAYID(),
                        3, ctx.timestamp(), value.getPASSID(), ctx.timestamp()));
            }
        } else {
            // 人工车道，记录
            VehicleType.update(value.getVEHICLETYPE());
        }
        // if (value.getVEHICLEID().equals("鲁NF8867-1")) {
        //     System.out.println(value.getTRANSPAYTYPE());
        //     System.out.println(value.getVEHICLETYPE());
        //     System.out.println( VehicleType.value());
        // }
    }

}
