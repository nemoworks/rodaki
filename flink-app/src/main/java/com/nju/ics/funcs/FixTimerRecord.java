package com.nju.ics.funcs;

import java.util.HashSet;
import java.util.Set;

import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import com.nju.ics.models.TimerRecord;

/**
 * 对TimerRecord的车牌尝试进行校验
 */
public class FixTimerRecord extends KeyedProcessFunction<String, TimerRecord, TimerRecord> {
    private MapState<String, String> passid2vehicle;
    MapStateDescriptor<String, String> passid2vehicleMapStateDescriptor = new MapStateDescriptor<String, String>(
            "passid2vehicle", String.class, String.class);
    private static Set<String> ignoresations = new HashSet<String>() {// 忽略5个已知的会经过休息区的情况
        {
            add("G00203700500132013");
            add("G03213700600071003");
            add("G00203700400082008");
            add("G00033700500041001");
            add("G00253700500111006");
        }
    };

    @Override
    public void open(Configuration parameters) throws Exception {
        // TODO Auto-generated method stub
        super.open(parameters);
        passid2vehicle = getRuntimeContext().getMapState(passid2vehicleMapStateDescriptor);
    }

    @Override
    public void processElement(TimerRecord value,
            KeyedProcessFunction<String, TimerRecord, TimerRecord>.Context ctx,
            Collector<TimerRecord> out) throws Exception {
        // 需要清楚timer的车牌，下面进行矫正
        String timervehicleid = value.getVEHICLEID();
        // 如果是省界入口门架或者入口站点记录，认为passid与车牌是对应的，需要记录一下
        if (value.getFLOWTYPE() == 1|| value.getPROVINCEBOUND() == 1) {
            passid2vehicle.put(value.getPASSID(), value.getVEHICLEID());
        } else if (value.getORIGINALFLAG() == 2
                || value.getPROVINCEBOUND() == 2 || value.getFLOWTYPE() == 3) {
            // 在省界出口、虚门架、出口站点矫正一下
            if (value.getMEDIATYPE() == 1) {
                // 如果是obu卡，认为之前的入口记录是有效的，这个时候应当通过入口passid对应的车牌纠正出口的车牌
                if (passid2vehicle.contains(value.getPASSID())
                        && !passid2vehicle.get(value.getPASSID()).equals(value.getVEHICLEID())) {
                    value.setVEHICLEID(passid2vehicle.get(value.getPASSID()));
                    timervehicleid = value.getVEHICLEID();
                }
            } else if (value.getMEDIATYPE() == 2) {
                // 如果是cpc卡，认为出口的车牌是正确的，这个时候通过passid，将要设置timer的车牌号重新设置成原先错误的车牌，保证其timer能够删除
                if (passid2vehicle.contains(value.getPASSID())
                        && !passid2vehicle.get(value.getPASSID()).equals(value.getVEHICLEID())) {
                    timervehicleid = passid2vehicle.get(value.getPASSID());
                }
            }
        }
        // 经过矫正之后仍然不合法
        if (timervehicleid.startsWith("默") || timervehicleid.startsWith("0")) {
            return;
        }
        // 判断是否是省界出口或者出口记录，如果是，则清除passid2vehicle
        if (value.getPROVINCEBOUND() == 2 || value.getFLOWTYPE() == 3) {
            passid2vehicle.remove(value.getPASSID());
        }
        // 直接向后输出经过矫正后的元素
        out.collect(value);
    }
}