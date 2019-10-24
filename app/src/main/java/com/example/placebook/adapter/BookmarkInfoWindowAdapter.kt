package com.example.placebook.adapter

import android.app.Activity
import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.placebook.R
import com.example.placebook.ui.MapsActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class BookmarkInfoWindowAdapter(context: Activity): GoogleMap.InfoWindowAdapter {
    private val contents: View

    init {
        contents = context.layoutInflater.inflate(R.layout.content_bookmark_info, null)
    }

    override fun getInfoContents(p0: Marker): View? {
        val titleView = contents.findViewById<TextView>(R.id.title)
        titleView.text = p0.title ?: ""

        val imageView = contents.findViewById<ImageView>(R.id.photo)
        imageView.setImageBitmap((p0.tag as MapsActivity.PlaceInfo).image)

        val titlePhone = contents.findViewById<TextView>(R.id.phone)
        titlePhone.text = p0.snippet ?: ""

        return contents
     }

    override fun getInfoWindow(p0: Marker?): View? {
        return null
    }
}