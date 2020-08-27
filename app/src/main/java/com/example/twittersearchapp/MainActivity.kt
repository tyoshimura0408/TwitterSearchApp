package com.example.twittersearchapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class MainActivity : AppCompatActivity() {

    private val consumerKey = "YivLs9XTJEoeFjsVQy6xNLuYA"
    private val consumerSecret = "IKF7eXbvlv88MZ59unRL2zwT7W9cD8XhLkgo2FZD5Il5ig7lEj"
    private val coroutineScope = CoroutineScope(Job() + Dispatchers.IO)
    private val mainCoroutine = CoroutineScope(Job() + Dispatchers.Main)
    private lateinit var getAccessTokenResult: GetAccessTokenResult
    private lateinit var service: TwitterAPIService
    val tweetStatuses = ArrayList<Status>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val credential = "$consumerKey:$consumerSecret"
        val base64Str = Base64.getEncoder().encodeToString(credential.toByteArray())

        val okHttpClient = provideOkHttpClient()
        val retrofit = provideRetrofit(okHttpClient)
        service = retrofit.create(TwitterAPIService::class.java)
        coroutineScope.launch {
            val response = service.getAccessToken("Basic $base64Str", "client_credentials")
            if (response.isSuccessful) {
                getAccessTokenResult = response.body()!!
                searchTweets("test")
            } else {
                // TODO: error handling
            }
        }
    }

    private fun searchTweets(keyword: String) {
        coroutineScope.launch {
            val response = service.searchTweets("Bearer ${getAccessTokenResult.access_token}", keyword, 25)
            if (response.isSuccessful) {
                val body = response.body() as SearchTweetsResult
                tweetStatuses.addAll(body.statuses)
                mainCoroutine.launch {
                    findViewById<ListView>(R.id.list).adapter = TweetAdapter(applicationContext, tweetStatuses)
                }
            }
        }
    }

    private fun provideOkHttpClient() = OkHttpClient().newBuilder()
        .addInterceptor(HttpLoggingInterceptor().also {
            it.level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private fun provideRetrofit(okHttpClient: OkHttpClient) = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("https://api.twitter.com/")
        .client(okHttpClient)
        .build()

    private class TweetAdapter(val context: Context, val tweetStatuses: ArrayList<Status>): BaseAdapter() {
        override fun getCount(): Int {
            return tweetStatuses.size
        }
        override fun getItem(p0: Int): Any {
            return tweetStatuses[p0]
        }
        override fun getItemId(p0: Int): Long {
            return tweetStatuses[p0].id_str.toLong()
        }
        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
            val viewHolder: ViewHolder
            val convertedView: View
            if (p1 == null) {
                val view = LayoutInflater.from(context).inflate(R.layout.list_item, p2, false)
                viewHolder = ViewHolder(
                    view.findViewById(R.id.text), view.findViewById(R.id.name), view.findViewById(R.id.image))
                view.tag = viewHolder
                convertedView = view
            } else {
                viewHolder = p1.tag as ViewHolder
                convertedView = p1
            }
            Picasso.get().load(tweetStatuses[p0].user.profile_image_url_https).into(viewHolder.image)
            viewHolder.name.text = tweetStatuses[p0].user.name
            viewHolder.text.text = tweetStatuses[p0].text
            return convertedView
        }
    }

    private class ViewHolder(
        val text: TextView,
        val name: TextView,
        val image: ImageView
    )
}