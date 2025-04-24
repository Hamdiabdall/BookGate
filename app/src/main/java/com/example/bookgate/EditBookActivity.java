package com.example.bookgate;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditBookActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_GALLERY = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int REQUEST_PDF_FILE = 3;

    private TextInputEditText titleInput, authorInput, descriptionInput;
    private ImageView coverPreview;
    private TextView pdfFileNameText;
    private Button updateBookButton;

    private DatabaseHelper dbHelper;
    private Book book;
    private String imagePath;
    private String pdfPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_book);

        // Get book ID from intent
        int bookId = getIntent().getIntExtra("BOOK_ID", -1);
        if (bookId == -1) {
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

        // Initialize UI components
        setupToolbar();
        initializeUI();

        // Load book details into UI
        loadBookDetails();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Edit Book");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initializeUI() {
        titleInput = findViewById(R.id.edit_book_title);
        authorInput = findViewById(R.id.edit_book_author);
        descriptionInput = findViewById(R.id.edit_book_description);
        coverPreview = findViewById(R.id.edit_book_cover_preview);
        pdfFileNameText = findViewById(R.id.edit_pdf_file_name);

        Button galleryButton = findViewById(R.id.edit_choose_gallery_button);
        Button photoButton = findViewById(R.id.edit_take_photo_button);
        Button pdfButton = findViewById(R.id.edit_choose_pdf_button);
        updateBookButton = findViewById(R.id.update_book_button);

        // Set up button click listeners
        galleryButton.setOnClickListener(v -> chooseFromGallery());
        photoButton.setOnClickListener(v -> takePhoto());
        pdfButton.setOnClickListener(v -> choosePdfFile());
        updateBookButton.setOnClickListener(v -> updateBook());
    }

    private void loadBookDetails() {
        // Set text fields
        titleInput.setText(book.getTitle());
        authorInput.setText(book.getAuthor());
        descriptionInput.setText(book.getDescription());

        // Initialize paths
        imagePath = book.getImagePath();
        pdfPath = book.getPdfPath();

        // Load cover image
        if (imagePath != null && !imagePath.isEmpty()) {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                coverPreview.setImageURI(Uri.fromFile(imageFile));
            }
        }

        // Set PDF file name
        if (pdfPath != null && !pdfPath.isEmpty()) {
            File pdfFile = new File(pdfPath);
            if (pdfFile.exists()) {
                pdfFileNameText.setText(pdfFile.getName());
            }
        }
    }

    private void chooseFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
    }

    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create file for the image
            File photoFile = null;
            try {
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "_";
                photoFile = File.createTempFile(imageFileName, ".jpg", storageDir);
                imagePath = photoFile.getAbsolutePath();
            } catch (IOException ex) {
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        getApplicationContext().getPackageName() + ".provider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void choosePdfFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        startActivityForResult(intent, REQUEST_PDF_FILE);
    }

    private void updateBook() {
        // Validate inputs
        String title = titleInput.getText().toString().trim();
        String author = authorInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            titleInput.setError("Title is required");
            return;
        }

        if (TextUtils.isEmpty(author)) {
            authorInput.setError("Author is required");
            return;
        }

        if (TextUtils.isEmpty(description)) {
            descriptionInput.setError("Description is required");
            return;
        }

        // Update book object
        book.setTitle(title);
        book.setAuthor(author);
        book.setDescription(description);
        book.setImagePath(imagePath);
        book.setPdfPath(pdfPath);

        // Save to database
        if (dbHelper.updateBook(book)) {
            Toast.makeText(this, "Book updated successfully", Toast.LENGTH_SHORT).show();
            finish(); // Close activity and return to list
        } else {
            Toast.makeText(this, "Failed to update book", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_GALLERY && data != null) {
                Uri selectedImage = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                    // Save bitmap to app's private storage
                    File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                    String imageFileName = "JPEG_" + timeStamp + ".jpg";
                    File imageFile = new File(storageDir, imageFileName);
                    FileOutputStream out = new FileOutputStream(imageFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.close();

                    imagePath = imageFile.getAbsolutePath();
                    coverPreview.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // Image captured and saved to imagePath
                coverPreview.setImageURI(Uri.fromFile(new File(imagePath)));
            } else if (requestCode == REQUEST_PDF_FILE && data != null) {
                Uri pdfUri = data.getData();
                
                // Copy PDF to app's private storage
                try {
                    // Ensure directory exists
                    File storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                    if (storageDir != null && !storageDir.exists()) {
                        storageDir.mkdirs();
                    }
                    
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                    String pdfFileName = "PDF_" + timeStamp + ".pdf";
                    File pdfFile = new File(storageDir, pdfFileName);
                    
                    // Copy the file with improved error handling
                    FileOutputStream out = new FileOutputStream(pdfFile);
                    byte[] buffer = new byte[8192]; // Larger buffer for efficiency
                    int read;
                    
                    InputStream in = getContentResolver().openInputStream(pdfUri);
                    if (in != null) {
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }
                        in.close();
                        out.flush();
                        out.close();
                        
                        // Ensure file was created properly
                        if (pdfFile.exists() && pdfFile.length() > 0) {
                            pdfPath = pdfFile.getAbsolutePath();
                            pdfFileNameText.setText(pdfFileName);
                            
                            // Show success message with path for debugging
                            Toast.makeText(this, 
                                "PDF saved successfully at: " + pdfPath, 
                                Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, 
                                "Error: PDF file not created properly", 
                                Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, 
                            "Could not open input stream for PDF", 
                            Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, 
                        "Error processing PDF: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
