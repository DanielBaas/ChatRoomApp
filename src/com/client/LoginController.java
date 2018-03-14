package com.client;

import com.google.gson.Gson;
import com.model.MessagePackage;
import com.view.LoginView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class LoginController {

    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private LoginView view;

    public LoginController(Socket socket, DataInputStream inputStream, DataOutputStream outputStream) {
        this.socket = socket;
        this.inputStream = inputStream;
        this.outputStream = outputStream;

        view = new LoginView();
        view.getButtonLogin().addActionListener(new LoginListener());
        view.setLocationRelativeTo(null);
        view.setVisible(true);
    }

    /**
     * Realiza conexión con el servidor para validar que el nombre de usuario dado por el usuario aún esté disponible.
     * Cuando recibe la respuesta del servidor, en caso de ser válida, procede a crear una instacia de la ventana con la
     * lista de salas disponibles. En caso de que el nombre de usuario no esté disponible, muestra un mensaje de error.
     * @param userName Nombre de usario que se desea validar
     */
    public void logIn(String userName) {
        try {
            Gson gson = new Gson();
            MessagePackage messagePackage = new MessagePackage(userName, "LOGIN", "REGISTER_USERNAME");

            /*Enviamos una petición para validar el nombre de usuario*/
            outputStream.writeUTF(gson.toJson(messagePackage));

            String response = inputStream.readUTF();
            messagePackage = gson.fromJson(response, MessagePackage.class);

            /*Si el nombre de usuario no está disponble, se muestra una ventana emergente con un mensaje de error*/
            if (messagePackage.getMessage().equals("USER_UNAVAILABLE")) {
                JOptionPane.showMessageDialog(null, "Nombre de usuario no disponible!");
            } else if (messagePackage.getMessage().equals("USER_AVAILABLE")) {
                /*Se crea una nueva instancia de la ventana que lista las salas de chat disponibles*/
                new RoomListController(socket, inputStream, outputStream, userName);
                view.dispose();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Listener que espera el evento de presionado del botón Login
     */
    class LoginListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String userName = view.getTextUserName().getText().trim();

            if (!userName.isEmpty()) {
                logIn(userName);
            }
        }
    }

    /**
     * Inicia la aplicación para el cliente. Se conecta al servidor usando el host y puerto especificados usando el protocolo
     * TCP.
     * @param args
     */
    public static void main(String[] args) {
        try {
            final int PORT = 5000;
            final String HOST = "localhost";

            Socket clientSocket = new Socket(HOST, PORT);
            DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());

            new LoginController(clientSocket, inputStream, outputStream);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
