import java.net.*;
import java.util.TimerTask;
import java.util.Timer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class SimpleChatClientA{

    JTextArea incoming;
    JTextField outgoing;
    PrintWriter writer;
    Socket sock;
    BufferedReader reader;

    public void go(){
        JFrame frame = new JFrame("Simple chat");
        JPanel mainPanel = new JPanel();
        incoming = new JTextArea(15, 50);
        incoming.setLineWrap(true);
        incoming.setWrapStyleWord(true);
        incoming.setEditable(false);
        JScrollPane qScroller = new JScrollPane(incoming);
        qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        outgoing = new JTextField(20);

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new SendButtonListener());
        mainPanel.add(qScroller);
        mainPanel.add(outgoing);
        mainPanel.add(sendButton);
        setUpNetworking();

        frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
        frame.setSize(700, 400);
        frame.setVisible(true);
    } 

    private void setUpNetworking(){
        Timer timer = new java.util.Timer();

        timer.schedule(new TimerTask(){ 
            private final int MAX_RETRIES = 20;
            private int currentRetries;

            @Override
            public void run() {
                if(currentRetries == MAX_RETRIES){
                    System.out.println("Maximum retries reached, shutting down client!");
                    timer.cancel();
                    System.exit(0);
                }
                try {
                    sock = new Socket("127.0.0.1", 5000);
                    InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
                    reader = new BufferedReader(streamReader);
                    writer = new PrintWriter(sock.getOutputStream());
                    System.out.println("Networking established");

                    Thread readerThread = new Thread(new IncomingReader());
                    readerThread.start();
                    timer.cancel();
                }catch(ConnectException ce){
                    System.out.println("Not connected yet, retrying in 5 seconds again.");
                    currentRetries++;
                }catch (IOException e) {
                    e.printStackTrace();
                    timer.cancel();
                }
            }
        }, 5000, 5000);

    }

    public class SendButtonListener implements ActionListener{
        public void actionPerformed(ActionEvent event){
            try {
                String text = outgoing.getText(); //Get the text from the textbox
                writer.println(text); //Send it to the socket writer
                writer.flush();
                incoming.append("Me: " + text + "\n");
                outgoing.setText("");
                outgoing.requestFocus();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class IncomingReader implements Runnable{
        public void run(){
            String message;
            try{
                while((message = reader.readLine()) != null){
                    System.out.println("read " + message);
                    incoming.append(message + "\n");
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new SimpleChatClientA().go();
    }
}