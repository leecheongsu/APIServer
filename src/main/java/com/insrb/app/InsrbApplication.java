package com.insrb.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication // same as @Configuration @EnableAutoConfiguration @ComponentScan
@MapperScan(basePackages = "com.insrb.app.mapper")
public class InsrbApplication {

	public static void main(String[] args) {
		SpringApplication.run(InsrbApplication.class, args);
	}

}
