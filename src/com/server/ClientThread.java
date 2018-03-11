package com.server;

import com.google.gson.Gson;
import com.model.GsonPackage;

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
                inputStream = new DataInputStream(clientSocket.getInputStream());
                outputStream = new DataOutputStream(clientSocket.getOutputStream());

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
                    case "EXIT":

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