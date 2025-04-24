package com.example.bookgate;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class AdminBookAdapter extends RecyclerView.Adapter<AdminBookAdapter.ViewHolder> {

    private final Context context;
    private final List<Book> bookList;
    private final OnBookDeleteListener deleteListener;
    private final OnBookEditListener editListener;

    public interface OnBookDeleteListener {
        void onBookDelete(int position);
    }
    
    public interface OnBookEditListener {
        void onBookEdit(int position);
    }

    public AdminBookAdapter(Context context, List<Book> bookList, OnBookDeleteListener deleteListener, OnBookEditListener editListener) {
        this.context = context;
        this.bookList = bookList;
        this.deleteListener = deleteListener;
        this.editListener = editListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book book = bookList.get(position);

        // Set book data
        holder.titleTextView.setText(book.getTitle());
        holder.authorTextView.setText(book.getAuthor());
        holder.descriptionTextView.setText(book.getDescription());

        // Load image if exists
        String imagePath = book.getImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                holder.coverImageView.setImageURI(Uri.fromFile(imageFile));
            } else {
                holder.coverImageView.setImageResource(R.drawable.ic_book);
            }
        } else {
            holder.coverImageView.setImageResource(R.drawable.ic_book);
        }

        // Set delete button click listener
        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onBookDelete(holder.getAdapterPosition());
            }
        });
        
        // Set item click listener for editing
        holder.itemView.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onBookEdit(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView coverImageView;
        TextView titleTextView, authorTextView, descriptionTextView;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            coverImageView = itemView.findViewById(R.id.admin_book_cover);
            titleTextView = itemView.findViewById(R.id.admin_book_title);
            authorTextView = itemView.findViewById(R.id.admin_book_author);
            descriptionTextView = itemView.findViewById(R.id.admin_book_description);
            deleteButton = itemView.findViewById(R.id.admin_delete_book_button);
        }
    }
}
