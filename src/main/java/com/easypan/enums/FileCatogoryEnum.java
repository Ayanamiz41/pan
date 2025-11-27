package com.easypan.enums;

public enum FileCatogoryEnum {
    VIDEO(1,"video","视频"),
    MUSIC(2,"music","音频"),
    IMAGE(3,"image","图片"),
    DOC(4,"doc","文档"),
    OTHERS(5,"others","其他");

    private Integer category;
    private String code;
    private String desc;

    FileCatogoryEnum(Integer category, String code, String desc) {
        this.category = category;
        this.code = code;
        this.desc = desc;
    }

    public static FileCatogoryEnum getByCode(String code){
        for(FileCatogoryEnum item : FileCatogoryEnum.values()){
            if(item.getCode().equalsIgnoreCase(code)){
                return item;
            }
        }
        return null;
    }

    public Integer getCategory(){return category;}

    public String getCode(){return code;}
}
