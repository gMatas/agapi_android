package com.agapi_android.gumbinas.agapi.api.handlers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.agapi_android.gumbinas.agapi.api.entities.ContactEntity;
import com.agapi_android.gumbinas.agapi.api.entities.HealthIssueEntity;
import com.agapi_android.gumbinas.agapi.api.entities.IssueDetailEntity;
import com.agapi_android.gumbinas.agapi.api.entities.UserProfileEntity;
import com.agapi_android.gumbinas.agapi.api.models.Contact;
import com.agapi_android.gumbinas.agapi.api.models.HealthIssue;
import com.agapi_android.gumbinas.agapi.api.models.Profile;

import java.util.ArrayList;
import java.util.List;


public class AgapiDBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "agapi.db";

    private static final String SQL_CREATE_USER_ENTITY =
            "CREATE TABLE IF NOT EXISTS " + UserProfileEntity.TABLE_NAME + " (" +
            UserProfileEntity._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            UserProfileEntity.COLUMN_NAME_FIRST_NAME + " TEXT, " +
            UserProfileEntity.COLUMN_NAME_LAST_NAME + " TEXT, " +
            UserProfileEntity.COLUMN_NAME_BIRTH_DATE + " TEXT, " +
            UserProfileEntity.COLUMN_NAME_PHONE + " TEXT, " +
            UserProfileEntity.COLUMN_NAME_EMAIL + " TEXT, " +
            UserProfileEntity.COLUMN_NAME_STREET_ADDRESS + " TEXT, " +
            UserProfileEntity.COLUMN_NAME_CITY + " TEXT, " +
            UserProfileEntity.COLUMN_NAME_COUNTRY + " TEXT, " +
//            UserProfileEntity.COLUMN_NAME_EMERGENCY_CONTACT_ID + " INTEGER);";
            UserProfileEntity.COLUMN_NAME_EMERGENCY_CONTACT_ID + " INTEGER DEFAULT NULL REFERENCES " +
            ContactEntity.TABLE_NAME + "(" + ContactEntity._ID + "));";

    private static final String SQL_CREATE_HEALTH_ISSUE_ENTITY =
            "CREATE TABLE IF NOT EXISTS " + HealthIssueEntity.TABLE_NAME + " (" +
            HealthIssueEntity._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            HealthIssueEntity.COLUMN_NAME_CATEGORY + " INTEGER, " +
            HealthIssueEntity.COLUMN_NAME_DESCRIPTION + " TEXT);";

    private static final String SQL_CREATE_ISSUE_DETAIL_ENTITY =
            "CREATE TABLE IF NOT EXISTS " + IssueDetailEntity.TABLE_NAME + " (" +
            IssueDetailEntity.COLUMN_NAME_DETAIL_TEXT + " TEXT, " +
            IssueDetailEntity.COLUMN_NAME_FK_ISSUEDETAIL_HEALTHISSUE + " REFERENCES " +
            HealthIssueEntity.TABLE_NAME + "(" + HealthIssueEntity._ID + "));";

    private static final String SQL_CREATE_CONTACT_ENTITY =
            "CREATE TABLE IF NOT EXISTS " + ContactEntity.TABLE_NAME + " (" +
            ContactEntity._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ContactEntity.COLUMN_NAME_NAME + " TEXT, " +
            ContactEntity.COLUMN_NAME_PHONE + " TEXT, " +
            ContactEntity.COLUMN_NAME_EMAIL + " TEXT, " +
            ContactEntity.COLUMN_NAME_PERSONAL_MESSAGE + "TEXT);";

    private static final String SQL_DROP_USER_ENTITY =
            "DROP TABLE IF EXISTS " + UserProfileEntity.TABLE_NAME;

    private static final String SQL_DROP_HEALTH_ISSUE_ENTITY =
            "DROP TABLE IF EXISTS " + HealthIssueEntity.TABLE_NAME;

    private static final String SQL_DROP_ISSUE_DETAIL_ENTITY =
            "DROP TABLE IF EXISTS " + IssueDetailEntity.TABLE_NAME;

    private static final String SQL_DROP_CONTACT_ENTITY =
            "DROP TABLE IF EXISTS " + ContactEntity.TABLE_NAME;

    public AgapiDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Enable foreign key constraint
        db.execSQL("PRAGMA foreign_keys=ON;");

        // Create tables
        db.execSQL(SQL_CREATE_USER_ENTITY);
        db.execSQL(SQL_CREATE_CONTACT_ENTITY);
        db.execSQL(SQL_CREATE_ISSUE_DETAIL_ENTITY);
        db.execSQL(SQL_CREATE_HEALTH_ISSUE_ENTITY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop old tables
        db.execSQL(SQL_DROP_USER_ENTITY);
        db.execSQL(SQL_DROP_HEALTH_ISSUE_ENTITY);
        db.execSQL(SQL_DROP_ISSUE_DETAIL_ENTITY);
        db.execSQL(SQL_DROP_CONTACT_ENTITY);

        // Create new upgraded tables
        onCreate(db);
    }

    public long addUserProfile(Profile userProfile) {
        // Get instance of database and open it with write mode
        SQLiteDatabase db = getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(UserProfileEntity.COLUMN_NAME_FIRST_NAME, userProfile.firstName);
        values.put(UserProfileEntity.COLUMN_NAME_LAST_NAME, userProfile.lastName);
        values.put(UserProfileEntity.COLUMN_NAME_BIRTH_DATE, userProfile.birthDate);
        values.put(UserProfileEntity.COLUMN_NAME_PHONE, userProfile.phone);
        values.put(UserProfileEntity.COLUMN_NAME_EMAIL, userProfile.email);
        values.put(UserProfileEntity.COLUMN_NAME_STREET_ADDRESS, userProfile.streetAddress);
        values.put(UserProfileEntity.COLUMN_NAME_CITY, userProfile.city);
        values.put(UserProfileEntity.COLUMN_NAME_COUNTRY, userProfile.country);

        // Insert values into the database
        return db.insert(UserProfileEntity.TABLE_NAME, null, values);
    }

    public Profile getUserProfile() {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                UserProfileEntity._ID,
                UserProfileEntity.COLUMN_NAME_FIRST_NAME,
                UserProfileEntity.COLUMN_NAME_LAST_NAME,
                UserProfileEntity.COLUMN_NAME_BIRTH_DATE,
                UserProfileEntity.COLUMN_NAME_PHONE,
                UserProfileEntity.COLUMN_NAME_EMAIL,
                UserProfileEntity.COLUMN_NAME_STREET_ADDRESS,
                UserProfileEntity.COLUMN_NAME_CITY,
                UserProfileEntity.COLUMN_NAME_COUNTRY,
                UserProfileEntity.COLUMN_NAME_EMERGENCY_CONTACT_ID
            };

        Cursor cursor = db.query(UserProfileEntity.TABLE_NAME, projection,
                null, null, null, null, null);

        Profile profile = new Profile();
        if (cursor.moveToFirst()) {
            profile.id = cursor.getLong(cursor.getColumnIndexOrThrow(UserProfileEntity._ID));
            profile.firstName = cursor.getString(cursor.getColumnIndexOrThrow(UserProfileEntity.COLUMN_NAME_FIRST_NAME));
            profile.lastName = cursor.getString(cursor.getColumnIndexOrThrow(UserProfileEntity.COLUMN_NAME_LAST_NAME));
            profile.birthDate = cursor.getString(cursor.getColumnIndexOrThrow(UserProfileEntity.COLUMN_NAME_BIRTH_DATE));
            profile.phone = cursor.getString(cursor.getColumnIndexOrThrow(UserProfileEntity.COLUMN_NAME_PHONE));
            profile.email = cursor.getString(cursor.getColumnIndexOrThrow(UserProfileEntity.COLUMN_NAME_EMAIL));
            profile.streetAddress = cursor.getString(cursor.getColumnIndexOrThrow(UserProfileEntity.COLUMN_NAME_STREET_ADDRESS));
            profile.city = cursor.getString(cursor.getColumnIndexOrThrow(UserProfileEntity.COLUMN_NAME_CITY));
            profile.country = cursor.getString(cursor.getColumnIndexOrThrow(UserProfileEntity.COLUMN_NAME_COUNTRY));
            Contact emergencyContact = new Contact();
            emergencyContact.id = cursor.getLong(cursor.getColumnIndexOrThrow(UserProfileEntity.COLUMN_NAME_EMERGENCY_CONTACT_ID));
            cursor.close();

            Log.d("AGAPI_DEBUG", "getUserProfile: \n" + profile.toString());
            return profile;
        } else {
            cursor.close();
            return null;
        }
    }

    public void updateUserProfile(Profile userProfile) {

    }

    public void removeUserProfile(Profile userProfile) {

    }

    public long addHealthIssue(HealthIssue healthIssue) {
        SQLiteDatabase db = getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(HealthIssueEntity.COLUMN_NAME_CATEGORY, healthIssue.category.getName());
        values.put(HealthIssueEntity.COLUMN_NAME_DESCRIPTION, healthIssue.description);

        // Insert values into the database
        return db.insert(HealthIssueEntity.TABLE_NAME, null, values);
    }

    public Contact getContact(long id) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                ContactEntity.COLUMN_NAME_NAME,
                ContactEntity.COLUMN_NAME_PHONE,
                ContactEntity.COLUMN_NAME_EMAIL,
                ContactEntity.COLUMN_NAME_PERSONAL_MESSAGE
        };
        String selection = ContactEntity._ID + "= ?";
        String[] selectionArgs = { String.valueOf(id) };

        Cursor cursor = db.query(ContactEntity.TABLE_NAME, projection, selection, selectionArgs,
                null, null, null);

        Contact contact = new Contact();
        if (cursor.moveToFirst()) {
            contact.id = cursor.getLong(cursor.getColumnIndexOrThrow(ContactEntity._ID));
            contact.name = cursor.getString(cursor.getColumnIndexOrThrow(ContactEntity.COLUMN_NAME_NAME));
            contact.phone = cursor.getString(cursor.getColumnIndexOrThrow(ContactEntity.COLUMN_NAME_PHONE));
            contact.email = cursor.getString(cursor.getColumnIndexOrThrow(ContactEntity.COLUMN_NAME_EMAIL));
            contact.personalMessage = cursor.getString(
                    cursor.getColumnIndexOrThrow(ContactEntity.COLUMN_NAME_PERSONAL_MESSAGE));
            cursor.close();

            Log.d("AGAPI_DEBUG", "getContact: \n" + contact.toString());
            return contact;
        } else {
            cursor.close();
            return null;
        }
    }

    public void dropAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(SQL_DROP_USER_ENTITY);
        db.execSQL(SQL_DROP_HEALTH_ISSUE_ENTITY);
        db.execSQL(SQL_DROP_ISSUE_DETAIL_ENTITY);
        db.execSQL(SQL_DROP_CONTACT_ENTITY);

        db.execSQL(SQL_CREATE_USER_ENTITY);
        db.execSQL(SQL_CREATE_CONTACT_ENTITY);
        db.execSQL(SQL_CREATE_HEALTH_ISSUE_ENTITY);
        db.execSQL(SQL_CREATE_ISSUE_DETAIL_ENTITY);
    }
}
