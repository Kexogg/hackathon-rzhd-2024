package com.backend.imagehandlerms.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
    private val pythonMLConsumerService: PythonMLConsumerService,
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



    fun uploadImage(base64Image: String): JsonNode? {
        val imageBytes: ByteArray
        var message: String

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

        message = try {
//                var response = s3.putObject(putObjectRequest, Path.of(tempFile.absolutePath))
            base64Image
        } catch (e: Exception) {
            "Failed to upload image ${tempFile.name} to S3: ${e.message}"
        }

        val returnResponse = sendMessageWithCorrelationId(message)

        if (returnResponse == null || !isJsonValid(returnResponse)) {
            println(returnResponse)
            return null
        }

        val mapper = jacksonObjectMapper()
        val json = mapper.readTree(returnResponse)
        tempFile.delete()

        return json
    }

    fun isJsonValid(jsonInString: String): Boolean {
        return try {
            val mapper = jacksonObjectMapper()
            mapper.readTree(jsonInString)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun sendMessageWithCorrelationId(message: String): String? {
        val correlationId = UUID.randomUUID().toString()

        val messagePostProcessor = MessagePostProcessor {
            it.messageProperties.correlationId = correlationId
            it
        }

        rabbitTemplate.convertAndSend(exchangeName, routingKeyProcessing, message, messagePostProcessor)

        var response: String? = null
        while (response == null) {
            response = pythonMLConsumerService.waitForMessageAndProcess()
            Thread.sleep(500)
        }

        println("Received response: $response")
        return response
    }
}