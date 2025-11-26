package com.easypan.enums;

public enum FileStatusEnum {
    TRANSCODING(0,"转码中"),
    TRANSCODING_FAILED(1,"转码失败"),
    USING(2,"使用中");

    private Integer status;
    private String desc;

    FileStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}
