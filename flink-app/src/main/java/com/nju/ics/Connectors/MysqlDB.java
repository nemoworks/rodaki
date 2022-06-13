package com.nju.ics.Connectors;

import com.mysql.cj.jdbc.MysqlXADataSource;

import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExactlyOnceOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

public class MysqlDB {

        public static <T> SinkFunction<T> generateJDBCSink(String sql, JdbcStatementBuilder<T> lambda) {
                return JdbcSink.sink(
                                sql,
                                lambda,
                                JdbcExecutionOptions.builder()
                                                .withBatchSize(5000)
                                                .withBatchIntervalMs(0)
                                                .withMaxRetries(1)
                                                .build(),
                                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                                                .withUrl("jdbc:mysql://localhost:13306/test?rewriteBatchedStatements=true")
                                                .withUsername("root")
                                                .withPassword("123456")
                                                .withDriverName("com.mysql.cj.jdbc.Driver")
                                                .withConnectionCheckTimeoutSeconds(5)
                                                .build());
        }
}
