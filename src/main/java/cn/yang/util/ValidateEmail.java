package cn.yang.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ValidateEmail {
    private Logger logger= LoggerFactory.getLogger(this.getClass());

    private ExcelUtil excelUtil;

    private int count;//工资条数

    public List<String> check(String path) {
        excelUtil=new ExcelUtil();
        File localfile=new File(path);
        List<String> messages=new ArrayList<>();
        try {
            final ArrayList<String[]> data = excelUtil.readExcel(localfile);

                if (data.size() < 2) {
                    logger.error("Excel"+path+"中至少要有一个工资条!");
                    messages.add("Excel中至少要有一个工资条!");
                }else {
                    //检查邮件地址格式是否正确
                     int size = data.size();
                    boolean error = false;
                    for (int i = 1; i < size; i++) {
                        String[] line = data.get(i);
                        String email = line[line.length - 1];
                        if (!CommonUtils.checkEmail(email)) {
                            logger.error("第" + (i + 1) + "行邮件地址[" + email + "]错误");
                            messages.add("第" + (i + 1) + "行邮件地址[" + email + "]错误");
                            error = true;
                            break;
                        }
                    }
                    if (!error) {
                        logger.info("将会下发" + (data.size() - 1) + "个工资条");
                        count=data.size()-1;
                    }

                }
        }catch (IOException e){
            logger.error(e.getMessage(),e);
            messages.add(e.getMessage());
        }
        return messages;
    }

    public int getRollCount(){
        return count;
    }
}
