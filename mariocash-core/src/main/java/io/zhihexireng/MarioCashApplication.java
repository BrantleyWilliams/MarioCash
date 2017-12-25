package dev.zhihexireng;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MarioCashApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(MarioCashApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

    }
}
