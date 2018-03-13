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

    public void logIn(String userName) {
        try {
            Gson gson = new Gson();
            MessagePackage messagePackage = new MessagePackage(userName, "LOGIN", "REGISTER_USERNAME");

            /*Enviamos una petición para validar el nombre de usuario*/
            outputStream.writeUTF(gson.toJson(messagePackage));

            String response = inputStream.readUTF();
            messagePackage = gson.fromJson(response, MessagePackage.class);

            if (messagePackage.getMessage().equals("USER_UNAVAILABLE")) {
                JOptionPane.showMessageDialog(null, "Nombre de usuario no disponible!");
            } else if (messagePackage.getMessage().equals("USER_AVAILABLE")) {
                //Se crea una nueva instancia de la ventana que lista las salas de chat disponibles
                new RoomListController(socket, inputStream, outputStream, userName);
                view.dispose();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class LoginListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String userName = view.getTextUserName().getText().trim();

            if (!userName.isEmpty()) {
                logIn(userName);
            }
        }
    }

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
