package com.santos.llamadas.viewmodel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.santos.llamadas.R
import com.santos.llamadas.model.PhotoData

class PhotoAdapter(private var photoList:List<PhotoData>,
                   private val onClickListener:(PhotoData) -> Unit,
                   private val onDeleteListener:(Int) -> Unit): RecyclerView.Adapter<PhotoViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return  PhotoViewHolder(layoutInflater.inflate(R.layout.item_photos,parent,false))
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val item = photoList[position]
        holder.render(item,onClickListener, onDeleteListener)
    }

    override fun getItemCount(): Int {
        return photoList.size
    }
}