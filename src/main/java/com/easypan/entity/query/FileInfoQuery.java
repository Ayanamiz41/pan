package com.easypan.entity.query;

import java.util.Date;



public class FileInfoQuery extends BaseQuery {
	/**
 	 * 文件id 查询对象
 	 */
	private String fileId;

	private String fileIdFuzzy;

	/**
 	 * 用户id 查询对象
 	 */
	private String userId;

	private String userIdFuzzy;

	/**
 	 * 文件md5值 查询对象
 	 */
	private String fileMd5;

	private String fileMd5Fuzzy;

	/**
 	 * 父级文件id 查询对象
 	 */
	private String filePid;

	private String filePidFuzzy;

	/**
 	 * 文件大小 单位字节 查询对象
 	 */
	private Long fileSize;

	/**
 	 * 文件名 查询对象
 	 */
	private String fileName;

	private String fileNameFuzzy;

	/**
 	 * 文件封面 查询对象
 	 */
	private String fileCover;

	private String fileCoverFuzzy;

	/**
 	 * 文件路径 查询对象
 	 */
	private String filePath;

	private String filePathFuzzy;

	/**
 	 * 创建时间 查询对象
 	 */
	private Date createTime;

	private String createTimeStart;
	private String createTimeEnd;
	/**
 	 * 最近更新时间 查询对象
 	 */
	private Date lastUpdateTime;

	private String lastUpdateTimeStart;
	private String lastUpdateTimeEnd;
	/**
 	 * 0：文件  1：目录 查询对象
 	 */
	private Integer folderType;

	/**
 	 * 文件分类 1：视频  2：音频  3：图片  4：文档  5：其他 查询对象
 	 */
	private Integer fileCategory;

	/**
 	 * 文件类型 1：视频  2：音频  3：图片  4：pdf  5：doc  6：excel  7：txt  8：code  9：zip  10：其他 查询对象
 	 */
	private Integer fileType;

	/**
 	 * 状态 0：转码中  1：转码失败  2：转码成功   查询对象
 	 */
	private Integer status;

	/**
 	 * 进入回收站时间 查询对象
 	 */
	private Date recycleTime;

	private String recycleTimeStart;
	private String recycleTimeEnd;
	/**
 	 * 删除标记 0：删除 1：回收站  2：正常 查询对象
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

	public void setFileIdFuzzy(String fileIdFuzzy) {
		this.fileIdFuzzy = fileIdFuzzy;
	}

	public String getFileIdFuzzy() {
		return fileIdFuzzy;
	}

	public void setUserIdFuzzy(String userIdFuzzy) {
		this.userIdFuzzy = userIdFuzzy;
	}

	public String getUserIdFuzzy() {
		return userIdFuzzy;
	}

	public void setFileMd5Fuzzy(String fileMd5Fuzzy) {
		this.fileMd5Fuzzy = fileMd5Fuzzy;
	}

	public String getFileMd5Fuzzy() {
		return fileMd5Fuzzy;
	}

	public void setFilePidFuzzy(String filePidFuzzy) {
		this.filePidFuzzy = filePidFuzzy;
	}

	public String getFilePidFuzzy() {
		return filePidFuzzy;
	}

	public void setFileNameFuzzy(String fileNameFuzzy) {
		this.fileNameFuzzy = fileNameFuzzy;
	}

	public String getFileNameFuzzy() {
		return fileNameFuzzy;
	}

	public void setFileCoverFuzzy(String fileCoverFuzzy) {
		this.fileCoverFuzzy = fileCoverFuzzy;
	}

	public String getFileCoverFuzzy() {
		return fileCoverFuzzy;
	}

	public void setFilePathFuzzy(String filePathFuzzy) {
		this.filePathFuzzy = filePathFuzzy;
	}

	public String getFilePathFuzzy() {
		return filePathFuzzy;
	}

	public void setCreateTimeStart(String createTimeStart) {
		this.createTimeStart = createTimeStart;
	}

	public String getCreateTimeStart() {
		return createTimeStart;
	}

	public void setCreateTimeEnd(String createTimeEnd) {
		this.createTimeEnd = createTimeEnd;
	}

	public String getCreateTimeEnd() {
		return createTimeEnd;
	}

	public void setLastUpdateTimeStart(String lastUpdateTimeStart) {
		this.lastUpdateTimeStart = lastUpdateTimeStart;
	}

	public String getLastUpdateTimeStart() {
		return lastUpdateTimeStart;
	}

	public void setLastUpdateTimeEnd(String lastUpdateTimeEnd) {
		this.lastUpdateTimeEnd = lastUpdateTimeEnd;
	}

	public String getLastUpdateTimeEnd() {
		return lastUpdateTimeEnd;
	}

	public void setRecycleTimeStart(String recycleTimeStart) {
		this.recycleTimeStart = recycleTimeStart;
	}

	public String getRecycleTimeStart() {
		return recycleTimeStart;
	}

	public void setRecycleTimeEnd(String recycleTimeEnd) {
		this.recycleTimeEnd = recycleTimeEnd;
	}

	public String getRecycleTimeEnd() {
		return recycleTimeEnd;
	}
}