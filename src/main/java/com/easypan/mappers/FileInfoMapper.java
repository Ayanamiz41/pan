package com.easypan.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * @Description: 文件信息 Mapper
 * @Author: false
 * @Date: 2025/07/25 20:22:51
 */
public interface FileInfoMapper<T, P> extends BaseMapper {

	/**
 	 * 根据 FileIdAndUserId 查询
 	 */
	T selectByFileIdAndUserId(@Param("fileId")String fileId, @Param("userId")String userId);

	/**
 	 * 根据 FileIdAndUserId 更新
 	 */
	Integer updateByFileIdAndUserId(@Param("bean") T t, @Param("fileId")String fileId, @Param("userId")String userId); 

	/**
 	 * 根据 FileIdAndUserId 删除
 	 */
	Integer deleteByFileIdAndUserId(@Param("fileId")String fileId, @Param("userId")String userId);

	/**
	 * 根据UserId查已使用总量
	 * @param userId
	 * @return
	 */
	Long selectUseSpaceByUserId(@Param("userId")String userId);

}