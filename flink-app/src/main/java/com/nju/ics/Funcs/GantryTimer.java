package com.nju.ics.Funcs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashSet;
import java.util.Set;
import com.alibaba.fastjson.JSON;
import com.nju.ics.Configs.GantryPosition;
import com.nju.ics.Fields.AbnormalVehicle;
import com.nju.ics.Models.GantryRecord;
import com.nju.ics.Models.GantryRecordSimple;
import com.nju.ics.Models.TimerRecord;

import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

/**
 * 处理门架记录model，根据车辆通过门架的时间与相邻两门架的时间来检测异常
 */
public class GantryTimer extends KeyedProcessFunction<String, TimerRecord, AbnormalVehicle>
        implements CheckpointedFunction {
    private ListState<Tuple2<Long, Map<String, AbnormalVehicle>>> checkpointedTimers;
    private MapState<String, String> passid2vehicle;
    private MapState<String, String> vehicle2passid;
    MapStateDescriptor<String, String> passid2vehicleMapStateDescriptor = new MapStateDescriptor<String, String>(
            "passid2vehicle", String.class, String.class);
    MapStateDescriptor<String, String> vehicle2passidMapStateDescriptor = new MapStateDescriptor<String, String>(
            "vehicle2passid", String.class, String.class);
    private List<Tuple2<Long, Map<String, AbnormalVehicle>>> currenTimers;
    private Map<Long, Integer> timerPosIncurrenTimers;
    private Map<String, Long> vehicleTimer;
    private Map<String, Integer> serviceArea;
    private Map<String, String> gantryid2hex;
    private Map<String, Integer> siteTime;
    private static long defaulttime;
    private static long fiveminutes = 1000 * 60 * 5;
    private static long oneday = Duration.ofDays(1).toMillis();
    private static long onehour = Duration.ofHours(1).toMillis();
    private static long fivehour = Duration.ofHours(5).toMillis();
    private static long offset = Duration.ofHours(8).toMillis();
    private static Set<String> ignoresations = new HashSet<String>() {
        {
            add("G00203700500132013");
            add("G03213700600071003");
            add("G00203700400082008");
            add("G00033700500041001");
            add("G00253700500111006");
        }
    };

    public GantryTimer(String gantryfile) throws FileNotFoundException, IOException {
        // this.siteTime = JSON.parseObject(new FileInputStream(gantryfile), Map.class);
        // GantryTimer.defaulttime = this.siteTime.get("avg");
        GantryTimer.defaulttime = fiveminutes * 60;
        this.siteTime = new HashMap<>();
        this.serviceArea = JSON.parseObject(new FileInputStream("/home/lzm/zc/simulate/serviceArea.json"), Map.class);
        this.gantryid2hex = JSON.parseObject(new FileInputStream("/home/lzm/zc/simulate/gantryid2hex.json"), Map.class);
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        // TODO Auto-generated method stub
        super.open(parameters);
        currenTimers = new ArrayList<>();
        timerPosIncurrenTimers = new HashMap<Long, Integer>();
        vehicleTimer = new HashMap<String, Long>();
        GantryPosition.initGantryPosition();// 初始化门架经纬度
        passid2vehicle = getRuntimeContext().getMapState(passid2vehicleMapStateDescriptor);
        vehicle2passid = getRuntimeContext().getMapState(vehicle2passidMapStateDescriptor);
    }

    @Override
    public void close() {

    }

    @Override
    public void processElement(TimerRecord value,
            KeyedProcessFunction<String, TimerRecord, AbnormalVehicle>.Context ctx,
            Collector<AbnormalVehicle> out) throws Exception {
        // TODO Auto-generated method stub

        long timestamp;
        if (value.getVEHICLETYPE() >= 11 && value.getVEHICLETYPE() <= 16) {
            // if (value.getPASSID().startsWith("000000")) {
            // return;
            // }
            // 如果是省界门架且特情类型为154或186，排除
            if (value.getPROVINCEBOUND() == 1
                    && (value.getSPECIALTYPE().contains("154") || value.getSPECIALTYPE().contains("186"))) {
                return;
            }
            // 需要清楚timer的车牌，下面进行矫正
            String timervehicleid = value.getVEHICLEID();
            // if (value.getPASSID().equals("020000320101610167125820211101151055")) {
            // System.out.printf("1. %s->%s\n", value.getPASSID(), value.getVEHICLEID());
            // System.out.println(passid2vehicle.get(value.getPASSID()));
            // }
            // 如果是省界入口门架或者入口站点记录，认为passid与车牌是对应的，需要记录一下
            if (!value.getPASSID().startsWith("000000") && (value.getFLOWTYPE() == 1
                    || value.getPROVINCEBOUND() == 1)) {
                // if (value.getPASSID().equals("020000320101610167125820211101151055")) {
                // System.out.printf("2. %s->%s\n", value.getPASSID(), value.getVEHICLEID());
                // }
                passid2vehicle.put(value.getPASSID(), value.getVEHICLEID());
                // vehicle2passid.put(value.getVEHICLEID(), value.getPASSID());
            } else if (value.getORIGINALFLAG() == 2
                    || value.getPROVINCEBOUND() == 2 || value.getFLOWTYPE() == 3) {
                // 在省界出口、虚门架、出口站点矫正一下
                if (value.getMEDIATYPE() == 1) {
                    // 如果是obu卡，认为之前的入口记录是有效的，这个时候应当通过入口passid对应的车牌纠正出口的车牌
                    if (passid2vehicle.contains(value.getPASSID())
                            && !passid2vehicle.get(value.getPASSID()).equals(value.getVEHICLEID())) {
                        // System.out.printf("%s->%s\n", passid2vehicle.get(value.getPASSID()),
                        // value.getVEHICLEID());
                        value.setVEHICLEID(passid2vehicle.get(value.getPASSID()));
                        timervehicleid = value.getVEHICLEID();
                    }
                } else if (value.getMEDIATYPE() == 2) {
                    // 如果是cpc卡，认为出口的车牌是正确的，这个时候通过passid，将要设置timer的车牌号重新设置成原先错误的车牌，保证其timer能够删除
                    // if (value.getVEHICLEID().equals("冀GE5391-1")) {
                    // System.out.printf("3. %s->%s->%s\n", value.getVEHICLEID(), value.getPASSID(),
                    // passid2vehicle.get(value.getPASSID()));
                    // }
                    if (passid2vehicle.contains(value.getPASSID())
                            && !passid2vehicle.get(value.getPASSID()).equals(value.getVEHICLEID())) {
                        // if (value.getVEHICLEID().equals("冀GE5391-1")) {
                        // System.out.printf("4. %s->%s\n", value.getVEHICLEID(),
                        // passid2vehicle.get(value.getPASSID()));
                        // }
                        // System.out.printf("4. %s->%s\n", value.getVEHICLEID(),
                        // passid2vehicle.get(value.getPASSID()));
                        timervehicleid = passid2vehicle.get(value.getPASSID());
                    }
                }

            }
            // 经过矫正之后仍然不合法
            if (timervehicleid.startsWith("默") || timervehicleid.startsWith("0")) {
                return;
            }
            // 1. 查找该车在哪个timer注册过,有就先移除；再清除车的timer信息
            if (vehicleTimer.containsKey(timervehicleid)) {
                timestamp = vehicleTimer.get(timervehicleid);
                if (timerPosIncurrenTimers.containsKey(timestamp)) {
                    // 如果能够通过时间戳获得下标,就直接从对应的timer map里面删除这辆车
                    try {
                        currenTimers.get(timerPosIncurrenTimers.get(timestamp)).f1.remove(timervehicleid);
                    } catch (Exception e) {
                        System.out.println(timerPosIncurrenTimers.get(timestamp));
                    }

                }
                vehicleTimer.remove(timervehicleid);
            }
            // 判断是否是虚门架
            if (value.getORIGINALFLAG() == 2) {
                return;
            }
            // 2. 判断是否是省界出口，如果是，则直接return
            if (value.getPROVINCEBOUND() == 2) {
                passid2vehicle.remove(value.getPASSID());
                return;
            }
            // 3.判断是否是出口记录，如果是，则直接return
            if (value.getFLOWTYPE() == 3) {
                passid2vehicle.remove(value.getPASSID());
                return;
            }
            if (value.getPASSID().startsWith("000000")) {
                return;
            }
            // 4. 判断时间是否有效，如果是凌晨1点到5点，直接return
            // if (!this.isTimeValid(ctx.timestamp())) {
            // return;
            // }
            if (ignoresations.contains(value.getSTATIONID())) {
                return;
            }
            // 5. 根据该车的通过时间计算该车应该放入哪个timer
            timestamp = ctx.timestamp();
            if (siteTime.containsKey(value.getSTATIONID())) {
                timestamp = timestamp + fiveminutes - timestamp % fiveminutes + siteTime.get(value.getSTATIONID());
            } else {
                timestamp = timestamp + fiveminutes - timestamp % fiveminutes + defaulttime;
            }
            // 6.查看这个timer是否已经有
            AbnormalVehicle entity = new AbnormalVehicle(value.getVEHICLEID(), value.getSTATIONID(),
                    value.getFLOWTYPE(), timestamp, value.getPASSID(), ctx.timestamp());
            if (timerPosIncurrenTimers.containsKey(timestamp)) {
                // 加入对应的timer，并且更新车辆的timer
                currenTimers.get(timerPosIncurrenTimers.get(timestamp)).f1.put(value.getVEHICLEID(), entity);
                vehicleTimer.put(value.getVEHICLEID(), timestamp);
            } else {
                // 注册一个timer
                ctx.timerService().registerEventTimeTimer(timestamp);
                // 将车的timer信息添加
                vehicleTimer.put(value.getVEHICLEID(), timestamp);
                // 将timer加入currenTimers的适当位置,同时更新timerPosIncurrenTimers里面的索引
                List<Tuple2<Long, Map<String, AbnormalVehicle>>> nextTimers = new ArrayList<>(currenTimers.size() + 1);
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
                nextTimers.add(new Tuple2<Long, Map<String, AbnormalVehicle>>(timestamp,
                        new HashMap<String, AbnormalVehicle>() {
                            {
                                put(value.getVEHICLEID(), entity);
                            }
                        }));
                j++;
                for (; i < currenTimers.size(); i++) {
                    timerPosIncurrenTimers.put(currenTimers.get(i).f0, j);
                    nextTimers.add(currenTimers.get(i));
                    j++;
                }
                currenTimers = nextTimers;
            }
        }
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

    public boolean isTimeValid(long timestamp) {
        long time = (timestamp + offset) % oneday;
        if (time < fivehour && time >= onehour) {

            return false;
        }
        return true;
    }

    @Override
    public void snapshotState(FunctionSnapshotContext context) throws Exception {
        // TODO Auto-generated method stub
        checkpointedTimers.clear();
        for (Tuple2<Long, Map<String, AbnormalVehicle>> element : currenTimers) {
            checkpointedTimers.add(element);
        }

    }

    @Override
    public void initializeState(FunctionInitializationContext context) throws Exception {
        // TODO Auto-generated method stub
        ListStateDescriptor<Tuple2<Long, Map<String, AbnormalVehicle>>> descriptor = new ListStateDescriptor<>(
                "timers",
                TypeInformation.of(new TypeHint<Tuple2<Long, Map<String, AbnormalVehicle>>>() {
                }));
        checkpointedTimers = context.getOperatorStateStore().getUnionListState(descriptor);

        if (context.isRestored()) {
            currenTimers.clear();
            vehicleTimer.clear();
            timerPosIncurrenTimers.clear();
            int i = 0;
            for (Tuple2<Long, Map<String, AbnormalVehicle>> element : checkpointedTimers.get()) {
                timerPosIncurrenTimers.put(element.f0, i);
                currenTimers.add(element);
                i++;
                for (AbnormalVehicle vehicle : element.f1.values()) {
                    vehicleTimer.put(vehicle.getVehicleid(), element.f0);
                }
            }
        }
    }

    @Override
    public void onTimer(long timestamp, KeyedProcessFunction<String, TimerRecord, AbnormalVehicle>.OnTimerContext ctx,
            Collector<AbnormalVehicle> out) throws Exception {

        int start = 0;
        int end = currenTimers.size() - 1;
        int mid;
        while (start < end) {
            mid = start + (end - start) / 2;
            if (currenTimers.get(mid).f0 >= timestamp) {
                end = mid;
            } else {
                start = mid + 1;
            }
        }
        // System.out.println(start);
        if (currenTimers.get(start).f0 == timestamp) {// 正常情况下，应该是相等的
            // 找到,直接将之前的timer认定超时
            int i = 0;
            for (; i <= start; i++) {
                Tuple2<Long, Map<String, AbnormalVehicle>> tmp = currenTimers.get(i);
                // 将timer的下标删除
                timerPosIncurrenTimers.remove(tmp.f0);
                for (Entry<String, AbnormalVehicle> vehicle : tmp.f1.entrySet()) {
                    // 先将车辆的timer记录删除，不删除也行
                    vehicleTimer.remove(vehicle.getKey());
                    // 判断下是否最近一次记录的站点id是否存在服务区
                    if (this.serviceArea
                            .containsKey(this.gantryid2hex.getOrDefault(vehicle.getValue().getStationid(), ""))) {
                        continue;
                    }
                    out.collect(vehicle.getValue());
                }

            }
            List<Tuple2<Long, Map<String, AbnormalVehicle>>> nextTimers = new ArrayList<>(
                    currenTimers.size() - start + 1);
            int j = 0;
            for (; i < currenTimers.size(); i++) {
                // 将timerPosIncurrenTimers里timer的下标更新
                timerPosIncurrenTimers.put(currenTimers.get(i).f0, j);
                nextTimers.add(currenTimers.get(i));
                j++;
            }
            currenTimers = nextTimers;
        }
    }
}
