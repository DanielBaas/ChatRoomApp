package com.client;

import com.google.gson.Gson;
import com.model.MessagePackage;
import com.view.ChatRoomView;
import com.view.RoomListView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class RoomListController {

    private RoomListView view;
    private Socket clientSocket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private String userName;

    public RoomListController (Socket clientSocket, DataInputStream inputStream, DataOutputStream outputStream, String userName) {
        this.clientSocket = clientSocket;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.userName = userName;

        view = new RoomListView();
        view.getButtonCreate().addActionListener(new CreateListener());
        view.getButtonJoin().addActionListener(new JoinListener());
        view.addWindowListener(new CloseListener());
        view.setLocationRelativeTo(null);
        view.setVisible(true);
        view.setName("Bienvenido " + userName + "!!!");

        fillAvailableRooms();
    }

    private void fillAvailableRooms () {
        try {
            Gson gson = new Gson();
            MessagePackage messagePackage = new MessagePackage(userName, "LIST_ROOMS", "LIST_ROOMS");
            String messageIn = "";

            outputStream.writeUTF(gson.toJson(messagePackage));
            messageIn = inputStream.readUTF();

            if (messageIn.contains("RL=")) {
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
            String roomName = JOptionPane.showInputDialog(null, "Cual es el nombre de su producto?");

            if (roomName != null) {
                roomName = roomName.trim();

                if (!roomName.isEmpty()) {
                    try {
                        MessagePackage messagePackage = new MessagePackage(userName, roomName, "CREATE");

                        //Enviamos petición de creación de sala
                        outputStream.writeUTF(gson.toJson(messagePackage));

                        //Esperamos la respuesta del servidor para saber si la sala está disponible o ya fue creada
                        String messageIn = inputStream.readUTF();
                        messagePackage = gson.fromJson(messageIn, MessagePackage.class);
                        String roomAvailable = messagePackage.getMessage();

                        if (roomAvailable.equals("ROOM_AVAILABLE")) {
                            ChatRoomView gui = new ChatRoomView();
                            ChatClient client = new ChatClient(userName, roomName, gui);

                            fillAvailableRooms();
                        } else if (roomAvailable.equals("ROOM_NOT_AVAILABLE")) {
                            JOptionPane.showMessageDialog(null, "El nombre del foro no se encuentra disponible!!!");
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

    class JoinListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Gson gson = new Gson ();
            String roomName = view.getListRooms().getSelectedValue();

            if (roomName != null) {
                ChatRoomView gui = new ChatRoomView();
                ChatClient client = new ChatClient(userName, roomName, gui);
            }
        }
    }

    /* Recibe los eventos de cierre de la ventana del usuario */
    class CloseListener extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent closeEvent) {
            try {
                MessagePackage messagePackage = null;
                Gson gson = new Gson();

                //Empaquetamos un mensaje para hacer petición de salir del servidor
                messagePackage = new MessagePackage(userName, "LIST_ROOMS", "EXIT_APP");

                //Enviamos la petición
                outputStream.writeUTF(gson.toJson(messagePackage));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
