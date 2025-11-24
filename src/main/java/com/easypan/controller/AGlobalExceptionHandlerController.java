package com.easypan.controller;

import com.easypan.entity.vo.ResponseVO;;
import com.easypan.enums.ResponseCodeEnum;;
import com.easypan.exception.BusinessException;;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;

import java.net.BindException;

/**
 * @Description: 全局异常处理
 * @Author: KunSpireUp
 * @Date: 3/27/2024 12:52 AM
 */
@RestControllerAdvice  // 标记为全局异常处理类，自动捕获控制器层抛出的异常，并返回 JSON 格式响应
public class AGlobalExceptionHandlerController extends ABaseController{

	// 日志对象，用于记录异常日志
	private static final Logger logger = LoggerFactory.getLogger(AGlobalExceptionHandlerController.class);

	/**
	 * 统一异常处理方法，捕获所有 Exception 及其子类异常
	 * @param e 捕获到的异常对象
	 * @param request 当前 HTTP 请求，便于获取请求相关信息（如 URL）
	 * @return 封装后的统一响应对象 ResponseVO，返回给前端
	 */
	@ExceptionHandler(value = Exception.class)
	Object handelException(Exception e, HttpServletRequest request) {
		// 记录异常日志，打印请求地址及完整异常堆栈信息
		logger.error("请求错误，请求地址{},错误信息:", request.getRequestURL(), e);

		// 新建响应对象，用于封装错误响应数据
		ResponseVO ajaxResponse = new ResponseVO();

		// 针对不同异常类型设置不同的状态码、错误信息和状态
		if (e instanceof NoHandlerFoundException) {
			// 404异常：请求路径不存在
			ajaxResponse.setCode(ResponseCodeEnum.CODE_404.getCode());
			ajaxResponse.setInfo(ResponseCodeEnum.CODE_404.getMsg());
			ajaxResponse.setStatus(STATUS_ERROR);

		} else if (e instanceof BusinessException) {
			// 业务异常：程序主动抛出的业务错误
			BusinessException businessException = (BusinessException) e;
			ajaxResponse.setCode(businessException.getCode());     // 使用业务异常自带的错误码
			ajaxResponse.setInfo(businessException.getMessage());  // 使用业务异常自带的错误信息
			ajaxResponse.setStatus(STATUS_ERROR);

		} else if (e instanceof BindException) {
			// 参数绑定异常：参数类型不匹配或绑定失败
			ajaxResponse.setCode(ResponseCodeEnum.CODE_600.getCode());
			ajaxResponse.setInfo(ResponseCodeEnum.CODE_600.getMsg());
			ajaxResponse.setStatus(STATUS_ERROR);

		} else if (e instanceof DuplicateKeyException) {
			// 数据库主键冲突异常（唯一索引重复）
			ajaxResponse.setCode(ResponseCodeEnum.CODE_601.getCode());
			ajaxResponse.setInfo(ResponseCodeEnum.CODE_601.getMsg());
			ajaxResponse.setStatus(STATUS_ERROR);

		} else {
			// 其他未知异常，统一返回 500 系统内部错误
			ajaxResponse.setCode(ResponseCodeEnum.CODE_500.getCode());
			ajaxResponse.setInfo(ResponseCodeEnum.CODE_500.getMsg());
			ajaxResponse.setStatus(STATUS_ERROR);
		}

		// 返回封装好的统一错误响应
		return ajaxResponse;

	}
}
