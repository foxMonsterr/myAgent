package com.chat.myAgent.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 启动程序
 *
 */
@SpringBootApplication(
        exclude = { DataSourceAutoConfiguration.class },
        scanBasePackages = "com.chat.myAgent"
)
@SpringBootConfiguration
public class MainApplication
{
    public static void main(String[] args)
    {

        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(MainApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  启动成功   ლ(´ڡ`ლ)ﾞ  \n");
    }
}
