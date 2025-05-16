package com.device.hk.module;

import com.device.hk.SdkService.StreamService.HCNetSDK;
import com.device.hk.po.RealPlayInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 操作模块
 * @author wulihao
 */
@Slf4j
public class OperationModule {
    public static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;
    //预览信息保存hash表,realPlayHandler-RealPlayInfo类
    public static Map<Long, RealPlayInfo> realPlayHandlers = new ConcurrentHashMap<>();
}
