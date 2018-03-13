package com.server;

import com.google.gson.Gson;
import com.model.MessagePackage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ClientThread extends Thread {
    private Socket clientSocket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private String userName;
    private ArrayList<ChatRoom> chatRooms;
    private ArrayList<String> userNameList;

    public ClientThread (Socket clientSocket, ArrayList<ChatRoom> chatRooms, ArrayList<String> userNameList) {
        this.clientSocket = clientSocket;
        this.chatRooms = chatRooms;
        this.userNameList = userNameList;
    }

    public String getUserName () {
        return userName;
    }

    private ChatRoom findRoom(String roomName) {
        if (chatRooms.size() == 0){
            return null;
        }

        synchronized (this) {
            for (ChatRoom currentChatRoom : chatRooms) {
                if (currentChatRoom.getRoomName().equals(roomName)) {
                    return currentChatRoom;
                }
            }

            return null;
        }
    }

    private boolean findUserName (String userNameToFind) {
        if (userNameList.size() == 0) {
            return false;
        }

        for (String currentUserName : userNameList) {
            if (currentUserName.equals(userNameToFind)) {
                return true;
            }
        }

        return false;
    }

    public void run () {
        boolean userConnected = true;

        try {
            while (userConnected) {
                inputStream = new DataInputStream(clientSocket.getInputStream());
                outputStream = new DataOutputStream(clientSocket.getOutputStream());

                Gson gson = new Gson();

                //Recibimos el mensaje del usuario en formato gson
                String messageIn = inputStream.readUTF();

                //Convertimos el mensaje gson a un objeto de la clase MessagePackage para acceder a los elementos del mensaje
                MessagePackage messagePackage = gson.fromJson(messageIn, MessagePackage.class);

                //Leemos el mensaje que escribió el usuario
                String message = messagePackage.getMessage().trim();
                String roomName = messagePackage.getRoomName().trim();
                ChatRoom currentRoom = findRoom(roomName);
                ArrayList<ClientThread> echoClients = null;

                System.out.println(message);

                synchronized (this) {
                    switch (message) {
                        //El usuario desea unirse a una sala
                        case "JOIN":
                            //Si la sala no existe, se crea una nueva
                            if (currentRoom == null) {
                                currentRoom = new ChatRoom(roomName);
                            }

                            userName = messagePackage.getUserName().trim();
                            currentRoom.addClient(this);
                            chatRooms.add(currentRoom);

                            break;
                        case "ECHO_JOIN":
                            messagePackage.setMessage("Bienvenido " + userName);
                            messagePackage.setUserName("SERVIDOR");
                            echoClients = currentRoom.getClients();

                            for (ClientThread client : echoClients) {
                                client.outputStream.writeUTF(gson.toJson(messagePackage));
                                client.outputStream.writeUTF("CL=" + gson.toJson(currentRoom.clientsInRoom()));
                            }

                            break;
                        case "REGISTER_USERNAME":
                            String userNameToRegister = messagePackage.getUserName();

                            messagePackage.setUserName("SERVIDOR");

                            if (findUserName(userNameToRegister) == false) {
                                userNameList.add(userNameToRegister);
                                messagePackage.setMessage("USER_AVAILABLE");
                            } else {
                                messagePackage.setMessage("USER_UNAVAILABLE");
                            }

                            outputStream.writeUTF(gson.toJson(messagePackage));

                            break;
                        case "EXIT":
                            currentRoom.getClients().remove(this);
                            userConnected = false;
                            messagePackage.setMessage(messagePackage.getUserName() + " ha salido se la sala");
                            messagePackage.setUserName("SERVIDOR");
                            echoClients = currentRoom.getClients();

                            for (ClientThread client : echoClients) {
                                client.outputStream.writeUTF(gson.toJson(messagePackage));
                            }

                            break;
                        default:
                            //Se recuperan los clientes de la habitación a la cuál se desea mandar el mensaje
                            ArrayList<ClientThread> roomClients = currentRoom.getClients();

                            for (ClientThread client : roomClients) {
                                client.outputStream.writeUTF(gson.toJson(messagePackage));
                            }

                            break;
                    }
                }
            }

            inputStream.close();
            outputStream.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}