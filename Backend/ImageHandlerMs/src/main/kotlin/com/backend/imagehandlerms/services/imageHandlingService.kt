package com.backend.imagehandlerms.services

import org.springframework.amqp.core.MessagePostProcessor
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import java.io.File
import java.nio.file.Path
import java.util.*

@Service
class ImageHandlingService(
    private val rabbitTemplate: RabbitTemplate,
) {

//    private val s3: S3Client = S3Client.builder()
//        .region(Region.of(dotenv["S3_REGION"]))
//        .credentialsProvider(
//            StaticCredentialsProvider.create(
//                AwsBasicCredentials.create(
//                    dotenv["S3_ACCESS_KEY"],
//                    dotenv["S3_SECRET_KEY"]
//                )
//            ))
//        .build()

    @Value("\${EXCHANGE_NAME}")
    private lateinit var exchangeName: String

    @Value("\${ROUTING_KEY_PROCESSING}")
    private lateinit var routingKeyProcessing: String

    @Value("\${BUCKET_NAME}")
    private lateinit var bucketName: String



    fun uploadImage(base64Image: String): PutObjectResponse? {
        val imageBytes: ByteArray
        try {
            imageBytes = Base64.getDecoder().decode(base64Image)
        } catch (e: IllegalArgumentException) {
            val logMessage = "Invalid base64 input"
            println(logMessage)
            return null
        }

        val tempFile = File.createTempFile("image", ".jpg")
        tempFile.writeBytes(imageBytes)

        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(tempFile.name)
            .build()

        var response: PutObjectResponse? = null
        var message: String

        try {
//            response = s3.putObject(putObjectRequest, Path.of(tempFile.absolutePath))
            message = base64Image
        } catch (e: Exception) {
            message = "Failed to upload image ${tempFile.name} to S3: ${e.message}"
        }

        sendMessageWithCorrelationId(message)

        tempFile.delete()

        return response
    }

    fun sendMessageWithCorrelationId(message: String) {
        val correlationId = UUID.randomUUID().toString()

        val messagePostProcessor = MessagePostProcessor {
            it.messageProperties.correlationId = correlationId
            it
        }

        val response = rabbitTemplate.convertSendAndReceive(exchangeName, routingKeyProcessing, message, messagePostProcessor)

        if (response != null) {
            println("Received response: $response")
        } else {
            println("No response received within the configured timeout period")
        }
    }
}