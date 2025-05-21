package com.device.hk.callback;

import com.device.hk.SdkService.StreamService.HCISUPSMS;
import com.device.hk.SdkService.StreamService.SMS;
import com.sun.jna.Pointer;

/**
 * 预览监听回调
 * @Author wulihao
 * @Create 2025/3/18 9:56
 */
public class PLAYBACK_NEWLINK_CB_WIN implements HCISUPSMS.PLAYBACK_NEWLINK_CB {
    public static PLAYBACK_DATA_CB_WIN fPLAYBACK_DATA_CB_WIN =  new PLAYBACK_DATA_CB_WIN();
    public boolean invoke(int lPlayBackLinkHandle, HCISUPSMS.NET_EHOME_PLAYBACK_NEWLINK_CB_INFO pNewLinkCBInfo, Pointer pUserData) {
        pNewLinkCBInfo.read();
        System.out.println("PLAYBACK_NEWLINK_CB callback, szDeviceID:" + new String(pNewLinkCBInfo.szDeviceID).trim()
                + ",lSessionID:" + pNewLinkCBInfo.lSessionID
                + ",dwChannelNo:" + pNewLinkCBInfo.dwChannelNo);
//        m_lPlayBackLinkHandle = lPlayBackLinkHandle;
        HCISUPSMS.NET_EHOME_PLAYBACK_DATA_CB_PARAM struCBParam = new HCISUPSMS.NET_EHOME_PLAYBACK_DATA_CB_PARAM();
        //预览数据回调参数
        struCBParam.fnPlayBackDataCB = fPLAYBACK_DATA_CB_WIN;
        struCBParam.byStreamFormat = 0;
        struCBParam.write();
        if (!SMS.hcISUPSMS.NET_ESTREAM_SetPlayBackDataCB(lPlayBackLinkHandle, struCBParam)) {
            System.out.println("NET_ESTREAM_SetPlayBackDataCB failed");
        }
        return true;
    }
}
