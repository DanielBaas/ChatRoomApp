package com.server;

import ejemplo2.Client;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayList;

public class Server {

    // This chat server can accept up to maxClientsCount clients' connections.
    private static final int maxClientsCount = 10;
    private static ClientThread[] threads = new ClientThread[maxClientsCount];
    private static ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();

    public static void main(String args[]) {
        int portNumber = 5000;

        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);

            System.out.println("Se inicio el servidor en el puerto " + portNumber);

            //Crea un socket cliente para cada conexión y se lo pasa a un nuevo hilo de cliente
            while (true) {
                Socket clientSocket = serverSocket.accept();

                System.out.println("Se acepto conexion de " + clientSocket.getInetAddress());

                int i = 0;
                for (i = 0; i < maxClientsCount; i++) {
                    if (threads[i] == null) {
                        (threads[i] = new ClientThread(clientSocket, threads)).start();
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }

    }
}

class ClientHandler extends Thread {

}

/*
 * The chat client thread. This client thread opens the input and the output
 * streams for a particular client, ask the client's name, informs all the
 * clients connected to the server about the fact that a new client has joined
 * the chat room, and as long as it receive data, echos that data back to all
 * other clients. The thread broadcast the incoming messages to all clients and
 * routes the private message to the particular client. When a client leaves the
 * chat room this thread informs also all the clients about that and terminates.
 */
class ClientThread extends Thread {

    private String clientName = null;
    private DataInputStream inputStream = null;
    private PrintStream outputStream = null;
    private Socket clientSocket = null;
    private final ClientThread[] threads;
    private int maxClientsCount;

    public ClientThread(Socket clientSocket, ClientThread[] threads) {
        this.clientSocket = clientSocket;
        this.threads = threads;
        maxClientsCount = threads.length;
    }

    public void run() {
        int maxClientsCount = this.maxClientsCount;
        ClientThread[] threads = this.threads;

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

            /* Welcome the new the client. */
            outputStream.println("Welcome " + name
                    + " to our chat room.\nTo leave enter /quit in a new line.");
            synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] == this) {
                        clientName = "@" + name;
                        break;
                    }
                }
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] != this) {
                        threads[i].outputStream.println("*** A new user " + name
                                + " entered the chat room !!! ***");
                    }
                }
            }
            /* Start the conversation. */
            while (true) {
                String line = inputStream.readLine();
                if (line.startsWith("/quit")) {
                    break;
                }
                /* If the message inputStream private sent it to the given client. */
                if (line.startsWith("@")) {
                    String[] words = line.split("\\s", 2);
                    if (words.length > 1 && words[1] != null) {
                        words[1] = words[1].trim();
                        if (!words[1].isEmpty()) {
                            synchronized (this) {
                                for (int i = 0; i < maxClientsCount; i++) {
                                    if (threads[i] != null && threads[i] != this
                                            && threads[i].clientName != null
                                            && threads[i].clientName.equals(words[0])) {
                                        threads[i].outputStream.println("<" + name + "> " + words[1]);
                                        /*
                                         * Echo this message to let the client know the private
                                         * message was sent.
                                         */
                                        this.outputStream.println(">" + name + "> " + words[1]);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    /* The message inputStream public, broadcast it to all other clients. */
                    synchronized (this) {
                        for (int i = 0; i < maxClientsCount; i++) {
                            if (threads[i] != null && threads[i].clientName != null) {
                                threads[i].outputStream.println("<" + name + "> " + line);
                            }
                        }
                    }
                }
            }
            synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] != this
                            && threads[i].clientName != null) {
                        threads[i].outputStream.println("*** The user " + name
                                + " inputStream leaving the chat room !!! ***");
                    }
                }
            }
            outputStream.println("*** Bye " + name + " ***");

            /*
             * Clean up. Set the current thread variable to null so that a new client
             * could be accepted by the server.
             */
            synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] == this) {
                        threads[i] = null;
                    }
                }
            }

            inputStream.close();
            outputStream.close();
            clientSocket.close();
        } catch (IOException e) {
        }
    }
}