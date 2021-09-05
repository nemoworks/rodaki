package com.nju.ics.Utils;

import java.util.Comparator;

import org.apache.flink.api.java.tuple.Tuple2;

public class SortUtils {
    public static Comparator VehicleSortByTimestamp = new Comparator<Tuple2<String, Long>>() {

        @Override
        public int compare(Tuple2<String, Long> o1, Tuple2<String, Long> o2) {
            // TODO Auto-generated method stub
            return o1.f1.compareTo(o2.f1);
        }

    };
}
