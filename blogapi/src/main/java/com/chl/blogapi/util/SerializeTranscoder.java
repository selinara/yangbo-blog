package com.chl.blogapi.util;

import java.io.Closeable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * redis对object对象进行序列化的父类
 *
 */
public abstract class SerializeTranscoder {
	private static final Log logger = LogFactory.getLog(SerializeTranscoder.class);
	
	public abstract byte[] serialize(Object value);
	public abstract Object deserialize(byte[] in);
	
	public void close(Closeable closeable) {
	    if (closeable != null) {
	    	try {
	    		closeable.close();
	    	} catch (Exception e) {
	    		logger.error("Unable to close " + closeable, e); 
	    	}
	    }
	}
}
