package com.controller;

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
    private static boolean closed = false;

    private static String host = "localhost";
    private static int portNumber = 5000;
    private static String userName = "anonimo";
    private static String roomName = "Sala sin nombre";

    private static ChatRoomView view = new ChatRoomView();

    public ClientController() {
        view.getButtonSend().addActionListener(new SendListener());
    }

    public static void main(String[] args) {
        portNumber = 5000;
        host = "localhost";
        roomName = "sala de prueba 1";

        /*
         * Open a socket on a given host and port. Open input and output streams.
         */
        try {
            clientSocket = new Socket(host, portNumber);
            inputLine = new BufferedReader(new InputStreamReader(System.in));
            outputStream = new PrintStream(clientSocket.getOutputStream());
            inputStream = new DataInputStream(clientSocket.getInputStream());
            startView(userName + "@" + roomName);
        } catch (UnknownHostException e) {
            System.err.println("No se encontro el host " + host);
        } catch (IOException e) {
            System.err.println("No se pudo obtener I/O del host " + host);
        }

        /*
         * If everything has been initialized then we want to write some data to the
         * socket we have opened a connection to on the port portNumber.
         */
        if (clientSocket != null && outputStream != null && inputStream != null) {
            try {

                /* Create a thread to read from the server. */
                new Thread(new ClientController()).start();
                while (!closed) {
                    String message = inputLine.readLine().trim();

                    if (userName.equals("anonimo")){
                        userName = message;
                    }

                    outputStream.println(userName);
                }
                /*
                 * Close the output stream, close the input stream, close the socket.
                 */
                outputStream.close();
                inputStream.close();
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("IOException:  " + e);
            }
        }
    }//Fin main

    public static void writeMessage(String message) {
        outputStream.println(message);
    }

    /**
     * Función que queda a la espera de mensajes del servidor
     */
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

    /**
     * Inicia la GUI
     * @param roomName Nombre que tendrá la ventana. Corresponde al nombre de la sala del foro.
     */
    public static void startView(String roomName) {
        view.setTitle(roomName);
        view.pack();
        view.setLocationRelativeTo(null);
        view.setVisible(true);
    }

    /**
     * Recibe el texto introducido por el usuario en la ventana de chat.
     */
    private void sendMessage() {
        String inputMessage = view.getTextInputArea().getText();

        if ( (!inputMessage.equals("")) && inputMessage != null ) {
            view.getTextInputArea().setText("");
            writeMessage(inputMessage);
        }
    }

    /**
     * Agrega los nuevos mensajes a la ventana del chat.
     * @param message Mensaje a mostrar en el chat.
     */
    public void showMessage(String message) {
        view.getTextOuputArea().append(message + "\n");
    }

    /**
     * Espera del evento correspondiente a presionar el botón "Enviar" dentro de la ventana del chat.
     */
    class SendListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent event) {
            if (event.getSource() == view.getButtonSend()) {
                sendMessage();
            }
        }
    }

}
