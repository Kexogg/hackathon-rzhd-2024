package com.backend.imagehandlerms.controllers

import com.backend.imagehandlerms.services.ImageHandlingService
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import software.amazon.awssdk.services.s3.model.PutObjectResponse

@RestController
class ImageHandlingController(
    private val imageHandlingService: ImageHandlingService,
) {

    data class ImageRequest(val image: String)

    @PostMapping("/upload")
    fun uploadImage(@RequestBody imageRequest: ImageRequest): JsonNode? {
        val result = imageHandlingService.uploadImage(imageRequest.image)
        return result
    }
}