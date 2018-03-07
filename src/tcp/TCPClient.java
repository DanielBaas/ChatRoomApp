//package tcp;

import java.net.*;
import java.io.*;

public class TCPClient {

    //argumentos: Mensaje y nombre del servidor
    public static void main (String args[]) {
        Socket s = null;

        try{
            int serverPort = 7896;
            s = new Socket(args[1], serverPort);
            DataInputStream in = new DataInputStream( s.getInputStream());
            DataOutputStream out = new DataOutputStream( s.getOutputStream());

            out.writeUTF(args[0]); // UTF is a string encoding
            String data = in.readUTF();
            System.out.println("Received: "+ data) ;
        }catch (UnknownHostException e){
            System.out.println("Sock:"+e.getMessage());
        }catch (EOFException e){
            System.out.println("EOF:"+e.getMessage());
        }catch (IOException e){
            System.out.println("IO:"+e.getMessage());
        }
    }
}