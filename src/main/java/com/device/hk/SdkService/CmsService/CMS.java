package com.device.hk.SdkService.CmsService;

import com.device.hk.VideoConfigManager;
import com.device.hk.VideoPluginConfig;
import com.device.hk.callback.FRegisterCallBack;
import com.device.hk.common.CommonUtil;
import com.device.hk.common.osSelect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * 中心注册服务
 * @author wulihao
 */
@Slf4j
public class CMS {
    public static HCISUPCMS hcISUPCMS = HCISUPCMS.hcISUPCMS;
    public static int CmsHandle = -1; //CMS监听句柄

    public static HCISUPCMS.NET_EHOME_CMS_LISTEN_PARAM struCMSListenParam = new HCISUPCMS.NET_EHOME_CMS_LISTEN_PARAM();

    public static FRegisterCallBack fRegisterCallBack = new FRegisterCallBack();

    public static VideoPluginConfig config = VideoConfigManager.getConfig();

    /**
     * 初始化CMS注册中心
     * @throws IOException
     */
    public void CMS_Init() {
        //根据系统加载对应的库
        if (osSelect.isWindows()) {
            //设置openSSL库的路径
            HCISUPCMS.BYTE_ARRAY ptrByteArrayCrypto = new HCISUPCMS.BYTE_ARRAY(256);
            String strPathCrypto = CommonUtil.getLibPath()+ "libeay32.dll"; //Linux版本是libcrypto.so库文件的路径
            System.arraycopy(strPathCrypto.getBytes(), 0, ptrByteArrayCrypto.byValue, 0, strPathCrypto.length());
            ptrByteArrayCrypto.write();
            hcISUPCMS.NET_ECMS_SetSDKInitCfg(0, ptrByteArrayCrypto.getPointer());

            //设置libssl.so所在路径
            HCISUPCMS.BYTE_ARRAY ptrByteArraySsl = new HCISUPCMS.BYTE_ARRAY(256);
            String strPathSsl = CommonUtil.getLibPath() + "ssleay32.dll";    //Linux版本是libssl.so库文件的路径
            System.arraycopy(strPathSsl.getBytes(), 0, ptrByteArraySsl.byValue, 0, strPathSsl.length());
            ptrByteArraySsl.write();
            hcISUPCMS.NET_ECMS_SetSDKInitCfg(1, ptrByteArraySsl.getPointer());
            //注册服务初始化
            boolean binit = hcISUPCMS.NET_ECMS_Init();
            if(binit){
            }else {
                System.out.println("CMS 注册中心初始化失败! 错误码:"+hcISUPCMS.NET_ECMS_GetLastError());
                throw new RuntimeException("CMS 注册中心初始化失败!");
            }
            //设置HCAapSDKCom组件库文件夹所在路径
            HCISUPCMS.BYTE_ARRAY ptrByteArrayCom = new HCISUPCMS.BYTE_ARRAY(256);
            String strPathCom = CommonUtil.getLibPath()+ "HCAapSDKCom";        //只支持绝对路径，建议使用英文路径
            System.arraycopy(strPathCom.getBytes(), 0, ptrByteArrayCom.byValue, 0, strPathCom.length());
            ptrByteArrayCom.write();
            hcISUPCMS.NET_ECMS_SetSDKLocalCfg(5, ptrByteArrayCom.getPointer());

        }
        else if (osSelect.isLinux()) {
            HCISUPCMS.BYTE_ARRAY ptrByteArrayCrypto = new HCISUPCMS.BYTE_ARRAY(256);
            String strPathCrypto = CommonUtil.getLibPath()+ "libcrypto.so"; //Linux版本是libcrypto.so库文件的路径
            System.arraycopy(strPathCrypto.getBytes(), 0, ptrByteArrayCrypto.byValue, 0, strPathCrypto.length());
            ptrByteArrayCrypto.write();
            hcISUPCMS.NET_ECMS_SetSDKInitCfg(0, ptrByteArrayCrypto.getPointer());

            //设置libssl.so所在路径
            HCISUPCMS.BYTE_ARRAY ptrByteArraySsl = new HCISUPCMS.BYTE_ARRAY(256);
            String strPathSsl = CommonUtil.getLibPath()+ "libssl.so";    //Linux版本是libssl.so库文件的路径
            System.arraycopy(strPathSsl.getBytes(), 0, ptrByteArraySsl.byValue, 0, strPathSsl.length());
            ptrByteArraySsl.write();
            hcISUPCMS.NET_ECMS_SetSDKInitCfg(1, ptrByteArraySsl.getPointer());
            //注册服务初始化
            boolean binit = hcISUPCMS.NET_ECMS_Init();
            if(binit){
                System.out.println("CMS 注册中心初始化成功!");
            }else {
                System.out.println("CMS 注册中心初始化失败! 错误码:"+hcISUPCMS.NET_ECMS_GetLastError());
                throw new RuntimeException("CMS 注册中心初始化失败!");
            }
            //设置HCAapSDKCom组件库文件夹所在路径
            HCISUPCMS.BYTE_ARRAY ptrByteArrayCom = new HCISUPCMS.BYTE_ARRAY(256);
            String strPathCom = CommonUtil.getLibPath()+ "HCAapSDKCom/";        //只支持绝对路径，建议使用英文路径
            System.arraycopy(strPathCom.getBytes(), 0, ptrByteArrayCom.byValue, 0, strPathCom.length());
            ptrByteArrayCom.write();
            hcISUPCMS.NET_ECMS_SetSDKLocalCfg(5, ptrByteArrayCom.getPointer());
        }

    }


    /**
     * 开启CMS监听 以接收设备注册信息
     */
    public void CMS_StartListen()
    {
        //实例化注册回调函数，便于处理设备事件
        //设置CMS监听参数
        struCMSListenParam.struAddress.szIP= config.getEhomeInIp().getBytes();
        struCMSListenParam.struAddress.wPort = config.getEhomeCmsPort();
        struCMSListenParam.fnCB = fRegisterCallBack;
        struCMSListenParam.write();

        //启动监听，接收设备注册信息
        CmsHandle = hcISUPCMS.NET_ECMS_StartListen(struCMSListenParam);
        if (CmsHandle < 0) {
            System.out.println("CMS注册中心监听失败, 错误码:" + hcISUPCMS.NET_ECMS_GetLastError());
            hcISUPCMS.NET_ECMS_Fini();
            return;
        }
        String CmsListenInfo = new String(struCMSListenParam.struAddress.szIP).trim() + "_" + struCMSListenParam.struAddress.wPort;
        System.out.println("CMS注册服务器:" + CmsListenInfo + "监听成功!");
    }


    /**
     * 录像文件查找
     * @param lUserID
     */
    public void FindRecored(int lUserID, Date startTime,Date endTime)
    {
        HCISUPCMS.NET_EHOME_REC_FILE_COND  RecFileCond = new HCISUPCMS.NET_EHOME_REC_FILE_COND();
        //录像查询条件
        RecFileCond.dwChannel = 1; //通道号
        RecFileCond.dwRecType = 0xff; //录像类型 0xff:全部录像
        RecFileCond.dwStartIndex = 0; //查询的起始位置，从0开始。
        RecFileCond.dwMaxFileCountPer = 8; //单次搜索可查询的最大文件数，由实际网络环境决定。建议最大文件数设为8。
        RecFileCond.byLocalOrUTC =0 ;
        RecFileCond.struStartTime.wYear = (short) startTime.getYear();
        RecFileCond.struStartTime.byMonth = (byte) startTime.getMonth();
        RecFileCond.struStartTime.byDay = (byte) startTime.getDay();
        RecFileCond.struStartTime.byHour = (byte) startTime.getHours();
        RecFileCond.struStartTime.byMinute = (byte) startTime.getMinutes();
        RecFileCond.struStartTime.bySecond = (byte) startTime.getSeconds();
        RecFileCond.struStopTime.wYear = (short) endTime.getYear();
        RecFileCond.struStopTime.byMonth = (byte) endTime.getMonth();
        RecFileCond.struStopTime.byDay = (byte) endTime.getDay();
        RecFileCond.struStopTime.byHour = (byte) endTime.getHours();
        RecFileCond.struStopTime.byMinute = (byte) endTime.getMinutes();
        RecFileCond.struStopTime.bySecond = (byte) endTime.getSeconds();
        RecFileCond.write();
        int FindFileHandle = hcISUPCMS.NET_ECMS_StartFindFile_V11(lUserID,HCISUPCMS.ENUM_SEARCH_RECORD_FILE,RecFileCond.getPointer(),RecFileCond.size());
        if (FindFileHandle<=-1)
        {
            System.out.println("NET_ECMS_StartFindFile_V11 error code:"+hcISUPCMS.NET_ECMS_GetLastError());
            return;
        }
        while (true)
        {
            HCISUPCMS.NET_EHOME_REC_FILE struFindData = new HCISUPCMS.NET_EHOME_REC_FILE();

            long State=hcISUPCMS.NET_ECMS_FindNextFile_V11(FindFileHandle,struFindData.getPointer(),struFindData.size());
            if (State<=-1)
            {

                System.out.println("查找失败，错误码为" + hcISUPCMS.NET_ECMS_GetLastError());
                return;

            }
            else if (State==1000)  //获取文件信息成功
            {
                struFindData.read();
                try {
                    String strFileName=new String(struFindData.sFileName,"UTF-8").trim();
                    System.out.println("文件名称："+strFileName);
                    System.out.println("文件大小:"+struFindData.dwFileSize);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                System.out.println("获取文件成功");
                continue;
            }
            else if (State==1001) //未查找到文件
            {
                System.out.println("未查找到文件");
                break;
            }
            else if (State==1002) //正在查找请等待
            {
                System.out.println("正在查找，请等待");
                continue;

            }

            else if (State==1003) //没有更多的文件，查找结束
            {
                System.out.println("没有更多的文件，查找结束");
                break;

            }
            else if (State==1004) //查找文件时异常
            {

                System.out.println("检索异常");
                break;

            }
            else if (State==1005) //查找文件超时
            {

                System.out.println("设备不支持改操作");
                break;

            }

        }

        if(!hcISUPCMS.NET_ECMS_StopFindFile(FindFileHandle))
        {
            System.out.println("NET_ECMS_StopFindFile error code:"+hcISUPCMS.NET_ECMS_GetLastError());
        }
        System.out.println("NET_ECMS_StopFindFile suss");
        return;
    }
}
