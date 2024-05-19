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
import java.time.LocalDateTime
import java.util.*

data class ImageText(
    val field1: String,
    val field2: String,
    val field3: String
)

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
            throw IllegalArgumentException("Invalid base64 input")
        }

        val uniqueId = UUID.randomUUID().toString()
        val tempFile = File.createTempFile(uniqueId, ".jpg")
        tempFile.writeBytes(imageBytes)

        try {
            minioClient.putObject(
                PutObjectArgs.builder().bucket(bucketName).`object`(uniqueId).stream(
                    tempFile.inputStream(), tempFile.length(), -1
                )
                    .contentType("image/jpeg")
                    .build()
            )
        } catch (e: Exception) {
            throw Exception("Failed to put object in bucket")
        }

        val s3Link = "${s3URL}${bucketName}/$uniqueId"

        val returnResponse = sendMessageWithCorrelationId(base64Image)
            ?: throw Exception("Failed to get response from Python ML service")

        val responseMap = jacksonObjectMapper().convertValue(returnResponse, MutableMap::class.java) as MutableMap<String, Any>

        val workbook = Workbook(data = returnResponse.toString(), accuracy = 0.0f, s3Link = s3Link)

        val imageTextList = responseMap.map { entry ->
            val list = entry.value as List<String>
            val imageText = ImageText(list[0], list[1], list[2])
            jacksonObjectMapper().valueToTree<JsonNode>(imageText)
        }

        val imageTextJsonNode = jacksonObjectMapper().createArrayNode().addAll(imageTextList)

        val newResponseMap = mutableMapOf<String, Any>()
        newResponseMap["imageText"] = imageTextJsonNode
        newResponseMap["imageId"] = uniqueId
        newResponseMap["s3_link"] = s3Link

        tempFile.delete()
        workbookRepo.save(workbook)

        return jacksonObjectMapper().valueToTree(newResponseMap)
    }

    fun editData(imageId: String, newData: String): Workbook {
        val workbook = workbookRepo.findByImageId(imageId)
            ?: throw IllegalArgumentException("Workbook with ImageId $imageId not found")

        val updatedWorkbook = workbook.apply {
            data = newData
        }

        return workbookRepo.save(updatedWorkbook)
    }

    fun getDataByImageId(imageId: String): Workbook {
        return workbookRepo.findByImageId(imageId)
            ?: throw IllegalArgumentException("Workbook with ImageId $imageId not found")
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

        logger.info("Received response in upload fun: $response")
        return try {
            jacksonObjectMapper().readTree(response)
        } catch (e: Exception) {
            logger.error("Failed to parse response into JSON", e)
            null
        }
    }
}