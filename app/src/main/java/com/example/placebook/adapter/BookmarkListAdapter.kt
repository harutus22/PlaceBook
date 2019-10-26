package com.example.placebook.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.placebook.R
import com.example.placebook.ui.MapsActivity
import com.example.placebook.viewmodel.MapsViewModel

class BookmarkListAdapter(private var bookmarkData: List<MapsViewModel.BookmarkView>?,
                          private val mapsActivity: MapsActivity): RecyclerView.Adapter<BookmarkListAdapter.ViewHolder>() {

    class ViewHolder(itemView: View, private val mapsActivity: MapsActivity): RecyclerView.ViewHolder(itemView){
        init {
            itemView.setOnClickListener {
                val bookmarkView = itemView.tag as MapsViewModel.BookmarkView
                mapsActivity.moveToBookmark(bookmarkView)
            }
        }
        val nameTextView: TextView = itemView.findViewById(R.id.bookmarkNameTextView) as TextView
        val categoryImageView: ImageView = itemView.findViewById(R.id.bookmarkIcon) as ImageView
    }

    fun setBookmarkData(bookmarks: List<MapsViewModel.BookmarkView>){
        this.bookmarkData = bookmarks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vh = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.bookmark_item,
            parent, false), mapsActivity)
        return vh
    }

    override fun getItemCount(): Int {
        return bookmarkData?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bookmarkData = bookmarkData ?: return
        val bookMarkViewData = bookmarkData[position]
        holder.itemView.tag = bookMarkViewData
        holder.nameTextView.text = bookMarkViewData.name
        bookMarkViewData.categoryResourceId?.
            let { holder.categoryImageView.setImageResource(it) }
    }
}