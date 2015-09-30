/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package filereader.experiments;

import filereader.EvaluationInterface;
import static filereader.EvaluationInterface.getTechniqueSet;
import filereader.TSTuftsFileReader;
import static filereader.experiments.AJExperiment.testAllAJ;
import static filereader.experiments.HondaExperiment.getFiles;
import java.util.ArrayList;
import org.json.JSONArray;
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
            IDExperiment exp = new IDExperiment();
            ArrayList<String> files = IDExperiment.getFiles();//IDExperiment.getSpecific(requested); //// 
            for (String s : files) {
                //leftRightML();
               // channelDistances(); 
            }
             boolean [][] done = new boolean[52][52];
             for (int i = 0; i < 52; i++) {
                for (int j = 0; j < 52; j++) {
                    done[i][j] = false;
                }
             }
             int tried =0; 
             float sumOfDiff = 0.0f;
             //exp.conditionalSaxDistances(files, 24, 45);
            //exp.evaluateSingles(files);
            for (int i = 0; i < 52; i++) {
                for (int j = 0; j < 52; j++) {
                   // if (!(done[j][i])) {
                        float likelihood = exp.conditionalSaxDistances(files, i, j);
                        //sumOfDiff += likelihood;
                       // tried++;
                        //System.out.println(tried +" , " +sumOfDiff / (1.0f * tried));

                    //}
                    done[i][j] = true;
                }
            }
        }
        catch(Exception e) {e.printStackTrace();}
    }
    
    public float conditionalSaxDistances(ArrayList<String> files, int channelA, int channelB) throws Exception {
        String condition = "mark";
        int LENGTH = 5;
        int ALPHABET = 5;
        
        Channel additionAverages = new Channel(1); 
        Channel restAverages  = new Channel(1);

        for (String filename : files) {
             //.. read
            TSTuftsFileReader f = new TSTuftsFileReader();
            ChannelSet cs = f.readData(",", filename,1);
            Experiment e = cs.splitByLabel(condition);
            ArrayList<String> toKeep = new ArrayList();
            toKeep.add("addition");
            toKeep.add("rest");
            e.removeAllClassesBut(toKeep);
            
            int additionSum =0;
            int restSum =0;
            int additionObs=0;
            int restObs =0;

            for (Instance ins : e.matrixes) {
                Channel a = ins.getChannel(channelA);
                Channel b = ins.getChannel(channelB);
                a.removeFirst(10, true);

                if (ins.condition.equals("rest")) {
                    restSum+= b.getSAXDistanceTo(a, LENGTH, ALPHABET);
                    additionObs++;

                }
                
                if (ins.condition.equals("addition")) {
                    additionSum += b.getSAXDistanceTo(a, LENGTH, ALPHABET);
                    restObs++;
                }
            }
           
            float additionAvg = ((1.0f * additionSum) / (1.0f *additionObs));
            float restAvg = ((1.0f * restSum) / (1.0f * restObs));
            additionAverages.addPoint(additionAvg);
            restAverages.addPoint(restAvg);
            //System.out.println(restAvg);
        }
        
        float additionMean = (float) additionAverages.getMean();
        float restMean = (float) restAverages.getMean();
        float additionStDev = (float) additionAverages.getStdDev();
        float restStDev = (float) restAverages.getStdDev();

        //additionAverages.printStream();
        float numerator =  Math.abs(restMean - additionMean);
        float denom = (float) (restMean / Math.sqrt(restAverages.numPoints));
        
        float likelihood = (1- numerator / denom);
        //System.out.println(likelihood);
        if (likelihood < 0.05f) {
            System.out.println((channelA + 1)+ ", " + (channelB+1) + " , " + likelihood);;
        }
        if(denom ==0) return 0;
        return likelihood;

    }
    
    public void computeAllDifferences(Experiment e, int LENGTH, int ALPHABET) throws Exception{
        int numChannels = e.matrixes.get(0).streams.size();

        int[][] addition = new int[numChannels][numChannels];
        int[][] rest = new int[numChannels][numChannels];

        int restAdded = 0;
        int additionAdded = 0;
        //.. for each instance
        for (Instance ins : e.matrixes) {
            int aIndex = 0;

            //.. for each channel
            for (Channel a : ins.streams) {
                int bIndex = 0;

                for (Channel b : ins.streams) {
                    int diff = b.getSAXDistanceTo(a, LENGTH, ALPHABET);
                    if (ins.condition.equals("addition")) {
                        addition[aIndex][bIndex] += diff;
                        additionAdded++;
                    }
                    if (ins.condition.equals("rest")) {
                        rest[aIndex][bIndex] += diff;
                        restAdded++;
                    }
                    bIndex++;
                }
                aIndex++;
            }

        }

        for (int i = 0; i < numChannels; i++) {
            for (int j = 0; j < numChannels; j++) {
                System.out.println("addition:" + i + ',' + j + ": " + (1.0f * addition[i][j]) / (1.0f * additionAdded));
                System.out.println("rest" + i + ',' + j + ": " + (1.0f * rest[i][j]) / (1.0f * restAdded));
            }
        }
    }
    
    public void evaluateSingles(ArrayList<String> files) throws Exception{
      ArrayList<TechniqueSet> techniques = new ArrayList();
      String condition = "mark";
      ArrayList<String> toKeep = new ArrayList();
      toKeep.add("addition");
      toKeep.add("rest");
      techniques.add(getTechniqueSet());
      testMulti(techniques, files, condition, toKeep);
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
        f.FRAMESIZE = 0.09;
        
        ChannelSet cs = f.readData(",", s,1);
        return cs;
    }
     public static ArrayList<String> getFiles() {
         ArrayList<String> files = new ArrayList(); 
        
         String folder = "input/IDOXY/";
         String filename = "ID-OXY-";
         int NUMFILES = 52;
         for (int i = 1; i <NUMFILES; i++) {
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
         files.add(folder+"Participant4.csv");
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
