package com.example.user.chatroom;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    EditText Username ;
    EditText IPAddr ;
    Button Connect ;
    TextView InvalidLabel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InvalidLabel = (TextView) findViewById(R.id.InvalidLabel);
        Username = (EditText) findViewById(R.id.Username);
        IPAddr = (EditText) findViewById(R.id.IP);
        Connect = (Button) findViewById(R.id.Connect);
        Connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = Username.getText().toString();
                final String ipaddr = IPAddr.getText().toString();
                Intent intent = new Intent(MainActivity.this, ChatRoom.class);
                IPAddressValidator ipValidator = new IPAddressValidator();
                if(username.length()!= 0 && ipValidator.validate(ipaddr) && !username.equals("ChatRoom")){
                    intent.putExtra("Username", username);
                    intent.putExtra("IPAddr", ipaddr);
                    startActivity(intent);
                }else{
                    InvalidLabel.setText("Invalid Username or IP");
                }

            }
        });
    }
}
