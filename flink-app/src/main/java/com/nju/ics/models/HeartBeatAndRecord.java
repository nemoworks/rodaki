package com.nju.ics.models;

public class HeartBeatAndRecord {
    public static int HEARTBEAT = 1;
    public static int RECORD = 2;
    private TimerRecord record;
    private int type;
    private long timestamp;
    private String key;

    public TimerRecord getRecord() {
        return record;
    }

    public void setRecord(TimerRecord record) {
        this.record = record;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTimestamp() {
        if (this.type == HeartBeatAndRecord.HEARTBEAT) {
            return this.timestamp;
        } else {
            return this.record.getTIME();
        }
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getKey() {
        if (this.type == HeartBeatAndRecord.HEARTBEAT) {
            return this.key;
        } else {
            return this.record.getVEHICLEID();
        }
    }

    public void setKey(String key) {
        this.key = key;
    }

    public static HeartBeatAndRecord build(TimerRecord record) {
        HeartBeatAndRecord tmp = new HeartBeatAndRecord();
        tmp.type = HeartBeatAndRecord.RECORD;
        tmp.record = record;
        return tmp;
    }

    public static HeartBeatAndRecord build(String key, long timestamp) {
        HeartBeatAndRecord tmp = new HeartBeatAndRecord();
        tmp.type = HeartBeatAndRecord.HEARTBEAT;
        tmp.key = key;
        tmp.timestamp = timestamp;
        return tmp;
    }
    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return super.hashCode();
    }
}
