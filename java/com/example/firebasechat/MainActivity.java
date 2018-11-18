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

import java.security.SecureRandom;
import java.util.ArrayList;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;

import org.apache.commons.codec.binary.Base64;

public class MainActivity extends AppCompatActivity  {
    ArrayList<Message> messages_list = new ArrayList<>();
    ArrayList<User> users_list = new ArrayList<>();
    ArrayList<String> keys = new ArrayList<>();
    private static int SIGN_IN_REQUEST_CODE = 1;
    private static int MAX_MESSAGE_LENGTH = 150;
    private FirebaseListAdapter<Message> adapter;
    RelativeLayout activity_main;
    DatabaseReference users = FirebaseDatabase.getInstance().getReference("users");

    DatabaseReference Admen = FirebaseDatabase.getInstance().getReference("Admen");

    DatabaseReference messages = FirebaseDatabase.getInstance().getReference("messages");

    private FirebaseAuth mAuth;

    Button button;
    User admen, current_user;

    private String[] sessionPair_ = new String[2];



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity_main = (RelativeLayout)findViewById(R.id.activity_main);
        // AES Keys
        sessionPair_[0] = "GzDshr7s34y1BrSL";
        sessionPair_[1] = "NMHjxlleWpApdxD2";
        admen = new User();
        admen.setSessionPair_(sessionPair_);
        current_user = new User();
        //admen  = new User();
        //admen.setSessionPair_(sessionPair_);
        //current_user = new User();
        String s;
        s = admen.encrypt("Hello");
        //String s1 = current_user.decrypt(s);
        //users.push().setValue(s);
        //users.push().setValue(s1);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), SIGN_IN_REQUEST_CODE);
        } else {
            displayChat();
        }
        /* User user = new User(email);
        герерация юзером 2х ключей
        обращние к самому первому чуваку из users и взятие двух строк
        расщифрование обеих строк одним из ключей
        используя строки, можно шифровать и расшифровывать сообщения
        */

        // ...

        // ...
        button = (Button)findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = (EditText)findViewById(R.id.editText);
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
                textMessage.setText(msg);
                author.setText(model.getAuthor());
                timeMessage.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model.getTimeMessage()));
            }
        };
        listMessages.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                Snackbar.make(activity_main, "Вход выполнен", Snackbar.LENGTH_SHORT).show();
                String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                FirebaseUser user = mAuth.getInstance().getCurrentUser();
                users.child(user.getUid()).setValue(user.getEmail());
                displayChat();
            } else {
                Snackbar.make(activity_main, "Вход не выполнен", Snackbar.LENGTH_SHORT).show();
                finish();
            }
        }
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
            AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Snackbar.make(activity_main, "Выход выполнен", Snackbar.LENGTH_SHORT).show();
                            finish();
                        }
                    });
        }
        return true;
    }
}

