package com.example.bookgate;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class BookListActivity extends AppCompatActivity {

    private RecyclerView booksRecyclerView;
    private BookAdapter bookAdapter;
    private FloatingActionButton fabAddBook;
    private DatabaseHelper dbHelper;
    private String userEmail;
    private boolean isLibrarian = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        // Get user email from intent
        userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userEmail == null) {
            // If no user email, redirect to login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);
        
        // Check if user is admin
        User user = dbHelper.getUserByEmail(userEmail);
        if (user != null) {
            isLibrarian = user.isLibrarian();
        }

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Book Library");
        }

        // Initialize views
        booksRecyclerView = findViewById(R.id.books_recycler_view);
        fabAddBook = findViewById(R.id.fab_add_book);

        // Show add book button only for librarians
        if (isLibrarian) {
            fabAddBook.setVisibility(View.VISIBLE);
            fabAddBook.setOnClickListener(v -> {
                Intent intent = new Intent(BookListActivity.this, AdminPanelActivity.class);
                intent.putExtra("USER_EMAIL", userEmail);
                startActivity(intent);
            });
        } else {
            fabAddBook.setVisibility(View.GONE);
        }

        // Set up the RecyclerView
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        // Get books from database
        List<Book> books = dbHelper.getAllBooks();
        
        // Set up the adapter and layout manager
        bookAdapter = new BookAdapter(this, books, userEmail);
        booksRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        booksRecyclerView.setAdapter(bookAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh book list when returning to this activity
        if (bookAdapter != null) {
            bookAdapter.updateData(dbHelper.getAllBooks());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book_list, menu);
        
        // Show admin panel menu item only for librarians
        MenuItem adminItem = menu.findItem(R.id.action_admin_panel);
        if (adminItem != null) {
            adminItem.setVisible(isLibrarian);
        }
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_logout) {
            // Handle logout
            Intent intent = new Intent(BookListActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_admin_panel && isLibrarian) {
            // Open admin panel
            Intent intent = new Intent(BookListActivity.this, AdminPanelActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
}
