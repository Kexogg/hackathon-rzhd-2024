package com.backend.imagehandlerms.configs

import io.github.cdimascio.dotenv.Dotenv
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMQConfig {

    private val dotenv: Dotenv = Dotenv.load()

    private val exchangeName = dotenv["EXCHANGE_NAME"]!!
    private val routingKey = dotenv["ROUTING_KEY"]!!

    @Bean
    fun connectionFactory(): CachingConnectionFactory {
        val connectionFactory = CachingConnectionFactory()
        connectionFactory.setHost(dotenv["RABBITMQ_HOST"]!!)
        connectionFactory.port = dotenv["RABBITMQ_PORT"]!!.toInt()
        connectionFactory.username = dotenv["RABBITMQ_USERNAME"]!!
        connectionFactory.setPassword(dotenv["RABBITMQ_PASSWORD"]!!)
        return connectionFactory
    }

    @Bean
    fun rabbitTemplate(): RabbitTemplate {
        return RabbitTemplate(connectionFactory())
    }

    @Bean
    fun directExchange(): DirectExchange {
        return DirectExchange(exchangeName, true, false)
    }

    @Bean
    fun queue(): Queue {
        return Queue(routingKey, true)
    }

    @Bean
    fun binding(directExchange: DirectExchange, queue: Queue): Binding {
        return BindingBuilder.bind(queue).to(directExchange).with(routingKey)
    }
}