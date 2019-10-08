package com.chl.gbo;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Auther: BoYanG
 * @Describe TODO
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringRedisTest {

    @Autowired
    StringRedisTemplate srt;

    @Test
    public void contextLoads() {
        srt.opsForValue().set("hotTags111:cache", "[{\"@type\":\"com.chl.gbo.cental.domain.Labels\",\"labelId\":1,\"labelName\":\"JQuery\",\"labelDescription\":\"JQuery相关\",\"labelAlias\":\"jq\",\"isCheck\":false},{\"@type\":\"com.chl.gbo.cental.domain.Labels\",\"labelId\":6,\"labelName\":\"Oracle\",\"labelDescription\":\"oracle\",\"labelAlias\":\"oracle\",\"isCheck\":false},{\"@type\":\"com.chl.gbo.cental.domain.Labels\",\"labelId\":2,\"labelName\":\"JAVA\",\"labelDescription\":\"JAVA相关\",\"labelAlias\":\"java\",\"isCheck\":false},{\"@type\":\"com.chl.gbo.cental.domain.Labels\",\"labelId\":5,\"labelName\":\"MySQL\",\"labelDescription\":\"MySQL\",\"labelAlias\":\"mysql\",\"isCheck\":false},{\"@type\":\"com.chl.gbo.cental.domain.Labels\",\"labelId\":9,\"labelName\":\"MyBatis\",\"labelDescription\":\"mybatis\",\"labelAlias\":\"MyBatis\",\"isCheck\":false},{\"@type\":\"com.chl.gbo.cental.domain.Labels\",\"labelId\":8,\"labelName\":\"Spring\",\"labelDescription\":\"spring\",\"labelAlias\":\"spring\",\"isCheck\":false},{\"@type\":\"com.chl.gbo.cental.domain.Labels\",\"labelId\":10,\"labelName\":\"Hibernate\",\"labelDescription\":\"Hibernate\",\"labelAlias\":\"Hibernate\",\"isCheck\":false},{\"@type\":\"com.chl.gbo.cental.domain.Labels\",\"labelId\":11,\"labelName\":\"Kafka\",\"labelDescription\":\"Kafka\",\"labelAlias\":\"Kafka\",\"isCheck\":false}]",200, TimeUnit.SECONDS);
        System.out.println("==========="+srt.opsForValue().get("hotTags111:cache"));
    }


}
