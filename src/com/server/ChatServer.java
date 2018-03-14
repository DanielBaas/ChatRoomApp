package com.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * El servidor corre en el puerto 5000. Procesa las peticiones de conexión en hilos, haciendo uso de la clase ClientThread
 */
public class ChatServer {

    /*Puerto especificado a usar. Si se desea cambiar el cambio debe realizarse en este atributo*/
    private final static int PORT = 5000;

    /*Lista se salas disponibles*/
    private static ArrayList<ChatRoom> chatRooms = new ArrayList<>();

    /*Lista de nombre de usuario registrados*/
    private static ArrayList<String> userNameList = new ArrayList<>();

    public static void main(String[] args) {
        Socket clientSocket = null;

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);

            System.out.println("Se inicio el servidor en el puerto " + PORT);

            while (true) {
                clientSocket = serverSocket.accept();

                ClientThread client = new ClientThread(clientSocket, chatRooms, userNameList);
                client.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
