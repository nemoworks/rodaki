package com.nju.ics.annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
public class IotDBAnnotion {
    /**
     * 需要存储在iotdb的字段注解
     * 
     * 
     *
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface sinkIotDB {

    }
}
