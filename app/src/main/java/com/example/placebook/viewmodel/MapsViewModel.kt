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
    private var bookmarks: LiveData<List<BookmarkMarkerView>>? = null

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

            val newId = bookmarkRepo.addBookmark(bookmark)
            image?.let{
                bookmark.setImage(it, getApplication())
            }
            Log.i(TAG, "New bookmark $newId has been added to the database")
        }
    }

    private fun bookmarkToMarkerView(bookMark: Bookmark): BookmarkMarkerView{
        return BookmarkMarkerView(bookMark.id, LatLng(bookMark.latitude, bookMark.longitude),
            bookMark.name, bookMark.phone)
    }

    private fun mapBookmarksToMarkerView(){
        bookmarks = Transformations.map(bookmarkRepo.allBookmarks){ bookmarkRepo ->
            bookmarkRepo.map {bookmark ->
                bookmarkToMarkerView(bookmark)
            }
        }
    }

    fun getBookmarkMarkerViews(): LiveData<List<BookmarkMarkerView>>?{
        if(bookmarks == null)
            mapBookmarksToMarkerView()
        return bookmarks
    }

    data class BookmarkMarkerView(var id: Long? = null, var location: LatLng = LatLng(0.0, 0.0),
                                  var name: String = "", var phone: String = ""){
        fun getImage(context: Context): Bitmap?{
            id?.let {
                return ImageUtils.loadImageFromFile(context, Bookmark.generateImageFileName(it))
            }
            return null
        }
    }
}