package com.device.hk;

import com.device.hk.po.DeviceInfo;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 视频插件配置
 * @author wulihao
 */
@Configuration
@Data
public class VideoPluginConfig {
    @Value("${ehome.pu-ip}")
    private String ehomePuIp;
    @Value("${ehome.in-ip}")
    private String ehomeInIp;
    @Value("${ehome.sms-preview-port}")
    private short ehomeSmsPreViewPort;
    @Value("${ehome.sms-back-server-ip}")
    private String smsBackServerListenIP;
    @Value("${ehome.sms-back-server-port}")
    private short smsBackServerListenPort;
    @Value("${ehome.cms-port}")
    private short ehomeCmsPort;
    @Value("${ehome.secret-key}")
    private String secretKey;
    @Value("${ehome.hls-url}")
    private String hlsUrl;
    @Value("${ehome.rtmp-url}")
    private String rtmpUrl;
}

