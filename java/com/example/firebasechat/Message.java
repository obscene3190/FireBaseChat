package com.example.firebasechat;
import java.util.Date;

public class Message {

    private String textMessage;
    private String author;
    private long timeMessage;

    public Message(String textMessage, String author) {
        this.textMessage = textMessage;
        this.author = author;

        timeMessage = new Date().getTime();
    }

    public Message() {
    }

    public String getTextMessage() {
        return textMessage;
    }

    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getTimeMessage() {
        return timeMessage;
    }

    public void setTimeMessage(long timeMessage) {
        this.timeMessage = timeMessage;
    }
}