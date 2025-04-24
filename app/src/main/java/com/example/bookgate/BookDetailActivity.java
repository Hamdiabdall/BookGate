package com.example.bookgate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;

public class BookDetailActivity extends AppCompatActivity {

    private ImageView bookCoverView;
    private TextView titleTextView, authorTextView, descriptionTextView;
    private Button downloadButton, submitKeyButton;
    private LinearLayout keyInputLayout;
    private TextInputEditText downloadKeyInput;
    
    private DatabaseHelper dbHelper;
    private Book book;
    private String userEmail;
    private boolean isLibrarian;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        // Get book ID and user email from intent
        int bookId = getIntent().getIntExtra("BOOK_ID", -1);
        userEmail = getIntent().getStringExtra("USER_EMAIL");
        
        if (bookId == -1 || userEmail == null) {
            Toast.makeText(this, "Error: Book not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);
        
        // Get book details
        book = dbHelper.getBookById(bookId);
        if (book == null) {
            Toast.makeText(this, "Error: Book not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Check if user is librarian
        User user = dbHelper.getUserByEmail(userEmail);
        isLibrarian = user != null && user.isLibrarian();

        // Initialize UI components
        initializeUI();
        
        // Display book details
        displayBookDetails();
    }

    private void initializeUI() {
        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Book Details");
        }
        
        // Initialize views
        bookCoverView = findViewById(R.id.book_cover);
        titleTextView = findViewById(R.id.book_title);
        authorTextView = findViewById(R.id.book_author);
        descriptionTextView = findViewById(R.id.book_description);
        downloadButton = findViewById(R.id.download_button);
        keyInputLayout = findViewById(R.id.key_input_layout);
        downloadKeyInput = findViewById(R.id.download_key_input);
        submitKeyButton = findViewById(R.id.submit_key_button);
        
        // Set download button click listener
        downloadButton.setOnClickListener(v -> {
            if (isLibrarian) {
                // Librarians can download directly
                downloadPdf();
            } else {
                // Members need to enter a key
                downloadButton.setVisibility(View.GONE);
                keyInputLayout.setVisibility(View.VISIBLE);
            }
        });
        
        // Set submit key button click listener
        submitKeyButton.setOnClickListener(v -> {
            String key = downloadKeyInput.getText().toString().trim();
            if (key.isEmpty()) {
                Toast.makeText(this, "Please enter a download key", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Validate the key
            if (dbHelper.isValidKey(key, book.getId())) {
                // Key is valid, download the PDF
                dbHelper.deleteKey(key); // Remove the key after use
                downloadPdf();
            } else {
                Toast.makeText(this, "Invalid download key", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayBookDetails() {
        // Set book details in UI
        titleTextView.setText(book.getTitle());
        authorTextView.setText(book.getAuthor());
        descriptionTextView.setText(book.getDescription());
        
        // Load book cover image if exists
        String imagePath = book.getImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                bookCoverView.setImageURI(Uri.fromFile(imageFile));
            } else {
                bookCoverView.setImageResource(R.drawable.ic_book);
            }
        } else {
            bookCoverView.setImageResource(R.drawable.ic_book);
        }
    }
    
    private void downloadPdf() {
        String pdfPath = book.getPdfPath();
        if (pdfPath != null && !pdfPath.isEmpty()) {
            File pdfFile = new File(pdfPath);
            
            // Debug information
            Toast.makeText(this, "PDF Path: " + pdfPath, Toast.LENGTH_LONG).show();
            
            if (pdfFile.exists()) {
                try {
                    // Use FileProvider to create a content URI
                    Uri fileUri = FileProvider.getUriForFile(
                            this,
                            getApplicationContext().getPackageName() + ".provider",
                            pdfFile);
                    
                    // Create an intent to view the PDF
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(fileUri, "application/pdf");
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    
                    // Check if there's an app to handle PDF viewing
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "No PDF viewer app found", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error opening PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "PDF file not found at: " + pdfPath, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "No PDF path available for this book", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
