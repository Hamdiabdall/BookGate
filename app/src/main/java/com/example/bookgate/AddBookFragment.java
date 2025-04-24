package com.example.bookgate;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddBookFragment extends Fragment {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;
    private static final int REQUEST_PDF_FILE = 3;

    private TextInputEditText titleInput, authorInput, descriptionInput;
    private ImageView coverPreview;
    private TextView pdfFileNameText;
    private Button chooseImageGalleryBtn, takePhotoBtn, choosePdfBtn, addBookBtn;

    private String imagePath = "";
    private String pdfPath = "";
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_book, container, false);

        // Initialize UI components
        titleInput = view.findViewById(R.id.book_title_input);
        authorInput = view.findViewById(R.id.book_author_input);
        descriptionInput = view.findViewById(R.id.book_description_input);
        coverPreview = view.findViewById(R.id.book_cover_preview);
        pdfFileNameText = view.findViewById(R.id.pdf_file_name);
        chooseImageGalleryBtn = view.findViewById(R.id.choose_image_from_gallery_button);
        takePhotoBtn = view.findViewById(R.id.take_photo_button);
        choosePdfBtn = view.findViewById(R.id.choose_pdf_button);
        addBookBtn = view.findViewById(R.id.add_book_button);

        dbHelper = new DatabaseHelper(requireContext());

        // Set click listeners
        chooseImageGalleryBtn.setOnClickListener(v -> openGallery());
        takePhotoBtn.setOnClickListener(v -> takePhoto());
        choosePdfBtn.setOnClickListener(v -> choosePdfFile());
        addBookBtn.setOnClickListener(v -> addBook());

        return view;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(requireContext(), "Error creating image file", Toast.LENGTH_SHORT).show();
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(requireContext(),
                        requireContext().getPackageName() + ".provider",
                        photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file path for use with ACTION_VIEW intents
        imagePath = image.getAbsolutePath();
        return image;
    }

    private void choosePdfFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        startActivityForResult(intent, REQUEST_PDF_FILE);
    }

    private void addBook() {
        String title = titleInput.getText().toString().trim();
        String author = authorInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();

        // Validate inputs
        if (title.isEmpty() || author.isEmpty() || description.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imagePath.isEmpty()) {
            Toast.makeText(requireContext(), "Please select a cover image", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pdfPath.isEmpty()) {
            Toast.makeText(requireContext(), "Please select a PDF file", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add book to database
        long bookId = dbHelper.addBook(title, author, description, imagePath, pdfPath);

        if (bookId != -1) {
            // Clear form
            titleInput.setText("");
            authorInput.setText("");
            descriptionInput.setText("");
            coverPreview.setImageResource(R.drawable.ic_book);
            pdfFileNameText.setText(R.string.no_file_selected);
            imagePath = "";
            pdfPath = "";

            Toast.makeText(requireContext(), "Book added successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Failed to add book", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_GALLERY && data != null) {
                Uri selectedImage = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), selectedImage);
                    // Save bitmap to app's private storage
                    File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
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
                    Toast.makeText(requireContext(), "Error processing image", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // Image captured and saved to imagePath
                coverPreview.setImageURI(Uri.fromFile(new File(imagePath)));
            } else if (requestCode == REQUEST_PDF_FILE && data != null) {
                Uri pdfUri = data.getData();
                
                // Copy PDF to app's private storage
                try {
                    // Ensure directory exists
                    File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
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
                    
                    java.io.InputStream in = requireContext().getContentResolver().openInputStream(pdfUri);
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
                            Toast.makeText(requireContext(), 
                                "PDF saved successfully at: " + pdfPath, 
                                Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(requireContext(), 
                                "Error: PDF file not created properly", 
                                Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), 
                            "Could not open input stream for PDF", 
                            Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(requireContext(), 
                        "Error processing PDF: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
