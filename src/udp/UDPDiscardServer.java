package udp;

import java.net.*;
import java.io.*;

public class UDPDiscardServer {
    public final static int discardPort = 9;
    static byte[] buffer = new byte[65507];
    public static void main(String[] args) {
        int port;
        try {
            port = Integer.parseInt(args[0]);
        }
        catch (Exception e) {
            port = discardPort;
        }
        try {
            DatagramSocket ds = new DatagramSocket(port);
            while (true) {
                try {
                    DatagramPacket dp = new DatagramPacket(buffer,
                            buffer.length);
                    ds.receive(dp);
                    String s = new String(dp.getData(), 0, dp.getLength());
                    System.out.println(dp.getAddress() + " at port " +
                            dp.getPort() + " says " + s);
                }
                catch (IOException e) {
                    System.err.println(e);
                }
            } // end while
        } // end try
        catch (SocketException se) {
            System.err.println(se);
        } // end catch
    } // end main
}
