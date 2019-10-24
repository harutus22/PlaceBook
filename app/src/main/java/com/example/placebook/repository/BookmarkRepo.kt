package com.example.placebook.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.placebook.model.Bookmark
import com.example.placebook.room.PlaceBookDatabase

class BookmarkRepo(context: Context) {
    private var database = PlaceBookDatabase.getInstance(context)
    private var bookmarkDao = database.bookMarkDao()

    fun addBookmark(bookmark: Bookmark): Long?{
        val newId = bookmarkDao.insertBookmark(bookmark)
        bookmark.id = newId
        return newId
    }

    fun createBookmark(): Bookmark = Bookmark()

    val allBookmarks: LiveData<List<Bookmark>>
    get(){
        return bookmarkDao.getAll()
    }
}