package com.chl.blogapi.util;

import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @title：RetryUtil重试执行工具类
 */
public class RetryUtil {
	private static final Log logger = LogFactory.getLog(RetryUtil.class);

    public interface ExecutionHandler {
        void execute() throws Exception;
    }

    /**
     * 重试执行
     * @param retryCount
     * @param interval
     * @param timeUnit
     * @param throwIfFail
     * @param function
     * @throws Exception
     */
    public static void retry(int retryCount, long interval, TimeUnit timeUnit, boolean throwIfFail, ExecutionHandler handler) throws Exception {
        if (handler == null) {
            return;
        }

        for (int i = 0; i < retryCount; i++) {
            try {
            	    logger.info("retry count, count=" + i);
            	    handler.execute();
                break;
            } catch (Exception e) {
            	    logger.error(e);
                if (i == retryCount - 1) {
                    if (throwIfFail) {
                        throw e;
                    } else {
                        break;
                    }
                } else {
                    if (timeUnit != null && interval > 0L) {
                        try {
                            timeUnit.sleep(interval);
                        } catch (Exception ex) {
                            logger.error(ex);
                        }
                    }
                }
            }
        }
    }

    /**
     * 有间隔的重试
     * @param retryCount
     * @param interval
     * @param timeUnit
     * @param handler
     * @throws Exception
     */
    public static void retry(int retryCount, long interval, TimeUnit timeUnit, ExecutionHandler handler) throws Exception {
        retry(retryCount, interval, timeUnit, true, handler);
    }

    /**
     * 不间隔重试
     * @param retryCount
     * @param function
     * @throws Exception
     */
    public static void retry(int retryCount, ExecutionHandler handler) throws Exception {
        retry(retryCount, -1, null, handler);
    }

}