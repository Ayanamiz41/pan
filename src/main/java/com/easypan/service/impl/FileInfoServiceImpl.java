package com.easypan.service.impl;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.List;

import com.easypan.component.RedisComponent;
import com.easypan.entity.config.AppConfig;
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
import com.easypan.utils.DateUtils;
import com.easypan.utils.ProcessUtils;
import com.easypan.utils.ScaleFilter;
import com.easypan.utils.StringTools;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
	@Autowired
	private AppConfig  appConfig;
	@Autowired
	@Lazy
	private FileInfoServiceImpl  fileInfoServiceImpl;

	private static final Logger logger = LoggerFactory.getLogger(FileInfoServiceImpl.class);

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
	@Transactional(rollbackFor = Exception.class) // 事务注解，发生异常时回滚
	public UploadResultDto uploadFile(SessionWebUserDto sessionWebUserDto, String fileId, MultipartFile file, String fileName, String filePid, String fileMd5, Integer chunkIndex, Integer chunks) {
		UploadResultDto  resultDto = new UploadResultDto();
		File tempFileFolder = null;
		Boolean uploadSuccess = true;
		try{
			// 若 fileId 为空，自动生成
			if(StringTools.isEmpty(fileId)){
				fileId = StringTools.getRandomString(Constants.LENGTH_10);
			}
			resultDto.setFileId(fileId);
			Date curDate = new Date();
			UserSpaceDto userSpaceDto = redisComponent.getUserSpace(sessionWebUserDto.getUserId());

			// 秒传逻辑，只在上传第一个分片时判断
			if(chunkIndex == 0){
				FileInfoQuery fileInfoQuery = new FileInfoQuery();
				fileInfoQuery.setFileMd5(fileMd5);
				fileInfoQuery.setSimplePage(new SimplePage(0,1));
				fileInfoQuery.setStatus(FileStatusEnum.TRANSCODING_COMPLETED.getStatus());
				List<FileInfo> fileList = fileInfoMapper.selectList(fileInfoQuery);

				// 如果找到了同样的文件，直接插入新记录，实现秒传
				if(!fileList.isEmpty()){
					FileInfo fileInfo = fileList.get(0);
					if(fileInfo.getFileSize()+userSpaceDto.getUseSpace()>userSpaceDto.getTotalSpace()){
						throw new BusinessException(ResponseCodeEnum.CODE_904); // 空间不足
					}
					fileInfo.setFileId(fileId);
					fileInfo.setFilePid(filePid);
					fileInfo.setUserId(sessionWebUserDto.getUserId());
					fileInfo.setCreateTime(curDate);
					fileInfo.setLastUpdateTime(curDate);
					fileInfo.setDelFlag(FileDelFlagEnum.USING.getFlag());
					fileName = autoRename(filePid,sessionWebUserDto.getUserId(),fileName); // 重命名重复文件
					fileInfo.setFileName(fileName);
					fileInfoMapper.insert(fileInfo);
					updateUserSpace(sessionWebUserDto,fileInfo.getFileSize()); // 更新用户空间
					resultDto.setStatus(UploadStatusEnum.UPLOAD_SECONDS.getCode());
					return resultDto;
				}
			}

			// 校验当前上传是否超出剩余空间
			Long currentTempSize = redisComponent.getFileTempSize(sessionWebUserDto.getUserId(),fileId);
			if(file.getSize()+currentTempSize+userSpaceDto.getUseSpace()>userSpaceDto.getTotalSpace()){
				throw new BusinessException(ResponseCodeEnum.CODE_904);
			}

			// 创建临时分片存储目录
			String tempFolderName = appConfig.getProjectFolder()+Constants.FILE_FOLDER_TEMP;
			String currentUserFolderName = sessionWebUserDto.getUserId()+fileId;
			tempFileFolder = new File(tempFolderName +"/"+ currentUserFolderName);
			if(!tempFileFolder.exists()){
				tempFileFolder.mkdirs();
			}

			// 将当前分片写入临时目录
			File newFile = new File(tempFileFolder.getPath()+"/"+chunkIndex);
			file.transferTo(newFile);
			redisComponent.saveFileTempSize(sessionWebUserDto.getUserId(),fileId,file.getSize());

			// 不是最后一个分片，直接返回
			if(chunkIndex<chunks-1){
				resultDto.setStatus(UploadStatusEnum.UPLOADING.getCode());
				return resultDto;
			}

			// 最后一个分片，执行合并逻辑，生成 FileInfo 并记录到数据库
			String month = DateUtils.format(new Date(),DateTimePatternEnum.YYYYMM.getPattern());
			String fileSuffix = StringTools.getFileSuffix(fileName);
			String realFileName = currentUserFolderName + fileSuffix;
			FileTypeEnum fileTypeEnum = FileTypeEnum.getFileTypeBySuffix(fileSuffix);
			fileName = autoRename(filePid,sessionWebUserDto.getUserId(),fileName);

			FileInfo  fileInfo = new FileInfo();
			fileInfo.setFileId(fileId);
			fileInfo.setUserId(sessionWebUserDto.getUserId());
			fileInfo.setFileMd5(fileMd5);
			fileInfo.setFileName(fileName);
			fileInfo.setFilePath(month+"/"+realFileName);
			fileInfo.setFilePid(filePid);
			fileInfo.setCreateTime(curDate);
			fileInfo.setLastUpdateTime(curDate);
			fileInfo.setFileCategory(fileTypeEnum.getCategory().getCategory());
			fileInfo.setFileType(fileTypeEnum.getType());
			fileInfo.setStatus(FileStatusEnum.TRANSCODING.getStatus());
			fileInfo.setFolderType(FileFolderTypeEnum.FILE.getType());
			fileInfo.setDelFlag(FileDelFlagEnum.USING.getFlag());

			Long totalSize = redisComponent.getFileTempSize(sessionWebUserDto.getUserId(),fileId);
			fileInfoMapper.insert(fileInfo);
			updateUserSpace(sessionWebUserDto,totalSize);
			resultDto.setStatus(UploadStatusEnum.UPLOAD_FINISH.getCode());

			// 注册事务提交后的回调，异步转码处理
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					fileInfoServiceImpl.transcodeFile(fileInfo.getFileId(),sessionWebUserDto);
				}
			});

			return resultDto;
		}catch (BusinessException e){
			logger.error("文件上传失败",e);
			uploadSuccess = false;
			throw e;
		}
		catch (Exception e) {
			logger.error("文件上传失败",e);
			uploadSuccess = false;
		}finally {
			if(!uploadSuccess&&tempFileFolder!=null){
				try {
					FileUtils.deleteDirectory(tempFileFolder); // 上传失败清理临时目录
				} catch (IOException e) {
					logger.error("删除临时目录失败",e);
				}
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

	@Async
	public void transcodeFile(String fileId, SessionWebUserDto sessionWebUserDto){
		Boolean transcodeSuccess = true;
		String targetFilePath = null;
		String cover = null;
		FileTypeEnum fileTypeEnum = null;
		FileInfo fileInfo = fileInfoMapper.selectByFileIdAndUserId(fileId,sessionWebUserDto.getUserId());
		try{
			if(fileInfo==null||!FileStatusEnum.TRANSCODING.getStatus().equals(fileInfo.getStatus())){
				return; // 状态异常或未找到
			}
			// 构造文件路径
			String tempFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER_TEMP;
			String currentUserFolderName = sessionWebUserDto.getUserId() + fileId;
			File fileFolder = new File(tempFolderName+"/"+currentUserFolderName);
			String fileSuffix = StringTools.getFileSuffix(fileInfo.getFileName());
			String month = DateUtils.format(fileInfo.getCreateTime(),DateTimePatternEnum.YYYYMM.getPattern());
			String targetFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
			File targetFolder = new File(targetFolderName+"/"+month);
			if(!targetFolder.exists()){
				targetFolder.mkdirs();
			}
			String realFileName = currentUserFolderName + fileSuffix;
			targetFilePath = targetFolder.getPath()+"/"+realFileName;

			// 合并分片文件为完整文件
			mergeFileChunks(fileFolder.getPath(),targetFilePath,fileInfo.getFileName(),true);
			fileTypeEnum = FileTypeEnum.getFileTypeBySuffix(fileSuffix);

			// 视频类型转码 + 封面生成
			if(fileTypeEnum==FileTypeEnum.VIDEO){
				cutFile4Video(fileId,targetFilePath);
				cover = month + "/" + currentUserFolderName + Constants.IMAGE_PNG_SUFFIX;
				String coverPath = targetFolderName + "/" + cover;
				ScaleFilter.createCover4Video(new File(targetFilePath),Constants.LENGTH_150,new File(coverPath));
			}
			// 图片生成缩略图
			else if(fileTypeEnum==FileTypeEnum.IMAGE){
				cover = month + "/" + realFileName.replace(".","_.");
				String coverPath = targetFolderName + "/" + cover;
				Boolean created = ScaleFilter.createThumbnailWidthFFmpeg(new File(targetFilePath),Constants.LENGTH_150,new File(coverPath),false);
				if(!created){
					FileUtils.copyFile(new File(targetFilePath),new File(coverPath));
				}
			}
		} catch (Exception e) {
			logger.error("文件转码失败，fileId:{}，userId:{}",fileId,sessionWebUserDto.getUserId(),e);
			transcodeSuccess = false;
		}finally {
			FileInfo updateFileInfo = new FileInfo();
			updateFileInfo.setFileSize(new File(targetFilePath).length());
			updateFileInfo.setFileCover(cover);
			updateFileInfo.setStatus(transcodeSuccess?FileStatusEnum.TRANSCODING_COMPLETED.getStatus() : FileStatusEnum.TRANSCODING_FAILED.getStatus());
			fileInfoMapper.updateWithOldStatus(fileId,sessionWebUserDto.getUserId(),updateFileInfo,FileStatusEnum.TRANSCODING.getStatus());
		}
	}


	private void mergeFileChunks(String dirPath,String toFilePath,String fileName,Boolean delSource){
		File dir = new File(dirPath);
		if(!dir.exists()){
			throw new BusinessException("目录不存在");
		}
		File[] fileList = dir.listFiles();
		File targetFile = new File(toFilePath);
		RandomAccessFile writeFile = null;
		try{
			writeFile = new RandomAccessFile(targetFile,"rw");
			byte[] buffer = new byte[1024*10];
			for(int index=0;index<fileList.length;index++){
				int len = -1;
				File chunkFile = new File(dirPath+"/"+index);
				RandomAccessFile readFile = null;
				try{
					readFile = new RandomAccessFile(chunkFile,"r");
					while((len = readFile.read(buffer))!=-1){
						writeFile.write(buffer,0,len);
					}
				} catch (Exception e) {
					logger.error("合并分片失败",e);
					throw new BusinessException("合并分片失败");
				}finally {
					if (readFile != null) {
						readFile.close();
					}
				}
			}
		} catch (Exception e) {
			logger.error("合并文件:{}失败",fileName,e);
			throw new BusinessException("合并文件"+fileName+"失败");
		}finally {
			if(writeFile!=null){
				try {
					writeFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(delSource&& dir.exists()){
				try {
					FileUtils.deleteDirectory(dir);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}


	private void cutFile4Video(String fileId,String videoFilePath){
		File tsFolder = new File(videoFilePath.substring(0,videoFilePath.lastIndexOf(".")));
		if(!tsFolder.exists()){
			tsFolder.mkdirs();
		}
		final String CMD_TRANSFER_2_TS = "ffmpeg -y -i %s  -vcodec copy -acodec copy -bsf:v h264_mp4toannexb %s";
		final String CMD_CUT_TS = "ffmpeg -i %s -c copy -map 0 -f segment -segment_list %s -segment_time 30 %s/%s_%%4d.ts";
		String tsPath = tsFolder + "/" + Constants.TS_NAME;
		String cmd = String.format(CMD_TRANSFER_2_TS,videoFilePath,tsPath);
		ProcessUtils.executeCommand(cmd,false);
		cmd =String.format(CMD_CUT_TS,tsPath,tsFolder.getPath()+"/"+Constants.M3U8_NAME,tsFolder.getPath(),fileId);
		ProcessUtils.executeCommand(cmd,false);
		new File(tsPath).delete();
	}

}