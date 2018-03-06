package chat_example;

import java.net.*;
import java.io.*;
import java.util.*;

public class ChatHandler extends Thread{

    protected Socket s;
    protected DataInputStream i;
    protected DataOutputStream o;
    public static Vector handlers = new Vector();

    public ChatHandler(Socket s) throws IOException {
        this.s = s;
        i = new DataInputStream (new BufferedInputStream(s.getInputStream()));
        o = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
    }

    public void run(){
        try {
            handlers.addElement(this);
            while (true){
                String msg = i.readUTF();
                broadcast(msg);
            }//end while
        }//end try
        catch(IOException io){
            io.printStackTrace();
        }//end
        finally {
            handlers.removeElement (this);
            try {
                s.close();
            }//end try
            catch(IOException io){
                io.printStackTrace();
            }//end catch
        }//end finally
    }//end public run

    protected static void broadcast(String message){
        synchronized (handlers){
            Enumeration e = handlers.elements();
            while (e.hasMoreElements()){
                ChatHandler c = (ChatHandler)e.nextElement();
                try {
                    synchronized (c.o) {
                        c.o.writeUTF(message);
                    }//end synchronized
                    c.o.flush();
                }//end try
                catch (IOException io){
                    c.stop();
                }//end catch
            }//end while
        }//end synchronized
    }//end protected broadcast

}