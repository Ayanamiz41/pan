package com.easypan.controller;

import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.po.FileShare;
import com.easypan.entity.query.FileShareQuery;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.service.FileShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RequestMapping("/share")
@RestController
public class FileShareController extends ABaseController{
    @Autowired
    private FileShareService fileShareService;

    @RequestMapping("/loadShareList")
    @GlobalInterceptor
    public ResponseVO loadShareList(HttpSession session, Integer pageNo, Integer pageSize) {
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        FileShareQuery fileShareQuery = new FileShareQuery();
        fileShareQuery.setPageNo(pageNo);
        fileShareQuery.setQueryFileName(true);
        fileShareQuery.setPageSize(pageSize);
        fileShareQuery.setUserId(sessionWebUserDto.getUserId());
        PaginationResultVO result = fileShareService.findListByPage(fileShareQuery);
        return getSuccessResponseVO(result);
    }

    @PostMapping("/shareFile")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO shareFile(HttpSession session,
                                 @VerifyParam(required = true) String fileId,
                                 @VerifyParam(required = true)Integer validType,
                                 String code) {
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        FileShare shareFile = new FileShare();
        shareFile.setFileId(fileId);
        shareFile.setValidType(validType);
        shareFile.setCode(code);
        shareFile.setUserId(sessionWebUserDto.getUserId());
        fileShareService.saveShare(shareFile);
        return getSuccessResponseVO(shareFile);
    }

    @PostMapping("/cancelShare")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO cancelShare(HttpSession session,@VerifyParam(required = true)String shareIds){
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        fileShareService.deleteFileShareBatch(shareIds.split(","),sessionWebUserDto.getUserId());
        return getSuccessResponseVO(null);
    }
}
