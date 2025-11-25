package com.easypan.entity.po;

import java.io.Serializable;

import com.easypan.enums.DateTimePatternEnum;
import com.easypan.utils.DateUtils;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;



public class FileInfo implements Serializable {
	/**
 	 * 文件id
 	 */
	private String fileId;

	/**
 	 * 用户id
 	 */
	private String userId;

	/**
 	 * 文件md5值
 	 */
	private String fileMd5;

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
 	 * 文件路径
 	 */
	private String filePath;

	/**
 	 * 创建时间
 	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

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
	@JsonIgnore
	private Integer status;

	/**
 	 * 进入回收站时间
 	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date recycleTime;

	/**
 	 * 删除标记 0：删除 1：回收站  2：正常
 	 */
	private Integer delFlag;


	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getFileId() {
		return fileId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}

	public void setFileMd5(String fileMd5) {
		this.fileMd5 = fileMd5;
	}

	public String getFileMd5() {
		return fileMd5;
	}

	public void setFilePid(String filePid) {
		this.filePid = filePid;
	}

	public String getFilePid() {
		return filePid;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileCover(String fileCover) {
		this.fileCover = fileCover;
	}

	public String getFileCover() {
		return fileCover;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setLastUpdateTime(Date lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public Date getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setFolderType(Integer folderType) {
		this.folderType = folderType;
	}

	public Integer getFolderType() {
		return folderType;
	}

	public void setFileCategory(Integer fileCategory) {
		this.fileCategory = fileCategory;
	}

	public Integer getFileCategory() {
		return fileCategory;
	}

	public void setFileType(Integer fileType) {
		this.fileType = fileType;
	}

	public Integer getFileType() {
		return fileType;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getStatus() {
		return status;
	}

	public void setRecycleTime(Date recycleTime) {
		this.recycleTime = recycleTime;
	}

	public Date getRecycleTime() {
		return recycleTime;
	}

	public void setDelFlag(Integer delFlag) {
		this.delFlag = delFlag;
	}

	public Integer getDelFlag() {
		return delFlag;
	}
	@Override
	public String toString() {
		return "文件id:" + (fileId == null ? "空" : fileId) + "," + 
				"用户id:" + (userId == null ? "空" : userId) + "," + 
				"文件md5值:" + (fileMd5 == null ? "空" : fileMd5) + "," + 
				"父级文件id:" + (filePid == null ? "空" : filePid) + "," + 
				"文件大小 单位字节:" + (fileSize == null ? "空" : fileSize) + "," + 
				"文件名:" + (fileName == null ? "空" : fileName) + "," + 
				"文件封面:" + (fileCover == null ? "空" : fileCover) + "," + 
				"文件路径:" + (filePath == null ? "空" : filePath) + "," + 
				"创建时间:" + (createTime == null ? "空" : DateUtils.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + "," + 
				"最近更新时间:" + (lastUpdateTime == null ? "空" : DateUtils.format(lastUpdateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + "," + 
				"0：文件  1：目录:" + (folderType == null ? "空" : folderType) + "," + 
				"文件分类 1：视频  2：音频  3：图片  4：文档  5：其他:" + (fileCategory == null ? "空" : fileCategory) + "," + 
				"文件类型 1：视频  2：音频  3：图片  4：pdf  5：doc  6：excel  7：txt  8：code  9：zip  10：其他:" + (fileType == null ? "空" : fileType) + "," + 
				"状态 0：转码中  1：转码失败  2：转码成功  :" + (status == null ? "空" : status) + "," + 
				"进入回收站时间:" + (recycleTime == null ? "空" : DateUtils.format(recycleTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + "," + 
				"删除标记 0：删除 1：回收站  2：正常:" + (delFlag == null ? "空" : delFlag);
		}
}