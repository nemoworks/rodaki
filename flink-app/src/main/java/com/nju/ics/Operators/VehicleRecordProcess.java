package com.nju.ics.Operators;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.nju.ics.Configs.GantryPosition;
import com.nju.ics.Configs.MediaType;
import com.nju.ics.Configs.StationPosition;
import com.nju.ics.ModelExtractors.CPCCardExtractor;
import com.nju.ics.ModelExtractors.ENStationRecordExtractor;
import com.nju.ics.ModelExtractors.ENVehicleRecordExtractor;
import com.nju.ics.ModelExtractors.GantryRecordExtractor;
import com.nju.ics.ModelExtractors.OBUCardExtractor;
import com.nju.ics.ModelExtractors.StationRecordExtractor;
import com.nju.ics.ModelExtractors.TrafficRecordExtractor;
import com.nju.ics.ModelExtractors.TrafficTransactionExtractor;
import com.nju.ics.ModelExtractors.VehicleExtractor;
import com.nju.ics.Models.CPCCard;
import com.nju.ics.Models.GantryRecord;
import com.nju.ics.Utils.ConfigureENV;
import com.nju.ics.Utils.DataSourceJudge;
import com.nju.ics.Utils.OutputTagCollection;
import com.nju.ics.Models.Media;
import com.nju.ics.Models.StationRecord;
import com.nju.ics.Models.TrafficTransaction;
import com.nju.ics.Models.Vehicle;
import com.nju.ics.Models.*;
import org.apache.commons.collections.IteratorUtils;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map;
import com.nju.ics.ModelExtractors.GeneralExtractor;
import org.apache.flink.api.java.tuple.Tuple2;

public class VehicleRecordProcess extends KeyedProcessFunction<String, JSONObject, String> {
	/** 入站记录 */
	ValueState<String> entryStationId;
	/** 出站记录 */
	ValueState<String> exitStationId;
	/** 门架记录 */
	ListState<String> gantryRecordIds;
	/** 通行介质id obu or cpc */
	ValueState<String> mediaId;
	/** 之前的通行标识ID 用来判断是否是新的通行 */
	ValueState<String> previousPassId;

	// Map<String, GeneralExtractor> modelExtractors;
	// Map<String, Tuple2<Boolean, Boolean>> extrctors;

	StateTtlConfig ttlConfig = StateTtlConfig.newBuilder(Time.days(1))
			.setUpdateType(StateTtlConfig.UpdateType.OnReadAndWrite)
			.setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired).build();

	public VehicleRecordProcess() {
		// this.extrctors = extrctors;
	}

	@Override
	public void open(Configuration parameters) throws Exception {

		GantryPosition.initGantryPosition();
		StationPosition.initStationPosition();
		OutputTagCollection.initCollection();
		// this.modelExtractors=new HashMap<String, GeneralExtractor>();
		// this.modelExtractors.put(VehicleExtractor.class.getSimpleName(),
		// new VehicleExtractor(Vehicle.class,
		// extrctors.get(VehicleExtractor.class.getSimpleName()).f0,
		// extrctors.get(VehicleExtractor.class.getSimpleName()).f1));

	}

	@Override
	public void processElement(JSONObject value, KeyedProcessFunction<String, JSONObject, String>.Context ctx,
			Collector<String> out) throws Exception {
		// TODO Auto-generated method stub
		// 如果关键的字段解析有问题，只能不处理了
		try {
			value.getIntValue("MEDIATYPE");
		} catch (Exception e) {
			return;
		}

		int source = value.getIntValue(DataSourceJudge.sourceKey);

		switch (source) {
			case DataSourceJudge.entryLane:

				OutputTagCollection.modelExtractors.get(ENVehicleRecordExtractor.class.getSimpleName()).f0
						.processElement(value, ctx, source, null, null);
				OutputTagCollection.modelExtractors.get(ENStationRecordExtractor.class.getSimpleName()).f0
						.processElement(value, ctx, source, null, null);
				break;

			default:
				break;
		}

	}

	public void clear() {
		entryStationId.clear();
		exitStationId.clear();
		gantryRecordIds.clear();
		previousPassId.clear();
		mediaId.clear();
	}

}
