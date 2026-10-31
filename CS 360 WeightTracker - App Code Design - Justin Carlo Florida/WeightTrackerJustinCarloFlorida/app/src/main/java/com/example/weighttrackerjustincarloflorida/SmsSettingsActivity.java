package com.example.weighttrackerjustincarloflorida;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/*
 * Developer: Justin Carlo Florida
 * Course: CS 360 - Mobile Architecture and Programming
 * Project Three: Weight-Tracking App
 *
 * This activity manages the user's SMS notification settings,
 * runtime permission request, phone number, and goal-weight
 * notification preference.
 */
public class SmsSettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "sms_permission_prefs";
    private static final String KEY_PERMISSION_REQUESTED = "sms_permission_requested";

    private DatabaseWT databaseWT;
    private long userId;

    private TextView textViewPermissionStatus;
    private Switch switchEnableSms;
    private EditText editTextPhoneNumber;
    private CheckBox checkBoxGoalReached;

    /*
     * Requests SEND_SMS permission and updates the screen based
     * on whether the user grants or denies the permission.
     */
    private final ActivityResultLauncher<String> smsPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {

                        if (isGranted) {

                            textViewPermissionStatus.setText(
                                    "Permission status: Granted");

                        } else {

                            textViewPermissionStatus.setText(
                                    "Permission status: Denied");
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the SMS notification settings interface.
        setContentView(R.layout.activity_sms_settings);

        // Connect to SQLite and retrieve the logged-in user's ID.
        databaseWT = new DatabaseWT(this);
        userId = getIntent().getLongExtra("USER_ID", -1);

        textViewPermissionStatus =
                findViewById(R.id.textViewPermissionStatus);

        switchEnableSms =
                findViewById(R.id.switchEnableSms);

        editTextPhoneNumber =
                findViewById(R.id.editTextPhoneNumber);

        checkBoxGoalReached =
                findViewById(R.id.checkBoxGoalReached);

        Button buttonRequestPermission =
                findViewById(R.id.buttonRequestSmsPermission);

        Button buttonSaveSmsSettings =
                findViewById(R.id.buttonSaveSmsSettings);

        Button buttonCancelSms =
                findViewById(R.id.buttonCancelSms);

        // Display the current Android permission status.
        updatePermissionStatus();

        // Load previously saved SMS settings from SQLite.
        loadSavedSettings();

        // Request SMS permission or open App Settings when Android
        // no longer presents the runtime permission dialog.
        buttonRequestPermission.setOnClickListener(view -> {

            // Permission is already granted.
            if (ContextCompat.checkSelfPermission(
                    SmsSettingsActivity.this,
                    Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(
                        SmsSettingsActivity.this,
                        "SMS permission is already granted.",
                        Toast.LENGTH_SHORT).show();

                return;
            }

            SharedPreferences preferences =
                    getSharedPreferences(
                            PREFS_NAME,
                            MODE_PRIVATE);

            boolean permissionPreviouslyRequested =
                    preferences.getBoolean(
                            KEY_PERMISSION_REQUESTED,
                            false);

            /*
             * If Android can still display the permission dialog,
             * request permission again.
             */
            if (!permissionPreviouslyRequested
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                    SmsSettingsActivity.this,
                    Manifest.permission.SEND_SMS)) {

                preferences.edit()
                        .putBoolean(
                                KEY_PERMISSION_REQUESTED,
                                true)
                        .apply();

                smsPermissionLauncher.launch(
                        Manifest.permission.SEND_SMS);

            } else {

                /*
                 * Android is no longer showing the runtime dialog.
                 * Send the user to this app's system settings instead.
                 */
                Toast.makeText(
                        SmsSettingsActivity.this,
                        "Enable SMS permission in App Settings.",
                        Toast.LENGTH_LONG).show();

                openAppSettings();
            }
        });

        // Save the user's SMS preference and phone number.
        buttonSaveSmsSettings.setOnClickListener(view -> {

            String phoneNumber =
                    editTextPhoneNumber
                            .getText()
                            .toString()
                            .trim();

            boolean smsEnabled =
                    switchEnableSms.isChecked()
                            && checkBoxGoalReached.isChecked();

            if (userId == -1) {

                Toast.makeText(
                        SmsSettingsActivity.this,
                        "Unable to identify the current user.",
                        Toast.LENGTH_SHORT).show();

                return;
            }

            // Require a phone number only when SMS is enabled.
            if (smsEnabled && phoneNumber.isEmpty()) {

                Toast.makeText(
                        SmsSettingsActivity.this,
                        "Please enter a phone number.",
                        Toast.LENGTH_SHORT).show();

                return;
            }

            boolean updated =
                    databaseWT.updateSmsSettings(
                            userId,
                            phoneNumber,
                            smsEnabled);

            if (updated) {

                Toast.makeText(
                        SmsSettingsActivity.this,
                        "SMS settings saved.",
                        Toast.LENGTH_SHORT).show();

                finish();

            } else {

                Toast.makeText(
                        SmsSettingsActivity.this,
                        "Unable to save SMS settings.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Return without saving changes.
        buttonCancelSms.setOnClickListener(view -> finish());
    }

    /*
     * Loads the user's previously saved SMS settings.
     */
    private void loadSavedSettings() {

        String savedPhoneNumber =
                databaseWT.getPhoneNumber(userId);

        boolean smsEnabled =
                databaseWT.isSmsEnabled(userId);

        editTextPhoneNumber.setText(savedPhoneNumber);
        switchEnableSms.setChecked(smsEnabled);
        checkBoxGoalReached.setChecked(smsEnabled);
    }

    /*
     * Displays whether SEND_SMS permission is currently granted.
     */
    private void updatePermissionStatus() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {

            textViewPermissionStatus.setText(
                    "Permission status: Granted");

        } else {

            textViewPermissionStatus.setText(
                    "Permission status: Not granted");
        }
    }

    /*
     * Opens Android's system settings page for this application
     * so the user can manually change SMS permission.
     */
    private void openAppSettings() {

        Intent intent =
                new Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);

        Uri packageUri =
                Uri.fromParts(
                        "package",
                        getPackageName(),
                        null);

        intent.setData(packageUri);

        startActivity(intent);
    }

    /*
     * Refreshes the displayed permission status after returning
     * from Android system settings.
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (textViewPermissionStatus != null) {
            updatePermissionStatus();
        }
    }
}