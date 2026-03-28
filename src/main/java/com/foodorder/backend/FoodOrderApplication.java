package com.foodorder.backend;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class FoodOrderApplication {

	@PostConstruct
	public void init() {
		// Set timezone mặc định cho JVM là giờ Việt Nam
		// Đảm bảo LocalDateTime.now() luôn trả về giờ Việt Nam
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
	}

	public static void main(String[] args) {
		// Set timezone trước khi Spring Boot khởi động
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
		SpringApplication.run(FoodOrderApplication.class, args);

	}

}
