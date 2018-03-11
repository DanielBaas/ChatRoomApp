package tcp;

import com.google.gson.Gson;
import udp.MessagePackage;

import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class TCPServer {
    private static MulticastIP currentAddress;
    private static ArrayList<ConnectionHandler.RoomMulticast> rooms;

    public TCPServer () {
        rooms = new ArrayList<ConnectionHandler.RoomMulticast>();
        currentAddress = new MulticastIP();
    }

    public static void main (String args[]) {
        try{
            int serverPort = 7896;
            ServerSocket listenSocket = new ServerSocket(serverPort);

            while(true) {
                Socket clientSocket = listenSocket.accept();

                ConnectionHandler connection = new ConnectionHandler(clientSocket, rooms, currentAddress);
                connection.start();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}

class MulticastIP {
    private final String START_MULTICAST_ADDRESS = "224.0.0.1";

    private int firstQuartet;
    private int secondQuartet;
    private int thirdQuartet;
    private int fourthQuartet;

    //224.0.0.1 a 239.255.255.255
    public MulticastIP (int firstQuartet, int secondQuartet, int thirdQuartet, int fourthQuartet) {
        this.firstQuartet = firstQuartet;
        this.secondQuartet = secondQuartet;
        this.thirdQuartet = thirdQuartet;
        this.fourthQuartet = fourthQuartet;
    }

    public MulticastIP () {
        this.firstQuartet = 224;
        this.secondQuartet = 0;
        this.thirdQuartet = 0;
        this.fourthQuartet = 1;
    }

    public String newAddress() {
        if (fourthQuartet == 255) {
            fourthQuartet = 0;

            if (thirdQuartet == 255) {
                thirdQuartet = 0;

                if (secondQuartet == 255) {
                    secondQuartet = 0;

                    if (firstQuartet == 239) {
                        firstQuartet = 224;
                        fourthQuartet = 1;
                    } else {
                        firstQuartet++;
                    }
                } else {
                    secondQuartet++;
                }
            } else {
                thirdQuartet++;
            }
        } else {
            fourthQuartet++;
        }

        return getAddress();
    }

    public String getAddress() {
        return firstQuartet + "." + secondQuartet + "." + thirdQuartet + "." + fourthQuartet;
    }
}

class ConnectionHandler extends Thread {
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private Socket clientSocket;
    private ArrayList<RoomMulticast> rooms;
    private MulticastIP currentAddress;

    public ConnectionHandler (Socket clientSocket, ArrayList<RoomMulticast> rooms, MulticastIP currentAddress) {
        try {
            this.clientSocket = clientSocket;
            this.inputStream = new DataInputStream(clientSocket.getInputStream());
            this.outputStream = new DataOutputStream(clientSocket.getOutputStream());
            this.rooms = rooms;
            this.currentAddress = currentAddress;
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void run(){
        ConnectionHandlerReceiver receiver = new ConnectionHandlerReceiver(inputStream, rooms, currentAddress);
        ConnectionHandlerSender sender = new ConnectionHandlerSender();

        receiver.start();
        sender.start();
    }

    public class ConnectionHandlerReceiver extends Thread {
        private DataInputStream inputStream;
        private ArrayList<RoomMulticast> rooms;
        private MulticastIP currentAddress;

        public ConnectionHandlerReceiver (DataInputStream inputStream, ArrayList<RoomMulticast> rooms, MulticastIP currentAddress) {
            this.inputStream = inputStream;
            this.rooms = rooms;
            this.currentAddress = currentAddress;
        }

        public void run() {
            while (true) {
                try {
                    Gson gson = new Gson();
                    MessagePackage messagePackage = new MessagePackage();
                    String message = "";

                    //Se recibe el mensaje del cliente
                    message = inputStream.readUTF();

                    //Se convierte el mensaje recibido a un objeto para poder desempaquetarlo y manejarlo
                    messagePackage = gson.fromJson(message, MessagePackage.class);

                    String action = messagePackage.getMessage();
                    String roomName = messagePackage.getRoomName();
                    MulticastSocket socket = new MulticastSocket();
                    InetAddress roomAddress = null;

                    switch (action) {
                        case "JOIN_ROOM":
                            RoomMulticast room = findRoom(roomName);

                            if (room == null) {
                                //Verificamos si la ip de multicast está disponible, sino, se genera una nueva
                                while (true) {
                                    roomAddress = InetAddress.getByName(currentAddress.getAddress());

                                    if (roomAddress.isReachable(5000)) {
                                        currentAddress.newAddress();
                                    } else {
                                        break;
                                    }
                                }

                                //Creamos una nueva sala
                                room = new RoomMulticast(roomAddress, roomName);

                                //Añadimos la nueva sala a la lista de salas disponbiles
                                rooms.add(room);

                                room.start();
                            }



                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private RoomMulticast findRoom(String roomName) {
            for (RoomMulticast room : rooms) {
                if (room.getRoomName().equals(roomName)) {
                    return room;
                }
            }

            return null;
        }
    }

    public class ConnectionHandlerSender extends Thread {
        private DataOutputStream outputStream;

        public void run() {
            try {
                String message = "";
                Gson gson = new Gson();

                /*INICIO DE CODIGO MULTICAST PARA RECIBIR MENSAJE*/

                /*FIN DE CODIGO MULTICAST PARA RECIBIR MENSAJE*/

                //Envia el mensaje al cliente
                outputStream.writeUTF(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
