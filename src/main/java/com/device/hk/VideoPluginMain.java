package com.device.hk;

import com.device.hk.SdkService.CmsService.CMS;
import com.device.hk.SdkService.StreamService.SMS;

import java.util.Map;

public class VideoPluginMain {

    private final DeviceOperationContext context = new DeviceOperationContext();

    // 初始化（IoT平台启动时调用）
    public void init(Map<String, Object> params) {
        VideoConfigManager.loadFromMap(params);

        //初始化流媒体服务(需要预览取流时使用)
        SMS sms = new SMS();
        sms.SMS_Init();
        sms.SMS_StartListen();

        //初始化注册服务
        CMS cms = new CMS();
        cms.CMS_Init();
        cms.CMS_StartListen();
    }

    // 执行命令（IoT平台调用）
    public String execute(String operationType, Map<String, Object> params) {
        DeviceOperationStrategy strategy = StrategyFactory.getStrategy(operationType);
        if (strategy == null) return "不支持的操作类型：" + operationType;

        context.setOperationStrategy(strategy);
        return context.executeOperation(params);
    }
}
