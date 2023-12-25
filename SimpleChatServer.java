
import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;
import static java.nio.charset.StandardCharsets.UTF_8;

public class SimpleChatServer {
    private final List<PrintWriter> clientWriter = new ArrayList<>();
     private Map<SocketChannel, String> usernames = new HashMap<>();
    
    public static void main(String[] args) {
        new SimpleChatServer().go();
    }

    public void go() {
        // Create a thread pool to handle client connections concurrently
        ExecutorService threadPool = Executors.newCachedThreadPool();

        try {
            // Open a server socket channel and bind it to a specific port (5000)
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress("127.0.0.1",5000));
            System.out.println("<Server> Server running at: " + serverSocketChannel.getLocalAddress());

            while (true) {
                // Accept a new client connection
                SocketChannel clientSocket = serverSocketChannel.accept();

                // Get the client's IP address
                InetSocketAddress clientAddress = (InetSocketAddress) clientSocket.getRemoteAddress();
                String clientIp = clientAddress.getHostString();
                
                System.out.println("<Server> New client connected from IP: " + clientIp);
                // Create a new PrintWriter to send messages to the client
                /** the writer is defined before the reader in the ClientHandler class because the server needs to be able to send messages to the client before it receives any messages from the client.  */
                BufferedReader reader = new BufferedReader(Channels.newReader(clientSocket, UTF_8));
                PrintWriter writer = new PrintWriter(Channels.newWriter(clientSocket, UTF_8));
                clientWriter.add(writer);

                // Receive the username
                String username = reader.readLine();
                // Store the username with its associated socket channel
                usernames.put(clientSocket, username);
                // Submit a new ClientHandler task to the thread pool to handle client messages
                threadPool.submit(new ClientHandler(clientSocket, clientIp));
            }
            }
            catch (IOException e) {
                System.out.println("<Server> Server error: " + e.getMessage());
                //e.printStackTrace(); you can remove it and use above.
            } finally {
                    threadPool.shutdown();
                }
        }

        private void tellEveryone (String message) {
            for (PrintWriter writer : clientWriter) {
                writer.println(message);
                writer.flush();
            }
        }


    public class ClientHandler implements Runnable {
        private final SocketChannel Socket;
        private final BufferedReader reader;
        private String clientIp;

        public ClientHandler (SocketChannel clientSocket, String clientIp) {
            this.Socket = clientSocket;
            this.clientIp = clientIp;
            this.reader = new BufferedReader(Channels.newReader(Socket, UTF_8));
        }
        
        @Override
        public void run() {
            String message;
            try {
                // Keep reading messages from the client until the connection is closed
                while((message = reader.readLine()) != null) {
                    System.out.println("<Server> <"+ clientIp + ">=" + message);
                    tellEveryone(message);
                }
            }
            catch (SocketException e) {
            System.out.println("<Server>Forced Close happened for: " + clientIp );
            // Handle the connection reset exception as needed
            }
            catch (IOException e) {
                e.printStackTrace();
                System.out.println("<Server>Socket is Closed ");
            }   
        }  
    }
}
