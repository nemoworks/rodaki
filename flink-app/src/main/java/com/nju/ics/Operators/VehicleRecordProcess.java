package com.nju.ics.Operators;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.nju.ics.Configs.GantryPosition;
import com.nju.ics.Configs.MediaType;
import com.nju.ics.ModelExtractors.CPCCardExtractor;
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
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import java.util.Properties;

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

	@Override
	public void open(Configuration parameters) throws Exception {
		ValueStateDescriptor<String> entryStationIdDescriptor = new ValueStateDescriptor<>("entryStationId event",
				String.class);
		entryStationId = getRuntimeContext().getState(entryStationIdDescriptor);

		ValueStateDescriptor<String> exitStationIdDescriptor = new ValueStateDescriptor<>("exitStationId event",
				String.class);
		exitStationId = getRuntimeContext().getState(exitStationIdDescriptor);
		ListStateDescriptor<String> gantryRecordIdsDescriptor = new ListStateDescriptor<>("gantryRecordId events",
				String.class);
		gantryRecordIds = getRuntimeContext().getListState(gantryRecordIdsDescriptor);
		ValueStateDescriptor<String> previousPassIdDescriptor = new ValueStateDescriptor<>("mediaId event",
				String.class);
		previousPassId = getRuntimeContext().getState(previousPassIdDescriptor);
		ValueStateDescriptor<String> mediaIdDescriptor = new ValueStateDescriptor<>("mediaId event", String.class);
		mediaId = getRuntimeContext().getState(mediaIdDescriptor);

		OutputTagCollection.initCollection();
		GantryPosition.initGantryPosition();
		Properties initparams = ConfigureENV.initConfiguration("/application.properties");
	}

	@Override
	public void processElement(JSONObject value, KeyedProcessFunction<String, JSONObject, String>.Context ctx,
			Collector<String> out) throws Exception {
		// TODO Auto-generated method stub
		int source = DataSourceJudge.typeDetect(value);
		// 先判断是否是新的通行标识ID
		String currentPassId = "";
		if (source == DataSourceJudge.entryLane || source == DataSourceJudge.exitLane) {

			currentPassId = value.getString("通行标识ID");

		} else {
			currentPassId = value.getString("通行标识 ID");
		}
		if (previousPassId.value() != null && currentPassId != previousPassId.value()) {
			// 说明是新的通行
			this.clear();
		}

		// 1.产生一个车辆 Vehicle
		Vehicle vehicle = (Vehicle) OutputTagCollection.modelExtractors.get(VehicleExtractor.class.getSimpleName()).f0
				.processElement(value, ctx, source, null, null);
		mediaId.update(vehicle == null ? null : vehicle.id());
		// 2. 产生一个obu或者cpc模型,并将Vehicle连接到media上
		Media media = null;
		if (value.getIntValue("通行介质类型") == MediaType.cpc) {
			media = (Media) OutputTagCollection.modelExtractors.get(CPCCardExtractor.class.getSimpleName()).f0
					.processElement(value, ctx, source, null, Arrays.asList(vehicle));
		} else if (value.getIntValue("通行介质类型") == MediaType.obu) {
			media = (Media) OutputTagCollection.modelExtractors.get(OBUCardExtractor.class.getSimpleName()).f0
					.processElement(value, ctx, source, null, Arrays.asList(vehicle));
		}
		// 3. 产生入口 、出口记录时，需要生成站点记录StationRecord ；产生门架记录时，需要生成门架记录GantryRecord
		GantryRecord gantryRecord = null;
		StationRecord stationRecord = null;
		TrafficTransaction trafficTransaction = null;
		switch (source) {
			case DataSourceJudge.entryLane:
				/* 说明之前没有入站记录，是正常的,就插入记录 */
				stationRecord = (StationRecord) OutputTagCollection.modelExtractors
						.get(StationRecordExtractor.class.getSimpleName()).f0.processElement(value, ctx, source, null,
								Arrays.asList(vehicle));
				entryStationId.update(stationRecord == null ? null : stationRecord.id());
				vehicle.lastPassStation = stationRecord.stationId;
				break;
			case DataSourceJudge.exitLane:
				stationRecord = (StationRecord) OutputTagCollection.modelExtractors
						.get(StationRecordExtractor.class.getSimpleName()).f0.processElement(value, ctx, source, null,
								Arrays.asList(vehicle));
				exitStationId.update(stationRecord.id());
				vehicle.lastPassStation = stationRecord.stationId;
				break;
			case DataSourceJudge.gantryCharge:
				gantryRecord = (GantryRecord) OutputTagCollection.modelExtractors
						.get(GantryRecordExtractor.class.getSimpleName()).f0.processElement(value, ctx, source, null,
								Arrays.asList(media));
				gantryRecordIds.add(gantryRecord.id());
				vehicle.lastPassStation = gantryRecord.gantryId;
				break;
			default:
				break;
		}
		// 将车辆的位置提取出来
		if (vehicle.lastPassStation != null) {
			vehicle.location = GantryPosition.geoMap.containsKey(vehicle.lastPassStation)
					? GantryPosition.geoMap.get(vehicle.lastPassStation).toIotDBString()
					: null;
		}

		// 4.产生通行交易TrafficTransaction
		trafficTransaction = (TrafficTransaction) OutputTagCollection.modelExtractors
				.get(TrafficTransactionExtractor.class.getSimpleName()).f0.processElement(value, ctx, source, null,
						null);
		// 5.产生通行记录
		Iterable<String> pathsIterable = gantryRecordIds.get();
		List<String> tmp = new ArrayList<String>(
				Arrays.asList(entryStationId.value(), exitStationId.value(), mediaId.value()));
		if (pathsIterable != null) {
			List<String> results = Lists.newArrayList(pathsIterable.iterator());

			tmp.addAll(results);

		}
		TrafficRecord trafficRecord = (TrafficRecord) OutputTagCollection.modelExtractors
				.get(TrafficRecordExtractor.class.getSimpleName()).f0.processElement_string(value, ctx, source, null,
						tmp);
		// 6.将通行记录的id记录在通行交易里,再将通行交易id记录在车辆里
		if (trafficTransaction != null) {
			trafficTransaction.trafficRecordId = trafficRecord == null ? null : trafficRecord.id();

			vehicle.trafficTransactionid = trafficTransaction.id();
			trafficTransaction.vehicleId = vehicle.id();
			OutputTagCollection.modelExtractors.get(TrafficTransactionExtractor.class.getSimpleName()).f0
					.sinkEntity(trafficTransaction, ctx);
		}

		// 7.将vehicle与trafficTransaction sink
		OutputTagCollection.modelExtractors.get(VehicleExtractor.class.getSimpleName()).f0.sinkEntity(vehicle, ctx);

	}

	public void clear() {
		entryStationId.clear();
		exitStationId.clear();
		gantryRecordIds.clear();
		previousPassId.clear();
		mediaId.clear();
	}

}
