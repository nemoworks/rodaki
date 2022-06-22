package com.nju.ics.configs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.nju.ics.fields.GantryPositionMsg;

import java.io.InputStream;

public class GantryPosition {
    // public static InputStream
    // file=GantryPosition.class.getResourceAsStream("/sdGantryPosition.xlsx");
    public static Map<String, GantryPositionMsg> geoMap = new HashMap<>();
    public static Map<String, GantryPositionMsg> geoMapHex = new HashMap<>();

    public static void initGantryPosition() {
        try {
            // 先将收费路段表格读取进来
            Workbook workbook = new XSSFWorkbook(GantryPosition.class.getResourceAsStream("/sdGantry1215.xlsx"));
            Sheet datatypeSheet = workbook.getSheetAt(0);
            int rowStart = datatypeSheet.getFirstRowNum() + 2;
            int rowEnd = datatypeSheet.getLastRowNum();
            for (int i = rowStart; i <= rowEnd; i++) {
                // System.out.println(i);
                Row currentRow = datatypeSheet.getRow(i);
                // System.out.println(currentRow);
                currentRow.getCell(1).setCellType(CellType.STRING);
                currentRow.getCell(2).setCellType(CellType.STRING);
                currentRow.getCell(4).setCellType(CellType.STRING);
                currentRow.getCell(6).setCellType(CellType.STRING);
                currentRow.getCell(9).setCellType(CellType.STRING);
                currentRow.getCell(10).setCellType(CellType.STRING);
                currentRow.getCell(15).setCellType(CellType.STRING);
                // System.out.println(currentRow.getCell(1).getStringCellValue());
                String key = String.valueOf(currentRow.getCell(1).getStringCellValue());
                // System.out.println(key);
                String hex = String.valueOf(currentRow.getCell(15).getStringCellValue());
                // System.out.println(hex);
                GantryPositionMsg value = new GantryPositionMsg();
                // System.out.println(currentRow.getCell(10).getStringCellValue());
                // System.out.println(currentRow.getCell(9).getStringCellValue());
                value.longitude = (Float.valueOf(currentRow.getCell(10).getStringCellValue()));// 经度
                value.latitude = (Float.valueOf(currentRow.getCell(9).getStringCellValue()));// 纬度
                value.name = currentRow.getCell(2).getStringCellValue();// name
                if (currentRow.getCell(6).getStringCellValue().trim().equals("省界出口")) {// 省界入/出口标识
                    value.gantryPositionFlag = 2;
                } else if (currentRow.getCell(6).getStringCellValue().trim().equals("省界入口")) {
                    value.gantryPositionFlag = 1;
                }
                if (currentRow.getCell(4).getStringCellValue().trim().equals("实体门架")) {// 门架类型
                    value.gantryType = 1;
                } else {
                    value.gantryType = 2;
                }

                // System.out.println(value);
                geoMap.put(key, value);
                geoMapHex.put(hex, value);
            }
            // System.out.println(geoMap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // System.out.println("*******************");
        // System.out.println(geoMap.size());
        // System.out.println("*******************");
    }

    public static float getLatitude(String id) {
        if (GantryPosition.geoMap.containsKey(id)) {
            return GantryPosition.geoMap.get(id).latitude;
        } else {
            return 0.0f;
        }
    }

    public static float getLongitude(String id) {
        if (GantryPosition.geoMap.containsKey(id)) {
            return GantryPosition.geoMap.get(id).longitude;
        } else {
            return 0.0f;
        }
    }
}
