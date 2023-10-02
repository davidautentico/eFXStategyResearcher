package com.drosa.efx.boot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@Slf4j
@ComponentScan(basePackages = {"com.drosa.efx"})
public class EfxStrategyResearchConnect {

    public static void main(String[] args) {

        SpringApplication.run(EfxStrategyResearchConnect.class, args);

        Runtime.getRuntime().addShutdownHook(new Thread(EfxStrategyResearchConnect::shutdown));

        log.info("***************** EfxStrategyResearchConnect Started *******************");
    }

    private static void shutdown() {

        log.info("***************** EfxStrategyResearchConnect Stopped *******************");
    }
}
