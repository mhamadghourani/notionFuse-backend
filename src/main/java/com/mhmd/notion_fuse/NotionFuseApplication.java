package com.mhmd.notion_fuse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class NotionFuseApplication {

	public static void main(String[] args) {

		SpringApplication.run(NotionFuseApplication.class, args);
		System.out.println("its Mario");
	}

}