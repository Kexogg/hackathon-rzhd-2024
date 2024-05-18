package com.backend.imagehandlerms.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.minio.*
import org.springframework.amqp.core.MessagePostProcessor
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.servlet.function.RequestPredicates.contentType
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.File
import java.io.InputStream
import java.nio.file.Path
import java.util.*


@Service
class ImageHandlingService(
    private val rabbitTemplate: RabbitTemplate,
    private val pythonMLConsumerService: PythonMLConsumerService,
    private val minioClient: MinioClient
) {
    @Value("\${EXCHANGE_NAME}")
    private lateinit var exchangeName: String

    @Value("\${ROUTING_KEY_PROCESSING}")
    private lateinit var routingKeyProcessing: String

    @Value("\${BUCKET_NAME}")
    private lateinit var bucketName: String


    fun uploadImage(base64Image: String): JsonNode? {
        val imageBytes: ByteArray
        val found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build())
        }
        try {
            imageBytes = Base64.getDecoder().decode(base64Image)
        } catch (e: IllegalArgumentException) {
            val logMessage = "Invalid base64 input"
            println(logMessage)
            return null
        }

        val tempFile = File.createTempFile("image", ".jpg")
        tempFile.writeBytes(imageBytes)

        try {
            minioClient.putObject(
                PutObjectArgs.builder().bucket(bucketName).`object`(tempFile.name).stream(
                    tempFile.inputStream(), tempFile.length(), -1
                )
                    .contentType("image/jpeg")
                    .build()
            )
            val retrievedObject = retrieveObject(bucketName, tempFile.name)
            println("Retrieved object: ${retrievedObject.reader().readText()}")
        } catch (e: Exception) {
            println("Failed to upload image ${tempFile.name} to MinIO: ${e.message}")
            return null
        }

        val returnResponse = sendMessageWithCorrelationId(base64Image)

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

    fun retrieveObject(bucketName: String, objectName: String): InputStream {
        return minioClient.getObject(
            GetObjectArgs.builder()
                .bucket(bucketName)
                .`object`(objectName)
                .build()
        )
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