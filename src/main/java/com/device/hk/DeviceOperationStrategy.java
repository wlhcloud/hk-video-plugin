package com.device.hk;

import com.device.hk.common.AjaxResult;

import java.util.Map;

public interface DeviceOperationStrategy {
    /**
     * 执行插件的操作（例如，启动/停止监控）
     *
     * @param config 设备配置信息
     * @return 操作结果
     */
    AjaxResult executeOperation(Map<String,Object> config);
}
