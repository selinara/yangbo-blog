package com.chl.blogapi.cental.bean;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * @Auther: BoYanG
 * @Describe 二维码
 */
@Data
public class MultiQrcode implements Serializable {

    private static final long serialVersionUID = -7275871290497582121L;

    public static final int QRID_STATUS_INIT = 0;//初始化
    public static final int QRID_STATUS_SCAN = 1;//扫码状态
    public static final int QRID_STATUS_SUCCESS_GRANT = 2;//已授权

    //二维码默认大小
    public static final int DEFAULT_SIZE = 180;

    private String username;
    private String token;
    private String qrid;
    private int status;
    private String appplt;

    private Date createTime;
    private Date updateTime;
}
