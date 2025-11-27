package com.easypan.controller;

import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.SessionShareDto;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.entity.vo.FileInfoVO;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.entity.vo.ShareInfoVO;
import com.easypan.enums.FileDelFlagEnum;
import com.easypan.enums.ResponseCodeEnum;
import com.easypan.exception.BusinessException;
import com.easypan.service.ExternalShareService;
import com.easypan.service.FileInfoService;
import com.easypan.utils.StringTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;

@RestController
@RequestMapping("/showShare")
public class ExternalShareController extends CommonFileController{

    @Autowired
    private ExternalShareService externalShareService;
    @Autowired
    private FileInfoService fileInfoService;

    @RequestMapping("/getShareInfo")
    @GlobalInterceptor(checkLogin = false,checkParams = true)
    public ResponseVO getShareInfo(@VerifyParam(required = true)String shareId) {
        return getSuccessResponseVO(externalShareService.getShareInfoCommon(shareId));
    }

    @RequestMapping("/getShareLoginInfo")
    @GlobalInterceptor(checkLogin = false,checkParams = true)
    public ResponseVO getShareLoginInfo(HttpSession session,@VerifyParam(required = true)String shareId){
        SessionShareDto sessionShareDto = getSessionShareFromSession(session,shareId);
        if(sessionShareDto == null){
            return getSuccessResponseVO(null);
        }
        ShareInfoVO shareInfoVO = externalShareService.getShareInfoCommon(shareId);
        //判断是否是当前登录用户分享的文件
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        shareInfoVO.setCurrentUser(sessionWebUserDto != null && sessionWebUserDto.getUserId().equals(sessionShareDto.getShareUserId()));
        return  getSuccessResponseVO(shareInfoVO);
    }

    @RequestMapping("/checkShareCode")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public ResponseVO checkShareCode(HttpSession session,
                                     @VerifyParam(required = true)String shareId,
                                     @VerifyParam(required = true)String code){
        SessionShareDto sessionShareDto = externalShareService.checkShareCode(shareId,code);
        session.setAttribute(Constants.SESSION_SHARE_KEY+shareId,sessionShareDto);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadFileList")
    @GlobalInterceptor(checkLogin = false, checkParams = true)
    public ResponseVO loadFileList(HttpSession session,
                                   @VerifyParam(required = true) String shareId,
                                   @VerifyParam(required = true) String filePid) {
        // 校验并获取当前会话的共享根信息（包含根目录ID和分享者用户ID）
        SessionShareDto sessionShareDto = checkShare(session, shareId);

        // 构造文件查询参数
        FileInfoQuery query = new FileInfoQuery();

        if (!StringTools.isEmpty(filePid) && !Constants.ZERO_STR.equals(filePid)) {
            // 如果前端传入了子目录ID，先校验它是否合法（必须在共享根目录下）
            fileInfoService.checkRootFilePid(
                    sessionShareDto.getFileId(),       // 共享根目录ID
                    sessionShareDto.getShareUserId(),   // 分享者用户ID
                    filePid                             // 当前访问目录ID
            );
            // 校验通过后，将查询的父ID设置为客户端传来的子目录
            query.setFilePid(filePid);
        } else {
            // 未传入或传入根目录标识，直接查询共享根目录下的内容
            query.setFileId(sessionShareDto.getFileId());
        }
        // 设置查询所属用户（分享者）
        query.setUserId(sessionShareDto.getShareUserId());
        // 按更新时间倒序
        query.setOrderBy("last_update_time");
        // 只查询未删除的文件
        query.setDelFlag(FileDelFlagEnum.USING.getFlag());

        // 执行分页查询
        PaginationResultVO result = fileInfoService.findListByPage(query);
        // 转换并返回前端约定的分页VO
        return getSuccessResponseVO(convert2PaginationVO(result, FileInfoVO.class));
    }

    private SessionShareDto checkShare(HttpSession session,String shareId){
        SessionShareDto sessionShareDto = getSessionShareFromSession(session,shareId);
        if(sessionShareDto == null){
            throw new BusinessException(ResponseCodeEnum.CODE_903);
        }
        if(sessionShareDto.getExpireTime()!=null&&new Date().after(sessionShareDto.getExpireTime())){
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        return sessionShareDto;
    }

    @RequestMapping("/getFolderInfo")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public ResponseVO getFolderInfo(HttpSession session,
                                    @VerifyParam(required = true) String shareId,
                                    @VerifyParam(required = true) String path){
        SessionShareDto sessionShareDto = checkShare(session, shareId);
        return super.getFolderInfo(path,sessionShareDto.getShareUserId());
    }

    @GetMapping("/getFile/{shareId}/{fileId}")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public void getFile(HttpServletResponse response,
                        HttpSession session,
                        @VerifyParam(required = true)@PathVariable("shareId")String shareId,
                        @VerifyParam(required = true)@PathVariable("fileId")String fileId){
        SessionShareDto sessionShareDto = checkShare(session, shareId);
        super.getFile(response, fileId, sessionShareDto.getShareUserId());
    }

    @GetMapping("/ts/getVideoInfo/{shareId}/{fileId}")
    @GlobalInterceptor(checkLogin = false,checkParams = true)
    public void getVideoInfo(HttpServletResponse response,
                             HttpSession session,
                             @VerifyParam(required = true)@PathVariable("shareId")String shareId,
                             @VerifyParam(required = true)@PathVariable("fileId")String fileId){
        SessionShareDto sessionShareDto = checkShare(session, shareId);
        super.getFile(response, fileId, sessionShareDto.getShareUserId());
    }

    @RequestMapping("/createDownloadUrl/{shareId}/{fileId}")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public ResponseVO createDownloadUrl(HttpSession session,
                                        @VerifyParam(required = true)@PathVariable("shareId") String shareId,
                                        @VerifyParam(required = true)@PathVariable("fileId") String fileId){
        SessionShareDto sessionShareDto = checkShare(session, shareId);
        return super.createDownloadUrl(fileId,sessionShareDto.getShareUserId());
    }

    @GetMapping("/download/{code}")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public void download(HttpServletRequest request, HttpServletResponse response,
                         @VerifyParam(required = true)@PathVariable String code)throws Exception{
        super.download(request,response,code);
    }

    @PostMapping("/saveShare")
    @GlobalInterceptor(checkLogin = false,checkParams = true)
    public ResponseVO saveShare(HttpSession session,
                          @VerifyParam(required = true)String shareId,
                          @VerifyParam(required = true)String shareFileIds,
                          @VerifyParam(required = true) String myFolderId){
        SessionShareDto sessionShareDto = checkShare(session, shareId);
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        if(sessionShareDto.getShareUserId().equals(sessionWebUserDto.getUserId())){
            throw new BusinessException("自己分享的文件无法保存到自己的网盘");
        }
        fileInfoService.saveShare(sessionShareDto.getFileId(),shareFileIds,myFolderId,sessionShareDto.getShareUserId(),sessionWebUserDto.getUserId());
        return getSuccessResponseVO(null);
    }
}
