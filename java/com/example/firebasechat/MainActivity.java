package com.example.firebasechat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

/**
 *  \brief Activity регистрации и авторизации
 *  Данное Activity предназначено для регистрации или авторизации пользователя, также содержит кнопку перехода на страницу информации об авторах
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth; ///<
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText ETemail; ///< Поле ввода email
    private EditText ETpassword; ///< Поле ввода пароля

    String email;
    String password;

    String[] sessionPair = new String[2]; ///< Сессионная пара

    private Button Login; ///< Кнопка авторизации
    private Button Registration; ///< Кнопка регистрации
    TextView instructions; ///< Текст-инструкция для пользователя, который проходит регистрацию

    User current_user; ///< Пользователь

    DatabaseReference pkeys = FirebaseDatabase.getInstance().getReference("Public_Key"); ///< Базад данных публичных ключей

    SharedPreferences sPref; ///<  Локальная база данных для хранения ключей пользователя

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.email_password);

        mAuth = FirebaseAuth.getInstance();

        ETemail = (EditText) findViewById(R.id.et_email);
        ETpassword = (EditText) findViewById(R.id.et_password);
        instructions = (TextView) findViewById(R.id.instruction);

        findViewById(R.id.btn_sign_in).setOnClickListener(this);
        findViewById(R.id.btn_registration).setOnClickListener(this);

        FirebaseUser user = mAuth.getCurrentUser();

        if(user != null) {
            Toast.makeText(MainActivity.this, "Вы вошли как " + mAuth.getInstance().getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, ChatRoom.class);
            startActivity(intent);
        }
    }
    @Override
    public void onClick(View view) {
        email = ETemail.getText().toString();
        password = ETpassword.getText().toString();
        if(view.getId() == R.id.btn_sign_in) {
            if( email.equals("") || password.equals("")
                    || email.equals(" ") || password.equals(" ")) {
                Toast.makeText(MainActivity.this, "Данные не введены", Toast.LENGTH_SHORT).show();
            }
            else {
                signin(ETemail.getText().toString(), ETpassword.getText().toString());
            }
        }
        else if (view.getId() == R.id.btn_registration) {
            if( email.equals("") || password.equals("")
                    || email.equals(" ") || password.equals(" ")) {
                Toast.makeText(MainActivity.this, "Данные не введены", Toast.LENGTH_SHORT).show();
            }
            else {
                instructions.setVisibility(View.VISIBLE);
                registration(ETemail.getText().toString(), ETpassword.getText().toString());
            }
        }
    }

    /**
     * \brief Функция, реализующая авторизацию пользователя по паролю и почте
     * \param email Данный, который пользователь ввел в поле почты
     * \param password Данный, который пользователь ввел в поле пароля
     */
    public void signin(String email , final String password_) {
        mAuth.signInWithEmailAndPassword(email,password_).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    if(mAuth.getInstance().getCurrentUser().getEmail().equals("starkiller44@yandex.ru")) {
                        admin_auth(password_);
                    }
                    else {
                        user_auth();
                    }
                } else
                    Toast.makeText(MainActivity.this, "Aвторизация провалена", Toast.LENGTH_SHORT).show();

            }
        });
    }

    /**
     * \brief Функция, реализующая регистрацию пользователя по паролю и почте
     * При успешной регистрации для пользователя генерируются ключи, которые после генерации отправляются на сервер
     * \param email Данный, который пользователь ввел в поле почты
     * \param password Данный, который пользователь ввел в поле пароля
     */
    public void registration (String email , String password){
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    // Процесс регистрации для пользователя: он создает ключи, отправляет их и ждет
                    current_user = new User();
                    current_user.init(0);

                    // сохранение пары ключей(приватного и публичного) в память устройства
                    sPref = getSharedPreferences(mAuth.getInstance().getCurrentUser().getUid(), MODE_PRIVATE);
                    Editor ed = sPref.edit();

                    ed.putString("Public_Key", current_user.getUserPubKeyEncStr());
                    ed.putString("Private_Key", current_user.getUserPrivateKeyEncStr());
                    ed.commit();

                    pkeys.child(mAuth.getInstance().getCurrentUser().getUid()).setValue(current_user.getUserPubKeyEncStr());
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

    /**
     * \brief Переотправка ключей для пользователей, у которых нет их на данном устройстве
     */
    public void reregistration () {
        current_user = new User();
        current_user.init(0);

        // сохранение пары ключей(приватного и публичного) в память устройства
        sPref = getSharedPreferences(mAuth.getInstance().getCurrentUser().getUid(), MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();

        ed.putString("Public_Key", current_user.getUserPubKeyEncStr());
        ed.putString("Private_Key", current_user.getUserPrivateKeyEncStr());
        ed.commit();

        pkeys.child(mAuth.getInstance().getCurrentUser().getUid()).setValue(current_user.getUserPubKeyEncStr());
        Toast.makeText(MainActivity.this, "Ключ отправлен, ожидайте ответа Администратора", Toast.LENGTH_SHORT).show();
    }

    /**
     * \brief Регистрация для администратора
     */
    public void admin_auth(String password) {
        sPref = getSharedPreferences(mAuth.getInstance().getCurrentUser().getUid(), MODE_PRIVATE);

        String iv = "";
        String key1 = "zNaazqRWLcI9xEhB2c1A14z7iC8g5qJ8e621p/Maqvw=";
        String key2 = "kNELl1znJ0EMVY4LU99fXeNCufW2EwEJT4Y9gmAdTD8";
        SharedPreferences.Editor ed = sPref.edit();
        // с помощью пароля идет расшифровка ключей
        ed.putString("Key1", "key1");
        ed.putString("Key2", "key2");
        ed.commit();

        Intent intent = new Intent(MainActivity.this, ChatRoom.class);
        startActivity(intent);
    }

    /**
     * \brief Авторизация для пользователей
     */
    public void user_auth() {
        sPref = getSharedPreferences(mAuth.getInstance().getCurrentUser().getUid(), MODE_PRIVATE);
        sessionPair[0] = sPref.getString("Key1", "");
        sessionPair[1] = sPref.getString("Key2", "");

        Toast.makeText(MainActivity.this, "Aвторизация успешна", Toast.LENGTH_SHORT).show();

        if (sessionPair[0].equals("") || sessionPair[1].equals("")) {
            Toast.makeText(MainActivity.this, "На данном устройстве нет ключей, создание...", Toast.LENGTH_SHORT).show();
            reregistration();
        }

        Intent intent = new Intent(MainActivity.this, ChatRoom.class);
        startActivity(intent);
    }
}
