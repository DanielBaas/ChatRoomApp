package com.server;

import com.google.gson.Gson;
import com.model.MessagePackage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Clase que procesa la conexión TCP con cada cliente. Recibe mensajes empaquetados en formato JSON y responde de la misma
 * manera. Cada instancia de esta clase se trabaja como un hilo individual.
 */
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

    /**
     * Cada instacia de ClientThread debe ser identificada con el nombre del usuario al cuál atiende. Este método regresa
     * el nombre del usuario asignado a este hilo.
     * @return Nombre del usuario al cuál se está atendiendo.
     */
    public String getUserName () {
        return userName;
    }

    /**
     * Retorna una ChatRoom a la que se desea enviar mensajes u obtener información.
     * @param roomName Nombre de la sala deseada.
     * @return Objeto ChatRoom correspondiente a la sala deseada. Retorna null en caso de no ser encontrada.
     */
    private ChatRoom findRoom(String roomName) {
        /*Si la lista de salas está vacía, se retorna null*/
        if (chatRooms.size() == 0){
            return null;
        }

        /*Se busca la sala en el arreglo de salas. Se hace de manera síncrona*/
        synchronized (this) {
            for (ChatRoom currentChatRoom : chatRooms) {
                if (currentChatRoom.getRoomName().equals(roomName)) {
                    return currentChatRoom;
                }
            }

            return null;
        }
    }

    /**
     * Busca un nombre de usuario en el arreglo de nombres de usuario para comprobar su disponibilidad.
     * @param userNameToFind Nombre de usuario a buscar.
     * @return true si el nombre ya se encuentra en el arreglo de nombres de usuario, false en caso contrario.
     */
    private boolean findUserName (String userNameToFind) {
        if (userNameList.size() == 0) {
            return false;
        }

        /*Se busca el nombre de usuario en el arreglo de nombres de usuario. Se hace de manera síncrona*/
        synchronized (this) {
            for (String currentUserName : userNameList) {
                if (currentUserName.equals(userNameToFind)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Retorna el arreglo de nombres de las salas disponibles.
     * @return String[] que contiene los nombres de las salas disponibles.
     */
    public String[] getRoomNamesList () {
        String[] roomNamesList = new String[chatRooms.size()];
        int i = 0;

        for (ChatRoom currentRoom : chatRooms) {
            roomNamesList[i] = currentRoom.getRoomName();
            i++;
        }

        return roomNamesList;
    }

    /**
     * Método que contiene la ejecución principal del hilo. Recibe los mensajes del usuario, los procesa y regresa las
     * respuestas adecuadas.
     */
    public void run () {

        /*Variable de control que indica la conexión con el usuario. En el momento que se vuelta false indicará que la
        * conexión con el usuario debe ser terminada*/
        boolean userConnected = true;

        try {

            /*Se usa un ciclo de duración indeterminada para recibir, procesar y enviar mensajes*/
            while (userConnected) {
                inputStream = new DataInputStream(clientSocket.getInputStream());
                outputStream = new DataOutputStream(clientSocket.getOutputStream());

                Gson gson = new Gson();

                /*Recibimos el mensaje del usuario en formato gson*/
                String messageIn = inputStream.readUTF();

                /*Convertimos el mensaje gson a un objeto de la clase MessagePackage para acceder a los elementos del mensaje*/
                MessagePackage messagePackage = gson.fromJson(messageIn, MessagePackage.class);

                /*Leemos el mensaje que escribió el usuario*/
                String message = messagePackage.getMessage().trim();
                String roomName = messagePackage.getRoomName().trim();

                /*Sala actual con la que el usuario realiza comunicación*/
                ChatRoom currentRoom = findRoom(roomName);

                /*Lista de clientes conectados a la sala en uso*/
                ArrayList<ClientThread> echoClients = null;

                /*Se muestra el contenido del mensaje recibido del usuario, en formato gson*/
                System.out.println(messageIn);

                /*El procesamiento de la petición del usuario a través de su mensaje y la respuesta se realiza de manera
                * síncrona*/
                synchronized (this) {
                    switch (message) {

                        /*Petición para crear una nueva sala*/
                        case "CREATE":
                            if (currentRoom == null) {
                                currentRoom = new ChatRoom(roomName);
                                chatRooms.add(currentRoom);
                                messagePackage.setMessage("ROOM_AVAILABLE");
                            } else {
                                messagePackage.setMessage("ROOM_NOT_AVAILABLE");
                            }

                            messagePackage.setUserName("SERVIDOR");
                            outputStream.writeUTF(gson.toJson(messagePackage));

                            break;

                            /*Petición para unirse a una sala existente*/
                        case "JOIN":
                            if (currentRoom != null) {
                                userName = messagePackage.getUserName().trim();
                                currentRoom.addClient(this);
                            }

                            break;

                        /*Petición para enviar un mensaje a todos los usuarios de la sala indicando que un nuevo
                        * usuario se ha conectado a la sala en uso*/
                        case "ECHO_JOIN":
                            messagePackage.setUserName("SERVIDOR");
                            messagePackage.setMessage("Bienvenido " + userName);
                            echoClients = currentRoom.getClients();

                            for (ClientThread client : echoClients) {
                                client.outputStream.writeUTF(gson.toJson(messagePackage));

                                /*Se envia un mensaje a todos los usuarios dentro de la salam indicando que deben actualizar
                                * su lista de usuarios en su ventana pues un usuario ha salido de la sala*/
                                client.outputStream.writeUTF("CL=" + gson.toJson(currentRoom.getClientsInRoom()));
                            }

                            break;

                        /*Petición para registrar un nombre de usuario al realizar el login*/
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

                        /*Petición para recuperar la lista de nombres de las salas disponibles y poder mostrarlas en la
                        * ventana el usuario*/
                        case "LIST_ROOMS":
                            outputStream.writeUTF("RL=" + gson.toJson(getRoomNamesList()));
                            break;

                        /*Petición para salir de una sala. Se informa al resto de los usuarios dentro de la sala que alguien
                        * ha salido y que actualicen su ventana*/
                        case "EXIT_ROOM":

                            /*Se remueve al cliente de la sala*/
                            currentRoom.getClients().remove(this);

                            /*Se actualiza la variable del while para romepr comunicación con el cliente*/
                            userConnected = false;

                            messagePackage.setUserName("SERVIDOR");
                            messagePackage.setMessage(messagePackage.getUserName() + " ha salido se la sala");
                            echoClients = currentRoom.getClients();

                            for (ClientThread client : echoClients) {
                                client.outputStream.writeUTF(gson.toJson(messagePackage));
                                client.outputStream.writeUTF("CL=" + gson.toJson(currentRoom.getClientsInRoom()));
                            }

                            break;

                        /*Petición para salir de la aplicación. Se remueve el nombre de usuario de la lista para que pueda
                        * ser usado por nuevos clientes*/
                        case "EXIT_APP":
                            userNameList.remove(messagePackage.getUserName());

                            /*Se actualiza la variable del while para romepr comunicación con el cliente*/
                            userConnected = false;

                            break;

                        /*Petición para enviar mensajes de manera generala a todos los clientes de la sala*/
                        default:

                            /*Se recuperan los clientes de la habitación a la cuál se desea mandar el mensaje*/
                            ArrayList<ClientThread> roomClients = currentRoom.getClients();

                            for (ClientThread client : roomClients) {
                                client.outputStream.writeUTF(gson.toJson(messagePackage));
                            }

                            break;
                    }
                }
            }

            /*Se cierra la conexión con el cliente*/
            inputStream.close();
            outputStream.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}