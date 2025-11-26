package com.easypan.utils;

import com.easypan.entity.constants.Constants;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

public class StringTools {
    /**
     * 生成随机数串
     * @param count
     * @return
     */
    public static final String getRandomNumber(Integer count){
        return RandomStringUtils.random(count,false,true);
    }

    public static final String getRandomString(Integer count){
        return RandomStringUtils.random(count,true,true);
    }

    /**
     * 校验是否位空
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        if(str==null||str.equals("")||str.equals("null")||str.equals("\u0000")){
            return true;
        }else if((str.trim()).equals("")){
            return true;
        }
        return false;
    }

    public static String encodeByMd5(String originStr){
        return isEmpty(originStr)?null: DigestUtils.md5Hex(originStr);
    }

    public static boolean pathIsOk(String filePath) {
        if(isEmpty(filePath)){
            return true;
        }
        if(filePath.contains("../")||filePath.contains("..\\")){
            return false;
        }
        return true;
    }

    public static String rename(String fileName){
        String fileNameReal = getFileNameNoSuffix(fileName);
        String fileSuffix = getFileSuffix(fileName);
        return fileNameReal + "_" + getRandomString(Constants.LENGTH_5) + fileSuffix;
    }

    public static String getFileNameNoSuffix(String fileName){
        Integer index = fileName.lastIndexOf(".");
        if(index==-1){
            return fileName;
        }
        return fileName.substring(0,index);
    }

    public static String getFileSuffix(String fileName){
        Integer index = fileName.lastIndexOf(".");
        if(index==-1){
            return "";
        }
        return fileName.substring(index);
    }
}
