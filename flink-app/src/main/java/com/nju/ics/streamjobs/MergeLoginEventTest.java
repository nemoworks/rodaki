package com.nju.ics.streamjobs;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.functions.PatternProcessFunction;
import org.apache.flink.cep.functions.TimedOutPartialMatchHandler;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

public class MergeLoginEventTest {
        public static void main(String[] args) throws Exception {
                // set up the streaming execution environment
                final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

                // 模拟输入流
                SingleOutputStreamOperator<LoginEvent> input = getInput(env);

                // 定义pattern
                Pattern<LoginEvent, ?> pattern = getPattern2();

                PatternStream<LoginEvent> patternStream = CEP.pattern(input, pattern)
                                .inProcessingTime();
                                
                //针对用pattern1的方式的处理函数
                // DataStream<List<LoginEvent>> result = patternStream.process(new MyPatternProcessFunction1())
                                // .setParallelism(1);
                
                //针对用pattern2的方式的处理函数
                DataStream<List<LoginEvent>> result = patternStream.process(new MyPatternProcessFunction2())
                .setParallelism(1);

                result.print();

                // execute program
                env.execute("Flink Streaming CEP Test ");
        }

        public static SingleOutputStreamOperator<LoginEvent> getInput(StreamExecutionEnvironment env) {
                SingleOutputStreamOperator<LoginEvent> input = env.addSource(new RichSourceFunction<LoginEvent>() {
                        boolean isClose = false;

                        @Override
                        public void run(SourceContext<LoginEvent> sourceContext) throws Exception {
                                long time = 0L;
                                int hex = 1;
                                Random random = new Random(0);
                                while (!isClose) {
                                        System.out.println("send data --------------|------------------");
                                        boolean picFirst = random.nextBoolean();
                                        if (picFirst) {
                                                sourceContext.collect(new LoginEvent("12345", "gantry-" + hex,
                                                                "GantryHex-" + hex,
                                                                "GantryPicID-" + hex, new Long(time), new Integer(97),
                                                                new Integer(2), new Integer(2)));
                                                time += 1000L;
                                        }
                                        int gantryRecords = random.nextInt(3);
                                        for (int i = 0; i < gantryRecords; i++) {
                                                sourceContext.collect(new LoginEvent("12345", "gantry-" + hex,
                                                                "GantryHex-" + hex,
                                                                "GantryPicID-" + hex, new Long(time), new Integer(97),
                                                                new Integer(2), new Integer(1)));
                                                time += 1000L;
                                        }
                                        if (!picFirst) {
                                                sourceContext.collect(new LoginEvent("12345", "gantry-" + hex,
                                                                "GantryHex-" + hex,
                                                                "GantryPicID-" + hex, new Long(time), new Integer(97),
                                                                new Integer(2), new Integer(2)));
                                                time += 1000L;
                                        }
                                        time += 10000L;
                                        hex++;
                                        Thread.sleep(5000);
                                }
                        }

                        @Override
                        public void cancel() {
                                System.out.println("close source");
                                isClose = true;
                        }
                }).setParallelism(1);

                input = input.assignTimestampsAndWatermarks(WatermarkStrategy
                                .<LoginEvent>forBoundedOutOfOrderness(
                                                Duration.ofSeconds(20))
                                .withTimestampAssigner(new SerializableTimestampAssigner<LoginEvent>() {
                                        @Override
                                        public long extractTimestamp(LoginEvent element, long recordTimestamp) {
                                                // TODO Auto-generated method stub
                                                return element.getGantryTime();
                                        }
                                }));
                return input;
        }

        public static Pattern<LoginEvent, ?> getPattern1() {
                // pattern设计，匹配所有连续出现的车牌相同、hex相同的LoginEvent对（不区分类型）
                Pattern<LoginEvent, ?> pattern = Pattern.<LoginEvent>begin("start", AfterMatchSkipStrategy.noSkip())
                                .where(
                                                new SimpleCondition<LoginEvent>() {
                                                        @Override
                                                        public boolean filter(LoginEvent event) {
                                                                return true;
                                                        }
                                                })
                                .followedBy("end").where(
                                                new SimpleCondition<LoginEvent>() {
                                                        @Override
                                                        public boolean filter(LoginEvent event, Context ctx) {
                                                                try {
                                                                        java.lang.Iterable<LoginEvent> startIterable = ctx
                                                                                        .getEventsForPattern("start");
                                                                        if (startIterable != null) {
                                                                                LoginEvent first = startIterable
                                                                                                .iterator().next();
                                                                                return first.getGantryHex().equals(
                                                                                                event.getGantryHex())
                                                                                                && first.getVehicleid()
                                                                                                                .equals(event.getVehicleid());
                                                                        }
                                                                } catch (Exception e) {
                                                                        // TODO Auto-generated catch block
                                                                        e.printStackTrace();
                                                                }
                                                                return false;
                                                        }

                                                        @Override
                                                        public boolean filter(LoginEvent arg0) throws Exception {
                                                                // TODO Auto-generated method stub
                                                                throw new UnsupportedOperationException(
                                                                                "Unimplemented method 'filter'");
                                                        }
                                                })
                                .within(Time.milliseconds(5000));
                return pattern;
        }


        public static Pattern<LoginEvent, ?> getPattern2() {
                //pattern设计，匹配所有连续出现的车牌相同、hex相同的LoginEvent序列（不区分类型），这里采用skipToLast策略
                //pattern2只有当同一个车牌经过不同门架的时间间隔确保比较长（超过同一个门架的所有流水、牌识流水之间的时间间隔时才有效）
                Pattern<LoginEvent, ?> pattern = Pattern.<LoginEvent>begin("start", AfterMatchSkipStrategy.skipToLast("end"))
                                .where(
                                                new SimpleCondition<LoginEvent>() {
                                                        @Override
                                                        public boolean filter(LoginEvent event) {
                                                                return true;
                                                        }
                                                })
                                .followedBy("middle").where(
                                                new SimpleCondition<LoginEvent>() {
                                                        @Override
                                                        public boolean filter(LoginEvent event, Context ctx) {
                                                                try {
                                                                        java.lang.Iterable<LoginEvent> startIterable = ctx
                                                                                        .getEventsForPattern("start");
                                                                        if (startIterable != null) {
                                                                                LoginEvent first = startIterable
                                                                                                .iterator().next();
                                                                                return first.getGantryHex().equals(
                                                                                                event.getGantryHex())
                                                                                                && first.getVehicleid()
                                                                                                                .equals(event.getVehicleid());
                                                                        }
                                                                } catch (Exception e) {
                                                                        // TODO Auto-generated catch block
                                                                        e.printStackTrace();
                                                                }
                                                                return false;
                                                        }

                                                        @Override
                                                        public boolean filter(LoginEvent arg0) throws Exception {
                                                                // TODO Auto-generated method stub
                                                                throw new UnsupportedOperationException(
                                                                                "Unimplemented method 'filter'");
                                                        }
                                                }).oneOrMore().greedy()
                                .followedBy("end").where(//hex不同时终止，最后一个LoginEvent是下一个开始的记录
                                                new SimpleCondition<LoginEvent>() {
                                                        @Override
                                                        public boolean filter(LoginEvent event, Context ctx) {
                                                                try {
                                                                        java.lang.Iterable<LoginEvent> startIterable = ctx
                                                                                        .getEventsForPattern("start");
                                                                        if (startIterable != null) {
                                                                                LoginEvent first = startIterable
                                                                                                .iterator().next();
                                                                                return !first.getGantryHex().equals(
                                                                                                event.getGantryHex())
                                                                                                && first.getVehicleid()
                                                                                                                .equals(event.getVehicleid());
                                                                        }
                                                                } catch (Exception e) {
                                                                        // TODO Auto-generated catch block
                                                                        e.printStackTrace();
                                                                }
                                                                return false;
                                                        }

                                                        @Override
                                                        public boolean filter(LoginEvent arg0) throws Exception {
                                                                // TODO Auto-generated method stub
                                                                throw new UnsupportedOperationException(
                                                                                "Unimplemented method 'filter'");
                                                        }
                                                })
                                .within(Time.milliseconds(20000));
                return pattern;
        }
}

class MyPatternProcessFunction1 extends PatternProcessFunction<LoginEvent, List<LoginEvent>>
                implements TimedOutPartialMatchHandler<LoginEvent> {
        OutputTag<LoginEvent> outputTag=new OutputTag<LoginEvent>("side-output"){};

        @Override
        public void processMatch(Map<String, List<LoginEvent>> pattern,
                        Context ctx,
                        Collector<List<LoginEvent>> out) throws Exception {

                List<LoginEvent> start = pattern.get("start");
                List<LoginEvent> end = pattern.get("end");

                List<LoginEvent> results = new LinkedList<>();
                if (start != null)
                        results.addAll(start);
                if (end != null)
                        results.addAll(end);
                // 对于所有被pattern匹配成功的LoginEvent，将其matched设为true
                for (LoginEvent event : results) {
                        event.setMatched(true);
                }
                System.out.println("*************************");
                System.out.println("start:" + start);
                System.out.println("end:" + end);
                // out.collect(results);
        }

        @Override
        public void processTimedOutMatch(Map<String, List<LoginEvent>> match, Context ctx) throws Exception {

                LoginEvent startEvent = match.get("start").get(0);
                // 对于没有被pettern匹配超时的，其只包含一个LoginEvent，若其matched=false，则表示该LoginEvent不存在任何匹配的其他LoginEvent，可以通过SideStream额外处理
                if (!startEvent.isMatched()) {
                        System.out.println("Notmatch: " + startEvent);
                        ctx.output(outputTag, startEvent);
                }
        }
}

class MyPatternProcessFunction2 extends PatternProcessFunction<LoginEvent, List<LoginEvent>>
                implements TimedOutPartialMatchHandler<LoginEvent> {
        OutputTag<LoginEvent> outputTag=new OutputTag<LoginEvent>("side-output"){};

        @Override
        public void processMatch(Map<String, List<LoginEvent>> pattern,
                        Context ctx,
                        Collector<List<LoginEvent>> out) throws Exception {

                List<LoginEvent> start = pattern.get("start");
                
                List<LoginEvent> middle = pattern.get("middle");
                List<LoginEvent> end = pattern.get("end");

                List<LoginEvent> results = new LinkedList<>();
                if (start != null)
                        results.addAll(start);
                if (middle != null)
                        results.addAll(middle);
                //end 此处是和start，middle不同的hex，应记为下一个记录。

                // 对于所有被pattern匹配成功的LoginEvent，将其matched设为true
                for (LoginEvent event : results) {
                        event.setMatched(true);
                }
                System.out.println("*************************");
                System.out.println("start:" + start);
                System.out.println("middle:" + middle);
                System.out.println("end:" + end);
                // out.collect(results);
        }

        @Override
        public void processTimedOutMatch(Map<String, List<LoginEvent>> pattern, Context ctx) throws Exception {

                List<LoginEvent> start = pattern.get("start");
                LoginEvent startEvent=start.get(0);
                List<LoginEvent> middle = pattern.get("middle");
                List<LoginEvent> end = pattern.get("end");
                if(middle!=null&&middle.size()>0){//此时实际上，start和middle是匹配的
                        startEvent.setMatched(true);
                        for(LoginEvent event : middle){
                                event.setMatched(true);
                        }
                }else{//此时middle为空，不存在与start匹配
                        // 对于没有被pettern匹配超时的，其只包含一个LoginEvent，若其matched=false，则表示该LoginEvent不存在任何匹配的其他LoginEvent，可以通过SideStream额外处理
                        if (!startEvent.isMatched()) {
                                System.out.println("Notmatch: " + startEvent);
                                ctx.output(outputTag, startEvent);
                        }
                }
                System.out.println("===============================================================");
                System.out.println("start:" + start);
                System.out.println("middle:" + middle);
                System.out.println("end:" + end);
                
        }
}

class LoginEvent {

        public String getVehicleid() {
                return vehicleid;
        }

        public void setVehicleid(String vehicleid) {
                this.vehicleid = vehicleid;
        }

        public String getGnntryID() {
                return GnntryID;
        }

        public void setGnntryID(String gnntryID) {
                GnntryID = gnntryID;
        }

        public String getGantryHex() {
                return GantryHex;
        }

        public void setGantryHex(String gantryHex) {
                GantryHex = gantryHex;
        }

        public String getGantryPicID() {
                return GantryPicID;
        }

        public void setGantryPicID(String gantryPicID) {
                GantryPicID = gantryPicID;
        }

        public Long getGantryTime() {
                return GantryTime;
        }

        public void setGantryTime(Long gantryTime) {
                GantryTime = gantryTime;
        }

        public Integer getKexindu() {
                return kexindu;
        }

        public void setKexindu(Integer kexindu) {
                this.kexindu = kexindu;
        }

        public Integer getChetou() {
                return chetou;
        }

        public void setChetou(Integer chetou) {
                this.chetou = chetou;
        }

        public Integer getSourcetype() {
                return sourcetype;
        }

        public void setSourcetype(Integer sourcetype) {
                this.sourcetype = sourcetype;
        }

        public Boolean isMatched() {
                return matched;
        }

        public void setMatched(boolean match) {
                matched = match;
        }

        public String vehicleid;
        public String GnntryID;
        public String GantryHex;
        public String GantryPicID;
        public Long GantryTime;
        public Integer kexindu;
        public Integer chetou;
        public Integer sourcetype;

        public Boolean matched = false;// 标记是否match，默认为false

        public LoginEvent(String vehicleid, String GnntryID, String GantryHex, String GantryPicID, Long GantryTime,
                        Integer kexindu, Integer chetou, Integer sourcetype) {
                this.vehicleid = vehicleid;
                this.GnntryID = GnntryID;
                this.GantryHex = GantryHex;
                this.GantryPicID = GantryPicID;
                this.GantryTime = GantryTime;
                this.kexindu = kexindu;
                this.chetou = chetou;
                this.sourcetype = sourcetype;
        }

        public LoginEvent() {
        }

        @Override
        public String toString() {
                return "LoginEvent{" +
                                "vehicleid='" + vehicleid + '\'' +
                                "GnntryID='" + GnntryID + '\'' +
                                ", GantryHex='" + GantryHex + '\'' +
                                ", GantryPicID='" + GantryPicID + '\'' +
                                ", GantryTime=" + GantryTime +
                                ", kexindu=" + kexindu +
                                ", chetou=" + chetou +
                                ", sourcetype=" + sourcetype +
                                ", matched=" + matched +
                                '}';
        }

}
