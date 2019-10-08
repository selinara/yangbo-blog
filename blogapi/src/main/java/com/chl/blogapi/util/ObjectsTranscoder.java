package com.chl.blogapi.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ObjectsTranscoder<M extends Serializable> extends SerializeTranscoder {
	private static final Log logger = LogFactory.getLog(ObjectsTranscoder.class);
	
	@SuppressWarnings("unchecked")
	@Override
	public byte[] serialize(Object value) {
		if (value == null) {  
			throw new NullPointerException("Can't serialize null");
		}
		byte[] result = null;
		ByteArrayOutputStream bos = null;
		ObjectOutputStream os = null;
	    try {  
	        bos = new ByteArrayOutputStream();
	        os = new ObjectOutputStream(bos);
	        M m = (M) value;
	        os.writeObject(m);  
	        result = bos.toByteArray();  
	    } catch (IOException e) {
	    	logger.error(e);
	        throw new IllegalArgumentException("Non-serializable object", e);
	    } finally {  
	        close(os);  
	        close(bos);  
	    }
	    
	    return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public M deserialize(byte[] in) {
		M result = null;
		ByteArrayInputStream bis = null;
		ObjectInputStream is = null;
		try {
			if (in != null) {
				bis = new ByteArrayInputStream(in);
				is = new ObjectInputStream(bis);
				result = (M) is.readObject();
			}
		} catch (IOException e) {
			logger.error("反序列化对象出现IO异常:",e);
		} catch (ClassNotFoundException e) {
			logger.error("反序列化对象出现异常:",e);
		} finally {  
			close(is);  
		    close(bis);  
		}
		
		return result;
	}

}
