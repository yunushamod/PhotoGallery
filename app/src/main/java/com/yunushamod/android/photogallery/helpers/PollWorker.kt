package com.yunushamod.android.photogallery.com.yunushamod.android.photogallery.helpers

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.yunushamod.android.photogallery.PhotoGalleryApplication.Companion.NOTIFICATION_CHANNEL_ID
import com.yunushamod.android.photogallery.R
import com.yunushamod.android.photogallery.com.yunushamod.android.photogallery.models.GalleryItem
import com.yunushamod.android.photogallery.com.yunushamod.android.photogallery.services.QueryPreference
import com.yunushamod.android.photogallery.controllers.PhotoGalleryActivity
import com.yunushamod.android.photogallery.services.FlickrFetcher

class PollWorker(private val context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {
    private val flickrFetcher = FlickrFetcher.get()
    override fun doWork(): Result {
        val query = QueryPreference.getStoredQuery(context)
        val lastResultId = QueryPreference.getLastResultId(context)
        val items: List<GalleryItem> = if(query.isEmpty()){
            flickrFetcher.fetchPhotosRequest().execute()
                .body()?.photos?.galleryItems
        }else {
            flickrFetcher.searchPhotosRequest(query)
                .execute().body()?.photos?.galleryItems
        } ?: emptyList()
        if(items.isEmpty()) return Result.success()
        val resultId = items.first().id
        if(resultId == lastResultId) Log.i(TAG, "Got an old result: $resultId")
        else {
            Log.i(TAG, "Got a new result: $resultId")
            QueryPreference.setLastResultId(context, resultId)
            val intent = PhotoGalleryActivity.newIntent(context)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            val resources = context.resources
            val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setTicker(resources.getString(R.string.new_pictures_title))
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(resources.getString(R.string.new_pictures_title))
                .setContentText(resources.getString(R.string.new_pictures_text))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
            showBackgroundNotification(0, notification)
        }
        return Result.success()
    }

    private fun showBackgroundNotification(requestCode: Int, notification: Notification){
        val intent = Intent(ACTION_SHOW_NOTIFICATION).apply {
            putExtra(REQUEST_CODE, requestCode)
            putExtra(NOTIFICATION, notification)
        }
        context.sendOrderedBroadcast(intent, PERM_PRIVATE)
    }
    companion object{
        private const val TAG: String = "PollWorker"
        const val ACTION_SHOW_NOTIFICATION = "com.yunushamod.android.SHOW_NOTIFICATION"
        const val PERM_PRIVATE = "com.yunushamod.android.photogallery.PRIVATE"
        const val REQUEST_CODE = "REQUEST_CODE"
        const val NOTIFICATION = "NOTIFICATION"
    }
}