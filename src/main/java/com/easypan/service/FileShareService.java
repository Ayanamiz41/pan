package com.easypan.service;


import java.util.List;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.po.FileShare;
import com.easypan.entity.query.FileShareQuery;
/**
 * @Description: 文件分享 Service
 * @Author: false
 * @Date: 2025/07/29 20:41:18
 */
public interface FileShareService{

	/**
 	 * 根据条件查询列表
 	 */
	List<FileShare> findListByParam(FileShareQuery query);

	/**
 	 * 根据条件查询数量
 	 */
	Integer findCountByParam(FileShareQuery query);

	/**
 	 * 分页查询
 	 */
	PaginationResultVO<FileShare> findListByPage(FileShareQuery query);

	/**
 	 * 新增
 	 */
	Integer add(FileShare bean);

	/**
 	 * 批量新增
 	 */
	Integer addBatch(List<FileShare> listBean);

	/**
 	 * 批量新增或修改
 	 */
	Integer addOrUpdateBatch(List<FileShare> listBean);

	/**
 	 * 根据 ShareId 查询
 	 */
	FileShare getFileShareByShareId(String shareId);

	/**
 	 * 根据 ShareId 更新
 	 */
	Integer updateFileShareByShareId(FileShare bean, String shareId); 

	/**
 	 * 根据 ShareId 删除
 	 */
	Integer deleteFileShareByShareId(String shareId);

	/**
	 * 保存分享
	 * @param fileShare
	 */
	void saveShare(FileShare fileShare);

	/**
	 * 批量删除分享
	 * @param ShareIdArray
	 * @param userId
	 */
	void deleteFileShareBatch(String[] ShareIdArray,String userId);
}