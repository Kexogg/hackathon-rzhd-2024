package com.backend.imagehandlerms.repositories

import com.backend.imagehandlerms.models.Workbook
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface WorkbookRepository : JpaRepository<Workbook, Long> {
    @Query("SELECT w FROM Workbook w WHERE w.s3Link LIKE %:imageId%")
    fun findByImageId(@Param("imageId") imageId: String): Workbook?
}