package com.example.bookgate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailInput, passwordInput;
    private Button loginButton, loginAdminButton;
    private TextView registerText;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI components
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        loginAdminButton = findViewById(R.id.login_admin_button);
        registerText = findViewById(R.id.register_text);
        
        dbHelper = new DatabaseHelper(this);

        // Member Login button click listener
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser(false);
            }
        });

        // Admin Login button click listener
        loginAdminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser(true);
            }
        });

        // Register text click listener
        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser(boolean isAdmin) {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate input
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if user exists and credentials are correct
        if (dbHelper.checkUser(email, password)) {
            // Check if user role matches the requested role
            boolean userIsAdmin = dbHelper.isUserAdmin(email);
            
            if (isAdmin && !userIsAdmin) {
                Toast.makeText(this, "You are not registered as a librarian", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get the user object for session
            User user = dbHelper.getUserByEmail(email);
            
            // Login successful, navigate to appropriate screen
            if (userIsAdmin) {
                // Admin flow
                Intent intent = new Intent(LoginActivity.this, AdminPanelActivity.class);
                intent.putExtra("USER_EMAIL", email);
                startActivity(intent);
            } else {
                // Member flow
                Intent intent = new Intent(LoginActivity.this, BookListActivity.class);
                intent.putExtra("USER_EMAIL", email);
                startActivity(intent);
            }
            
            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
            finish(); // Close login activity
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
    }
}
