package com.newegg.ec.redis.util;

import com.newegg.ec.redis.entity.ExcelData;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * @ClassName ExcelUtil
 * @Description TODO
 * @Author kz37
 * @Date 2018/10/18
 */
public class ExcelUtil {

    private final static Logger LOG = LoggerFactory.getLogger(ExcelUtil.class);

    /**
     * please close workbook，after used!
     * @param dataMap
     * @param file
     * @return
     * @throws Exception
     */
    public static Workbook writeExcel(Map<String, List<ExcelData>> dataMap, File file) throws Exception{
        XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(file));
        for(Map.Entry<String, List<ExcelData>> entry : dataMap.entrySet()) {
        	try {
        		writeOneSheet(workbook, entry.getValue(), entry.getKey());
    		} catch (Exception e) {
    			LOG.error("write workbook error!", e);
    		}
        }
        return workbook;
    }

    private static void writeOneSheet(XSSFWorkbook xssfWorkbook, List<ExcelData> excelListData, String sheetName) {
        XSSFSheet sheet = xssfWorkbook.getSheet(sheetName);
        if(null == sheet) {
            sheet = xssfWorkbook.createSheet(sheetName);
        }
        List<String> rowData = null;
        for(ExcelData excelData : excelListData) {
        	//XSSFCellStyle cellStyle = null;
            List<List<String>> sheetData = excelData.getTableData();
            int startColumn = excelData.getStartColumn();
            for(int i = 0; i < sheetData.size(); i++) {
                rowData = sheetData.get(i);
                writeOneRow(i, sheet, rowData, startColumn);
            }
        } 
    }

    /*private static XSSFCellStyle getTableHeadStyle(XSSFWorkbook workbook) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        // Horizontal center
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        IndexedColorMap colorMap = workbook.getStylesSource().getIndexedColors();
        // set head color
        XSSFColor blue = new XSSFColor(new java.awt.Color(79, 129, 189), colorMap);
        cellStyle.setFillForegroundColor(blue);
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        // font head color
        XSSFFont font = workbook.createFont();
        XSSFColor white = new XSSFColor(new java.awt.Color(255, 255, 255), colorMap);
        font.setColor(white);
        cellStyle.setFont(font);
        return cellStyle;
    }

    private static XSSFCellStyle setBorder(XSSFCellStyle cellStyle) {
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        return cellStyle;
    }

    private static XSSFCellStyle getTableBodyStyle(XSSFWorkbook workbook) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        // Horizontal center
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        XSSFFont font = workbook.createFont();
        font.setFontName("Calibri");
        cellStyle.setFont(font);
        return cellStyle;
    }

    private static void writeOneRow(int rowNumber, XSSFSheet sheet, List<String> data, XSSFCellStyle style) {
        XSSFRow row = sheet.createRow(rowNumber + 1);;
        XSSFCell cell = null;
        for(int i = 0; i < data.size(); i++) {
            cell = row.createCell(i);
            if(isNumber(data.get(i))){
                cell.setCellValue(Double.parseDouble(data.get(i)));
            }
            else
            {
                cell.setCellValue(data.get(i));
            }
            cell.setCellStyle(style);
            //sheet.autoSizeColumn(i);
        }
    }*/

    private static boolean isNumber(String s){
        try {
            Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private static void writeOneRow(int rowNumber, XSSFSheet sheet, List<String> data, int startColumn) {
        XSSFCellStyle style = null;
        XSSFRow row = null;
        XSSFCell cell = null;
/*        if(rowNumber == 0){
            //第一行采用excel中的格式
            row = sheet.getRow(rowNumber + 1);
        }*/
        row = sheet.getRow(rowNumber + 1);
        if( null == row){
            row = sheet.createRow(rowNumber + 1);
        }
        for(int i = 0; i < data.size(); i++) {

            //cell = row.createCell(i);

        	if(null == data.get(i) || data.get(i).equals(""))
            {
        		startColumn++;
                continue;
            }
            if(rowNumber == 0){
                cell = row.getCell(startColumn);
            }
            else
            {
                cell = row.createCell(startColumn);
                style = sheet.getRow(1).getCell(startColumn).getCellStyle();
                cell.setCellStyle(style);
            }
            if(isNumber(data.get(i))){
                cell.setCellValue(Double.parseDouble(data.get(i)));
            }
            else 
            {
            	cell.setCellValue(data.get(i));
            }
            //sheet.autoSizeColumn(startColumn);
            startColumn++;
        }
    }

    public static void closeCloseable(Closeable closeable) {
        if(null != closeable) {
            try{
                closeable.close();
            }
            catch (Exception e) {
                LOG.error("close closeable fail!", e);
            }
        }
    }

    public static void writeFile(File file, Workbook workbook) {
        FileOutputStream fileOutputStream = null;
        try{
            fileOutputStream = new FileOutputStream(file);
            workbook.write(fileOutputStream);
        }
        catch (FileNotFoundException e){
            LOG.error("File Not Found", e);
        }
        catch (IOException e) {
            LOG.error("write to file failed!", e);
        }
        finally {
            closeCloseable(fileOutputStream);
        }
    }

    public static void writeExcelToFile(Map<String, List<ExcelData>> dataMap, String fileName) throws Exception{
        writeExcelToFile(dataMap, fileName != null ? new File(fileName) : null);
    }

    public static void writeExcelToFile(Map<String, List<ExcelData>> dataMap, File file) throws Exception{
        Workbook workbook = null;
        try {
            workbook = writeExcel(dataMap, file);
            writeFile(file, workbook);
        } catch (Exception e) {
            LOG.error("write Excel data to file error", e);
        } finally {
            closeCloseable(workbook);
        }
    }
}
