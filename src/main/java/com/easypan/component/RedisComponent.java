package com.easypan.component;

import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.SysSettingDto;
import com.easypan.entity.dto.UserSpaceDto;
import com.easypan.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("redisComponent")
public class RedisComponent {
    @Autowired
    private RedisUtils redisUtils;

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
            //TODO 查询当前用户已经上传文件大小总和
            userSpaceDto.setUseSpace(0L);
            userSpaceDto.setTotalSpace(getSysSettingDto().getUserInitTotalSpace()*Constants.MB);
            saveUserSpace(userId, userSpaceDto);
        }
        return userSpaceDto;
    }
}
