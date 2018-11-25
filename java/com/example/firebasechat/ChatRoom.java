package com.example.firebasechat;

import android.content.Intent;
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

    Button button;
    EditText input;

    static User admin, current_user;
    String[] sessionPair = new String[2];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity_main = (RelativeLayout)findViewById(R.id.activity_main);

        current_user = new User(0);
        admin = new User(1);

        // For tests
        sessionPair[0] = "GzDshr7s34y1BrSL";
        sessionPair[1] = "NMHjxlleWpApdxD2";
        admin.setSessionPair(sessionPair);
        // ...

        // передача публичного ключа на сервер:
        pkeys.child("userPubKeyEncStr").setValue(current_user.userPubKeyEncStr);
        // ...
        // админ получает их и заливает зашифрованные сессионные ключи
        pkeys.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userPubKeyEncStr = dataSnapshot.child("userPubKeyEncStr").getValue(String.class);
                // осторожно, работает Админ
                String[] result = admin.DHGenerateAdmin(userPubKeyEncStr);
                Admen.child("adminPubKeyEnc").setValue(result[0]);
                Admen.child("cipherString1").setValue(result[1]);
                Admen.child("cipherString2").setValue(result[2]);
                Admen.child("encodedParams").setValue(result[3]);
                Listener();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // ...


        button = (Button)findViewById(R.id.button2);
        input = (EditText)findViewById(R.id.editText);

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
                // ...

                messages.push()
                        .setValue(new Message(msg,
                                FirebaseAuth.getInstance().getCurrentUser().getEmail()));

                input.setText("");
            }
        });
        messages.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Snackbar.make(activity_main, "Новое сообщение", Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void displayChat() {
        ListView listMessages = (ListView)findViewById(R.id.list_of_messages);
        adapter = new FirebaseListAdapter<Message>(this, Message.class, R.layout.item, messages) {
            @Override
            protected void populateView(View v, Message model, int position) {

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
            }
        };
        listMessages.setAdapter(adapter);
    }

    private void Listener() {
        Admen.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // осторожно, работает Юзер
                String[] result = new String[4];
                result[0] = dataSnapshot.child("adminPubKeyEnc").getValue(String.class);
                result[1] = dataSnapshot.child("cipherString1").getValue(String.class);
                result[2] = dataSnapshot.child("cipherString2").getValue(String.class);
                result[3] = dataSnapshot.child("encodedParams").getValue(String.class);
                // ...
                current_user.EncryptSession(result);
                pkeys.child("session1").setValue(current_user.sessionPair_[0]);
                pkeys.child("session2").setValue(current_user.sessionPair_[1]);
                displayChat();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
