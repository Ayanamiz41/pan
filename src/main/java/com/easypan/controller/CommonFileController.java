package com.easypan.controller;

import com.easypan.component.RedisComponent;
import com.easypan.entity.config.AppConfig;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.DownloadFileDto;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.entity.vo.FileInfoVO;
import com.easypan.entity.vo.FolderVO;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.enums.FileCatogoryEnum;
import com.easypan.enums.FileFolderTypeEnum;
import com.easypan.enums.ResponseCodeEnum;
import com.easypan.exception.BusinessException;
import com.easypan.service.FileInfoService;
import com.easypan.utils.CopyTools;
import com.easypan.utils.StringTools;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URLEncoder;
import java.util.List;

public class CommonFileController extends ABaseController{
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private FileInfoService fileInfoService;
    @Autowired
    private RedisComponent  redisComponent;

    /**
     * 获取图片文件并通过响应流输出到客户端浏览器
     * @param response HttpServletResponse，用于将图片写回客户端
     * @param imageFolder 图片所在的文件夹名（也是上传时保存的子目录）
     * @param imageName 图片文件名（包括后缀）
     */
    protected void getImage(HttpServletResponse response, String imageFolder, String imageName) {
        // 如果文件夹名或文件名为空，或路径非法，则直接返回
        if (StringTools.isEmpty(imageFolder) || StringTools.isEmpty(imageName) || !StringTools.pathIsOk(imageFolder)) {
            return;
        }

        // 获取图片文件的后缀名，如 jpg、png 等
        String imageSuffix = StringTools.getFileSuffix(imageName);
        imageSuffix = imageSuffix.replace(".","");

        // 构建图片文件的完整路径（根目录 + /file/ + 文件夹 + 文件名）
        String filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + "/" +imageFolder + "/" + imageName;

        // 设置响应头的内容类型，例如 image/jpeg 或 image/png
        String contentType = "image/" + imageSuffix;
        response.setContentType(contentType);

        // 设置缓存控制，允许浏览器缓存该图片 30 天（2592000 秒）
        response.setHeader("Cache-Control", "max-age=2592000");

        // 调用工具方法将图片读取并写入到响应流中
        readFile(response, filePath);
    }

    protected void getFile(HttpServletResponse response, String fileId, String userId) {
        // 文件路径变量初始化
        String filePath = null;

        // 如果请求的是.ts切片文件（说明是HLS的切片文件）
        if (fileId.endsWith(".ts")) {
            // 拆分.ts文件名获取真实的 fileId
            String[] tsArray = fileId.split("_");
            String realFileId = tsArray[0];

            // 根据真实 fileId 和用户ID查询文件信息
            FileInfo fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(realFileId, userId);

            // 如果未查询到，直接返回
            if (fileInfo == null) {
                return;
            }

            // 构造切片文件的完整路径：项目根路径 + 文件夹路径 + 去后缀的原文件名 + / + 当前的 .ts 文件名
            String fileName = StringTools.getFileNameNoSuffix(fileInfo.getFilePath()) + "/" + fileId;
            filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + "/" + fileName;
        } else {
            FileInfo fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(fileId, userId);

            // 文件信息不存在直接返回
            if (fileInfo == null) {
                return;
            }

            // 如果是视频文件，构造 m3u8 文件路径
            if (FileCatogoryEnum.VIDEO.getCategory().equals(fileInfo.getFileCategory())) {
                // 获取不带后缀的文件名
                String fileNameNoSuffix = StringTools.getFileNameNoSuffix((fileInfo.getFilePath()));
                // 构造 m3u8 文件路径：项目路径 + 存储文件夹 + 不带后缀的文件名 + / + 固定的 m3u8 文件名
                filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + "/" + fileNameNoSuffix + "/" + Constants.M3U8_NAME;
            }else{
                //如果是其他文件，则直接读
                filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE +"/"+ fileInfo.getFilePath();
            }
        }

        // 文件对象创建
        File file = new File(filePath);

        // 如果文件不存在，直接返回
        if (!file.exists()) {
            return;
        }

        // 将文件内容写入到响应中（响应给前端播放器）
        readFile(response, filePath);
    }

    // 获取指定路径对应的文件夹信息
    protected ResponseVO getFolderInfo(String path, String userId) {
        // 将路径按 "/" 分割为数组，例如 "a/b/c" -> ["a", "b", "c"]
        String[] pathArray = path.split("/");

        // 构造查询对象
        FileInfoQuery fileInfoQuery = new FileInfoQuery();
        fileInfoQuery.setUserId(userId); // 设置所属用户
        fileInfoQuery.setFolderType(FileFolderTypeEnum.FOLDER.getType()); // 设置类型为“文件夹”
        fileInfoQuery.setFileIdArray(pathArray); // 设置需要查询的多个文件夹 ID

        // 构造排序语句，按路径中各个文件夹的顺序排序
        String orderBy = "field(file_id,\"" + StringUtils.join(pathArray, "\",\"") + "\")";
        fileInfoQuery.setOrderBy(orderBy); // 设置排序字段

        // 根据参数查询文件夹信息
        List<FileInfo> fileInfoList = fileInfoService.findListByParam(fileInfoQuery);

        // 返回查询结果（包装成统一响应结构）
        return getSuccessResponseVO(CopyTools.copyList(fileInfoList, FolderVO.class));
    }

    // 创建一个用于下载的URL code（验证码），并将文件信息暂存到Redis中，供后续下载接口使用
    protected ResponseVO createDownloadUrl(String fileId,String userId){
        // 根据 fileId 和 userId 从数据库中查出对应的文件信息
        FileInfo fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(fileId, userId);

        // 若查不到这个文件，或该用户无权限，则抛出业务异常
        if(fileInfo == null){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        // 判断该 fileInfo 是否是“文件夹”类型，如果是，则也禁止下载，抛异常
        if(FileFolderTypeEnum.FOLDER.getType().equals(fileInfo.getFolderType())){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        // 生成一个随机的50位下载验证码，作为临时下载凭证
        String code = StringTools.getRandomString(Constants.LENGTH_50);

        // 构造下载所需的 DTO，封装文件名、路径和下载验证码
        DownloadFileDto downloadFileDto = new DownloadFileDto();
        downloadFileDto.setFileName(fileInfo.getFileName());
        downloadFileDto.setFilePath(fileInfo.getFilePath());
        downloadFileDto.setDownloadCode(code);

        // 把该下载信息保存到 Redis 中，key 是 code，值是 downloadFileDto，供稍后下载使用
        redisComponent.saveDownloadCode(downloadFileDto);
        // 返回响应给前端，包含成功状态和该下载 code
        return getSuccessResponseVO(code);
    }

    // 下载接口，通过传入的 code 去 Redis 中查找文件信息，然后写入响应流，实现文件下载
    protected void download(HttpServletRequest request,HttpServletResponse response,String code)throws Exception {
        // 根据下载 code 从 Redis 获取对应的文件下载信息
        DownloadFileDto downloadFileDto = redisComponent.getDownloadDto(code);

        // 如果 Redis 中没有该 code（说明 code 失效或错误），直接返回，不进行任何下载
        if(downloadFileDto == null){
            return;
        }

        // 构造完整的文件系统路径，用于后续从磁盘读取文件（注意：filePath 是相对路径）
        String filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + "/" +downloadFileDto.getFilePath();

        // 获取原始文件名
        String fileName = downloadFileDto.getFileName();

        // 设置响应头的内容类型为下载类型，防止浏览器直接打开
        response.setContentType("application/x-msdownload;charset=utf-8");

        // 判断是否是 IE 浏览器（msie），IE 对文件名编码要求不同
        if(request.getHeader("User-Agent").toLowerCase().indexOf("msie") >0){
            // IE 下使用 URLEncoder 进行 utf-8 编码
            fileName = URLEncoder.encode(fileName, "utf-8");
        }else{
            // 非 IE 浏览器下，用 ISO8859-1 重新编码，防止中文乱码
            fileName = new String(fileName.getBytes("UTF-8"), "ISO8859-1");
        }

        // 设置 Content-Disposition 头，告诉浏览器“这是个附件”，触发下载，并设置文件名
        response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");

        // 调用工具方法，将磁盘文件内容读入并写入 response 输出流，完成下载
        readFile(response, filePath);
    }


}
