package com.example.placebook.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
import java.net.URLEncoder

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
            val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            val intentActivities = packageManager.queryIntentActivities(captureIntent, PackageManager.MATCH_DEFAULT_ONLY)
            intentActivities.map { it.activityInfo.packageName }.forEach { grantUriPermission(it, photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION) }
            startActivityForResult(captureIntent, REQUEST_CAPTURE_IMAGE)
            }
        }


    override fun onPickClick() {
        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickIntent, REQUEST_GALLERY_IMAGE)
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
        setupFab()
    }

    private fun setupFab(){
        fab.setOnClickListener {
            sharePlace()
        }
    }

    private fun replaceImage(){
        val newFragment = PhotoOptionDialogFragment.newInstance(this)
        newFragment?.show(supportFragmentManager, "photoOptionDialog")
    }

    private fun getImageWithAuthority(uri: Uri): Bitmap?{
        return ImageUtils.decodeUriStreamToSize(uri,
            resources.getDimensionPixelSize(R.dimen.default_image_width),
            resources.getDimensionPixelSize(R.dimen.default_image_height), this)
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
            R.id.action_delete->{
                deleteBookmark()
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

    private fun sharePlace(){
        val bookmarkView = bookmarkDetailsView ?: return

        var mapUrl = ""
        if (bookmarkView.placeId == null){
            val location = URLEncoder.encode("${bookmarkView.latitude},${bookmarkView.longitude}","utf-8")
            mapUrl = "https://www.google.com/maps/dir/?api=1&destination=$location"
        } else {
            val name = URLEncoder.encode(bookmarkView.name, "utf-8")
            mapUrl = "https://www.google.com/maps/dir/?api=1&destination=$name&destination_place_id=${bookmarkView.placeId}"
        }
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out${bookmarkView.name} at :\n$mapUrl")
        sendIntent.putExtra(Intent.EXTRA_SUBJECT,  "Sharing ${bookmarkView.name}")

        sendIntent.type = "text/plain"
        startActivity(sendIntent)
    }

    private fun getIntentData(){
        val bookmarkId = intent.getLongExtra(MapsActivity.EXTRA_BOOKMARK_ID, 0)
        bookmarkDetailsViewmodel.getBookmark(bookmarkId)?.observe(this,
            Observer<BookmarkDetailsViewModel.BookmarkDetailsView> {
            it?.let {
                bookmarkDetailsView = it
                populateFields()
                populateImageView()
                populateCategoryList()
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
            bookmarkDetailsView.category = spinnerCategory.selectedItem as String
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

    private fun populateCategoryList(){
        val bookmarkView = bookmarkDetailsView ?: return
        val resourceId = bookmarkDetailsViewmodel.getCategoryResourceId(bookmarkView.category)

        resourceId?.let { imageViewCategory.setImageResource(it) }

        val categories = bookmarkDetailsViewmodel.getCategories()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
        val placeCategory = bookmarkView.category
        spinnerCategory.setSelection(adapter.getPosition(placeCategory))
        spinnerCategory.post {
            spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val category = parent?.getItemAtPosition(position) as String
                    val resourceId = bookmarkDetailsViewmodel.getCategoryResourceId(category)
                    resourceId?.let { imageViewCategory.setImageResource(it) }
                }

            }
        }
    }

    private fun deleteBookmark(){
        val bookmarkView = bookmarkDetailsView ?: return
        AlertDialog.Builder(this).setMessage("Delete?").setPositiveButton("Ok"){_, _ ->
            bookmarkDetailsViewmodel.deleteBookmark(bookmarkView)
            finish()
        }.setNegativeButton("Cancel", null).create().show()
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
                REQUEST_GALLERY_IMAGE -> {
                    if (data != null && data.data != null){
                        val imageUri = data.data!!
                        val image = getImageWithAuthority(imageUri)
                        image?.let { updateImage(image) }
                    }
                }
            }
        }
    }

    companion object{
        private const val REQUEST_CAPTURE_IMAGE = 1
        private const val REQUEST_GALLERY_IMAGE = 2
    }
}
