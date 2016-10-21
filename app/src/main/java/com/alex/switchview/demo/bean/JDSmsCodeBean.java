package com.alex.switchview.demo.bean;

/**
 * 作者：Alex
 * 时间：2016/10/21 18:03
 * 简述：
 */
public class JDSmsCodeBean {

    /**
     * code : 1
     * data : {"token":"SbdzQR2ja0f8yu9bRaoNymPGb7xDIYuwnlAB6XX7Y6w="}
     * message : 成功
     */

    public String code;
    /**
     * token : SbdzQR2ja0f8yu9bRaoNymPGb7xDIYuwnlAB6XX7Y6w=
     */

    public DataBean data;
    public String message;

    public static class DataBean {
        public String token;
    }
}
