package com.example.pettrackerapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class PetDatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "pets.sqlite";
    public static  final int DB_VERSION = 1;

    public PetDatabaseHelper(@Nullable Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "CREATE TABLE pets("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT, "
                + "type TEXT, "
                + "drawable TEXT, "
                + "homeLat DOUBLE,"
                + "homeLong DOUBLE,"
                + "petLat DOUBLE,"
                + "petLong DOUBLE);";
        sqLiteDatabase.execSQL(query);

    }

    public void insertData(PetEntry petEntry){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues dataValues = new ContentValues();
        dataValues.put("name", petEntry.name);
        dataValues.put("type", petEntry.type);
        dataValues.put("drawable", "drawing stuff");
        dataValues.put("homeLat", 0);
        dataValues.put("homeLong", 0);
        dataValues.put("petLat", 0);
        dataValues.put("petLong", 0);
        db.insert("pets", null, dataValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }
}
