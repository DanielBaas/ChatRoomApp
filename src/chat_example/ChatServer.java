package chat_example;

import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {

    public static void main(String[] args) {
        //if (args.length != 1)
        //    throw new RuntimeException ("Sintaxis: ChatServer <port>");

        try {
            //int serverPort = Integer.parseInt(args[0]);
            int serverPort = 4444;
            ServerSocket server = new ServerSocket(serverPort);

            while (true) {
                Socket client = server.accept();
                System.out.println("Aceptado de" + client.getInetAddress());
                ChatHandler c = new ChatHandler(client);
                c.start();
            }
        } catch(IOException io) {
            io.printStackTrace();
        }
    }

}