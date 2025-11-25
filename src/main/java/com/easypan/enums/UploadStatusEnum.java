package com.easypan.enums;

public enum UploadStatusEnum {
    UPLOAD_INSTANT("upload_instant","秒传"),
    UPLOADING("uploading","上传中"),
    UPLOAD_COMPLETED("upload_completed","上传完成");

    private String code;
    private String desc;

    UploadStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public String getCode() {
        return code;
    }
}
