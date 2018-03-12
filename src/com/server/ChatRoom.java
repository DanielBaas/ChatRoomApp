package com.server;

import java.util.ArrayList;

public class ChatRoom {
    private String roomName;
    private ArrayList<ClientThread> clients;

    public ChatRoom(String roomName) {
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

}
