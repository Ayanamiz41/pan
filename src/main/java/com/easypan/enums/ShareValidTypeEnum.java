package com.easypan.enums;

public enum ShareValidTypeEnum {
    DAY_1(0,1,"一天"),
    DAY_7(1,7,"七天"),
    DAY_30(2,30,"三十天"),
    PERMANENT(3,-1,"永久有效");

    private Integer type;
    private Integer days;
    private String desc;

    ShareValidTypeEnum(Integer type,Integer days,String desc) {
        this.type = type;
        this.days = days;
        this.desc = desc;
    }

    public static ShareValidTypeEnum getByType(Integer type){
        for(ShareValidTypeEnum item : ShareValidTypeEnum.values()){
            if(item.getType().equals(type)){
                return item;
            }
        }
        return null;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public Integer getDays() {
        return days;
    }
}
