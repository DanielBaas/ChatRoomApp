package com.client;

import com.google.gson.Gson;
import com.model.GsonPackage;
import com.view.ChatRoomView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ChatClient extends Thread {

    private static final int PORT = 5000;
    private static final String HOST = "localhost";

    private Socket clientSocket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    private String userName;
    private String roomName;

    private static ChatRoomView view = new ChatRoomView();

    public ChatClient(String userName, String roomName) {
        clientSocket = null;
        inputStream = null;
        outputStream = null;
        this.userName = userName;
        this.roomName = roomName;
        view.getButtonSend().addActionListener(new SendListener());

        this.start();
    }

    public void run () {
        try {
            GsonPackage messagePackage = null;
            Gson gson = new Gson();
            Scanner keyboard = new Scanner(System.in);
            clientSocket = new Socket(HOST, PORT);
            inputStream = new DataInputStream(clientSocket.getInputStream());
            outputStream = new DataOutputStream(clientSocket.getOutputStream());

            messagePackage = new GsonPackage(userName, roomName, "JOIN");

            outputStream.writeUTF(gson.toJson(messagePackage));

            startView(userName + "@" + roomName);

            messagePackage.setMessage("ECHO_JOIN");
            outputStream.writeUTF(gson.toJson(messagePackage));

            Receiver receiver = new Receiver(inputStream);

            receiver.start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startView(String roomName) {
        view.setTitle(roomName);
        view.pack();
        view.setLocationRelativeTo(null);
        view.setVisible(true);
    }

    private void sendMessage() {
        try {
            //Leemos lo que el String que el usuario introdujo en su ventana
            String messageOut = view.getTextInputArea().getText();

            if ( (!messageOut.equals("")) && messageOut == null) {
                Gson gson = new Gson();

                //Empaquetamos el mensaje a enviar
                GsonPackage messagePackage = new GsonPackage(userName, roomName, messageOut);

                //Limpiamos el area de texto del usuario después de que presiona enviar
                view.getTextInputArea().setText("");

                //Enviamos el mensaje al servidor
                outputStream.writeUTF(gson.toJson(messagePackage));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class SendListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent event) {
            if (event.getSource() == view.getButtonSend()) {
                sendMessage();
            }
        }
    }

    class Receiver extends Thread {
        private DataInputStream inputStream;

        public Receiver (DataInputStream inputStream) {
            this.inputStream = inputStream;
        }

        public void run() {
            while (true) {
                try {
                    Gson gson = new Gson();

                    //Leemos el String json que contiene la información del mensaje
                    String messageIn = inputStream.readUTF();

                    System.out.println(messageIn);
                    //Convertimos el json a un objeto MessagePackage para poder acceder a la información del mensaje
                    GsonPackage messsagePackage = gson.fromJson(messageIn, GsonPackage.class);

                    //Preparamos el mensaje que se mostrará en la ventana del usuario
                    String messageToShow = "<<" + messsagePackage.getUserName() + ">> " + messsagePackage.getMessage();

                    //Mostramos el mensaje en pantalla
                    view.getTextOuputArea().append(messageToShow + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        inputStream.close();
                        outputStream.close();
                        clientSocket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

}
