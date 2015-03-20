/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package filereader.experiments;

import filereader.TSTuftsFileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import timeseriestufts.evaluatable.AttributeSelection;
import timeseriestufts.evaluatable.Dataset;
import timeseriestufts.evaluatable.FeatureSet;
import timeseriestufts.evaluatable.PassFilter;
import timeseriestufts.evaluatable.TechniqueSet;
import timeseriestufts.evaluatable.Transformation;
import timeseriestufts.evaluatable.WekaClassifier;
import timeseriestufts.evaluatable.performances.Performances;
import timeseriestufts.kth.streams.bi.ChannelSet;
import timeseriestufts.kth.streams.quad.MultiExperiment;
import timeseriestufts.kth.streams.tri.Experiment;
import timeseriestufts.kth.streams.tri.TridimensionalLayer;

/**
 * 
 * @author samhincks
 */ 
public class MarkExperiment {
    
    
    public static void main(String [] args) {
        try{
            testFiles();
            //testCrossSubj();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        
    }
    
      public static void testCrossSubj() {
        try {
                TechniqueSet ts = getMusicTS("test");

                MultiExperiment multi = new MultiExperiment();

                for (String file : getMarkFiles()) {
                    Experiment e = getExperiment(file, ts);
                    multi.addExperiment(e);
                }
                
                ArrayList<String> toKeep = new ArrayList();
                toKeep.add("3missile");
                toKeep.add("6missile");

                multi.removeAllClassesBut(toKeep);

                multi.evaluateX(ts);
                for (TridimensionalLayer l : multi.piles) {
                    Experiment e = (Experiment) l;
                    System.out.println("When " + e.getDataSet().getId() + "left out, " + "%avg = " + e.getDataSet().getAverage());
                }
                System.out.println(ts.getId() + ":: Average %CRCT: " + ts.getAverage());
                
            

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    public static Experiment getExperiment(String file, TechniqueSet ts) throws Exception {
        TSTuftsFileReader f = new TSTuftsFileReader();
        f.readEvery = 1;
        ChannelSet cs = f.readData(",", file);
        cs.manipulate(ts, false);
        Experiment a = cs.splitByLabel("Difficulty");
        Dataset DSa = new Dataset(file);
        a.setDataset(DSa);

        return a;
    }
    
    
    public static void testFiles() throws Exception{
        
        FileWriter fw = new FileWriter("marktest.csv");
        BufferedWriter bw = new BufferedWriter(fw);
        try {
            
        
            Performances performances = new Performances();

            ArrayList<TechniqueSet> techniques = new ArrayList();
            techniques.add(getMusicTS("test"));

            boolean first = true;

            for (TechniqueSet ts : techniques) {
                System.out.println("Now " + ts.getFeatureSet().getConsoleString());
                for (String filename : getMarkFiles()) {
                    Dataset ds = new Dataset(filename);
                    performances.addNewDataset(ds);
                    testMarkFile(ds,ts, filename);
                }
                performances.addNewTechniqueSet(ts);
                //performances.printPerformances(bw, first);
                performances.resetDatasets();
                System.out.println("AVERAGE : " +ts.getAverage());
                System.out.println("-----------------------------");
                System.out.println("-----------------------------");

                first = false;
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
            bw.close();
        }
    }
    
   
    
    private static TechniqueSet getMusicTS(String id) throws Exception{
        TechniqueSet ts = new TechniqueSet(id);

        //.. add ML, featureSet
        ts.addTechnique(new WekaClassifier(WekaClassifier.MLType.smo));
        FeatureSet fs = new FeatureSet("fs");
        fs.addFeaturesFromConsole("slope^mean^absmean^largest^smallest", "*", "*");

        //fs.addFeaturesFromConsole("slope", "*", "*");
        ts.addTechnique(fs);
        ts.addTechnique(new AttributeSelection(AttributeSelection.ASType.none, 150));
        ts.addTechnique(new PassFilter(PassFilter.FilterType.LowPass,0.1));
        ts.addTechnique(new Transformation(Transformation.TransformationType.ZScore));
        
        return ts;
    }
    
    public static void testMarkFile(Dataset ds, TechniqueSet ts, String filename ) {
         try {
             //.. read
            TSTuftsFileReader f = new TSTuftsFileReader();
            f.readEvery = 1;
            f.FRAMESIZE= 0.09;
            ChannelSet cs = f.readData(",", filename);
            cs.manipulate(ts, false);
            //cs.printStream();
           
            //.. split
            Experiment e = cs.splitByLabel("QuestvsState10");
            //Experiment e = cs.splitByLabel("QuestvsState20");
            ArrayList<String> toKeep = new ArrayList();
             toKeep.add("question");toKeep.add("statement");
            //toKeep.add("alert");toKeep.add("not_alert");

            e = e.removeAllClassesBut(toKeep); 
          //  e.printStream();
          // e = e.anchorToZero(false);
            
            //.. evaluate
            e.evaluate(ts, ds,-1);
              
             //ts.printPredictions();

            System.out.println(filename + ":: Average %CRCT: "+ds.getAverage()); 
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public static ArrayList<String> getMarkFiles() {
        String folder = "input/FullMissile_n23_run2/";
        String filename = "FullMissile_";
        ArrayList<String> files = new ArrayList();
        
        for (int i =3; i < 26; i++) {
            String name = folder + filename +i+".csv";
            //if (i == 6 ) files.add(name);
            //if (i != 6 && i !=19 && i!=20)
                files.add(name);
        }

        return files;
    }
    
}
