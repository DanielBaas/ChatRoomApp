package com.client;

import com.controller.ChatRoomController;
import com.view.ChatRoomView;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client implements Runnable {

    private static Socket clientSocket;
    private static PrintStream outputStream;
    private static DataInputStream inputStream;
    private static BufferedReader inputLine = null;
    private static boolean closed;

    private static String host;
    private static int portNumber;
    private String userName;
    private String roomName;

    private static ChatRoomView view;
    private static ChatRoomController controller;

    public Client() {
        clientSocket = null;
        outputStream = null;
        inputStream = null;
        closed = false;

        host = "localhost";
        portNumber = 5000;
        userName = "Anonimo";
        roomName = "Sala sin nombre";

        view = new ChatRoomView();
        controller = new ChatRoomController(view);
    }

    public static String getHost() {
        return host;
    }

    public static void setHost(String host) {
        Client.host = host;
    }

    public static int getPortNumber() {
        return portNumber;
    }

    public static void setPortNumber(int portNumber) {
        Client.portNumber = portNumber;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = userName + "@" + roomName;
    }

    public static void main(String[] args) {
        /*
         * Open a socket on a given host and port. Open input and output streams.
         */
        try {
            clientSocket = new Socket(host, portNumber);
            inputLine = new BufferedReader(new InputStreamReader(System.in));
            outputStream = new PrintStream(clientSocket.getOutputStream());
            inputStream = new DataInputStream(clientSocket.getInputStream());
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + host);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to the host "
                    + host);
        }

        /*
         * If everything has been initialized then we want to write some data to the
         * socket we have opened a connection to on the port portNumber.
         */
        if (clientSocket != null && outputStream != null && inputStream != null) {
            try {

                /* Create a thread to read from the server. */
                new Thread(new Client()).start();
                while (!closed) {
                    outputStream.println(inputLine.readLine().trim());
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
    }

    /*
     * Create a thread to read from the server. (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    public void run() {
        /*
         * Keep on reading from the socket till we receive "Bye" from the
         * server. Once we received that then we want to break.
         */
        String responseLine;
        try {
            while ((responseLine = inputStream.readLine()) != null) {
                System.out.println(responseLine);
                if (responseLine.indexOf("*** Bye") != -1)
                    break;
            }
            closed = true;
        } catch (IOException e) {
            System.err.println("IOException:  " + e);
        }
    }
}