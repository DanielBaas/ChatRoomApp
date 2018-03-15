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

    /**
     * Realiza una petición al servidor para obtener una lista de las salas disponibles, para luego msotrarlas en la venta
     * del usuario.
     */
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

    /**
     * Recibe el evento del presionado del botón "Crear nueva sala". Envía petición al servidor para validar que dicha sala
     * no existe y luego crea una nueva instancia del objeto ChatClient, la cual ya realiza el envío y recibo de mensajes
     */
    class CreateListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Gson gson = new Gson ();
            String roomName = JOptionPane.showInputDialog(null, "Cual es el nombre de su producto?");

            if (roomName != null) {
                roomName = roomName.trim();

                if (!roomName.isEmpty()) {
                    try {
                        roomName = roomName.toUpperCase();
                        MessagePackage messagePackage = new MessagePackage(userName, roomName, "CREATE");

                        //Enviamos petición de creación de sala
                        outputStream.writeUTF(gson.toJson(messagePackage));

                        //Esperamos la respuesta del servidor para saber si la sala está disponible o ya fue creada
                        String messageIn = inputStream.readUTF();
                        messagePackage = gson.fromJson(messageIn, MessagePackage.class);
                        String roomAvailable = messagePackage.getMessage();

                        if (roomAvailable.equals("ROOM_AVAILABLE")) {
                            /*Nueva instancia de la ventana de chat*/
                            ChatRoomView gui = new ChatRoomView();

                            /*Nueva instancia del controlador de la vista del usuario. Realiza todo el procesamiento de
                            * envío y recibo de mensajes*/
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

    /**
     * Recibe el evento del presionado del botón "Entrar a una sala". Envía una petición al servidor para realizar el
     * registro del usuario a la sala selecionada.
     */
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

    /**
     * Recibe el evento de cierre de la ventana del usuario y manda una petición al servidor para cerrar comunicación por
     * cierre de la aplicación.
     */
    class CloseListener extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent closeEvent) {
            try {
                MessagePackage messagePackage = null;
                Gson gson = new Gson();

                messagePackage = new MessagePackage(userName, "LIST_ROOMS", "EXIT_APP");

                outputStream.writeUTF(gson.toJson(messagePackage));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
