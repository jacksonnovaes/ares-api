package br.com.ares;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AresApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(AresApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

    }
}
