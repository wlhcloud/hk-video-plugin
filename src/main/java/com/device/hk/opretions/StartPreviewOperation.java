package com.device.hk.opretions;

import com.device.hk.DeviceOperationStrategy;
import com.device.hk.SdkService.StreamService.SMS;
import com.device.hk.VideoConfigManager;
import com.device.hk.VideoPluginConfig;
import com.device.hk.common.AjaxResult;
import com.device.hk.common.DeviceListUtil;
import com.device.hk.module.DevicesModule;
import com.device.hk.po.DeviceInfo;
import com.device.hk.websocket.WebSocketManager;
import com.device.hk.websocket.WebSocketServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.apache.bcel.internal.generic.NEW;

import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * StartPreviewOperation 类实现了 DeviceOperationStrategy 接口，用于启动设备的视频预览功能。
 * 该类的主要职责是根据提供的配置信息，初始化设备连接、启动 WebSocket 服务，并通过异步方式处理视频流数据。
 * 具体操作包括验证设备注册状态、设置 WebSocket 端口、启动视频预览以及将视频帧推送到客户端。
 *
 * 执行流程如下：
 * 1. 从配置中提取设备 ID 和 WebSocket 端口信息。
 * 2. 根据设备 ID 获取对应的设备模块实例，若设备未注册则返回错误结果。
 * 3. 设置设备模块的 WebSocket 端口并获取设备信息。
 * 4. 启动或获取 WebSocket 服务实例，用于向客户端推送视频帧数据。
 * 5. 创建异步控制器以处理视频预览的启动结果。
 * 6. 定义视频帧消费逻辑，将视频帧数据推送给所有连接的 WebSocket 客户端。
 * 7. 调用 SMS 的 RealPlay 方法启动视频预览，并阻塞等待启动结果。
 * 8. 根据启动结果返回成功或失败的操作反馈。
 *
 * 注意事项：
 * - 如果设备未注册或视频预览启动失败，将返回相应的错误信息。
 * - 视频帧数据的推送逻辑默认为广播给所有客户端，可根据需求调整为按 playKey 选择性推送。
 * - 异步操作中可能会抛出 InterruptedException 或 ExecutionException，需进行异常处理。
 */
public class StartPreviewOperation implements DeviceOperationStrategy {
    @Override
    public AjaxResult executeOperation(Map<String, Object> config) {
        Object deviceId = config.get("deviceId");
        Object wsPort = config.get("wsPort");

        DevicesModule devicesModule = DeviceListUtil.getDeviceModuleByDeviceId(String.valueOf(deviceId));
        if (devicesModule == null) {
            return AjaxResult.error("设备未注册");
        }
        devicesModule.setWsPort(Short.parseShort(String.valueOf(wsPort)));
        DeviceInfo deviceInfo = devicesModule.getDeviceInfo();

        // 打印预览设备信息
        System.out.println("启动设备预览，IP = " + deviceInfo.getDeviceIp() + ", 端口 = " + deviceInfo.getDevicePort());

        // 启动或获取 WebSocket 服务
        WebSocketServer wsServer = WebSocketManager.getOrCreateServer(devicesModule.getWsPort());

        // 创建异步控制器
        CompletableFuture<String> completableFuture = new CompletableFuture<>();

        // 定义视频帧消费逻辑（推送给 WebSocket 客户端）
        // 将 frame 推送给所有连接的客户端，或按 playKey 选择推送
        Consumer<byte[]> frameConsumer = wsServer::sendToAll;

        // 启动视频预览
        SMS sms = new SMS();
        sms.RealPlay(
                deviceInfo.getDeviceId()+wsPort,            // playKey
                devicesModule.getLUserID(),                   // 登录ID
                completableFuture,                            // 结果通知
                frameConsumer                                  // 视频帧回调
        );

        // 等待结果
        try {
            String realPlayResult = completableFuture.get();  // 阻塞等待启动结果
            System.out.println("异步结果是 " + realPlayResult);
            if (Objects.equals(realPlayResult, "true")) {
                return AjaxResult.success("设备预览启动成功");
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return AjaxResult.error("设备预览启动失败");
    }

}