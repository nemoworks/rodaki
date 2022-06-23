package com.nju.ics.utils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.functions.PatternProcessFunction;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

import com.nju.ics.funcs.UniversalDataStreamOpsFieldChange;

/**
 * 为DataStream生成一些通用的操作
 */
public class UniversalDataStreamOps {

    /**
     * 关注object的某个属性值的变化,前后不一样就调用fn函数
     * 
     * @param input
     * @param keyby
     * @param feild
     * @param bfn
     * @param fn
     * @param afn
     * @return DataStream<OUT>
     */
    public static class ObserveFieldChangeBuilder<I, OUT> {
        private DataStream<I> input;
        private Class<OUT> out;
        private KeySelector<I, String> key;
        private String field;
        private BeforeNearElement<OUT, I> bfn;
        private NearElement<OUT, I> fn;
        private AfterNearElement<OUT, I> afn;

        public ObserveFieldChangeBuilder(DataStream<I> input) {
            this.input = input;
            this.bfn = (x, y) -> null;
            this.fn = (x, y) -> null;
            this.afn = (x, y) -> null;
            this.key = x -> "";
        }

        /**
         * 设置函数返回的Datastream的类型
         */
        public ObserveFieldChangeBuilder<I, OUT> outputType(Class<OUT> out) {
            this.out = out;
            return this;
        }

        /**
         * 
         * @param key 对元素进行keyby操作的lambda表达式
         * @return
         */
        public ObserveFieldChangeBuilder<I, OUT> keyby(KeySelector<I, String> key) {
            this.key = key;
            return this;
        }

        /**
         * 
         * @param field 关注元素的某个field
         * @return
         */
        public ObserveFieldChangeBuilder<I, OUT> observeField(String field) {
            this.field = field;
            return this;
        }

        /**
         * 
         * @param bfn 在changeProcess回调函数处理前进行的pre process
         * @return
         */
        public ObserveFieldChangeBuilder<I, OUT> preProcess(BeforeNearElement<OUT, I> bfn) {
            this.bfn = bfn;
            return this;
        }

        /**
         * 
         * @param fn 当关注的field发生变化后（当前元素与前一个元素的field进行equal比较）进行调用的函数
         * @return
         */
        public ObserveFieldChangeBuilder<I, OUT> changeProcess(NearElement<OUT, I> fn) {
            this.fn = fn;
            return this;
        }

        /**
         * 
         * @param afn 在changeProcess回调函数处理后进行的post process
         * @return
         */
        public ObserveFieldChangeBuilder<I, OUT> postProcess(AfterNearElement<OUT, I> afn) {
            this.afn = afn;
            return this;
        }

        public DataStream<OUT> build() {
            return this.input.keyBy(this.key)
                    .process(new UniversalDataStreamOpsFieldChange<I, OUT>(this.bfn, this.fn, this.afn, this.field,
                            this.input.getType().getTypeClass(), this.out))
                    .returns(this.out);
        }
    }

    /**
     * 关注某类元素是否频繁出现
     * 
     * 
     * 
     * @param stationRecordFixed 输入datastream
     * @param out                输出类型
     * @param filter             根据某个字段的来过滤的函数
     * @param key                对input的元素进行keyby
     * @param freq               连续出现的次数
     * @param interval           第一个元素与最后一个元素的的最大时间间隔,单位毫秒
     * @return
     */
    public static class ObserveFieldFrequentBuilder<I, OUT> {
        private DataStream<I> input;
        private Class<OUT> out;
        private FilterElement<I> filter;
        private FrequentElements<OUT, I> matchFn;
        private KeySelector<I, String> key;
        private int freq;
        private long interval;

        public ObserveFieldFrequentBuilder(DataStream<I> input) {
            this.input = input;
            this.filter = x -> true;
            this.matchFn = x -> null;
            this.key = x -> "";
        }

        /**
         * 设置函数返回的Datastream的类型
         */
        public ObserveFieldFrequentBuilder<I, OUT> outputType(Class<OUT> out) {
            this.out = out;
            return this;
        }

        /**
         * 
         * @param key 对元素进行keyby操作的lambda表达式
         * @return
         */
        public ObserveFieldFrequentBuilder<I, OUT> keyby(KeySelector<I, String> key) {
            this.key = key;
            return this;
        }
        /**
         * 
         * @param filter 对元素进行过滤，只关注符合条件的元素的出现频率
         * @return
         */
        public ObserveFieldFrequentBuilder<I, OUT> filter(FilterElement<I> filter) {
            this.filter = filter;
            return this;
        }
        /**
         * 
         * @param matchFn 对符合pattern（某段时间内出现了freq次）的元素进行处理
         * @return
         */
        public ObserveFieldFrequentBuilder<I, OUT> matchProcess(FrequentElements<OUT, I> matchFn) {
            this.matchFn = matchFn;
            return this;
        }
        /**
         * 
         * @param freq 经过过滤的元素出现的频率
         * @return
         */
        public ObserveFieldFrequentBuilder<I, OUT> frequent(int freq) {
            this.freq = freq;
            return this;
        }
        /**
         * 
         * @param interval 检测的pattern的时间间隔，即第一个元素与最后一个元素的时间间隔要小于等于interval
         * @return
         */
        public ObserveFieldFrequentBuilder<I, OUT> maxInterval(long interval) {
            this.interval = interval;
            return this;
        }

        public DataStream<OUT> build() {
            DataStream<I> filterKeyby = this.input.filter(new UniversalDataStreamOps.InnerFilterFunction(filter))
                    .returns(this.input.getType()).keyBy(key);
            // 使用cep来监测
            AfterMatchSkipStrategy skipStrategy = AfterMatchSkipStrategy.skipPastLastEvent();
            // 必须在检测到个事件，以on event开始，passidchanged event结束
            Pattern<I, ?> pattern = Pattern.<I>begin("match", skipStrategy)
                    .where(new SimpleCondition<I>() {
                        @Override
                        public boolean filter(I value) throws Exception {
                            return true;
                        }
                    }).times(freq).consecutive().within(Time.milliseconds(interval));

            PatternStream<I> patternStream = CEP.pattern(filterKeyby, pattern);
            SingleOutputStreamOperator<OUT> alerts = patternStream
                    .process(new UniversalDataStreamOps.InnerPatternProcessFunction<I, OUT>(matchFn),
                            TypeInformation.of(out));
            return alerts;
        }
    }

    public interface NearElement<OUT, I> extends Serializable {
        public OUT process(I pre, I cur);
    }

    public interface BeforeNearElement<OUT, I> extends Serializable {
        public OUT process(I pre, I cur);
    }

    public interface AfterNearElement<OUT, I> extends Serializable {
        public OUT process(I pre, I cur);
    }

    /**
     * 当频繁的记录出现n次时，将这n个元素交给用户进行处理
     */
    public interface FrequentElements<OUT, I> extends Serializable {
        public OUT process(List<I> match);
    }

    public interface FilterElement<I> extends Serializable {
        public boolean process(I cur);
    }

    static class InnerFilterFunction<T> implements FilterFunction<T> {
        private FilterElement<T> fn;

        public InnerFilterFunction(FilterElement<T> fn) {
            this.fn = fn;
        }

        @Override
        public boolean filter(T value) throws Exception {
            // TODO Auto-generated method stub
            return this.fn.process(value);
        }

    }

    public static class InnerPatternProcessFunction<I, OUT> extends PatternProcessFunction<I, OUT> {
        private FrequentElements<OUT, I> matchFn;

        public InnerPatternProcessFunction(FrequentElements<OUT, I> matchFn) {
            this.matchFn = matchFn;
        }

        @Override
        public void processMatch(Map<String, List<I>> match, Context ctx, Collector<OUT> out)
                throws Exception {
            // TODO Auto-generated method stub
            OUT e = this.matchFn.process(match.get("match"));
            if (e != null) {
                out.collect(e);
            }

        }
    }
}
