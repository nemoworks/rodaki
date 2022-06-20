package com.nju.ics.analyses;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nju.ics.funcs.Row2JSONObject;
import com.nju.ics.utils.DataSourceJudge;
import com.nju.ics.utils.GetColInfo;

import org.apache.flink.core.fs.Path;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.io.RowCsvInputFormat;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

/**
 * 统计不同门架之间的时间分布情况
 */
public class GantryTime {
	public static TypeInformation[] getTypeInfo(String file_path) {
		List<TypeInformation> types = new ArrayList<TypeInformation>(60);

		List<Map<String, Object>> infos = JSON.parseObject(file_path, List.class);

		for (Map<String, Object> entry : infos) {
			switch ((String) entry.get("type")) {
				case "string":
					types.add(Types.STRING);
					break;
				case "int":
					types.add(Types.STRING);
					break;
				case "float":
					types.add(Types.STRING);
					break;
				default:
					types.add(Types.STRING);
					break;
			}
		}

		return types.toArray(new TypeInformation[0]);

	}

	public static int[] getIdx(String file_path) {
		List<Integer> types = new ArrayList<Integer>(60);

		List<Map<String, Object>> infos = JSON.parseObject(file_path, List.class);

		for (Map<String, Object> entry : infos) {
			types.add((int) entry.get("index"));
		}

		return types.stream().mapToInt(Integer::valueOf).toArray();

	}

	public static String[] getColNames(String file_path) {
		List<String> types = new ArrayList<String>(150);

		List<Map<String, Object>> infos = JSON.parseObject(file_path,List.class);

		for (Map<String, Object> entry : infos) {
			types.add((String) entry.get("col"));
		}

		return types.toArray(new String[0]);
	}

	public static void main(String[] args) throws Exception {
		// set up the streaming execution environment
		final ParameterTool params = ParameterTool.fromArgs(args);

		Configuration conf = new Configuration();

		conf.setInteger("rest.port", 9000);
		StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);
		// 输入文件路径
		String gantrycsv = "/hdd/data/1101/gantrywaste_fix.csv";
		// 使用 RowCsvInputFormat 把每一行记录解析为一个 Row
		RowCsvInputFormat csvGantryInput = new RowCsvInputFormat(
				new Path(gantrycsv), // 文件路径
				GantryTime.getTypeInfo("/home/lzm/zc/simulate/gantry_time_gantrywaste.json"), // 字段类型
				"\n", // 行分隔符
				"€",
				GantryTime.getIdx("/home/lzm/zc/simulate/gantry_time_gantrywaste.json"),
				false); // 字段分隔符
		// System.out.println(GetColInfo.getTypeInfo("/gantrywaste.json").length);
		// System.out.println(Arrays.toString(GetColInfo.getIdx("/gantrywaste.json")));
		csvGantryInput.setSkipFirstLineAsHeader(true);
		csvGantryInput.setLenient(true);
		DataStream<JSONObject> rawGantry = env.readFile(csvGantryInput, gantrycsv)
				.map(new Row2JSONObject(GantryTime.getColNames("/home/lzm/zc/simulate/gantry_time_gantrywaste.json")));

		SingleOutputStreamOperator<JSONObject> timeGantry = rawGantry.map(new MapFunction<JSONObject, JSONObject>() {
			SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			@Override
			public JSONObject map(JSONObject value) throws Exception {
				// TODO Auto-generated method stub
				try {
					value.put(DataSourceJudge.timeKey, time.parse(value.getString("TRANSTIME")).getTime());
				} catch (Exception e) {
					value.put(DataSourceJudge.timeKey, 0);
				}

				return value;
			}

		});
		DataStream<JSONObject> FilterdVehicleRecordsStream = timeGantry.filter(new RichFilterFunction<JSONObject>() {

			@Override
			public void open(Configuration parameters) throws Exception {
				// TODO Auto-generated method stub
				super.open(parameters);
			}
			@Override
			public boolean filter(JSONObject value) throws Exception {
				// TODO Auto-generated method stub

				return true;
			}

		});
		FilterdVehicleRecordsStream.keyBy(x->x.getString("GANTRYHEX")).process(new KeyedProcessFunction<String,JSONObject,JSONObject>() {
			@Override
			public void open(Configuration parameters) throws Exception {
				
			};
			@Override
			public void processElement(JSONObject value,
					KeyedProcessFunction<String, JSONObject, JSONObject>.Context ctx, Collector<JSONObject> out)
					throws Exception {
				// TODO Auto-generated method stub
				
			}
			
		});
		env.execute("gantry time extract");
	}
}
