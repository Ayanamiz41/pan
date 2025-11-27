package com.easypan.service;

import com.easypan.entity.dto.SessionShareDto;
import com.easypan.entity.vo.ShareInfoVO;

import javax.servlet.http.HttpSession;

public interface ExternalShareService {

    ShareInfoVO getShareInfoCommon(String shareId);

    SessionShareDto checkShareCode(String shareId,String code);

}
