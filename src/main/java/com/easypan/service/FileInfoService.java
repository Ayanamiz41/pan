package com.easypan.service;


import java.util.List;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.query.FileInfoQuery;

public interface FileInfoService{

	/**
 	 * 根据条件查询列表
 	 */
	List<FileInfo> findListByParam(FileInfoQuery query);

	/**
 	 * 根据条件查询数量
 	 */
	Integer findCountByParam(FileInfoQuery query);

	/**
 	 * 分页查询
 	 */
	PaginationResultVO<FileInfo> findListByPage(FileInfoQuery query);

	/**
 	 * 新增
 	 */
	Integer add(FileInfo bean);

	/**
 	 * 批量新增
 	 */
	Integer addBatch(List<FileInfo> listBean);

	/**
 	 * 批量新增或修改
 	 */
	Integer addOrUpdateBatch(List<FileInfo> listBean);

	/**
 	 * 根据 FileIdAndUserId 查询
 	 */
	FileInfo getFileInfoByFileIdAndUserId(String fileId, String userId);

	/**
 	 * 根据 FileIdAndUserId 更新
 	 */
	Integer updateFileInfoByFileIdAndUserId(FileInfo bean, String fileId, String userId); 

	/**
 	 * 根据 FileIdAndUserId 删除
 	 */
	Integer deleteFileInfoByFileIdAndUserId(String fileId, String userId);
}