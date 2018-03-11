package com.server;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ClientThread extends Thread {
    private Socket clientSocket;
    private ArrayList<ChatRoom> chatRooms;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public ClientThread (Socket clientSocket, ArrayList<ChatRoom> chatRooms) {
        this.clientSocket = clientSocket;
        this.chatRooms = chatRooms;

        try {
            this.inputStream = new DataInputStream(clientSocket.getInputStream());
            this.outputStream = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ChatRoom findRoom(String roomName) {
        synchronized (this) {
            for (ChatRoom currentChatRoom : chatRooms) {
                if (currentChatRoom.getRoomName().equals(roomName)) {
                    return currentChatRoom;
                }
            }

            return null;
        }
    }

    public void run () {
        while (true) {
            try {
                Gson gson = new Gson();

                //Recibimos el mensaje del usuario en formato gson
                String messageData = inputStream.readUTF();

                //Convertimos el mensaje gson a un objeto de la clase MessagePackage para acceder a los elementos del mensaje
                GsonPackage messagePackage = gson.fromJson(messageData, GsonPackage.class);

                //Leemos el mensaje que escribió el usuario
                String message = messagePackage.getMessage();
                String roomName = messagePackage.getRoomName();

                ChatRoom chatRoom = findRoom(roomName);

                switch (message) {
                    //El usuario desea unirse a una sala
                    case "JOIN":
                        //Si la sala no existe, se crea una nueva
                        if (chatRoom == null) {
                            chatRoom = new ChatRoom(roomName);
                        }
                        chatRoom.addClient(this);
                        break;
                    default:
                        //Se recuperan los clientes de la habitación a la cuál se desea mandar el mensaje
                        ArrayList<ClientThread> roomClients = chatRoom.getClients();

                        synchronized (this) {
                            for (ClientThread client : roomClients) {
                                client.outputStream.writeUTF(messageData);
                            }
                        }

                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

class GsonPackage {
    private String userName;
    private String roomName;
    private String message;

    public GsonPackage(String userName, String roomName, String message) {
        this.userName = userName;
        this.roomName = roomName;
        this.message = message;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}