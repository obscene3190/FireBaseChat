package com.example.firebasechat;
import java.util.Date;

/**
 * \brief Класс сообщений пользователей
 */
public class Message {

    private String textMessage; ///< Текст сообщения
    private String author; ///< Автор сообщения
    private long timeMessage; ///< Время отправки сообщения

    /**
     * Конструктор класса Message
     * \param textMessage Текст сообщения
     * \param author Автор сообщения
     */
    public Message(String textMessage, String author) {
        this.textMessage = textMessage;
        this.author = author;
        timeMessage = new Date().getTime();
    }

    public Message() {
    }

    /**
     * Getter для текста сообщения
     * @return Текст сообщения
     */
    public String getTextMessage() {
        return textMessage;
    }

    /**
     * Setter для текста сообщения
     * \param textMessage Текст сообщения
     */
    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }

    /**
     * Getter для автора
     * \return Автор
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Setter для авора
     * \param author Автор
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Getter для времени сообщения
     * \return Время сообщения
     */
    public long getTimeMessage() {
        return timeMessage;
    }

    /**
     * Setter для времени сообщения
     * \param timeMessage время сообщения
     */
    public void setTimeMessage(long timeMessage) {
        this.timeMessage = timeMessage;
    }
}