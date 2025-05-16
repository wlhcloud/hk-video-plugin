package com.device.hk.callback;


import com.device.hk.SdkService.CmsService.CMS;
import com.device.hk.SdkService.CmsService.HCISUPCMS;
import com.device.hk.VideoConfigManager;
import com.device.hk.VideoPluginConfig;
import com.device.hk.common.DeviceListUtil;
import com.device.hk.module.DevicesModule;
import com.device.hk.po.DeviceInfo;
import com.sun.jna.Pointer;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ISUP-海康注册回调
 * @author wulihao
 */
public class FRegisterCallBack implements HCISUPCMS.DEVICE_REGISTER_CB {

    public static HCISUPCMS hcISUPCMS = HCISUPCMS.hcISUPCMS;


    public boolean invoke(int lUserID, int dwDataType, Pointer pOutBuffer, int dwOutLen, Pointer pInBuffer, int dwInLen, Pointer pUser) {
        VideoPluginConfig config = VideoConfigManager.getConfig();
        System.out.println("注册回调 ,dwDataType:" + dwDataType + ", lUserID:" + lUserID);

        switch (dwDataType) {
            case HCISUPCMS.EHOME_REGISTER_TYPE.ENUM_DEV_ON:  //设备上线
                HCISUPCMS.NET_EHOME_DEV_REG_INFO_V12 strDevRegInfo = new HCISUPCMS.NET_EHOME_DEV_REG_INFO_V12();
                strDevRegInfo.write();
                Pointer pDevRegInfo = strDevRegInfo.getPointer();
                pDevRegInfo.write(0, pOutBuffer.getByteArray(0, strDevRegInfo.size()), 0, strDevRegInfo.size());
                strDevRegInfo.read();

                String deviceID = new String(strDevRegInfo.struRegInfo.byDeviceID).trim();
                String deviceIP = new String(strDevRegInfo.struRegInfo.struDevAdd.szIP).trim();
                short devicePort =  strDevRegInfo.struRegInfo.struDevAdd.wPort;
                System.out.println("设备上线==========>,DeviceID:"+ deviceID
                +"，DeviceIP:" + deviceIP
                +"，DevicePort:" + devicePort
                );

                // 添加在线设备
                DevicesModule devicesModule = DeviceListUtil.getDeviceModuleByDeviceId(deviceID);
                if(devicesModule == null){
                    // 获取设备集合
                    List<DevicesModule> deviceList = DeviceListUtil.getDeviceList();

                    // 添加新注册的设备
                    DeviceInfo deviceInfo = new DeviceInfo();
                    deviceInfo.setDeviceId(deviceID);
                    deviceInfo.setDeviceIp(deviceIP);
                    deviceInfo.setDevicePort(devicePort);
                    DevicesModule registerModule = new DevicesModule(deviceInfo);
                    registerModule.setLUserID(lUserID);
                    registerModule.setOnline(true);
                    deviceList.add(registerModule);
                }else {
                    // 修改在线状态、登录userid
                    devicesModule.setOnline(true);
                    devicesModule.setLUserID(lUserID);
                }
                return true;
            case HCISUPCMS.EHOME_REGISTER_TYPE.ENUM_DEV_AUTH: //ENUM_DEV_AUTH
                strDevRegInfo = new HCISUPCMS.NET_EHOME_DEV_REG_INFO_V12();
                strDevRegInfo.write();
                pDevRegInfo = strDevRegInfo.getPointer();
                pDevRegInfo.write(0, pOutBuffer.getByteArray(0, strDevRegInfo.size()), 0, strDevRegInfo.size());
                strDevRegInfo.read();
                byte[] bs = new byte[0];
                String szEHomeKey = VideoConfigManager.getConfig().getSecretKey(); //ISUP5.0登录校验值
                bs = szEHomeKey.getBytes();
                pInBuffer.write(0, bs, 0, szEHomeKey.length());
                break;
            case HCISUPCMS.EHOME_REGISTER_TYPE.ENUM_DEV_SESSIONKEY: //Ehome5.0设备Sessionkey回调
                strDevRegInfo = new HCISUPCMS.NET_EHOME_DEV_REG_INFO_V12();
                strDevRegInfo.write();
                pDevRegInfo = strDevRegInfo.getPointer();
                pDevRegInfo.write(0, pOutBuffer.getByteArray(0, strDevRegInfo.size()), 0, strDevRegInfo.size());
                strDevRegInfo.read();
                HCISUPCMS.NET_EHOME_DEV_SESSIONKEY struSessionKey = new HCISUPCMS.NET_EHOME_DEV_SESSIONKEY();
                System.arraycopy(strDevRegInfo.struRegInfo.byDeviceID, 0, struSessionKey.sDeviceID, 0, strDevRegInfo.struRegInfo.byDeviceID.length);
                System.arraycopy(strDevRegInfo.struRegInfo.bySessionKey, 0, struSessionKey.sSessionKey, 0, strDevRegInfo.struRegInfo.bySessionKey.length);
                struSessionKey.write();
                Pointer pSessionKey = struSessionKey.getPointer();
                hcISUPCMS.NET_ECMS_SetDeviceSessionKey(pSessionKey);
//                    AlarmDemo.hcEHomeAlarm.NET_EALARM_SetDeviceSessionKey(pSessionKey);
                break;
            case HCISUPCMS.EHOME_REGISTER_TYPE.ENUM_DEV_DAS_REQ: //Ehome5.0设备重定向请求回调
                String dasInfo = "{\n" +
                        "    \"Type\":\"DAS\",\n" +
                        "    \"DasInfo\": {\n" +
                        "        \"Address\":\"" + config.getEhomeInIp() + "\",\n" +
                        "        \"Domain\":\"\",\n" +
                        "        \"ServerID\":\"\",\n" +
                        "        \"Port\":" + config.getEhomeCmsPort() + ",\n" +
                        "        \"UdpPort\":\n" +
                        "    }\n" +
                        "}";
                byte[] bs1 = dasInfo.getBytes();
                pInBuffer.write(0, bs1, 0, dasInfo.length());
                break;
            default:
                System.out.println("回调类型为:"+dwDataType);
                break;
        }
        return true;
    }
}