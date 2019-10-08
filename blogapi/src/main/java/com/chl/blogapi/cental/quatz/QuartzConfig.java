package com.chl.blogapi.cental.quatz;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Auther: BoYanG
 * @Describe 点赞以及缓存定时器配置
 */
@Configuration
public class QuartzConfig {
    private static final String LIKE_TASK_IDENTITY = "LikeTaskQuartz";
    private static final String USERNAME_TASK = "UserNameQuartz";

    @Bean
    public JobDetail quartzDetail(){
        return JobBuilder.newJob(LikeViewTask.class).withIdentity(LIKE_TASK_IDENTITY).storeDurably().build();
    }

    @Bean
    public Trigger quartzTrigger(){
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
//                .withIntervalInSeconds(10)  //设置时间周期单位秒
                .withIntervalInHours(2)  //两个小时执行一次
                .repeatForever();
        return TriggerBuilder.newTrigger().forJob(quartzDetail())
                .withIdentity(LIKE_TASK_IDENTITY)
                .withSchedule(scheduleBuilder)
                .build();
    }

    @Bean
    public JobDetail usernamesQuatzTrigger(){
        return JobBuilder.newJob(UserNamesTask.class).withIdentity(USERNAME_TASK).storeDurably().build();
    }

    @Bean
    public Trigger nameQuartzTrigger(){
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInHours(24)
                .repeatForever();
        return TriggerBuilder.newTrigger().forJob(usernamesQuatzTrigger())
                .withIdentity(USERNAME_TASK)
                .withSchedule(scheduleBuilder)
                .build();
    }

}
