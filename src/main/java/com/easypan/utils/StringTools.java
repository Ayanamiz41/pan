package com.easypan.utils;

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
}
