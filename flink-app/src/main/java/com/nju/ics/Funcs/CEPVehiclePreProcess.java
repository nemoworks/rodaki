package com.nju.ics.Funcs;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import com.nju.ics.Models.HeartBeatAndRecord;
import com.nju.ics.Models.TimerRecord;

import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

/**
 * 在进行cep检测前，对无效的数据进行过滤，同时纠正一些字段,然后设置定时器，如果在定时器触发前来了新的记录，就清除
 */
public class CEPVehiclePreProcess extends KeyedProcessFunction<String, TimerRecord, HeartBeatAndRecord> {
    private MapState<String, String> passid2vehicle;
    private ValueState<Boolean> hasSetTimer;
    private ValueState<Integer> count;
    MapStateDescriptor<String, String> passid2vehicleMapStateDescriptor = new MapStateDescriptor<String, String>(
            "passid2vehicle", String.class, String.class);
    ValueStateDescriptor<Integer> countValueStateDescriptor = new ValueStateDescriptor<Integer>(
            "count", Integer.class);
    ValueStateDescriptor<Boolean> hasSetTimerValueStateDescriptor = new ValueStateDescriptor<Boolean>(
            "hasSetTimer", Boolean.class);
    private static Set<String> ignoresations = new HashSet<String>() {
        {
            add("G00203700500132013");
            add("G03213700600071003");
            add("G00203700400082008");
            add("G00033700500041001");
            add("G00253700500111006");
        }
    };
    private static long onehour = Duration.ofHours(1).toMillis();

    @Override
    public void open(Configuration parameters) throws Exception {
        // TODO Auto-generated method stub
        super.open(parameters);
        passid2vehicle = getRuntimeContext().getMapState(passid2vehicleMapStateDescriptor);
        hasSetTimer = getRuntimeContext().getState(hasSetTimerValueStateDescriptor);
        count = getRuntimeContext().getState(countValueStateDescriptor);
    }

    @Override
    public void processElement(TimerRecord value,
            KeyedProcessFunction<String, TimerRecord, HeartBeatAndRecord>.Context ctx,
            Collector<HeartBeatAndRecord> out) throws Exception {
        // TODO Auto-generated method stub
        if (value.getVEHICLETYPE() >= 11 && value.getVEHICLETYPE() <= 16) {
            if (count.value() == null) {
                ctx.timerService().registerEventTimeTimer(ctx.timestamp() + onehour);
                count.update(0);
            }
            // 如果是省界门架且特情类型为154或186，排除
            if (value.getPROVINCEBOUND() == 1
                    && (value.getSPECIALTYPE().contains("154") || value.getSPECIALTYPE().contains("186"))) {
                return;
            }
            // 需要清楚timer的车牌，下面进行矫正
            String timervehicleid = value.getVEHICLEID();
            // 如果是省界入口门架或者入口站点记录，认为passid与车牌是对应的，需要记录一下
            if (!value.getPASSID().startsWith("000000") && (value.getFLOWTYPE() == 1
                    || value.getPROVINCEBOUND() == 1)) {
                passid2vehicle.put(value.getPASSID(), value.getVEHICLEID());
            } else if (value.getORIGINALFLAG() == 2
                    || value.getPROVINCEBOUND() == 2 || value.getFLOWTYPE() == 3) {
                // 在省界出口、虚门架、出口站点矫正一下
                if (value.getMEDIATYPE() == 1) {
                    // 如果是obu卡，认为之前的入口记录是有效的，这个时候应当通过入口passid对应的车牌纠正出口的车牌
                    if (passid2vehicle.contains(value.getPASSID())
                            && !passid2vehicle.get(value.getPASSID()).equals(value.getVEHICLEID())) {
                        value.setVEHICLEID(passid2vehicle.get(value.getPASSID()));
                        // timervehicleid = value.getVEHICLEID();
                    }
                } else if (value.getMEDIATYPE() == 2) {
                    // 如果是cpc卡，认为出口的车牌是正确的，这个时候通过passid，将要设置timer的车牌号重新设置成原先错误的车牌，保证其timer能够删除
                    if (passid2vehicle.contains(value.getPASSID())
                            && !passid2vehicle.get(value.getPASSID()).equals(value.getVEHICLEID())) {
                        value.setVEHICLEID(passid2vehicle.get(value.getPASSID()));
                        // timervehicleid = passid2vehicle.get(value.getPASSID());
                    }
                }

            }
            // 经过矫正之后仍然不合法
            if (timervehicleid.startsWith("默") || timervehicleid.startsWith("0")) {
                return;
            }

            // 判断是否是虚门架
            if (value.getORIGINALFLAG() == 2) {
                return;
            }
            if (value.getPASSID().startsWith("000000")) {
                return;
            }
            // if (ignoresations.contains(value.getSTATIONID())) {
            // return;
            // }

            // 直接向后输出经过矫正后的元素
            out.collect(HeartBeatAndRecord.build(value));
            // 判断是否是省界出口或者出口记录，如果是，则清除passid2vehicle
            if (value.getPROVINCEBOUND() == 2 || value.getFLOWTYPE() == 3) {
                passid2vehicle.remove(value.getPASSID());
            }

        }
    }

    @Override
    public void onTimer(long timestamp,
            KeyedProcessFunction<String, TimerRecord, HeartBeatAndRecord>.OnTimerContext ctx,
            Collector<HeartBeatAndRecord> out) throws Exception {
        // TODO Auto-generated method stub
        out.collect(HeartBeatAndRecord.build(ctx.getCurrentKey(), timestamp));
        count.update(count.value() + 1);
        // 循环注册,避免batch模式下循环注册产生heatbeat，导致任务停不下来
        if (count.value() < 5) {
            ctx.timerService().registerEventTimeTimer(timestamp + onehour);
        } else {
            count.clear();
        }

    }

}
