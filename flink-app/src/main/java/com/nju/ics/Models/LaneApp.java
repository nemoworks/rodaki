package com.nju.ics.Models;

public class LaneApp extends AbstractModel {
    /** 入口、出口：车道程序版本号 */
    public String Version;

    @Override
    public String id() {
        // TODO Auto-generated method stub
        return Version;
    }

    public LaneApp() {
    }

    public LaneApp(String version) {
        Version = version == null ? null : version.trim();
    }

}
