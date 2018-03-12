package com.model;

public class MessagePackage {
    private String userName;
    private String roomName;
    private String message;

    public MessagePackage(String userName, String roomName, String message) {
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
