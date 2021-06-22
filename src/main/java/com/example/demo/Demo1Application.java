package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Demo1Application {

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(Demo1Application.class, args);

        for(String name: applicationContext.getBeanDefinitionNames()){
            System.out.println(name);
        }

    }

}
