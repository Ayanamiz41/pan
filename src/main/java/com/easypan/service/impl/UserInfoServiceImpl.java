package com.easypan.service.impl;


import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.easypan.component.RedisComponent;
import com.easypan.entity.config.AppConfig;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.QQInfoDto;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.dto.SysSettingDto;
import com.easypan.entity.dto.UserSpaceDto;
import com.easypan.entity.query.SimplePage;
import com.easypan.enums.PageSize;
import com.easypan.enums.UserStatusEnum;
import com.easypan.exception.BusinessException;
import com.easypan.mappers.FileInfoMapper;
import com.easypan.mappers.UserInfoMapper;
import com.easypan.service.EmailCodeService;
import com.easypan.service.UserInfoService;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.po.UserInfo;
import com.easypan.entity.query.UserInfoQuery;
import com.easypan.utils.JsonUtils;
import com.easypan.utils.OKHttpUtils;
import com.easypan.utils.StringTools;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;

/**
 * @Description: 用户信息 业务接口实现
 * @Author: false
 * @Date: 2025/07/23 12:25:33
 */
@Service("UserInfoMapper")
public class UserInfoServiceImpl implements UserInfoService{

	@Autowired
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;
	@Autowired
	private EmailCodeService  emailCodeService;
	@Autowired
	private RedisComponent  redisComponent;
	@Autowired
	private AppConfig appConfig;
	@Autowired
	private FileInfoMapper fileInfoMapper;

	private static final Logger logger = LoggerFactory.getLogger(UserInfoServiceImpl.class);

	/**
 	 * 根据条件查询列表
 	 */
	@Override
	public List<UserInfo> findListByParam(UserInfoQuery query) {
		return this.userInfoMapper.selectList(query);	}

	/**
 	 * 根据条件查询数量
 	 */
	@Override
	public Integer findCountByParam(UserInfoQuery query) {
		return this.userInfoMapper.selectCount(query);	}

	/**
 	 * 分页查询
 	 */
	@Override
	public PaginationResultVO<UserInfo> findListByPage(UserInfoQuery query) {
		Integer count = this.findCountByParam(query);
		Integer pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
		SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
		query.setSimplePage(page);
		List<UserInfo> list = this.findListByParam(query);
		PaginationResultVO<UserInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
 	 * 新增
 	 */
	@Override
	public Integer add(UserInfo bean) {
		return this.userInfoMapper.insert(bean);
	}

	/**
 	 * 批量新增
 	 */
	@Override
	public Integer addBatch(List<UserInfo> listBean) {
		if ((listBean == null) || listBean.isEmpty()) {
			return 0;
		}
			return this.userInfoMapper.insertBatch(listBean);
	}

	/**
 	 * 批量新增或修改
 	 */
	@Override
	public Integer addOrUpdateBatch(List<UserInfo> listBean) {
		if ((listBean == null) || listBean.isEmpty()) {
			return 0;
		}
			return this.userInfoMapper.insertOrUpdateBatch(listBean);
	}

	/**
 	 * 根据 UserId 查询
 	 */
	@Override
	public UserInfo getUserInfoByUserId(String userId) {
		return this.userInfoMapper.selectByUserId(userId);}

	/**
 	 * 根据 UserId 更新
 	 */
	@Override
	public Integer updateUserInfoByUserId(UserInfo bean, String userId) {
		return this.userInfoMapper.updateByUserId(bean, userId);}

	/**
 	 * 根据 UserId 删除
 	 */
	@Override
	public Integer deleteUserInfoByUserId(String userId) {
		return this.userInfoMapper.deleteByUserId(userId);}

	/**
 	 * 根据 Email 查询
 	 */
	@Override
	public UserInfo getUserInfoByEmail(String email) {
		return this.userInfoMapper.selectByEmail(email);}

	/**
 	 * 根据 Email 更新
 	 */
	@Override
	public Integer updateUserInfoByEmail(UserInfo bean, String email) {
		return this.userInfoMapper.updateByEmail(bean, email);}

	/**
 	 * 根据 Email 删除
 	 */
	@Override
	public Integer deleteUserInfoByEmail(String email) {
		return this.userInfoMapper.deleteByEmail(email);}

	/**
 	 * 根据 NickName 查询
 	 */
	@Override
	public UserInfo getUserInfoByNickName(String nickName) {
		return this.userInfoMapper.selectByNickName(nickName);}

	/**
 	 * 根据 NickName 更新
 	 */
	@Override
	public Integer updateUserInfoByNickName(UserInfo bean, String nickName) {
		return this.userInfoMapper.updateByNickName(bean, nickName);}

	/**
 	 * 根据 NickName 删除
 	 */
	@Override
	public Integer deleteUserInfoByNickName(String nickName) {
		return this.userInfoMapper.deleteByNickName(nickName);}

	/**
 	 * 根据 QqOpenId 查询
 	 */
	@Override
	public UserInfo getUserInfoByQqOpenId(String qqOpenId) {
		return this.userInfoMapper.selectByQqOpenId(qqOpenId);}

	/**
 	 * 根据 QqOpenId 更新
 	 */
	@Override
	public Integer updateUserInfoByQqOpenId(UserInfo bean, String qqOpenId) {
		return this.userInfoMapper.updateByQqOpenId(bean, qqOpenId);}

	/**
 	 * 根据 QqOpenId 删除
 	 */
	@Override
	public Integer deleteUserInfoByQqOpenId(String qqOpenId) {
		return this.userInfoMapper.deleteByQqOpenId(qqOpenId);}

	/**
	 * 注册方法
	 * @param email 用户邮箱
	 * @param nickName 用户昵称
	 * @param password 用户密码（会进行MD5加密）
	 * @param emailCode 邮箱验证码，用于验证邮箱有效性
	 */
	@Transactional(rollbackFor = Exception.class) // 开启事务，出现异常时自动回滚
	public void register(String email, String nickName, String password, String emailCode) {
		// 查询该邮箱是否已被注册，若存在则抛异常
		UserInfo userInfo = userInfoMapper.selectByEmail(email);
		if(userInfo != null){
			throw new BusinessException("邮箱账号已存在");
		}

		// 查询该昵称是否已被占用，若存在则抛异常
		UserInfo nickNameUser =  userInfoMapper.selectByNickName(nickName);
		if(nickNameUser != null){
			throw new BusinessException("昵称已存在");
		}

		// 校验邮箱验证码，验证邮箱所有权和验证码有效性
		emailCodeService.checkCode(email, emailCode);

		// 生成长度为10的随机用户ID
		String userId = StringTools.getRandomNumber(Constants.LENGTH_10);

		// 创建新的用户实体并赋值
		userInfo = new UserInfo();
		userInfo.setUserId(userId);
		userInfo.setEmail(email);
		userInfo.setNickName(nickName);
		// 密码经过MD5加密后保存，避免明文存储
		userInfo.setPassword(StringTools.encodeByMd5(password));
		userInfo.setJoinTime(new Date());               // 记录注册时间
		userInfo.setStatus(UserStatusEnum.ENABLE.getStatus()); // 默认启用状态
		userInfo.setUseSpace(0L);                        // 初始已用空间为0

		// 从 Redis 获取系统配置，设置用户初始可用总空间（单位转换为字节）
		SysSettingDto sysSettingDto = redisComponent.getSysSettingDto();
		userInfo.setTotalSpace(sysSettingDto.getUserInitTotalSpace() * Constants.MB);

		// 插入新用户数据到数据库
		userInfoMapper.insert(userInfo);
	}


	/**
	 * 登录方法
	 * @param email 用户邮箱
	 * @param password 用户密码（这里是明文比对，实际项目建议密码加密比对）
	 * @return 登录成功后封装的用户会话信息 SessionWebUserDto
	 */
	public SessionWebUserDto login(String email, String password) {
		// 根据邮箱查询用户信息
		UserInfo userInfo = userInfoMapper.selectByEmail(email);

		// 如果用户不存在或者密码不匹配，抛出业务异常
		if(userInfo == null || !userInfo.getPassword().equals(password)){
			throw new BusinessException("账号或者密码错误");
		}

		// 判断用户状态是否被禁用，如果是则抛出异常
		if(userInfo.getStatus().equals(UserStatusEnum.DISABLE.getStatus())){
			throw new BusinessException("账号已被禁用");
		}

		// 更新用户的最后登录时间
		UserInfo updateInfo = new UserInfo();
		updateInfo.setLastLoginTime(new Date());
		userInfoMapper.updateByUserId(updateInfo, userInfo.getUserId());

		// 创建会话用户对象，封装用户基本信息
		SessionWebUserDto sessionWebUserDto = new SessionWebUserDto();
		sessionWebUserDto.setNickName(userInfo.getNickName());
		sessionWebUserDto.setUserId(userInfo.getUserId());

		// 判断该邮箱是否为管理员邮箱，设置管理员标识
		if(ArrayUtils.contains(appConfig.getAdminEmails().split(","), email)){
			sessionWebUserDto.setIsAdmin(true);
		} else {
			sessionWebUserDto.setIsAdmin(false);
		}

		// 创建用户空间信息对象，并设置总空间和已用空间
		UserSpaceDto userSpaceDto = new UserSpaceDto();
		userSpaceDto.setTotalSpace(userInfo.getTotalSpace());
		Long useSpace = fileInfoMapper.selectUseSpaceByUserId(userInfo.getUserId());
		userSpaceDto.setUseSpace(useSpace);

		// 将用户空间信息保存到 Redis 中（用于快速读取和缓存）
		redisComponent.saveUserSpace(userInfo.getUserId(), userSpaceDto);

		// 返回封装好的会话用户信息
		return sessionWebUserDto;
	}


	/**
	 * 重置密码
	 * @param email
	 * @param password
	 * @param emailCode
	 */
	@Transactional(rollbackFor = Exception.class)
	public void resetPwd(String email, String password, String emailCode) {
		// 1. 查询账号
		UserInfo userInfo = userInfoMapper.selectByEmail(email);
		if(userInfo == null){
			throw new BusinessException("账号不存在");
		}

		// 2. 校验验证码
		emailCodeService.checkCode(email, emailCode);

		// 3. 修改密码
		UserInfo updateInfo = new UserInfo();
		updateInfo.setPassword(StringTools.encodeByMd5(password));
		userInfoMapper.updateByEmail(updateInfo, email);
	}

	/**
	 * qq登录
	 * @param code
	 * @return
	 */
	public SessionWebUserDto qqLogin(String code) {

		String accessToken = getQQAccessToken(code);
		String openId = getQQOpenId(accessToken);
		UserInfo user = userInfoMapper.selectByQqOpenId(openId);
		String avatar =null;
		if(user == null){
			QQInfoDto qqInfoDto = getQQUserInfo(accessToken,openId);
			user = new UserInfo();
			String nickName = qqInfoDto.getNickname();
			nickName  = nickName.length()>Constants.LENGTH_20?nickName.substring(0,Constants.LENGTH_20):nickName;
			avatar = StringTools.isEmpty(qqInfoDto.getFigureurl_qq_2())?qqInfoDto.getFigureurl_qq_1():qqInfoDto.getFigureurl_qq_2();
			Date curDate = new Date();
			user.setQqOpenId(openId);
			user.setJoinTime(curDate);
			user.setNickName(nickName);
			user.setQqAvatar(avatar);
			user.setStatus(UserStatusEnum.ENABLE.getStatus());
			user.setUserId(StringTools.getRandomNumber(Constants.LENGTH_10));
			user.setLastLoginTime(curDate);
			user.setUseSpace(0L);
			user.setTotalSpace(redisComponent.getSysSettingDto().getUserInitTotalSpace() * Constants.MB);
			userInfoMapper.insert(user);
		}else{
			UserInfo updateUserInfo =  new UserInfo();
			updateUserInfo.setLastLoginTime(new Date());
			userInfoMapper.updateByQqOpenId(updateUserInfo, openId);
		}
		SessionWebUserDto sessionWebUserDto = new SessionWebUserDto();
		sessionWebUserDto.setNickName(user.getNickName());
		sessionWebUserDto.setUserId(user.getUserId());
		sessionWebUserDto.setAvatar(avatar);
		if(ArrayUtils.contains(appConfig.getAdminEmails().split(","),user.getEmail()==null?"":user.getEmail())){
			sessionWebUserDto.setIsAdmin(true);
		} else {
			sessionWebUserDto.setIsAdmin(false);
		}
		UserSpaceDto userSpaceDto = new UserSpaceDto();
		userSpaceDto.setTotalSpace(user.getTotalSpace());
		Long useSpace = fileInfoMapper.selectUseSpaceByUserId(user.getUserId());
		userSpaceDto.setUseSpace(useSpace);
		redisComponent.saveUserSpace(user.getUserId(), userSpaceDto);
		return sessionWebUserDto;
	}

	private String getQQAccessToken(String code){
		String accessToken = null;
		String url = null;
		try{
			url = String.format(appConfig.getQqUrlAccessToken(),appConfig.getQqAppId(),appConfig.getQqAppKey(),code, URLEncoder.encode(appConfig.getQqUrlRedirect(),"utf-8"));
		} catch (UnsupportedEncodingException e) {
			logger.error("encode失败",e);
		}
		String tokenResult = OKHttpUtils.getRequest(url);
		if(tokenResult==null||tokenResult.indexOf(Constants.VIEW_OBJ_RESULT_KEY)!=-1){
			logger.error("获取qqToken失败:{}",tokenResult);
			throw new BusinessException("获取qqToken失败");
		}
		String[] params = tokenResult.split("&");
		if(params!=null&&params.length>0){
			for(String param:params){
				if(param.indexOf("access_token")!=-1){
					accessToken = param.split("=")[1];
					break;
				}
			}
		}
		return accessToken;
	}

	private String getQQOpenId(String accessToken)throws BusinessException{
		String url = String.format(appConfig.getQqUrlOpenId(),accessToken);
		String openIdResult = OKHttpUtils.getRequest(url);
		String tmpJson = getQQResp(openIdResult);
		if(tmpJson==null){
			logger.error("调用qq接口获取openId失败:tmpJson={}",tmpJson);
			throw new BusinessException("调用qq接口获取openId失败");
		}
		Map jsonData = JsonUtils.convertJson2Obj(tmpJson, Map.class);
		if(jsonData==null||jsonData.containsKey(Constants.VIEW_OBJ_RESULT_KEY)){
			logger.error("调用qq接口获取openId失败:{}",jsonData);
			throw new BusinessException("调用qq接口获取openId失败");
		}
		return String.valueOf(jsonData.get("openid"));
	}

	private String getQQResp(String result){
		if(StringUtils.isNotBlank(result)){
			int pos = result.indexOf("callback");
			if(pos!=-1){
				int start = result.indexOf("(");
				int end = result.indexOf(")");
				String jsonStr = result.substring(start+1,end-1);
				return jsonStr;
			}
		}
		return null;
	}

	private QQInfoDto getQQUserInfo(String accessToken,String qqOpenId)throws BusinessException{
		String url = String.format(appConfig.getQqUrlUserInfo(), accessToken,appConfig.getQqAppId(),qqOpenId);
		String response = OKHttpUtils.getRequest(url);
		if(StringUtils.isNotBlank(response)){
			QQInfoDto qqInfoDto = JsonUtils.convertJson2Obj(response, QQInfoDto.class);
			if(qqInfoDto.getRet()!=0){
				logger.error("qqInfo:{}",response);
				throw new BusinessException("调用qq接口获取用户信息异常");
			}
			return qqInfoDto;
		}
		throw new BusinessException("调用qq接口获取用户信息异常");
	}
}