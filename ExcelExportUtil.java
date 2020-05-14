package com.sbcm.utils;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExcelExportUtil {
    /**
     * 导出excel文件
     *
     * @param title    表sheet的名字
     * @param headers  表头
     * @param dataList 正文单元格
     * @param out      输出流
     */
    public void exporteExcel(String title, String[] headers, String [] properties, List<Map> dataList, OutputStream out) {
        HSSFWorkbook workBook = new HSSFWorkbook();
        createSheet(title, headers,properties, dataList, workBook);
        try {
            workBook.write(out);
        } catch (IOException e) {
            System.out.println("写入文件失败" + e.getMessage());
        }
    }


    /**
     * 创建sheet
     *
     * @param title    sheet的名字
     * @param headers  表头
     * @param dataList 正文单元格
     */
    private void createSheet(String title, String[] headers, String [] properties, List<Map> dataList, HSSFWorkbook workBook) {
        HSSFSheet sheet = workBook.createSheet(title);
//        sheet.setDefaultColumnWidth(15);
        //设置表头和普通单元格的格式
        HSSFCellStyle headStyle = setHeaderStyle(workBook);
        HSSFCellStyle bodyStyle = setBodyStyle(workBook);

        createBody(properties,dataList, sheet, bodyStyle);
        createHeader(headers, sheet, headStyle);
    }

    /**
     * 创建正文单元格
     *
     * @param dataList  数据数组
     * @param sheet     表
     * @param bodyStyle 单元格格式
     */
    private void createBody(String [] properties, List<Map> dataList, HSSFSheet sheet, HSSFCellStyle bodyStyle) {
        for (int a = 0; a < dataList.size(); a++) {
            HSSFRow row = sheet.createRow(a + 1);
            for (int j = 0; j < properties.length; j++) {
                HSSFCell cell = row.createCell(j);
                cell.setCellStyle(bodyStyle);
                HSSFRichTextString textString = new HSSFRichTextString(String.valueOf(dataList.get(a).get(properties[j])));
                cell.setCellValue(textString);
            }
        }
    }

    /**
     * 创建表头
     *
     * @param headers   表头
     * @param sheet     表
     * @param headStyle 表头格式
     */
    private void createHeader(String[] headers, HSSFSheet sheet, HSSFCellStyle headStyle) {
        HSSFRow row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            HSSFCell cell = row.createCell(i);
            cell.setCellStyle(headStyle);
            HSSFRichTextString textString = new HSSFRichTextString(headers[i]);
            cell.setCellValue(textString);
            sheet.autoSizeColumn((short) i);
        }
    }


    /**
     * 设置正文单元格格式
     *
     * @param workBook
     * @return
     */
    private HSSFCellStyle setBodyStyle(HSSFWorkbook workBook) {
        HSSFCellStyle style2 = workBook.createCellStyle();
        style2.setFillForegroundColor(HSSFColor.WHITE.index);
        style2.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style2.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        style2.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style2.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style2.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style2.setAlignment(HSSFCellStyle.ALIGN_LEFT);

        HSSFFont font2 = workBook.createFont();
        font2.setFontName("微软雅黑");
        font2.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
        style2.setFont(font2);
        return style2;
    }

    /**
     * 设置表头格式
     *
     * @param workBook
     * @return
     */
    private HSSFCellStyle setHeaderStyle(HSSFWorkbook workBook) {
        HSSFCellStyle style = workBook.createCellStyle();
        style.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setAlignment(HSSFCellStyle.ALIGN_LEFT);

        HSSFFont font = workBook.createFont();
        font.setFontName("微软雅黑");
        font.setFontHeightInPoints((short) 12);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        style.setFont(font);
        return style;
    }

    public static void main(String[] args) {
        String full_name = "abc";
        try {
            String path = System.getProperty("user.dir");
            OutputStream os = new FileOutputStream(path+"/"+full_name+".xls");
            String[] headers = {"姓名","年龄","学号","博客链接"};
            String [] properties = new String[]{"name","age","number","url"};  // 查询对应的字段
            List<Map> userList = new ArrayList<Map>();
            Map usermap1 = new HashMap();
            usermap1.put("name","白滚滚上神");
            usermap1.put("age","1000000");
            usermap1.put("number","20100914001000000000");
            usermap1.put("url","http://baigungun.blog.com.cn/index");

            Map usermap2 = new HashMap();
            usermap2.put("name","天族夜华");
            usermap2.put("age","300000");
            usermap2.put("number","20100914002");
            usermap2.put("url","http://yehua.com.cn/index");

            userList.add(usermap1);
            userList.add(usermap2);

            ExcelExportUtil transToExcel = new ExcelExportUtil();
            transToExcel.exporteExcel(full_name,headers,properties,userList,os);
            os.close();

        }catch (FileNotFoundException e){
            System.out.println("无法找到文件");
        }catch (IOException e){
            System.out.println("写入文件失败");
        }
    }
}
