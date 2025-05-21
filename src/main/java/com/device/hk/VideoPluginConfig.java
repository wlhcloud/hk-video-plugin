package com.device.hk;

import lombok.Data;

/**
 * 视频插件配置
 * @author wulihao
 */
@Data
public class VideoPluginConfig {
    private String ehomePuIp;
    private String ehomeInIp;
    private short ehomeSmsPreViewPort;
    private String smsBackServerListenIP;
    private short smsBackServerListenPort;
    private short ehomeCmsPort;
    private String secretKey;
    private String hlsUrl;
    private String rtmpUrl;
}

