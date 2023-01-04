package com.yunushamod.android.photogallery.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.yunushamod.android.photogallery.com.yunushamod.android.photogallery.models.GalleryItem
import com.yunushamod.android.photogallery.com.yunushamod.android.photogallery.services.QueryPreference
import com.yunushamod.android.photogallery.services.FlickrFetcher

class PhotoGalleryFragmentViewModel(private val app: Application): AndroidViewModel(app) {
    val galleryItemsLiveData: LiveData<List<GalleryItem>>
    private val flickrFetcher = FlickrFetcher.get()
    private var mutableSearchTerm: MutableLiveData<String> = MutableLiveData()
    init {
        mutableSearchTerm.value = QueryPreference.getStoredQuery(app)
        galleryItemsLiveData = Transformations.switchMap(mutableSearchTerm){
            if(it.isBlank()) flickrFetcher.fetchContents()
            else flickrFetcher.searchPhotos(it)
        }
    }
    val searchTerm: String
    get() = mutableSearchTerm.value ?: ""
    fun searchPhotos(query: String = ""){
        QueryPreference.setStoredQuery(app, query)
        mutableSearchTerm.value = query
    }
}