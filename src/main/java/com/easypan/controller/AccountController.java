package com.easypan.controller;


import java.io.IOException;

import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.enums.VerifyRegexEnum;
import com.easypan.utils.CreateImageCode;
import com.easypan.exception.BusinessException;
import com.easypan.service.EmailCodeService;
import com.easypan.service.UserInfoService;
import com.easypan.entity.vo.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @Description: 用户信息 Controller
 * @Author: false
 * @Date: 2025/07/23 12:25:33
 */
@RestController
public class AccountController extends ABaseController{

	@Autowired
	private UserInfoService userInfoService;
	@Autowired
	private EmailCodeService emailCodeService;

	@GetMapping("/checkCode")
	public void checkCode(HttpServletResponse response, HttpSession session, Integer type) throws IOException {
		// 创建验证码对象，指定宽度、高度、验证码字符数量、干扰线数量
		CreateImageCode vCode = new CreateImageCode(130, 38, 5, 10);

		// 设置响应头，禁止浏览器缓存验证码图片，确保每次请求都能刷新图片
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "No-cache");
		response.setDateHeader("Expires", 0);

		// 设置响应内容类型为图片格式 jpeg
		response.setContentType("image/jpeg");

		// 获取生成的验证码字符串
		String code = vCode.getCode();

		// 根据 type 类型，将验证码保存到不同的 session key 中
		if (type == null || type == 0) {
			// 默认验证码，通常用于登录、普通校验
			session.setAttribute(Constants.CHECK_CODE_KEY, code);
		} else {
			// 其他类型验证码，比如邮箱验证码
			session.setAttribute(Constants.CHECK_CODE_KEY_EMAIL, code);
		}

		// 将验证码图片写入响应输出流，发送给客户端
		vCode.write(response.getOutputStream());
	}

	@PostMapping("/sendEmailCode")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO sendEmailCode(HttpSession session, @VerifyParam(required = true,regex = VerifyRegexEnum.EMAIL,max=150) String email,
									@VerifyParam(required = true) String checkCode,
									@VerifyParam(required = true) Integer type) {
		try {
			// 从 session 中获取之前保存的图片验证码，并忽略大小写比对
			if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY_EMAIL))) {
				// 验证码不匹配，抛出业务异常
				throw new BusinessException("图片验证码不正确");
			}

			// 调用业务服务发送邮箱验证码
			emailCodeService.sendEmailCode(email, type);

			// 返回成功响应（无数据）
			return getSuccessResponseVO(null);
		} finally {
			// 无论成功失败，都清理 session 中的图片验证码，防止重复使用
			session.removeAttribute(Constants.CHECK_CODE_KEY_EMAIL);
		}
	}

	@PostMapping("/register")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO register(HttpSession session, @VerifyParam(required = true,regex = VerifyRegexEnum.EMAIL,max=150) String email,
									@VerifyParam(required = true) String nickName,
									@VerifyParam(required = true,regex = VerifyRegexEnum.PASSWORD,min = 8,max = 18) String password,
									@VerifyParam(required = true) String checkCode,
									@VerifyParam(required = true) String emailCode) {
		try {
			// 从 session 中获取之前保存的图片验证码，并忽略大小写比对
			if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {
				// 验证码不匹配，抛出业务异常
				throw new BusinessException("图片验证码不正确");
			}

			userInfoService.register(email,nickName,password,emailCode);

			// 返回成功响应（无数据）
			return getSuccessResponseVO(null);
		} finally {
			// 无论成功失败，都清理 session 中的图片验证码，防止重复使用
			session.removeAttribute(Constants.CHECK_CODE_KEY);
		}
	}

	@PostMapping("/login")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO login(HttpSession session, @VerifyParam(required = true) String email,
									@VerifyParam(required = true) String password,
									@VerifyParam(required = true) String checkCode) {
		try {
			// 从 session 中获取之前保存的图片验证码，并忽略大小写比对
			if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {
				// 验证码不匹配，抛出业务异常
				throw new BusinessException("图片验证码不正确");
			}

			SessionWebUserDto sessionWebUserDto = userInfoService.login(email,password);
			session.setAttribute(Constants.SESSION_KEY, sessionWebUserDto);
			// 返回成功响应
			return getSuccessResponseVO(sessionWebUserDto);
		} finally {
			// 无论成功失败，都清理 session 中的图片验证码，防止重复使用
			session.removeAttribute(Constants.CHECK_CODE_KEY);
		}
	}
}