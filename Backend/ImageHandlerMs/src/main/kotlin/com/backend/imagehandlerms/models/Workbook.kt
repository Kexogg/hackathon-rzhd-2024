package com.backend.imagehandlerms.models

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "workbooks")
data class Workbook(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "data", length = 1000)
    var data: String,

    @Column(name = "accuracy")
    val accuracy: Float,

    @Column(name = "s3_link")
    val s3Link: String,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    constructor() : this(0, "", 0.0f, "", LocalDateTime.now()) {

    }
}