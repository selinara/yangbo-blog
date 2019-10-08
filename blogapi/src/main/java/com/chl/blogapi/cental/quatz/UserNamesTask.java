package com.chl.blogapi.cental.quatz;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.chl.blogapi.cental.service.ViewUserService;
import com.chl.blogapi.cental.threadpool.SynaOperationUtil;
import com.chl.blogapi.util.DateUtil;

/**
 * @Auther: BoYanG
 * @Describe 每天重新过滤一次用户名集合缓存
 */
public class UserNamesTask extends QuartzJobBean {

    private static Log logger = LogFactory.getLog(UserNamesTask.class);

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    ViewUserService viewUserService;


    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.info(String.format("UserNamesTask-------- %s", DateUtil.now()));
        SynaOperationUtil.updateUserNames(stringRedisTemplate, viewUserService, null);
        SynaOperationUtil.updateUserMails(stringRedisTemplate, viewUserService);
    }
}
