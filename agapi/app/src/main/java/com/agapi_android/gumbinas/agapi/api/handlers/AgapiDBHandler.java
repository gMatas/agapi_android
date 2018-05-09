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
import com.agapi_android.gumbinas.agapi.api.enumerators.HealthIssueCategory;
import com.agapi_android.gumbinas.agapi.api.models.Contact;
import com.agapi_android.gumbinas.agapi.api.models.HealthIssue;
import com.agapi_android.gumbinas.agapi.api.models.Profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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
            UserProfileEntity.COLUMN_NAME_COUNTRY + " TEXT);";

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
            ContactEntity.COLUMN_NAME_PERSONAL_MESSAGE + " TEXT, " +
            ContactEntity.COLUMN_NAME_IN_QUEUE + " INTEGER);";

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

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
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
                UserProfileEntity.COLUMN_NAME_COUNTRY
            };

        Cursor cursor = db.query(UserProfileEntity.TABLE_NAME, projection,
                null, null, null, null, null);

        Profile profile = null;
        if (cursor.moveToFirst()) {
            profile = new Profile();
            profile.id = cursor.getLong(cursor.getColumnIndexOrThrow(UserProfileEntity._ID));
            profile.firstName = cursor.getString(cursor.getColumnIndexOrThrow(UserProfileEntity.COLUMN_NAME_FIRST_NAME));
            profile.lastName = cursor.getString(cursor.getColumnIndexOrThrow(UserProfileEntity.COLUMN_NAME_LAST_NAME));
            profile.birthDate = cursor.getString(cursor.getColumnIndexOrThrow(UserProfileEntity.COLUMN_NAME_BIRTH_DATE));
            profile.phone = cursor.getString(cursor.getColumnIndexOrThrow(UserProfileEntity.COLUMN_NAME_PHONE));
            profile.email = cursor.getString(cursor.getColumnIndexOrThrow(UserProfileEntity.COLUMN_NAME_EMAIL));
            profile.streetAddress = cursor.getString(cursor.getColumnIndexOrThrow(UserProfileEntity.COLUMN_NAME_STREET_ADDRESS));
            profile.city = cursor.getString(cursor.getColumnIndexOrThrow(UserProfileEntity.COLUMN_NAME_CITY));
            profile.country = cursor.getString(cursor.getColumnIndexOrThrow(UserProfileEntity.COLUMN_NAME_COUNTRY));
            Log.d("AGAPI_DEBUG", "getUserProfile: \n" + profile.toString());
        }
        cursor.close();
        return profile;
    }

    public void updateUserProfile(Profile userProfile) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UserProfileEntity.COLUMN_NAME_FIRST_NAME, userProfile.firstName);
        values.put(UserProfileEntity.COLUMN_NAME_LAST_NAME, userProfile.lastName);
        values.put(UserProfileEntity.COLUMN_NAME_BIRTH_DATE, userProfile.birthDate);
        values.put(UserProfileEntity.COLUMN_NAME_PHONE, userProfile.phone);
        values.put(UserProfileEntity.COLUMN_NAME_EMAIL, userProfile.email);
        values.put(UserProfileEntity.COLUMN_NAME_STREET_ADDRESS, userProfile.streetAddress);
        values.put(UserProfileEntity.COLUMN_NAME_CITY, userProfile.city);
        values.put(UserProfileEntity.COLUMN_NAME_COUNTRY, userProfile.country);

        String selection = UserProfileEntity._ID + " = ?";
        String[] selectionArgs = {String.valueOf(userProfile.id)};

        db.update(UserProfileEntity.TABLE_NAME, values, selection, selectionArgs);
    }

    public void removeUserProfile() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(UserProfileEntity.TABLE_NAME, null, null);
    }

    public long addHealthIssue(HealthIssue healthIssue) {
        SQLiteDatabase db = getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(HealthIssueEntity.COLUMN_NAME_CATEGORY, healthIssue.category.name());
        values.put(HealthIssueEntity.COLUMN_NAME_DESCRIPTION, healthIssue.description);

        // Insert values into the database
        return db.insert(HealthIssueEntity.TABLE_NAME, null, values);
    }

    public List<HealthIssue> getAllHealthIssues() {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                HealthIssueEntity._ID,
                HealthIssueEntity.COLUMN_NAME_CATEGORY,
                HealthIssueEntity.COLUMN_NAME_DESCRIPTION
        };
        Cursor cursor = db.query(HealthIssueEntity.TABLE_NAME, projection,
                null, null, null, null, null);

        List<HealthIssue> healthIssues = new ArrayList<>();
        while (cursor.moveToNext()) {
            HealthIssue issue = new HealthIssue();
            issue.id = cursor.getLong(cursor.getColumnIndexOrThrow(HealthIssueEntity._ID));
            String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(HealthIssueEntity.COLUMN_NAME_CATEGORY));
            issue.category = HealthIssueCategory.valueOf(categoryName);
            issue.description = cursor.getString(cursor.getColumnIndexOrThrow(HealthIssueEntity.COLUMN_NAME_DESCRIPTION));
            healthIssues.add(issue);
        }
        cursor.close();
        return healthIssues;
    }

    public void updateHealthIssue(HealthIssue healthIssue) {
        ContentValues values = new ContentValues();
        values.put(HealthIssueEntity.COLUMN_NAME_CATEGORY, healthIssue.category.name());
        values.put(HealthIssueEntity.COLUMN_NAME_DESCRIPTION, healthIssue.description);

        String selection = HealthIssueEntity._ID + " = ?";
        String[] selectionArgs = {String.valueOf(healthIssue.id)};

        SQLiteDatabase db = getWritableDatabase();
        db.update(HealthIssueEntity.TABLE_NAME, values, selection, selectionArgs);
    }

    public void removeHealthIssue(long id) {
        SQLiteDatabase db = getWritableDatabase();
        String selection = HealthIssueEntity._ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        db.delete(HealthIssueEntity.TABLE_NAME, selection, selectionArgs);
    }

    public Contact getContact(long id) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                ContactEntity.COLUMN_NAME_NAME,
                ContactEntity.COLUMN_NAME_PHONE,
                ContactEntity.COLUMN_NAME_EMAIL,
                ContactEntity.COLUMN_NAME_PERSONAL_MESSAGE,
                ContactEntity.COLUMN_NAME_IN_QUEUE
        };
        String selection = ContactEntity._ID + "= ?";
        String[] selectionArgs = { String.valueOf(id) };

        Cursor cursor = db.query(ContactEntity.TABLE_NAME, projection, selection, selectionArgs,
                null, null, null);

        Contact contact = null;
        if (cursor.moveToFirst()) {
            contact = new Contact();
            contact.id = cursor.getLong(cursor.getColumnIndexOrThrow(ContactEntity._ID));
            contact.name = cursor.getString(cursor.getColumnIndexOrThrow(ContactEntity.COLUMN_NAME_NAME));
            contact.phone = cursor.getString(cursor.getColumnIndexOrThrow(ContactEntity.COLUMN_NAME_PHONE));
            contact.email = cursor.getString(cursor.getColumnIndexOrThrow(ContactEntity.COLUMN_NAME_EMAIL));
            contact.personalMessage = cursor.getString(
                    cursor.getColumnIndexOrThrow(ContactEntity.COLUMN_NAME_PERSONAL_MESSAGE));
            contact.inQueue = 1 == cursor.getInt(cursor.getColumnIndexOrThrow(ContactEntity.COLUMN_NAME_IN_QUEUE));
            Log.d("AGAPI_DEBUG", "getContact: \n" + contact.toString());
        }
        cursor.close();
        return contact;
    }

    public List<Contact> getAllContacts() {
        String[] projection = {
                ContactEntity._ID,
                ContactEntity.COLUMN_NAME_NAME,
                ContactEntity.COLUMN_NAME_PHONE,
                ContactEntity.COLUMN_NAME_EMAIL,
                ContactEntity.COLUMN_NAME_PERSONAL_MESSAGE,
                ContactEntity.COLUMN_NAME_IN_QUEUE
        };

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(ContactEntity.TABLE_NAME, projection,
                null, null, null, null, null);

        List<Contact> contacts = new ArrayList<>();
        while (cursor.moveToNext()) {
            Contact contact = new Contact();
            contact.id = cursor.getLong(cursor.getColumnIndexOrThrow(ContactEntity._ID));
            contact.name = cursor.getString(cursor.getColumnIndexOrThrow(ContactEntity.COLUMN_NAME_NAME));
            contact.phone = cursor.getString(cursor.getColumnIndexOrThrow(ContactEntity.COLUMN_NAME_PHONE));
            contact.email = cursor.getString(cursor.getColumnIndexOrThrow(ContactEntity.COLUMN_NAME_EMAIL));
            contact.personalMessage = cursor.getString(
                    cursor.getColumnIndexOrThrow(ContactEntity.COLUMN_NAME_PERSONAL_MESSAGE));
            contact.inQueue = 1 == cursor.getInt(cursor.getColumnIndexOrThrow(ContactEntity.COLUMN_NAME_IN_QUEUE));
            contacts.add(contact);
        }
        cursor.close();
        return contacts;
    }

    public long addContact(Contact contact) {
        ContentValues values = new ContentValues();
        values.put(ContactEntity.COLUMN_NAME_NAME, contact.name);
        values.put(ContactEntity.COLUMN_NAME_PHONE, contact.phone);
        values.put(ContactEntity.COLUMN_NAME_EMAIL, contact.email);
        values.put(ContactEntity.COLUMN_NAME_PERSONAL_MESSAGE, contact.personalMessage);
        values.put(ContactEntity.COLUMN_NAME_IN_QUEUE, contact.inQueue);

        SQLiteDatabase db = getWritableDatabase();
        return db.insert(ContactEntity.TABLE_NAME, null, values);
    }

    public void updateContact(Contact contact) {
        ContentValues values = new ContentValues();
        values.put(ContactEntity.COLUMN_NAME_NAME, contact.name);
        values.put(ContactEntity.COLUMN_NAME_PHONE, contact.phone);
        values.put(ContactEntity.COLUMN_NAME_EMAIL, contact.email);
        values.put(ContactEntity.COLUMN_NAME_PERSONAL_MESSAGE, contact.personalMessage);
        values.put(ContactEntity.COLUMN_NAME_IN_QUEUE, contact.inQueue);

        String selection = ContactEntity._ID + " = ?";
        String[] selectionArgs = {String.valueOf(contact.id)};

        SQLiteDatabase db = getWritableDatabase();
        db.update(ContactEntity.TABLE_NAME, values, selection, selectionArgs);
    }

    public void removeContact(long id) {
        String selection = ContactEntity._ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        SQLiteDatabase db = getWritableDatabase();
        db.delete(ContactEntity.TABLE_NAME, selection, selectionArgs);
    }

    public void dropAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(SQL_DROP_USER_ENTITY);
        db.execSQL(SQL_DROP_HEALTH_ISSUE_ENTITY);
        db.execSQL(SQL_DROP_ISSUE_DETAIL_ENTITY);
        db.execSQL(SQL_DROP_CONTACT_ENTITY);

        onCreate(db);
    }
}
