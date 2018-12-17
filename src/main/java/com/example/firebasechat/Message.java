package com.example.firebasechat;
import java.util.Date;

/**
 * @brief Класс сообщений пользователей
 */
public class Message {

    public String textMessage; ///< Текст сообщения
    public String author; ///< Автор сообщения
    public long timeMessage; ///< Время отправки сообщения

    /**
     * @brief Конструктор класса Message
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
     * @briefv Getter для текста сообщения
     * @return Текст сообщения
     */
    public String getTextMessage() {
        return textMessage;
    }

    /**
     * @brief Setter для текста сообщения
     * @param textMessage Текст сообщения
     */
    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }

    /**
     * @brief Getter для автора
     * @return Автор
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @brief Setter для авора
     * @param author Автор
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * @brief Getter для времени сообщения в формате String
     * @return Время сообщения
     */
    public String getTimeMessageString() {
        return String.valueOf(timeMessage);
    }

    /**
     * @brief Getter для времени сообщения
     * @return Время сообщения
     */
    public long getTimeMessage() { return timeMessage; }

    /**
     * @brief Setter для времени сообщения
     * @param timeMessage время сообщения
     */
    public void setTimeMessage(long timeMessage) {
        this.timeMessage = timeMessage;
    }
}