
package com.nju.ics.Utils;

import java.io.File;
import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.io.FileUtils;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.types.FloatValue;
import org.apache.flink.types.IntValue;

public class GetColInfo {
    public static TypeInformation[] getTypeInfo(String file_path) {
        List<TypeInformation> types = new ArrayList<TypeInformation>(60);

        try {
            List<Map<String, Object>> infos = JSON.parseObject(GetColInfo.class.getResourceAsStream(file_path),
                    List.class);

            for (Map<String, Object> entry : infos) {
                switch ((String) entry.get("type")) {
                    case "string":
                        types.add(Types.STRING);
                        break;
                    case "int":
                        types.add(Types.STRING);
                        break;
                    case "float":
                        types.add(Types.STRING);
                        break;
                    default:
                        types.add(Types.STRING);
                        break;
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(1);
        }

        return types.toArray(new TypeInformation[0]);

    }

    public static int[] getIdx(String file_path) {
        List<Integer> types = new ArrayList<Integer>(60);

        try {
            List<Map<String, Object>> infos = JSON.parseObject(GetColInfo.class.getResourceAsStream(file_path),
                    List.class);

            for (Map<String, Object> entry : infos) {
                types.add((int) entry.get("index"));
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(1);
        }

        return types.stream().mapToInt(Integer::valueOf).toArray();

    }

    public static String[] getColNames(String file_path) {
        List<String> types = new ArrayList<String>(150);

        try {
            List<Map<String, Object>> infos = JSON.parseObject(GetColInfo.class.getResourceAsStream(file_path),
                    List.class);

            for (Map<String, Object> entry : infos) {
                types.add((String) entry.get("col"));
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(1);
        }

        return types.toArray(new String[0]);
    }
}
