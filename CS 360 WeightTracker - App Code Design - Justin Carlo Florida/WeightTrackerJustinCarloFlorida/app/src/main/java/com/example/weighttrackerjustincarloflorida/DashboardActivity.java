package com.example.weighttrackerjustincarloflorida;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

/*
 * Developer: Justin Carlo Florida
 * Course: CS 360 - Mobile Architecture and Programming
 * Project Three: Weight-Tracking App
 *
 * This activity displays the logged-in user's weight summary,
 * goal information, weight history, and account controls.
 * Weight-history records are retrieved from the SQLite database
 * and displayed dynamically in the dashboard table.
 */
public class DashboardActivity extends AppCompatActivity {

    private long userId;
    private String username;

    private DatabaseWT databaseWT;
    private TableLayout tableLayoutWeightHistory;
    private TextView textViewCurrentWeight;
    private TextView textViewGoalWeight;
    private TextView textViewProgressSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the dashboard and weight-history interface.
        setContentView(R.layout.activity_dashboard);

        // Retrieve the logged-in user's information from MainActivity.
        userId = getIntent().getLongExtra("USER_ID", -1);
        username = getIntent().getStringExtra("USERNAME");

        // Connect this activity to the Weight Tracker SQLite database.
        databaseWT = new DatabaseWT(this);

        // Connect the weight-history table from the dashboard layout.
        tableLayoutWeightHistory =
                findViewById(R.id.tableLayoutWeightHistory);

        TextView textViewWelcome =
                findViewById(R.id.textViewWelcome);

        textViewCurrentWeight =
                findViewById(R.id.textViewCurrentWeight);

        textViewGoalWeight =
                findViewById(R.id.textViewGoalWeight);

        textViewProgressSummary =
                findViewById(R.id.textViewProgressSummary);

        final Button buttonAddWeight =
                findViewById(R.id.buttonAddWeight);

        final Button buttonEditGoal =
                findViewById(R.id.buttonEditGoal);

        final Button buttonSmsSettings =
                findViewById(R.id.buttonSmsSettings);

        final Button buttonLogout =
                findViewById(R.id.buttonLogout);

        // Display the username belonging to the current logged-in account.
        if (username != null && !username.isEmpty()) {
            textViewWelcome.setText(
                    "Welcome back, " + username + "!");
        }

        // Open the screen used to create a new daily weight record.
        buttonAddWeight.setOnClickListener(view -> {

            Intent intent =
                    new Intent(
                            DashboardActivity.this,
                            AddWeightActivity.class);

            // Pass the user's ID so the new weight belongs to this account.
            intent.putExtra("USER_ID", userId);

            startActivity(intent);
        });

        // Open the screen used to review and update the user's goal weight.
        buttonEditGoal.setOnClickListener(view -> {

            Intent intent =
                    new Intent(
                            DashboardActivity.this,
                            EditGoalActivity.class);

            // Pass the current user's database ID to the goal screen.
            intent.putExtra("USER_ID", userId);

            startActivity(intent);
        });

        // Open the SMS notification settings screen.
        buttonSmsSettings.setOnClickListener(view -> {

            Intent intent =
                    new Intent(
                            DashboardActivity.this,
                            SmsSettingsActivity.class);

            // Pass the current user's database ID to SMS settings.
            intent.putExtra("USER_ID", userId);

            startActivity(intent);
        });

        // Log the user out and return to the login screen.
        buttonLogout.setOnClickListener(view -> {

            Intent intent =
                    new Intent(
                            DashboardActivity.this,
                            MainActivity.class);

            // Remove the dashboard from the active navigation stack.
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
            finish();
        });
    }

    /*
     * Refreshes the weight-history table whenever the dashboard
     * becomes active again. This allows newly saved weights to
     * appear immediately after returning from AddWeightActivity.
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (databaseWT != null && userId != -1) {
            loadWeightHistory();
            loadWeightSummary();
        }
    }

    /*
     * Loads all saved weight records belonging to the logged-in user
     * and creates a table row for each record.
     */
    private void loadWeightHistory() {

        // Remove previously generated rows while preserving the header row.
        int childCount =
                tableLayoutWeightHistory.getChildCount();

        if (childCount > 1) {
            tableLayoutWeightHistory.removeViews(
                    1,
                    childCount - 1);
        }

        // Query SQLite for weight records belonging to this user.
        Cursor cursor =
                databaseWT.getWeightsForUser(userId);

        // Display a message when the user has not entered any weights yet.
        if (cursor.getCount() == 0) {

            TableRow emptyRow =
                    new TableRow(this);

            TextView emptyText =
                    new TextView(this);

            emptyText.setText("No weight entries yet.");
            emptyText.setGravity(Gravity.CENTER);
            emptyText.setPadding(12, 18, 12, 18);

            TableRow.LayoutParams emptyParams =
                    new TableRow.LayoutParams(
                            0,
                            TableRow.LayoutParams.WRAP_CONTENT,
                            1f);

            emptyText.setLayoutParams(emptyParams);

            emptyRow.addView(emptyText);

            tableLayoutWeightHistory.addView(emptyRow);

            cursor.close();

            return;
        }

        // Create one dashboard row for each weight stored in SQLite.
        while (cursor.moveToNext()) {

            long weightId =
                    cursor.getLong(
                            cursor.getColumnIndexOrThrow(
                                    DatabaseWT.WEIGHT_ID));

            String date =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    DatabaseWT.WEIGHT_DATE));

            double weight =
                    cursor.getDouble(
                            cursor.getColumnIndexOrThrow(
                                    DatabaseWT.WEIGHT_VALUE));

            String notes =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    DatabaseWT.WEIGHT_NOTES));

            TableRow row =
                    new TableRow(this);

            row.setPadding(6, 6, 6, 6);

            // Create the Date column.
            TextView dateView =
                    createTableTextView(date);

            // Create the Weight column.
            TextView weightView =
                    createTableTextView(
                            String.format(
                                    Locale.US,
                                    "%.1f lb",
                                    weight));

            // The Change column is reserved for displaying weight differences.
            TextView changeView =
                    createTableTextView("—");

            // Create an action area containing Edit and Delete controls.
            LinearLayout actionLayout =
                    new LinearLayout(this);

            actionLayout.setOrientation(
                    LinearLayout.VERTICAL);

            // Create the Edit button for this record.
            Button editButton =
                    new Button(this);

            editButton.setText("Edit");
            editButton.setTextSize(11);
            editButton.setMinWidth(0);

            // Open AddWeightActivity in edit mode.
            editButton.setOnClickListener(view -> {

                Intent intent =
                        new Intent(
                                DashboardActivity.this,
                                AddWeightActivity.class);

                // Pass the current user and selected weight record.
                intent.putExtra("USER_ID", userId);
                intent.putExtra("EDIT_MODE", true);
                intent.putExtra("WEIGHT_ID", weightId);
                intent.putExtra("WEIGHT_VALUE", weight);
                intent.putExtra("WEIGHT_DATE", date);
                intent.putExtra("WEIGHT_NOTES", notes);

                startActivity(intent);
            });

            // Create the Delete button for this record.
            Button deleteButton =
                    new Button(this);

            deleteButton.setText("Delete");
            deleteButton.setTextSize(11);
            deleteButton.setMinWidth(0);

            // Delete only the selected record belonging to this user.
            deleteButton.setOnClickListener(view -> {

                boolean deleted =
                        databaseWT.deleteWeight(
                                weightId,
                                userId);

                if (deleted) {

                    Toast.makeText(
                            DashboardActivity.this,
                            "Weight entry deleted.",
                            Toast.LENGTH_SHORT).show();

                    // Refresh the table immediately after deletion.
                    loadWeightHistory();

                } else {

                    Toast.makeText(
                            DashboardActivity.this,
                            "Unable to delete weight entry.",
                            Toast.LENGTH_SHORT).show();
                }
            });

            // Place both controls into the Action column.
            actionLayout.addView(editButton);
            actionLayout.addView(deleteButton);

            row.addView(dateView);
            row.addView(weightView);
            row.addView(changeView);
            row.addView(actionLayout);

            tableLayoutWeightHistory.addView(row);
        }

        // Close the query after all records have been displayed.
        cursor.close();
    }

    /*
     * Creates a consistently formatted TextView for each
     * cell displayed in the weight-history table.
     */
    private TextView createTableTextView(String text) {

        TextView textView =
                new TextView(this);

        textView.setText(text);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(6, 14, 6, 14);

        TableRow.LayoutParams params =
                new TableRow.LayoutParams(
                        0,
                        TableRow.LayoutParams.WRAP_CONTENT,
                        1f);

        textView.setLayoutParams(params);

        return textView;
    }

    /*
     * Loads the user's current weight and saved goal information
     * and updates the dashboard summary cards.
     */
    private void loadWeightSummary() {

        double currentWeight =
                databaseWT.getLatestWeight(userId);

        double goalWeight =
                databaseWT.getGoalWeight(userId);

        // Display the most recently saved weight entry.
        if (currentWeight >= 0) {

            textViewCurrentWeight.setText(
                    String.format(
                            Locale.US,
                            "%.1f lb",
                            currentWeight));

        } else {

            textViewCurrentWeight.setText(
                    "No entry");
        }

        // Display the user's saved goal weight.
        if (goalWeight >= 0) {

            textViewGoalWeight.setText(
                    String.format(
                            Locale.US,
                            "%.1f lb",
                            goalWeight));

        } else {

            textViewGoalWeight.setText(
                    "No goal");
        }

        // Calculate progress only when both values exist.
        if (currentWeight >= 0 && goalWeight >= 0) {

            double remaining =
                    currentWeight - goalWeight;

            if (remaining > 0) {

                textViewProgressSummary.setText(
                        String.format(
                                Locale.US,
                                "%.1f pounds remaining until your goal",
                                remaining));

            } else {

                textViewProgressSummary.setText(
                        "Goal weight reached!");
            }

        } else {

            textViewProgressSummary.setText(
                    "Add a weight entry and goal to track progress.");
        }
    }
}