package com.yunushamod.android.photogallery.com.yunushamod.android.photogallery.services

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.yunushamod.android.photogallery.services.FlickrFetcher
import java.util.concurrent.ConcurrentHashMap



class ThumbnailDownloader<in T : Any>(private val responseHandler: Handler,
                                      private val onThumbnailDownload: (T, Bitmap) -> Unit) : HandlerThread(TAG), LifecycleObserver {
    private var hasQuit = false
    private lateinit var requestHandler: Handler
    private val requestMap = ConcurrentHashMap<T, String>()
    private val flickrFetcher = FlickrFetcher.get()
    val fragmentLifecycleObserver = object : LifecycleObserver{
        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        fun setUp(){
            Log.i(TAG, "Starting background thread")
            start()
            looper
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun tearDown(){
            Log.i(TAG, "Destroying background thread")
            quit()
        }
    }

    val viewLifecycleObserver = object : LifecycleObserver{
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun clearQueue(){
            Log.i(TAG, "Clearing all messages on queue")
            requestHandler.removeMessages(MESSAGE_DOWNLOAD)
            requestMap.clear()
        }
    }

    override fun quit(): Boolean {
        hasQuit = true
        return super.quit()
    }

    fun queueThumbnail(target: T, url: String){
        Log.i(TAG, "Got a url")
        requestMap[target] = url
        requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target)
            .sendToTarget()
    }

    @Suppress("UNCHECKED_CAST")
    @SuppressLint("HandlerLeak")
    override fun onLooperPrepared() {
        requestHandler = object: Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                if(msg.what == MESSAGE_DOWNLOAD){
                    val target = msg.obj as T
                    handleRequest(target)
                }
            }
        }
    }

    private fun handleRequest(target: T){
        val url = requestMap[target] ?: return
        val bitmap = flickrFetcher.fetchPhoto(url) ?: return
        responseHandler.post{
            if(requestMap[target] != url || hasQuit){
                return@post
            }
            requestMap.remove(target)
            onThumbnailDownload(target, bitmap)
        }
    }

    companion object{
        const val MESSAGE_DOWNLOAD = 0
        private const val TAG = "ThumbnailDownloader"
    }
}