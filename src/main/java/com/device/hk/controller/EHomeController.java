package com.device.hk.controller;

import com.alibaba.fastjson.JSON;
import com.device.hk.DeviceConfig;
import com.device.hk.SdkService.CmsService.CMS;
import com.device.hk.VideoPluginMain;
import com.device.hk.common.AjaxResult;
import com.device.hk.SdkService.StreamService.SMS;
import com.device.hk.common.DeviceListUtil;
import com.device.hk.module.DevicesModule;
import com.device.hk.websocket.WebSocketManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class EHomeController {
    /**
     * 执行指令
     */
    @PostMapping(value = "/execute/{operationType}")
    public AjaxResult execute(@PathVariable("operationType") String operationType, @RequestBody Map<String, Object> params)
    {
        VideoPluginMain videoPluginMain = new VideoPluginMain();
        return JSON.parseObject(videoPluginMain.execute(operationType, params),AjaxResult.class);
    }

    /**
     * 初始化
     */
    @PostMapping(value = "/init")
    public AjaxResult init()
    {
        VideoPluginMain videoPluginMain = new VideoPluginMain();
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("ehomePuIp", "192.168.1.242");
        configMap.put("ehomeInIp", "192.168.1.242");
        configMap.put("ehomeCmsPort", "7660");
        configMap.put("ehomeSmsPreViewPort", "9127");
        configMap.put("smsBackServerListenIP", "192.168.1.242");
        configMap.put("smsBackServerListenPort", "9128");
        videoPluginMain.init(configMap);
        return AjaxResult.success();
    }

    public static void main(String[] args) throws InterruptedException {
        VideoPluginMain videoPluginMain = new VideoPluginMain();
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("ehomePuIp", "192.168.1.242");
        configMap.put("ehomeInIp", "192.168.1.242");
        configMap.put("ehomeCmsPort", "7660");
        configMap.put("ehomeSmsPreViewPort", "9127");
        configMap.put("smsBackServerListenIP", "192.168.1.242");
        configMap.put("smsBackServerListenPort", "9128");
        videoPluginMain.init(configMap);
        boolean tag =  true;
        while (tag){
            Thread.sleep(1000);
            if(DeviceListUtil.getDeviceList().isEmpty()){
                continue;
            }
            tag = false;
            Map<String, Object> deviceInfoMap = new HashMap<>();
            deviceInfoMap.put("deviceId", "245652081");
            deviceInfoMap.put("wsPort", "8024");

            videoPluginMain.execute("startPreview",  deviceInfoMap);


            Thread.sleep(1000*20);
//            videoPluginMain.execute("stopPreview",  deviceInfoMap);
        }
        while (true){
            Thread.sleep(1000);
        }
    }
}
