package com.example.bookgate;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class AdminBookListFragment extends Fragment implements AdminBookAdapter.OnBookDeleteListener, AdminBookAdapter.OnBookEditListener {

    private RecyclerView recyclerView;
    private AdminBookAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<Book> bookList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_book_list, container, false);

        // Initialize database helper
        dbHelper = new DatabaseHelper(requireContext());

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.admin_books_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Load books
        loadBooks();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload books when fragment becomes visible
        loadBooks();
    }

    private void loadBooks() {
        // Get all books from database
        bookList = dbHelper.getAllBooks();

        // Create and set adapter
        adapter = new AdminBookAdapter(requireContext(), bookList, this, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onBookDelete(int position) {
        final Book book = bookList.get(position);

        // Show confirmation dialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Book")
                .setMessage("Are you sure you want to delete \"" + book.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Delete the book
                    if (dbHelper.deleteBook(book.getId())) {
                        // Delete associated files
                        deleteBookFiles(book);
                        
                        // Remove from list and update adapter
                        bookList.remove(position);
                        adapter.notifyItemRemoved(position);
                        Toast.makeText(requireContext(), "Book deleted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete book", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteBookFiles(Book book) {
        // Delete image file if exists
        String imagePath = book.getImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                imageFile.delete();
            }
        }

        // Delete PDF file if exists
        String pdfPath = book.getPdfPath();
        if (pdfPath != null && !pdfPath.isEmpty()) {
            File pdfFile = new File(pdfPath);
            if (pdfFile.exists()) {
                pdfFile.delete();
            }
        }
    }
    
    @Override
    public void onBookEdit(int position) {
        Book book = bookList.get(position);
        
        // Launch EditBookActivity to edit book details
        Intent intent = new Intent(requireContext(), EditBookActivity.class);
        intent.putExtra("BOOK_ID", book.getId());
        startActivity(intent);
    }
}
