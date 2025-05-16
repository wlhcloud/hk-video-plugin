package com.device.hk.opretions;

import com.device.hk.DeviceOperationStrategy;
import com.device.hk.SdkService.StreamService.SMS;
import com.device.hk.VideoPluginConfig;
import com.device.hk.common.AjaxResult;
import com.device.hk.common.DeviceListUtil;
import com.device.hk.module.DevicesModule;
import com.device.hk.po.DeviceInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.apache.bcel.internal.generic.RET;

import java.util.Map;

/**
 * 停止设备预览的操作类，实现了 DeviceOperationStrategy 接口。
 * 该类用于处理停止设备预览的逻辑，包括验证设备注册状态、检查预览会话是否存在以及调用底层接口停止预览。
 *
 * 主要功能：
 * 1. 根据配置参数中的设备ID和WebSocket端口，获取对应的设备模块信息。
 * 2. 验证设备是否已注册，若未注册则返回错误结果。
 * 3. 检查设备是否处于预览状态，若未预览则返回错误结果。
 * 4. 调用 SMS 接口停止设备的实时预览，并返回操作结果。
 *
 * 注意事项：
 * - 配置参数中必须包含 "deviceId" 和 "wsPort"，否则可能导致操作失败。
 * - 设备预览的停止依赖于 SMS 的 StopRealPlay 方法，需确保 SMS 实例及相关映射关系正确初始化。
 * - 若设备未注册或未处于预览状态，将直接返回相应的错误提示。
 */
public class StopPreviewOperation implements DeviceOperationStrategy{
    public AjaxResult executeOperation(Map<String,Object> config)
    {
        Object deviceId = config.get("deviceId");
        Object wsPort = config.get("wsPort");
        DevicesModule devicesModule = DeviceListUtil.getDeviceModuleByDeviceId(String.valueOf(deviceId));
        if (devicesModule == null) {
            return AjaxResult.error("设备未注册");
        }
        DeviceInfo deviceInfo = devicesModule.getDeviceInfo();
        System.out.println("停止设备预览，IP = " + deviceInfo.getDeviceIp() + ", 端口 = " + deviceInfo.getDevicePort());

        SMS sms = new SMS();
        String playKey = deviceInfo.getDeviceId()+wsPort;
        Integer sessionId = SMS.LuserIDandSessionMap.get(playKey);
        if (sessionId == null) {
            return AjaxResult.error("设备未预览");
        }
        Integer previewHandle = SMS.SessionIDAndPreviewHandleMap.get(sessionId);
        if (previewHandle == null) {
            return AjaxResult.error("设备未预览");
        }
        sms.StopRealPlay(deviceInfo.getDeviceId()+wsPort,devicesModule.getLUserID(),sessionId,previewHandle);

        return AjaxResult.success("设备预览已停止");
    }
}
