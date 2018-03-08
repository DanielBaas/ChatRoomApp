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
import java.util.Scanner;

public class Client implements Runnable {

    private static Socket clientSocket = null;
    private static PrintStream outputStream = null;
    private static DataInputStream inputStream = null;
    private static BufferedReader inputLine = null;
    private static boolean closed = false;

    private static String host = "localhost";
    private static int portNumber = 5555;
    private static String userName = "anonimo";
    private static String roomName = "Sala sin nombre";

    private static ChatRoomView view = new ChatRoomView();
    private static ChatRoomController controller = new ChatRoomController(view);

    public void setRoomName(String roomName) {
        this.roomName = userName + "@" + roomName;
    }

    public static void main(String[] args) {
        // The default port.
        portNumber = 5000;
        // The default host.
        host = "localhost";
        System.out.println("user?");
        userName = new Scanner(System.in).nextLine();
        roomName = "sala de prueba 1";

        /*
         * Open a socket on a given host and port. Open input and output streams.
         */
        try {
            clientSocket = new Socket(host, portNumber);
            inputLine = new BufferedReader(new InputStreamReader(System.in));
            outputStream = new PrintStream(clientSocket.getOutputStream());
            inputStream = new DataInputStream(clientSocket.getInputStream());
            controller.startView(userName + "@" + roomName);
        } catch (UnknownHostException e) {
            System.err.println("No se encontro el host " + host);
        } catch (IOException e) {
            System.err.println("No se pudo obtener I/O del host " + host);
        }

        writeMessage();
    }

    public static void writeMessage() {
        /*
         * If everything has been initialized then we want to write some data to the
         * socket we have opened a connection to on the port portNumber.
         */
        if (clientSocket != null && outputStream != null && inputStream != null) {
            try {

                /* Create a thread to read from the server. */
                new Thread(new Client()).start();
                while (!closed) {
                    String messsage = inputLine.readLine().trim();
                    outputStream.println(messsage);
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
                controller.showMessage(responseLine);
            }
            closed = true;
        } catch (IOException e) {
            System.err.println("IOException:  " + e);
        }
    }
}