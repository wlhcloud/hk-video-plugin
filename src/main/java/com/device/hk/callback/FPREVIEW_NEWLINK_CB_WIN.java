package com.device.hk.callback;


import com.device.hk.SdkService.StreamService.HCISUPSMS;
import com.device.hk.SdkService.StreamService.SMS;
import com.sun.jna.Pointer;


/**
 * 实时预览数据回调（带窗口）
 * @author wulihao
 */
public class FPREVIEW_NEWLINK_CB_WIN implements HCISUPSMS.PREVIEW_NEWLINK_CB {
    public static HCISUPSMS hcISUPSMS = HCISUPSMS.hcISUPSMS;

    public static FPREVIEW_DATA_CB_WIN fPREVIEW_DATA_CB_WIN = new FPREVIEW_DATA_CB_WIN();

    @Override
    public boolean invoke(int lLinkHandle, HCISUPSMS.NET_EHOME_NEWLINK_CB_MSG pNewLinkCBMsg, Pointer pUserData) {

        HCISUPSMS.NET_EHOME_PREVIEW_DATA_CB_PARAM struDataCB = new HCISUPSMS.NET_EHOME_PREVIEW_DATA_CB_PARAM();

        System.out.println("参数解析,lLinkHandle:"+lLinkHandle+"设备会话ID"+pNewLinkCBMsg.iSessionID);

        //双向存储session和lLinkHandle
        SMS.PreviewHandSAndSessionIDandMap.put(lLinkHandle,pNewLinkCBMsg.iSessionID);
        SMS.SessionIDAndPreviewHandleMap.put(pNewLinkCBMsg.iSessionID,lLinkHandle);

        struDataCB.fnPreviewDataCB = fPREVIEW_DATA_CB_WIN;
        //注册回调函数以接收实时码流
        if (!hcISUPSMS.NET_ESTREAM_SetPreviewDataCB(lLinkHandle, struDataCB)) {
            System.out.println("NET_ESTREAM_SetPreviewDataCB failed err:：" + hcISUPSMS.NET_ESTREAM_GetLastError());
            return false;
        }
        return true;
    }
}