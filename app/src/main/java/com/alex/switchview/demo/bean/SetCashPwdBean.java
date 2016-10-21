package com.alex.switchview.demo.bean;

/**
 * 作者：Alex
 * 时间：2016/10/21 15:23
 * 简述：
 */
public class SetCashPwdBean {

    /**
     * code : 1
     * data : {"certChannel":2}
     * message : success
     */

    public String code;
    /**
     * certChannel : 2
     */

    public DataBean data;
    public String message;

    public static class DataBean {
        public String certChannel;
    }
}
