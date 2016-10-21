package com.alex.switchview.demo.bean;

/**
 * 作者：Alex
 * 时间：2016/10/21 16:56
 * 简述：
 */
public class BankNameBean {

    /**
     * code : 1
     * data : {"bankName":"建设银行"}
     * message : 获取银行名称成功!
     */

    public String code;
    /**
     * bankName : 建设银行
     */

    public DataBean data;
    public String message;

    public static class DataBean {
        public String bankName;
    }
}
