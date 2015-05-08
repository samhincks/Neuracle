/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package filereader;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import timeseriestufts.evaluatable.AttributeSelection;
import timeseriestufts.evaluatable.Dataset;
import timeseriestufts.evaluatable.FeatureDescription;
import timeseriestufts.evaluatable.FeatureDescription.FSTimeWindow;
import timeseriestufts.evaluatable.FeatureDescription.Statistic;
import timeseriestufts.evaluatable.FeatureSet;
import timeseriestufts.evaluatable.PassFilter;
import timeseriestufts.evaluatable.Technique;
import timeseriestufts.evaluatable.TechniqueSet;
import timeseriestufts.evaluatable.Transformation;
import timeseriestufts.evaluatable.WekaClassifier;
import timeseriestufts.evaluatable.performances.Performances;
import timeseriestufts.evaluation.featureextraction.SAXAttribute;
import timeseriestufts.kth.streams.bi.ChannelSet;
import timeseriestufts.kth.streams.quad.MultiExperiment;
import timeseriestufts.kth.streams.tri.Experiment;
import timeseriestufts.kth.streams.tri.TridimensionalLayer;

/**
 *
 * @author samhincks
 */
public class EvaluationInterface {
    
    public static void main(String []args) {
        Aggregated.main(args);
    }
    
    public float frameSize =0.0848f;
    public int readEvery =1; //.. make larger if we want to make it faster at the expense of precision
    public String outputFile = "t3.csv";
    
    public void testMulti(ArrayList<TechniqueSet> techniques, ArrayList<String> files, String condition, ArrayList<String> keep) throws Exception {
        FileWriter fw = new FileWriter(outputFile);
        BufferedWriter bw = new BufferedWriter(fw);
        
        try {
            Performances performances = new Performances();

            boolean first = true;

            for (TechniqueSet ts : techniques) {
                System.out.println("Now " + ts.getFeatureSet().getConsoleString());
                for (String filename : files) {
                    Dataset ds = new Dataset(filename);
                    performances.addNewDataset(ds);
                    testFile(ds,ts, filename, condition, keep);
                }
                performances.addNewTechniqueSet(ts);
                performances.printPerformances(bw, first);
                performances.resetDatasets();
                first = false;
                  System.out.println("AVERAGE : " +ts.getAverage());
              System.out.println("-----------------------");
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
            bw.close();
        }
    }
   
     public void testFile(Dataset ds, TechniqueSet ts, String filename, String condition, ArrayList<String> keep ) {
         try {
            //.. read
            TSTuftsFileReader f = new TSTuftsFileReader();
            if (filename.contains("back")) frameSize = 0.16f;
            f.FRAMESIZE= frameSize;
            ChannelSet cs = f.readData(",", filename,1);
            //  ChannelSet baseline = f.readData(",", "input/UAV_processed/4.csv");

            //cs.detrend(baseline, 1.2, false);

            cs.manipulate(ts, false);
            //cs.printStream();
           
            //.. split
            Experiment e = cs.splitByLabel(condition);
            //System.out.println(filename + " " +e.matrixes.size());
            
            ArrayList<String> baseandkeep = (ArrayList<String>) keep.clone();
            baseandkeep.add("baseline");
            e = e.removeAllClassesBut(baseandkeep);
            
            e = e.removeUnfitInstances(334, 0.1, false);
            
           // e.detrend(false);
            
            e = e.removeAllClassesBut(keep);
           //  System.out.println(filename + " " +e.matrixes.size());
            if (ts.getTransformation().type == Transformation.TransformationType.anchor)
                 e = e.manipulate(new Transformation(Transformation.TransformationType.anchor), false);
            
           // e.printStream();
            Enumeration ene = e.getAmountOfEachCondition().elements();
            while(ene.hasMoreElements()){ System.out.print(ene.nextElement().toString()+" , ");}
            //.. evaluate
            e.evaluate(ts, ds,-1);
           // ds.printPredictions();
            System.out.println(filename + ":: Average %CRCT: "+ds.getAverage()); 
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
     public  void testCross(ArrayList<TechniqueSet> techniques, ArrayList<String> files, String condition, ArrayList<String> keep) throws IOException {
        FileWriter fw = new FileWriter(outputFile);
        BufferedWriter bw = new BufferedWriter(fw);
        try{   
            Performances performances = new Performances();
            boolean first = true;

            for (TechniqueSet ts : techniques) {
        
                MultiExperiment multi = new MultiExperiment();

                for (String file : files) {
                    Experiment e = getExperiment(file, condition);
                    multi.addExperiment(e);
                }
                
                ArrayList<String> toKeep = new ArrayList();
                toKeep.add("easy");
                toKeep.add("hard");

                multi.removeAllClassesBut(toKeep);

                multi.evaluateX(ts);
                for (TridimensionalLayer l : multi.piles) {
                    Experiment e = (Experiment) l;
                    System.out.println("When " + e.getDataSet().getId() + "left out, " + "%avg = " + e.getDataSet().getAverage());
                }
                System.out.println(ts.getId() + ":: Average %CRCT: " + ts.getAverage());
                
                performances.addNewTechniqueSet(ts);
                performances.printPerformances(bw, first);
                performances.resetDatasets();
                first = false;
            }
            
             bw.close();

        } catch (Exception e) {
             bw.close();
            e.printStackTrace();
        }
    }
     
    public static Experiment getExperiment(String file, String condition) throws Exception {
        TSTuftsFileReader f = new TSTuftsFileReader();
        ChannelSet cs = f.readData(",", file, 1);
        Experiment a = cs.splitByLabel(condition);
        Dataset DSa = new Dataset(file);
        a.setDataset(DSa);

        return a;
    }
     
    public static TechniqueSet getTechniqueSet() throws Exception {

        TechniqueSet ts = new TechniqueSet("Default");

        //.. add ML, featureSet
        ts.addTechnique(new WekaClassifier(WekaClassifier.MLType.smo));
        FeatureSet fs = new FeatureSet("fs");
      //  fs.addFeaturesFromConsole("sax-kq^sax-ogj^sax-hn^sax-hq^sax-ac^sax-fk^sax-kq^sax-is^sax-hm", "*", "*");
        fs.addFeaturesFromConsole("*", "*", "*");
        /*String features ="";
        for (int i=0 ;i < 256; i++) {
            features += "freq-"+i;
            if (i!= 255) features += "^";
        }
        fs.addFeaturesFromConsole(features, "*", "*");*/

        ts.addTechnique(fs);
        ts.addTechnique(new AttributeSelection(AttributeSelection.ASType.none, 0.8f));
        ts.addTechnique(new Transformation(Transformation.TransformationType.calcoxy));
        ts.addTechnique(new PassFilter());//PassFilter.FilterType.LowPass, 0.35));

        return ts;
    }
    
    public static TechniqueSet getTechniqueSetID() throws Exception {
        TechniqueSet ts = new TechniqueSet("Default");

        //.. add ML, featureSet
        ts.addTechnique(new WekaClassifier(WekaClassifier.MLType.smo));
        FeatureSet fs = new FeatureSet("fs");
        fs.addFeaturesFromConsole("*", "*", "*"); //.. -> 57.5 with 10%, 65% with 50
        //fs.addFeaturesFromConsole("saxpair-200-5", "*", "WHOLE"); //. 57.5% with 10%

       // fs.addFeaturesFromConsole("saxpair-5-5", "1:-1", "WHOLE"); //.. note adding channels doesnt work

        ts.addTechnique(fs);
        ts.addTechnique(new AttributeSelection(AttributeSelection.ASType.info, 0.35f));
        ts.addTechnique(new Transformation(Transformation.TransformationType.none));
        ts.addTechnique(new PassFilter());//PassFilter.FilterType.LowPass, 0.35));
        return ts;

    }
    
      public static ArrayList<TechniqueSet> getTechniqueSetsByML() throws Exception {
        ArrayList<TechniqueSet> ts = new ArrayList();
        
        TechniqueSet a = getTechniqueSet();
        a.resetTechnique(new WekaClassifier(WekaClassifier.MLType.smo));
        ts.add(a);
        
         a = getTechniqueSet();
        a.resetTechnique(new WekaClassifier(WekaClassifier.MLType.nb));
        ts.add(a);
        
        a = getTechniqueSet();
        a.resetTechnique(new WekaClassifier(WekaClassifier.MLType.tnn));
        ts.add(a);
        
         a = getTechniqueSet();
        a.resetTechnique(new WekaClassifier(WekaClassifier.MLType.libsvm));
        ts.add(a);
       
        a = getTechniqueSet();
        a.resetTechnique(new WekaClassifier(WekaClassifier.MLType.simple));
        ts.add(a);
        
        a = getTechniqueSet();
        a.resetTechnique(new WekaClassifier(WekaClassifier.MLType.logistic));
        ts.add(a);
        
         a = getTechniqueSet();
        a.resetTechnique(new WekaClassifier(WekaClassifier.MLType.adaboost));
        ts.add(a);
        
         a = getTechniqueSet();
        a.resetTechnique(new WekaClassifier(WekaClassifier.MLType.lmt));
        ts.add(a);
        
        return ts;
    }
      
    public static ArrayList<TechniqueSet> getTechniqueSetsByAS(int numSets) throws Exception {
        ArrayList<TechniqueSet> ts = new ArrayList();
        float inc = 1 / (float) numSets;
        float start = inc;
        while(start <=1) {
            TechniqueSet a = getTechniqueSet();
            a.resetTechnique(new AttributeSelection(AttributeSelection.ASType.info, start));
            ts.add(a); 
            System.out.println(start);
            start+=inc;
            start = FSTimeWindow.roundTwoDecimals(start);
        }
        
        return ts;
    }
    
    public static ArrayList<TechniqueSet> getTechniqueSetsByFS() throws Exception {
        ArrayList<TechniqueSet> ts = new ArrayList();

        TechniqueSet a = getTechniqueSet();
        FeatureSet fs = new FeatureSet("fs");
        fs.addFeaturesFromConsole("slope", "*", "*");
        a.resetTechnique(fs); //. H:50%
        ts.add(a);
        /*
         a = getTechniqueSet();
         fs = new FeatureSet("fs");
        fs.addFeaturesFromConsole("slope^stddev", "*", "*");
        a.resetTechnique(fs); //.. H:36%
        ts.add(a);
        
         a = getTechniqueSet();
         fs = new FeatureSet("fs");
        fs.addFeaturesFromConsole("slope^stddev^smallest", "*", "*");
        a.resetTechnique(fs);//. H:50%
        ts.add(a);
        
         a = getTechniqueSet();
         fs = new FeatureSet("fs");
        fs.addFeaturesFromConsole("slope^stddev^smallest^t2p", "*", "*");
        a.resetTechnique(fs); //.H:47%
        ts.add(a);
        
         a = getTechniqueSet();
         fs = new FeatureSet("fs");
        fs.addFeaturesFromConsole("slope^stddev^smallest^t2p^absmean", "*", "*");
        a.resetTechnique(fs); //.H:47%
        ts.add(a);
        */
        a = getTechniqueSet();
        fs = new FeatureSet("fs");
        fs.addFeaturesFromConsole("slope^stddev^smallest^t2p^absmean^largest", "*", "*");
        a.resetTechnique(fs); //.H:47%
        ts.add(a);/*
         
        a = getTechniqueSet();
        fs = new FeatureSet("fs");
        fs.addFeaturesFromConsole("slope^stddev^smallest^t2p^absmean^largest^mean^absslope", "*", "*");
        a.resetTechnique(fs); //.H:47%
        ts.add(a);
         
        a = getTechniqueSet();
        fs = new FeatureSet("fs");
        fs.addFeaturesFromConsole("slope^stddev^smallest^t2p^absmean^largest^mean^absslope^fwhm", "*", "*");
        a.resetTechnique(fs); //.H:47%
        ts.add(a);
         
        a = getTechniqueSet();
        fs = new FeatureSet("fs");
        fs.addFeaturesFromConsole("slope^stddev^smallest^t2p^absmean^largest^mean^absslope^fwhm^secondder", "*", "*");
        a.resetTechnique(fs); //.H:47%
        ts.add(a);
        */
        return ts;
    }
    
   public static ArrayList<TechniqueSet> getTechniqueSetsByFilter(PassFilter.FilterType filter) throws Exception {
          ArrayList<TechniqueSet> ts = new ArrayList();
          TechniqueSet a = getTechniqueSet();
            a.resetTechnique(new PassFilter(filter, 0.1));
            ts.add(a);
            
           a = getTechniqueSet();
            a.resetTechnique(new PassFilter(filter, 0.09));
            ts.add(a);
            
            a = getTechniqueSet();
            a.resetTechnique(new PassFilter(filter, 0.08));
            ts.add(a);
            
            a = getTechniqueSet();
            a.resetTechnique(new PassFilter(filter, 0.07));
            ts.add(a);
            
            a = getTechniqueSet();
            a.resetTechnique(new PassFilter(filter, 0.06));
            ts.add(a);
            
            a = getTechniqueSet();
            a.resetTechnique(new PassFilter(filter, 0.05));
            ts.add(a);
            
            a = getTechniqueSet();
            a.resetTechnique(new PassFilter(filter, 0.04));
            ts.add(a);
            
            a = getTechniqueSet();
            a.resetTechnique(new PassFilter(filter, 0.03));
            ts.add(a);
            
            a = getTechniqueSet();
            a.resetTechnique(new PassFilter(filter, 0.02));
            ts.add(a);
          return ts;

   }
    
     /**Honda filtering. Lowpass 10, =54%. Lowpass, 5 = 53%, 1.25 = 49%... 
      Highpass = 10: 41%, 5: 37%, 2.5: 39%, 1.25: 41%, 0.75: 37.%
      **/ 
    public static ArrayList<TechniqueSet> getTechniqueSetsByFilter(PassFilter.FilterType filter, float highest, float lowest, int numTests) throws Exception {
        ArrayList<TechniqueSet> ts = new ArrayList();
        float decrement = (highest - lowest) / (float) numTests;
        while (highest > lowest) {
            TechniqueSet a = getTechniqueSet();
            a.resetTechnique(new PassFilter(filter, highest));
            ts.add(a);
            System.out.println(highest);
            //highest = highest /2.0;
            highest -= decrement;
        }
        
        return ts;
    }
    
    public static ArrayList<TechniqueSet> getBandPassByFilter(int order) throws Exception {
        ArrayList<TechniqueSet> ts = new ArrayList();
        TechniqueSet a = getTechniqueSet();
        a.resetTechnique(new PassFilter(order, 0.01, 0.5));
        ts.add(a);
        
        return ts;
    }
    
    public static ArrayList<TechniqueSet> getTechniqueSetsByPreprocessing() throws Exception{
        ArrayList<TechniqueSet> ts = new ArrayList();
                
         TechniqueSet a = getTechniqueSet();
          a.resetTechnique(new Transformation(Transformation.TransformationType.anchor));
          ts.add(a);
          
           a = getTechniqueSet();
          a.resetTechnique(new Transformation(Transformation.TransformationType.zscore));
          ts.add(a);
          
           a = getTechniqueSet();
          a.resetTechnique(new Transformation(Transformation.TransformationType.movingaverage, 10));
          ts.add(a);
          
            a = getTechniqueSet();
          a.resetTechnique(new Transformation(Transformation.TransformationType.movingaverage, 30));
          ts.add(a);
          
          
          a = getTechniqueSet();
          a.resetTechnique(new Transformation(Transformation.TransformationType.movingaverage, 50));
          ts.add(a);
          
          
          a = getTechniqueSet();
          a.resetTechnique(new Transformation(Transformation.TransformationType.movingaverage, 75));
          ts.add(a);
          
          
          a = getTechniqueSet();
          a.resetTechnique(new Transformation(Transformation.TransformationType.movingaverage, 100));
          ts.add(a);
          return ts;

    }
    
    /**100 took 127 minutes
     * Best were hgceekmm 52%, hlccso 49%, pbi 48%, ad 47%
     */
    public static ArrayList<TechniqueSet> getTechniqueSetsBySAX(int numSets) throws Exception {
        ArrayList<TechniqueSet> ts = new ArrayList();
        int maxLength =12;
        
        for (int i =0; i < numSets; i++) {
            int length = (int) (Math.random()*maxLength);
            if (length <2) length =2;

            String randomString = SAXAttribute.getRandomString(length);
            System.out.println("eventually: " + randomString);
            TechniqueSet a = getTechniqueSet();
            FeatureSet fs = new FeatureSet("fs");
            fs.addFeaturesFromConsole("sax-"+randomString, "*", "*");
            a.resetTechnique(fs);
            ts.add(a);
        }
        return ts;
    }
    
    public static ArrayList<TechniqueSet> getTechniqueSetsByChannel() throws Exception {
         ArrayList<TechniqueSet> ts = new ArrayList();
         int numChannels = 16;
         for (int i=0; i < numChannels; i++) {
             TechniqueSet a = getTechniqueSet();
             FeatureSet fs = new FeatureSet("fs");
             fs.addFeaturesFromConsole("*", String.valueOf(i), "*");
             a.resetTechnique(fs); 
             ts.add(a);
         }
         return ts;
    }
     
    public ArrayList<TechniqueSet> getTechniqueSetsByWindow (int length, int end) throws Exception { 
        ArrayList<TechniqueSet> ts = new ArrayList();

        int tests = (int) (end / (double)length);
        int start = 0;
        for (int i =0; i < tests; i++){
            TechniqueSet a = getTechniqueSet();
            FeatureSet fs = new FeatureSet("fs");
            int endPos = start + length;
            fs.addFeaturesFromConsole("*", "*", start+":"+endPos);
            a.resetTechnique(fs); //. H:50%
            ts.add(a);
            start+= length;
        }
        return ts;
    }
    
   
    public ArrayList<TechniqueSet> getTechniqueSetsByIndividualWindow(float mostSpecific) throws Exception{
        ArrayList<TechniqueSet> ts = new ArrayList();

        String all = FSTimeWindow.getWithIncInc(mostSpecific);
        String [] components = all.split("\\^");
        
        for(String part : components) {
            TechniqueSet a = getTechniqueSet();
            FeatureSet fs = new FeatureSet("fs");
            System.out.println(part);
            fs.addFeaturesFromConsole("*", "*",part);
            a.resetTechnique(fs); //. H:50%
            ts.add(a);
        }
        
        return ts;
    }
    
    
    public ArrayList<TechniqueSet> getTechniqueSetsByWindowInc2(int numSets) throws Exception {
        ArrayList<TechniqueSet> ts = new ArrayList();
        
        for (int i = 2; i <numSets+2; i++){
            float incOfInc = 1 / (float) i;

            TechniqueSet a = getTechniqueSet();
            FeatureSet fs = new FeatureSet("fs");
            
            fs.addFeaturesFromConsole("*", "*",FSTimeWindow.getWithIncInc(incOfInc));
            a.resetTechnique(fs); //. H:50%
            ts.add(a);
        }
        
        return ts;
    }
    
    public ArrayList<TechniqueSet> getTechniqueSetsByWindowInc(int numSets) throws Exception {
        ArrayList<TechniqueSet> ts = new ArrayList();
        float incOfInc = 1/ (float)numSets;
        float incStart = incOfInc;
        
        while (incStart < 1) {
            TechniqueSet a = getTechniqueSet();
            FeatureSet fs = new FeatureSet("fs");
            fs.addFeaturesFromConsole("*", "*", FSTimeWindow.getWithInc(incStart));
            a.resetTechnique(fs); //. H:50%
            ts.add(a);
            System.out.println(incStart);
            incStart += incOfInc;
        }
        
        return ts;
    }
    public ArrayList<TechniqueSet> getTechniqueSetsByWindow () throws Exception { 
         ArrayList<TechniqueSet> ts = new ArrayList();

        TechniqueSet a = getTechniqueSet();
        FeatureSet fs = new FeatureSet("fs");
        fs.addFeaturesFromConsole("*", "*", "0:325");
        a.resetTechnique(fs); //. H:50%
        ts.add(a);
        
        a = getTechniqueSet();
         fs = new FeatureSet("fs");
        fs.addFeaturesFromConsole("*", "*", "0:300");
        a.resetTechnique(fs); //. H:50%
        ts.add(a);
        
        
        a = getTechniqueSet();
         fs = new FeatureSet("fs");
        fs.addFeaturesFromConsole("*", "*", "0:275");
        a.resetTechnique(fs); //. H:50%
        ts.add(a);
        
        
        a = getTechniqueSet();
         fs = new FeatureSet("fs");
        fs.addFeaturesFromConsole("*", "*", "0:250");
        a.resetTechnique(fs); //. H:50%
        ts.add(a);
        
        
        a = getTechniqueSet();
         fs = new FeatureSet("fs");
        fs.addFeaturesFromConsole("*", "*", "0:225");
        a.resetTechnique(fs); //. H:50%
        ts.add(a);
        
        
        a = getTechniqueSet();
         fs = new FeatureSet("fs");
        fs.addFeaturesFromConsole("*", "*", "0:200");
        a.resetTechnique(fs); //. H:50%
        ts.add(a);
        
        a = getTechniqueSet();
         fs = new FeatureSet("fs");
        fs.addFeaturesFromConsole("*", "*", "0:175");
        a.resetTechnique(fs); //. H:50%
        ts.add(a);
        
        
        a = getTechniqueSet();
         fs = new FeatureSet("fs");
        fs.addFeaturesFromConsole("*", "*", "0:150");
        a.resetTechnique(fs); //. H:50%
        ts.add(a);
        
        
        a = getTechniqueSet();
         fs = new FeatureSet("fs");
        fs.addFeaturesFromConsole("*", "*", "0:125");
        a.resetTechnique(fs); //. H:50%
        ts.add(a);
        
        a = getTechniqueSet();
         fs = new FeatureSet("fs");
        fs.addFeaturesFromConsole("*", "*", "0:100");
        a.resetTechnique(fs); //. H:50%
        ts.add(a);
        
        a = getTechniqueSet();
         fs = new FeatureSet("fs");
        fs.addFeaturesFromConsole("*", "*", "0:75");
        a.resetTechnique(fs); //. H:50%
        ts.add(a);
        
        a = getTechniqueSet();
         fs = new FeatureSet("fs");
        fs.addFeaturesFromConsole("*", "*", "0:50");
        a.resetTechnique(fs); //. H:50%
        ts.add(a);
        
        a = getTechniqueSet();
         fs = new FeatureSet("fs");
        fs.addFeaturesFromConsole("*", "*", "0:25");
        a.resetTechnique(fs); //. H:50%
        ts.add(a);
        return ts;
    }
    
    /**Honda tests, mean: 32%, smallest 23%, largest: 28%, fwhm: 17%, : slope 43%, : absslope 28% 
     * stddev: 40%, secondder 29%, absmean, 32%
     **/
    public static ArrayList<TechniqueSet> getTechniqueSetsByFeature() throws Exception {
        ArrayList<TechniqueSet> ts = new ArrayList();

        for (Statistic.Stat stat : Statistic.Stat.values()){ 
            if(!(stat.name().equals("sax") || stat.name().equals("saxdist"))){
                TechniqueSet a = getTechniqueSet();
                FeatureSet fs = new FeatureSet("fs");
                System.out.println(stat.name());
                fs.addFeaturesFromConsole(stat.name(), "*", "*");
                a.resetTechnique(fs); //. H:50%
                ts.add(a);
            }
        }
        
        return ts;

    }
    
      
      
}
