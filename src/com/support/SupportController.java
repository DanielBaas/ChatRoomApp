package com.support;

import com.client.ChatClient;
import com.google.gson.Gson;
import com.model.MessagePackage;

import com.view.ChatRoomView;
import com.view.SupportView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class SupportController extends Thread {

    private final String HOST = "localhost";
    private final int PORT = 5000;
    private final String USERNAME = "SOPORTE";
    private final String ROOMNAME = "SOPORTE";

    private SupportView view;
    private Socket clientSocket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private boolean isConnected;
    private RoomInfoSupport currentRoom;
    private ArrayList<RoomInfoSupport> currentRooms;


    public SupportController(SupportView view) {
        clientSocket = null;
        inputStream = null;
        outputStream = null;
        isConnected = false;

        currentRooms = new ArrayList<>();

        this.view = view;
        view.getButtonSend().addActionListener(new SendListener());
        view.getButtonChangeRoom().addActionListener(new ChangeRoomListener());
        view.addWindowListener(new CloseListener());
        view.setLocationRelativeTo(null);
        view.setVisible(true);
        view.setName("Bienvenido " + USERNAME + "!!!");

        run();
    }

    public void run() {
        try {
            Gson gson = new Gson();
            clientSocket = new Socket(HOST, PORT);
            inputStream = new DataInputStream(clientSocket.getInputStream());
            outputStream = new DataOutputStream(clientSocket.getOutputStream());

            isConnected = true;

            MessagePackage messagePackage = new MessagePackage(USERNAME, ROOMNAME, "SUPPORT_ONLINE");

            /*Petición para iniciar sesión como soporte*/
            outputStream.writeUTF(gson.toJson(messagePackage));

            messagePackage = new MessagePackage(USERNAME, ROOMNAME, "CREATE");

            /*Enviamos petición de creación de sala*/
            outputStream.writeUTF(gson.toJson(messagePackage));

            /*Esperamos la respuesta del servidor para saber si la sala está disponible o ya fue creada*/
            String messageIn = inputStream.readUTF();

            messagePackage = gson.fromJson(messageIn, MessagePackage.class);
            String roomAvailable = messagePackage.getMessage();

            if (roomAvailable.equals("ROOM_AVAILABLE")) {
                currentRoom = new RoomInfoSupport(ROOMNAME);
                currentRooms.add(currentRoom);

                view.setTitle(USERNAME + "@" + currentRoom.getRoomName());

                messagePackage = new MessagePackage(USERNAME, ROOMNAME, "JOIN");
                outputStream.writeUTF(gson.toJson(messagePackage));

                messagePackage.setMessage("LIST_ROOMS");
                outputStream.writeUTF(gson.toJson(messagePackage));

                messagePackage.setMessage("ECHO_JOIN");
                outputStream.writeUTF(gson.toJson(messagePackage));

                Receiver receiver = new Receiver(inputStream);
                receiver.start();

                messagePackage.setMessage("LIST_CLIENTS");
                outputStream.writeUTF(gson.toJson(messagePackage));
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Procesa las entradas del usuario, las empaqueta y las envía al servidor.
     */
    private void sendMessage() {
        try {
            /*Leemos lo que el String que el usuario introdujo en su ventana*/
            String messageOut = view.getTextInputArea().getText().trim();

            if (messageOut != null) {
                if (!messageOut.isEmpty()) {
                    Gson gson = new Gson();

                    /*Empaquetamos el mensaje a enviar*/
                    MessagePackage messagePackage = new MessagePackage(USERNAME, currentRoom.getRoomName(), messageOut);

                    /*Limpiamos el area de texto del usuario después de que presiona enviar*/
                    view.getTextInputArea().setText("");

                    /*Enviamos el mensaje al servidor*/
                    outputStream.writeUTF(gson.toJson(messagePackage));

                    currentRoom.addMessage("<<" + USERNAME + ">> " + messageOut);

                    currentRoom.printMessages();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private RoomInfoSupport findRoom (String roomName) {
        for (RoomInfoSupport room : currentRooms) {
            if (room.getRoomName().equals(roomName)) {
                return room;
            }
        }

        return null;
    }

    private class SendListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent event) {
            if (event.getSource() == view.getButtonSend()) {
                sendMessage();
            }
        }
    }

    /**
     * Thread que procesa los mensajes recibidos del servidor.
     */
    private class Receiver extends Thread {
        private DataInputStream inputStream;

        public Receiver (DataInputStream inputStream) {
            this.inputStream = inputStream;
        }

        public void run() {
            try {
                while (isConnected) {
                    Gson gson = new Gson();
                    String messageIn = "";

                    /*Leemos el String json que contiene la información del mensaje*/
                    messageIn = inputStream.readUTF();

                    System.out.println(messageIn);

                    /*Si el mensaje recibido es una Client List, se actualiza la ventan del usuario con los clientes en la sala*/
                    if (messageIn.contains("CL=")) {
                        /*Se extrae la lista de clientes del mensaje recibido*/
                        messageIn = messageIn.substring(3);
                        String[] clientsInRoom = gson.fromJson(messageIn, String[].class);

                        /*Se actualiza la ventana del usuario*/
                        view.getListUsers().setListData(clientsInRoom);
                    } else if (messageIn.contains("RL=")) {
                        messageIn = messageIn.substring(3);
                        String[] roomList = gson.fromJson(messageIn, String[].class);

                        view.getListRooms().setListData(roomList);
                    } else if (messageIn.contains("NR=")) {
                        messageIn = messageIn.substring(3);
                        messageIn = messageIn.replace("\"", "");
                        System.out.println("nr=" + messageIn);
                        currentRooms.add(new RoomInfoSupport(messageIn));
                    } else {
                        /*Convertimos el json a un objeto MessagePackage para poder acceder a la información del mensaje*/
                        MessagePackage messsagePackage = gson.fromJson(messageIn, MessagePackage.class);

                        /*Preparamos el mensaje que se mostrará en la ventana del usuario*/
                        String messageToShow = "<<" + messsagePackage.getUserName() + ">> " + messsagePackage.getMessage();

                        if (currentRoom.getRoomName().equals(messsagePackage.getRoomName())) {
                            /*Mostramos el mensaje en pantalla*/
                            view.getTextOuputArea().append(messageToShow + "\n");
                        }

                        for (int i = 0; i < currentRooms.size(); i++) {
                            if (currentRooms.get(i).getRoomName().equals(messsagePackage.getRoomName())) {
                                currentRooms.get(i).addMessage(messageToShow);
                                break;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ChangeRoomListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                Gson gson = new Gson();
                String roomName = view.getListRooms().getSelectedValue();

                if (roomName != null) {
                    currentRoom = findRoom(roomName);

                    if (currentRoom != null) {
                        view.getTextOuputArea().setText("");
                        view.setTitle(USERNAME + "@" + currentRoom.getRoomName());

                        ArrayList<String> messages = currentRoom.getMessages();

                        for (String message : messages) {
                            view.getTextOuputArea().append(message + "\n");
                        }

                        MessagePackage messagePackage = new MessagePackage(USERNAME, currentRoom.getRoomName(), "LIST_ROOMS");

                        outputStream.writeUTF(gson.toJson(messagePackage));
                    }
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }
    }

    /**
     * Recibe el evento de cierre de la ventana del usuario y manda una petición al servidor para cerrar comunicación por
     * cierre de la aplicación.
     */
     private class CloseListener extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent closeEvent) {
            try {
                Gson gson = new Gson();
                MessagePackage messagePackage = new MessagePackage(USERNAME, "SUPPORT_ROOM", "SUPPORT_EXIT_APP");

                outputStream.writeUTF(gson.toJson(messagePackage));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new SupportController(new SupportView());
    }

}
