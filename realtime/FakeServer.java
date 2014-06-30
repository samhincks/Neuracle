/* This code is used as a fake server to send the data to the web server.
 * It uses socket communication.The port number is 8899.  
 * 
 * */
 

package FakeServer;
import java.net.*;
import java.io.*;

public class FakeServer {  
   
   public static void main(String args[]) throws IOException, InterruptedException {  
	      int port = 8899;  
	     
	      //create socket servers
	      ServerSocket server = new ServerSocket(port);  
	     
	      //Listen for client block until client connect,
	      System.out.println("Waiting for Connection");
	      Socket socket = server.accept();  
	      
	      //Receive the data from client
	      Reader reader = new InputStreamReader(socket.getInputStream());  
	      char chars[] = new char[64];  
	      int len;  
	      StringBuilder sb = new StringBuilder();  
	      String temp;  
	      int index;  
	      while ((len=reader.read(chars)) != -1) {  
	         temp = new String(chars, 0, len);  
	         if ((index = temp.indexOf("eof")) != -1) {
	            sb.append(temp.substring(0, index));  
	            break;  
	         }  
	         sb.append(temp);  
	      }  
	      System.out.println("from client: " + sb);  
	     
	      
	      // Send fake data to the client
	      
	      Writer writer = new OutputStreamWriter(socket.getOutputStream()); 
	      writer.write(" [testfile; chanA, chanB, chanC, chanD; classA, classB]+ ");
	      writer.flush();
	      Thread.sleep(3000);
	      
	      int i=4;
	      while(i>0){
	          writer.write("Testfile; 234, 556, 567, 232; Highworkload, Angry");
	          i--;
	          writer.flush();
	          Thread.sleep(1000);
	      }
	          
	      writer.close();  
	      reader.close();  
	      socket.close();  
	      server.close();  
	   }  
	     
	}  