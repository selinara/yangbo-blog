package com.chl.gbo.cental.bean;

import javax.servlet.http.HttpServletRequest;

/**
 * @Auther: BoYanG
 * DataTable 请求参数
 */
public class DatatableRequest {

    /**
     *服务器请求次数，防止XSS攻击
     */
    public static Integer draw(HttpServletRequest request){
        return Integer.parseInt(request.getParameter("draw"));
    }
    /**
     *请求开始条数
     */
    public static Integer start(HttpServletRequest request){
        return Integer.parseInt(request.getParameter("start"));
    }
    /**
     * 当前页展示条数
     */
    public static Integer length(HttpServletRequest request){
        return Integer.parseInt(request.getParameter("length"));
    }
}
