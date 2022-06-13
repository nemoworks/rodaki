package com.nju.ics.Mappers;

import com.nju.ics.Models.HeartBeatAndRecord;
import com.nju.ics.Models.TimeoutEvent;
import com.nju.ics.Models.TimerRecord;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

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
