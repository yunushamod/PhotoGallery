package com.yunushamod.android.photogallery.com.yunushamod.android.photogallery.models

import com.google.gson.annotations.SerializedName

class PhotoResponse {
    @SerializedName("photo")
    lateinit var galleryItems: List<GalleryItem>
}