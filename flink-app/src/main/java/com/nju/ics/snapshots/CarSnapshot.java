package com.nju.ics.snapshots;

import com.alibaba.fastjson.annotation.JSONField;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Field;
import com.datastax.driver.mapping.annotations.Table;
import com.nju.ics.models.Car;

/**
 * 

 */

@Table(keyspace = "test", name = "CarSnapshot")
public class CarSnapshot extends AbstractSnapshot {
    /** id */
    @JSONField(name = "ID")
    @Column(name = "id")
    private String ID;
    @JSONField(name = "Car")
    @Column(name = "car")
    private Car Car;
    @JSONField(name = "SnapshotPre")
    @Column(name = "snapshotPre")
    private String SnapshotPre;
    @JSONField(name = "SnapshotNext")
    @Column(name = "snapshotNext")
    private String SnapshotNext;
    @JSONField(name = "OperationCall")
    @Column(name = "operationCall")
    private String operationCall;
    @Column(name = "timestamp")
    private long timestamp;
    @Column(name = "longitude")
    private float longitude;
    @Column(name = "latitude")
    private float latitude;
    @Column(name = "carid")
    private String carid;

    public CarSnapshot() {
    }

    public CarSnapshot(Car car) {
        Car = car;
        ID = String.format("%s-%s", car.getTIME(), car.id());
        carid = car.id();
    }

    @Override
    public String id() {
        // TODO Auto-generated method stub
        return ID;
    }

    public String getID() {
        return ID;
    }

    public void setID(String iD) {
        ID = iD;
    }

    public Car getCar() {
        return Car;
    }

    public void setCar(Car car) {
        Car = car;
    }

    public String getSnapshotPre() {
        return SnapshotPre;
    }

    public void setSnapshotPre(String snapshotPre) {
        SnapshotPre = snapshotPre;
    }

    public String getSnapshotNext() {
        return SnapshotNext;
    }

    public void setSnapshotNext(String snapshotNext) {
        SnapshotNext = snapshotNext;
    }

    public String getOperationCall() {
        return this.operationCall;
    }

    public void setOperationCall(String operationCall) {
        this.operationCall = operationCall;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public String getCarid() {
        return carid;
    }

    public void setCarid(String carid) {
        this.carid = carid;
    }

}
