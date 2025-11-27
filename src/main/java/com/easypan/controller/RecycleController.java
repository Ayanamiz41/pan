package com.easypan.controller;

import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.entity.vo.FileInfoVO;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.enums.FileDelFlagEnum;
import com.easypan.service.FileInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/recycle")
public class RecycleController extends ABaseController{
    @Autowired
    private FileInfoService fileInfoService;

    @RequestMapping("/loadRecycleList")
    @GlobalInterceptor
    public ResponseVO loadRecycleList(HttpSession session, Integer pageNo, Integer pageSize) {
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        FileInfoQuery  fileInfoQuery = new FileInfoQuery();
        fileInfoQuery.setPageNo(pageNo);
        fileInfoQuery.setPageSize(pageSize);
        fileInfoQuery.setUserId(sessionWebUserDto.getUserId());
        fileInfoQuery.setOrderBy("recycle_time desc");
        fileInfoQuery.setDelFlag(FileDelFlagEnum.RECYCLE.getFlag());
        PaginationResultVO result = fileInfoService.findListByPage(fileInfoQuery);
        return getSuccessResponseVO(convert2PaginationVO(result, FileInfoVO.class));
    }

    @PostMapping("/recoverFile")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO recoverFile(HttpSession session, @VerifyParam(required = true) String fileIds) {
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        fileInfoService.recoverFileBatch(sessionWebUserDto.getUserId(),fileIds);
        return getSuccessResponseVO(null);
    }

    @PostMapping("/delFile")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO delFile(HttpSession session,@VerifyParam(required = true) String fileIds) {
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        fileInfoService.delFileBatch(sessionWebUserDto.getUserId(),fileIds,false);
        return getSuccessResponseVO(null);
    }

}
