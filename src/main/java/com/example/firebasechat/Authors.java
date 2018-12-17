package com.example.firebasechat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.ImageVideoBitmapDecoder;

/**
 * @brief Класс для отображения страницы с информацией о создателях
 */
public class Authors extends AppCompatActivity {
    ImageView author1, author2;
    TextView desc1, desc2;

    /**
     * @brief Создание activity Authors
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authors);

        author1 = (ImageView) findViewById(R.id.Author1);
        author1.setImageResource(R.drawable.jojo_launcher);
        author2 = (ImageView) findViewById(R.id.Author2);
        author2.setImageResource(R.drawable.jojo_launcher);
        desc1 = (TextView) findViewById(R.id.text1);
        desc2 = (TextView) findViewById(R.id.text2);
    }
}
