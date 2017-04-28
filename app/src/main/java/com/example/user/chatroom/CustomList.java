package com.example.user.chatroom;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by User on 12/19/2016.
 */

public class CustomList extends ArrayAdapter<Message> {
    Activity context;
    ArrayList<Message> arrayOfData;
    public CustomList(Activity context,ArrayList<Message> arrayOfData) {
        super(context,R.layout.message_view);
        this.context = context;
        this.arrayOfData = arrayOfData;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.message_view,null,false);
        TextView Username  = (TextView)rowView.findViewById(R.id.Username);
        TextView message = (TextView)rowView.findViewById(R.id.Message);
        Username.setText(" "+arrayOfData.get(position).Username);
        if(arrayOfData.get(position).Username.equals("ChatRoom")){
            Username.setTextColor(Color.RED);
        }else if(arrayOfData.get(position).Username.equals(ChatRoom.Username)){
            Username.setTextColor(Color.parseColor("#00b0ff"));
            Username.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            message.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        }
        message.setText(" "+arrayOfData.get(position).Text);
        return rowView;
    }
    @Override
    public int getCount() {
        return arrayOfData.size();
    }
}
