package com.nju.ics.Funcs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSON;
import com.nju.ics.Configs.GantryPosition;
import com.nju.ics.Fields.AbnormalVehicle;
import com.nju.ics.Models.GantryRecord;
import com.nju.ics.Models.GantryRecordSimple;

import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

/**
 * 处理门架记录model，根据车辆通过门架的时间与相邻两门架的时间来检测异常
 */
public class GantryTimer extends ProcessFunction<GantryRecordSimple, AbnormalVehicle> implements CheckpointedFunction {
    private ListState<Tuple2<Long, Map<String, String>>> checkpointedTimers;

    private List<Tuple2<Long, Map<String, String>>> currenTimers;
    private Map<Long, Integer> timerPosIncurrenTimers;
    private Map<String, Long> vehicleTimer;

    private Map<String, Integer> siteTime;
    private static long defaulttime;
    private static long fiveminutes = 1000 * 60 * 5;

    public GantryTimer(String gantryfile) throws FileNotFoundException, IOException {
        this.siteTime = JSON.parseObject(new FileInputStream(gantryfile), Map.class);
        GantryPosition.initGantryPosition();// 初始化门架经纬度
        GantryTimer.defaulttime = this.siteTime.get("avg");
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        // TODO Auto-generated method stub
        super.open(parameters);
        currenTimers = new ArrayList<>();
        timerPosIncurrenTimers = new HashMap<Long, Integer>();
        vehicleTimer = new HashMap<String, Long>();
    }

    @Override
    public void processElement(GantryRecordSimple value,
            ProcessFunction<GantryRecordSimple, AbnormalVehicle>.Context ctx,
            Collector<AbnormalVehicle> out) throws Exception {
        // TODO Auto-generated method stub
        long timestamp;
        if (value.getVEHICLETYPE() >= 11 && value.getVEHICLETYPE() <= 16) {
            // 1. 查找该车在哪个timer注册过,有就先移除
            if (vehicleTimer.containsKey(value.getVEHICLEID())) {
                timestamp = vehicleTimer.get(value.getVEHICLEID());
                if (timerPosIncurrenTimers.containsKey(timestamp)) {
                    // 如果能够通过时间戳获得下标,就直接从对应的timer map里面删除这辆车
                    try{
                        currenTimers.get(timerPosIncurrenTimers.get(timestamp)).f1.remove(value.getVEHICLEID());
                    }catch (Exception e){
                        System.out.println(timerPosIncurrenTimers);
                        System.exit(1);
                    }
                   
                }
            }
            // 2. 根据该车的通过时间计算该车应该放入哪个timer
            timestamp = ctx.timestamp();
            if (siteTime.containsKey(value.getGANTRYID())) {
                timestamp = timestamp + fiveminutes - timestamp % fiveminutes + siteTime.get(value.getGANTRYID());
            } else {
                timestamp = timestamp + fiveminutes - timestamp % fiveminutes + defaulttime;
            }
            // 3.查看这个timer是否已经有
            if (timerPosIncurrenTimers.containsKey(timestamp)) {
                // 加入对应的timer，并且更新车辆的timer

                currenTimers.get(timerPosIncurrenTimers.get(timestamp)).f1.put(value.getVEHICLEID(),
                        value.getGANTRYNAME());
                vehicleTimer.put(value.getVEHICLEID(), timestamp);
            } else {
                // 注册一个timer
                ctx.timerService().registerEventTimeTimer(timestamp);
                // 将车的timer信息添加
                vehicleTimer.put(value.getVEHICLEID(), timestamp);
                // 将timer加入currenTimers的适当位置,同时更新timerPosIncurrenTimers里面的索引
                List<Tuple2<Long, Map<String, String>>> nextTimers = new ArrayList<>(currenTimers.size() + 1);
                int i = 0;
                int j = 0;
                for (; i < currenTimers.size(); i++) {
                    if (currenTimers.get(i).f0 < timestamp) {
                        nextTimers.add(currenTimers.get(i));
                        j++;
                    } else {
                        break;
                    }
                }
                timerPosIncurrenTimers.put(timestamp, j);
                nextTimers.add(new Tuple2<Long, Map<String, String>>(timestamp, new HashMap<>()));
                j++;
                for (; i < currenTimers.size(); i++) {
                    timerPosIncurrenTimers.put(currenTimers.get(i).f0, j);
                    nextTimers.add(currenTimers.get(i));
                    j++;
                }
                currenTimers=nextTimers;
            }
        }
    }

    @Override
    public void snapshotState(FunctionSnapshotContext context) throws Exception {
        // TODO Auto-generated method stub
        checkpointedTimers.clear();
        for (Tuple2<Long, Map<String, String>> element : currenTimers) {
            checkpointedTimers.add(element);
        }

    }

    @Override
    public void initializeState(FunctionInitializationContext context) throws Exception {
        // TODO Auto-generated method stub
        ListStateDescriptor<Tuple2<Long, Map<String, String>>> descriptor = new ListStateDescriptor<>(
                "timers",
                TypeInformation.of(new TypeHint<Tuple2<Long, Map<String, String>>>() {
                }));
        checkpointedTimers = context.getOperatorStateStore().getUnionListState(descriptor);
        
        if (context.isRestored()) {
            currenTimers.clear();
            vehicleTimer.clear();
            timerPosIncurrenTimers.clear();
            int i = 0;
            for (Tuple2<Long, Map<String, String>> element : checkpointedTimers.get()) {
                timerPosIncurrenTimers.put(element.f0, i);
                currenTimers.add(element);
                i++;
                for (String vehicle : element.f1.values()) {
                    vehicleTimer.put(vehicle, element.f0);
                }
            }
        }
    }

    @Override
    public void onTimer(long timestamp, ProcessFunction<GantryRecordSimple, AbnormalVehicle>.OnTimerContext ctx,
            Collector<AbnormalVehicle> out) throws Exception {
        int start = 0;
        int end = currenTimers.size() - 1;
        int mid;
        while (start < end) {
            mid = start + (end - start) / 2;
            if (currenTimers.get(mid).f0 >= timestamp) {
                end=mid;
            } else{
                start=mid+1;
            }
        }
        System.out.println(start);
        if (currenTimers.get(start).f0==timestamp){//正常情况下，应该是相等的
            //找到,直接将之前的timer认定超时
            int i=0;
            for (;i<=start;i++){
                Tuple2<Long, Map<String, String>> tmp=currenTimers.get(i);
                //将timer的下标删除
                timerPosIncurrenTimers.remove(tmp.f0);
                for (Entry<String,String> vehicle :tmp.f1.entrySet()){
                    //先将车辆的timer记录删除，不删除也行
                    vehicleTimer.remove(vehicle.getKey());
                    out.collect(new AbnormalVehicle(vehicle.getKey(),vehicle.getValue()));
                }
                
            }
            List<Tuple2<Long, Map<String, String>>> nextTimers = new ArrayList<>(currenTimers.size()-start+1);
            int j=0;
            for (;i<currenTimers.size();i++){
                //将timerPosIncurrenTimers里timer的下标更新
                timerPosIncurrenTimers.put(currenTimers.get(i).f0, j);
                nextTimers.add(currenTimers.get(i));
                j++;
            }
            currenTimers=nextTimers;
        }
    }
}
