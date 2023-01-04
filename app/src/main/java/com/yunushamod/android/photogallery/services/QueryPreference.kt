package com.yunushamod.android.photogallery.com.yunushamod.android.photogallery.services

import android.content.Context
import android.preference.PreferenceManager


private const val QUERY_STRING = "QUERY_STRING"
private const val PREF_LAST_RESULT_ID = "lastResultId"
private const val PREF_POLLING_REQUIRED = "prefPollingRequired"
object QueryPreference {
    fun getStoredQuery(context: Context): String{
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(QUERY_STRING, "")!!
    }

    fun setStoredQuery(context: Context, query: String){
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(QUERY_STRING, query)
            .apply()
    }

    fun getLastResultId(context: Context): String{
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(PREF_LAST_RESULT_ID, "")!!
    }

    fun setLastResultId(context: Context, lastResultId: String){
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(PREF_LAST_RESULT_ID, lastResultId)
            .apply()
    }

    fun isPolling(context: Context): Boolean{
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(PREF_POLLING_REQUIRED, false)!!
    }

    fun setPollingPreference(context: Context, wantsPooling: Boolean){
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(PREF_POLLING_REQUIRED, wantsPooling)
            .apply()
    }


}