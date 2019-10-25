package com.example.placebook.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.placebook.R
import com.example.placebook.util.ImageUtils
import com.example.placebook.viewmodel.BookmarkDetailsViewModel
import kotlinx.android.synthetic.main.activity_bookmark_details.*
import java.io.File
import java.io.IOException

class BookmarkDetailsActivity : AppCompatActivity(), PhotoOptionDialogFragment.PhotoOptionDialogListener {
    override fun onCaptureClick() {
        photoFile = null
        try {
            photoFile = ImageUtils.createUniqueImageFile(this)
        } catch (ex: IOException) {
            return
        }
        photoFile?.let { photoFile ->
            val photoUri = FileProvider.getUriForFile(this,"com.example.placebook.fileprovider", photoFile)
            val captureIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            captureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri)
            val intentActivities = packageManager.queryIntentActivities(captureIntent, PackageManager.MATCH_DEFAULT_ONLY)
            intentActivities.map { it.activityInfo.packageName }.forEach { grantUriPermission(it, photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION) }
            startActivityForResult(captureIntent, REQUEST_CAPTURE_IMAGE)
            }
        }


    override fun onPickClick() {
        Toast.makeText(this, "Gallery Pick", Toast.LENGTH_LONG).show()
    }

    private lateinit var bookmarkDetailsViewmodel: BookmarkDetailsViewModel
    private var bookmarkDetailsView: BookmarkDetailsViewModel.BookmarkDetailsView? = null
    private var photoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark_details)
        setUpToolbar()
        setupViewModel()
        getIntentData()
    }

    private fun replaceImage(){
        val newFragment = PhotoOptionDialogFragment.newInstance(this)
        newFragment?.show(supportFragmentManager, "photoOptionDialog")
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
        imageViewPlace.setOnClickListener { replaceImage() }
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

    private fun updateImage(image: Bitmap){
        val bookmarkView = bookmarkDetailsView ?: return
        imageViewPlace.setImageBitmap(image)
        bookmarkView.setImage(this, image)
    }

    private fun getImageWithPath(filePath: String): Bitmap?{
        return ImageUtils.decodeFileToSize(filePath,
            resources.getDimensionPixelSize(R.dimen.default_image_width),
            resources.getDimensionPixelSize(R.dimen.default_image_height))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            when(requestCode){
                REQUEST_CAPTURE_IMAGE -> {
                    val photoFile = photoFile ?: return
                    val uri = FileProvider.getUriForFile(this,
                        "com.example.placebook.fileprovider",
                        photoFile)
                    revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    val image = getImageWithPath(photoFile.absolutePath)
                    image?.let { updateImage(it) }
                }
            }
        }
    }

    companion object{
        private const val REQUEST_CAPTURE_IMAGE = 1
        private const val REQUEST_GALLERY_IMAGE = 2
    }
}
