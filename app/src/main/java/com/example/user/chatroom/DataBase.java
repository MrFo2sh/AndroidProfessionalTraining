package com.example.user.chatroom;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by User on 12/22/2016.
 */

public class DataBase extends SQLiteOpenHelper {
    private static DataBase DB;

    // Database Info
    private static final String DB_NAME = "ChatHistoryDB";
    private static final int DB_VERSION = 1;

    // Table Name
    private static final String TABLE_MESSAGES = "Messages";

    //Column Names
    private static final String USERNAME = "Username";
    private static final String MESSAGE = "Message";

    private DataBase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static synchronized DataBase getInstance(Context context){
        if(DB == null){
            DB = new DataBase(context.getApplicationContext());
        }
        return DB;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE "+TABLE_MESSAGES+"("+USERNAME+" TEXT,"+MESSAGE+" TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        if (i != i1) {
            // Simplest implementation is to drop all old tables and recreate them
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
            onCreate(sqLiteDatabase);
        }
    }

    public void SaveMessage(Message message) {
        SQLiteDatabase db = DB.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USERNAME,message.Username);
        values.put(MESSAGE,message.Text);
        db.insert(TABLE_MESSAGES,null,values);
    }

    public ArrayList<Message> getStoredMessages(){
        ArrayList<Message> messages = new ArrayList<Message>();
        String SQL = "SELECT * FROM "+TABLE_MESSAGES;
        SQLiteDatabase db = DB.getWritableDatabase();
        Cursor cursor = db.rawQuery(SQL,null);
        if(cursor.moveToFirst()){
            do{
                String username = cursor.getString(0);
                String text = cursor.getString(1);
                messages.add(new Message(username,text));
            }
            while(cursor.moveToNext());
        }
        return messages;
    }

    public void clearStoredMessages(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_MESSAGES);
    }

}
