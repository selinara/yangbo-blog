package com.chl.gbo.cental.vo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPObject;
import com.chl.gbo.util.AntiXssUtil;
import com.google.gson.Gson;

public class CommonResultVO extends HashMap<String, Object> {

    private static final Log logger = LogFactory.getLog(CommonResultVO.class);

    private static final long serialVersionUID = 595983338868825637L;

    private static final Gson gson = new Gson();

    public CommonResultVO(){
        setErrorCode("0");
        setMessage("success");
    }

    public void setErrorMessage(String errorCode, String message){
        setMessage(message);
        setErrorCode(errorCode);
    }

    public void setErrorCode(String errorCode){
        put("errorCode", errorCode);
    }

    public void setMessage(String message){
        put("message", message);
    }

    public String toGsonResultString(){
        return gson.toJson(this);
    }

    public String toGsonResultString(String cb){
        if (StringUtils.isEmpty(cb)) {
            return gson.toJson(this);
        }
        if (AntiXssUtil.isScriptParaValue(cb)) {
            cb = AntiXssUtil.HtmlEncode(cb);
        }
        if (AntiXssUtil.isContainDangerChar4Url(cb)) {
            cb = AntiXssUtil.filterParameter4Url(cb);
        }
        return cb + "(" + gson.toJson(this) + ")";
    }

    public String buildFailRes(String code, String msg){
        this.setErrorCode(code);
        this.setMessage(msg);
        return JSONObject.toJSONString(this);
    }

    public String buildJSONPFailRes(String code, String message,String cb){
        this.setErrorCode(code);
        this.setMessage(message);
        try {
            this.setMessage(URLEncoder.encode(message, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.warn(String.format("URLEncoder msg failed by msg %s", message), e);
        }
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(cb)){
            if (AntiXssUtil.isScriptParaValue(cb)) {
                cb = AntiXssUtil.HtmlEncode(cb);
            }
            if (AntiXssUtil.isContainDangerChar4Url(cb)) {
                cb = AntiXssUtil.filterParameter4Url(cb);
            }
            JSONPObject jsonpObject = new JSONPObject(cb);
            jsonpObject.addParameter(this);
            return jsonpObject.toJSONString();
        }
        return JSONObject.toJSONString(this);
    }
}
