package com.example.weighttrackerjustincarloflorida;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Locale;

/*
 * Developer: Justin Carlo Florida
 * Course: CS 360 - Mobile Architecture and Programming
 * Project Three: Weight-Tracking App
 *
 * This activity allows the logged-in user to save a new
 * daily weight record to the SQLite database.
 */
public class AddWeightActivity extends AppCompatActivity {

    private DatabaseWT databaseWT;
    private long userId;

    private boolean editMode;
    private long weightId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the add-weight-entry interface.
        setContentView(R.layout.activity_add_weight);

        // Connect to the database and retrieve the current user.
        databaseWT = new DatabaseWT(this);
        userId = getIntent().getLongExtra("USER_ID", -1);

        TextView textViewEntryGoalWeight =
                findViewById(R.id.textViewEntryGoalWeight);

        // Display the logged-in user's saved goal weight.
        double currentGoal =
                databaseWT.getGoalWeight(userId);

        if (currentGoal >= 0) {

            textViewEntryGoalWeight.setText(
                    String.format(
                            Locale.US,
                            "%.1f lb",
                            currentGoal));

        } else {

            textViewEntryGoalWeight.setText(
                    "No goal set");
        }

        // Determine whether this screen is creating or editing a record.
        editMode =
                getIntent().getBooleanExtra(
                        "EDIT_MODE",
                        false);

        weightId =
                getIntent().getLongExtra(
                        "WEIGHT_ID",
                        -1);

        TextView textViewTitle =
                findViewById(R.id.textViewAddWeightTitle);

        EditText editTextWeight =
                findViewById(R.id.editTextWeightEntry);

        EditText editTextDate =
                findViewById(R.id.editTextWeightDate);

        EditText editTextNotes =
                findViewById(R.id.editTextWeightNotes);

        Button buttonSaveWeight =
                findViewById(R.id.buttonSaveWeight);

        Button buttonCancel =
                findViewById(R.id.buttonCancelWeight);

        // Populate the existing record when this screen is opened in edit mode.
        if (editMode) {

            double existingWeight =
                    getIntent().getDoubleExtra(
                            "WEIGHT_VALUE",
                            0.0);

            String existingDate =
                    getIntent().getStringExtra(
                            "WEIGHT_DATE");

            String existingNotes =
                    getIntent().getStringExtra(
                            "WEIGHT_NOTES");

            editTextWeight.setText(
                    String.valueOf(existingWeight));

            if (existingDate != null) {
                editTextDate.setText(existingDate);
            }

            if (existingNotes != null) {
                editTextNotes.setText(existingNotes);
            }

            textViewTitle.setText("Edit Weight Entry");
            buttonSaveWeight.setText("Update Entry");
        }

        // Save a new daily weight record to the database.
        buttonSaveWeight.setOnClickListener(view -> {

            String weightText =
                    editTextWeight.getText().toString().trim();

            String date =
                    editTextDate.getText().toString().trim();

            String notes =
                    editTextNotes.getText().toString().trim();

            if (weightText.isEmpty() || date.isEmpty()) {

                Toast.makeText(
                        AddWeightActivity.this,
                        "Please enter a weight and date.",
                        Toast.LENGTH_SHORT).show();

                return;
            }

            if (userId == -1) {

                Toast.makeText(
                        AddWeightActivity.this,
                        "Unable to identify the current user.",
                        Toast.LENGTH_SHORT).show();

                return;
            }

            try {

                double weight =
                        Double.parseDouble(weightText);

                boolean saved;

                // Update an existing record or create a new one.
                if (editMode) {

                    saved =
                            databaseWT.updateWeight(
                                    weightId,
                                    userId,
                                    weight,
                                    date,
                                    notes);

                } else {

                    saved =
                            databaseWT.addWeight(
                                    userId,
                                    weight,
                                    date,
                                    notes);
                }

                if (saved) {

                    String successMessage;

                    if (editMode) {
                        successMessage = "Weight entry updated.";
                    } else {
                        successMessage = "Weight entry saved.";
                    }

                    Toast.makeText(
                            AddWeightActivity.this,
                            successMessage,
                            Toast.LENGTH_SHORT).show();

                    // Check whether this saved weight reaches the user's goal.
                    checkAndSendGoalSms(weight);

                    finish();

                } else {

                    Toast.makeText(
                            AddWeightActivity.this,
                            "Unable to save weight entry.",
                            Toast.LENGTH_SHORT).show();
                }

            } catch (NumberFormatException exception) {

                Toast.makeText(
                        AddWeightActivity.this,
                        "Please enter a valid weight.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Return to the dashboard without saving changes.
        buttonCancel.setOnClickListener(view -> finish());
    }

    /*
     * Sends a goal-weight SMS notification when the user has enabled
     * notifications, granted permission, and reached the saved goal.
     */
    private void checkAndSendGoalSms(double currentWeight) {

        double goalWeight =
                databaseWT.getGoalWeight(userId);

        // A goal must exist before progress can trigger an alert.
        if (goalWeight < 0) {
            return;
        }

        // Continue only when the user's current weight reaches the goal.
        if (currentWeight > goalWeight) {
            return;
        }

        // Respect the user's saved SMS preference.
        if (!databaseWT.isSmsEnabled(userId)) {
            return;
        }

        // The rest of the application continues normally if denied.
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            Log.d(
                    "WeightTrackerSMS",
                    "Goal reached, but SEND_SMS permission was not granted.");

            return;
        }

        String phoneNumber =
                databaseWT.getPhoneNumber(userId);

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return;
        }

        String message =
                String.format(
                        Locale.US,
                        "Congratulations! You reached your goal weight of %.1f lb.",
                        goalWeight);

        try {

            SmsManager smsManager =
                    SmsManager.getDefault();

            smsManager.sendTextMessage(
                    phoneNumber,
                    null,
                    message,
                    null,
                    null);

            // Log the action because the Android Emulator may not deliver
            // an actual SMS to an external mobile device.
            Log.d(
                    "WeightTrackerSMS",
                    "Goal SMS triggered: " + message);

        } catch (Exception exception) {

            Log.e(
                    "WeightTrackerSMS",
                    "Unable to send goal SMS.",
                    exception);
        }
    }
}