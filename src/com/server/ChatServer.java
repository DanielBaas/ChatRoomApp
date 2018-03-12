package com.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ChatServer {

    private final static int PORT = 5000;

    private static ArrayList<ChatRoom> chatRooms = new ArrayList<ChatRoom>();

    public static void main(String[] args) {
        Socket clientSocket = null;

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);

            System.out.println("Se inicio el servidor en el puerto " + PORT);

            while (true) {
                clientSocket = serverSocket.accept();

                ClientThread client = new ClientThread(clientSocket, chatRooms);
                client.start();
            }


        } catch (IOException e) {
            e.printStackTrace();

            try {
                clientSocket.getInputStream().close();
                clientSocket.getOutputStream().close();
                clientSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

}
