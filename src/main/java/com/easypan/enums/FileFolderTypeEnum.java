package com.easypan.enums;

public enum FileFolderTypeEnum {
    FILE(0,"文件"),
    FOLDER(1,"目录");

    private Integer type;
    private String desc;
    private FileFolderTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public Integer getType() {
        return type;
    }
}
