package com.server;

import com.google.gson.Gson;
import com.model.MessagePackage;

import javax.xml.crypto.Data;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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

    private static boolean supportOnline = false;

    public static void main(String[] args) {
        Socket clientSocket = null;

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Se inicio el servidor en el puerto " + PORT);

            while (true) {
                clientSocket = serverSocket.accept();
                DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());

                if (supportOnline) {
                    ClientThread client = new ClientThread(clientSocket, chatRooms, userNameList);
                    client.start();
                } else {
                    String messageIn = inputStream.readUTF();

                    Gson gson = new Gson();
                    MessagePackage messagePackage = gson.fromJson(messageIn, MessagePackage.class);
                    String userName = messagePackage.getUserName().trim();

                    if (userName.equals("SOPORTE")) {
                        supportOnline = true;
                        ClientThread client = new ClientThread(clientSocket, chatRooms, userNameList);
                        client.setUserName(userName);
                        client.setSupportClient(client);
                        client.start();
                    } else {
                        messagePackage.setUserName("SERVIDOR");
                        messagePackage.setMessage("SUPPORT_NOT_ONLINE");
                        outputStream.writeUTF(gson.toJson(messagePackage));
                        inputStream.close();
                        outputStream.close();
                        clientSocket.close();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
