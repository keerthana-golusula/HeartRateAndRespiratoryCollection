package com.example.DiaSheild;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;

public class DBHelper extends SQLiteOpenHelper
{
    String[] Symptoms = new String[] {"Nausea", "Headache", "Diarea", "SoarThroat", "Fever", "MuscleAche", "LossofSmellorTaste", "cough", "Shortnessofbreath", "FeelingTired" };

    public DBHelper(Context context) {
        super(context, "RatesAndSymptomsDB.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            String Query = "create Table RatesAndSymptomsTable (lastname Text primary key, HeartRate INT, Respiratory INT";
            for (int i = 0; i < 10; i++) {
                Query += ", " + Symptoms[i] + " FLOAT ";
            }

            Query += ")";

            db.execSQL(Query);
        }catch(Exception ex){
            Log.i("Exception", "Exception during Table creation"+ex);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("drop Table if exists RatesAndSymptomsTable");

    }

    public Boolean updateSignsDatatoDB(String lastname, Integer HeartRate, Integer Respiratory)
    {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentvalues = new ContentValues();
            contentvalues.put("lastname", lastname);
            contentvalues.put("HeartRate", HeartRate);
            contentvalues.put("Respiratory", Respiratory);
            final Cursor cursor = db.rawQuery("SELECT * FROM RatesAndSymptomsTable WHERE lastname = ?", new String[]{lastname});
            if (cursor.getCount()>0) {
                long result = db.update("RatesAndSymptomsTable", contentvalues, "lastname = ?", new String[]{lastname});
                if (result == -1) {
                    Log.i("SignUpload", "updated signs for the existing user");
                    return false;
                }
            } else {
                long result = db.insert("RatesAndSymptomsTable", null, contentvalues);
                if (result == -1) {
                    Log.i("SignUpload", "inserted signs for the new user");
                    return false;
                }
            }
        }catch(Exception ex){
            Log.i("Exception", "Exception during Symptoms upload"+ex);
        }
        return true;
    }

    public Boolean updateSymptomsDatatoDB(String lastname, Integer HeartRate, Integer Respiratory, HashMap<String, Float> Symptomslist)
    {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentvalues = new ContentValues();
            contentvalues.put("lastname", lastname);
            contentvalues.put("HeartRate", HeartRate);
            contentvalues.put("Respiratory", Respiratory);
            for (int i = 0; i < 10; i++) {
                String symptom = Symptoms[i];
                contentvalues.put(symptom, Symptomslist.get(symptom));
            }
            Cursor cursor = db.rawQuery("select * from RatesAndSymptomsTable where lastname = ?", new String[]{lastname});
            if (cursor.getCount()>0) {
                long result = db.update("RatesAndSymptomsTable", contentvalues, "lastname = ?", new String[]{lastname});
                if (result == -1) {
                    Log.i("SymptomUpload", "updated symptoms for the existing user");
                    return false;
                }
               // return true;
            } else {
                long result = db.insert("RatesAndSymptomsTable", null, contentvalues);
                if (result == -1) {
                    Log.i("SymptomUpload", "inserted symptoms for the existing user");
                    return false;
                }
            }
        }catch(Exception ex){
            Log.i("Exception", "Exception during Symptoms upload"+ex);
        }
        return true;
    }


}
