package com.device.hk.callback;

import com.device.hk.SdkService.StreamService.HCISUPSMS;
import com.sun.jna.Pointer;
import org.springframework.stereotype.Component;

/**
 * 预览数据回调
 * @Author wulihao
 * @Create 2025/3/18 10:06
 */
public class PLAYBACK_DATA_CB_WIN implements HCISUPSMS.PLAYBACK_DATA_CB {
    private static int iCount ;
    private static boolean stopPlayBackFlag;
    //实时流回调函数
    public boolean invoke(int iPlayBackLinkHandle, HCISUPSMS.NET_EHOME_PLAYBACK_DATA_CB_INFO pDataCBInfo, Pointer pUserData) {
        if (iCount == 500) {//降低打印频率
            System.out.println("PLAYBACK_DATA_CB callback , dwDataLen:" + pDataCBInfo.dwDataLen + ",dwType:" + pDataCBInfo.dwType);
            iCount = 0;
        }
        iCount++;
        //播放库SDK解码显示在win窗口上，
        switch (pDataCBInfo.dwType) {
            case HCISUPSMS.NET_EHOME_SYSHEAD: //系统头
                stopPlayBackFlag = false;
            case HCISUPSMS.NET_EHOME_STREAMDATA:   //码流数据
                break;
            case HCISUPSMS.NET_EHOME_STREAMEND:  // 视频流结束标记
                System.err.println("收到回放结束信令！");
                stopPlayBackFlag = true;
                break;
        }
        return true;
    }
}