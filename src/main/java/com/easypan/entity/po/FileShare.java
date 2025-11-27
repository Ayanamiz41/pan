package com.easypan.entity.po;

import java.io.Serializable;

import com.easypan.enums.DateTimePatternEnum;
import com.easypan.utils.DateUtils;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @Description: 文件分享
 * @Author: false
 * @Date: 2025/07/29 20:41:18
 */
@Data
public class FileShare implements Serializable {
	/**
 	 * 分享id
 	 */
	private String shareId;

	/**
 	 * 文件id
 	 */
	private String fileId;

	/**
 	 * 分享人id
 	 */
	private String userId;

	/**
 	 * 有效期类型 0:一天 1:七天 2:三十天 3:永久有效
 	 */
	private Integer validType;

	/**
 	 * 失效时间
 	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date expireTime;

	/**
 	 * 分享时间
 	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date shareTime;

	/**
 	 * 提取码
 	 */
	private String code;

	/**
 	 * 浏览次数
 	 */
	private Integer showCount;

	/**
	 * 文件名
	 */
	private String fileName;

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
	 * 文件封面
	 */
	private String fileCover;

}