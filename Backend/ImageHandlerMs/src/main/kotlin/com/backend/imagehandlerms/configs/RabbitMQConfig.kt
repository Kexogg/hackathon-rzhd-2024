package com.backend.imagehandlerms.configs

import io.github.cdimascio.dotenv.Dotenv
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMQConfig {

    @Value("\${EXCHANGE_NAME}")
    private lateinit var exchangeName: String

    @Value("\${ROUTING_KEY_PROCESSING}")
    private lateinit var routingKeyProcessing: String

    @Value("\${ROUTING_KEY_HANDLER}")
    private lateinit var routingKeyHandler: String

    @Value("\${RABBITMQ_HOST}")
    private lateinit var rabbitHost: String

    @Value("\${RABBITMQ_PORT}")
    private lateinit var rabbitPort: String

    @Value("\${RABBITMQ_USERNAME}")
    private lateinit var rabbitUser: String

    @Value("\${RABBITMQ_PASSWORD}")
    private lateinit var rabbitPass: String


    @Bean
    fun connectionFactory(): CachingConnectionFactory {
        val connectionFactory = CachingConnectionFactory()
        connectionFactory.setHost(rabbitHost)
        connectionFactory.port = rabbitPort.toInt()
        connectionFactory.username = rabbitUser
        connectionFactory.setPassword(rabbitPass)
        return connectionFactory
    }

    @Bean
    fun rabbitTemplate(): RabbitTemplate {
        val template = RabbitTemplate(connectionFactory())
        template.setReceiveTimeout(20000)
        template.setReplyTimeout(20000)
        return template
    }

    @Bean
    fun directExchange(): DirectExchange {
        return DirectExchange(exchangeName, true, false)
    }

    @Bean
    fun queue(): Queue {
        return Queue(routingKeyProcessing, true)
    }

    @Bean
    fun pythonQueue(): Queue {
        return Queue(routingKeyHandler, true)
    }

    @Bean
    fun binding(directExchange: DirectExchange, @Qualifier("queue") queue: Queue): Binding {
        return BindingBuilder.bind(queue).to(directExchange).with(routingKeyProcessing)
    }

    @Bean
    fun pythonBinding(directExchange: DirectExchange, @Qualifier("pythonQueue") queue: Queue): Binding {
        return BindingBuilder.bind(queue).to(directExchange).with(routingKeyHandler)
    }
}