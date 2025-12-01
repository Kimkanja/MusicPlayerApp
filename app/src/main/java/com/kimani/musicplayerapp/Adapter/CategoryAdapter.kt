package com.kimani.musicplayerapp.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // <-- 1. ADD THIS IMPORT for Glide
import com.kimani.musicplayerapp.databinding.CategoryItemRecyclerRowBinding
import com.kimani.musicplayerapp.models.CategoryModel // <-- 2. ADD THIS IMPORT for your model

class CategoryAdapter(private val categoryList: List<CategoryModel>) :
    RecyclerView.Adapter<CategoryAdapter.MyViewHolder>() {

    // Define the ViewHolder as an inner class
    class MyViewHolder(private val binding: CategoryItemRecyclerRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // This function will bind the data to the views in the layout
        fun bindData(category: CategoryModel) {
            binding.nameTextView.text = category.name
            // Use Glide to load the image from a URL
            Glide.with(binding.root).load(category.coverUrl)
                .into(binding.coverImageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // Inflate the layout for each item
        val binding = CategoryItemRecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // Get the data for the current position and bind it
        holder.bindData(categoryList[position])
    }

    override fun getItemCount(): Int {
        // Return the total number of items in the list
        return categoryList.size
    }
}
