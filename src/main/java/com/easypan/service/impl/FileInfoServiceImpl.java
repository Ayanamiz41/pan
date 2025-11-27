package com.easypan.service.impl;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
@Service
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
				fileInfoQuery.setStatus(FileStatusEnum.USING.getStatus());
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


	public FileInfo newFolder(String filePid, String userId, String folderName) {
		checkFileName(filePid,userId,folderName,FileFolderTypeEnum.FOLDER.getType());
		Date curDate = new Date();
		FileInfo fileInfo = new FileInfo();
		fileInfo.setFilePid(filePid);
		fileInfo.setUserId(userId);
		fileInfo.setFileId(StringTools.getRandomString(Constants.LENGTH_10));
		fileInfo.setFileName(folderName);
		fileInfo.setCreateTime(curDate);
		fileInfo.setLastUpdateTime(curDate);
		fileInfo.setFolderType(FileFolderTypeEnum.FOLDER.getType());
		fileInfo.setStatus(FileStatusEnum.USING.getStatus());
		fileInfo.setDelFlag(FileDelFlagEnum.USING.getFlag());
		fileInfoMapper.insert(fileInfo);
		return fileInfo;
	}


	public FileInfo rename(String fileId, String userId, String newName) {
		FileInfo fileInfo = fileInfoMapper.selectByFileIdAndUserId(fileId, userId);
		if (fileInfo == null) {
			throw new BusinessException("文件不存在");
		}
		String filePid = fileInfo.getFilePid();
		if (fileInfo.getFolderType().equals(FileFolderTypeEnum.FILE.getType())) {
			newName = newName + StringTools.getFileSuffix(fileInfo.getFileName());
		}
		checkFileName(filePid, userId, newName, fileInfo.getFolderType());
		Date curDate = new Date();
		FileInfo updateFileInfo = new FileInfo();
		updateFileInfo.setFileName(newName);
		updateFileInfo.setLastUpdateTime(curDate);
		fileInfoMapper.updateByFileIdAndUserId(updateFileInfo, fileId, userId);
		fileInfo.setFileName(newName);
		fileInfo.setLastUpdateTime(curDate);
		return fileInfo;
	}

	public List<FileInfo> loadAllFolder(String userId, String filePid, String currentFolderIds) {
		FileInfoQuery fileInfoQuery = new FileInfoQuery();
		fileInfoQuery.setUserId(userId);
		fileInfoQuery.setFilePid(filePid);
		fileInfoQuery.setFolderType(FileFolderTypeEnum.FOLDER.getType());
		if(!StringTools.isEmpty(currentFolderIds)){
			fileInfoQuery.setExcludeFileIdArray(currentFolderIds.split(","));
		}
		fileInfoQuery.setOrderBy("create_time desc");
		fileInfoQuery.setDelFlag(FileDelFlagEnum.USING.getFlag());
		fileInfoQuery.setStatus(FileStatusEnum.USING.getStatus());
		return fileInfoMapper.selectList(fileInfoQuery);
	}

	/**
	 * 批量修改文件（或文件夹）所在目录。
	 * 若有重名文件将自动重命名，支持事务回滚。
	 *
	 * @param userId 操作用户ID
	 * @param fileIds 要移动的文件ID，多个以英文逗号分隔
	 * @param filePid 目标文件夹ID（可为"0"表示根目录）
	 */
	@Transactional(rollbackFor = Exception.class)
	public void changeFileFolder(String userId, String fileIds, String filePid) {
		// 若目标目录和源文件相同，直接抛异常（不能把文件移动到自己下面）
		if(fileIds.equals(filePid)){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}

		// 如果目标不是根目录，校验该目录是否存在且未被删除
		if(!Constants.ZERO_STR.equals(filePid)){
			FileInfo fileInfo = fileInfoMapper.selectByFileIdAndUserId(filePid, userId);
			if (fileInfo == null || !fileInfo.getDelFlag().equals(FileDelFlagEnum.USING.getFlag())) {
				throw new BusinessException(ResponseCodeEnum.CODE_600);
			}
		}

		// 拆分多个文件ID
		String[] fileIdArray = fileIds.split(",");

		// 查询目标目录下已有的文件列表，用于后续重名检测
		FileInfoQuery fileInfoQuery = new FileInfoQuery();
		fileInfoQuery.setUserId(userId);
		fileInfoQuery.setFilePid(filePid);
		List<FileInfo> fileInfoList = fileInfoMapper.selectList(fileInfoQuery);

		// 将目标目录中的文件按名称映射，用于快速判断是否重名
		Map<String, FileInfo> fileInfoMap = fileInfoList.stream()
				.collect(Collectors.toMap(FileInfo::getFileName, Function.identity(), (o1, o2) -> o1));

		// 查询用户选中的源文件信息
		fileInfoQuery = new FileInfoQuery();
		fileInfoQuery.setUserId(userId);
		fileInfoQuery.setFileIdArray(fileIdArray);
		List<FileInfo> selectedFileInfoList = fileInfoMapper.selectList(fileInfoQuery);

		// 遍历每一个需要移动的文件
		for(FileInfo item : selectedFileInfoList){
			// 检查新目录中是否已有重名文件
			FileInfo rootFileInfo = fileInfoMap.get(item.getFileName());

			// 构建更新对象
			FileInfo updateFileInfo = new FileInfo();
			updateFileInfo.setFilePid(filePid); // 设置新目录
			updateFileInfo.setLastUpdateTime(new Date()); // 更新时间

			// 若存在重名，则重命名该文件
			if(rootFileInfo != null){
				String newName = StringTools.rename(item.getFileName());
				updateFileInfo.setFileName(newName);
			}

			// 更新数据库中该文件的目录信息
			fileInfoMapper.updateByFileIdAndUserId(updateFileInfo, item.getFileId(), userId);
		}
	}

	/**
	 * 将多个文件或文件夹批量移动到回收站（逻辑删除）
	 * @param userId
	 * @param fileIds
	 */
	@Transactional(rollbackFor = Exception.class)
	public void removeFile2RecycleBatch(String userId, String fileIds) {
		// 将 fileIds 以逗号分隔成数组
		String[] fileIdArray = fileIds.split(",");

		// 查询这些 fileId 对应的文件信息，过滤掉已删除的
		FileInfoQuery query = new FileInfoQuery();
		query.setUserId(userId);
		query.setFileIdArray(fileIdArray);
		query.setDelFlag(FileDelFlagEnum.USING.getFlag());
		List<FileInfo> fileInfoList = fileInfoMapper.selectList(query);

		// 如果一个文件都查不到，直接返回
		if ((fileInfoList.isEmpty())) {
			return;
		}

		// 存储所有需要逻辑删除的文件夹的子文件的fileId
		List<String> delSubFileIdList = new ArrayList<>();
		for (FileInfo item : fileInfoList) {
			// 如果当前项是文件夹，递归查找其所有子文件夹，加入 delFileIdList
			if (item.getFolderType().equals(FileFolderTypeEnum.FOLDER.getType())) {
				findAllSubFolderFileList(delSubFileIdList,userId, item.getFileId(), FileDelFlagEnum.USING.getFlag());
			}
		}

		// 如果有文件需要处理，则批量更新这些文件的 delFlag 为 DEL
		if (!delSubFileIdList.isEmpty()) {
			FileInfo updateFileInfo = new FileInfo();
			updateFileInfo.setDelFlag(FileDelFlagEnum.DEL.getFlag());
			updateFileInfo.setRecycleTime(new Date());
			updateFileInfo.setLastUpdateTime(new Date());
			fileInfoMapper.updateFileDelFlagBatch(updateFileInfo, userId, null, delSubFileIdList, FileDelFlagEnum.USING.getFlag());
		}

		// 接下来更新传入的所有 fileId（包括文件和文件夹本身）的状态为“已回收”
		List<String> delFileIdList = Arrays.asList(fileIdArray);
		FileInfo fileInfo = new FileInfo();
		fileInfo.setRecycleTime(new Date());
		fileInfo.setLastUpdateTime(new Date());
		fileInfo.setDelFlag(FileDelFlagEnum.RECYCLE.getFlag());
		fileInfoMapper.updateFileDelFlagBatch(fileInfo, userId, null, delFileIdList, FileDelFlagEnum.USING.getFlag());
	}

	/**
	 * 批量还原文件
	 * @param userId
	 * @param fileIds
	 */
	@Transactional(rollbackFor = Exception.class)
	public void recoverFileBatch(String userId, String fileIds) {
		String[] fileIdArray = fileIds.split(",");
		FileInfoQuery query = new FileInfoQuery();
		query.setUserId(userId);
		query.setFileIdArray(fileIdArray);
		query.setDelFlag(FileDelFlagEnum.RECYCLE.getFlag());
		List<FileInfo> fileInfoList =  fileInfoMapper.selectList(query);
		//存储所有需要还原的文件夹的子文件的fileId
		List<String> recSubFileIdList = new ArrayList<>();
		for (FileInfo item : fileInfoList) {
			if(item.getFolderType().equals(FileFolderTypeEnum.FOLDER.getType())){
				findAllSubFolderFileList(recSubFileIdList, userId, item.getFileId(), FileDelFlagEnum.DEL.getFlag());
			}
		}
		//查询根目录所有文件
		query =  new FileInfoQuery();
		query.setUserId(userId);
		query.setDelFlag(FileDelFlagEnum.USING.getFlag());
		query.setFilePid(Constants.ZERO_STR);
		List<FileInfo> rootFileInfoList = fileInfoMapper.selectList(query);
		Map<String, FileInfo> fileInfoMap = rootFileInfoList.stream().collect(Collectors.toMap(FileInfo::getFileName, Function.identity(),(o1, o2) -> o1));

		// 如果有文件需要处理，则批量更新这些文件的 delFlag 为 USING
		if(!recSubFileIdList.isEmpty()){
			FileInfo updateFileInfo = new FileInfo();
			updateFileInfo.setDelFlag(FileDelFlagEnum.USING.getFlag());
			updateFileInfo.setLastUpdateTime(new Date());
			fileInfoMapper.updateFileDelFlagBatch(updateFileInfo, userId, null, recSubFileIdList, FileDelFlagEnum.DEL.getFlag());
		}

		//重命名
		for(FileInfo item : fileInfoList){
			FileInfo rootFileInfo = fileInfoMap.get(item.getFileName());
			if(rootFileInfo != null){
				//根目录存在同名文件或文件夹,需要重命名
				FileInfo updateFileInfo = new FileInfo();
				updateFileInfo.setFileName(StringTools.rename(item.getFileName()));
				fileInfoMapper.updateByFileIdAndUserId(updateFileInfo, item.getFileId(), item.getUserId());
			}
		}

		//将选中的文件更新为使用中，父级id更新为根目录（"0"）
		List<String> recFileIdList = Arrays.asList(fileIdArray);
		FileInfo updateFileInfo = new FileInfo();
		updateFileInfo.setDelFlag(FileDelFlagEnum.USING.getFlag());
		updateFileInfo.setFilePid(Constants.ZERO_STR);
		updateFileInfo.setLastUpdateTime(new Date());
		fileInfoMapper.updateFileDelFlagBatch(updateFileInfo,userId, null, recFileIdList, FileDelFlagEnum.RECYCLE.getFlag());
	}

	/**
	 * 批量彻底删除文件
	 * @param userId
	 * @param fileIds
	 * @param admin
	 */
	@Transactional(rollbackFor = Exception.class)
	public void delFileBatch(String userId, String fileIds,Boolean admin) {
		String[] fileIdArray = fileIds.split(",");
		FileInfoQuery query = new FileInfoQuery();
		query.setUserId(userId);
		query.setFileIdArray(fileIdArray);
		query.setDelFlag(admin?FileDelFlagEnum.USING.getFlag() : FileDelFlagEnum.RECYCLE.getFlag());
		List<FileInfo> fileInfoList = fileInfoMapper.selectList(query);

		//所选文件的所有子文件id
		List<String> delSubFileIdList = new ArrayList<>();
		for(FileInfo item:fileInfoList){
			if(item.getFolderType().equals(FileFolderTypeEnum.FOLDER.getType())){
				findAllSubFolderFileList(delSubFileIdList,userId, item.getFileId(), admin?FileDelFlagEnum.USING.getFlag():FileDelFlagEnum.DEL.getFlag());
			}
		}
		//删除所选文件子文件
		if(!delSubFileIdList.isEmpty()) {
			fileInfoMapper.deleteFileBatchWithOldDelFlag(userId, null, delSubFileIdList, admin ? null : FileDelFlagEnum.DEL.getFlag());
		}
		//删除所选文件
		List<String> delFileIdList = Arrays.asList(fileIdArray);
		fileInfoMapper.deleteFileBatchWithOldDelFlag(userId,null,delFileIdList,admin?null:FileDelFlagEnum.RECYCLE.getFlag());

		//更新用户使用空间
		Long useSpace = fileInfoMapper.selectUseSpaceByUserId(userId);
		UserInfo updateUserInfo = new UserInfo();
		updateUserInfo.setUseSpace(useSpace);
		userInfoMapper.updateByUserId(updateUserInfo, userId);

		//设置缓存
		UserSpaceDto userSpaceDto = redisComponent.getUserSpace(userId);
		userSpaceDto.setUseSpace(useSpace);
		redisComponent.saveUserSpace(userId,userSpaceDto);

	}

	/**
	 * 校验子目录 fileId 是否在共享根 rootFilePid 之下
	 */
	public void checkRootFilePid(String rootFilePid, String userId, String fileId) {
		// 如果 fileId 为空，视为非法
		if (StringTools.isEmpty(fileId)) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		// 如果访问的目录正好是共享根目录本身，则合法
		if (rootFilePid.equals(fileId)) {
			return;
		}
		// 否则递归向上校验父级目录
		checkFilePid(rootFilePid, userId, fileId);
	}


	/**
	 * 递归查找并验证，直到找到 rootFilePid 或到达顶层
	 */
	private void checkFilePid(String rootFilePid, String userId, String fileId) {
		// 查询当前目录信息
		FileInfo fileInfo = fileInfoMapper.selectByFileIdAndUserId(fileId, userId);
		// 如果不存在或不属于该用户，则非法
		if (fileInfo == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		// 如果已到顶层（父ID为"0"）还没找到根，则非法
		if (Constants.ZERO_STR.equals(fileInfo.getFilePid())) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		// 如果当前节点的父ID就是根目录，验证通过
		if (fileInfo.getFilePid().equals(rootFilePid)) {
			return;
		}
		// 否则继续向上一级验证
		checkFilePid(rootFilePid, userId, fileInfo.getFilePid());
	}

	/**
	 * 递归查找某文件夹下所有的子文件，并把它们的 fileId 加入到 fileIdList 中
	 * @param fileIdList
	 * @param userId
	 * @param fileId
	 * @param delFlag
	 */
	private void findAllSubFolderFileList(List<String> fileIdList ,String userId, String fileId, Integer delFlag) {
		//构造查询条件，查找该文件夹下的所有“子文件”
		FileInfoQuery query = new FileInfoQuery();
		query.setUserId(userId);
		query.setFilePid(fileId);  // 父级是当前文件夹
		query.setDelFlag(delFlag);
		// 执行查询
		List<FileInfo> fileInfoList = fileInfoMapper.selectList(query);
		//此文件不是文件夹或者文件夹中没有文件
		if(fileInfoList.isEmpty()){
			return;
		}
		for (FileInfo item : fileInfoList) {
			//将文件或者文件夹加入结果集合中
			fileIdList.add(item.getFileId());
			if(item.getFolderType().equals(FileFolderTypeEnum.FOLDER.getType())) {
				// 对每个子文件夹，递归调用本方法，继续查找下一层
				findAllSubFolderFileList(fileIdList, userId, item.getFileId(), delFlag);
			}
		}
	}

	/**
	 * 递归拷贝并重构一个文件（或文件夹）及其所有子节点的信息到新的列表中
	 *
	 * @param copyList       最终要插入的新 FileInfo 对象列表
	 * @param fileInfo       当前要拷贝的源 FileInfo 对象（会在此对象上修改）
	 * @param sourceUserId   源文件所属的用户 ID
	 * @param currentUserId  拷贝到目标的用户 ID
	 * @param curDate        统一使用的拷贝时间戳
	 * @param newFilePid     拷贝后该项在目标用户的父目录 ID
	 */
	private void findAllSubFolderFileList(List<FileInfo> copyList,
										  FileInfo fileInfo,
										  String sourceUserId,
										  String currentUserId,
										  Date curDate,
										  String newFilePid) {
		String sourceFileId = fileInfo.getFileId();      // 1. 记录源文件（或文件夹）原始 ID
		fileInfo.setCreateTime(curDate);                 // 2. 重置为拷贝时间
		fileInfo.setLastUpdateTime(curDate);             // 3. 同步更新时间
		fileInfo.setFilePid(newFilePid);                 // 4. 设置到目标目录的新父 ID
		fileInfo.setUserId(currentUserId);                // 5. 指定为目标用户所有
		String newFileId = StringTools.getRandomString(Constants.LENGTH_10);
		fileInfo.setFileId(newFileId);                   // 6. 生成并设置新的唯一 ID
		copyList.add(fileInfo);                          // 7. 将“修改后”的 FileInfo 放入待插入列表

		// 8. 如果这是一个文件夹，就继续查它的子级
		if (fileInfo.getFolderType().equals(FileFolderTypeEnum.FOLDER.getType())) {
			FileInfoQuery fileInfoQuery = new FileInfoQuery();
			fileInfoQuery.setFilePid(sourceFileId);      // 查源文件夹下的直接子项
			fileInfoQuery.setUserId(sourceUserId);       // 限定为原用户的数据
			List<FileInfo> sourceFileInfoList = fileInfoMapper.selectList(fileInfoQuery);

			// 9. 递归拷贝每个子项
			for (FileInfo item : sourceFileInfoList) {
				findAllSubFolderFileList(
						copyList,
						item,
						sourceUserId,
						currentUserId,
						curDate,
						newFileId     // 子项的新父 ID 是刚才生成的 newFileId
				);
			}
		}
	}

	/**
	 * 实现“分享到自己目录”：把别人分享给你的那些文件／文件夹及其所有子孙
	 * 拷贝到你指定的 myFolderId 目录下，并更新你的空间使用量。
	 *
	 * @param shareRootFileId  本次分享任务的根目录文件id
	 * @param shareFileIds    从分享者处拷贝哪些文件／文件夹（逗号分隔 ID 列表）
	 * @param myFolderId      你本地要放到哪个目录（父 ID）
	 * @param shareUserId     分享者用户 ID
	 * @param currentUserId   当前登录用户 ID（接收分享者文件的目标用户）
	 */
	public void saveShare(String shareRootFileId,
						  String shareFileIds,
						  String myFolderId,
						  String shareUserId,
						  String currentUserId) {
		// A. 找到目标目录下已有文件，用于后续重名检测
		String[] shareFileIdArray = shareFileIds.split(",");
		FileInfoQuery fileInfoQuery = new FileInfoQuery();
		fileInfoQuery.setUserId(currentUserId);
		fileInfoQuery.setFilePid(myFolderId);
		List<FileInfo> fileInfoList = fileInfoMapper.selectList(fileInfoQuery);
		Map<String, FileInfo> fileInfoMap = fileInfoList.stream()
				.collect(Collectors.toMap(
						FileInfo::getFileName,
						Function.identity(),
						(o1, o2) -> o1
				));

		// B. 校验：确保每个要拷贝的 fileId 都在 shareRootFileId 目录下
		for (String fileId : shareFileIdArray) {
			checkRootFilePid(shareRootFileId, shareUserId, fileId);
		}

		// C. 查询“分享者”这批 fileId 对应的源 FileInfo 列表
		fileInfoQuery = new FileInfoQuery();
		fileInfoQuery.setUserId(shareUserId);
		fileInfoQuery.setFileIdArray(shareFileIdArray);
		List<FileInfo> shareFileInfoList = fileInfoMapper.selectList(fileInfoQuery);

		// D. 递归拷贝：把每个源节点及其子孙重构到 copyFileList
		List<FileInfo> copyFileList = new ArrayList<>();
		Date curDate = new Date();
		for (FileInfo item : shareFileInfoList) {
			// D1. 如果目标目录已有同名，先改个名
			FileInfo existing = fileInfoMap.get(item.getFileName());
			if (existing != null) {
				item.setFileName(StringTools.rename(item.getFileName()));
			}
			// D2. 递归拷贝到 copyFileList
			findAllSubFolderFileList(
					copyFileList,
					item,
					shareUserId,
					currentUserId,
					curDate,
					myFolderId   // 顶层父目录
			);
		}

		// E. 校验并更新目标用户的空间配额
		UserInfo userInfo = userInfoMapper.selectByUserId(currentUserId);
		long fileSizeSum = 0L;
		for (FileInfo item : copyFileList) {
			if (item.getFolderType().equals(FileFolderTypeEnum.FILE.getType())) {
				fileSizeSum += item.getFileSize();
			}
		}
		if (fileSizeSum + userInfo.getUseSpace() > userInfo.getTotalSpace()) {
			throw new BusinessException(ResponseCodeEnum.CODE_904); // 超出配额
		}
		UserSpaceDto userSpaceDto = new UserSpaceDto();
		userSpaceDto.setUseSpace(userInfo.getUseSpace() + fileSizeSum);
		userSpaceDto.setTotalSpace(userInfo.getTotalSpace());
		redisComponent.saveUserSpace(currentUserId, userSpaceDto);
		userInfoMapper.updateUserSpace(currentUserId, fileSizeSum, null);

		// F. 批量插入所有拷贝后的记录
		fileInfoMapper.insertBatch(copyFileList);
	}

	private void checkFileName(String filePid,String userId,String fileName,Integer folderType){
		FileInfoQuery fileInfoQuery = new FileInfoQuery();
		fileInfoQuery.setUserId(userId);
		fileInfoQuery.setFilePid(filePid);
		fileInfoQuery.setFolderType(folderType);
		fileInfoQuery.setFileName(fileName);
		fileInfoQuery.setDelFlag(FileDelFlagEnum.USING.getFlag());
		Integer count = fileInfoMapper.selectCount(fileInfoQuery);
		if(count>0){
			throw new BusinessException("此目录下已经存在同名"+(folderType.equals(FileFolderTypeEnum.FILE.getType())?"文件":"文件夹")+"请修改名称");
		}
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
			updateFileInfo.setStatus(transcodeSuccess?FileStatusEnum.USING.getStatus() : FileStatusEnum.TRANSCODING_FAILED.getStatus());
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