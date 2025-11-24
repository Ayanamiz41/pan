package com.easypan.controller;

import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.vo.ResponseVO;;

import com.easypan.enums.ResponseCodeEnum;
import com.easypan.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;;import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @Description: 信息返回状态
 * @Author: KunSpireUp
 * @Date: 3/27/2024 12:24 AM
 */
public class ABaseController {

	protected static final String STATUS_SUCCESS = "success";

	protected static final String STATUS_ERROR = "error";

	private static final Logger logger = LoggerFactory.getLogger(ABaseController.class);

	protected <T> ResponseVO getSuccessResponseVO(T t) {
		ResponseVO<T> responseVO = new ResponseVO<>();
		responseVO.setStatus(STATUS_SUCCESS);
		responseVO.setCode(ResponseCodeEnum.CODE_200.getCode());
		responseVO.setInfo(ResponseCodeEnum.CODE_200.getMsg());
		responseVO.setData(t);
		return responseVO;
	}

	protected void readFile(HttpServletResponse response,String filePath){
		if(!StringTools.pathIsOk(filePath)){
			return;
		}
		OutputStream out = null;
		FileInputStream fin = null;
		try{
			File file = new File(filePath);
			if(!file.exists()){
				return;
			}
			fin = new FileInputStream(file);
			byte[] byteData = new byte[1024];
			out = response.getOutputStream();
			int len = 0;
			while((len=fin.read(byteData))!=-1){
				out.write(byteData,0,len);
			}
			out.flush();
		} catch (Exception e) {
			logger.error("读取文件异常",e);
		}finally {
			if(out!=null){
				try{
					out.close();
				} catch (IOException e) {
					logger.error("IO异常",e);
				}
			}
			if(fin!=null){
				try{
					fin.close();
				} catch (IOException e) {
					logger.error("IO异常",e);
				}
			}
		}
	}

	protected SessionWebUserDto getUserInfoFromSession(HttpSession session){
		SessionWebUserDto sessionWebUserDto = (SessionWebUserDto)session.getAttribute(Constants.SESSION_KEY);
		return sessionWebUserDto;
	}
}
