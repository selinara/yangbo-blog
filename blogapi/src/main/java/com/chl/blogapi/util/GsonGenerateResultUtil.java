package com.chl.blogapi.util;

import org.springframework.util.StringUtils;

import com.chl.blogapi.cental.vo.CommonResultVO;
import com.google.gson.Gson;

public class GsonGenerateResultUtil {
	/**
	 * 获取返回结果
	 * @param commonResultVO
	 * @param format
	 * @param cb
	 * @return
	 */
	public static String generateResult(CommonResultVO commonResultVO, String format, String cb) {
		Gson gson = new Gson();
		if(StringUtils.isEmpty(format) || "json".equals(format)) {
			return gson.toJson(commonResultVO);
		}else {
			return getJsonPResult(cb, gson.toJson(commonResultVO));
		}
	}


	public static String generateResult(String result, String format, String cb) {
		Gson gson = new Gson();
		if(StringUtils.isEmpty(format) || "json".equals(format)) {
			return result;
		}else {
			return getJsonPResult(cb, result);
		}
	}

	private static String getJsonPResult(String cb, String s) {
		if (AntiXssUtil.isScriptParaValue(cb)) {
			cb = AntiXssUtil.HtmlEncode(cb);
		}
		if (AntiXssUtil.isContainDangerChar4Url(cb)) {
			cb = AntiXssUtil.filterParameter4Url(cb);
		}
		return cb + "(" + s + ")";
	}
}
