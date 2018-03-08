package multicast;

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class MulticastPeer{
    public static void main(String args[]){   // args give message contents
        String grp = "228.5.6.7";
        MulticastSocket s =null;
        try {
            InetAddress group = InetAddress.getByName(grp);
            s = new MulticastSocket(6789);
            s.joinGroup(group);
            String mensaje = new Scanner(System.in).nextLine();
            byte [] m = mensaje.getBytes();
            DatagramPacket messageOut = new DatagramPacket(m, m.length, group, 6789);
            s.send(messageOut);
            // get messages from others in group
            byte[] buffer = new byte[1000];
            for(int i=0; i< 5; i++) {
                DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
                s.receive(messageIn);
                System.out.println("Received:" + new String(messageIn.getData()));
            }
            s.leaveGroup(group);
        }catch (SocketException e){System.out.println("Socket: " + e.getMessage());
        }catch (IOException e){System.out.println("IO: " + e.getMessage());}
    }
}
