package com.simats.dermacareai.models

import com.google.gson.annotations.SerializedName

data class Doctor(
    val id: Int,
    val name: String,
    val specialty: String,
    val rating: Float,
    val distance: String,
    @SerializedName("image_url")
    val imageUrl: String,
    val address: String
)
