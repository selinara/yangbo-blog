package com.chl.blogapi.cental.exception;

/**
 * @Auther: BoYanG
 * @Describe 自定义异常
 */
public class BlogException extends Exception{

    private static final long serialVersionUID = 1189329151266220071L;

    private String code;

    public BlogException(String code) {
        this.code = code;
    }

    public BlogException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
