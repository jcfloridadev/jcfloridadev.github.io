package com.example.weighttrackerjustincarloflorida;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/*
 * Developer: Justin Carlo Florida
 * Course: CS 360 - Mobile Architecture and Programming
 * Project Three: Weight-Tracking App
 *
 * This activity serves as the starting screen for the Weight-Tracking app.
 * It manages account creation and validates returning users against
 * credentials stored in the SQLite database.
 */

public class MainActivity extends AppCompatActivity {

    // Provides access to the SQLite database methods.
    private DatabaseWT databaseWT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the login screen layout.
        setContentView(R.layout.activity_main);

        // Connect to the Weight Tracker SQLite database.
        databaseWT = new DatabaseWT(this);

        Button buttonLogin = findViewById(R.id.buttonLogin);

        Button buttonCreateAccount =
                findViewById(R.id.buttonCreateAccount);

        EditText editTextUsername =
                findViewById(R.id.editTextUsername);

        EditText editTextPassword =
                findViewById(R.id.editTextPassword);

        // Validate an existing user's credentials against the database.
        buttonLogin.setOnClickListener(view -> {

            String username =
                    editTextUsername.getText().toString().trim();

            String password =
                    editTextPassword.getText().toString().trim();

            // Require both login fields before checking the database.
            if (username.isEmpty() || password.isEmpty()) {

                Toast.makeText(
                        MainActivity.this,
                        "Please enter a username and password.",
                        Toast.LENGTH_SHORT).show();

                return;
            }

            // Check whether the entered credentials match a saved user.
            if (databaseWT.checkUser(username, password)) {

                long userId = databaseWT.getUserId(username);

                Toast.makeText(
                        MainActivity.this,
                        "Login successful.",
                        Toast.LENGTH_SHORT).show();

                Intent intent =
                        new Intent(
                                MainActivity.this,
                                DashboardActivity.class);

                // Pass the logged-in user's information to the dashboard.
                intent.putExtra("USER_ID", userId);
                intent.putExtra("USERNAME", username);

                startActivity(intent);

            } else {

                Toast.makeText(
                        MainActivity.this,
                        "Incorrect username or password.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Create and save a first-time user's account in the database.
        buttonCreateAccount.setOnClickListener(view -> {

            String username =
                    editTextUsername.getText().toString().trim();

            String password =
                    editTextPassword.getText().toString().trim();

            // Prevent an empty username or password from being saved.
            if (username.isEmpty() || password.isEmpty()) {

                Toast.makeText(
                        MainActivity.this,
                        "Please enter a username and password then press create account.",
                        Toast.LENGTH_SHORT).show();

                return;
            }

            // Prevent duplicate usernames.
            if (databaseWT.usernameExists(username)) {

                Toast.makeText(
                        MainActivity.this,
                        "That username already exists.",
                        Toast.LENGTH_SHORT).show();

                return;
            }

            // Save the new account to SQLite.
            boolean accountCreated =
                    databaseWT.addUser(username, password);

            if (accountCreated) {

                Toast.makeText(
                        MainActivity.this,
                        "Account created successfully. You can now log in.",
                        Toast.LENGTH_SHORT).show();

            } else {

                Toast.makeText(
                        MainActivity.this,
                        "Unable to create account.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}