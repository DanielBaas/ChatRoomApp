package com.client;

import com.google.gson.Gson;
import com.model.MessagePackage;
import com.view.RoomListView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.MessageDigest;

public class RoomListController {

    private RoomListView view;
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private String userName;

    public RoomListController (Socket socket, DataInputStream inputStream, DataOutputStream outputStream, String userName) {
        this.socket = socket;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.userName = userName;

        view = new RoomListView();
        view.setName("Bienvenido " + userName + "!!!");
        view.getButtonCreate().addActionListener(new CreateListener());
        view.getButtonJoin().addActionListener(new JoinListener());
        view.setLocationRelativeTo(null);
        view.setVisible(true);

        fillAvailableRooms();
    }

    public void run () {
        while (true) {

        }
    }

    private void fillAvailableRooms () {
        try {
            Gson gson = new Gson();
            MessagePackage messagePackage = new MessagePackage(userName, "LIST_ROOMS", "LIST_ROOMS");
            String messageIn = "";

            outputStream.writeUTF(gson.toJson(messagePackage));
            messageIn = inputStream.readUTF();

            if (messageIn.contains("UL=")) {
                messageIn = messageIn.substring(3);
                String[] userList = gson.fromJson(messageIn, String[].class);
                view.getListRooms().setListData(userList);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class CreateListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Gson gson = new Gson ();
            String roomName = JOptionPane.showInputDialog(null, "Cual es el nombre de su producto?").trim();
            MessagePackage messagePackage = new MessagePackage(userName, roomName, "JOIN");

            if (!roomName.isEmpty()) {
                try {
                    //Enviamos petición de creación de sala
                    outputStream.writeUTF(gson.toJson(messagePackage));

                    //Esperamos la respuesta del servidor para saber si la sala está disponible o ya fue creada
                    String messageIn = inputStream.readUTF();
                    messagePackage = gson.fromJson(messageIn, MessagePackage.class);
                    String roomAvailable = messagePackage.getMessage();

                    if (roomAvailable.equals("ROOM_AVAILABLE")) {
                        new ChatClient(userName, roomName);
                    } else if (roomAvailable.equals("ROOM_NOT_AVAILABLE")) {
                        JOptionPane.showMessageDialog(null, "El nombre del foro no se encuentra disponible!!!");
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    class JoinListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Gson gson = new Gson ();
            String roomName = view.getListRooms().getSelectedValue().trim();

            if (!roomName.isEmpty()) {
                new ChatClient(userName, roomName);
            }
        }
    }

}
