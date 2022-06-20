package com.nju.ics.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.nju.ics.models.HeartBeatAndRecord;
import com.nju.ics.models.TimeoutEvent;
import com.nju.ics.models.TimerRecord;

@Mapper()
public interface TimeoutEventMapper {
    TimeoutEventMapper INSTANCE = Mappers.getMapper(TimeoutEventMapper.class);

    @Mapping(source = "VEHICLEID", target = "vehicleid")
    @Mapping(source = "STATIONID", target = "stationid")
    @Mapping(source = "TIME", target = "lasttimestamp")
    TimeoutEvent timerecordToTimeoutEvent(TimerRecord timerecord);

    @Mapping(target = "vehicleid", source = "record.VEHICLEID")
    @Mapping(target = "stationid", source = "record.STATIONID")
    @Mapping(target = "lasttimestamp", source = "record.TIME")
    TimeoutEvent HeartBeatAndRecordToTimeoutEvent(HeartBeatAndRecord record);
}
