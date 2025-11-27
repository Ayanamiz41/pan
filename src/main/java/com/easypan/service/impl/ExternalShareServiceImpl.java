package com.easypan.service.impl;

import com.easypan.entity.dto.SessionShareDto;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.po.FileShare;
import com.easypan.entity.po.UserInfo;
import com.easypan.entity.query.FileShareQuery;
import com.easypan.entity.vo.ShareInfoVO;
import com.easypan.enums.FileDelFlagEnum;
import com.easypan.enums.ResponseCodeEnum;
import com.easypan.exception.BusinessException;
import com.easypan.mappers.FileShareMapper;
import com.easypan.service.ExternalShareService;
import com.easypan.service.FileInfoService;
import com.easypan.service.FileShareService;
import com.easypan.service.UserInfoService;
import com.easypan.utils.CopyTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Date;

@Service
public class ExternalShareServiceImpl implements ExternalShareService {

    @Autowired
    private FileShareService fileShareService;
    @Autowired
    private FileInfoService fileInfoService;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private FileShareMapper<FileShare, FileShareQuery> fileShareMapper;

    public ShareInfoVO getShareInfoCommon(String shareId){
        FileShare fileShare = fileShareService.getFileShareByShareId(shareId);
        if(fileShare == null||fileShare.getValidType()!=3&&new Date().after(fileShare.getExpireTime())){
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        ShareInfoVO shareInfoVO = CopyTools.copy(fileShare, ShareInfoVO.class);
        FileInfo fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(fileShare.getFileId(), fileShare.getUserId());
        if(fileInfo==null||!fileInfo.getDelFlag().equals(FileDelFlagEnum.USING.getFlag())){
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        UserInfo userInfo = userInfoService.getUserInfoByUserId(fileShare.getUserId());
        shareInfoVO.setFileName(fileInfo.getFileName());
        shareInfoVO.setNickName(userInfo.getNickName());
        shareInfoVO.setAvatar(userInfo.getQqAvatar());
        return shareInfoVO;
    }

    public SessionShareDto checkShareCode(String shareId, String code){
        FileShare fileShare = fileShareService.getFileShareByShareId(shareId);
        if(fileShare == null||fileShare.getValidType()!=3&&new Date().after(fileShare.getExpireTime())){
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        if(!fileShare.getCode().equals(code)){
            throw new BusinessException("提取码错误");
        }
        //更新浏览次数(直接让浏览次数加1，不能先查浏览次数再传+1之后的去更新)
        fileShareMapper.updateShareShowCountPlusOne(shareId);
        SessionShareDto sessionShareDto = new SessionShareDto();
        sessionShareDto.setShareId(shareId);
        sessionShareDto.setFileId(fileShare.getFileId());
        sessionShareDto.setShareUserId(fileShare.getUserId());
        sessionShareDto.setExpireTime(fileShare.getExpireTime());
        return sessionShareDto;
    }

}
