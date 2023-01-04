package com.yunushamod.android.photogallery.com.yunushamod.android.photogallery.services

import okhttp3.Interceptor
import okhttp3.Response

class PhotoInterceptor : Interceptor{
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val newUrl = originalRequest.url().newBuilder()
            .addQueryParameter("api_key", API_KEY)
            .addQueryParameter("format", "json")
            .addQueryParameter("nojsoncallback", "1")
            .addQueryParameter("extras", "url_s")
            .addQueryParameter("safesearch", "1")
            .build()
        val newRequest = originalRequest.newBuilder().url(newUrl)
            .build()
        return chain.proceed(newRequest)
    }

    companion object{
        private const val API_KEY = "6d5b94b84e6ba3e218f21cdbf5a27c37"
    }
}