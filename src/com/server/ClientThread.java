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
    private static ArrayList<ChatRoom> chatRooms;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public ClientThread (Socket clientSocket, ArrayList<ChatRoom> chatRooms) {
        this.clientSocket = clientSocket;
        this.chatRooms = chatRooms;
    }

    private ChatRoom findRoom(String roomName) {
        if (chatRooms.size() == 0){
            System.out.println("No hay salas");
            return null;
        }

        synchronized (this) {
            for (ChatRoom currentChatRoom : chatRooms) {
                if (currentChatRoom.getRoomName().equals(roomName)) {
                    System.out.println("Sala encontrada");
                    return currentChatRoom;
                }
            }

            System.out.println("No se encontro sala");
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
                String messageIn = inputStream.readUTF();

                //Convertimos el mensaje gson a un objeto de la clase MessagePackage para acceder a los elementos del mensaje
                GsonPackage messagePackage = gson.fromJson(messageIn, GsonPackage.class);

                //Leemos el mensaje que escribió el usuario
                String message = messagePackage.getMessage().trim();
                String roomName = messagePackage.getRoomName().trim();
                ChatRoom chatRoom = findRoom(roomName);

                synchronized (this) {
                    switch (message) {
                        //El usuario desea unirse a una sala
                        case "JOIN":
                            //Si la sala no existe, se crea una nueva
                            if (chatRoom == null) {
                                chatRoom = new ChatRoom(roomName);
                                System.out.println("Se creo la sala " + chatRoom.getRoomName());
                            } else {
                                System.out.println("Se conectó a la sala " + chatRoom.getRoomName());
                            }
                            chatRoom.addClient(this);
                            chatRooms.add(chatRoom);
                            break;
                        case "ECHO_JOIN":
                            messagePackage.setMessage("Bienvenido " + messagePackage.getUserName());
                            messagePackage.setUserName("SERVIDOR");

                            ArrayList<ClientThread> echoClients = chatRoom.getClients();
                            for (ClientThread client : echoClients) {
                                client.outputStream.writeUTF(gson.toJson(messagePackage));
                            }
                            break;
                        default:
                            //Se recuperan los clientes de la habitación a la cuál se desea mandar el mensaje
                            ArrayList<ClientThread> roomClients = chatRoom.getClients();
                            for (ClientThread client : roomClients) {
                                client.outputStream.writeUTF(gson.toJson(messagePackage));
                            }
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}