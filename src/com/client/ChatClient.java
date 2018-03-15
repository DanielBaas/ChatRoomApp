package com.client;

import com.google.gson.Gson;
import com.model.MessagePackage;
import com.view.ChatRoomView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Clase que maneja la ventana de envío y recibo de mensajes para una sala. Se crea una nueva conexión TCP con el servidor
 * que estará activa mientras el usuario mantenga la ventana abierta.
 */
public class ChatClient extends Thread {

    private final int PORT = 5000;
    private final String HOST = "localhost";

    private Socket clientSocket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    private String userName;
    private String roomName;
    private boolean isConnected;

    private ChatRoomView view;

    public ChatClient(String userName, String roomName, ChatRoomView view) {
        clientSocket = null;
        inputStream = null;
        outputStream = null;
        this.userName = userName;
        this.roomName = roomName;
        isConnected = false;

        this.view = view;
        view.getButtonSend().addActionListener(new SendListener());
        view.addWindowListener(new CloseListener());

        this.start();
    }

    /**
     * Ejecución principal de la clase. Realiza una solicitud para unirse a la sala deseada, así como para informar
     * a todos los usuarios que un nuevo cliente entró.
     */
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

    /**
     * Inicia la ventana del usuario.
     * @param roomName Nombre de la sala a la cual se realizó la conexión.
     */
    public void startView(String roomName) {
        view.setTitle(roomName);
        view.pack();
        view.setLocationRelativeTo(null);
        view.setVisible(true);
    }

    /**
     * Procesa las entradas del usuario, las empaqueta y las envía al servidor.
     */
    private void sendMessage() {
        try {
            /*Leemos lo que el String que el usuario introdujo en su ventana*/
            String messageOut = view.getTextInputArea().getText().trim();

            if (!messageOut.isEmpty() && messageOut != null) {
                Gson gson = new Gson();

                /*Empaquetamos el mensaje a enviar*/
                MessagePackage messagePackage = new MessagePackage(userName, roomName, messageOut);

                /*Limpiamos el area de texto del usuario después de que presiona enviar*/
                view.getTextInputArea().setText("");

                /*Enviamos el mensaje al servidor*/
                outputStream.writeUTF(gson.toJson(messagePackage));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Recibe el evento de cierre de la ventana del usuario, y envía una petición al servidor para desconexión.
     */
    private class CloseListener extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent closeEvent) {
            try {
                MessagePackage messagePackage = null;
                Gson gson = new Gson();

                //Empaquetamos un mensaje para hacer petición de salir de la sala
                messagePackage = new MessagePackage(userName, roomName, "EXIT_ROOM");

                //Enviamos la petición
                outputStream.writeUTF(gson.toJson(messagePackage));
                isConnected = false;
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Recibe los evento de presionado del botón "Enviar" (o tecla intro)
     */
    private class SendListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent event) {
            if (event.getSource() == view.getButtonSend()) {
                sendMessage();
            }
        }
    }

    /**
     * Thread que procesa los mensajes recibidos del servidor.
     */
    private class Receiver extends Thread {
        private DataInputStream inputStream;

        public Receiver (DataInputStream inputStream) {
            this.inputStream = inputStream;
        }

        public void run() {
            try {
                while (isConnected) {
                    Gson gson = new Gson();
                    String messageIn = "";

                    /*Leemos el String json que contiene la información del mensaje*/
                    messageIn = inputStream.readUTF();

                    /*Si el mensaje recibido es una Client List, se actualiza la ventan del usuario con los clientes en la sala*/
                    if (messageIn.contains("CL=")) {
                        /*Se extrae la lista de clientes del mensaje recibido*/
                        messageIn = messageIn.substring(3);
                        String[] clientsInRoom = gson.fromJson(messageIn, String[].class);

                        /*Se actualiza la ventana del usuario*/
                        view.getListUsers().setListData(clientsInRoom);
                    } else if (messageIn.equals("SUPPORT_OFFLINE")) {
                        JOptionPane.showMessageDialog(null, "El servicio de soporte en línea ha finalizado.\nGracias!");
                        view.dispose();
                    } else {
                        /*Convertimos el json a un objeto MessagePackage para poder acceder a la información del mensaje*/
                        MessagePackage messsagePackage = gson.fromJson(messageIn, MessagePackage.class);

                        /*Preparamos el mensaje que se mostrará en la ventana del usuario*/
                        String messageToShow = "<<" + messsagePackage.getUserName() + ">> " + messsagePackage.getMessage();

                        /*Mostramos el mensaje en pantalla*/
                        view.getTextOuputArea().append(messageToShow + "\n");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
