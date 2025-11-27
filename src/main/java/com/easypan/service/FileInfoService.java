package com.easypan.service;


import java.util.List;

import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.dto.UploadResultDto;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.query.FileInfoQuery;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Description: 文件信息 Service
 * @Author: false
 * @Date: 2025/07/25 20:22:51
 */
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

	/**
	 * 上传文件
	 * @param sessionWebUserDto
	 * @param fileId
	 * @param file
	 * @param fileName
	 * @param filePid
	 * @param fileMd5
	 * @param chunkIndex
	 * @param chunks
	 * @return
	 */
	UploadResultDto uploadFile(SessionWebUserDto sessionWebUserDto, String fileId, MultipartFile file, String fileName, String filePid, String fileMd5, Integer chunkIndex, Integer chunks);

	/**
	 * 新建文件夹
	 * @param filePid
	 * @param userId
	 * @param folderName
	 * @return
	 */
	FileInfo newFolder(String filePid,String userId,String folderName);

	/**
	 * 文件重命名
	 * @param fileId
	 * @param userId
	 * @param newName
	 * @return
	 */
	FileInfo rename(String fileId,String userId,String newName);

	/**
	 * 加载所有文件夹
	 * @param userId
	 * @param filePid
	 * @param currentFolderIds
	 * @return
	 */
	List<FileInfo> loadAllFolder(String userId,String filePid,String currentFolderIds);

	/**
	 * 移动文件和文件夹
	 * @param userId
	 * @param fileIds
	 * @param filePid
	 */
	void changeFileFolder(String userId,String fileIds,String filePid);

	/**
	 * 将文件批量移动到回收站
	 * @param userId
	 * @param fileIds
	 */
	void removeFile2RecycleBatch(String userId,String fileIds);

	/**
	 * 批量还原文件
	 * @param userId
	 * @param fileIds
	 */
	void recoverFileBatch(String userId,String fileIds);

	/**
	 * 批量彻底删除文件
	 * @param userId
	 * @param fileIds
	 */
	void delFileBatch(String userId,String fileIds,Boolean admin);
}