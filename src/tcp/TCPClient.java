package tcp;

import com.google.gson.Gson;
import udp.MessagePackage;

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class TCPClient {

    //argumentos: Mensaje y nombre del servidor
    public static void main (String args[]) {
        Socket serverSocket = null;
        Scanner keyboard = new Scanner(System.in);
        int serverPort = 7896;
        String host = "localhost";
        String message = "";
        String userName = "";
        String roomName = "";
        MessagePackage messagePackage = new MessagePackage();


        System.out.println("Cual es tu nombre de usuario?");
        userName = keyboard.nextLine();

        System.out.println("Cual es el nombre de la sala?");
        roomName = keyboard.nextLine();

        try{
            serverSocket = new Socket(host, serverPort);
            DataInputStream inputStream = new DataInputStream(serverSocket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(serverSocket.getOutputStream());

            messagePackage.setUserName(userName.trim());
            messagePackage.setRoomName(roomName);

            Receiver r = new Receiver(inputStream);
            Sender s = new Sender(outputStream, messagePackage);
            Thread rt = new Thread(r);
            Thread st = new Thread(s);
            rt.start();
            st.start();

            outputStream.writeUTF(args[0]); // UTF is a string encoding
        }catch (UnknownHostException e){
            e.printStackTrace();
        }catch (EOFException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}

class Receiver implements Runnable{
    private DataInputStream inputStream;

    public Receiver(DataInputStream inputStream) {
        this.inputStream = inputStream;
    }

    public void run() {
        while (true) {
            try {
                String message = inputStream.readUTF();
                System.out.println(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class Sender implements Runnable {
    private DataOutputStream outputStream;
    private MessagePackage messagePackage;

    public Sender(DataOutputStream outputStream, MessagePackage messagePackage) {
        this.outputStream = outputStream;
        this.messagePackage = messagePackage;
    }

    public void run() {
        Scanner keyboard = new Scanner(System.in);
        String message = "";
        Gson gson = new Gson();

        try{
            message = "JOIN_ROOM";
            messagePackage.setMessage(message);
            outputStream.writeUTF(gson.toJson(messagePackage));

            while (true) {
                message = keyboard.nextLine();
                messagePackage.setMessage(message);
                outputStream.writeUTF(gson.toJson(messagePackage));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}