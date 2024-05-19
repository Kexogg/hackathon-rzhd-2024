package com.backend.imagehandlerms.controllers

import com.backend.imagehandlerms.models.Workbook
import com.backend.imagehandlerms.services.ImageHandlingService
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
    fun uploadImage(@RequestBody imageRequest: ImageRequest): ResponseEntity<Any> {
        return try {
            val base64Image = imageRequest.image.substringAfter("base64,")
            val result = imageHandlingService.uploadImage(base64Image)
            ResponseEntity.ok(result)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.message)
        }
    }

    @PutMapping("/image/{imageId}")
    fun editData(@PathVariable imageId: String, @RequestBody editDataRequest: EditDataRequest): ResponseEntity<Any> {
        return try {
            val result = imageHandlingService.editData(imageId, editDataRequest.data)
            ResponseEntity.ok(result)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.message)
        }
    }

    @GetMapping("/image/{imageId}")
    fun getDataByImageId(@PathVariable imageId: String): ResponseEntity<Any> {
        return try {
            val result = imageHandlingService.getDataByImageId(imageId)
            ResponseEntity.ok(result)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.message)
        }
    }

}