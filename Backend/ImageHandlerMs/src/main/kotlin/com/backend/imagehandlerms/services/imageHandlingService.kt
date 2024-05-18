package com.backend.imagehandlerms.services

import com.backend.imagehandlerms.models.Workbook
import com.backend.imagehandlerms.repositories.WorkbookRepository
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.minio.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.MessagePostProcessor
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.io.InputStream
import java.util.*


@Service
class ImageHandlingService(
    private val rabbitTemplate: RabbitTemplate,
    private val pythonMLConsumerService: PythonMLConsumerService,
    private val minioClient: MinioClient,
    private val workbookRepo: WorkbookRepository
) {
    private val logger: Logger = LoggerFactory.getLogger(ImageHandlingService::class.java)

    @Value("\${EXCHANGE_NAME}")
    private lateinit var exchangeName: String

    @Value("\${ROUTING_KEY_PROCESSING}")
    private lateinit var routingKeyProcessing: String

    @Value("\${BUCKET_NAME}")
    private lateinit var bucketName: String

    @Value("\${S3_URL}")
    private lateinit var s3URL: String


    fun uploadImage(base64Image: String): JsonNode? {
        val imageBytes: ByteArray
        val found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build())
        }
        try {
            imageBytes = Base64.getDecoder().decode(base64Image)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid base64 input", e)
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
        } catch (e: Exception) {
            return null
        }

        val s3Link = "${s3URL}${bucketName}/${tempFile.name}"

        val returnResponse = sendMessageWithCorrelationId(base64Image) ?: return null

        tempFile.delete()
        val workbook = Workbook(data = returnResponse.toString(), accuracy = 0.0f, s3Link = s3Link)
        workbookRepo.save(workbook)

        return returnResponse
    }

    fun editData(id: Long, newData: String): Workbook? {
        val workbook = workbookRepo.findById(id)

        if (!workbook.isPresent) {
            logger.error("Workbook with id $id not found")
            return null
        }

        val updatedWorkbook = workbook.get().apply {
            data = newData
        }

        return workbookRepo.save(updatedWorkbook)
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

    fun sendMessageWithCorrelationId(message: String): JsonNode? {
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

        logger.info("Received response: $response")
        return try {
            jacksonObjectMapper().readTree(response)
        } catch (e: Exception) {
            logger.error("Failed to parse response into JSON", e)
            null
        }
    }
}