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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import com.kimani.musicplayerapp.MyExoplayer;
import com.kimani.musicplayerapp.OnlinePlayerActivity;
import com.kimani.musicplayerapp.databinding.SectionSongListRecyclerRowBinding;
import com.kimani.musicplayerapp.models.SongModel;

public class SectionSongListAdapter extends RecyclerView.Adapter<SectionSongListAdapter.MyViewHolder> {

    private final List<String> songIdList;

    public SectionSongListAdapter(List<String> songIdList) {
        this.songIdList = songIdList;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final SectionSongListRecyclerRowBinding binding;

        public MyViewHolder(SectionSongListRecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        // Bind data with view
        public void bindData(String songId) {
            FirebaseFirestore.getInstance().collection("songs")
                    .document(songId).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            SongModel song = documentSnapshot.toObject(SongModel.class);
                            if (song != null) {
                                binding.songTitleTextView.setText(song.getTitle());
                                binding.songSubtitleTextView.setText(song.getSubtitle());

                                Glide.with(binding.songCoverImageView.getContext())
                                        .load(song.getCoverUrl())
                                        .apply(new RequestOptions().transform(new RoundedCorners(32)))
                                        .into(binding.songCoverImageView);

                                binding.getRoot().setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Context context = binding.getRoot().getContext();
                                        MyExoplayer.startPlaying(context, song);
                                        context.startActivity(new Intent(context, OnlinePlayerActivity.class));
                                    }
                                });
                            }
                        }
                    });
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        SectionSongListRecyclerRowBinding binding = SectionSongListRecyclerRowBinding.inflate(inflater, parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public int getItemCount() {
        return songIdList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.bindData(songIdList.get(position));
    }
}
