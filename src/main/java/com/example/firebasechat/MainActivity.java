package com.example.firebasechat;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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

import java.security.spec.KeySpec;
import java.util.concurrent.TimeUnit;
import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/** @brief Activity регистрации и авторизации пользователей
 *
 *  Данное Activity предназначено для регистрации или авторизации пользователя, также содержит кнопку перехода на страницу информации об авторах
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth; ///<
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText ETemail; ///< Поле ввода email
    private EditText ETpassword; ///< Поле ввода пароля

    ProgressBar progress;

    String email;
    String password;

    String[] sessionPair = new String[2]; ///< Сессионная пара

    private Button Login; ///< Кнопка авторизации
    private Button Registration;
    private Button Authors;///< Кнопка авторов
    TextView instructions; ///< Текст-инструкция для пользователя, который проходит регистрацию

    static public  User current_user; ///< Пользователь

    public DatabaseReference pkeys = FirebaseDatabase.getInstance().getReference("Public_Key"); ///< Базад данных публичных ключей

    SharedPreferences sPref; ///<  Локальная база данных для хранения ключей пользователя

    /**
     * @brief Создание activity MainActivity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.email_password);

        mAuth = FirebaseAuth.getInstance();

        progress = (ProgressBar) findViewById(R.id.progress);
        ETemail = (EditText) findViewById(R.id.et_email);
        ETpassword = (EditText) findViewById(R.id.et_password);
        instructions = (TextView) findViewById(R.id.instruction);

        findViewById(R.id.btn_sign_in).setOnClickListener(this);
        findViewById(R.id.btn_registration).setOnClickListener(this);
        findViewById(R.id.authors).setOnClickListener(this);

        FirebaseUser user = mAuth.getCurrentUser();

        if(user != null) {
            Toast.makeText(MainActivity.this, "Вы вошли как " + mAuth.getInstance().getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
            ETemail.setText(mAuth.getInstance().getCurrentUser().getEmail());
            //Intent intent = new Intent(MainActivity.this, ChatRoom.class);
            //startActivity(intent);
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
        else if (view.getId() == R.id.authors) {
            Intent intent = new Intent(MainActivity.this, Authors.class);
            startActivity(intent);
        }
    }

    /**
     * @brief Функция, реализующая авторизацию пользователя по паролю и почте
     * @param email Данный, который пользователь ввел в поле почты
     * @param password Данный, который пользователь ввел в поле пароля
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
     * @brief Функция, реализующая регистрацию пользователя по паролю и почте
     *
     * При успешной регистрации для пользователя генерируются ключи, которые после генерации отправляются на сервер
     * @param email Данный, который пользователь ввел в поле почты
     * @param password Данный, который пользователь ввел в поле пароля
     */
    public void registration (String email , String password){
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    keys_generation();
                }
                else
                    Toast.makeText(MainActivity.this, "Регистрация провалена", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * @brief Переотправка ключей для пользователей, у которых нет их на данном устройстве
     */
    public void keys_generation () {
        current_user = new User();
        // отправить в другой поток метод инит, а в основу отобразить колесико зарузки
        Key_Thread key = new Key_Thread();
        key.execute(current_user);
        //...

        //current_user.init(0);
        // сохранение пары ключей(приватного и публичного) в память устройства

        //Toast.makeText(MainActivity.this, "Ключ отправлен, ожидайте ответа Администратора", Toast.LENGTH_SHORT).show();
    }

    /**
     * @brief Авторизация для Администратора
     * @param password
     */
    public void admin_auth(String password) {
        sPref = getSharedPreferences(mAuth.getInstance().getCurrentUser().getUid(), MODE_PRIVATE);

        String key1 = "gKiQ6Su+j6aUKPPHWOkBrQ==";
        String key2 = "7zv5LNrcOvVQxgFgdOPDuw==";
        String Iv = "9j2QR73Hsmfto5w+mfTFJA==";

        // с помощью пароля идет расшифровка ключей
        try {
            String[] sessionPair = {key1, key2};
            byte[] iv = Base64.decode(Iv, Base64.DEFAULT);
            byte[] salt = Base64.decode("0Gh6Tbnxf4oWer==", Base64.DEFAULT);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 10000, 128);
            SecretKey tmp = factory.generateSecret(spec);

            SecretKeySpec skey = new SecretKeySpec(tmp.getEncoded(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skey, ivspec);

            byte[] encrypted1 = cipher.doFinal(Base64.decode(sessionPair[0], Base64.DEFAULT));// SesP
            byte[] encrypted2 = cipher.doFinal(Base64.decode(sessionPair[1], Base64.DEFAULT));//SesP
            SharedPreferences.Editor ed = sPref.edit();

            String Key1 = Base64.encodeToString(encrypted1, Base64.DEFAULT).substring(0, 16);
            String Key2 = Base64.encodeToString(encrypted2, Base64.DEFAULT).substring(0, 16);
            ed.putString("Key1", Key1);
            ed.putString("Key2", Key2);
            ed.commit();
            Intent intent = new Intent(MainActivity.this, ChatRoom.class);
            startActivity(intent);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(MainActivity.this, "Ошибка входа Администратора", Toast.LENGTH_SHORT).show();
            return;
        }

    }

    /**
     * @brief Авторизация для пользователей
     */
    public void user_auth() {
        sPref = getSharedPreferences(mAuth.getInstance().getCurrentUser().getUid(), MODE_PRIVATE);

        String publicKey = sPref.getString("Public_Key", "");
        String privateKey = sPref.getString("Private_Key", "");

        if( publicKey.equals("") || privateKey.equals("") ) {
            Toast.makeText(MainActivity.this, "Aвторизация успешна", Toast.LENGTH_SHORT).show();
            instructions.setVisibility(View.VISIBLE);
            sessionPair[0] = sPref.getString("Key1", "");
            sessionPair[1] = sPref.getString("Key2", "");
            if (sessionPair[0].equals("") || sessionPair[1].equals("")) {
                instructions.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "На данном устройстве нет ключей, создание...", Toast.LENGTH_SHORT).show();
                keys_generation();
            }
            else {
                Intent intent = new Intent(MainActivity.this, ChatRoom.class);
                startActivity(intent);
            }
        }
        else {
            Intent intent = new Intent(MainActivity.this, ChatRoom.class);
            startActivity(intent);
        }
    }

    /**
     * @brief Поток для обработки генерации ключей
     */
    public class Key_Thread extends AsyncTask<User, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.setVisibility(View.VISIBLE);
            findViewById(R.id.btn_sign_in).setEnabled(false);
            findViewById(R.id.btn_registration).setEnabled(false);
            findViewById(R.id.authors).setEnabled(false);
        }

        @Override
        protected Void doInBackground(User ... current_user) {
            try {
                current_user[0].init(0);
                sPref = getSharedPreferences(mAuth.getInstance().getCurrentUser().getUid(), MODE_PRIVATE);
                SharedPreferences.Editor ed = sPref.edit();

                ed.putString("Public_Key", current_user[0].getUserPubKeyEncStr());
                ed.putString("Private_Key", current_user[0].getUserPrivateKeyEncStr());
                ed.commit();

                pkeys.child(mAuth.getInstance().getCurrentUser().getUid()).setValue(current_user[0].getUserPubKeyEncStr());
            }
            catch (Exception ex ) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            progress.setVisibility(View.INVISIBLE);
            findViewById(R.id.btn_sign_in).setEnabled(true);
            findViewById(R.id.btn_registration).setEnabled(true);
            findViewById(R.id.authors).setEnabled(true);
            Intent intent = new Intent(MainActivity.this, ChatRoom.class);
            startActivity(intent);
        }
    }
}