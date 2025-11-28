package com.easypan.task;

import com.easypan.entity.po.FileInfo;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.enums.FileDelFlagEnum;
import com.easypan.service.FileInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FileCleanTask {
    @Autowired
    private FileInfoService fileInfoService;

    @Scheduled(fixedDelay = 1000*60*3)
    public void execute(){
        FileInfoQuery query = new FileInfoQuery();
        query.setDelFlag(FileDelFlagEnum.RECYCLE.getFlag());
        query.setQueryExpire(true);
        List<FileInfo> fileInfoList = fileInfoService.findListByParam(query);
        Map<String,List<FileInfo>> fileInfoMap = fileInfoList.stream().collect(Collectors.groupingBy(FileInfo::getUserId));
        for(Map.Entry<String,List<FileInfo>> entry : fileInfoMap.entrySet()){
            List<String> fileIds = entry.getValue().stream().map(p->p.getFileId()).collect(Collectors.toList());
            fileInfoService.delFileBatch(entry.getKey(),String.join(",",fileIds),false);
        }
        query.setDelFlag(FileDelFlagEnum.DEL.getFlag());
        fileInfoList = fileInfoService.findListByParam(query);
        fileInfoMap = fileInfoList.stream().collect(Collectors.groupingBy(FileInfo::getUserId));
        for(Map.Entry<String,List<FileInfo>> entry : fileInfoMap.entrySet()){
            List<String> fileIds = entry.getValue().stream().map(p->p.getFileId()).collect(Collectors.toList());
            fileInfoService.delFileBatch(entry.getKey(),String.join(",",fileIds),false);
        }
    }
}
