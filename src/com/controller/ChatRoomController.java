package com.controller;

import com.client.Client;
import com.google.gson.Gson;
import com.model.ChatRoomModel;
import com.view.ChatRoomView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatRoomController implements ActionListener {

    private ChatRoomView chatRoomView;

    public ChatRoomController(ChatRoomView chatRoomView) {
        this.chatRoomView = chatRoomView;
    }

    public void startView(String roomName) {
        chatRoomView.setTitle(roomName);
        chatRoomView.pack();
        chatRoomView.setLocationRelativeTo(null);
        chatRoomView.setVisible(true);
    }

    private void sendMessage() {
        String inputMessage = chatRoomView.getTextInputArea().getText();

        if ( (!inputMessage.equals("")) && inputMessage != null ) {
            chatRoomView.getTextInputArea().setText("");
        }
    }

    public void showMessage(String message) {
        chatRoomView.getTextInputArea().setText("");
        chatRoomView.getTextOuputArea().append(message + "\n");
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == chatRoomView.getButtonSend()) {
            sendMessage();
        }
    }

}
