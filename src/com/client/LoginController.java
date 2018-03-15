package com.client;

import com.google.gson.Gson;
import com.model.MessagePackage;
import com.view.LoginView;
import sun.rmi.runtime.Log;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class LoginController {

    private final int PORT = 5000;
    private final String HOST = "localhost";

    private static Socket socket = null;
    private static DataInputStream inputStream = null;
    private static DataOutputStream outputStream = null;
    private LoginView view;

    public LoginController() {
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
    private void logIn(String userName) {
        try {
            socket = new Socket(HOST, PORT);
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());

            Gson gson = new Gson();
            MessagePackage messagePackage = new MessagePackage(userName, "LOGIN", "REGISTER_USERNAME");

            /*Enviamos una petición para validar el nombre de usuario*/
            outputStream.writeUTF(gson.toJson(messagePackage));

            String response = inputStream.readUTF();
            messagePackage = gson.fromJson(response, MessagePackage.class);
            String messageIn = messagePackage.getMessage();

            switch (messageIn) {
                case "USER_UNAVAILABLE":
                    JOptionPane.showMessageDialog(null, "Nombre de usuario no disponible!");
                    break;
                case "USER_AVAILABLE":
                    /*Se crea una nueva instancia de la ventana que lista las salas de chat disponibles*/
                    new RoomListController(socket, inputStream, outputStream, userName);
                    view.dispose();
                    break;
                case "SUPPORT_NOT_ONLINE":
                    JOptionPane.showMessageDialog(null, "El servicio de soporte no se encuentra en línea");
                    break;
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
    private class LoginListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String userName = view.getTextUserName().getText();

            if (!userName.isEmpty()) {
                userName = userName.toUpperCase().trim();

                if (userName.equals("SOPORTE")) {
                    JOptionPane.showMessageDialog(null, "Nombre de usuario no válido");
                } else {
                    logIn(userName);
                }
            }
        }
    }

    public static void main(String[] args) {
        new LoginController();
    }

}
