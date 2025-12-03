package com.kimani.musicplayerapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import com.kimani.musicplayerapp.SongsListActivity;
import com.kimani.musicplayerapp.databinding.CategoryItemRecyclerRowBinding;
import com.kimani.musicplayerapp.models.CategoryModel;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.MyViewHolder> {

    private final List<CategoryModel> categoryList;

    public CategoryAdapter(List<CategoryModel> categoryList) {
        this.categoryList = categoryList;
    }

    // Inner ViewHolder class
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final CategoryItemRecyclerRowBinding binding;

        public MyViewHolder(CategoryItemRecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        // Binds the data with the views
        public void bindData(CategoryModel category) {
            binding.nameTextView.setText(category.getName());

            Glide.with(binding.coverImageView.getContext())
                    .load(category.getCoverUrl())
                    .apply(new RequestOptions().transform(new RoundedCorners(32)))
                    .into(binding.coverImageView);

            // Start SongsListActivity on click
            Context context = binding.getRoot().getContext();
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SongsListActivity.category = category;
                    Intent intent = new Intent(context, SongsListActivity.class);
                    context.startActivity(intent);
                }
            });
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        CategoryItemRecyclerRowBinding binding = CategoryItemRecyclerRowBinding.inflate(inflater, parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.bindData(categoryList.get(position));
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }
}
