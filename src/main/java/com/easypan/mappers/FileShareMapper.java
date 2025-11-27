package com.easypan.mappers;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description: 文件分享 Mapper
 * @Author: false
 * @Date: 2025/07/29 20:41:18
 */
public interface FileShareMapper<T, P> extends BaseMapper {

	/**
 	 * 根据 ShareId 查询
 	 */
	T selectByShareId(@Param("shareId")String shareId);

	/**
 	 * 根据 ShareId 更新
 	 */
	Integer updateByShareId(@Param("bean") T t, @Param("shareId")String shareId); 

	/**
 	 * 根据 ShareId 删除
 	 */
	Integer deleteByShareId(@Param("shareId")String shareId);

	/**
	 * 批量删除分享
	 * @param shareIdList
	 * @param userId
	 * @return
	 */
	Integer deleteFileShareBatch(@Param("shareIdList") List<String> shareIdList,@Param("userId")String userId);

}