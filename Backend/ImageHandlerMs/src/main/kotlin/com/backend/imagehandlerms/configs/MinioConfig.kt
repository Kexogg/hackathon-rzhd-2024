package com.backend.imagehandlerms.configs

import io.minio.MinioClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class MinioConfig {
    @Value("\${S3_URL}")
    private lateinit var s3URL: String

    @Value("\${S3_SECRET_KEY}")
    private lateinit var secretKey: String

    @Value("\${S3_ACCESS_KEY}")
    private lateinit var accessKey: String

    @Bean
    fun minioClient(): MinioClient {
        return MinioClient.builder()
            .endpoint(s3URL)
            .credentials(accessKey, secretKey)
            .build()
    }
}