package com.example.firebasechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;



public class ChatRoom extends AppCompatActivity  {
    ArrayList<Message> messages_list = new ArrayList<>();
    ArrayList<User> users_list = new ArrayList<>();
    ArrayList<String> keys = new ArrayList<>();
    private static int SIGN_IN_REQUEST_CODE = 1;
    private static int MAX_MESSAGE_LENGTH = 150;
    private FirebaseListAdapter<Message> adapter;
    RelativeLayout activity_main;

    DatabaseReference Admen = FirebaseDatabase.getInstance().getReference("Admen");
    DatabaseReference messages = FirebaseDatabase.getInstance().getReference("messages");
    DatabaseReference pkeys = FirebaseDatabase.getInstance().getReference("Public_Key");
    DatabaseReference users = FirebaseDatabase.getInstance().getReference("Users");

    SharedPreferences sPref;

    Button button;
    EditText input;
    private FirebaseAuth mAuth;

    static User admin, current_user;
    String[] sessionPair = new String[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity_main = (RelativeLayout)findViewById(R.id.activity_main);

        //...

        // ЧАСТЬ ЮЗЕРА: просто берет ключи из своей ячейки
        button = (Button)findViewById(R.id.button2);
        input = (EditText) findViewById(R.id.editText);
        current_user = new User();
        users.child(mAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                sessionPair[0] = dataSnapshot.child("session1").getValue(String.class);
                sessionPair[1] = dataSnapshot.child("session2").getValue(String.class);
                if(sessionPair[0] != null && sessionPair[1] != null) {
                    current_user.setSessionPair_(sessionPair);
                    button.setEnabled(true);
                    displayChat();
                }
                else {
                    // берет ключи из файла на телефоне
                    // ...
                    sPref = getSharedPreferences(mAuth.getInstance().getCurrentUser().getUid(), MODE_PRIVATE);
                    current_user.userPubKeyEncStr = sPref.getString("Public_Key", "");
                    current_user.userPrivateKeyEncStr = sPref.getString("Private_Key", "");
                    if(current_user.userPubKeyEncStr == "" || current_user.userPrivateKeyEncStr == "") {
                        Toast.makeText(ChatRoom.this, "Ключи чета пустые(один из или два)", Toast.LENGTH_SHORT).show();
                    }

                    Toast.makeText(ChatRoom.this, "Ожидайте ответа Администратора", Toast.LENGTH_SHORT).show();
                    Admen.child(mAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String[] result = new String[4];
                            result[0] = dataSnapshot.child("adminPubKeyEnc").getValue(String.class);
                            result[1] = dataSnapshot.child("cipherString1").getValue(String.class);
                            result[2] = dataSnapshot.child("cipherString2").getValue(String.class);
                            result[3] = dataSnapshot.child("encodedParams").getValue(String.class);
                            if ((result[0] != null) && (result[1] != null) && (result[2] != null) && (result[3] != null)) {
                                current_user.EncryptSession(result);
                                users.child(mAuth.getInstance().getCurrentUser().getUid()).child("session1").setValue(current_user.sessionPair_[0]);
                                users.child(mAuth.getInstance().getCurrentUser().getUid()).child("session2").setValue(current_user.sessionPair_[1]);
                                Admen.child(mAuth.getInstance().getCurrentUser().getUid()).removeValue();
                                //pkeys.child(mAuth.getInstance().getCurrentUser().getUid()).removeValue();
                                Toast.makeText(ChatRoom.this, "Ключ получен", Toast.LENGTH_SHORT).show();
                                button.setEnabled(true);
                                displayChat();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = input.getText().toString();
                if(msg.equals("")) {
                    Toast.makeText(getApplicationContext(), "Input message", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(msg.length() > MAX_MESSAGE_LENGTH) {
                    Toast.makeText(getApplicationContext(), "Too long message", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Encrypt...
                msg = current_user.encrypt(msg);

                messages.push()
                        .setValue(new Message(msg,
                                FirebaseAuth.getInstance().getCurrentUser().getEmail()));

                input.setText("");
            }
        });
    }

    private void displayChat() {
        final ListView listMessages = (ListView)findViewById(R.id.list_of_messages);
        adapter = new FirebaseListAdapter<Message>(this, Message.class, R.layout.item, messages) {
            @Override
            protected void populateView(View v, Message model, final int position) {
                TextView textMessage, author, timeMessage;
                textMessage = (TextView)v.findViewById(R.id.tvMessage);
                author = (TextView)v.findViewById(R.id.tvUser);
                timeMessage = (TextView)v.findViewById(R.id.tvDate);
                String msg = model.getTextMessage();
                // Decrypt...
                msg = current_user.decrypt(msg);
                // ...
                author.setText(model.getAuthor());
                textMessage.setText(msg);
                timeMessage.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model.getTimeMessage()));
                listMessages.post(new Runnable() {
                    @Override
                    public void run() {
                        listMessages.setSelection(position);
                    }
                });
            }
        };
        listMessages.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_signout)
        {
            FirebaseAuth Auth = FirebaseAuth.getInstance();
            Auth.signOut();
            Snackbar.make(activity_main, "Выход выполнен", Snackbar.LENGTH_SHORT).show();
            finish();
        }
        return true;
    }
}
