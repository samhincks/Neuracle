/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package realtime;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class Client {

  private static String SERVER_IP;
  private int port;

  private Socket socket;
  private DataOutputStream toServer ;

  public Client(int port) throws Exception{
      this.port = port;
      SERVER_IP = InetAddress.getLocalHost().getHostAddress();
      this.socket = new Socket(SERVER_IP, port);
      toServer = new DataOutputStream(this.socket.getOutputStream());
  }

  public void disconnect() throws Exception{
        this.socket.close();
        toServer.close();
  }


  public void sendMessage(String message) throws Exception{
     toServer.writeBytes(message);
  }
  
    public static void main(String[] args) {
        try{
            Client c = new Client(LabelInterceptorTask.LABELPORT);
            c.disconnect();
        }
        catch(Exception e ) {e.printStackTrace();}
    }
}
