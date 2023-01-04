package com.yunushamod.android.photogallery.com.yunushamod.android.photogallery.controllers

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.fragment.app.Fragment
import com.yunushamod.android.photogallery.com.yunushamod.android.photogallery.helpers.PollWorker

abstract class VisibleFragment: Fragment() {
    private val onShowNotification = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            Log.i(TAG, "cancelling notification")
            resultCode = Activity.RESULT_CANCELED
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(PollWorker.ACTION_SHOW_NOTIFICATION)
        requireActivity().registerReceiver(onShowNotification,
        filter, PollWorker.PERM_PRIVATE, null)
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(onShowNotification)
    }

    companion object{
        private const val TAG = "VisibleFrgment"
    }
}