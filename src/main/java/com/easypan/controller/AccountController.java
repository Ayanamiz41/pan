package com.easypan.controller;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.entity.config.AppConfig;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.enums.VerifyRegexEnum;
import com.easypan.utils.CreateImageCode;
import com.easypan.exception.BusinessException;
import com.easypan.service.EmailCodeService;
import com.easypan.service.UserInfoService;
import com.easypan.entity.vo.ResponseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

	private static final Logger logger = LoggerFactory.getLogger(AccountController.class);
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String CONTENT_TYPE_VALUE = "application/json;charset=UTF-8";

	@Autowired
	private UserInfoService userInfoService;
	@Autowired
	private EmailCodeService emailCodeService;
    @Autowired
    private AppConfig appConfig;

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

	@PostMapping("/resetPwd")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO resetPwd(HttpSession session, @VerifyParam(required = true,regex = VerifyRegexEnum.EMAIL,max=150) String email,
							   @VerifyParam(required = true,regex = VerifyRegexEnum.PASSWORD,min = 8,max = 18) String password,
							   @VerifyParam(required = true) String checkCode,
							   @VerifyParam(required = true) String emailCode) {
		try {
			// 从 session 中获取之前保存的图片验证码，并忽略大小写比对
			if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {
				// 验证码不匹配，抛出业务异常
				throw new BusinessException("图片验证码不正确");
			}

			userInfoService.resetPwd(email,password,emailCode);
			// 返回成功响应(无数据)
			return getSuccessResponseVO(null);
		} finally {
			// 无论成功失败，都清理 session 中的图片验证码，防止重复使用
			session.removeAttribute(Constants.CHECK_CODE_KEY);
		}
	}

	@GetMapping("/getAvatar/{userId}")
	@GlobalInterceptor(checkParams = true) // 切面拦截，开启参数校验
	public void getAvatar(HttpServletResponse response,
						 @VerifyParam(required = true) // 校验 userId 不为空
						 @PathVariable String userId) {

		// 构造头像目录路径
		String avatarFolderName = Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_AVATAR_NAME;

		// 如果目录不存在，则新建
		File folder = new File(appConfig.getProjectFolder()+avatarFolderName);
		if (!folder.exists()) {
			folder.mkdirs();
		}

		// 构造用户头像路径：例如 /projectRoot/file/avatar/123.jpg
		String avatarPath = appConfig.getProjectFolder() + avatarFolderName + userId + Constants.AVATAR_SUFFIX;
		File file = new File(avatarPath);

		// 判断用户头像是否存在
		if (!file.exists()) {

			// 如果默认头像也不存在，输出提示文字（非图片）
			if (!new File(appConfig.getProjectFolder() + avatarFolderName + Constants.AVATAR_DEFAULT).exists()) {
				printNoDefaultAvatar(response);
				return;
			}

			// 使用默认头像路径
			avatarPath = appConfig.getProjectFolder() + avatarFolderName + Constants.AVATAR_DEFAULT;
		}

		// 设置响应类型为图片 JPG
		response.setContentType("image/jpg");

		// 输出头像（或默认头像）文件内容到 response
		readFile(response, avatarPath);
	}

	private void printNoDefaultAvatar(HttpServletResponse response) {
		response.setHeader(CONTENT_TYPE,CONTENT_TYPE_VALUE);
		response.setStatus(HttpStatus.OK.value());
		PrintWriter writer = null;
		try{
			writer = response.getWriter();
			if (writer != null) {
				writer.print("请在头像目录下放置默认头像default_avatar.jpg");
			}
		}catch (Exception e) {
			logger.error("输出无默认图提示失败");
		}finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
}