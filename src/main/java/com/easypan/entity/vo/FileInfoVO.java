package com.easypan.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class FileInfoVO {
    /**
     * 文件id
     */
    private String fileId;

    /**
     * 父级文件id
     */
    private String filePid;

    /**
     * 文件大小 单位字节
     */
    private Long fileSize;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件封面
     */
    private String fileCover;



    /**
     * 最近更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdateTime;

    /**
     * 0：文件  1：目录
     */
    private Integer folderType;

    /**
     * 文件分类 1：视频  2：音频  3：图片  4：文档  5：其他
     */
    private Integer fileCategory;

    /**
     * 文件类型 1：视频  2：音频  3：图片  4：pdf  5：doc  6：excel  7：txt  8：code  9：zip  10：其他
     */
    private Integer fileType;

    /**
     * 状态 0：转码中  1：转码失败  2：转码成功
     */
    private Integer status;


}
