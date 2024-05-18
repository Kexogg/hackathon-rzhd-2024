package com.backend.imagehandlerms.services

import io.github.cdimascio.dotenv.Dotenv
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

    private val dotenv: Dotenv = Dotenv.load()

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

    @Value("\${ROUTING_KEY}")
    private lateinit var routingKey: String

    @Value("\${BUCKET_NAME}")
    private lateinit var bucketName: String



    fun uploadImage(base64Image: String): PutObjectResponse? {
        val imageBytes: ByteArray
        try {
            imageBytes = Base64.getDecoder().decode(base64Image)
        } catch (e: IllegalArgumentException) {
            val message = "Invalid base64 input"
            rabbitTemplate.convertAndSend(exchangeName, routingKey, message)
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
            message = "Image ${tempFile.name} was successfully uploaded to S3"
        } catch (e: Exception) {
            message = "Failed to upload image ${tempFile.name} to S3: ${e.message}"
        }

        rabbitTemplate.convertAndSend(exchangeName, routingKey, message)

        tempFile.delete()

        return response
    }
}