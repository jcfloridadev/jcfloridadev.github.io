package com.example.weighttrackerjustincarloflorida;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

/*
 * Developer: Justin Carlo Florida
 * Course: CS 360 - Mobile Architecture and Programming
 * Project Three: Weight-Tracking App
 *
 * This activity allows the logged-in user to review and update
 * goal information stored in the SQLite database.
 */
public class EditGoalActivity extends AppCompatActivity {

    private DatabaseWT databaseWT;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the goal-editing interface.
        setContentView(R.layout.activity_edit_goal);

        // Connect to the database and retrieve the current user.
        databaseWT = new DatabaseWT(this);
        userId = getIntent().getLongExtra("USER_ID", -1);

        TextView textViewCurrentGoal =
                findViewById(R.id.textViewCurrentGoal);

        EditText editTextNewGoalWeight =
                findViewById(R.id.editTextNewGoalWeight);

        EditText editTextGoalDate =
                findViewById(R.id.editTextGoalDate);

        EditText editTextGoalNote =
                findViewById(R.id.editTextGoalNote);

        Button buttonSaveGoal =
                findViewById(R.id.buttonSaveGoal);

        Button buttonCancelGoal =
                findViewById(R.id.buttonCancelGoal);

        // Display the currently saved goal if one exists.
        double savedGoal =
                databaseWT.getGoalWeight(userId);

        if (savedGoal >= 0) {

            textViewCurrentGoal.setText(
                    String.format(
                            Locale.US,
                            "%.1f lb",
                            savedGoal));

        } else {

            textViewCurrentGoal.setText(
                    "No goal set");
        }

        // Save the user's updated goal information.
        buttonSaveGoal.setOnClickListener(view -> {

            String goalWeightText =
                    editTextNewGoalWeight
                            .getText()
                            .toString()
                            .trim();

            String goalDate =
                    editTextGoalDate
                            .getText()
                            .toString()
                            .trim();

            String goalNote =
                    editTextGoalNote
                            .getText()
                            .toString()
                            .trim();

            if (goalWeightText.isEmpty()) {

                Toast.makeText(
                        EditGoalActivity.this,
                        "Please enter a goal weight.",
                        Toast.LENGTH_SHORT).show();

                return;
            }

            if (userId == -1) {

                Toast.makeText(
                        EditGoalActivity.this,
                        "Unable to identify the current user.",
                        Toast.LENGTH_SHORT).show();

                return;
            }

            try {

                double goalWeight =
                        Double.parseDouble(goalWeightText);

                boolean updated =
                        databaseWT.updateGoal(
                                userId,
                                goalWeight,
                                goalDate,
                                goalNote);

                if (updated) {

                    Toast.makeText(
                            EditGoalActivity.this,
                            "Goal updated.",
                            Toast.LENGTH_SHORT).show();

                    finish();

                } else {

                    Toast.makeText(
                            EditGoalActivity.this,
                            "Unable to update goal.",
                            Toast.LENGTH_SHORT).show();
                }

            } catch (NumberFormatException exception) {

                Toast.makeText(
                        EditGoalActivity.this,
                        "Please enter a valid goal weight.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Return to the dashboard without saving changes.
        buttonCancelGoal.setOnClickListener(view -> finish());
    }
}