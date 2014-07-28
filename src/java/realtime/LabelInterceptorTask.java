/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package realtime;

import dao.InputParser;
import dao.TransformationParser;
import dao.datalayers.BiDAO;
import dao.techniques.TechniqueDAO;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import stripes.ext.ThisActionBeanContext;
import timeseriestufts.kth.streams.DataLayer;

/**
 *
 * @author samhincks
 */
public class LabelInterceptorTask implements Runnable{
    public static final int LABELPORT = 13237;
    
    private int port;
    private ServerSocket socket;
    private Socket connectionSocket;
    private BufferedReader inFromClient;
    TransformationParser tp;
    String dbName;
    String labelName;
    ThisActionBeanContext ctx;
    int pingDelay;

    public LabelInterceptorTask(int port,
            String dbName, String labelName, TransformationParser tp, int pingDelay) throws Exception{
        this.port = port;
        
        this.tp = tp;
        this.dbName = dbName;
        this.labelName = labelName;
        this.pingDelay = pingDelay;
    }
    
    @Override
    public void run() {
        try {
            this.socket = new ServerSocket(port);
            connectionSocket = socket.accept();
            inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            getMessage();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

    }
    public void getMessage( ){
        try {
            String clientSentence = inFromClient.readLine();
            if (clientSentence == null) {
                Thread.sleep(pingDelay);
                getMessage();
            }
            else if(clientSentence.equals("next")) {
                String[] parameters = new String[]{dbName, labelName, "rest"};
                tp.label(parameters);
                this.disconnect();
                run();
            }
            else if (clientSentence.equals("end")) {
                this.disconnect();
                return;
            } else {
                String[] parameters = new String[]{dbName, labelName, clientSentence};
                tp.label(parameters);
            }

            Thread.sleep(pingDelay);
            getMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public void disconnect() throws Exception {
        this.socket.close();
        connectionSocket.close();
        inFromClient.close();

    }
    
    public static void main(String[] args) {
        try {
            LabelInterceptorTask lt = new LabelInterceptorTask(LabelInterceptorTask.LABELPORT, "","",null,500);
            Thread t = new Thread(lt);
            t.start();
        }
        catch(Exception e) {e.printStackTrace();}
    }
  
    
    
}
