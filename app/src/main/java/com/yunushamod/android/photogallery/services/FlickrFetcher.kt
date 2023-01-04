package com.yunushamod.android.photogallery.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.yunushamod.android.photogallery.com.yunushamod.android.photogallery.models.FlickrResponse
import com.yunushamod.android.photogallery.com.yunushamod.android.photogallery.models.GalleryItem
import com.yunushamod.android.photogallery.com.yunushamod.android.photogallery.services.PhotoInterceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FlickrFetcher private constructor(context: Context) {
    private val flickrApi: FlickrApi
    init {
        val client = OkHttpClient.Builder()
            .addInterceptor(PhotoInterceptor())
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        flickrApi = retrofit.create(FlickrApi::class.java)
    }

    fun fetchPhotosRequest(): Call<FlickrResponse> = flickrApi.fetchPhotos()
    fun searchPhotosRequest(search: String): Call<FlickrResponse> = flickrApi.searchPhotos(search)
    fun fetchContents(): LiveData<List<GalleryItem>>
        = fetchPhotoMetadata(fetchPhotosRequest())
    fun searchPhotos(query: String): LiveData<List<GalleryItem>>
        = fetchPhotoMetadata(searchPhotosRequest(query))

    @WorkerThread
    fun fetchPhoto(url: String): Bitmap?{
        val response = flickrApi.fetchUrlBytes(url).execute()
        val bitmap = response.body()?.byteStream()?.use(BitmapFactory::decodeStream)
        Log.i(TAG, "Decoded bitmap=$bitmap from url=$url")
        return bitmap
    }

    private fun fetchPhotoMetadata(flickrRequest: Call<FlickrResponse>): LiveData<List<GalleryItem>>{
        var responseLiveResponse = MutableLiveData<List<GalleryItem>>()
        flickrRequest.enqueue(object: Callback<FlickrResponse>{
            override fun onResponse(call: Call<FlickrResponse>, response: Response<FlickrResponse>) {
                Log.d(TAG, "Response received")
                val flickrResponse = response.body()
                val photoResponse = flickrResponse?.photos
                var galleryItems = photoResponse?.galleryItems ?: mutableListOf()
                galleryItems = galleryItems.filterNot {
                    it.url.isBlank()
                }
                responseLiveResponse.value = galleryItems
            }

            override fun onFailure(call: Call<FlickrResponse>, t: Throwable) {
                Log.e(TAG, "Couldn't load photos", t)
                responseLiveResponse.value = emptyList()
            }
        })
        return responseLiveResponse
    }

    companion object{
        private const val TAG: String = "FlickrFetcher"
        private var INSTANCE: FlickrFetcher? = null
        fun initialize(context: Context){
            if(INSTANCE == null){
                INSTANCE = FlickrFetcher(context)
            }
        }
        fun get(): FlickrFetcher{
            return INSTANCE ?: throw java.lang.IllegalStateException("${FlickrFetcher::class.simpleName} must be initialized")
        }
    }
}