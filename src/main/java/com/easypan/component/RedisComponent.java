package com.easypan.component;

import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.DownloadFileDto;
import com.easypan.entity.dto.SysSettingDto;
import com.easypan.entity.dto.UserSpaceDto;
import com.easypan.mappers.FileInfoMapper;
import com.easypan.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("redisComponent")
public class RedisComponent {
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private FileInfoMapper fileInfoMapper;

    public SysSettingDto getSysSettingDto(){
        SysSettingDto sysSettingDto = (SysSettingDto) redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        if(sysSettingDto == null){
            sysSettingDto = new SysSettingDto();
            redisUtils.set(Constants.REDIS_KEY_SYS_SETTING, sysSettingDto);
        }
        return sysSettingDto;
    }

    public void saveUserSpace(String userId, UserSpaceDto userSpaceDto){
        redisUtils.setex(Constants.REDIS_KEY_USER_SPACE+userId,userSpaceDto,Constants.REDIS_KEY_EXPIRES_DAY);
    }

    public UserSpaceDto getUserSpace(String userId){
        UserSpaceDto userSpaceDto =  (UserSpaceDto) redisUtils.get(Constants.REDIS_KEY_USER_SPACE+userId);
        if(userSpaceDto == null){
            userSpaceDto = new UserSpaceDto();
            Long useSpace = fileInfoMapper.selectUseSpaceByUserId(userId);
            userSpaceDto.setUseSpace(useSpace);
            userSpaceDto.setTotalSpace(getSysSettingDto().getUserInitTotalSpace()*Constants.MB);
            saveUserSpace(userId, userSpaceDto);
        }
        return userSpaceDto;
    }

    //获取临时文件大小
    public Long getFileTempSize(String userId,String fileId){
        Long currentSize = getFileSizeFromRedis(Constants.REDIS_KEY_USER_FILE_TEMP_SIZE+userId+fileId);
        return currentSize;
    }

    public void saveFileTempSize(String userId,String fileId,Long fileSize){
        Long currentSize = getFileTempSize(userId,fileId);
        redisUtils.setex(Constants.REDIS_KEY_USER_FILE_TEMP_SIZE+userId+fileId,fileSize+currentSize,Constants.REDIS_KEY_EXPIRES_ONE_HOUR);
    }

    private Long getFileSizeFromRedis(String key){
        Object sizeObj = redisUtils.get(key);
        if(sizeObj == null){
            return 0L;
        }
        if(sizeObj instanceof Integer){
            return ((Integer) sizeObj).longValue();
        }else if(sizeObj instanceof Long){
            return  (Long) sizeObj;
        }
        return 0L;
    }

    public void saveDownloadCode(DownloadFileDto downloadFileDto){
        redisUtils.setex(Constants.REDIS_KEY_DOWNLOAD+downloadFileDto.getDownloadCode(),downloadFileDto,Constants.REDIS_KEY_EXPIRES_FIVE_MIN);
    }

    public DownloadFileDto getDownloadDto(String code){
        return (DownloadFileDto) redisUtils.get(Constants.REDIS_KEY_DOWNLOAD+code);
    }
}
