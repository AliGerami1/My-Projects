import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.concurrent.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import static java.nio.charset.StandardCharsets.UTF_8;

public class SimpleChatClient {
  private JTextArea incoming;
  private JTextField outgoing;
  private BufferedReader reader;
  private PrintWriter writer;
  private String username;

  public void go() {
    setUpNetworking();
    sendUsername();

    JScrollPane scroller = createScrollableTextArea();

    outgoing = new JTextField(20);
    

    JButton sendButton = new JButton("Send");
    sendButton.addActionListener(e -> sendMessage());
    outgoing.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
          if (e.getKeyCode() == KeyEvent.VK_ENTER) {
              sendMessage();
          }
      }
  });

    JPanel mainPanel = new JPanel();
    mainPanel.add(scroller);
    mainPanel.add(outgoing);
    mainPanel.add(sendButton);

    ExecutorService executor = Executors.newSingleThreadExecutor();
    executor.execute(new IncomingReader());

    JFrame frame = new JFrame("Ludicrously Simple Chat Client");
    frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
    frame.setSize(400, 350);
    frame.setVisible(true);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
  }

  private JScrollPane createScrollableTextArea() {
    incoming = new JTextArea(15, 30);
    incoming.setLineWrap(true);
    incoming.setWrapStyleWord(true);
    incoming.setEditable(false);
    JScrollPane scroller = new JScrollPane(incoming);
    scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    return scroller;
  }

  private void setUpNetworking() {
    try {
      InetSocketAddress serverAddress = new InetSocketAddress("127.0.0.1", 5000);
      SocketChannel socketChannel = SocketChannel.open(serverAddress);

      reader = new BufferedReader(Channels.newReader(socketChannel, UTF_8));
      writer = new PrintWriter(Channels.newWriter(socketChannel, UTF_8));

      System.out.println("Networking established. Client running at: " + socketChannel.getLocalAddress());
      username = JOptionPane.showInputDialog(null, "Enter your username:", "Login", JOptionPane.QUESTION_MESSAGE);
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }
  private void sendUsername() {
    writer.println(username);
    writer.flush();
}

private void sendMessage() {
  String message = outgoing.getText();
  outgoing.setText("");
  outgoing.requestFocus();

  // Prepend the username to the message
  String completeMessage = username + ": " + message;

  writer.println(completeMessage);
  writer.flush();
}

  public class IncomingReader implements Runnable {
    public void run() {
      String message;
      try {
        while ((message = reader.readLine()) != null) {
          System.out.println("From " + message);
          incoming.append(message + "\n");
        }
      } 
      
      catch (IOException ex) {
        //ex.printStackTrace(); you can use it instead of all code below:
      }
    }
  }

  public static void main(String[] args) {
    new SimpleChatClient().go();
  }
}
