package com.brinux.musify;

import android.content.ContentUris;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.brinux.musify.databinding.ItemSongBinding;
import com.bumptech.glide.Glide;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewholder> {
    private final List<Song> songs;
    private final OnItemClickListener listener;

    public SongAdapter(List<Song> songs, OnItemClickListener listener) {
        this.songs = songs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SongAdapter.SongViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSongBinding binding = ItemSongBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SongViewholder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull SongAdapter.SongViewholder holder, int position) {
        Song song = songs.get(position);
        holder.binding.textTitle.setText(song.title);
        holder.binding.textArtist.setText(song.artist);

        Uri albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), song.albumId);
        Glide.with(holder.binding.getRoot().getContext())
                .load(albumArtUri)
                .circleCrop()
                .placeholder(R.drawable.baseline_music_note_24)
                .error(R.drawable.baseline_music_note_24)
                .into(holder.binding.imageAlbumArt);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public class SongViewholder extends RecyclerView.ViewHolder {
        final ItemSongBinding binding;
        final OnItemClickListener listener;

        public SongViewholder(ItemSongBinding binding, OnItemClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            listener.onItemClick(pos);
                        }
                    }
                }
            });
        }
    }
}
