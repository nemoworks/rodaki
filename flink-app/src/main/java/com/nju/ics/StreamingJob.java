/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nju.ics;

import java.time.Duration;
import java.util.Properties;


import com.nju.ics.Utils.ConfigureENV;
import com.nju.ics.Utils.DataFlowBuilder;
import com.nju.ics.Utils.InputTimeStampAssigner;
import com.nju.ics.Utils.JsonObjectDeserializationSchema;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.runtime.state.storage.FileSystemCheckpointStorage;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;

/**
 * Skeleton for a Flink Streaming Job.
 *
 * <p>
 * For a tutorial how to write a Flink streaming application, check the
 * tutorials and examples on the
 * <a href="https://flink.apache.org/docs/stable/">Flink Website</a>.
 *
 * <p>
 * To package your application into a JAR file for execution, run 'mvn clean
 * package' on the command line.
 *
 * <p>
 * If you change the name of the main class (with the public static void
 * main(String[] args)) method, change the respective entry in the POM.xml file
 * (simply search for 'mainClass').
 */
public class StreamingJob {
	public static final String CHECKPOINTING_OPTION = "checkpointing";
	public static final String OPERATOR_CHAINING_OPTION = "chaining";
	public static final String PARALLELISM = "parallelism";

	public static void main(String[] args) throws Exception {


		// set up the streaming execution environment
		final ParameterTool params = ParameterTool.fromArgs(args);
		Properties initparams = ConfigureENV.initConfiguration("/application.properties");

		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		ConfigureENV.configureEnvironment(params, env);

		DataFlowBuilder.generateDataStream(env, params);
		// execute program
		env.execute("highway data analyse");
	}


}
