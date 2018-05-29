package cn.yang.util;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User:cool coding
 * Date:2017/12/19
 * Time:14:46
 */
public final class CommonUtils {
    private static Logger logger= LoggerFactory.getLogger(CommonUtils.class);

    private static final String property_path="C:"+
            File.separator+"ProgramData"+
            File.separator+"salarydata"+
            File.separator+"salary";

    public static String getExtention(File file){
        String name=file.getName();
        int index=name.lastIndexOf('.');
        if (index>0){
            return name.substring(index+1);
        }else{
            return null;
        }
    }

    public static boolean checkIsExcel(String name){
        String ext=null;
        int index=name.lastIndexOf('.');
        if (index>0){
            ext=name.substring(index+1);
        }

        if (ext!=null && (ext.equals("xls") || ext.equals("xlsx"))){
            return true;
        }else{
            logger.error(name+"的类型不是Excel");
            return false;
        }
    }

    public static String buildHTML(ArrayList headerColors, String[] header, String[] line){
        String str="<style>\n" +
                "tr td,tr th{border:1px solid #AAA;padding-top:2px;padding-bottom:2px;padding-left:15px;padding-right:15px;vertical-align:middle;text-align:center;}\n" +
                "table{border:0;border-collapse:collapse}\n" +
                "</style>" +
                "<table><tr>";
        int count=header.length-1;//最后一列不显示在工资条中
        for(int i=0;i<count;i++){
            str+="<th "+(headerColors.get(i)==null?"":("bgcolor=\""+headerColors.get(i)+"\""))+">"+header[i]+"</th>";
        }
        str+="</tr><tr>";
        for(int i=0;i<count;i++){
            str+="<td>"+(line[i]==null?"":(line[i].equals("0.00")|| line[i].equals(".00")?0:line[i]))+"</td>";
        }
        str+="</tr></table>";
        return str;

    }

    /**
     * 将Excel颜色转换为十六进制颜色
     * @param number
     * @return
     */
    public static String colorParse2Hex(short number){
        int colorIndex=((number & 0x0f0) >> 4)*16 +(number & 0x0f);
        return Integer.toHexString(colorIndex);
    }

    /**
     * 将Excel颜色转换为十进制颜色
     * @param number
     * @return
     */
    public static int colorParse2Int(short number){
        return ((number & 0x0f0) >> 4)*16 +(number & 0x0f);
    }

    public static void saveEmailLoginData(HashMap<String,String> loginData){
        try {
            File file=new File(property_path);
            File parentFile=file.getParentFile();
            if (!parentFile.exists())parentFile.mkdirs();
            FileOutputStream outputStream=new FileOutputStream(file);
            Properties properties=new Properties();
            for(Map.Entry<String,String> entry:loginData.entrySet()){
                try {
                    properties.setProperty(new BASE64Encoder().encode(entry.getKey().getBytes("utf-8")), new BASE64Encoder().encode(entry.getValue().getBytes("utf-8")));
                }catch(UnsupportedEncodingException e){
                }
            }

            try {
                properties.store(outputStream,"email login data");
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 正则表达式校验邮箱
     * @param email 待匹配的邮箱
     * @return 匹配成功返回true 否则返回false;
     */
    public static boolean checkEmail(String email){
        String RULE_EMAIL = "^\\w+((-\\w+)|(\\.\\w+))*\\@[A-Za-z0-9]+((\\.|-)[A-Za-z0-9]+)*\\.[A-Za-z0-9]+$";
        //正则表达式的模式
        Pattern p = Pattern.compile(RULE_EMAIL);
        //正则表达式的匹配器
        Matcher m = p.matcher(email);
        //进行正则匹配
        return m.matches();
    }

    /**
     * 去除字符串中的空格字符
     * @param var0
     * @return
     */
    public static String trimWhitespace(String var0) {
        if(var0 == null) {
            return var0;
        } else {
            StringBuffer var1 = new StringBuffer();

            for(int var2 = 0; var2 < var0.length(); ++var2) {
                char var3 = var0.charAt(var2);
                if(var3 != 10 && var3 != 12 && var3 != 13 && var3 != 9) {
                    var1.append(var3);
                }
            }

            return var1.toString().trim();
        }
    }
}