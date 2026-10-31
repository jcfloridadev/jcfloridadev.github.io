package com.example.weighttrackerjustincarloflorida;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/*
 * Developer: Justin Carlo Florida
 * Course: CS 360 - Mobile Architecture and Programming
 * Project Three: Weight-Tracking App
 *
 * This class creates and manages the SQLite database used by the
 * Weight Tracker application. The database stores user accounts,
 * goal information, SMS settings, and daily weight records.
 */
public class DatabaseWT extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "weight_tracker.db";
    private static final int DATABASE_VERSION = 1;

    // Users table and column names.
    public static final String TABLE_USERS = "users";
    public static final String USER_ID = "id";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String GOAL_WEIGHT = "goal_weight";
    public static final String GOAL_DATE = "goal_date";
    public static final String GOAL_NOTE = "goal_note";
    public static final String PHONE_NUMBER = "phone_number";
    public static final String SMS_ENABLED = "sms_enabled";

    // Weight-history table and column names.
    public static final String TABLE_WEIGHTS = "weights";
    public static final String WEIGHT_ID = "id";
    public static final String WEIGHT_USER_ID = "user_id";
    public static final String WEIGHT_VALUE = "weight";
    public static final String WEIGHT_DATE = "date";
    public static final String WEIGHT_NOTES = "notes";

    /*
     * Creates the database helper using the application's context.
     */
    public DatabaseWT(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /*
     * Creates the database tables the first time the database is opened.
     */
    @Override
    public void onCreate(SQLiteDatabase database) {

        String createUsersTable =
                "CREATE TABLE " + TABLE_USERS + " (" +
                        USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        USERNAME + " TEXT UNIQUE NOT NULL, " +
                        PASSWORD + " TEXT NOT NULL, " +
                        GOAL_WEIGHT + " REAL, " +
                        GOAL_DATE + " TEXT, " +
                        GOAL_NOTE + " TEXT, " +
                        PHONE_NUMBER + " TEXT, " +
                        SMS_ENABLED + " INTEGER DEFAULT 0)";

        String createWeightsTable =
                "CREATE TABLE " + TABLE_WEIGHTS + " (" +
                        WEIGHT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        WEIGHT_USER_ID + " INTEGER NOT NULL, " +
                        WEIGHT_VALUE + " REAL NOT NULL, " +
                        WEIGHT_DATE + " TEXT NOT NULL, " +
                        WEIGHT_NOTES + " TEXT)";

        database.execSQL(createUsersTable);
        database.execSQL(createWeightsTable);
    }

    /*
     * Recreates database tables if the database structure is upgraded.
     */
    @Override
    public void onUpgrade(
            SQLiteDatabase database,
            int oldVersion,
            int newVersion) {

        database.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGHTS);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        onCreate(database);
    }

    /*
     * Adds a new user account to the database.
     * Returns true if the account was created successfully.
     */
    public boolean addUser(String username, String password) {

        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(USERNAME, username);
        values.put(PASSWORD, password);

        long result = database.insert(
                TABLE_USERS,
                null,
                values);

        return result != -1;
    }

    /*
     * Checks whether a username already exists in the database.
     */
    public boolean usernameExists(String username) {

        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query(
                TABLE_USERS,
                new String[]{USER_ID},
                USERNAME + " = ?",
                new String[]{username},
                null,
                null,
                null);

        boolean exists = cursor.moveToFirst();

        cursor.close();

        return exists;
    }

    /*
     * Validates a user's login credentials against the database.
     */
    public boolean checkUser(String username, String password) {

        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query(
                TABLE_USERS,
                new String[]{USER_ID},
                USERNAME + " = ? AND " + PASSWORD + " = ?",
                new String[]{username, password},
                null,
                null,
                null);

        boolean validUser = cursor.moveToFirst();

        cursor.close();

        return validUser;
    }

    /*
     * Returns the database ID for a user account.
     * Returns -1 if the username cannot be found.
     */
    public long getUserId(String username) {

        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query(
                TABLE_USERS,
                new String[]{USER_ID},
                USERNAME + " = ?",
                new String[]{username},
                null,
                null,
                null);

        long userId = -1;

        if (cursor.moveToFirst()) {
            userId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(USER_ID));
        }

        cursor.close();

        return userId;
    }

    /*
     * Adds a new weight record for the specified user.
     * Returns true if the weight entry was saved successfully.
     */
    public boolean addWeight(
            long userId,
            double weight,
            String date,
            String notes) {

        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(WEIGHT_USER_ID, userId);
        values.put(WEIGHT_VALUE, weight);
        values.put(WEIGHT_DATE, date);
        values.put(WEIGHT_NOTES, notes);

        long result = database.insert(
                TABLE_WEIGHTS,
                null,
                values);

        return result != -1;
    }

    /*
     * Returns all weight records belonging to the specified user.
     * Newer entries are returned first.
     */
    public Cursor getWeightsForUser(long userId) {

        SQLiteDatabase database = this.getReadableDatabase();

        return database.query(
                TABLE_WEIGHTS,
                null,
                WEIGHT_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                WEIGHT_ID + " DESC");
    }

    /*
     * Deletes a specific weight record belonging to the specified user.
     * Returns true if a record was successfully removed.
     */
    public boolean deleteWeight(long weightId, long userId) {

        SQLiteDatabase database = this.getWritableDatabase();

        int rowsDeleted = database.delete(
                TABLE_WEIGHTS,
                WEIGHT_ID + " = ? AND " + WEIGHT_USER_ID + " = ?",
                new String[]{
                        String.valueOf(weightId),
                        String.valueOf(userId)
                });

        return rowsDeleted > 0;
    }

    /*
     * Updates a specific weight record belonging to the specified user.
     * Returns true if the record was successfully changed.
     */
    public boolean updateWeight(
            long weightId,
            long userId,
            double weight,
            String date,
            String notes) {

        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(WEIGHT_VALUE, weight);
        values.put(WEIGHT_DATE, date);
        values.put(WEIGHT_NOTES, notes);

        int rowsUpdated = database.update(
                TABLE_WEIGHTS,
                values,
                WEIGHT_ID + " = ? AND " + WEIGHT_USER_ID + " = ?",
                new String[]{
                        String.valueOf(weightId),
                        String.valueOf(userId)
                });

        return rowsUpdated > 0;
    }

    /*
     * Saves the user's goal weight, target date, and goal note.
     * Returns true if the user's goal information was updated.
     */
    public boolean updateGoal(
            long userId,
            double goalWeight,
            String goalDate,
            String goalNote) {

        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(GOAL_WEIGHT, goalWeight);
        values.put(GOAL_DATE, goalDate);
        values.put(GOAL_NOTE, goalNote);

        int rowsUpdated = database.update(
                TABLE_USERS,
                values,
                USER_ID + " = ?",
                new String[]{String.valueOf(userId)});

        return rowsUpdated > 0;
    }

    /*
     * Returns the saved goal weight for the specified user.
     * Returns -1 if no goal weight has been saved.
     */
    public double getGoalWeight(long userId) {

        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query(
                TABLE_USERS,
                new String[]{GOAL_WEIGHT},
                USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                null);

        double goalWeight = -1;

        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            goalWeight = cursor.getDouble(0);
        }

        cursor.close();

        return goalWeight;
    }

    /*
     * Returns the user's most recently saved weight record.
     * Returns -1 if the user has no saved weight entries.
     */
    public double getLatestWeight(long userId) {

        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query(
                TABLE_WEIGHTS,
                new String[]{WEIGHT_VALUE},
                WEIGHT_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                WEIGHT_ID + " DESC",
                "1");

        double latestWeight = -1;

        if (cursor.moveToFirst()) {
            latestWeight = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(WEIGHT_VALUE));
        }

        cursor.close();

        return latestWeight;
    }

    /*
     * Saves the user's phone number and SMS notification preference.
     * Returns true if the user's settings were updated successfully.
     */
    public boolean updateSmsSettings(
            long userId,
            String phoneNumber,
            boolean smsEnabled) {

        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PHONE_NUMBER, phoneNumber);
        values.put(SMS_ENABLED, smsEnabled ? 1 : 0);

        int rowsUpdated = database.update(
                TABLE_USERS,
                values,
                USER_ID + " = ?",
                new String[]{String.valueOf(userId)});

        return rowsUpdated > 0;
    }

    /*
     * Returns the saved phone number for the specified user.
     * Returns an empty string if no phone number has been saved.
     */
    public String getPhoneNumber(long userId) {

        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query(
                TABLE_USERS,
                new String[]{PHONE_NUMBER},
                USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                null);

        String phoneNumber = "";

        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            phoneNumber = cursor.getString(0);
        }

        cursor.close();

        return phoneNumber;
    }

    /*
     * Returns whether SMS notifications are enabled for the user.
     */
    public boolean isSmsEnabled(long userId) {

        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query(
                TABLE_USERS,
                new String[]{SMS_ENABLED},
                USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                null);

        boolean enabled = false;

        if (cursor.moveToFirst()) {
            enabled = cursor.getInt(0) == 1;
        }

        cursor.close();

        return enabled;
    }

}