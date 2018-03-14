package com.server;

import java.util.ArrayList;

/**
 * Objeto que representa las salas del chat. Sirve como contenedor para las instacias ClientThread, además de identificarse
 * con un nombre de sala.
 */
public class ChatRoom {

    /*Nombre de la sala*/
    private String roomName;

    /*Conjunto de procesos que representan a los clientes conectados a la sala*/
    private ArrayList<ClientThread> clients;

    public ChatRoom (String roomName) {
        this.roomName = roomName;
        clients = new ArrayList<>();
    }

    public void addClient (ClientThread client) {
        synchronized (this) {
            clients.add(client);
        }
    }

    public String getRoomName () {
        return roomName;
    }

    public ArrayList<ClientThread> getClients () {
        return clients;
    }

    public String[] getClientsInRoom() {
        String[] clientsInRoom = new String[clients.size()];
        int i = 0;

        for (ClientThread currentClient : clients) {
            clientsInRoom[i] = currentClient.getUserName();
            i++;
        }

        return clientsInRoom;
    }

}
