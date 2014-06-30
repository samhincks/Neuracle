/*This part is use to commnunication with the fake server
 and return the content to the framwork
 
Enhao

*/
package realtimereceiver;

import java.io.*;
import java.net.*;

public class Client {

    public String run() {
        String content =  new String();
        try {
            byte[] input = new byte[100];
            
            String host = "127.0.0.1";
            int port = 8899;
           
         // Connect to the Server
            Socket client = new Socket(host, port);
         
        // Send data to the Server
            Writer writer = new OutputStreamWriter(client.getOutputStream());
            writer.write("The conection is connected");
            writer.write("eof");
            writer.flush();
        // Read data from 
            Reader reader = new InputStreamReader(client.getInputStream());
            char chars[] = new char[5000];
            int len;
            StringBuffer sb = new StringBuffer();
            String temp;
            int index;
            while ((len = reader.read(chars)) != -1) {
                temp = new String(chars, 0, len);
                sb.append(temp);
                if ((index = temp.indexOf("eof")) != -1) {
                    sb.append(temp.substring(0, index));
                    break;
                }
                

            }
            content = sb.toString();
            writer.close();
            reader.close();
            client.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return content;

    }

}





