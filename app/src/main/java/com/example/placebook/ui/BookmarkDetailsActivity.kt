package com.example.placebook.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.placebook.R
import com.example.placebook.viewmodel.BookmarkDetailsViewModel
import kotlinx.android.synthetic.main.activity_bookmark_details.*

class BookmarkDetailsActivity : AppCompatActivity() {
    private lateinit var bookmarkDetailsViewmodel: BookmarkDetailsViewModel
    private var bookmarkDetailsView: BookmarkDetailsViewModel.BookmarkDetailsView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark_details)
        setUpToolbar()
        setupViewModel()
        getIntentData()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_bookmark_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_save -> {
                saveChanges()
                return true
            }
            else-> return super.onOptionsItemSelected(item)
        }
    }

    private fun setUpToolbar(){
        setSupportActionBar(toolbar as Toolbar)
    }

    private fun setupViewModel(){
        bookmarkDetailsViewmodel = ViewModelProviders.of(this).
            get(BookmarkDetailsViewModel::class.java)
    }

    private fun populateFields(){
        bookmarkDetailsView?.let {bookmarkDetailsView ->
            editTextName.setText(bookmarkDetailsView.name)
            editTextPhone.setText(bookmarkDetailsView.phone)
            editTextAddress.setText(bookmarkDetailsView.address)
            editTextNotes.setText(bookmarkDetailsView.notes)
        }
    }

    private fun populateImageView(){
        bookmarkDetailsView?.let { bookmarkDetailsView ->
            val placeImage = bookmarkDetailsView.getImage(this)
            placeImage?.let { imageViewPlace.setImageBitmap(it) }
        }
    }

    private fun getIntentData(){
        val bookmarkId = intent.getLongExtra(MapsActivity.EXTRA_BOOKMARK_ID, 0)
        bookmarkDetailsViewmodel.getBookmark(bookmarkId)?.observe(this,
            Observer<BookmarkDetailsViewModel.BookmarkDetailsView> {
            it?.let {
                bookmarkDetailsView = it
                populateFields()
                populateImageView()
            }
        })
    }

    private fun saveChanges(){
        val name = editTextName.text.toString()
        if (name.isEmpty()){
            return
        }
        bookmarkDetailsView?.let { bookmarkDetailsView ->
            bookmarkDetailsView.name = editTextName.text.toString()
            bookmarkDetailsView.phone = editTextPhone.text.toString()
            bookmarkDetailsView.address = editTextAddress.text.toString()
            bookmarkDetailsView.notes = editTextNotes.text.toString()
            bookmarkDetailsViewmodel.updateBookmark(bookmarkDetailsView)
        }
        finish()
    }
}
