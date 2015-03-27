/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package filereader.experiments;

import filereader.EvaluationInterface;
import filereader.TSTuftsFileReader;
import static filereader.experiments.AJExperiment.testAllAJ;
import java.util.ArrayList;
import timeseriestufts.evaluatable.Dataset;
import timeseriestufts.evaluatable.TechniqueSet;
import timeseriestufts.evaluatable.performances.Performances;
import timeseriestufts.evaluation.experiment.Classification;
import timeseriestufts.kth.streams.bi.ChannelSet;
import timeseriestufts.kth.streams.bi.Instance;
import timeseriestufts.kth.streams.quad.MultiExperiment;
import timeseriestufts.kth.streams.tri.Experiment;
import timeseriestufts.kth.streams.uni.Channel;

/**
 *
 * @author samhincks
 */
public class IDExperiment extends EvaluationInterface{
    public static double obs =0;
    public static void main(String [] args) {
        try {
            int[] requested = {1};

            ArrayList<String> files = IDExperiment.getSpecific(requested);
            for (String s : files) {
                //leftRightML();
                channelDistances(); 
            }

        }
        catch(Exception e) {e.printStackTrace();}
        
    }
    
    private static void channelDistances() throws Exception {
        int[] requested = {1};
        ArrayList<String> filesRight = IDExperiment.getSpecific(requested);
        for (String s : filesRight) {
            ChannelSet cs = getChannelSet(s);
            System.out.print("X,");
            for (Channel a : cs.streams) {
                System.out.print(a.id + ",");
            }
            System.out.println("min,max");
            for (Channel a : cs.streams) {
                System.out.print(a.id+",");
                int max = -1;
                String maxId ="";
                int min = 999999;
                String minId="";
                
                for (Channel b : cs.streams) {
                    
                    int dis = a.getSAXDistanceTo(b, 100, 15);
                    if (dis < min && dis != 0) {
                        min = dis; minId= b.id;
                    } 
                    if (dis >max) {
                        max = dis;
                        maxId = b.id;
                    }
                    System.out.print(dis +",");
                }
                System.out.println(maxId+"-"+max+","+minId+"-"+min);
            }
        }
    }
    
    private static void leftRightML() throws Exception{
        int[] requested = {1,2,3,4,7};
        ArrayList<String> filesRight = IDExperiment.getSpecific(requested);
        ArrayList<String> filesLeft = IDExperiment.getLeftFiles(true);
        ArrayList<String> values = new ArrayList();values.add("LEFT"); values.add("RIGHT");
        
        Classification c = new Classification(values, "handedness");
        Experiment all = new Experiment("all", c, 11f);

        for (String s : filesRight) {
            ChannelSet cs = getChannelSet(s);     
            //.. Make experiment four instances. But then coax it to have ad different classificaoitn
            Experiment e = cs.splitByLabel("filenum"); 
            e.changeClassificationToAll(c, "RIGHT"); 
            all.addExperiment(e);
        } 
        
        for (String s : filesLeft) {
            ChannelSet cs = getChannelSet(s);
            //.. Make experiment four instances. But then coax it to have ad different classificaoitn
            Experiment e = cs.splitByLabel("filenum");
            e.changeClassificationToAll(c, "LEFT");
            
            all.addExperiment(e);
        }
        
        TechniqueSet ts = EvaluationInterface.getTechniqueSetID();
        Dataset ds = new Dataset("Bajshora");
        all.evaluate(ts, ds, -1);
        System.out.println(ts.getAverage());
    }
    
    private static void lagAnalysis() {
        double[] LAGS = new double[100];
        for (int i = 0; i < LAGS.length; i++) {
            LAGS[i] = 0;
        }

        int[] requested = {1, 2};
        ArrayList<String> files = IDExperiment.getSpecific(requested);// IDExperiment.getLeftFiles(false);
        for (String s : files) {
            try {
                ChannelSet cs = getChannelSet(s);
                ArrayList<ChannelSet> subsets = cs.partitionByLabel("filenum");
                int index = 0;
                System.out.println("xxxxxxxxxxxxxxxx");
                System.out.println(s);
                System.out.println("xxxxxxxxxxxxxxxx");
                for (ChannelSet c : subsets) {
                    System.out.println("NOW " + index);
                    System.out.println("-------------------");
                    estimateLags(c, LAGS);
                    index++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        System.out.println("Based on " + obs);
        for (int i = 0; i < LAGS.length; i++) {
            if (LAGS[i] > 0) {
                System.out.println((i) + " , " + (LAGS[i] / obs));
            }
        }
    }
    
    private static void estimateLags(ChannelSet cs, double [] LAGS) throws Exception {        
        int lagIncrease = 5;
        int seed = 4;
        Channel a = cs.getChannel(seed);
        for (int i = 0; i < LAGS.length; i+=lagIncrease) {
            for (int j = 0; j < 52; j++) {
                Channel b = cs.getChannel(j);
                double granger =  a.granger(b, i);
                LAGS[i]+=granger;
                obs++;
                
            }
        }
        
    }
    private static void granger(ChannelSet cs, int LAG) throws Exception {
        for (int i = 0; i < 52; i++) {
            Channel a = cs.getChannel(i);
            for (int j = 0; j < 52; j++) { 
                Channel b = cs.getChannel(j);
                
                double granger = a.granger(b, LAG);
                System.out.println(i + " , " + j + " : " + granger);

            }
        }
        
        
    }
    private static ChannelSet getChannelSet(String s)  throws Exception{
        TSTuftsFileReader f = new TSTuftsFileReader();
        f.readEvery = 1;
        f.FRAMESIZE = 0.09;
        
        ChannelSet cs = f.readData(",", s);
        return cs;
    }
     public static ArrayList<String> getFiles() {
         ArrayList<String> files = new ArrayList(); 
         String folder = "input/IDOXY/";
         String filename = "ID-OXY-";
         int NUMFILES = 52;
         for (int i = 1; i <NUMFILES+1; i++) {
             if (i!= 6) {
                String name = folder+filename + i+".csv";
                files.add(name);
             }
         }
         return files;
         
    }
     
     public static ArrayList<String> getSpecific(int [] requested) {
         ArrayList<String> files = new ArrayList();
         String folder = "input/IDOXY/";
         String filename = "ID-OXY-";
         for (int i = 0; i < requested.length ; i++) {
            String name = folder + filename + requested[i] + ".csv";
            files.add(name);
         }
         return files;
     }
     public static ArrayList<String> getLeftFiles(boolean left) {
        ArrayList<String> files = new ArrayList();
        String folder = "input/IDOXY/";
        String filename = "ID-OXY-";
        int NUMFILES = 52;
        for (int i = 1; i < NUMFILES + 1; i++) {
            if (left &&isLeft(i)){
                String name = folder + filename + i + ".csv";
                files.add(name);
            }
            if (!left & !isLeft(i)){
                String name = folder + filename + i + ".csv";
                files.add(name);
            }
        }
        return files;

    }

     
     public static boolean isLeft(int i) {
          if ( i == 22 ||i == 11 || i== 5 || i ==22 || i ==33 || i ==43) return true;
          return false;

     }
    
}
