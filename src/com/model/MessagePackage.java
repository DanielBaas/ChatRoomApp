package com.model;

/**
 * Clase que sirve como contenedor para los mensajes enviados entre el usuario y el servidor. Cuando se crea un mensaje
 * que desea ser enviado, el nombre del usuario que lo envía, la sala a la cuál debe llegar y el mensaje que se desea
 * enviar son empaquetados en este objeto, sólo para luego ser transformados a Gson y ser enviados.
 */
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
