package com.example.o_k;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DBHelper extends SQLiteOpenHelper {

    //생성자 - database 파일을 생성한다.
    /*
    public DBHelper(Context context){
        super(context, "MyDB", null, 1);
    }*/

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    //DB 처음 만들때 호출. - 테이블 생성 등의 초기 처리.
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE closet (_id integer primary key autoincrement, category TEXT, thickness TEXT, length TEXT, image BLOB);");
    }

    //DB 업그레이드 필요 시 호출. (version값에 따라 반응)
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS closet");
        onCreate(db);
    }

}
