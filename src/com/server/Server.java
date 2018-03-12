/**package com.server;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class Server {

    private static ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();

    public static void main(String args[]) {
        int portNumber = 5000;

        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);

            System.out.println("Se inicio el servidor en el puerto " + portNumber);

            //Crea un socket cliente para cada conexión y se lo pasa a un nuevo hilo de cliente
            while (true) {
                Socket clientSocket = serverSocket.accept();

                System.out.println("Nuevo cliente " + clientSocket.getInetAddress());

                ClientHandler newClientHandler = new ClientHandler(clientSocket, clients);

                clients.add(newClientHandler);
                newClientHandler.start();
            }
        } catch (IOException e) {
            System.out.println(e);
        }

    }

}

class ClientHandler extends Thread {

    private String clientName = null;
    private DataInputStream inputStream = null;
    private PrintStream outputStream = null;
    private Socket clientSocket = null;
    private ArrayList<ClientHandler> clients;

    public ClientHandler (Socket clientSocket, ArrayList<ClientHandler> clients) {
        this.clientSocket = clientSocket;
        this.clients = clients;
    }

    public void run() {
        ArrayList<ClientHandler> clients = this.clients;
        String line = "";
        ClientListRequestPackage data = new ClientListRequestPackage();

        try {
            inputStream = new DataInputStream(clientSocket.getInputStream());
            outputStream = new PrintStream(clientSocket.getOutputStream());
            String name;

            while (true) {
                outputStream.println("Enter your name:");
                name = inputStream.readLine().trim();
                if (name.indexOf('@') == -1) {
                    break;
                } else {
                    outputStream.println("The name should not contain '@' character.");
                }
            }

            //Welcome the new the client
            outputStream.println("Welcome " + name + " to our chat room.\nTo leave enter /quit in a new line.");

            System.out.println("client size:"+clients.size()  );
            synchronized (this) {
                for (ClientHandler client : clients) {
                    if (client != null && client == this) {
                        clientName = "@" + name;
                        break;
                    }
                }

                for (ClientHandler client : clients) {
                    if (client != null && client != this) {
                        client.outputStream.println(name + " ha entrado en la sala!");
                    }
                }
            }

            //Star the conversation
            while (true) {
                Gson gsonMessage = new Gson();
                line = inputStream.readLine();
                data = gsonMessage.fromJson(line, ClientListRequestPackage.class);

                if (line.startsWith("/quit")) {
                    break;
                }

                //Broadcast
                synchronized (this) {
                    for (ClientHandler client : clients) {
                        if (client != null && client.clientName != null) {
                            client.outputStream.println(">>" + data.getUserName() + ": " + data.getMessage());
                        }
                    }
                }

            }

            synchronized (this) {
                for (ClientHandler client : clients) {
                    if (client != null && client != this
                            && client.clientName != null) {
                        client.outputStream.println("*** The user " + data.getUserName()
                                + " inputStream leaving the chat room !!! ***");
                    }
                }
            }

            outputStream.println("***" + data.getUserName() + " se ha dejado la sala***");

            //clean
            synchronized (this) {
                for (int i = 0; i < clients.size(); i++) {
                    if (clients.get(i) == this) {
                        clients.remove(i);
                        break;
                    }
                }
            }

            inputStream.close();
            outputStream.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}**/