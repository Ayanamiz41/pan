package com.easypan.service.impl;


import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.easypan.entity.constants.Constants;
import com.easypan.entity.query.SimplePage;
import com.easypan.enums.PageSize;
import com.easypan.enums.ResponseCodeEnum;
import com.easypan.enums.ShareValidTypeEnum;
import com.easypan.exception.BusinessException;
import com.easypan.mappers.FileShareMapper;
import com.easypan.service.FileShareService;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.po.FileShare;
import com.easypan.entity.query.FileShareQuery;
import com.easypan.utils.DateUtils;
import com.easypan.utils.StringTools;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
/**
 * @Description: 文件分享 业务接口实现
 * @Author: false
 * @Date: 2025/07/29 20:41:18
 */
@Service
public class FileShareServiceImpl implements FileShareService{

	@Resource
	private FileShareMapper<FileShare, FileShareQuery> fileShareMapper;

	/**
 	 * 根据条件查询列表
 	 */
	@Override
	public List<FileShare> findListByParam(FileShareQuery query) {
		return this.fileShareMapper.selectList(query);	}

	/**
 	 * 根据条件查询数量
 	 */
	@Override
	public Integer findCountByParam(FileShareQuery query) {
		return this.fileShareMapper.selectCount(query);	}

	/**
 	 * 分页查询
 	 */
	@Override
	public PaginationResultVO<FileShare> findListByPage(FileShareQuery query) {
		Integer count = this.findCountByParam(query);
		Integer pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
		SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
		query.setSimplePage(page);
		List<FileShare> list = this.findListByParam(query);
		PaginationResultVO<FileShare> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
 	 * 新增
 	 */
	@Override
	public Integer add(FileShare bean) {
		return this.fileShareMapper.insert(bean);
	}

	/**
 	 * 批量新增
 	 */
	@Override
	public Integer addBatch(List<FileShare> listBean) {
		if ((listBean == null) || listBean.isEmpty()) {
			return 0;
		}
			return this.fileShareMapper.insertBatch(listBean);
	}

	/**
 	 * 批量新增或修改
 	 */
	@Override
	public Integer addOrUpdateBatch(List<FileShare> listBean) {
		if ((listBean == null) || listBean.isEmpty()) {
			return 0;
		}
			return this.fileShareMapper.insertOrUpdateBatch(listBean);
	}

	/**
 	 * 根据 ShareId 查询
 	 */
	@Override
	public FileShare getFileShareByShareId(String shareId) {
		return this.fileShareMapper.selectByShareId(shareId);}

	/**
 	 * 根据 ShareId 更新
 	 */
	@Override
	public Integer updateFileShareByShareId(FileShare bean, String shareId) {
		return this.fileShareMapper.updateByShareId(bean, shareId);}

	/**
 	 * 根据 ShareId 删除
 	 */
	@Override
	public Integer deleteFileShareByShareId(String shareId) {
		return this.fileShareMapper.deleteByShareId(shareId);}

	/**
	 * 保存分享
	 * @param fileShare
	 */
	public void saveShare(FileShare fileShare) {
		ShareValidTypeEnum shareValidTypeEnum = ShareValidTypeEnum.getByType(fileShare.getValidType());
		if (shareValidTypeEnum == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		if(shareValidTypeEnum!=ShareValidTypeEnum.PERMANENT){
			fileShare.setExpireTime(DateUtils.getDateAfterDays(shareValidTypeEnum.getDays()));
		}
		fileShare.setShareTime(new Date());
		if(StringTools.isEmpty(fileShare.getCode())){
			fileShare.setCode(StringTools.getRandomString(Constants.LENGTH_5));
		}
		fileShare.setShowCount(Constants.ZERO);
		fileShare.setShareId(StringTools.getRandomString(Constants.LENGTH_20));
		fileShareMapper.insert(fileShare);
	}

	/**
	 * 批量删除分享
	 * @param shareIdArray
	 * @param userId
	 */
	@Transactional(rollbackFor = Exception.class)
	public void deleteFileShareBatch(String[] shareIdArray, String userId) {
		List<String> shareIdList = Arrays.asList(shareIdArray);
		Integer count = fileShareMapper.deleteFileShareBatch(shareIdList, userId);
		if(count!=shareIdArray.length){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
	}
}