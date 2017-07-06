package sample.camel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class CachingRouterApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(CachingRouterApplication.class, args);
    }

}