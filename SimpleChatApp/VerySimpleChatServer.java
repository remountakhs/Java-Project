
/**
 * VerySimpleChatServer
 */
import java.io.*;
import java.net.*;
import java.util.*;
import javafx.util.Pair;

public class VerySimpleChatServer {
    ArrayList clientOutputStreams; // Arraylist that will hold each clientSocket connected to our server. This is
                                   // to be able to iterate all the clients subscribed.
    Pair<Integer, String> pair = new Pair<>(1, "One");

    public class ClientHandler implements Runnable { // Innerclass to handle each client connected and create a loop to
                                                     // listen to each of his messages.
        BufferedReader reader;
        Socket sock;

        public ClientHandler(Socket clientSocket) {
            try {
                sock = clientSocket;
                InputStreamReader isReader = new InputStreamReader(sock.getInputStream()); // Whenever a new client is
                                                                                           // connected get the
                                                                                           // inputStream to a buffer
                reader = new BufferedReader(isReader);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public void run() {
            String message;
            try {
                while ((message = reader.readLine()) != null) { // Whenever there is information on the
                                                                // inputStream(message from the client), read it and
                                                                // send it to tellEveryone()
                    System.out.println("Read " + message);
                    tellEveryone(message, sock.hashCode());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new VerySimpleChatServer().go(); // Initialize the server
    }

    public void go() {
        clientOutputStreams = new ArrayList(); // The clients arraylist
        try {
            ServerSocket serverSock = new ServerSocket(5000); // The port the server will run on

            while (true) { // Loop and accept each socket connection
                Socket clientSocket = serverSock.accept();
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream()); // Whenever a client connects, get
                                                                                      // his outputstream and put it on
                                                                                      // the arraylist so we can send
                                                                                      // messages to everyone
                clientOutputStreams.add(new Pair<Integer, PrintWriter>(clientSocket.hashCode(), writer));
                Thread clientHandlingThread = new Thread(new ClientHandler(clientSocket)); // Start each clientHandling(listening for
                                                                        // messages) in a new thread
                clientHandlingThread.start();
                System.out.println("Got a connection");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void tellEveryone(String message, int socketHashcode) {
        Iterator it = clientOutputStreams.iterator(); // Iterate over each clientoutputstream and send the message
        while (it.hasNext()) {
            try {
                Pair<Integer, PrintWriter> currentPair = (Pair) it.next();
                PrintWriter writer = currentPair.getValue();

                if (currentPair.getKey() != socketHashcode) { // Do not send message to myself again.
                    System.out.println("written");
                    writer.println(currentPair.getKey() + ": " + message);
                    writer.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}