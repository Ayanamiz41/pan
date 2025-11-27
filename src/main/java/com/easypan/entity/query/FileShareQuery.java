package com.easypan.entity.query;

import lombok.Data;

import java.util.Date;


/**
 * @Description: 文件分享
 * @Author: false
 * @Date: 2025/07/29 20:41:18
 */
@Data
public class FileShareQuery extends BaseQuery {
	/**
 	 * 分享id 查询对象
 	 */
	private String shareId;

	private String shareIdFuzzy;

	/**
 	 * 文件id 查询对象
 	 */
	private String fileId;

	private String fileIdFuzzy;

	/**
 	 * 分享人id 查询对象
 	 */
	private String userId;

	private String userIdFuzzy;

	/**
 	 * 有效期类型 0:一天 1:七天 2:三十天 3:永久有效 查询对象
 	 */
	private Integer validType;

	/**
 	 * 失效时间 查询对象
 	 */
	private Date expireTime;

	private String expireTimeStart;
	private String expireTimeEnd;
	/**
 	 * 分享时间 查询对象
 	 */
	private Date shareTime;

	private String shareTimeStart;
	private String shareTimeEnd;
	/**
 	 * 提取码 查询对象
 	 */
	private String code;

	private String CodeFuzzy;

	/**
 	 * 浏览次数 查询对象
 	 */
	private Integer showCount;

	/**
	 * 是否查询文件名
	 */
	private Boolean queryFileName;
}