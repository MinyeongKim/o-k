package com.example.o_k;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.Tag;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "MyDB";
    static final String DATABASE_TABLE = "table_image";
    static final int DATABASE_VERSION = 1;
    static final String DATABASE_CREATE = "create table Closet (idx integer primary key autoincrement, classification TEXT NOT NULL,"
            + "thickness TEXT NOT NULL, length TEXT NOT NULL, clothe BLOB );";

    static final String TAG = "DataBaseHelper";

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db){
        try {
            db.execSQL(DATABASE_CREATE);
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        Log.w(TAG, "Upgrading database from version" + oldVersion + " to "+ newVersion
        + ", which will destroy all old data");
        db.execSQL("DROP TABlE IF EXISTS " + DATABASE_TABLE);
        onCreate(db);
    }

    public void dbInsert(String classification, String thickness, String length, byte[] image) throws SQLiteException {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("classification", classification);
        cv.put("thickness", thickness);
        cv.put("length_sleeve", length);
        cv.put("clothe", image);
        database.insert(DATABASE_TABLE, null, cv);
    }

    public void delete(int idx){
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("DELETE FROM DATABASE_NAME WHERE idx=" + idx + ";");
        database.close();
    }

}
