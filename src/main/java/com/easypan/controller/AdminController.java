package com.easypan.controller;

import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.component.RedisComponent;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.dto.SysSettingDto;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.entity.query.UserInfoQuery;
import com.easypan.entity.vo.FileInfoVO;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.entity.vo.UserInfoVO;
import com.easypan.service.FileInfoService;
import com.easypan.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequestMapping("/admin")
@RestController
public class AdminController extends CommonFileController{

    @Autowired
    private FileInfoService fileInfoService;
    @Autowired
    private RedisComponent redisComponent;
    @Autowired
    private UserInfoService userInfoService;

    @RequestMapping("/getSysSettings")
    @GlobalInterceptor(checkParams = true,checkAdmin = true)
    public ResponseVO getSysSettings(){
        return getSuccessResponseVO(redisComponent.getSysSettingDto());
    }

    @PostMapping("/saveSysSettings")
    @GlobalInterceptor(checkAdmin = true,checkParams = true)
    public ResponseVO saveSysSettings(@VerifyParam(required = true)String registerEmailTitle,
                                     @VerifyParam(required = true)String registerEmailContent,
                                     @VerifyParam(required = true)Integer userInitUseSpace){
        SysSettingDto sysSettingDto = new SysSettingDto();
        sysSettingDto.setRegisterEmailTitle(registerEmailTitle);
        sysSettingDto.setRegisterEmailContent(registerEmailContent);
        sysSettingDto.setUserInitUseSpace(userInitUseSpace);
        redisComponent.saveSysSettingDto(sysSettingDto);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadUserList")
    @GlobalInterceptor(checkParams = true,checkAdmin = true)
    public ResponseVO loadUserList(UserInfoQuery userInfoQuery){
        userInfoQuery.setOrderBy("join_time desc");
        PaginationResultVO result = userInfoService.findListByPage(userInfoQuery);
        return getSuccessResponseVO(convert2PaginationVO(result, UserInfoVO.class));
    }

    @PostMapping("/updateUserStatus")
    @GlobalInterceptor(checkParams = true,checkAdmin = true)
    public ResponseVO updateUserStatus(@VerifyParam(required = true)String userId,
                                       @VerifyParam(required = true)Integer status){
        userInfoService.updateUserStatus(userId, status);
        return getSuccessResponseVO(null);
    }

    @PostMapping("/updateUserSpace")
    @GlobalInterceptor(checkAdmin = true,checkParams = true)
    public ResponseVO updateUserSpace(@VerifyParam(required = true)String userId,
                                      @VerifyParam(required = true)Long changeSpace){
        userInfoService.updateUserSpace(userId, changeSpace);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadFileList")
    @GlobalInterceptor(checkParams = true,checkAdmin = true)
    public ResponseVO loadFileList(FileInfoQuery fileInfoQuery){
        fileInfoQuery.setOrderBy("last_update_time desc");
        fileInfoQuery.setQueryUserNickName(true);
        PaginationResultVO result = fileInfoService.findListByPage(fileInfoQuery);
        return getSuccessResponseVO(result);
    }

    @RequestMapping("/getFolderInfo")
    @GlobalInterceptor(checkAdmin = true,checkParams = true)
    public ResponseVO getFolderInfo(@VerifyParam(required = true)String path){
        return super.getFolderInfo(path, null);
    }

    @GetMapping("/getFile/{userId}/{fileId}")
    @GlobalInterceptor(checkParams = true,checkAdmin = true)
    public void getFile(HttpServletResponse response,
                              @VerifyParam(required = true)@PathVariable("userId")String userId,
                              @VerifyParam(required = true)@PathVariable("fileId")String fileId){
        super.getFile(response, fileId, userId);
    }

    @GetMapping("/ts/getVideoInfo/{userId}/{fileId}")
    @GlobalInterceptor(checkAdmin = true,checkParams = true)
    public void getVideoInfo(HttpServletResponse response,
                             @VerifyParam(required = true)@PathVariable("userId")String userId,
                             @VerifyParam(required = true)@PathVariable("fileId")String fileId){
        super.getFile(response, fileId, userId);
    }

    @RequestMapping("/createDownloadUrl/{userId}/{fileId}")
    @GlobalInterceptor(checkParams = true,checkAdmin = true)
    public ResponseVO createDownloadUrl(@VerifyParam(required = true)@PathVariable("userId") String userId,
                                        @VerifyParam(required = true)@PathVariable("fileId") String fileId){
        return super.createDownloadUrl(fileId,userId);
    }

    @GetMapping("/download/{code}")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public void download(HttpServletRequest request, HttpServletResponse response,
                         @VerifyParam(required = true)@PathVariable String code)throws Exception{
        super.download(request,response,code);
    }

    @PostMapping("/delFile")
    @GlobalInterceptor(checkParams = true,checkAdmin = true)
    public ResponseVO delFile(@VerifyParam(required = true)String fileIdAndUserIds){
        String[] fileIdAndUserIdArray = fileIdAndUserIds.split(",");
        for(String item : fileIdAndUserIdArray){
            String[] itemArray = item.split("_");
            fileInfoService.delFileBatch(itemArray[0],itemArray[1],true);
        }
        return getSuccessResponseVO(null);
    }

}
