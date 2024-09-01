package com.capstoneproject.kotlin

data class DataClass(
    val dataTitle: String,
    val dataDesc: Int,
    val dataLang: String,
    val dataImage: Int,
    val rating: Float = 0f,
    val comment: String = ""
)
