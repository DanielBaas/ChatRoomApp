package com.model;

public class ChatRoomModel {

    private String roomName;
    private String userName;
    private String address;
    private String port;
    private String message;

    public ChatRoomModel(String roomName, String userName, String address, String port) {
        this.roomName = roomName;
        this.userName = userName;
        this.address = address;
        this.port = port;
        this.message = "";
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
