package com.easypan.mappers;

import org.apache.ibatis.annotations.Param;


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

}