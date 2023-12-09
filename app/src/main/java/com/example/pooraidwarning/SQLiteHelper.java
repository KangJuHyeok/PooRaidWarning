package com.example.pooraidwarning;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class SQLiteHelper extends SQLiteOpenHelper {

    Context context;
    private static final String DATABASE_NAME = "test.db";
    private static final int DATABASE_VERSION = 1;

    public SQLiteHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @SuppressLint("Range")
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table if not exists reviewList (Name text primary key, recentStar integer, recentReview String, totalCall integer, totalStar integer);");

        // SharedPreferences를 통해 초기화 여부 확인
        SharedPreferences sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE);
        boolean isInitialized = sharedPreferences.getBoolean("isInitialized", false);

        if (!isInitialized) {
            // 테이블이 비어있을 때만 초기화
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT COUNT(*) FROM reviewList", null);
            if (cursor != null) {
                cursor.moveToFirst();
                int count = cursor.getInt(0);
                cursor.close();

                if (count == 0) {
                    sqLiteDatabase.execSQL("INSERT INTO reviewList VALUES ('default', 0, '', 0, 0);");

                    // 초기화가 완료되면 플래그를 설정
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isInitialized", true);
                    editor.apply();
                }
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
    @SuppressLint("Range")
    public String getRecentReview (String title){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM reviewList WHERE Name=?", new String[]{title});
        if (cursor.getCount()>0 && cursor.moveToFirst()) {
            // moveToFirst()가 true를 반환하면 결과 집합에 데이터가 있음
            String recentReview = cursor.getString(cursor.getColumnIndex("recentReview"));
            cursor.close();
            return recentReview;
        } else {
            // 결과 집합이 비어있을 경우
            cursor.close();
            return "false";
        }
    }

    public double getAvgStar (String title){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM reviewList WHERE Name=?", new String[]{title});
        if (cursor.getCount()>0 && cursor.moveToFirst()) {
            // moveToFirst()가 true를 반환하면 결과 집합에 데이터가 있음
            @SuppressLint("Range") double tmp=cursor.getInt(cursor.getColumnIndex("totalStar"));
            @SuppressLint("Range") double tmp2=cursor.getInt(cursor.getColumnIndex("totalCall"));
            cursor.close();
            double tmpp=tmp/tmp2;
            return tmpp;
        } else {
            // 결과 집합이 비어있을 경우
            cursor.close();
            return 5;
        }
    }

    public void setReview (String name, int recentStar, String recentReview, int totalCall, int totalStar){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        Cursor cursor = db.rawQuery("SELECT * FROM reviewList WHERE Name=?", new String[]{name});

        if (cursor.getCount() > 0) {
            cursor.moveToFirst(); // 커서를 첫 번째 행으로 이동
            // 이미 존재하는 경우 업데이트
            @SuppressLint("Range") int existingTotalStar = cursor.getInt(cursor.getColumnIndex("totalStar"));
            values.put("totalStar", existingTotalStar + recentStar);
            @SuppressLint("Range") int existingTotalCall = cursor.getInt(cursor.getColumnIndex("totalCall"));
            values.put("totalCall", existingTotalCall + 1);
            values.put("recentStar", recentStar);
            values.put("recentReview", recentReview);
            db.update("reviewList", values, "Name=?", new String[]{name});
        } else {
            // 존재하지 않는 경우 추가
            values.put("Name", name);
            values.put("totalStar", recentStar);
            values.put("totalCall", 1);
            values.put("recentStar", recentStar);
            values.put("recentReview", recentReview);
            db.insert("reviewList", null, values);
        }
        cursor.close();
        db.close();
    }
}