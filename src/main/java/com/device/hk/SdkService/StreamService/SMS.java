package com.device.hk.SdkService.StreamService;

import com.device.hk.SdkService.CmsService.CMS;
import com.device.hk.SdkService.CmsService.HCISUPCMS;
import com.device.hk.VideoConfigManager;
import com.device.hk.VideoPluginConfig;
import com.device.hk.callback.FPREVIEW_NEWLINK_CB_WIN;
import com.device.hk.callback.PLAYBACK_NEWLINK_CB_WIN;
import com.device.hk.common.CommonUtil;
import com.device.hk.common.DeviceListUtil;
import com.device.hk.stream.HandleStreamV2;
import com.device.hk.common.osSelect;
import com.device.hk.module.DevicesModule;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * 流媒体服务
 *
 * @author wulihao
 */
@Slf4j
public class SMS {
    public SMS() {
    }

    public static HCISUPSMS hcISUPSMS = HCISUPSMS.hcISUPSMS;

    public static Map<Integer, HandleStreamV2> concurrentMap = new HashMap<>();
    public static Map<Integer, Integer> PreviewHandSAndSessionIDandMap = new HashMap<>();
    public static Map<Integer, Integer> SessionIDAndPreviewHandleMap = new HashMap<>();
    public static Map<String, Integer> LuserIDandSessionMap = new HashMap<>();

    public static FPREVIEW_NEWLINK_CB_WIN fPREVIEW_NEWLINK_CBWIN = new FPREVIEW_NEWLINK_CB_WIN();//预览监听回调函数实现
    public static PLAYBACK_NEWLINK_CB_WIN fPLAYBACK_NEWLINK_CB_WIN = new PLAYBACK_NEWLINK_CB_WIN();

    public static int StreamHandle = -1;   //预览监听句柄

    public static int Count = 0;//
    public static HCISUPSMS.NET_EHOME_LISTEN_PREVIEW_CFG struPreviewListen = new HCISUPSMS.NET_EHOME_LISTEN_PREVIEW_CFG();
    public static HCISUPSMS.NET_EHOME_PLAYBACK_LISTEN_PARAM struPlayBackListen = new HCISUPSMS.NET_EHOME_PLAYBACK_LISTEN_PARAM();

    public static VideoPluginConfig config = VideoConfigManager.getConfig();

    public void SMS_Init() {
        //根据系统加载对应的库
        if (osSelect.isWindows()) {
            HCISUPCMS.BYTE_ARRAY ptrByteArrayCrypto = new HCISUPCMS.BYTE_ARRAY(256);
            String strPathCrypto = CommonUtil.getLibPath() + "libeay32.dll"; //Linux版本是libcrypto.so库文件的路径
            System.arraycopy(strPathCrypto.getBytes(), 0, ptrByteArrayCrypto.byValue, 0, strPathCrypto.length());
            ptrByteArrayCrypto.write();
            if (!hcISUPSMS.NET_ESTREAM_SetSDKInitCfg(0, ptrByteArrayCrypto.getPointer())) {
                System.out.println("NET_ESTREAM_SetSDKInitCfg 0 failed, error:" + hcISUPSMS.NET_ESTREAM_GetLastError());
            }

            HCISUPCMS.BYTE_ARRAY ptrByteArraySsl = new HCISUPCMS.BYTE_ARRAY(256);
            String strPathSsl = CommonUtil.getLibPath() + "ssleay32.dll";    //Linux版本是libssl.so库文件的路径
            System.arraycopy(strPathSsl.getBytes(), 0, ptrByteArraySsl.byValue, 0, strPathSsl.length());
            ptrByteArraySsl.write();
            if (!hcISUPSMS.NET_ESTREAM_SetSDKInitCfg(1, ptrByteArraySsl.getPointer())) {
                System.out.println("NET_ESTREAM_SetSDKInitCfg 1 failed, error:" + hcISUPSMS.NET_ESTREAM_GetLastError());
            }
            //流媒体初始化
            boolean b = hcISUPSMS.NET_ESTREAM_Init();
            if (b) {
                System.out.println("SMS 流媒体初始化成功!");
            } else {
                System.out.println("SMS 流媒体初始化失败! 错误码:" + hcISUPSMS.NET_ESTREAM_GetLastError());
            }
            //设置HCAapSDKCom组件库文件夹所在路径
            HCISUPCMS.BYTE_ARRAY ptrByteArrayCom = new HCISUPCMS.BYTE_ARRAY(256);
            String strPathCom = CommonUtil.getLibPath() + "HCAapSDKCom";      //只支持绝对路径，建议使用英文路径
            System.arraycopy(strPathCom.getBytes(), 0, ptrByteArrayCom.byValue, 0, strPathCom.length());
            ptrByteArrayCom.write();
            if (!hcISUPSMS.NET_ESTREAM_SetSDKLocalCfg(5, ptrByteArrayCom.getPointer())) {
                System.out.println("NET_ESTREAM_SetSDKLocalCfg 5 failed, error:" + hcISUPSMS.NET_ESTREAM_GetLastError());
            }
            hcISUPSMS.NET_ESTREAM_SetLogToFile(3, "..\\EHomeSDKLog", false);
        } else if (osSelect.isLinux()) {
            //设置libcrypto.so所在路径
            HCISUPCMS.BYTE_ARRAY ptrByteArrayCrypto = new HCISUPCMS.BYTE_ARRAY(256);
            String strPathCrypto = CommonUtil.getLibPath() + "libcrypto.so"; //Linux版本是libcrypto.so库文件的路径
            System.arraycopy(strPathCrypto.getBytes(), 0, ptrByteArrayCrypto.byValue, 0, strPathCrypto.length());
            ptrByteArrayCrypto.write();
            if (!hcISUPSMS.NET_ESTREAM_SetSDKInitCfg(0, ptrByteArrayCrypto.getPointer())) {
                System.out.println("NET_ESTREAM_SetSDKInitCfg 0 failed, error:" + hcISUPSMS.NET_ESTREAM_GetLastError());
            }
            //设置libssl.so所在路径
            HCISUPCMS.BYTE_ARRAY ptrByteArraySsl = new HCISUPCMS.BYTE_ARRAY(256);
            String strPathSsl = CommonUtil.getLibPath() + "libssl.so";    //Linux版本是libssl.so库文件的路径
            System.arraycopy(strPathSsl.getBytes(), 0, ptrByteArraySsl.byValue, 0, strPathSsl.length());
            ptrByteArraySsl.write();
            if (!hcISUPSMS.NET_ESTREAM_SetSDKInitCfg(1, ptrByteArraySsl.getPointer())) {
                System.out.println("NET_ESTREAM_SetSDKInitCfg 1 failed, error:" + hcISUPSMS.NET_ESTREAM_GetLastError());
            }
            //流媒体初始化
            boolean b = hcISUPSMS.NET_ESTREAM_Init();
            if (b) {
                System.out.println("SMS 流媒体初始化成功!");
            } else {
                System.out.println("SMS 流媒体初始化失败! 错误码:" + hcISUPSMS.NET_ESTREAM_GetLastError());
            }
            //设置HCAapSDKCom组件库文件夹所在路径
            HCISUPCMS.BYTE_ARRAY ptrByteArrayCom = new HCISUPCMS.BYTE_ARRAY(256);
            String strPathCom = CommonUtil.getLibPath() + "HCAapSDKCom/";      //只支持绝对路径，建议使用英文路径
            System.arraycopy(strPathCom.getBytes(), 0, ptrByteArrayCom.byValue, 0, strPathCom.length());
            ptrByteArrayCom.write();
            if (!hcISUPSMS.NET_ESTREAM_SetSDKLocalCfg(5, ptrByteArrayCom.getPointer())) {
                System.out.println("NET_ESTREAM_SetSDKLocalCfg 5 failed, error:" + hcISUPSMS.NET_ESTREAM_GetLastError());
            }
            hcISUPSMS.NET_ESTREAM_SetLogToFile(3, "./EHomeSDKLog", false);
        }

    }

    /**
     * 开启实时预览监听(带界面窗口)
     */
    public void SMS_StartListen() {
        //预览监听·
        struPreviewListen.struIPAdress.szIP = config.getEhomeInIp().getBytes();
        struPreviewListen.struIPAdress.wPort = config.getEhomeSmsPreViewPort(); //流媒体服务器监听端口
        struPreviewListen.fnNewLinkCB = fPREVIEW_NEWLINK_CBWIN; //预览连接请求回调函数
        struPreviewListen.pUser = null;
        struPreviewListen.byLinkMode = 0; //0- TCP方式，1- UDP方式
        struPreviewListen.write();

        int SmsHandle = hcISUPSMS.NET_ESTREAM_StartListenPreview(struPreviewListen);

        if (SmsHandle < 0) {
            System.out.println("SMS流媒体服务监听失败, 错误码:" + hcISUPSMS.NET_ESTREAM_GetLastError());
//            SMSListenerFlag=false;
            hcISUPSMS.NET_ESTREAM_Fini();
            return;
        } else {
            String StreamListenInfo = new String(struPreviewListen.struIPAdress.szIP).trim() + "_" + struPreviewListen.struIPAdress.wPort;
            System.out.println("SMS流媒体服务:" + StreamListenInfo + "监听成功!");
//            SMSListenerFlag=true;
        }
    }


    /**
     * 开启预览，
     *
     * @param luserID 预览通道号
     */
    public void RealPlay(String playKey, int luserID, CompletableFuture<String> completableFutureOne, Consumer<byte[]> frameConsumer) {
        HCISUPCMS.NET_EHOME_PREVIEWINFO_IN struPreviewIn = new HCISUPCMS.NET_EHOME_PREVIEWINFO_IN();
        struPreviewIn.iChannel = 1; //通道号
        struPreviewIn.dwLinkMode = 0; //0- TCP方式，1- UDP方式
        struPreviewIn.dwStreamType = 0; //码流类型：0- 主码流，1- 子码流, 2- 第三码流
        struPreviewIn.struStreamSever.szIP = config.getEhomePuIp().getBytes();
        //流媒体服务器IP地址,公网地址
        struPreviewIn.struStreamSever.wPort = config.getEhomeSmsPreViewPort(); //流媒体服务器端口，需要跟服务器启动监听端口一致
        struPreviewIn.write();
        //预览请求
        HCISUPCMS.NET_EHOME_PREVIEWINFO_OUT struPreviewOut = new HCISUPCMS.NET_EHOME_PREVIEWINFO_OUT();
        //请求开始预览
        if (!CMS.hcISUPCMS.NET_ECMS_StartGetRealStream(luserID, struPreviewIn, struPreviewOut)) {
            System.out.println("请求开始预览失败,错误码:" + CMS.hcISUPCMS.NET_ECMS_GetLastError());
            return;
        } else {
            struPreviewOut.read();
            System.out.println("请求预览成功, sessionID:" + struPreviewOut.lSessionID);
        }
        HCISUPCMS.NET_EHOME_PUSHSTREAM_IN struPushInfoIn = new HCISUPCMS.NET_EHOME_PUSHSTREAM_IN();
        struPushInfoIn.read();
        struPushInfoIn.dwSize = struPushInfoIn.size();
        struPushInfoIn.lSessionID = struPreviewOut.lSessionID;
        struPushInfoIn.write();
        HCISUPCMS.NET_EHOME_PUSHSTREAM_OUT struPushInfoOut = new HCISUPCMS.NET_EHOME_PUSHSTREAM_OUT();
        struPushInfoOut.read();
        struPushInfoOut.dwSize = struPushInfoOut.size();
        struPushInfoOut.write();
        //中心管理服务器（CMS）向设备发送请求，设备开始传输预览实时码流
        if (!CMS.hcISUPCMS.NET_ECMS_StartPushRealStream(luserID, struPushInfoIn, struPushInfoOut)) {
            System.out.println("CMS向设备发送请求预览实时码流失败, error code:" + CMS.hcISUPCMS.NET_ECMS_GetLastError());
            return;
        } else {
            System.out.println("CMS向设备发送请求预览实时码流成功, sessionID:" + struPushInfoIn.lSessionID);

            if (LuserIDandSessionMap.get(playKey) == null) {
                LuserIDandSessionMap.put(playKey, struPushInfoIn.lSessionID);
            }

            if (concurrentMap.get(struPushInfoIn.lSessionID) == null) {
                concurrentMap.put(struPushInfoIn.lSessionID,
                        new HandleStreamV2(playKey,config.getRtmpUrl(), config.getHlsUrl(), true,completableFutureOne, frameConsumer));
                System.out.println("加入concurrentMap :" + luserID);
            }
        }
    }

    /**
     * 停止预览,Stream服务停止实时流转发，CMS向设备发送停止预览请求
     */
    public void StopRealPlay(String playKey, int luserID, int sessionID, int lPreviewHandle) {

        //停止线程
        cleanCache(playKey);

        if (!concurrentMap.containsKey(sessionID) && !PreviewHandSAndSessionIDandMap.containsKey(lPreviewHandle) && !LuserIDandSessionMap.containsKey(playKey) && !SessionIDAndPreviewHandleMap.containsKey(sessionID)) {
            System.out.println("会话" + sessionID + "相关资源已被清空");
        }

        //停止某一通道转发预览实时码流
        if (!hcISUPSMS.NET_ESTREAM_StopPreview(lPreviewHandle)) {
            System.out.println("停止转发预览实时码流失败, 错误码: " + hcISUPSMS.NET_ESTREAM_GetLastError());
            return;
        }
        System.out.println("停止Stream的实时流转发");
        //请求停止预览
        if (!CMS.hcISUPCMS.NET_ECMS_StopGetRealStream(luserID, sessionID)) {
            System.out.println("请求停止预览失败,错误码: " + CMS.hcISUPCMS.NET_ECMS_GetLastError());
            return;
        }

        System.out.println("CMS已发送停止预览请求");
    }


    /**
     * 设置回放监听
     * 启用流媒体服务器（SMS）的回放监听并注册回调函数以接收设备连接请求
     */
    public void startPlayBackListen() {
        //回放监听
        System.arraycopy(config.getSmsBackServerListenIP().getBytes(), 0, struPlayBackListen.struIPAdress.szIP, 0, config.getSmsBackServerListenIP().length());
        struPlayBackListen.struIPAdress.wPort = config.getSmsBackServerListenPort(); //流媒体服务器监听端口
        struPlayBackListen.fnNewLinkCB = fPLAYBACK_NEWLINK_CB_WIN;
        struPlayBackListen.byLinkMode = 0; //0- TCP方式，1- UDP方式

        // 开始监听预览
        int m_lPlayBackListenHandle = hcISUPSMS.NET_ESTREAM_StartListenPlayBack(struPlayBackListen);
        if (m_lPlayBackListenHandle < -1) {
            System.out.println("NET_ESTREAM_StartListenPlayBack failed, error code:" + hcISUPSMS.NET_ESTREAM_GetLastError());
            hcISUPSMS.NET_ESTREAM_Fini();
            return;
        } else {
            String BackStreamListenInfo = new String(struPlayBackListen.struIPAdress.szIP).trim() + "_" + struPlayBackListen.struIPAdress.wPort;
            System.out.println("回放流媒体服务：" + BackStreamListenInfo + ",NET_ESTREAM_StartListenPlayBack succeed");
        }
    }

    /**
     * 停止回放监听
     */
    public static void cleanCache(String playKey) {
        //停止线程
        Integer sessionID = SMS.LuserIDandSessionMap.get(playKey);
        if (sessionID == null) {
            return;
        }
        HandleStreamV2 handleStreamV2 = SMS.concurrentMap.get(sessionID);
        handleStreamV2.close();
        handleStreamV2.stopProcessing();
        Integer lPreviewHandle = SMS.SessionIDAndPreviewHandleMap.get(sessionID);
        SMS.concurrentMap.remove(sessionID);
        SMS.PreviewHandSAndSessionIDandMap.remove(lPreviewHandle);
        SMS.LuserIDandSessionMap.remove(playKey);
        SMS.SessionIDAndPreviewHandleMap.remove(sessionID);
    }
}
