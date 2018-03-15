package com.support;

import java.util.ArrayList;

public class RoomInfoSupport {

    private String roomName;
    private ArrayList<String> messages;

    public RoomInfoSupport (String roomName) {
        this.roomName = roomName;
        messages = new ArrayList<>();
    }

    public void addMessage (String message) {
        messages.add(message);
    }

    public String getRoomName () {
        return roomName;
    }

    public ArrayList<String> getMessages () {
        return messages;
    }
}
