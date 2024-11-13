package org.netprime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class JwtAuthMongodbApplication {

    public static void main(String[] args) {
        SpringApplication.run(JwtAuthMongodbApplication.class, args);
    }

}
