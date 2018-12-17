package com.example.firebasechat;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * @brief Класс holder отображения сообщений в RecyclerView
 */
public class ViewHolder extends RecyclerView.ViewHolder {

    TextView textMessage, author, timeMessage;

    public ViewHolder(View ItemView) {
        super(ItemView);
        textMessage = (TextView)ItemView.findViewById(R.id.Message);
        author = (TextView)ItemView.findViewById(R.id.User);
        timeMessage = (TextView)ItemView.findViewById(R.id.Date);
    }
}
