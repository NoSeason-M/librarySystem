package com.library.librarysystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.library.librarysystem.mapper")
public class
LibrarySystemServerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(LibrarySystemServerApplication.class, args);
    }

}
