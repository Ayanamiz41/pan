package com.easypan.controller;


import java.util.List;
import com.easypan.service.UserInfoService;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.entity.po.UserInfo;
import com.easypan.entity.query.UserInfoQuery;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.annotation.Resource;

@RestController
@RequestMapping("/userInfo")
public class UserInfoController extends ABaseController{

	@Resource
	private UserInfoService userInfoService;

	@RequestMapping("/loadDataList")
	public ResponseVO loadDataList(UserInfoQuery query) {
		return getSuccessResponseVO(userInfoService.findListByPage(query));
	}

	/**
 	 * 新增
 	 */
	@RequestMapping("/add")
	public ResponseVO add(UserInfo bean) {
		Integer result = this.userInfoService.add(bean);
		return getSuccessResponseVO(null);
	}

	/**
 	 * 批量新增
 	 */
	@RequestMapping("/addBatch")
	public ResponseVO addBatch(@RequestBody List<UserInfo> listBean) {
		this.userInfoService.addBatch(listBean);
		return getSuccessResponseVO(null);
	}

	/**
 	 * 批量新增或修改
 	 */
	@RequestMapping("/addOrUpdateBatch")
	public ResponseVO addOrUpdateBatch(@RequestBody List<UserInfo> listBean) {
		this.userInfoService.addOrUpdateBatch(listBean);
		return getSuccessResponseVO(null);
	}

	/**
 	 * 根据 UserId 查询
 	 */
	@RequestMapping("/getUserInfoByUserId")
	public ResponseVO getUserInfoByUserId(String userId) {
		return getSuccessResponseVO(userInfoService.getUserInfoByUserId(userId));}

	/**
 	 * 根据 UserId 更新
 	 */
	@RequestMapping("/updateUserInfoByUserId")
	public ResponseVO updateUserInfoByUserId(UserInfo bean, String userId) {
		this.userInfoService.updateUserInfoByUserId(bean, userId);
		return getSuccessResponseVO(null);
}

	/**
 	 * 根据 UserId 删除
 	 */
	@RequestMapping("/deleteUserInfoByUserId")
	public ResponseVO deleteUserInfoByUserId(String userId) {
		this.userInfoService.deleteUserInfoByUserId(userId);
		return getSuccessResponseVO(null);
}

	/**
 	 * 根据 Email 查询
 	 */
	@RequestMapping("/getUserInfoByEmail")
	public ResponseVO getUserInfoByEmail(String email) {
		return getSuccessResponseVO(userInfoService.getUserInfoByEmail(email));}

	/**
 	 * 根据 Email 更新
 	 */
	@RequestMapping("/updateUserInfoByEmail")
	public ResponseVO updateUserInfoByEmail(UserInfo bean, String email) {
		this.userInfoService.updateUserInfoByEmail(bean, email);
		return getSuccessResponseVO(null);
}

	/**
 	 * 根据 Email 删除
 	 */
	@RequestMapping("/deleteUserInfoByEmail")
	public ResponseVO deleteUserInfoByEmail(String email) {
		this.userInfoService.deleteUserInfoByEmail(email);
		return getSuccessResponseVO(null);
}

	/**
 	 * 根据 NickName 查询
 	 */
	@RequestMapping("/getUserInfoByNickName")
	public ResponseVO getUserInfoByNickName(String nickName) {
		return getSuccessResponseVO(userInfoService.getUserInfoByNickName(nickName));}

	/**
 	 * 根据 NickName 更新
 	 */
	@RequestMapping("/updateUserInfoByNickName")
	public ResponseVO updateUserInfoByNickName(UserInfo bean, String nickName) {
		this.userInfoService.updateUserInfoByNickName(bean, nickName);
		return getSuccessResponseVO(null);
}

	/**
 	 * 根据 NickName 删除
 	 */
	@RequestMapping("/deleteUserInfoByNickName")
	public ResponseVO deleteUserInfoByNickName(String nickName) {
		this.userInfoService.deleteUserInfoByNickName(nickName);
		return getSuccessResponseVO(null);
}

	/**
 	 * 根据 QqOpenId 查询
 	 */
	@RequestMapping("/getUserInfoByQqOpenId")
	public ResponseVO getUserInfoByQqOpenId(String qqOpenId) {
		return getSuccessResponseVO(userInfoService.getUserInfoByQqOpenId(qqOpenId));}

	/**
 	 * 根据 QqOpenId 更新
 	 */
	@RequestMapping("/updateUserInfoByQqOpenId")
	public ResponseVO updateUserInfoByQqOpenId(UserInfo bean, String qqOpenId) {
		this.userInfoService.updateUserInfoByQqOpenId(bean, qqOpenId);
		return getSuccessResponseVO(null);
}

	/**
 	 * 根据 QqOpenId 删除
 	 */
	@RequestMapping("/deleteUserInfoByQqOpenId")
	public ResponseVO deleteUserInfoByQqOpenId(String qqOpenId) {
		this.userInfoService.deleteUserInfoByQqOpenId(qqOpenId);
		return getSuccessResponseVO(null);
}
}