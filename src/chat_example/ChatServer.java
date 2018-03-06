package chat_example;

import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {

    public ChatServer(int port) throws IOException {
        ServerSocket server = new ServerSocket(port);
        while (true) {
            Socket client = server.accept();
            System.out.println("Aceptado de" + client.getInetAddress());
            ChatHandler c = new ChatHandler(client);
            c.start();
        }
    }

    public static void main(String[] args) {
        if (args.length != 1)
            throw new RuntimeException ("Sintaxis: ChatServer <port>");
        try {
            new ChatServer(Integer.parseInt(args[0]));
        }//end try
        catch(IOException io) {
            io.printStackTrace();
        }//end catch
    }//end public main

}