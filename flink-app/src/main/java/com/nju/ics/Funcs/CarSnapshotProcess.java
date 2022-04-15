package com.nju.ics.Funcs;

import com.nju.ics.Configs.GantryPosition;
import com.nju.ics.Configs.StationPosition;
import com.nju.ics.Mappers.CarSnapshotMapper;
import com.nju.ics.ModelExtractors.CarExtractor;
import com.nju.ics.Models.Car;
import com.nju.ics.Models.TimerRecord;
import com.nju.ics.Snapshots.AbstractSnapshot;
import com.nju.ics.Snapshots.CarSnapshot;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

public class CarSnapshotProcess extends KeyedProcessFunction<String, TimerRecord, CarSnapshot> {
    private ValueState<CarSnapshot> preCarSnapshot;
    ValueStateDescriptor<CarSnapshot> preCarSnapshotDescriptor = new ValueStateDescriptor<CarSnapshot>("preCarSnapshot",
            CarSnapshot.class);
    private CarExtractor carExtractor;

    @Override
    public void processElement(TimerRecord value, KeyedProcessFunction<String, TimerRecord, CarSnapshot>.Context ctx,
            Collector<CarSnapshot> out) throws Exception {
        // TODO Auto-generated method stub
        // TODO Auto-generated method stub
        if (!this.isRecordValid(value)) {
            return;
        }
        Car car = CarSnapshotMapper.INSTANCE.timerecordToCar(value);
        CarSnapshot carSnapshot = CarSnapshotMapper.INSTANCE.timerecordToCarSnapshot(value);
        carSnapshot.setCar(car);

        if (preCarSnapshot.value() != null) {
            preCarSnapshot.value().setSnapshotNext(carSnapshot.id());
            out.collect(preCarSnapshot.value());
            carSnapshot.setSnapshotPre(preCarSnapshot.value().id());
        }

        switch (value.getFLOWTYPE()) {
            case 1:
                // 入站
                break;
            case 2:
                // 门架
                if (value.getPROVINCEBOUND() == 2) {
                    // 省界出口门架
                    preCarSnapshot.clear();
                    out.collect(carSnapshot);
                } else {
                    preCarSnapshot.update(carSnapshot);
                }
                break;
            case 3:
                // 出站
                preCarSnapshot.clear();
                out.collect(carSnapshot);
                break;
        }

    }

    @Override
    public void open(Configuration parameters) throws Exception {
        // TODO Auto-generated method stub
        super.open(parameters);
        // validPassidDescriptor.enableTimeToLive(ttlConfig);
        preCarSnapshot = getRuntimeContext().getState(preCarSnapshotDescriptor);
        this.carExtractor = new CarExtractor(CarSnapshot.class);
        GantryPosition.initGantryPosition();
        StationPosition.initStationPosition();
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

    public static float getLatitude(int flowtype, String id) {
        switch (flowtype) {
            case 1:
            case 3:
                return StationPosition.getLatitude(id);
            case 2:
                return GantryPosition.getLatitude(id);
        }
        return 0.0f;
    }

    public static float getLongitude(int flowtype, String id) {
        switch (flowtype) {
            case 1:
            case 3:
                return StationPosition.getLongitude(id);
            case 2:
                return GantryPosition.getLongitude(id);
        }
        return 0.0f;
    }

    public static String getOperationCall(int flowtype) {
        switch (flowtype) {
            case 1:
                return AbstractSnapshot.EntryOperationCall;
            case 3:
                return AbstractSnapshot.ExitOperationCall;
            case 2:
                return AbstractSnapshot.PassOperationCall;
        }
        return "";
    }
}
