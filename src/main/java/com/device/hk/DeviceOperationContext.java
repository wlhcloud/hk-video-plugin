package com.device.hk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.device.hk.common.AjaxResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 负责根据操作指令选择并执行对应的策略
 */
public class DeviceOperationContext {

    private static final Logger logger = LoggerFactory.getLogger(DeviceOperationContext.class);

    private DeviceOperationStrategy operationStrategy;

    /**
     * 设置操作策略
     *
     * @param operationStrategy 操作策略
     * @throws IllegalArgumentException 如果策略为空，抛出异常
     */
    public void setOperationStrategy(DeviceOperationStrategy operationStrategy) {
        if (operationStrategy == null) {
            throw new IllegalArgumentException("操作策略不能为空");
        }
        this.operationStrategy = operationStrategy;
        logger.info("操作策略已设置为: {}", operationStrategy.getClass().getSimpleName());
    }

    /**
     * 执行操作
     *
     * @param config 设备配置信息
     * @return 操作结果
     */
    public String executeOperation(Map<String, Object> config) {
        if (operationStrategy == null) {
            logger.error("未指定操作策略");
            return "未指定操作策略";
        }
        try {
            logger.info("执行操作: {}", operationStrategy.getClass().getSimpleName());
            AjaxResult ajaxResult = operationStrategy.executeOperation(config);
            return JSON.toJSONString(ajaxResult);
        } catch (Exception e) {
            logger.error("操作执行失败: {}", e.getMessage(), e);
            return "操作执行失败: " + e.getMessage();
        }
    }
}
