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
    public AudioNBack(int kBack, int length) {
        this.kBack = kBack;
        this.length = length;
    }
    
    public AudioNBack(int kBack, int length, Client client) {
        this.kBack = kBack;
        this.length = length;
        this.client = client;
        if(kBack ==0) condition = "easy\n";
        else if (kBack ==1) condition ="medium\n";
        else/*(kBack ==2)*/condition = "hard\n";
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
        int randFile = (int) (Math.random() * files.length);
        play(files[randFile]);
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
                int duration  = ticks*sleepLength;
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
            AudioNBack nBack = new AudioNBack(0, 5000); //.. 5000 actually lasts 12 second
            Thread t = new Thread(nBack);
            t.start();
            //Thread.sleep(1000);
            
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    
}
