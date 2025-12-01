package com.kimani.musicplayerapp.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kimani.musicplayerapp.databinding.CategoryItemRecyclerRowBinding;
import com.kimani.musicplayerapp.models.CategoryModel;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.MyViewHolder> {

    private final List<CategoryModel> categoryList;

    public CategoryAdapter(List<CategoryModel> categoryList) {
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item using ViewBinding
        CategoryItemRecyclerRowBinding binding = CategoryItemRecyclerRowBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // Get the data for the current position and bind it
        holder.bindData(categoryList.get(position));
    }

    @Override
    public int getItemCount() {
        // Return the total number of items in the list
        return categoryList.size();
    }

    // This is the ViewHolder, a normal static inner class in Java
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final CategoryItemRecyclerRowBinding binding;

        public MyViewHolder(CategoryItemRecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        // This method binds the data to the views in the layout
        void bindData(CategoryModel category) {
            binding.nameTextView.setText(category.getName());

            // Use Glide to load the image from a URL
            Glide.with(binding.getRoot().getContext())
                    .load(category.getCoverUrl())
                    .into(binding.coverImageView);
        }
    }
}
