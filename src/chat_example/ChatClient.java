package chat_example;

import java.net.*;
import java.io.*;
import java.awt.*;

public class ChatClient extends Frame implements Runnable {
    protected DataInputStream i;
    protected DataOutputStream o;
    protected TextArea output;
    protected TextField input;
    protected Thread listener;

    public ChatClient(String title, InputStream i, OutputStream o) {
        super (title);
        this.i = new DataInputStream (new BufferedInputStream(i));
        this.o = new DataOutputStream (new BufferedOutputStream(o));
        setLayout (new BorderLayout ());
        add ("Center", output = new TextArea());
        output.setEditable (false);
        add("South", input = new TextField());
        pack();
        show();
        input.requestFocus();
        listener = new Thread(this);
        listener.start();
    }

    public void run(){
        try{
            while (true){
                String line = i.readUTF();
                output.appendText(line+"\n");
            }//end while
        }//end try
        catch(IOException io){
            io.printStackTrace();
        }//end catch
        finally{
            listener = null;
            input.hide();
            validate();
            try{
                o.close();
            }//end try
            catch(IOException io){
                io.printStackTrace();
            }//end catch
        }//end finally
    }

    public boolean handleEvent (Event e){
        if ((e.target == input) && (e.id == Event.ACTION_EVENT)){
            try{
                o.writeUTF ((String) e.arg);
                o.flush();
            }//edn try
            catch (IOException io){
                io.printStackTrace();
                listener.stop();
            }//end catch
            input.setText("");
            return true;

        }//end if
        else if ((e.target == this) && (e.id == Event.WINDOW_DESTROY)){
            if (listener != null)
                listener.stop();
            hide();
            return true;

        }//end else if
        return super.handleEvent(e);
    }

    public static void main(String args[]) {
        //if (args.length != 2)
        //    throw new RuntimeException ("Sintaxis: ChatClient <host> <puerto>");

        try {
            //String serverHost = args[0];
            //int serverPort = Integer.parseInt(args[1]);
            String serverHost = "localhost";
            int serverPort = 4444;

            Socket s = new Socket (serverHost, serverPort);
            new ChatClient("Chat "+ serverHost + ":" + serverPort, s.getInputStream(),s.getOutputStream());
        }//end try
        catch(IOException io){
            io.printStackTrace();
        }//end catch
    }

}