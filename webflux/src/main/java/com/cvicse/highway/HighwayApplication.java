package com.cvicse.highway;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.PostConstruct;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import com.cvicse.highway.domain.Msg;

@SpringBootApplication
@EnableAsync
public class HighwayApplication {

    public static Map<String, Msg> geoMap = new HashMap<>();

    private static final String file_name = "jx.xlsx";// 必须是xlsx版本
    private static final String file_name3 = "test.txt";// 必须是xlsx版本
    private static final String file_name2 = "jx收费路段.xlsx";

    public static void main(String[] args) {
        SpringApplication.run(HighwayApplication.class, args);
    }

   

   
}
