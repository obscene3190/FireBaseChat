package com.example.firebasechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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

/**
 * \brief Данное Activity представляет собой комнату обмена зашифрованными сообщениями
 */
public class ChatRoom extends AppCompatActivity  {
    private static int MAX_MESSAGE_LENGTH = 300; ///< Максимальная длина сообщения
    private FirebaseListAdapter<Message> adapter; ///< Адаптер для отображения сообщений с сервера
    //private FirebaseRecyclerAdapter<Message> adapter2;

    static User current_user;

    RelativeLayout activity_main;

    DatabaseReference Admen = FirebaseDatabase.getInstance().getReference("Admen"); ///< База данных с обработанными Администратором публичными ключами пользователей
    DatabaseReference messages = FirebaseDatabase.getInstance().getReference("messages"); ///< База данных с сообщениям пользователей
    DatabaseReference pkeys = FirebaseDatabase.getInstance().getReference("Public_Key"); ///< Базад данных публичных ключей

    SharedPreferences sPref; ///<  Локальная база данных для хранения ключей пользователя

    Button button; ///< Кнопка отправки сообщения
    EditText input; ///< Поле ввода сообщения
    private FirebaseAuth mAuth;

    String[] sessionPair = new String[2]; ///< Сессионная пара

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity_main = (RelativeLayout)findViewById(R.id.activity_main);

        button = (Button)findViewById(R.id.Send);
        button.setEnabled(false);
        input = (EditText) findViewById(R.id.editText);
        current_user = new User();

        // Получаем ключи из локальной базы данных
        sPref = getSharedPreferences(mAuth.getInstance().getCurrentUser().getUid(), MODE_PRIVATE);
        sessionPair[0] = sPref.getString("Key1", "");
        sessionPair[1] = sPref.getString("Key2", "");

        if(mAuth.getInstance().getCurrentUser().getEmail().equals("starkiller44@yandex.ru")) {
            current_user.setSessionPair_(sessionPair);
            button.setEnabled(true);
            displayChat();
            Admin_actrivity();
        }
        else {
            if (sessionPair[0].equals("") || sessionPair[1].equals("")) {
                get_keys();
            } else {
                current_user.setSessionPair_(sessionPair);
                button.setEnabled(true);
                displayChat();
            }
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = input.getText().toString();
                if(msg.equals("")) {
                    Toast.makeText(getApplicationContext(), "Введите сообщение", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(msg.length() > MAX_MESSAGE_LENGTH) {
                    Toast.makeText(getApplicationContext(), "Слишком длинное сообщение", Toast.LENGTH_SHORT).show();
                    return;
                }
                msg = current_user.encrypt(msg);
                messages.push()
                        .setValue(new Message(msg,
                                FirebaseAuth.getInstance().getCurrentUser().getEmail()));
                input.setText("");
            }
        });
    }

    /**
     * @brief Функция отображения сообщений
     * Данная функция обращается к базе данных сообщений пользователей и, используя специальный адаптер, отображает их
     */
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

                // Назначение полей
                author.setText(model.getAuthor());
                textMessage.setText(msg);
                timeMessage.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model.getTimeMessage()));

                // Прокрутка сообщений
                listMessages.post(new Runnable() {
                    @Override
                    public void run() {
                        listMessages.setSelection(position);
                    }
                });
            }
        };
        listMessages.setAdapter(adapter);

/*
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.List_of_messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseRecyclerAdapter<Message, ViewHolder> adapter = new FirebaseRecyclerAdapter<Message, ViewHolder>(
                Message.class,
                R.layout.item,
                ViewHolder.class,
                messages
        ) {
            @Override
            protected void populateViewHolder(ViewHolder viewHolder, Message model, int position) {
                String msg = model.getTextMessage();
                // Decrypt...
                msg = current_user.decrypt(msg);

                viewHolder.author.setText(model.getAuthor());
                viewHolder.textMessage.setText(msg);
                viewHolder.timeMessage.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model.getTimeMessage()));
                Snackbar.make(activity_main, "Выход выполнен", Snackbar.LENGTH_SHORT).show();
            }
        };

        recyclerView.setAdapter(adapter);
*/
    }

    /**
     * @brief Данная функция предназначена для получения ключей
     */
    public void get_keys() {
        sPref = getSharedPreferences(mAuth.getInstance().getCurrentUser().getUid(), MODE_PRIVATE);

        current_user.setUserPubKeyEncStr(sPref.getString("Public_Key", ""));
        current_user.setUserPrivateKeyEncStr(sPref.getString("Private_Key", ""));

        if(current_user.getUserPubKeyEncStr() == "" || current_user.getUserPrivateKeyEncStr() == "") {
            Toast.makeText(ChatRoom.this, "Ключи не сгенерированы. Поробуйте повторить попытку.", Toast.LENGTH_SHORT).show();
            return;
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
                if ((result[0] != null) && (result[1] != null)
                        && (result[2] != null) && (result[3] != null)) {
                    // Юзер обрабатывает данные Администратора
                    current_user.EncryptSession(result);

                    // Записываем ключи на устройство
                    SharedPreferences.Editor ed = sPref.edit();

                    ed.putString("Key1", current_user.sessionPair_[0]);
                    ed.putString("Key2", current_user.sessionPair_[1]);
                    ed.commit();

                    Admen.child(mAuth.getInstance().getCurrentUser().getUid()).removeValue();
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

    /**
     * @brief Данная функция предназначена для обработки публичных ключей администатором
     */
    private void Admin_actrivity() {
        pkeys.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String userPubKeyEncStr = dataSnapshot.getValue(String.class);
                String uid = dataSnapshot.getKey();
                if (userPubKeyEncStr != null) {
                    // осторожно, работает Админ
                    String[] result = current_user.DHGenerateAdmin(userPubKeyEncStr);
                    Admen.child(uid).child("adminPubKeyEnc").setValue(result[0]);
                    Admen.child(uid).child("cipherString1").setValue(result[1]);
                    Admen.child(uid).child("cipherString2").setValue(result[2]);
                    Admen.child(uid).child("encodedParams").setValue(result[3]);
                    pkeys.child(uid).removeValue();
                }
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
