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

/**
 * CategoryAdapter is responsible for displaying a list of music categories (e.g., Albums, Genres).
 * It uses Glide for image loading and handles navigation to SongsListActivity when a category is selected.
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.MyViewHolder> {

    private final List<CategoryModel> categoryList;

    /**
     * Constructor for CategoryAdapter.
     * @param categoryList The list of CategoryModel objects to display.
     */
    public CategoryAdapter(List<CategoryModel> categoryList) {
        this.categoryList = categoryList;
    }

    /**
     * ViewHolder class that holds the view for each category item.
     */
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final CategoryItemRecyclerRowBinding binding;

        public MyViewHolder(CategoryItemRecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * Binds a CategoryModel object to the item's views.
         * @param category The category data to display.
         */
        public void bindData(CategoryModel category) {
            // Set the category name text
            binding.nameTextView.setText(category.getName());

            // Use Glide to load the category cover image with rounded corners
            Glide.with(binding.coverImageView.getContext())
                    .load(category.getCoverUrl())
                    .apply(new RequestOptions().transform(new RoundedCorners(32)))
                    .into(binding.coverImageView);

            // Handle click events to open SongsListActivity for the selected category
            Context context = binding.getRoot().getContext();
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Set the static category reference in SongsListActivity before starting it
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
        // Inflate the item layout using View Binding
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        CategoryItemRecyclerRowBinding binding = CategoryItemRecyclerRowBinding.inflate(inflater, parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // Bind data for the item at the specified position
        holder.bindData(categoryList.get(position));
    }

    @Override
    public int getItemCount() {
        // Return the total number of categories in the list
        return categoryList.size();
    }
}
