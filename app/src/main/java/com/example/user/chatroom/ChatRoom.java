package com.example.user.chatroom;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;

public class ChatRoom extends AppCompatActivity {
    private static final String MY_PREFS_NAME="MiniChatApplication";
    private Socket clientSocket = null;
    public static String Username=null;
    private String IPAddr=null;
    private Button SendButton;
    private EditText MSGField;
    private ListView ChatListView;
    private ArrayList<Message> Messages ;
    private CustomList GlobalAdapter ;
    private boolean NotificationSound = true;
    private Menu MainMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        SendButton =(Button) findViewById(R.id.Send);
        MSGField = (EditText) findViewById(R.id.Message);
        load();
        ChatListView = (ListView) findViewById(R.id.ChatList);
        GlobalAdapter  = new CustomList(ChatRoom.this, Messages);
        ChatListView.setAdapter(GlobalAdapter);
        try{
            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            Username = bundle.getString("Username");
            IPAddr = bundle.getString("IPAddr");
        }catch(Exception e){}

        try{

            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if(networkInfo != null && networkInfo.isConnected()){
                Connect(IPAddr);
                Receive();
            }else{
                Context context = getApplicationContext();
                CharSequence text = "Network status: Offline";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
            SendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String msg = MSGField.getText().toString();
                    if(!msg.equals("")) {
                        MSGField.setText("");
                        SendMessage(msg);
                        SaveMessage(new Message(Username,msg));
                        GlobalAdapter.notifyDataSetChanged();
                        try{
                            Thread.sleep(1);
                            GlobalAdapter.notifyDataSetChanged();
                            ChatListView.refreshDrawableState();
                        }catch (Exception e){}
                    }
                }
            });
        }catch(Exception e){
            Intent intent = new Intent(ChatRoom.this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(IPAddr==null && Username==null){
            this.loadUserInfo(Username,IPAddr);
            load();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.main_menu,menu);
            MainMenu = menu;
            return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.clear:
                DataBase DB = DataBase.getInstance(getApplicationContext());
                DB.clearStoredMessages();
                Messages.clear();
                RefreshList();
                return true;
            case R.id.sound:
                if(NotificationSound){
                    NotificationSound = false;
                    item.setIcon(R.mipmap.mute_icon);
                }else{
                    NotificationSound = true;
                    item.setIcon(R.mipmap.sound_icon);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void RefreshList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GlobalAdapter.notifyDataSetChanged();
                scrollMyListViewToBottom();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        SendMessage("!#%&");
        try{
            clientSocket.close();
        }catch(Exception e){}
    }

    @Override
    protected void onPause() {
        super.onPause();
        try{
            SendMessage("!#%&");
            this.saveUserInfo(Username,IPAddr);
            clientSocket.close();
        }catch (Exception e){}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.ClearPrefrences();
    }

    private void SendMessage(final String msg) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    PrintStream out = new PrintStream(clientSocket.getOutputStream());
                    out.println(msg);
                    out.flush();
                    AddMessageToList(new Message(Username,msg));
                }catch (Exception e){}
            }
        }).start();
    }

    private void AddMessageToList(final Message message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Messages.add(message);
                RefreshList();
            }
        });
    }

    private void NotificationAudio(){
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void Receive() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try{
                        DataInputStream in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
                        if(in.available()>0){
                            String username = in.readLine();
                            String M = in.readLine();
                            Message MSG = new Message(username,M);
                            //add in the list of messages
                            AddMessageToList(MSG);
                            SaveMessage(MSG);
                            if(NotificationSound){
                                NotificationAudio();
                            }
                        }else{
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException e1) {
                            }
                        }
                    }catch(Exception e){
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e1) {
                        }
                    }
                }
            }
        }).start();
    }

    private void Connect(final String ipAddr) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    clientSocket = new Socket(ipAddr,12345);
                    PrintStream out = new PrintStream(clientSocket.getOutputStream());
                    out.println(Username);
                    out.flush();
                }catch(Exception e){
                    AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(getApplicationContext());
                    dlgAlert.setTitle("Chat Room");
                    dlgAlert.setMessage("Error: connection error!");
                    dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(ChatRoom.this, MainActivity.class);
                            startActivity(intent);
                        }
                    });
                }
            }
        }).start();
    }

    private void saveUserInfo(String Username , String IPAddr){
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("Username", Username);
        editor.putString("IPAddr", IPAddr);
        editor.commit();
    }

    private void loadUserInfo(String Username , String IPAddr){
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
            Username= prefs.getString("Username", "");//"No name defined" is the default value.
            IPAddr = prefs.getString("IPAddr", null); //0 is the default value.
    }

    private void ClearPrefrences(){
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.remove("Username"); // will delete key key_name3
        editor.remove("IPAddr"); // will delete key key_name4
    // Save the changes in SharedPreferences
        editor.commit(); // commit changes
    }

    private void load(){
        DataBase DB = DataBase.getInstance(getApplicationContext());
        Messages = DB.getStoredMessages();
    }

    private void SaveMessage(Message message){
        DataBase DB = DataBase.getInstance(getApplicationContext());
        DB.SaveMessage(message);
    }

    private void scrollMyListViewToBottom() {
        ChatListView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                ChatListView.setSelection(GlobalAdapter.getCount() - 1);
            }
        });
    }
}
