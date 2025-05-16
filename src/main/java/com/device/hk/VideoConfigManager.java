package com.device.hk;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;

import java.util.Map;

/**
 * 视频插件配置管理器
 */
public class VideoConfigManager {
    private static final VideoPluginConfig config = new VideoPluginConfig();

    public static VideoPluginConfig getConfig() {
        return config;
    }

    public static void loadFromMap(Map<String, Object> map) {
        ObjectMapper mapper = new ObjectMapper();
        VideoPluginConfig newConfig = mapper.convertValue(map, VideoPluginConfig.class);
        BeanUtils.copyProperties(newConfig, config); // 拷贝更新
    }
}
