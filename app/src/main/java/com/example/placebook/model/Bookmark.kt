package com.example.placebook.model

import android.content.Context
import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.placebook.util.FileUtils
import com.example.placebook.util.ImageUtils

@Entity
data class Bookmark(@PrimaryKey(autoGenerate = true) var id: Long? = null,
                    var placeId: String? = null,
                    var name: String = "",
                    var address: String = "",
                    var latitude: Double = 0.0,
                    var longitude: Double = 0.0,
                    var phone: String = "",
                    var notes: String = "",
                    var category: String = ""){

    fun setImage(image: Bitmap, context: Context){
        id?.let{
            ImageUtils.saveBitmapToFile(context, image, generateImageFileName(it))
        }
    }

    companion object {
        fun generateImageFileName(id: Long): String {
            return "bookmark$id.png"
        }
    }

    fun deleteImage(context: Context){
        id?.let { FileUtils.deleteFile(context, generateImageFileName(it)) }
    }
}