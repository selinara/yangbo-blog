package com.chl.gbo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class})//spring security 关闭默认安全访问控制
public class GboApplication {

    public static void main(String[] args) {
        SpringApplication.run(GboApplication.class, args);
    }

}
