package com.easypan.mappers;

import com.easypan.entity.po.FileInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description: 文件信息 Mapper
 * @Author: false
 * @Date: 2025/07/25 20:22:51
 */
public interface FileInfoMapper<T, P> extends BaseMapper {

	/**
	 * 根据 FileIdAndUserId 查询
	 */
	T selectByFileIdAndUserId(@Param("fileId") String fileId, @Param("userId") String userId);

	/**
	 * 根据 FileIdAndUserId 更新
	 */
	Integer updateByFileIdAndUserId(@Param("bean") T t, @Param("fileId") String fileId, @Param("userId") String userId);

	/**
	 * 根据 FileIdAndUserId 删除
	 */
	Integer deleteByFileIdAndUserId(@Param("fileId") String fileId, @Param("userId") String userId);

	/**
	 * 根据UserId查已使用总量（查文件表而非用户表）
	 * @param userId
	 * @return
	 */
	Long selectUseSpaceByUserId(@Param("userId") String userId);

	/**
	 * 根据旧状态更新文件
	 *
	 * @param fileId
	 * @param userId
	 * @param t
	 * @param oldStatus
	 */
	void updateWithOldStatus(@Param("fileId") String fileId, @Param("userId") String userId, @Param("bean") T t, @Param("oldStatus") Integer oldStatus);

	/**
	 * 根据旧delFlag批量更新文件delFlag
	 *
	 * @param fileInfo
	 * @param userId
	 * @param filePidList
	 * @param fileIdList
	 * @param oldDelFlag
	 */
	void updateFileDelFlagBatch(@Param("bean") FileInfo fileInfo,
								@Param("userId") String userId,
								@Param("filePidList") List<String> filePidList,
								@Param("fileIdList") List<String> fileIdList,
								@Param("oldDelFlag") Integer oldDelFlag);

	/**
	 * 根据旧delFlag批量删除文件
	 * @param userId
	 * @param filePidList
	 * @param fileIdList
	 * @param oldDelFlag
	 */
	Integer deleteFileBatchWithOldDelFlag(@Param("userId") String userId,
									   @Param("filePidList") List<String> filePidList,
									   @Param("fileIdList") List<String> fileIdList,
									   @Param("oldDelFlag") Integer oldDelFlag);
}