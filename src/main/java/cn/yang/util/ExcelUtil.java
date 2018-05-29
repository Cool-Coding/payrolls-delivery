package cn.yang.util;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

/**
 * User:cool coding
 * Date:2017/12/19
 * Time:14:00
 */
public class ExcelUtil {
    private final String ext1="xls";
    private final String ext2="xlsx";
    private ArrayList<String> headColors;
    private Logger logger=LoggerFactory.getLogger(this.getClass());
    private String password;

      /**
     * 读取Excel数据
     * @param file
     * @return
     * @throws IOException
     */
    public ArrayList<String[]> readExcel(File file) throws IOException{
          ArrayList<String[]> data;
          try {
              data = readExcel_(file);
              return data;
          }catch(InvalidFormatException e){
              logger.error(file.getName()+"的类型不是Excel");
              throw new IOException(file.getName()+"的类型不是Excel");
          }
    }

    /**
     *
     * @param file
     * @return
     * @throws IOException
     * @throws InvalidFormatException
     */
    private ArrayList<String[]> readExcel_(File file) throws IOException,InvalidFormatException{
        Workbook wb=openWorkbook(file);
        if (wb==null) return null;
        if(wb.getNumberOfSheets()>1){
            logger.info(file.getName()+"发现多个sheet，只会读取第一个");
        }
        //读取第一个sheet
        Sheet sheet = wb.getSheetAt(0);
        //读取列数(以第一行为准)
        int colCount=sheet.getRow(0).getPhysicalNumberOfCells();
        //列索引
        int colIndex=0;
        //日期格式
        String dateFormat="yyyy-MM-dd";
        //读取数据
        Iterator<Row> rowIterator = sheet.rowIterator();
        //数值保留两位小数
        DecimalFormat df   = new DecimalFormat("#0.00");
        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
        //抬头颜色列表
        headColors=new ArrayList<>();
        //Excel数据
        ArrayList<String[]> data=new ArrayList<>();

        while (rowIterator.hasNext()){
            //每行时初始化列索引和数组
            colIndex=0;
            String[] row_string=new String[colCount];
            //读取列的数据
            Row row=rowIterator.next();
            final Iterator<Cell> cellIterator = row.cellIterator();
            while(cellIterator.hasNext()){
                Cell cell=cellIterator.next();
                switch (evaluator.evaluateInCell(cell).getCellType()) {
                    case Cell.CELL_TYPE_BOOLEAN:
                        row_string[cell.getColumnIndex()]=cell.getBooleanCellValue()?"true":"false";
                        break;
                    case Cell.CELL_TYPE_NUMERIC:
                        if (HSSFDateUtil.isCellDateFormatted(cell)) {// 日期类型
                            Date date = cell.getDateCellValue();
                            row_string[cell.getColumnIndex()]=new SimpleDateFormat(dateFormat).format(date);
                        }else
                        row_string[cell.getColumnIndex()]=df.format(cell.getNumericCellValue());
                        break;
                    case Cell.CELL_TYPE_STRING:
                        row_string[cell.getColumnIndex()]= CommonUtils.trimWhitespace(cell.getStringCellValue());
                        break;
                    case Cell.CELL_TYPE_BLANK:
                        row_string[cell.getColumnIndex()] = "";
                        break;
                    case Cell.CELL_TYPE_ERROR:
                        break;
                    // CELL_TYPE_FORMULA will never happen
                    case Cell.CELL_TYPE_FORMULA:
                        break;
                }


                if (cell.getRowIndex()==0){
                    final Color fillBackgroundColorColor = cell.getCellStyle().getFillForegroundColorColor();
                    if(fillBackgroundColorColor instanceof HSSFColor){
                        HSSFColor color=(HSSFColor)fillBackgroundColorColor;
                        final short[] triplet = color.getTriplet();
                        String color_str="#";
                        for(short c:triplet){
                            color_str+=Integer.toHexString(c);
                        }
                        if(color_str.equals("#000"))color_str="";
                        headColors.add(color_str);
                    }else if(fillBackgroundColorColor instanceof XSSFColor){
                        XSSFColor color=(XSSFColor)fillBackgroundColorColor;
                        final byte[] argb = color.getRGB();
                        double tint = color.getTint();

                        String color_str="";
                        for(byte b:argb){
                            int Lum= CommonUtils.colorParse2Int(b);
                            if(tint>0) {
                                Lum=(int)(Lum * (1.0-tint) + (255- 255 * (1.0-tint)));
                            }else {
                                Lum=(int)(Lum * (1.0 + tint));
                            }
                            color_str+=Integer.toHexString(Lum);
                        }
                        color_str="#"+color_str;
                        headColors.add(color_str);
                    }else {
                        headColors.add(null);
                    }
                }
                colIndex++;
                //如果当前列比第一行的列数多，则按照第一行列数取值，其它列值丢弃
                if(colIndex>=colCount)break;
            }
            data.add(row_string);
        }
        if (data != null && data.size() > 0) {
            int size = data.size();
            ArrayList<Integer> indexs = new ArrayList<>();
            char nullLine;
            for (int i = 0; i < size; i++) {
                nullLine = 'X';
                for (String word : data.get(i)) {
                    if (word != null) nullLine = ' ';
                }
                if (nullLine == 'X') indexs.add(i);
            }
            if (indexs.size() > 0) {
                data.removeAll(indexs);
            }
        }
        wb.close();//关闭打开的excel
        return data;
    }

      /**
     *打开工作薄
     */

    private  Workbook openWorkbook(File file) throws IOException,InvalidFormatException {
        InputStream fileInputStream = new FileInputStream(file);
        Workbook wb;
        try {
            password=getPassword();
            wb = WorkbookFactory.create(fileInputStream, password);
            return wb;
        } catch (EncryptedDocumentException e) {
           logger.error(e.getMessage(),e);
        }
        return null;
    }

    /**
     * 获取excel标题颜色
     * @return 颜色列表
     */
    public ArrayList<String> getHeadColors(){
        return headColors;
    }

    /**
     * 读取密码
     * @return
     */
    private String getPassword(){
        final Properties properties = new Properties();
        try {
            properties.load(new ClassPathResource("src/main/resources/application.properties",this.getClass().getClassLoader()).getInputStream());
            password = properties.getProperty("excel.password");
        }catch (IOException e){
            logger.error(e.getMessage(),e);
        }
        return password;
    }
}
