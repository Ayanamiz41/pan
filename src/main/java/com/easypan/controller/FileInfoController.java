package com.easypan.controller;


import java.util.List;

import com.easypan.annotation.GlobalInterceptor;
import com.easypan.entity.vo.FileInfoVO;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.enums.FileCatogoryEnum;
import com.easypan.enums.FileDelFlagEnum;
import com.easypan.service.FileInfoService;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.query.FileInfoQuery;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;


@RestController
@RequestMapping("/file")
public class FileInfoController extends ABaseController{

	@Resource
	private FileInfoService fileInfoService;

	/**
	 * 根据条件分页查询
	 * @param session
	 * @param query
	 * @return
	 */
	@PostMapping("/loadDataList")
	@GlobalInterceptor
	public ResponseVO loadDataList(HttpSession session, FileInfoQuery query,String category) {

		FileCatogoryEnum catogoryEnum = FileCatogoryEnum.getByCode(category);
		if(catogoryEnum != null){
			query.setFileCategory(catogoryEnum.getCategory());
		}
		query.setUserId(getUserInfoFromSession(session).getUserId());
		query.setOrderBy("last_update_time desc");
		query.setDelFlag(FileDelFlagEnum.USING.getFlag());
		PaginationResultVO result = fileInfoService.findListByPage(query);
		return getSuccessResponseVO(convert2PaginationVO(result, FileInfoVO.class));
	}


}