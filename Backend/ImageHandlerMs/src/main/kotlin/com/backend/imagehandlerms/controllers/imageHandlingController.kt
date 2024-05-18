package com.backend.imagehandlerms.controllers

import com.backend.imagehandlerms.models.Workbook
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
    data class EditDataRequest(val data: String)

    @PostMapping("/upload")
    fun uploadImage(@RequestBody imageRequest: ImageRequest): JsonNode? {
        val base64Image = imageRequest.image.substringAfter("base64,")
        val result = imageHandlingService.uploadImage(base64Image)
        return result
    }

    @PutMapping("/Image/{imageId}")
    fun editData(@PathVariable imageId: String, @RequestBody editDataRequest: EditDataRequest): Workbook? {
        val result = imageHandlingService.editData(imageId, editDataRequest.data)
        return result
    }

    @GetMapping("/Image/{imageId}")
    fun getDataByImageId(@PathVariable imageId: String): Workbook? {
        val result = imageHandlingService.getDataByImageId(imageId)
        return result
    }

}