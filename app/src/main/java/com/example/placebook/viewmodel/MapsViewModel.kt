package com.example.placebook.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.placebook.model.Bookmark
import com.example.placebook.repository.BookmarkRepo
import com.example.placebook.util.ImageUtils
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place

class MapsViewModel(app: Application) : AndroidViewModel(app){
    private val TAG = "MapsViewModel"
    private var bookmarks: LiveData<List<BookmarkView>>? = null

    private var bookmarkRepo: BookmarkRepo = BookmarkRepo(app)

    fun addBookmarkFromPlace(place: Place, image: Bitmap?){
        val bookmark = bookmarkRepo.createBookmark()
        bookmark.apply {
            placeId = place.id
            name = place.name.toString()
            longitude = place.latLng?.longitude ?: 0.0
            latitude = place.latLng?.latitude ?: 0.0
            phone = place.phoneNumber.toString()
            address = place.address.toString()

            bookmark.category = getPlaceCategory(place)

            val newId = bookmarkRepo.addBookmark(bookmark)
            image?.let{
                bookmark.setImage(it, getApplication())
            }
            Log.i(TAG, "New bookmark $newId has been added to the database")
        }
    }

    private fun bookmarkToBookmarkView(bookMark: Bookmark): BookmarkView{
        return BookmarkView(bookMark.id, LatLng(bookMark.latitude, bookMark.longitude),
            bookMark.name, bookMark.phone, bookmarkRepo.getCategoryResourceId(bookMark.category))
    }

    private fun mapBookmarksToBookmarkView(){
        bookmarks = Transformations.map(bookmarkRepo.allBookmarks){ bookmarkRepo ->
            bookmarkRepo.map {bookmark ->
                bookmarkToBookmarkView(bookmark)
            }
        }
    }

    fun getBookmarkViews(): LiveData<List<BookmarkView>>?{
        if(bookmarks == null)
            mapBookmarksToBookmarkView()
        return bookmarks
    }

    fun addBookmark(latLng: LatLng): Long?{
        val bookmark = bookmarkRepo.createBookmark()
        bookmark.name = "Untitled"
        bookmark.longitude = latLng.longitude
        bookmark.latitude = latLng.latitude
        bookmark.category = "Other"
        return bookmarkRepo.addBookmark(bookmark)
    }

    data class BookmarkView(var id: Long? = null, var location: LatLng = LatLng(0.0, 0.0),
                            var name: String = "", var phone: String = "",
                            var categoryResourceId: Int? = null){
        fun getImage(context: Context): Bitmap?{
            id?.let {
                return ImageUtils.loadImageFromFile(context, Bookmark.generateImageFileName(it))
            }
            return null
        }
    }

    private fun getPlaceCategory(place: Place): String{
        var category = "Other"
        val placeTypes = place.types

        if (placeTypes!!.size > 0){
            val placeCategory = placeTypes[0]
            category = bookmarkRepo.placeTypeToCategory(placeCategory)
        }
        return category
    }
}