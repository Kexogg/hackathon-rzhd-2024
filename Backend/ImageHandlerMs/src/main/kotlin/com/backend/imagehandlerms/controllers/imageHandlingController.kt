package com.backend.imagehandlerms.controllers

import com.backend.imagehandlerms.services.ImageHandlingService
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.web.bind.annotation.*
import software.amazon.awssdk.services.s3.model.PutObjectResponse

@RestController
@RequestMapping("/api")
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