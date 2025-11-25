package com.easypan.service.impl;


import java.util.List;
import com.easypan.entity.query.SimplePage;
import com.easypan.enums.PageSize;
import com.easypan.mappers.FileInfoMapper;
import com.easypan.service.FileInfoService;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.query.FileInfoQuery;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

@Service("FileInfoMapper")
public class FileInfoServiceImpl implements FileInfoService{

	@Resource
	private FileInfoMapper<FileInfo, FileInfoQuery> fileInfoMapper;

	/**
 	 * 根据条件查询列表
 	 */
	@Override
	public List<FileInfo> findListByParam(FileInfoQuery query) {
		return this.fileInfoMapper.selectList(query);	}

	/**
 	 * 根据条件查询数量
 	 */
	@Override
	public Integer findCountByParam(FileInfoQuery query) {
		return this.fileInfoMapper.selectCount(query);	}

	/**
 	 * 分页查询
 	 */
	@Override
	public PaginationResultVO<FileInfo> findListByPage(FileInfoQuery query) {
		Integer count = this.findCountByParam(query);
		Integer pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
		SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
		query.setSimplePage(page);
		List<FileInfo> list = this.findListByParam(query);
		PaginationResultVO<FileInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
 	 * 新增
 	 */
	@Override
	public Integer add(FileInfo bean) {
		return this.fileInfoMapper.insert(bean);
	}

	/**
 	 * 批量新增
 	 */
	@Override
	public Integer addBatch(List<FileInfo> listBean) {
		if ((listBean == null) || listBean.isEmpty()) {
			return 0;
		}
			return this.fileInfoMapper.insertBatch(listBean);
	}

	/**
 	 * 批量新增或修改
 	 */
	@Override
	public Integer addOrUpdateBatch(List<FileInfo> listBean) {
		if ((listBean == null) || listBean.isEmpty()) {
			return 0;
		}
			return this.fileInfoMapper.insertOrUpdateBatch(listBean);
	}

	/**
 	 * 根据 FileIdAndUserId 查询
 	 */
	@Override
	public FileInfo getFileInfoByFileIdAndUserId(String fileId, String userId) {
		return this.fileInfoMapper.selectByFileIdAndUserId(fileId, userId);}

	/**
 	 * 根据 FileIdAndUserId 更新
 	 */
	@Override
	public Integer updateFileInfoByFileIdAndUserId(FileInfo bean, String fileId, String userId) {
		return this.fileInfoMapper.updateByFileIdAndUserId(bean, fileId, userId);}

	/**
 	 * 根据 FileIdAndUserId 删除
 	 */
	@Override
	public Integer deleteFileInfoByFileIdAndUserId(String fileId, String userId) {
		return this.fileInfoMapper.deleteByFileIdAndUserId(fileId, userId);}
}