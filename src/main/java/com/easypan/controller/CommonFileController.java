package com.easypan.controller;

import com.easypan.entity.config.AppConfig;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.enums.FileCatogoryEnum;
import com.easypan.enums.FileFolderTypeEnum;
import com.easypan.service.FileInfoService;
import com.easypan.utils.StringTools;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;

public class CommonFileController extends ABaseController{
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private FileInfoService fileInfoService;

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
        return getSuccessResponseVO(fileInfoList);
    }


}
