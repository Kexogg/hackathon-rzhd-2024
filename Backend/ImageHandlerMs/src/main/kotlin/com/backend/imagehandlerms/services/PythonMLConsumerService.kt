package com.backend.imagehandlerms.services

import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentLinkedQueue

@Service
class PythonMLConsumerService(
    private val rabbitTemplate: RabbitTemplate,
) {
    private val messages = ConcurrentLinkedQueue<String>()
    private val logger = LoggerFactory.getLogger(PythonMLConsumerService::class.java)

    @RabbitListener(queues = ["\${ROUTING_KEY_HANDLER}"])
    fun receivePythonServiceMessage(message: Message){
        val receivedMessage = String(message.body)
        logger.info("Received message from Python service: $receivedMessage")

        messages.add(receivedMessage)
    }

    fun waitForMessageAndProcess(): String? {
        var message: String? = null
        while (message == null) {
            message = messages.poll()
            Thread.sleep(500)
        }

        logger.info("Processed message in process fun: $message")
        return message
    }
}