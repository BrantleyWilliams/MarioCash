package dev.zhihexireng.node;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MarioCashNode {
    public static void main(String[] args) {
        SpringApplication.run(MarioCashNode.class, args);
    }
}
