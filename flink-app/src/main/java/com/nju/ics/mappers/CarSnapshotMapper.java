package com.nju.ics.mappers;

import com.nju.ics.funcs.CarSnapshotProcess;
import com.nju.ics.models.Car;
import com.nju.ics.models.TimerRecord;
import com.nju.ics.snapshots.CarSnapshot;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(imports = CarSnapshotProcess.class)
public interface CarSnapshotMapper {
    CarSnapshotMapper INSTANCE = Mappers.getMapper(CarSnapshotMapper.class);

    @Mapping(target = "ID", expression = "java(String.format(\"%s-%s\", timerecord.getTIME(), timerecord.getVEHICLEID()))")
    @Mapping(source = "TIME", target = "timestamp")
    @Mapping(source = "VEHICLEID", target = "carid")
    @Mapping(target = "longitude", expression = "java(CarSnapshotProcess.getLongitude(timerecord.getFLOWTYPE(),timerecord.getSTATIONID()))")
    @Mapping(target = "latitude", expression = "java(CarSnapshotProcess.getLatitude(timerecord.getFLOWTYPE(),timerecord.getSTATIONID()))")
    @Mapping(target = "operationCall", expression = "java(CarSnapshotProcess.getOperationCall(timerecord.getFLOWTYPE()))")
    CarSnapshot timerecordToCarSnapshot(TimerRecord timerecord);

    @Mapping(source = "VEHICLEID", target = "VEHICLEID")
    @Mapping(source = "VLPC", target = "VLPC")
    @Mapping(source = "VLP", target = "VLP")
    @Mapping(source = "PASSID", target = "PASSID")
    @Mapping(source = "STATIONID", target = "STATIONID")
    @Mapping(source = "FLOWTYPE", target = "STATIONTYPE")
    @Mapping(source = "TIME", target = "TIME")
    Car timerecordToCar(TimerRecord timerecord);

}
