package com.example.firebasechat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivityPresenter extends Activity {
    DatabaseReference users = FirebaseDatabase.getInstance().getReference("user's rooms");
    RelativeLayout activity_main_presenter;
    Button find;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_presenter);
        activity_main_presenter = (RelativeLayout)findViewById(R.id.activity_main_presenter);

        find = (Button)findViewById(R.id.Next);
        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = (EditText)findViewById(R.id.name);
                String name = input.getText().toString();
                input = (EditText)findViewById(R.id.password);
                String friend = input.getText().toString();
                String room_name = name + friend;
                users.child(room_name).setValue(name);
                //Intent intent = new Intent(MainActivityPresenter.this, MainActivity.class);
                //startActivity(intent);
            }
        });


    }
}
