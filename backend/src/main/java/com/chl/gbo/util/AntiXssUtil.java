package com.chl.gbo.util;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

public class AntiXssUtil {
	// Private variables
	private static String EmptyString = "";
	// script 匹配
	private static Pattern scriptPattern = Pattern.compile("script");
	
	private static final String[] dangerCharacters = {"<",">","\"","\'","%","(",")","\\"};
	
	//部分接口的returnUrl参数会进行encode带有‘%’，为正常跳转，‘%’暂不认为是危险字符
	private static final String[] dangerCharacters4Url = {"<",">","\"","\'","(",")","\\"};
	
	private static final String[] dangerCharactersOther = {"<",">","%"};

	private static String EncodeHtml(String strInput) {
		if (strInput.length() == 0) {
			return EmptyString;
		}
		StringBuilder builder = new StringBuilder(strInput.length() * 2);
		CharacterIterator it = new StringCharacterIterator(strInput);
		for (char ch = it.first(); ch != CharacterIterator.DONE; ch = it.next()) {
			if ((((ch > '`') && (ch < '{')) || ((ch > '@') && (ch < '[')))
					|| (((ch == ' ') || ((ch > '/') && (ch < ':'))) || (((ch == '.') || (ch == ',')) || ((ch == '-') || (ch == '_'))))) {
				builder.append(ch);
			} else {
				builder.append("&#" + (int) ch + ";");
			}
		}
		return builder.toString();
	}

	private static String EncodeHtmlAttribute(String strInput) {
		if (strInput.length() == 0) {
			return EmptyString;
		}
		StringBuilder builder = new StringBuilder(strInput.length() * 2);
		CharacterIterator it = new StringCharacterIterator(strInput);
		for (char ch = it.first(); ch != CharacterIterator.DONE; ch = it.next()) {
			if ((((ch > '`') && (ch < '{')) || ((ch > '@') && (ch < '[')))
					|| (((ch > '/') && (ch < ':')) || (((ch == '.') || (ch == ',')) || ((ch == '-') || (ch == '_'))))) {
				builder.append(ch);
			} else {
				builder.append("&#" + (int) ch + ";");
			}
		}
		return builder.toString();
	}
	
	public static String filterParameter(String parameter){
		StringBuffer sb = new StringBuffer(parameter);
		for(String s : dangerCharacters){
			while(sb.indexOf(s) > -1){
				sb.deleteCharAt(sb.indexOf(s));
			}
		}
		return sb.toString();
	}
	
	/**
	 * 针对url类的参数，url类参数encode会产生%
	 * @param parameter
	 * @return
	 */
	public static String filterParameter4Url(String parameter){
		StringBuffer sb = new StringBuffer(parameter);
		for(String s : dangerCharacters4Url){
			while(sb.indexOf(s) > -1){
				sb.deleteCharAt(sb.indexOf(s));
			}
		}
		return sb.toString();
	}
	
	public static boolean isContainDangerChar(String parameter){
		if(!StringUtils.isEmpty(parameter)){
			for(String s : dangerCharacters){
				if(parameter.indexOf(s) > -1){
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 针对url类的参数，url类参数encode会产生%
	 * @param parameter
	 * @return
	 */
	public static boolean isContainDangerChar4Url(String parameter){
		if(!StringUtils.isEmpty(parameter)){
			for(String s : dangerCharacters4Url){
				if(parameter.indexOf(s) > -1){
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isContainDangerChar2(String parameter){
		if(!StringUtils.isEmpty(parameter)){
			for(String s : dangerCharactersOther){
				if(parameter.indexOf(s) > -1){
					return true;
				}
			}
		}
		return false;
	}

	private static String EncodeXml(String strInput) {
		return EncodeHtml(strInput);
	}

	private static String EncodeXmlAttribute(String strInput) {
		return EncodeHtmlAttribute(strInput);
	}

	public static String HtmlAttributeEncode(String s) {
		return EncodeHtmlAttribute(s);
	}

	public static String HtmlEncode(String s) {
		return EncodeHtml(s);
	}

	public static String XmlAttributeEncode(String s) {
		return EncodeXmlAttribute(s);
	}

	public static String XmlEncode(String s) {
		return EncodeXml(s);
	}

	/****
	 * 如果参数值含有script语句就把它html encode
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isScriptParaValue(String str) {
		if (str == null || str.equals("")) {
			return false;
		} else {
			return scriptPattern.matcher(str).find();
		}
	}

}
