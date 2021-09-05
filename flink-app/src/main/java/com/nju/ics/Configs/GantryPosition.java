package com.nju.ics.Configs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.nju.ics.Fields.GantryPositionMsg;


import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.InputStream;
public class GantryPosition {
    public static InputStream file=GantryPosition.class.getResourceAsStream("/sdGantryPosition.xlsx");
    public static Map<String, GantryPositionMsg> geoMap=new HashMap<>();
    public static void initGantryPosition(){
        try {
            // 先将收费路段表格读取进来
            Workbook workbook = new XSSFWorkbook(file);
            Sheet datatypeSheet = workbook.getSheetAt(0);
            int rowStart = datatypeSheet.getFirstRowNum() + 2;
            int rowEnd = datatypeSheet.getLastRowNum();
            for (int i = rowStart; i <= rowEnd; i++) {
                // System.out.println(i);
                Row currentRow = datatypeSheet.getRow(i);
                // System.out.println(currentRow);
                currentRow.getCell(1).setCellType(CellType.STRING);
                currentRow.getCell(4).setCellType(CellType.STRING);
                currentRow.getCell(3).setCellType(CellType.STRING);
                // System.out.println(currentRow.getCell(1).getStringCellValue());
                String key = String.valueOf(currentRow.getCell(1).getStringCellValue());
                GantryPositionMsg value = new GantryPositionMsg();
                //System.out.println(currentRow.getCell(10).getStringCellValue());
                //System.out.println(currentRow.getCell(9).getStringCellValue());
                value.longtitude = (Float.valueOf(currentRow.getCell(4).getStringCellValue()));// 经度
                value.latitude = (Float.valueOf(currentRow.getCell(3).getStringCellValue()));// 纬度
                geoMap.put(key, value);
            }
            //System.out.println(geoMap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println(geoMap);
    }
}
