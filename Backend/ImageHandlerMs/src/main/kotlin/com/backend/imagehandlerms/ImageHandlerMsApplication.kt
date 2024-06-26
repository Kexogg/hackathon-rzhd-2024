package com.backend.imagehandlerms

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class ImageHandlerMsApplication

fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger(ImageHandlerMsApplication::class.java)
    logger.info("Checking com.backend.imagehandlerms.configs.RabbitMQConfig...")
    runApplication<ImageHandlerMsApplication>(*args)
}