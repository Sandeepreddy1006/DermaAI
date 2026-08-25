package com.simats.dermacareai.network.models

data class LoginResponse(
    val access_token: String,
    val token_type: String
)

data class UserResponse(
    val id: Int,
    val email: String,
    val full_name: String,
    val avatar_url: String? = null
)

data class AnalysisResponse(
    val id: Int,
    val result_title: String,
    val result_description: String,
    val confidence_score: Int,
    val precautions: String?,
    val first_aid: String?,
    val image_url: String,
    val created_at: String
)

data class HelpResponse(
    val title: String,
    val content: List<HelpItem>
)

data class HelpItem(
    val q: String,
    val a: String
)

data class PrivacyResponse(
    val title: String,
    val content: String
)
