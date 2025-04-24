package com.example.bookgate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText nameInput, emailInput, passwordInput, adminKeyInput;
    private TextInputLayout adminKeyLayout;
    private CheckBox registerAsAdminCheckbox;
    private Button registerButton;
    private TextView loginText;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize UI components
        nameInput = findViewById(R.id.name_input);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        adminKeyInput = findViewById(R.id.admin_key_input);
        adminKeyLayout = findViewById(R.id.admin_key_layout);
        registerAsAdminCheckbox = findViewById(R.id.register_as_admin_checkbox);
        registerButton = findViewById(R.id.register_button);
        loginText = findViewById(R.id.login_text);
        
        dbHelper = new DatabaseHelper(this);

        // Show/hide admin key input based on checkbox
        registerAsAdminCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            adminKeyLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Register button click listener
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // Login text click listener
        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Return to login activity
            }
        });
    }

    private void registerUser() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        boolean isAdmin = registerAsAdminCheckbox.isChecked();
        String adminKey = isAdmin ? adminKeyInput.getText().toString().trim() : "";

        // Validate input
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Password length validation
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if email already exists
        if (dbHelper.getUserByEmail(email) != null) {
            Toast.makeText(this, "Email already registered. Please use a different email.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verify admin key if trying to register as admin
        if (isAdmin) {
            if (adminKey.isEmpty() || !adminKey.equals(DatabaseHelper.ADMIN_KEY)) {
                Toast.makeText(this, "Invalid admin key", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // All validations passed, register the user
        String role = isAdmin ? DatabaseHelper.ROLE_LIBRARIAN : DatabaseHelper.ROLE_MEMBER;
        long userId = dbHelper.addUser(name, email, password, role);

        if (userId != -1) {
            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
            
            // Navigate to appropriate screen
            Intent intent;
            if (isAdmin) {
                intent = new Intent(RegisterActivity.this, AdminPanelActivity.class);
            } else {
                intent = new Intent(RegisterActivity.this, BookListActivity.class);
            }
            intent.putExtra("USER_EMAIL", email);
            startActivity(intent);
            finish(); // Close registration activity
        } else {
            Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
