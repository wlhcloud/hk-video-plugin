package com.device.hk.po;

import lombok.Data;


/**
 * 下载视频信息
 * @author wulihao
 */
@Data
public class VideoDateInfo {
    /**
     * 开始时间
     */
    private String beginTime;
    /**
     * 结束时间
     */
    private String endTime;
    /**
     * 通道号
     */
    private int channelNum;
}
