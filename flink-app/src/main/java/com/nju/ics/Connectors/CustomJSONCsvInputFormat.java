package com.nju.ics.Connectors;

import com.alibaba.fastjson.JSONObject;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.io.CsvInputFormat;
import org.apache.flink.api.java.io.RowCsvInputFormat;
import org.apache.flink.api.java.typeutils.ResultTypeQueryable;
import org.apache.flink.core.fs.Path;

public class CustomJSONCsvInputFormat extends CsvInputFormat<JSONObject> implements ResultTypeQueryable<JSONObject>{

    protected CustomJSONCsvInputFormat(Path filePath) {
        super(filePath);
        //TODO Auto-generated constructor stub
    }

    @Override
    public TypeInformation<JSONObject> getProducedType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected JSONObject fillRecord(JSONObject reuse, Object[] parsedValues) {
        // TODO Auto-generated method stub
        return null;
    }



    
}
