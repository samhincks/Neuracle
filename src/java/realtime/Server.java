package realtime;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DecimalFormat;


//This class emulates the Python data relay server that relays MATLAB data to applications.
public class Server implements Runnable{
 
  private int PORT = 50003;

  private int count;
  private DecimalFormat dFormat;
  private  PrintWriter out;

  public Server( int PORT){
    this.PORT = PORT;
    this.count = 0;
    this.dFormat = new DecimalFormat("0.00");
  }

  public void open() {
      try{
          System.out.println("A");
          ServerSocket serverSocket = new ServerSocket(PORT);
          Socket clientSocket = serverSocket.accept();
          out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);
          System.out.println("B");
          serverSocket.setReuseAddress(true);
          System.out.println("--- GRfNIRSDataRelayServer --- Client (IP: " + clientSocket.getInetAddress().getHostAddress() + ") connected.");
      }
      catch(Exception e) {
          e.printStackTrace();
      }
  }
  public boolean sendMessage(String message) { 
      if (out == null) return false;
      out.println(message);
      return true;
  }
 
  @Override
  public void run(){
    try{
        System.out.println("--- GRfNIRSDataRelayServer --- IP: " + InetAddress.getLocalHost().getHostAddress() + ", Port: " + PORT);
    }catch(UnknownHostException e){
        e.printStackTrace();
    }
    
    open();
  }

  private String makeFakeData(){
        this.count += 2; //Speed: increasing 2[degree] per 500[msec] = 4[degree/sec]
        double signal = Math.abs(Math.sin(Math.toRadians(this.count)));
        if(count >= 360)
          count = 0;
        String classification;
        if(signal >= 0.5)
          classification = "easy";
        else
          classification = "hard";
        String high = this.dFormat.format(signal * 100);
        String low = this.dFormat.format(100 - (signal * 100));
        return classification + ";" + high + ";" + low;
  }


  public static void main(String[] args) throws InterruptedException{
    Server s = new Server(50001);
    Thread t  = new Thread(s);
    t.start();
    Thread.sleep(20500);
    s.sendMessage("bajs");
  }
}
