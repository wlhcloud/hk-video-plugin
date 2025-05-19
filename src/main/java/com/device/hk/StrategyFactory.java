package com.device.hk;

import com.device.hk.opretions.GetDeviceStatusOperation;
import com.device.hk.opretions.StartPreviewOperation;
import com.device.hk.opretions.StopPreviewOperation;

import java.util.HashMap;
import java.util.Map;

/**
 * StrategyFactory 是一个工厂类，用于根据操作类型获取对应的设备操作策略实例。
 * 该类通过静态初始化块将支持的操作类型与具体的策略实现类进行映射，并提供了一个静态方法来检索这些策略。
 *
 * 功能描述：
 * 1. 维护一个操作类型到设备操作策略的映射表，支持动态扩展新的操作类型和策略。
 * 2. 提供静态方法 getStrategy，根据传入的操作类型返回对应的设备操作策略实例。
 * 3. 如果传入的操作类型未注册，则返回 null。
 *
 * 主要用途：
 * - 用于解耦操作类型的管理和具体策略的实现，便于扩展和维护。
 * - 支持多种设备操作（如启动预览、停止预览、获取设备状态等）的统一管理。
 *
 * 注意事项：
 * - 操作类型的键值必须与映射表中的定义一致，否则无法获取到对应的策略实例。
 * - 策略实例的实现类需确保线程安全，特别是在多线程环境下使用时。
 */
public class StrategyFactory {
    private static final Map<String, DeviceOperationStrategy> STRATEGY_MAP = new HashMap<>();

    static {
        STRATEGY_MAP.put("startPreview", new StartPreviewOperation());
        STRATEGY_MAP.put("stopPreview", new StopPreviewOperation());
        STRATEGY_MAP.put("deviceStatus", new GetDeviceStatusOperation());

        // 其他操作
    }

    public static DeviceOperationStrategy getStrategy(String type) {
        return STRATEGY_MAP.get(type);
    }
}
