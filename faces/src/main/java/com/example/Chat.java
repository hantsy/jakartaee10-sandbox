package com.example;

import jakarta.faces.lifecycle.ClientWindowScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


@Named
@ClientWindowScoped
public class Chat implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(Chat.class.getName());
    private List<String> messages;

    private String newMessage;

    public void send() {
        if(this.messages == null) {
            this.messages = new ArrayList<>();
        }

        var hello = newMessage +" at "+ LocalDateTime.now();
        this.messages.add(hello);

        LOGGER.log(Level.INFO, "current message list: {0}", this.messages);
        this.newMessage = null;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public String getNewMessage() {
        return newMessage;
    }

    public void setNewMessage(String newMessage) {
        this.newMessage = newMessage;
    }
}
