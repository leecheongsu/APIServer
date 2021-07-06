package com.insrb.app;

import com.insrb.app.util.StorageService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication // same as @Configuration @EnableAutoConfiguration @ComponentScan
@EnableConfigurationProperties(StorageProperties.class)
@MapperScan(basePackages = "com.insrb.app.mapper")
public class InsrbApplication {

	@Autowired
	StorageService storageService;

	public static void main(String[] args) {
		SpringApplication.run(InsrbApplication.class, args);
	}
     
}
