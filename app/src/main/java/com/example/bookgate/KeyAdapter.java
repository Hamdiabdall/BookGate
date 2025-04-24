package com.example.bookgate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class KeyAdapter extends RecyclerView.Adapter<KeyAdapter.KeyViewHolder> {

    private final Context context;
    private final List<DownloadKey> keysList;
    private final OnKeyDeleteListener listener;

    public interface OnKeyDeleteListener {
        void onKeyDelete(String keyValue);
    }

    public KeyAdapter(Context context, List<DownloadKey> keysList, OnKeyDeleteListener listener) {
        this.context = context;
        this.keysList = keysList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public KeyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_download_key, parent, false);
        return new KeyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KeyViewHolder holder, int position) {
        DownloadKey key = keysList.get(position);
        
        holder.keyValueText.setText(key.getKeyValue());
        holder.bookTitleText.setText(key.getBookTitle());
        
        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onKeyDelete(key.getKeyValue());
            }
        });
    }

    @Override
    public int getItemCount() {
        return keysList.size();
    }

    public static class KeyViewHolder extends RecyclerView.ViewHolder {
        TextView keyValueText;
        TextView bookTitleText;
        ImageButton deleteButton;

        public KeyViewHolder(@NonNull View itemView) {
            super(itemView);
            keyValueText = itemView.findViewById(R.id.key_value);
            bookTitleText = itemView.findViewById(R.id.book_title);
            deleteButton = itemView.findViewById(R.id.delete_key_button);
        }
    }
}
