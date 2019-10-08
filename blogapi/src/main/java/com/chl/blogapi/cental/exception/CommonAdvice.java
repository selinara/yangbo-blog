package com.chl.blogapi.cental.exception;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.chl.blogapi.cental.vo.CommonResultVO;

/**
 * @Auther: BoYanG
 * @Describe Blog异常捕捉
 */
@ControllerAdvice
public class CommonAdvice {

    private static final Log logger  = LogFactory.getLog(CommonAdvice.class);

    @InitBinder
    public void initBinder(WebDataBinder binder) {}

    /**
     * 针对自定义异常BlogException的捕捉
     * @param ex
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = BlogException.class)
    public Object errorHandler(BlogException ex) {
        logger.warn("log PassportBizException", ex);
        CommonResultVO baseResponse = new CommonResultVO();

        HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
        String format = request.getParameter("format");
        String cb = request.getParameter("cb");
        if ("jsonp".equals(format) && StringUtils.isNotEmpty(cb)){
            return baseResponse.buildJSONPFailRes(ex.getCode(), ex.getMessage(), cb);
        }
        baseResponse.buildFailRes(ex.getCode(), ex.getMessage());
        return baseResponse;
    }

}
