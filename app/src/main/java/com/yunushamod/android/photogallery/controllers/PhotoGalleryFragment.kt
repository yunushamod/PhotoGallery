package com.yunushamod.android.photogallery.controllers

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.squareup.picasso.Picasso
import com.yunushamod.android.photogallery.R
import com.yunushamod.android.photogallery.com.yunushamod.android.photogallery.controllers.VisibleFragment
import com.yunushamod.android.photogallery.com.yunushamod.android.photogallery.helpers.PollWorker
import com.yunushamod.android.photogallery.com.yunushamod.android.photogallery.models.GalleryItem
import com.yunushamod.android.photogallery.com.yunushamod.android.photogallery.services.QueryPreference
import com.yunushamod.android.photogallery.viewmodels.PhotoGalleryFragmentViewModel
import java.util.concurrent.TimeUnit

class PhotoGalleryFragment: VisibleFragment() {
    private lateinit var recyclerView: RecyclerView
    //private lateinit var thumbnailDownloader: ThumbnailDownloader<PhotoViewHolder>
    private val viewModel: PhotoGalleryFragmentViewModel by lazy {
        ViewModelProvider(this)[PhotoGalleryFragmentViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .build()
        val workerRequest = OneTimeWorkRequest.Builder(PollWorker::class.java)
            .setConstraints(constraints)
            .build()
        context?.let { WorkManager.getInstance(it).enqueue(workerRequest) }

        //retainInstance = true
//        val responseHandler = Handler()
//        thumbnailDownloader = ThumbnailDownloader(responseHandler){
//                photoHolder: PhotoViewHolder, bitmap ->
//            val drawable = BitmapDrawable(resources, bitmap)
//            photoHolder.bindDrawable(drawable)
//        }
//        lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //viewLifecycleOwner.lifecycle.addObserver(thumbnailDownloader.viewLifecycleObserver)
        val view = inflater.inflate(R.layout.fragment_photo_gallery, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(context, 3)
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_photo_gallery, menu)
        val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
        val searchView = searchItem.actionView as SearchView
        searchView.apply {
            setOnQueryTextListener(object: SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    Log.d(TAG, "QueryTextSubmit: $query")
                    query?.let {
                        viewModel.searchPhotos(it)
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    Log.d(TAG, "QueryTextChange: $newText")
                    return false
                }
            })
            setOnSearchClickListener {
                searchView.setQuery(viewModel.searchTerm, false)
            }
        }
        val toggleItem = menu.findItem(R.id.menu_item_toggle_polling)
        val isPolling = QueryPreference.isPolling(requireContext())
        val toggleItemTitle = if (isPolling) {
            R.string.stop_polling
        } else {
            R.string.start_polling
        }
        toggleItem.setTitle(toggleItemTitle)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.menu_item_clear -> {
                viewModel.searchPhotos("")
                true
            }
            R.id.menu_item_toggle_polling -> {
                val isPolling = QueryPreference.isPolling(requireContext())
                if (isPolling) {
                    WorkManager.getInstance().cancelUniqueWork(POLL_WORK)
                    QueryPreference.setPollingPreference(requireContext(), false)
                } else {
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.UNMETERED)
                        .build()
                    val periodicRequest = PeriodicWorkRequest
                        .Builder(PollWorker::class.java, 15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build()
                    WorkManager.getInstance().enqueueUniquePeriodicWork(POLL_WORK,
                        ExistingPeriodicWorkPolicy.KEEP,
                        periodicRequest)
                    QueryPreference.setPollingPreference(requireContext(), true)
                }
                activity?.invalidateOptionsMenu()
                return true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.galleryItemsLiveData.observe(viewLifecycleOwner){
            Log.d(TAG, "Got ${it.size} photos")
            updateUI(it)
        }
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        lifecycle.removeObserver(thumbnailDownloader.fragmentLifecycleObserver)
//    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//        viewLifecycleOwner.lifecycle.removeObserver(thumbnailDownloader.viewLifecycleObserver)
//    }

    private fun updateUI(galleryItems: List<GalleryItem>){
        recyclerView.adapter = PhotoAdapter(galleryItems)
    }

    private inner class PhotoAdapter(private val photoList: List<GalleryItem>) : RecyclerView.Adapter<PhotoViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
            val view = layoutInflater.inflate(R.layout.list_item_gallery, parent, false) as ImageView
            return PhotoViewHolder(view)
        }

        override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
            val galleryItem = photoList[position]
            holder.bindGalleryItem(galleryItem)
        }

        override fun getItemCount(): Int = photoList.size
    }

    private inner class PhotoViewHolder(private val imageView: ImageView) : RecyclerView.ViewHolder(imageView),
    View.OnClickListener{
        init {
            itemView.setOnClickListener(this)
        }
        private lateinit var galleryItem: GalleryItem
        val bindDrawable : (Drawable?) -> Unit = imageView::setImageDrawable
        fun bindGalleryItem(galleryItem: GalleryItem){
            this.galleryItem = galleryItem
            Picasso.get()
                .load(galleryItem.url)
                .placeholder(R.drawable.ic_image_placeholder)
                .into(imageView)
        }

        override fun onClick(p0: View?) {
//            val intent = Intent(Intent.ACTION_VIEW, galleryItem.photoPageUri)
//            startActivity(intent)
            CustomTabsIntent.Builder()
                .setToolbarColor(ContextCompat.getColor(requireContext(), R.color.purple_500))
                .setShowTitle(true)
                .build()
                .launchUrl(requireContext(), galleryItem.photoPageUri)
        }
    }
    companion object{
        private const val TAG: String = "PhotoGalleryFragment"
        private const val POLL_WORK = "pollWork"
        fun newInstance(): PhotoGalleryFragment{
            return PhotoGalleryFragment()
        }
    }
}