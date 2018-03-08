package com.controller;

import com.google.gson.Gson;
import com.model.ChatRoomModel;
import com.view.ChatRoomView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatRoomController implements ActionListener {

    private ChatRoomView chatRoomView;
    private ChatRoomModel room;

    public ChatRoomController(ChatRoomView chatRoomView, ChatRoomModel room) {
        this.chatRoomView = chatRoomView;
        this.room = room;

        this.chatRoomView.getButtonSend().addActionListener(this);
    }

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
        Gson messageToSend = new Gson();
        String inputMessage = chatRoomView.getTextInputArea().getText();

        if ( (!inputMessage.equals("")) && inputMessage != null ) {
            room.setMessage(inputMessage);

            messageToSend.toJson(room);
            chatRoomView.getTextInputArea().setText("");

            chatRoomView.getTextOuputArea().append(inputMessage + "\n");
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
