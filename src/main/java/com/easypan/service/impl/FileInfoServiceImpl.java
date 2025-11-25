package com.easypan.service.impl;


import java.io.File;
import java.util.Date;
import java.util.List;

import com.easypan.component.RedisComponent;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.dto.UploadResultDto;
import com.easypan.entity.dto.UserSpaceDto;
import com.easypan.entity.po.UserInfo;
import com.easypan.entity.query.SimplePage;
import com.easypan.entity.query.UserInfoQuery;
import com.easypan.enums.*;
import com.easypan.exception.BusinessException;
import com.easypan.mappers.FileInfoMapper;
import com.easypan.mappers.UserInfoMapper;
import com.easypan.service.FileInfoService;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.utils.StringTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
/**
 * @Description: 文件信息 业务接口实现
 * @Author: false
 * @Date: 2025/07/25 20:22:51
 */
@Service("FileInfoMapper")
public class FileInfoServiceImpl implements FileInfoService{

	@Resource
	private FileInfoMapper<FileInfo, FileInfoQuery> fileInfoMapper;
	@Autowired
	private RedisComponent redisComponent;
	@Autowired
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

	/**
 	 * 根据条件查询列表
 	 */
	@Override
	public List<FileInfo> findListByParam(FileInfoQuery query) {
		return this.fileInfoMapper.selectList(query);	}

	/**
 	 * 根据条件查询数量
 	 */
	@Override
	public Integer findCountByParam(FileInfoQuery query) {
		return this.fileInfoMapper.selectCount(query);	}

	/**
 	 * 分页查询
 	 */
	@Override
	public PaginationResultVO<FileInfo> findListByPage(FileInfoQuery query) {
		Integer count = this.findCountByParam(query);
		Integer pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
		SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
		query.setSimplePage(page);
		List<FileInfo> list = this.findListByParam(query);
		PaginationResultVO<FileInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
 	 * 新增
 	 */
	@Override
	public Integer add(FileInfo bean) {
		return this.fileInfoMapper.insert(bean);
	}

	/**
 	 * 批量新增
 	 */
	@Override
	public Integer addBatch(List<FileInfo> listBean) {
		if ((listBean == null) || listBean.isEmpty()) {
			return 0;
		}
			return this.fileInfoMapper.insertBatch(listBean);
	}

	/**
 	 * 批量新增或修改
 	 */
	@Override
	public Integer addOrUpdateBatch(List<FileInfo> listBean) {
		if ((listBean == null) || listBean.isEmpty()) {
			return 0;
		}
			return this.fileInfoMapper.insertOrUpdateBatch(listBean);
	}

	/**
 	 * 根据 FileIdAndUserId 查询
 	 */
	@Override
	public FileInfo getFileInfoByFileIdAndUserId(String fileId, String userId) {
		return this.fileInfoMapper.selectByFileIdAndUserId(fileId, userId);}

	/**
 	 * 根据 FileIdAndUserId 更新
 	 */
	@Override
	public Integer updateFileInfoByFileIdAndUserId(FileInfo bean, String fileId, String userId) {
		return this.fileInfoMapper.updateByFileIdAndUserId(bean, fileId, userId);}

	/**
 	 * 根据 FileIdAndUserId 删除
 	 */
	@Override
	public Integer deleteFileInfoByFileIdAndUserId(String fileId, String userId) {
		return this.fileInfoMapper.deleteByFileIdAndUserId(fileId, userId);}

	/**
	 * 文件上传
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
	@Transactional(rollbackFor = Exception.class)
	public UploadResultDto uploadFile(SessionWebUserDto sessionWebUserDto, String fileId, MultipartFile file, String fileName, String filePid, String fileMd5, Integer chunkIndex, Integer chunks) {
		UploadResultDto  resultDto = new UploadResultDto();
		if(StringTools.isEmpty(fileId)){
			fileId = StringTools.getRandomNumber(Constants.LENGTH_10);
		}
		resultDto.setFileId(fileId);
		Date curDate = new Date();
		UserSpaceDto userSpaceDto = redisComponent.getUserSpace(sessionWebUserDto.getUserId());
		if(chunkIndex == 0){
			FileInfoQuery fileInfoQuery = new FileInfoQuery();
			fileInfoQuery.setFileMd5(fileMd5);
			fileInfoQuery.setSimplePage(new SimplePage());
			fileInfoQuery.setStatus(FileStatusEnum.TRANSCODING_COMPLETED.getStatus());
			List<FileInfo> fileList = fileInfoMapper.selectList(fileInfoQuery);
			if(!fileList.isEmpty()){
				//秒传
				FileInfo fileInfo = fileList.get(0);
				//判断文件大小
				if(fileInfo.getFileSize()+userSpaceDto.getUseSpace()>userSpaceDto.getTotalSpace()){
					throw new BusinessException(ResponseCodeEnum.CODE_904);
				}
				fileInfo.setFileId(fileId);
				fileInfo.setFilePid(filePid);
				fileInfo.setUserId(sessionWebUserDto.getUserId());
				fileInfo.setCreateTime(curDate);
				fileInfo.setLastUpdateTime(curDate);
				fileInfo.setDelFlag(FileDelFlagEnum.USING.getFlag());
				//文件重命名
				fileName = autoRename(filePid,sessionWebUserDto.getUserId(),fileName);
				fileInfo.setFileName(fileName);
				fileInfoMapper.insert(fileInfo);
				//更新用户使用空间
				updateUserSpace(sessionWebUserDto,fileInfo.getFileSize());
				resultDto.setStatus(UploadStatusEnum.UPLOAD_INSTANT.getCode());
				return resultDto;
			}
		}
		return resultDto;
	}

	private String autoRename(String filePid,String userId,String fileName){
		FileInfoQuery fileInfoQuery = new FileInfoQuery();
		fileInfoQuery.setUserId(userId);
		fileInfoQuery.setFilePid(filePid);
		fileInfoQuery.setDelFlag(FileDelFlagEnum.USING.getFlag());
		fileInfoQuery.setFileName(fileName);
		Integer count = fileInfoMapper.selectCount(fileInfoQuery);
		if(count>0){
  			fileName = StringTools.rename(fileName);
		}
		return fileName;
	}

	private void updateUserSpace(SessionWebUserDto sessionWebUserDto,Long fileSize){
		Integer count = userInfoMapper.updateUserSpace(sessionWebUserDto.getUserId(),fileSize,null);
		if(count==0){
			throw new BusinessException(ResponseCodeEnum.CODE_904);
		}
		UserSpaceDto userSpaceDto = redisComponent.getUserSpace(sessionWebUserDto.getUserId());
		userSpaceDto.setUseSpace(userSpaceDto.getUseSpace()+fileSize);
		redisComponent.saveUserSpace(sessionWebUserDto.getUserId(),userSpaceDto);
	}
}