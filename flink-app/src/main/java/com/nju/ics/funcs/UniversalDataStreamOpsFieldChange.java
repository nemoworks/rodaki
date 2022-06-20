package com.nju.ics.funcs;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.ResultTypeQueryable;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import com.nju.ics.utils.UniversalDataStreamOps.AfterNearElement;
import com.nju.ics.utils.UniversalDataStreamOps.BeforeNearElement;
import com.nju.ics.utils.UniversalDataStreamOps.NearElement;

public class UniversalDataStreamOpsFieldChange<I, OUT> extends KeyedProcessFunction<String, I, OUT> {

    private ValueState<I> pre;
    ValueStateDescriptor<I> preDescriptor;
    private Method fieldfn;
    private BeforeNearElement<OUT, I> _bfn;
    private NearElement<OUT, I> fn;
    private AfterNearElement<OUT, I> _afn;
    private String field;
    private Class<I> incls;
    private Class<OUT> outcls;

    /**
     * 
     * @param _bfn
     * @param fn
     * @param _afn
     */
    public UniversalDataStreamOpsFieldChange(BeforeNearElement<OUT, I> _bfn,
            NearElement<OUT, I> fn,
            AfterNearElement<OUT, I> _afn, String field, Class<I> in, Class<OUT> out) {
        this.field = field;
        this._bfn = _bfn;
        this._afn = _afn;
        this.fn = fn;
        this.preDescriptor = new ValueStateDescriptor<I>("pre", in);
        this.incls = in;
        this.outcls = out;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        // TODO Auto-generated method stub
        super.open(parameters);
        this.pre = getRuntimeContext().getState(preDescriptor);
        Method[] m = this.incls.getMethods();
        for (int i = 0; i < m.length; i++) {
            if (("get" + field).toLowerCase().equals(m[i].getName().toLowerCase())) {
                System.out.println(m[i].getName());
                this.fieldfn = m[i];
                break;
            }
        }
    }

    @Override
    public void processElement(I value, KeyedProcessFunction<String, I, OUT>.Context ctx, Collector<OUT> out)
            throws Exception {
        OUT _out = this._bfn.process(pre.value(), value);
        if (_out != null) {
            out.collect(_out);
        }

        if (pre.value() != null && !fieldfn.invoke(pre.value()).equals(fieldfn.invoke(value))) {
            _out = null;
            _out = this.fn.process(pre.value(), value);
            if (_out != null) {
                out.collect(_out);
            }
        }
        _out = null;
        _out = this._afn.process(pre.value(), value);
        if (_out != null) {
            out.collect(_out);
        }
        pre.update(value);
    }

   
}
