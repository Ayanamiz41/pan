package com.easypan.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * @Description: 邮箱验证码 Mapper
 * @Author: false
 * @Date: 2025/07/23 16:16:27
 */
public interface EmailCodeMapper<T, P> extends BaseMapper {

	/**
 	 * 根据 EmailAndCode 查询
 	 */
	T selectByEmailAndCode(@Param("email")String email, @Param("code")String code);

	/**
 	 * 根据 EmailAndCode 更新
 	 */
	Integer updateByEmailAndCode(@Param("bean") T t, @Param("email")String email, @Param("code")String code); 

	/**
 	 * 根据 EmailAndCode 删除
 	 */
	Integer deleteByEmailAndCode(@Param("email")String email, @Param("code")String code);

	/**
	 * 停用此email的验证码
	 * @param email
	 */
	void disableEmailCode(@Param("email") String email);
}