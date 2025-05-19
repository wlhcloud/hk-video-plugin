package com.device.hk.callback;

import com.device.hk.SdkService.StreamService.HCISUPSMS;
import com.device.hk.SdkService.StreamService.HCNetSDK;
import com.device.hk.SdkService.StreamService.SMS;
import com.device.hk.stream.HandleStreamV2;
import com.sun.jna.Pointer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 预览数据的回调函数 - 窗口实时预览
 * @author wulihao
 */
public class FPREVIEW_DATA_CB_WIN implements HCISUPSMS.PREVIEW_DATA_CB {


    static File file;


    static Map<Integer, FileOutputStream> outputStreamMap = new HashMap();
    //实时流回调函数/
    @Override
    public void invoke(int iPreviewHandle, HCISUPSMS.NET_EHOME_PREVIEW_CB_MSG pPreviewCBMsg, Pointer pUserData) {
        switch (pPreviewCBMsg.byDataType) {
            case HCNetSDK.NET_DVR_SYSHEAD: //系统头
            {

                System.out.println("系统头:"+pPreviewCBMsg.pRecvdata);
            }
            case HCNetSDK.NET_DVR_STREAMDATA:   //码流数据
            {

                byte[] dataStream = pPreviewCBMsg.pRecvdata.getByteArray(0, pPreviewCBMsg.dwDataLen);


//                try {
//                    //从map中取出对应的流读取数据
//                    outputStreamMap.get(iPreviewHandle).write(dataStream);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

                if(dataStream!=null){
                    Integer l = SMS.PreviewHandSAndSessionIDandMap.get(iPreviewHandle);
                    HandleStreamV2 handleStreamV2 =SMS.concurrentMap.get(l);
                    handleStreamV2.processStream(dataStream);
                }
            }
        }
    }

    //创建文件
    /**
     *
     *
     * @date 2022/8/31 23:37
     * @param userId：登陆返回的用户句柄
     */
    public static void setFile(int userId) {
        file = new File("D:/video/" + new Date().getTime() + "(" + userId + ")" + ".mp4");  //保存回调函数的音频数据

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//            FileOutputStream outputStream=new FileOutputStream(file);
        try {

            outputStreamMap.put(userId, new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}