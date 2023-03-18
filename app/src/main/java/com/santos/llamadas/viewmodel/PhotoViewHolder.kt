package com.santos.llamadas.viewmodel

import android.view.View
import androidx.lifecycle.ViewTreeViewModelStoreOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.santos.llamadas.databinding.ItemPhotosBinding
import com.santos.llamadas.model.PhotoData

class PhotoViewHolder(view: View):RecyclerView.ViewHolder(view) {
    private var binding = ItemPhotosBinding.bind(view)

    fun render(
        photoModel: PhotoData,
        onClickListener: (PhotoData) -> Unit,
        onDeleteListener: (Int) -> Unit
    ){

        Glide.with(binding.ivPhoto.context).load(photoModel.photo).into(binding.ivPhoto)

        itemView.setOnClickListener{
            onClickListener(photoModel)
        }

        binding.btnSelected.setOnClickListener {
            onDeleteListener(adapterPosition)
        }

    }
}