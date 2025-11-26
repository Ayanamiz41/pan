package com.easypan.controller;

import com.easypan.entity.config.AppConfig;
import com.easypan.entity.constants.Constants;
import com.easypan.utils.StringTools;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletResponse;

public class CommonFileController extends ABaseController{
    @Autowired
    private AppConfig appConfig;

    /**
     * 获取图片文件并通过响应流输出到客户端浏览器
     * @param response HttpServletResponse，用于将图片写回客户端
     * @param imageFolder 图片所在的文件夹名（也是上传时保存的子目录）
     * @param imageName 图片文件名（包括后缀）
     */
    public void getImage(HttpServletResponse response, String imageFolder, String imageName) {
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
}
