package com.nju.ics.Models;

/**
 * 每个站点（门架、出入站）的每小时流量统计
 * 
 * @return
 */
public class StationTraffic extends AbstractModel {
    private String id;
    private int count;
    private long timestamp;
    @Override
    public String id() {
        // TODO Auto-generated method stub
        return id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void addCount() {
        this.count++;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
}
