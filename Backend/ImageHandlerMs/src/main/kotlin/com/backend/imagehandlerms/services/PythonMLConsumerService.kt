package com.backend.imagehandlerms.services

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service

@Service
class PythonMLConsumerService {

    @RabbitListener(queues = ["\${ROUTING_KEY_PROCESSING}"])
    fun receiveMessage(message: String) {
        println("Received message: $message")
    }

    @RabbitListener(queues = ["\${ROUTING_KEY_HANDLER}"])
    fun receivePythonServiceMessage(message: String) {
        println("Received message from Python service: $message")
    }
}