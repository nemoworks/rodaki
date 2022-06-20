package com.nju.ics.connectors;

import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.core.fs.Path;
import org.apache.flink.formats.sequencefile.SequenceFileWriterFactory;
import org.apache.flink.runtime.util.HadoopUtils;
import org.apache.flink.api.common.serialization.BulkWriter;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.GlobalConfiguration;
import org.apache.hadoop.io.Text;

import com.nju.ics.snapshots.AbstractSnapshot;

import org.apache.hadoop.io.ObjectWritable;

public class HadoopFS {
        public static CustomFileSink<Tuple2<Text, Text>> generateHadoopSink(String outputPath)
                        throws IOException {
                Configuration hadoopConf = HadoopUtils.getHadoopConfiguration(GlobalConfiguration.loadConfiguration());
                BulkWriter.Factory<Tuple2<Text, Text>> seq = new SequenceFileWriterFactory<>(hadoopConf, Text.class,
                                Text.class);
                final CustomFileSink<Tuple2<Text, Text>> sink = CustomFileSink
                                .forBulkFormat(new Path(outputPath),
                                seq)
                                .build();

                return sink;
        }
}
