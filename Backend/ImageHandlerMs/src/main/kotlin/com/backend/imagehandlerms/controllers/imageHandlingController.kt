package com.backend.imagehandlerms.controllers

import com.backend.imagehandlerms.services.ImageHandlingService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import software.amazon.awssdk.services.s3.model.PutObjectResponse

@RestController
class ImageHandlingController(
    private val imageHandlingService: ImageHandlingService
) {

    @PostMapping("/upload")
    fun uploadImage(@RequestBody image: String): PutObjectResponse? {
        val result = imageHandlingService.uploadImage(image)
        return result
    }
}