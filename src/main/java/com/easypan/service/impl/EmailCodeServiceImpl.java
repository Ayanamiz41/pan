package com.easypan.service.impl;


import java.util.Date;
import java.util.List;

import com.easypan.entity.constants.Constants;
import com.easypan.entity.po.UserInfo;
import com.easypan.entity.query.SimplePage;
import com.easypan.entity.query.UserInfoQuery;
import com.easypan.enums.PageSize;
import com.easypan.exception.BusinessException;
import com.easypan.mappers.EmailCodeMapper;
import com.easypan.mappers.UserInfoMapper;
import com.easypan.service.EmailCodeService;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.po.EmailCode;
import com.easypan.entity.query.EmailCodeQuery;
import com.easypan.utils.StringTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
/**
 * @Description: 邮箱验证码 业务接口实现
 * @Author: false
 * @Date: 2025/07/23 16:16:27
 */
@Service("EmailCodeMapper")
public class EmailCodeServiceImpl implements EmailCodeService{

	@Resource
	private EmailCodeMapper<EmailCode, EmailCodeQuery> emailCodeMapper;
	@Autowired
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

	/**
 	 * 根据条件查询列表
 	 */
	@Override
	public List<EmailCode> findListByParam(EmailCodeQuery query) {
		return this.emailCodeMapper.selectList(query);	}

	/**
 	 * 根据条件查询数量
 	 */
	@Override
	public Integer findCountByParam(EmailCodeQuery query) {
		return this.emailCodeMapper.selectCount(query);	}

	/**
 	 * 分页查询
 	 */
	@Override
	public PaginationResultVO<EmailCode> findListByPage(EmailCodeQuery query) {
		Integer count = this.findCountByParam(query);
		Integer pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
		SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
		query.setSimplePage(page);
		List<EmailCode> list = this.findListByParam(query);
		PaginationResultVO<EmailCode> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
 	 * 新增
 	 */
	@Override
	public Integer add(EmailCode bean) {
		return this.emailCodeMapper.insert(bean);
	}

	/**
 	 * 批量新增
 	 */
	@Override
	public Integer addBatch(List<EmailCode> listBean) {
		if ((listBean == null) || listBean.isEmpty()) {
			return 0;
		}
			return this.emailCodeMapper.insertBatch(listBean);
	}

	/**
 	 * 批量新增或修改
 	 */
	@Override
	public Integer addOrUpdateBatch(List<EmailCode> listBean) {
		if ((listBean == null) || listBean.isEmpty()) {
			return 0;
		}
			return this.emailCodeMapper.insertOrUpdateBatch(listBean);
	}

	/**
 	 * 根据 EmailAndCode 查询
 	 */
	@Override
	public EmailCode getEmailCodeByEmailAndCode(String email, String code) {
		return this.emailCodeMapper.selectByEmailAndCode(email, code);}

	/**
 	 * 根据 EmailAndCode 更新
 	 */
	@Override
	public Integer updateEmailCodeByEmailAndCode(EmailCode bean, String email, String code) {
		return this.emailCodeMapper.updateByEmailAndCode(bean, email, code);}

	/**
 	 * 根据 EmailAndCode 删除
 	 */
	@Override
	public Integer deleteEmailCodeByEmailAndCode(String email, String code) {
		return this.emailCodeMapper.deleteByEmailAndCode(email, code);}

	/**
	 * 发送邮箱验证码
	 * @param email
	 * @param type
	 */
	@Transactional(rollbackFor = Exception.class)
	public void sendEmailCode(String email, Integer type) {
		//判断是否是注册
		if(type==Constants.ZERO){
			UserInfo userInfo = userInfoMapper.selectByEmail(email);
			if(userInfo!=null){
				//如果是注册且已经存在邮箱，则抛出异常
				throw new BusinessException("邮箱已经存在");
			}
		}
		//生成随机数串
		String code = StringTools.getRandomNumber(Constants.LENGTH_5);
		//TODO 发送验证码
		//停用此邮箱以前的验证码
		emailCodeMapper.disableEmailCode(email);
		//插入新验证码
		EmailCode emailCode = new EmailCode();
		emailCode.setEmail(email);
		emailCode.setCode(code);
		emailCode.setStatus(Constants.ZERO);
		emailCode.setCreateTime(new Date());
		emailCodeMapper.insert(emailCode);
	}
}