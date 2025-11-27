package com.easypan.entity.query;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;


/**
 * @Description: 文件信息
 * @Author: false
 * @Date: 2025/07/25 20:22:51
 */
@Setter
@Getter
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
	 * 是否查用户昵称
	 */
	private Boolean queryUserNickName;

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

	/**
	 * 文件id数组
	 */
	private String[] fileIdArray;

	/**
	 * 排除的文件id数组
	 */
	private String[] excludeFileIdArray;

}