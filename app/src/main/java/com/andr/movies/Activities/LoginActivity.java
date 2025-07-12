package com.andr.movies.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.andr.movies.R;
import com.andr.movies.db.DatabaseHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout emailInputLayout, passwordInputLayout;
    private TextInputEditText emailEditText, passwordEditText;
    private AppCompatButton loginButton;
    private TextView signUpTextView, forgotPasswordTextView;
    private ProgressBar progressBar;
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();
        setupClickListeners();

        databaseHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("MovieCenterPrefs", MODE_PRIVATE);

        // Pre-fill email if coming from registration
        String email = getIntent().getStringExtra("email");
        if (email != null) {
            emailEditText.setText(email);
        }

        // Check if user is already logged in
        if (isUserLoggedIn()) {
            navigateToMainActivity();
        }
    }

    private void initializeViews() {
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        loginButton = findViewById(R.id.loginButton);
        signUpTextView = findViewById(R.id.signUpTextView);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> validateAndLogin());

        signUpTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });

        forgotPasswordTextView.setOnClickListener(v -> {
            // Handle forgot password (you can implement this later)
            Toast.makeText(LoginActivity.this, "Forgot password feature coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void validateAndLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        // Reset error messages
        emailInputLayout.setError(null);
        passwordInputLayout.setError(null);

        boolean isValid = true;

        // Validate email
        if (TextUtils.isEmpty(email)) {
            emailInputLayout.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError("Please enter a valid email address");
            isValid = false;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            passwordInputLayout.setError("Password is required");
            isValid = false;
        }

        if (isValid) {
            loginUser(email, password);
        }
    }

    private void loginUser(String email, String password) {
        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);

        // Simulate network delay (you can remove this in production)
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Simulate network delay

                boolean isLoginSuccessful = databaseHelper.loginUser(email, password);

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    loginButton.setEnabled(true);

                    if (isLoginSuccessful) {
                        // Save login state
                        saveLoginState(email);

                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        navigateToMainActivity();
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    loginButton.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void saveLoginState(String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("userEmail", email);
        editor.putString("userFullName", databaseHelper.getUserFullName(email));
        editor.putInt("userId", databaseHelper.getUserId(email));
        editor.apply();
    }

    private boolean isUserLoggedIn() {
        return sharedPreferences.getBoolean("isLoggedIn", false);
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(LoginActivity.this, IntroActivity.class);
        startActivity(intent);
        finish();
    }
}