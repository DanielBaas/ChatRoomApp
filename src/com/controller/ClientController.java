/**package com.controller;

import com.google.gson.Gson;
import com.view.ChatRoomView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientController implements Runnable {

    private static Socket clientSocket = null;
    private static PrintStream outputStream = null;
    private static DataInputStream inputStream = null;
    private static BufferedReader inputLine = null;

    private static String host = "localhost";
    private static int portNumber = 5000;

    private static boolean closed = false;
    private static String userName = "anonimo";
    private static String roomName = "Sala sin nombre";

    private static ClientListRequestPackage packageToSend = null;
    private static ChatRoomView view = new ChatRoomView();

    public ClientController() {
        view.getButtonSend().addActionListener(new SendListener());
    }

    public static void main(String[] args) {
        portNumber = 5000;
        host = "localhost";
        roomName = "sala de prueba 1";

        try {
            clientSocket = new Socket(host, portNumber);

            inputLine = new BufferedReader(new InputStreamReader(System.in));
            outputStream = new PrintStream(clientSocket.getOutputStream());
            inputStream = new DataInputStream(clientSocket.getInputStream());

            packageToSend = new ClientListRequestPackage();
            packageToSend.setRoomName(roomName);

            startView(userName + "@" + roomName);
        } catch (UnknownHostException e) {
            System.err.println("No se encontro el host " + host);
        } catch (IOException e) {
            System.err.println("No se pudo obtener I/O del host " + host);
        }

        if (clientSocket != null && outputStream != null && inputStream != null) {
            try {

                new Thread(new ClientController()).start();
                while (!closed) {}

                outputStream.close();
                inputStream.close();
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("IOException:  " + e);
            }
        }
    }//Fin main

    public static void writeMessage(String message) {
        Gson gsonMessage = new Gson();

        packageToSend.setMessage(message);

        if (userName.equals("anonimo")) {
            userName = message;
            outputStream.println(userName);
            packageToSend.setUserName(userName);
        } else {
            outputStream.println(gsonMessage.toJson(packageToSend));
        }
    }

    public void run() {
        String responseLine;

        try {
            while ((responseLine = inputStream.readLine()) != null) {
                showMessage(responseLine);
            }

            closed = true;
        } catch (IOException e) {
            System.err.println("IOException:  " + e);
        }
    }

    public static void startView(String roomName) {
        view.setTitle(roomName);
        view.pack();
        view.setLocationRelativeTo(null);
        view.setVisible(true);
    }

    private void sendMessage() {
        String inputMessage = view.getTextInputArea().getText();

        if ( (!inputMessage.equals("")) && inputMessage != null ) {
            view.getTextInputArea().setText("");
            writeMessage(inputMessage);
        }
    }

    public void showMessage(String message) {
        view.getTextOuputArea().append(message + "\n");
    }

    class SendListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent event) {
            if (event.getSource() == view.getButtonSend()) {
                sendMessage();
            }
        }
    }

}**/
