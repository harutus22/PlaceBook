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

    fun getLiveBookmark(bookmarkId: Long): LiveData<Bookmark>{
        return bookmarkDao.loadLiveBookmark(bookmarkId)
    }

    fun createBookmark(): Bookmark = Bookmark()

    fun updateBookmark(bookmark: Bookmark){
        bookmarkDao.update(bookmark)
    }

    fun getBookmark(bookmarkId: Long): Bookmark{
        return bookmarkDao.loadBookmark(bookmarkId)
    }

    val allBookmarks: LiveData<List<Bookmark>>
    get(){
        return bookmarkDao.getAll()
    }
}