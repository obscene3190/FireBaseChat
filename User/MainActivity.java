package com.example.firebasechat;

import android.content.Intent;
//import android.database.Observable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.example.firebasechat.ChatRoom;
import com.example.firebasechat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText ETemail;
    private EditText ETpassword;

    private Button Login;
    private Button Registration;

    User current_user;

    DatabaseReference Admen = FirebaseDatabase.getInstance().getReference("Admen");
    DatabaseReference messages = FirebaseDatabase.getInstance().getReference("messages");
    DatabaseReference pkeys = FirebaseDatabase.getInstance().getReference("Public_Key");
    DatabaseReference test = FirebaseDatabase.getInstance().getReference("test");
    DatabaseReference users = FirebaseDatabase.getInstance().getReference("Users");
    SharedPreferences sPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.email_password);

        mAuth = FirebaseAuth.getInstance();

        ETemail = (EditText) findViewById(R.id.et_email);
        ETpassword = (EditText) findViewById(R.id.et_password);

        findViewById(R.id.btn_sign_in).setOnClickListener(this);
        findViewById(R.id.btn_registration).setOnClickListener(this);

        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null) {
            Intent intent = new Intent(MainActivity.this, ChatRoom.class);
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btn_sign_in) {
            signin(ETemail.getText().toString(),ETpassword.getText().toString());
        }else if (view.getId() == R.id.btn_registration) {
            registration(ETemail.getText().toString(),ETpassword.getText().toString());
        }
    }

    public void signin(String email , String password) {
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Aвторизация успешна", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this,ChatRoom.class);
                    startActivity(intent);
                }else
                    Toast.makeText(MainActivity.this, "Aвторизация провалена", Toast.LENGTH_SHORT).show();

            }
        });
    }
    public void registration (String email , String password){
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(MainActivity.this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
                    //...
                    // Процесс регистрации для пользователя: он создает ключи, отправляет их и ждет
                    current_user = new User();
                    current_user.init(0);
                    // сохранение пары ключей(приватного и публичного) в память устройства
                    sPref = getSharedPreferences(mAuth.getInstance().getCurrentUser().getUid(), MODE_PRIVATE);
                    Editor ed = sPref.edit();
                    ed.putString("Public_Key", current_user.userPubKeyEncStr);
                    ed.putString("Private_Key", current_user.userPrivateKeyEncStr);
                    ed.commit();
                    //...
                    pkeys.child(mAuth.getInstance().getCurrentUser().getUid()).setValue(current_user.userPubKeyEncStr);
                    Toast.makeText(MainActivity.this, "Ключ отправлен, ожидайте ответа Администратора", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this,ChatRoom.class);
                    startActivity(intent);
                    //...

                }
                else
                    Toast.makeText(MainActivity.this, "Регистрация провалена", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
