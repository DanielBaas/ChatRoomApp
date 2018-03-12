package com.client;

import com.google.gson.Gson;
import com.model.MessagePackage;
import com.view.ChatRoomView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClient extends Thread {

    private static final int PORT = 5000;
    private static final String HOST = "localhost";

    private Socket clientSocket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    private String userName;
    private String roomName;
    private boolean isConnected;

    private static ChatRoomView view = new ChatRoomView();

    public ChatClient(String userName, String roomName) {
        clientSocket = null;
        inputStream = null;
        outputStream = null;
        this.userName = userName;
        this.roomName = roomName;
        isConnected = false;

        view.getButtonSend().addActionListener(new SendListener());
        view.addWindowListener(new CloseListener());

        this.start();
    }

    public void run () {
        try {
            MessagePackage messagePackage = null;
            Gson gson = new Gson();
            clientSocket = new Socket(HOST, PORT);
            inputStream = new DataInputStream(clientSocket.getInputStream());
            outputStream = new DataOutputStream(clientSocket.getOutputStream());

            messagePackage = new MessagePackage(userName, roomName, "JOIN");

            outputStream.writeUTF(gson.toJson(messagePackage));

            startView(userName + "@" + roomName);
            isConnected = true;

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
            String messageOut = view.getTextInputArea().getText().trim();

            if (!messageOut.isEmpty()) {
                Gson gson = new Gson();

                //Empaquetamos el mensaje a enviar
                MessagePackage messagePackage = new MessagePackage(userName, roomName, messageOut);

                //Limpiamos el area de texto del usuario después de que presiona enviar
                view.getTextInputArea().setText("");

                //Enviamos el mensaje al servidor
                outputStream.writeUTF(gson.toJson(messagePackage));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class CloseListener extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent closeEvent) {
            try {
                MessagePackage messagePackage = null;
                Gson gson = new Gson();

                messagePackage = new MessagePackage(userName, roomName, "EXIT");

                outputStream.writeUTF(gson.toJson(messagePackage));
                isConnected = false;

                //inputStream.close();
                //outputStream.close();
                //clientSocket.close();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            try {
                while (isConnected) {
                    Gson gson = new Gson();
                    String messageIn = "";

                    //Leemos el String json que contiene la información del mensaje
                    messageIn = inputStream.readUTF();

                    System.out.println(messageIn);
                    //Convertimos el json a un objeto MessagePackage para poder acceder a la información del mensaje
                    MessagePackage messsagePackage = gson.fromJson(messageIn, MessagePackage.class);

                    //Preparamos el mensaje que se mostrará en la ventana del usuario
                    String messageToShow = "<<" + messsagePackage.getUserName() + ">> " + messsagePackage.getMessage();

                    //Mostramos el mensaje en pantalla
                    view.getTextOuputArea().append(messageToShow + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
