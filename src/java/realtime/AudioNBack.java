package realtime;

import java.io.File;
import javax.sound.sampled.*;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author samhincks
 */
public class AudioNBack implements Runnable{
    public String directory = "web/WEB-INF/audio/"; 
    public static boolean nbackRunning = false;
    private  String [] files; 
    private Clip clip;
    private int kBack;
    private int length; //.. length to play in milliseconds
    private int sleepLength =1000;
    private Client client = null;
    private String condition;
    private int [] a = {1,8,2,3,9,5,4,0,7,6,0,2,8,3,5,4,0,1,8,4,6,2,9,7,3,5,1,6};
    private int [] b = {7,4,9,0,8,3,6,1,5,2,8,0,5,1,2,6,2,6,5,1,8,0,3,4,9,7,2,9};
    private int []  c = {4,0,3,5,8,7,2,6,1,9,6,4,3,2,9,7,7,0,6,4,9,1,8,2,3,5,1,5};
    private int [] d = {2,6,5,1,8,0,3,4,9,7,2,9,3,6,7,4,7,0,6,4,9,1,8,2,3,5,1,5};
    public int  sequence =-1;
    
    private Boolean interrupted = false;
    private Integer interruptLength =1000;
    private int interrupts =0;
    
    public AudioNBack(int kBack, int length) {
        this.kBack = kBack;
        this.length = length;
    }
    
    /*State in advance what the current sequence is*/
    public AudioNBack(int kBack, int length, int sequence) {
        this.kBack = kBack;
        this.length = length;
        this.sequence = sequence;
        this.length = this.length -2000;
    }
    
    public AudioNBack(int kBack, int length, Client client) {
        this.kBack = kBack;
        this.length = length;
        this.client = client;
        if(kBack ==0) condition = "easy\n";
        else if (kBack ==1) condition ="medium\n";
        else/*(kBack ==2)*/condition = "hard\n";
    }
    
    public void interrupt(int duration) {
        synchronized(interrupted) {
            interrupted = true;
            interruptLength = duration;
            interrupts++;
        }
    }
    public boolean isInterrupted() {
        synchronized(interrupted) {
            return interrupted;
        }
        
    }
    @Override
    public void run() {
        files = new String[] {directory + "rbnbacka.wav", directory + "rbnbackb.wav", directory + "rbnbackc.wav",directory + "rbnbackd.wav" };

        if (kBack ==0) {
            playIntro(directory + "0back.wav");
        } else if (kBack ==1){
            playIntro(directory + "1back.wav");
        }
        else if (kBack ==-1)
            System.out.println("Skipping intro");
        else
            playIntro(directory + "2back.wav");
        if (sequence ==-1)
            sequence = (int) (Math.random() * files.length);
        
        //.. save current sequence to static variable
        play(files[sequence]);
    }
    private void playIntro(String filename) {
        try{  
            clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(new File(filename)));
            clip.start();
            while (!clip.isRunning()) {
                Thread.sleep(sleepLength);
            }
            while (clip.isRunning()) {
                Thread.sleep(sleepLength);
            }
            clip.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }  
    }
    private void play(String filename) {
        try {
            if (client != null)
                nbackRunning = true;
            int ticks =0;
            clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(new File(filename)));
            clip.start();
            while (!clip.isRunning()) {
                Thread.sleep(sleepLength);
            }
            while (clip.isRunning()){
                Thread.sleep(sleepLength); 
                int duration  = ticks*sleepLength + interrupts*interruptLength;
                if (this.isInterrupted()) {
                    if (interruptLength >0){
                        clip.stop();
                        Thread.sleep(interruptLength);
                        clip.start();
                        this.interrupted = false;       
                    }
                    else //.. just close the clip    
                        duration = length +1; 
                }
                if (duration>length) {
                    clip.close();
                    if(client != null){
                        client.sendMessage("next");
                        client.disconnect();
                    }    
                    nbackRunning = false;
                    return;    
                }  
                
                if (client!=null) {  
                    client.sendMessage(condition);
                }
                ticks++;  
            }  
            if (client!= null)
                client.disconnect();
            clip.close();
        }
        catch (Exception exc)
        {
            exc.printStackTrace(System.out);
            System.err.println("err");
        }
        
    }
    
    /** args[0] = 0 or 1, for 0 versus 1 nback.
     *  args[1] = #played 
     **/
    public static void main(String[] args) {
        try{  
            boolean zeroBack = true;
            if (args.length == 1) {
                int nback = Integer.parseInt(args[0]);
                if (nback == 0) zeroBack =true;
            }
            AudioNBack nBack = new AudioNBack(-1, 10000); //.. 5000 actually lasts 12 second
            Thread t = new Thread(nBack);
            t.start();
            Thread.sleep(3000);
            nBack.interrupt(3000);
            //Thread.sleep(1000);
            
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    
}
