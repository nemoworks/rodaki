package com.nju.ics.Configs;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.nju.ics.Fields.StationPositionMsg;

public class StationPosition {
    public static Map<String, StationPositionMsg> geoMap = new HashMap<>();

    public static void initStationPosition() {
        try {
            // 先将收费路段表格读取进来
            Workbook workbook = new XSSFWorkbook(StationPosition.class.getResourceAsStream("/sdStationPosition.xlsx"));
            Sheet datatypeSheet = workbook.getSheetAt(1);
            int rowStart = datatypeSheet.getFirstRowNum() + 2;
            int rowEnd = datatypeSheet.getLastRowNum();
            for (int i = rowStart; i <= rowEnd; i++) {
                // System.out.println(i);
                Row currentRow = datatypeSheet.getRow(i);
                // System.out.println(currentRow);
                currentRow.getCell(1).setCellType(CellType.STRING);
                currentRow.getCell(2).setCellType(CellType.STRING);
                currentRow.getCell(4).setCellType(CellType.STRING);
                currentRow.getCell(5).setCellType(CellType.STRING);
                // System.out.println(currentRow.getCell(1).getStringCellValue());
                String key = String.valueOf(currentRow.getCell(1).getStringCellValue().subSequence(0, 14));
                StationPositionMsg value = new StationPositionMsg();
                // System.out.println(currentRow.getCell(10).getStringCellValue());
                // System.out.println(currentRow.getCell(9).getStringCellValue());
                try {
                    value.longitude = (Float.valueOf(currentRow.getCell(5).getStringCellValue()));// 经度
                } catch (Exception e) {
                    value.longitude = 0.0f;
                }
                try {
                    value.latitude = (Float.valueOf(currentRow.getCell(4).getStringCellValue()));// 纬度
                } catch (Exception e) {
                    value.latitude = 0.0f;
                }

                value.stationName = currentRow.getCell(2).getStringCellValue();
                geoMap.put(key, value);
            }
            // System.out.println(geoMap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // System.out.println(geoMap);
    }

    public static float getLatitude(String id) {
        if (StationPosition.geoMap.containsKey(id)) {
            return StationPosition.geoMap.get(id).latitude;
        } else {
            return 0.0f;
        }
    }

    public static float getLongitude(String id) {
        if (StationPosition.geoMap.containsKey(id)) {
            return StationPosition.geoMap.get(id).longitude;
        } else {
            return 0.0f;
        }
    }
}
