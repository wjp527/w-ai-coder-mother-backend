package com.wjp.waicodermotherbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication
public class WAiCoderMotherBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(WAiCoderMotherBackendApplication.class, args);
    }

}
