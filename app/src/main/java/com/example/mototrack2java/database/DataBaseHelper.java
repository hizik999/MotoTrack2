package com.example.mototrack2java.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mototrack2java.domain.Moto;

import java.util.ArrayList;
import java.util.List;

public class DataBaseHelper extends SQLiteOpenHelper implements DataBaseHelperInterface{

    private static final String DATABASE_NAME = "mototrack2.db";
    private static final int SCHEMA = 1;

    private final String TABLE_MOTO = "moto";
    private final String COLUMN_ID = "id";
    private final String COLUMN_LAT = "lat";
    private final String COLUMN_LON = "lon";


    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String createTableMotoStatement = "CREATE TABLE IF NOT EXISTS " + TABLE_MOTO + " (" + COLUMN_ID + " BIGINT, " + COLUMN_LAT + " FLOAT, " + COLUMN_LON + ")";
        sqLiteDatabase.execSQL(createTableMotoStatement);
        //sqLiteDatabase.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_MOTO);
        onCreate(sqLiteDatabase);
        //sqLiteDatabase.close();
    }


    @Override
    public boolean addOne(Moto moto) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_ID, moto.getId());
        cv.put(COLUMN_LAT, moto.getLat());
        cv.put(COLUMN_LON, moto.getLon());

        long insert = db.insert(TABLE_MOTO, null, cv);
        db.close();
        return insert != -1;
    }


    @Override
    public List<Moto> getAllMoto() {
        List<Moto> list = new ArrayList<>();

        String query = "SELECT * FROM " + TABLE_MOTO;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {

            do {
                long id = cursor.getLong(0);
                float lat = cursor.getFloat(1);
                float lon = cursor.getFloat(2);

                Moto moto = new Moto(id, lat, lon);
                list.add(moto);

                Log.d("DB_ADD_MOTO", "true");

            } while (cursor.moveToNext());
        } else {
            Log.d("DB_ADD_MOTO", "false");
        }

        cursor.close();
        db.close();

        return list;
    }

    @Override
    public void dropTableMoto() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_MOTO);

        onCreate(sqLiteDatabase);
        sqLiteDatabase.close();
    }

    @Override
    public void cleanTableMoto() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.execSQL("DELETE FROM " + TABLE_MOTO);
        sqLiteDatabase.close();
    }

}
