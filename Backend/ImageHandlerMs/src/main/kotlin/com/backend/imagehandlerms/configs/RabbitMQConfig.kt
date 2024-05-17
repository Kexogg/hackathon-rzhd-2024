import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
class RabbitMQConfig(private val env: Environment) {

    @Bean
    fun connectionFactory(): CachingConnectionFactory {
        val connectionFactory = CachingConnectionFactory()
        connectionFactory.setHost(env.getProperty("RABBITMQ_HOST")!!)
        connectionFactory.port = env.getProperty("RABBITMQ_PORT")!!.toInt()
        connectionFactory.username = env.getProperty("RABBITMQ_USERNAME")!!
        connectionFactory.setPassword(env.getProperty("RABBITMQ_PASSWORD")!!)
        return connectionFactory
    }

    @Bean
    fun rabbitTemplate(): RabbitTemplate {
        return RabbitTemplate(connectionFactory())
    }
}