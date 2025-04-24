package com.example.bookgate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class ManageKeysFragment extends Fragment implements KeyAdapter.OnKeyDeleteListener {

    private Spinner bookSpinner;
    private TextInputEditText keyInput;
    private Button addKeyButton;
    private RecyclerView keysRecyclerView;
    private KeyAdapter keyAdapter;
    private DatabaseHelper dbHelper;
    private List<Book> booksList;
    private List<DownloadKey> keysList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_keys, container, false);

        // Initialize UI components
        bookSpinner = view.findViewById(R.id.book_spinner);
        keyInput = view.findViewById(R.id.key_input);
        addKeyButton = view.findViewById(R.id.add_key_button);
        keysRecyclerView = view.findViewById(R.id.keys_recycler_view);

        dbHelper = new DatabaseHelper(requireContext());
        
        // Initialize lists
        booksList = new ArrayList<>();
        keysList = new ArrayList<>();

        // Set up RecyclerView
        keyAdapter = new KeyAdapter(requireContext(), keysList, this);
        keysRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        keysRecyclerView.setAdapter(keyAdapter);

        // Load books and keys
        loadBooks();
        loadKeys();

        // Set click listener for add key button
        addKeyButton.setOnClickListener(v -> addKey());

        return view;
    }

    private void loadBooks() {
        booksList = dbHelper.getAllBooks();
        List<String> bookTitles = new ArrayList<>();
        
        // Add book titles to spinner
        for (Book book : booksList) {
            bookTitles.add(book.getTitle());
        }
        
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, bookTitles);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bookSpinner.setAdapter(spinnerAdapter);
    }

    private void loadKeys() {
        keysList.clear();
        keysList.addAll(dbHelper.getAllKeys());
        keyAdapter.notifyDataSetChanged();
    }

    private void addKey() {
        String keyValue = keyInput.getText().toString().trim();
        
        // Validate inputs
        if (keyValue.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a key value", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (booksList.isEmpty()) {
            Toast.makeText(requireContext(), "No books available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Get selected book
        int position = bookSpinner.getSelectedItemPosition();
        if (position >= 0 && position < booksList.size()) {
            Book selectedBook = booksList.get(position);
            
            // Add key to database
            long result = dbHelper.addDownloadKey(selectedBook.getId(), keyValue);
            
            if (result != -1) {
                keyInput.setText("");
                Toast.makeText(requireContext(), "Key added successfully", Toast.LENGTH_SHORT).show();
                loadKeys(); // Refresh keys list
            } else {
                Toast.makeText(requireContext(), "Failed to add key", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onKeyDelete(String keyValue) {
        // Delete key from database
        dbHelper.deleteKey(keyValue);
        Toast.makeText(requireContext(), "Key deleted", Toast.LENGTH_SHORT).show();
        loadKeys(); // Refresh keys list
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        loadBooks();
        loadKeys();
    }
}
