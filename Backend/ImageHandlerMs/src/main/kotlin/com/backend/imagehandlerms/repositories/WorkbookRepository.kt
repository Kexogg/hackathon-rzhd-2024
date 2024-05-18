package com.backend.imagehandlerms.repositories

import com.backend.imagehandlerms.models.Workbook
import org.springframework.data.jpa.repository.JpaRepository

interface WorkbookRepository : JpaRepository<Workbook, Long> {
}