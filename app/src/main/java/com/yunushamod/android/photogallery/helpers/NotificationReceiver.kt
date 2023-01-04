package com.yunushamod.android.photogallery.com.yunushamod.android.photogallery.helpers

import android.app.Activity
import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        Log.i(TAG, "Received broadcast: ${p1?.action}")
        if(resultCode != Activity.RESULT_OK){
            return
        }
        val requestCode = p1?.getIntExtra(PollWorker.REQUEST_CODE, 0)
        val notification = p1?.getParcelableExtra<Notification>(PollWorker.NOTIFICATION)
        val notificationManager = p0?.let { NotificationManagerCompat.from(it)}
        notification?.let { notificationManager?.notify(requestCode ?: 0, it) }
    }
    companion object{
        private const val TAG = "NotificationReceiver"
    }
}