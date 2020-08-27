package com.example.twittersearchapp

data class SearchTweetsResult(
    val statuses: Array<Status>
)

data class Status(
    val id_str: String,
    val text: String,
    val user: User
)

data class User(
    val name: String,
    val profile_image_url_https: String
)