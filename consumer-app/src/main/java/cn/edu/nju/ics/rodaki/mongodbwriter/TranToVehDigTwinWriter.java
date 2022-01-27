package cn.edu.nju.ics.rodaki.mongodbwriter;

import cn.edu.nju.ics.rodaki.pipelinethread.RabbitmqConsumerThread;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.conversions.Bson;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TranToVehDigTwinWriter implements MongodbWriter{

    MongoClient mongoClient;
    MongoDatabase database;
    MongoCollection<Document> vehicleDigital;
    List<WriteModel<Document>> BulkOperations = new ArrayList<>();

    Bson filter;
    Bson update;
    UpdateOptions options = new UpdateOptions().upsert(true);



    long TIME;
    boolean ISINHIGHWAY;
    long TOTALPASSTIME = 0;
    float CURRENTSPEED = 0.0F;
    float CURRENTAVGSPEED = 0.0F;
    double totalrmileage = 0.0;
    long tranStartTime = 0;
    long tranEndTime = 0;



    JSONArray stationInfo;
    JSONObject lastStationInfo;
    Iterator it;
    JSONObject station;
    ArrayList CURRENTSPEEDLIST;
    int stationInfoSize;
    long rtime;
    double rmileage;
    ArrayList temp;
    ArrayList stations;
    JSONArray l1;
    JSONArray l2;
    JSONObject firstStationInfo;
    String MEDIATYPE;
    ArrayList locationTran;



    public TranToVehDigTwinWriter(String db, String col) {
        this.mongoClient = new MongodbClient().getMongoClient();
        this.database = mongoClient.getDatabase(db);
        this.vehicleDigital = database.getCollection(col);
    }



    @Override
    public void insertData(JSONObject obj) {



        stationInfo =  obj.getJSONArray("STATIONINFO");

        // 按时间和站点类型排序，站点类型1，2，3分别为入口，门架，出口
        stationInfo = BubbleSort(stationInfo);


        lastStationInfo = (JSONObject) stationInfo.get(stationInfo.size() - 1);

        // 此刻时间为最后一个站点的时间
        TIME = lastStationInfo.getLong("TIME");




        // 如果最后一个站点记录是出口，那么车辆不在高速上
        if (lastStationInfo.getInteger("STYPE").equals(3)) {
            ISINHIGHWAY = false;
        // 如果最后一个站点记录是虚门架，那么车辆不在高速上
        } else if(lastStationInfo.getInteger("STYPE").equals(2) && lastStationInfo.getString("ORIGINALFLAG").equals("2")){

            ISINHIGHWAY = false;
        }
        else {  // 如果最后一个站点记录不是出口也不是虚门架，那么车辆在高速上

            ISINHIGHWAY = true;

        }





        // 计算速度

        CURRENTSPEEDLIST = new ArrayList();
        stationInfoSize = stationInfo.size();
        totalrmileage = 0.0;

        // 如果有2条以上的站点记录，此次通行时间为最后一条记录的时间减去第一条记录的时间，最近速度为最近两个站点间里程除以最近站点间时间，此次行程平均速度为总里程除以总时间

        // 如果有2条以上的站点记录，计算两两站点间的速度，组成当前通行的速度列表
        if (stationInfoSize >= 2) {
            for (int i = 0; i < stationInfoSize-1; i++) {

                // 时间为两个站点间时间
                rtime = stationInfo.getJSONOb78ject(i+1).getLong("TIME") - stationInfo.getJSONObject(i).getLong("TIME");

                // 取两个站点坐标
                l1 = stationInfo.getJSONObject(i).getJSONArray("LOCATION");
                l2 = stationInfo.getJSONObject(i+1).getJSONArray("LOCATION");


                // 根据坐标计算大致距离
                rmileage = GetShortDistance(l1.getJSONObject(0).getDouble("$numberDecimal"),
                        l1.getJSONObject(1).getDouble("$numberDecimal"),
                        l2.getJSONObject(0).getDouble("$numberDecimal"),
                        l2.getJSONObject(1).getDouble("$numberDecimal"));

                // 总里程累加
                totalrmileage += rmileage;

                temp = new ArrayList();
                // 计算速度
                // 若里程和时间都为有效值，大于零，计算速度
                if (rtime>0 && rmileage>0){
                    // 里程除以时间，单位为 km/h
                    temp.add((float) (rmileage / (rtime/1000) * 3.6));
                    // 添用后一个站点的时间作为该速度的时间戳
                    temp.add(stationInfo.getJSONObject(i+1).getLong("TIME"));
                    CURRENTSPEEDLIST.add(temp);

                }

            }


            // 如果速度列表不为空，则将最后一个速度作为当前速度
            if(CURRENTSPEEDLIST.size()>0){
                CURRENTSPEED = (float) ((ArrayList) CURRENTSPEEDLIST.get(CURRENTSPEEDLIST.size()-1)).get(0);
            } else {
                CURRENTSPEED = 0.0F;
            }

            // 计算当前通行的平均速度，取第一个和最后一个记录的时间作为总通行时间，累加的里程为总里程
            firstStationInfo = stationInfo.getJSONObject(0);
            tranStartTime = firstStationInfo.getLong("TIME");
            tranEndTime = lastStationInfo.getLong("TIME");

            TOTALPASSTIME = tranEndTime - tranStartTime;
            // 若总时间和总里程都为有效值，计算平均速度，单位是 km/h
            if (TOTALPASSTIME>0 && totalrmileage>0){
                CURRENTAVGSPEED = (float) (totalrmileage / (TOTALPASSTIME/1000) * 3.6);
            } else {
                CURRENTAVGSPEED = 0.0F;
            }

        // 若只有一个站点记录，不能求速度
        } else {

            CURRENTSPEED = 0.0F;
            CURRENTAVGSPEED = 0.0F;

            tranStartTime = (long) lastStationInfo.get("TIME");
            tranEndTime = (long) lastStationInfo.get("TIME");
        }



        // 根据 MEDIATYPE 判断通行介质是 OBU 或 CPC
        if(obj.get("MEDIATYPE").equals(1)){
            MEDIATYPE = "OBU";
        } else if(obj.get("MEDIATYPE").equals(2)){
            MEDIATYPE = "CPC";
        } else {
            MEDIATYPE = "null";
        };




        // 添加所有站点信息到列表
        stations = new ArrayList();
        it = stationInfo.iterator();

        while(it.hasNext()) {
            station = (JSONObject) it.next();
            locationTran = new ArrayList();
            locationTran.add(station.getJSONArray("LOCATION").getJSONObject(0).get("$numberDecimal"));
            locationTran.add(station.getJSONArray("LOCATION").getJSONObject(1).get("$numberDecimal"));

            if(station.get("STYPE").equals(1) || station.get("STYPE").equals(3)){
                stations.add(new Document()
                        .append("TIME",station.get("TIME"))
                        .append("SID",station.get("SID"))
                        .append("STYPE",station.get("STYPE"))
                        .append("LOCATION",locationTran));
            }else if(station.get("STYPE").equals(2)){
                stations.add(new Document()
                        .append("TIME",station.get("TIME"))
                        .append("SID",station.get("SID"))
                        .append("STYPE",station.get("STYPE"))
                        .append("LOCATION",locationTran)
                        .append("SPECIALTYPE",station.get("SPECIALTYPE"))
                        .append("ORIGINALFLAG",station.get("ORIGINALFLAG"))
                        .append("GANTRYPOSITIONFLAG",station.get("GANTRYPOSITIONFLAG")));
            }


        }



        // 使用以上字段，更新车辆的 digitaltwin 模型
        filter = Filters.eq("_id", obj.get("VEHICLEID"));

        update = Updates.combine(
                    Updates.set("TIME", TIME),
                Updates.set("CURRENTPASSID", obj.get("PASSID")),
                Updates.set("PASSLIST." + obj.get("PASSID"), new Document()
                        .append("STIME",tranStartTime)
                        .append("ETIME",tranEndTime)),
                Updates.set("ISINHIGHWAY", ISINHIGHWAY),
                Updates.set("CURRENTSPEED", CURRENTSPEED),
                Updates.set("CURRENTAVGSPEED", CURRENTAVGSPEED),
                Updates.set("MEDIATYPE", MEDIATYPE),
                Updates.set("MEDIAID",  obj.get("MEDIAID")),
                Updates.set("PASSSTATION", stations),
                Updates.set("CURRENTSPEEDLIST", CURRENTSPEEDLIST));


        BulkOperations.add(new UpdateOneModel<>(filter, update, options));
        if (BulkOperations.size() >= 500){
            try {
                vehicleDigital.bulkWrite(BulkOperations);
                BulkOperations.clear();
            } catch (MongoBulkWriteException e){
                System.out.println("A MongoBulkWriteException occured with the following message: " + e.getMessage());
            }
        }



    }



    public static JSONArray BubbleSort(JSONArray list){

        JSONObject temp;

        for(int i = 0; i < list.size()-1; i++){
            for (int j = 0; j < list.size()-1-i; j++) {
                if((long)list.getJSONObject(j).get("TIME") > (long)list.getJSONObject(j+1).get("TIME")){
                    temp = list.getJSONObject(j);
                    list.set(j,list.get(j+1));
                    list.set(j+1,temp);
                } else if ((long)list.getJSONObject(j).get("TIME") == (long)list.getJSONObject(j+1).get("TIME")
                        && (int) list.getJSONObject(j).getInteger("STYPE") > (int) list.getJSONObject(j+1).getInteger("STYPE")){
                    temp = list.getJSONObject(j);
                    list.set(j,list.get(j+1));
                    list.set(j+1,temp);
                }
            }
        }


        return list;
    }



    static double DEF_PI = 3.14159265359; // PI
    static double DEF_2PI= 6.28318530712; // 2*PI
    static double DEF_PI180= 0.01745329252; // PI/180.0
    static double DEF_R = 6370693.5; // radius of earth
    public static double GetShortDistance(double lon1, double lat1, double lon2, double lat2)
    {
        double ew1, ns1, ew2, ns2;
        double dx, dy, dew;
        double distance;
        // 角度转换为弧度
        ew1 = lon1 * DEF_PI180;
        ns1 = lat1 * DEF_PI180;
        ew2 = lon2 * DEF_PI180;
        ns2 = lat2 * DEF_PI180;
        // 经度差
        dew = ew1 - ew2;
        // 若跨东经和西经180 度，进行调整
        if (dew > DEF_PI)
            dew = DEF_2PI - dew;
        else if (dew < -DEF_PI)
            dew = DEF_2PI + dew;
        dx = DEF_R * Math.cos(ns1) * dew; // 东西方向长度(在纬度圈上的投影长度)
        dy = DEF_R * (ns1 - ns2); // 南北方向长度(在经度圈上的投影长度)
        // 勾股定理求斜边长
        distance = Math.sqrt(dx * dx + dy * dy);
        return distance;
    }

    public static double GetLongDistance(double lon1, double lat1, double lon2, double lat2)
    {
        double ew1, ns1, ew2, ns2;
        double distance;
        // 角度转换为弧度
        ew1 = lon1 * DEF_PI180;
        ns1 = lat1 * DEF_PI180;
        ew2 = lon2 * DEF_PI180;
        ns2 = lat2 * DEF_PI180;
        // 求大圆劣弧与球心所夹的角(弧度)
        distance = Math.sin(ns1) * Math.sin(ns2) + Math.cos(ns1) * Math.cos(ns2) * Math.cos(ew1 - ew2);
        // 调整到[-1..1]范围内，避免溢出
        if (distance > 1.0)
            distance = 1.0;
        else if (distance < -1.0)
            distance = -1.0;
        // 求大圆劣弧长度
        distance = DEF_R * Math.acos(distance);
        return distance;
    }




}
