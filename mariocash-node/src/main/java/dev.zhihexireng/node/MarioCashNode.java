package dev.zhihexireng.node;

import dev.zhihexireng.core.BlockChain;
import dev.zhihexireng.core.BlockGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
public class MarioCashNode {

    public static void main(String[] args) {
        SpringApplication.run(MarioCashNode.class, args);
    }

    @Configuration
    class NodeConfig {

        @Bean
        BlockGenerator blockGenerator() {
            return new BlockGenerator();
        }

        @Bean
        BlockChain blockChain() {
            return new BlockChain();
        }
    }
}
