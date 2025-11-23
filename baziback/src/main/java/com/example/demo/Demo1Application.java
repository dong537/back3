package com.example.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@SpringBootApplication
@MapperScan("com.example.demo.mapper")
public class Demo1Application implements CommandLineRunner {
    @Autowired
    private ApplicationContext context;

    public static void main(String[] args) {
        SpringApplication.run(Demo1Application.class, args);
    }

    @Override
    public void run(String... args) {
        // 打印所有 Controller Bean
        String[] controllers = context.getBeanNamesForAnnotation(RestController.class);
        System.out.println("=== 发现 " + controllers.length + " 个 Controller ===");
        Arrays.stream(controllers).forEach(System.out::println);

        // 特别检查 DeepSeekController
        try {
            Object bean = context.getBean("deepSeekController");
            System.out.println("✅ DeepSeekController 加载成功: " + bean.getClass());
        } catch (NoSuchBeanDefinitionException e) {
            System.err.println("❌ DeepSeekController 未被加载！");
        }
    }
}