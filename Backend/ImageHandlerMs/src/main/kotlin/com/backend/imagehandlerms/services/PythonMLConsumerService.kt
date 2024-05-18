package com.backend.imagehandlerms.services

import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service

@Service
class PythonMLConsumerService {

//    @RabbitListener(queues = ["\${ROUTING_KEY_PROCESSING}"])
//    fun receiveMessage(message: Message): String {
//        val correlationId = message.messageProperties.correlationId
//        println("Received message: ${String(message.body)} with correlationId: $correlationId")
//        return "bebra"
//    }

    @RabbitListener(queues = ["\${ROUTING_KEY_HANDLER}"])
    fun receivePythonServiceMessage(message: String) {
        println("Received message from Python service: $message")
    }
}