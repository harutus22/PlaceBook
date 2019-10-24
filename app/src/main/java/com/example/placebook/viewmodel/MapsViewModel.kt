package com.example.placebook.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.placebook.model.Bookmark
import com.example.placebook.repository.BookmarkRepo
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
            Log.i(TAG, "New bookmark $newId has been added to the database")
        }
    }

    private fun bookmarkToMArkerView(bookMark: Bookmark): MapsViewModel.BookmarkMarkerView{
        return BookmarkMarkerView(bookMark.id, LatLng(bookMark.latitude, bookMark.longitude))
    }

    private fun mapBookMarksToMarkerView(){
        bookmarks = Transformations.map(bookmarkRepo.allBookmarks){
            bookmarkToMArkerView(it)
        }
    }

    data class BookmarkMarkerView(var id: Long? = null, var location: LatLng = LatLng(0.0, 0.0))
}