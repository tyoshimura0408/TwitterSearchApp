package com.example.twittersearchapp

import retrofit2.Response
import retrofit2.http.*

interface TwitterAPIService {

    @FormUrlEncoded
    @POST("oauth2/token")
    suspend fun getAccessToken(
        @Header("Authorization") authorization: String,
        @Field("grant_type") grantType: String
    ): Response<GetAccessTokenResult>

    @GET("1.1/search/tweets.json")
    suspend fun searchTweets(
        @Header("Authorization") authorization: String,
        @Query("q") q: String,
        @Query("count") count: Int
    ): Response<SearchTweetsResult>

    @GET("1.1/search/tweets.json")
    suspend fun searchTweetsByMaxId(
        @Header("Authorization") authorization: String,
        @Query("q") q: String,
        @Query("count") count: Int,
        @Query("max_id") maxId: String
    ): Response<SearchTweetsResult>
}