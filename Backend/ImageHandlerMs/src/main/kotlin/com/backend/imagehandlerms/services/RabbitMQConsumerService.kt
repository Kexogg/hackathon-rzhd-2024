package com.backend.imagehandlerms.services

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class RabbitMQConsumerService {

    @RabbitListener(queues = ["\${RABBITMQ_QUEUE}"])
    fun receiveMessage(message: String) {
        println("Received message: $message")
        // process the message here
    }
}