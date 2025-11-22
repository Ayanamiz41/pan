package com.easypan.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component("redisUtils")
public class RedisUtils<V> {
    @Autowired
    private RedisTemplate<String,V> redisTemplate;

    private static Logger logger = LoggerFactory.getLogger(RedisUtils.class);

    /**
     * 根据键获取值
     * @param key
     * @return
     */
    public V get(String key){
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 普通缓存放入
     * @param key
     * @param value
     * @return
     */
    public boolean set(String key,V value){
        try{
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            logger.error("设置redisKey:{},value:{}失败",key,value);
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间
     * @param key
     * @param value
     * @param time
     * @return
     */
    public boolean setex(String key,V value,long time){
        try{
            if(time>0){
                redisTemplate.opsForValue().set(key, value,time, TimeUnit.SECONDS);
            }else{
                set(key,value);
            }
            return true;
        } catch (Exception e) {
            logger.error("设置redisKey:{},value:{}失败",key,value);
            return false;
        }
    }
}
