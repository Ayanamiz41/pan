package com.easypan.controller;


import java.util.List;

import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.dto.UploadResultDto;
import com.easypan.entity.vo.FileInfoVO;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.enums.FileCatogoryEnum;
import com.easypan.enums.FileDelFlagEnum;
import com.easypan.service.FileInfoService;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.query.FileInfoQuery;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.mail.Multipart;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @Description: 文件信息 Controller
 * @Author: false
 * @Date: 2025/07/25 20:22:51
 */
@RestController
@RequestMapping("/file")
public class FileInfoController extends CommonFileController{

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

	@PostMapping("/uploadFile")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO uploadFile(HttpSession session,
								 String fileId,
								 MultipartFile file,
								 @VerifyParam(required = true) String fileName,
								 @VerifyParam(required = true) String filePid,
								 @VerifyParam(required = true) String fileMd5,
								 @VerifyParam(required = true) Integer chunkIndex,
								 @VerifyParam(required = true) Integer chunks) {
		SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
		UploadResultDto uploadResultDto = fileInfoService.uploadFile(sessionWebUserDto,fileId,file,fileName,filePid,fileMd5,chunkIndex,chunks);
		return getSuccessResponseVO(uploadResultDto);
	}

	@GetMapping("/getImage/{imageFolder}/{imageName}")
	@GlobalInterceptor(checkParams = true)
	public void getImage(HttpServletResponse response,@PathVariable("imageFolder") String imageFolder,@PathVariable("imageName") String imageName) {
		super.getImage(response, imageFolder, imageName);
	}

	@GetMapping("ts/getVideoInfo/{fileId}")
	@GlobalInterceptor(checkParams = true)
	public void getVideoInfo(HttpServletResponse response,HttpSession session,@PathVariable("fileId") String fileId) {
		SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
		super.getFile(response,fileId,sessionWebUserDto.getUserId());
	}

	@RequestMapping ("/getFile/{fileId}")
	@GlobalInterceptor(checkParams = true)
	public void getFile(HttpServletResponse response,HttpSession session,@PathVariable("fileId") String fileId) {
		SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
		super.getFile(response,fileId,sessionWebUserDto.getUserId());
	}

	@PostMapping("/newFolder")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO newFolder(HttpSession session,
						  	@VerifyParam(required = true) String filePid,
						  	@VerifyParam(required = true) String fileName) {
		SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
   		FileInfo fileInfo = fileInfoService.newFolder(filePid,sessionWebUserDto.getUserId(),fileName);
		return getSuccessResponseVO(fileInfo);
	}

	@RequestMapping("/getFolderInfo")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO getFolderInfo(HttpSession session,
									@VerifyParam(required = true) String path){
		SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
		return super.getFolderInfo(path,sessionWebUserDto.getUserId());
	}


}