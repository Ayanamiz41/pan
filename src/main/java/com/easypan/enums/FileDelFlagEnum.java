package com.easypan.enums;

public enum FileDelFlagEnum {
    DEL(0,""),
    RECYCLE(1,""),
    USING(2,"");

    private Integer flag;
    private String desc;

    FileDelFlagEnum(Integer flag, String desc) {
        this.flag = flag;
        this.desc = desc;
    }

    public Integer getFlag(){return flag;}

    public String getDesc(){return desc;}
}
