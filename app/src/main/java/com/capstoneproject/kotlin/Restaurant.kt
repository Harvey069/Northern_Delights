package com.capstoneproject.kotlin

import java.io.Serializable

data class Restaurant(
    val name: String = "",
    val location: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val id: String = "",
    val userCount: Int = 0,
    val totalRating: Float = 0.0f,
    val averageRating: Float = 0.0f
) : Serializable
