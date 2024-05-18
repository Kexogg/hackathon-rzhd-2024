package com.backend.imagehandlerms.services

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service

@Service
class PythonMLConsumerService {

    @RabbitListener(queues = ["\${RABBITMQ_QUEUE}"])
    fun receiveMessage(message: String) {
        println("Received message: $message")
    }

    @RabbitListener(queues = ["\${PYTHON_SERVICE_QUEUE}"])
    fun receivePythonServiceMessage(message: String) {
        println("Received message from Python service: $message")
    }
}