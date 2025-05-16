package com.device.hk;

import com.device.hk.po.DeviceInfo;
import lombok.Data;

/**
 * 视频插件配置
 * @author hk
 */
public class VideoPluginConfig {
    private String ehomePuIp;
    private String ehomeInIp;
    private short ehomeSmsPreViewPort;
    private String smsBackServerListenIP;
    private short smsBackServerListenPort;
    private short ehomeCmsPort;
    private String secretKey;
    private DeviceInfo deviceInfo;
    private short wsPort;

    public VideoPluginConfig() {
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getEhomePuIp() {
        return ehomePuIp;
    }

    public void setEhomePuIp(String ehomePuIp) {
        this.ehomePuIp = ehomePuIp;
    }

    public String getEhomeInIp() {
        return ehomeInIp;
    }

    public void setEhomeInIp(String ehomeInIp) {
        this.ehomeInIp = ehomeInIp;
    }

    public short getEhomeSmsPreViewPort() {
        return ehomeSmsPreViewPort;
    }

    public void setEhomeSmsPreViewPort(short ehomeSmsPreViewPort) {
        this.ehomeSmsPreViewPort = ehomeSmsPreViewPort;
    }

    public String getSmsBackServerListenIP() {
        return smsBackServerListenIP;
    }

    public void setSmsBackServerListenIP(String smsBackServerListenIP) {
        this.smsBackServerListenIP = smsBackServerListenIP;
    }

    public short getSmsBackServerListenPort() {
        return smsBackServerListenPort;
    }

    public void setSmsBackServerListenPort(short smsBackServerListenPort) {
        this.smsBackServerListenPort = smsBackServerListenPort;
    }

    public short getEhomeCmsPort() {
        return ehomeCmsPort;
    }

    public void setEhomeCmsPort(short ehomeCmsPort) {
        this.ehomeCmsPort = ehomeCmsPort;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public short getWsPort() {
        return wsPort;
    }

    public void setWsPort(short wsPort) {
        this.wsPort = wsPort;
    }
}

