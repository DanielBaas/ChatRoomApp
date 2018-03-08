package com.main;

import com.controller.ChatRoomController;
import com.model.ChatRoomModel;
import com.view.ChatRoomView;

import javax.swing.*;
import java.util.Scanner;

public class ChatRoomTest {
    public static void main(String[] args) {
        String roomName = "Sala 1";
        System.out.println("user name?");
        String userName = new Scanner(System.in).nextLine();
        ChatRoomModel chatRoomModel = new ChatRoomModel(roomName, userName, "localhost", "5000");

        ChatRoomView chatRoomView = new ChatRoomView();
        ChatRoomController chatRoomController = new ChatRoomController(chatRoomView, chatRoomModel);

        chatRoomController.startView();
    }
}
