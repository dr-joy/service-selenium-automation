package com.drjoy.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelUtils {
    static final Logger logger = LogManager.getLogger(ExcelUtils.class);

    public static List<String[]> readDataFromFilePath(String path) {
        return readDataFromFile(new File(path));
    }

    public static List<String[]> readDataFromFile(File file) {
        List<String[]> data = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
            Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);

            DataFormatter formatter = new DataFormatter();
            int totalColumn = sheet.getRow(0).getLastCellNum();

            // Bỏ qua header (row 0), bắt đầu từ dòng 1
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (isRowEmpty(row)) {
                    break;
                }

                // Create an array with the correct size
                String[] rowData = new String[totalColumn];

                for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    rowData[j] = getCellValue(cell, formatter);
                }
                data.add(rowData);
            }

        } catch (Exception e) {
            logger.error("Error reading data from Excel file: {}",
                file.getAbsolutePath(), e);
        }

        return data;
    }

    private static String getCellValue(Cell cell, DataFormatter formatter) {
        if (cell == null) {
            return "";
        }
        return formatter.formatCellValue(cell).trim();
    }

    private static boolean isRowEmpty(Row row) {
        if (row == null) return true;

        for (int c = 0; c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }
}
