package com.javaedit.terabithia;

import com.javaedit.terabithia.config.HttpServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class TerabithiaApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(TerabithiaApplication.class, args);
        HttpServer server = new HttpServer(context);
        server.start();
    }

}
