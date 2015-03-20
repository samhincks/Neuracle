/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package filereader.experiments;

import filereader.TSTuftsFileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import timeseriestufts.evaluatable.Dataset;
import timeseriestufts.evaluatable.*;
import timeseriestufts.evaluatable.AttributeSelection.ASType;
import timeseriestufts.evaluatable.performances.Performances;
import timeseriestufts.evaluatable.performances.Predictions;
import timeseriestufts.evaluation.featureextraction.SAXAttribute;
import timeseriestufts.kth.streams.bi.ChannelSet;
import timeseriestufts.kth.streams.bi.ChannelSet.Tuple;
import timeseriestufts.kth.streams.quad.MultiExperiment;
import timeseriestufts.kth.streams.tri.Experiment;

/**The new system is extremely successful, creating a 15-subject model in 5 seconds. 
 * Instead of storing tons of data at each individual point, it now only stores floats
 * in arrays. Accessing these points is the responsibility of more complex datastructures.
 * 
 * Datalayers are now parameterized classes which was a much-needed change. 
 * There is one point that was difficult and forced a slightly bad solution memory-wise.
 * Whenever a new layer is extracted that is not just a collection of some lower dimension we 
 * allocate new memory, since you cant have multiple pointers to these primitives. I think
 * this is better than having uppercase Floats. But it means when we split a channelset into
 * into distinct instances, twice the memory then what should really be needed is allocated.
 * This has its upsides too though, if we manipulate directly instances, we still have channelsets
 * in tact.
 * 
 * Things are stable and modereately clean. The next step is to remove all unused classes
 * and clean things in general -- remove unused methods and labels. 
 * Then I want to fix the notion of performance. Once this back-end is really robust, then 
 * I'm gonna bring in the DAOS, and start manipulating them with back-end test classes. 
 * Finally, I'm gonna reintegrate with the front-end, fix the basic problems, not rewrite too much
 * but organize things and band-aid the things a user can mess up, and aim to publish online. 
 * @author samhincks
 */
public class BesteExperiment {
    public static void main(String [] args) {
        try{
           //  testMusic();
           testAllMusic(); 
         //   testMulti();
         //   testRealTime();
          //  testFromDFFile();
        }
        catch(Exception e) {e.printStackTrace();}
    }
    

    public static void testRealTime() throws Exception {
        TechniqueSet ts = getMusicTS("bajs");
        ArrayList<String> files = getFiles();
        for (String filename : files) {
            Dataset ds = new Dataset(filename);
            testRealTime(ds, ts, filename);
        }
        ts.printPredictions();
        for (int i = 1; i < 15; i++) {
            printConsec(ts, i);
            
        }

    }
    
    private static void printConsec(Evaluatable e, int k) {
        Tuple<Double, Double> consec = e.getAverageAndTotalWithKConsecutive(k);
        System.out.println(k + ": average = " + consec.x + " of " + consec.y);
    }
    
    
    /**
     Evaluate how this experiment might fare in real time. Partition the dataset into
     * n pairs of stream and experiment. Stream is what you predict on and experiment is
     * what you train on n. N is length of your experiment divided by how many samples in each.
     * They must be adjacent or else the notion of stream wouldn't make sense. 
     **/
    public static void testRealTime(Dataset ds, TechniqueSet ts, String filename )  {
        try {
            //.. read
            TSTuftsFileReader f = new TSTuftsFileReader();
            f.readEvery = 1;
            f.FRAMESIZE = 0.09;
            ChannelSet cs = f.readData(",", filename);
            cs.manipulate(ts, false);
            
            
            Tuple<Experiment, ChannelSet>[] pairs = cs.getExperimentAndStreamSet(4000, "condition");
            for (Tuple<Experiment, ChannelSet> pair:  pairs){
                Experiment e = pair.x;
                e = e.removeInstancesByClass("baseline");
                WekaClassifier wc = e.train(ts);
                Predictions p = wc.testRealStream(e.classification, ts, ds, pair.y, e.minPoints(), 20, e.asAlgosApplied);
                ds.addPredictions(p);
                ts.addPredictions(p);
            }
            
            System.out.println(filename + ":: Average %CRCT: " + ds.getAverage());
            int consec =3;
            System.out.println("AVG where must be consecutive  "+consec + ": "+ ds.getAverageAndTotalWithKConsecutive(consec).x + " of " +ds.getAverageAndTotalWithKConsecutive(consec).y);
             ds.printNumInstancesOfEach("condition");
            System.out.println("xxxxxxxxxxxxxxxxxxxxxxxx");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void testAllMusic() throws Exception {
        FileWriter fw = new FileWriter("saxfeatures4zscore1000.csv");
        BufferedWriter bw = new BufferedWriter(fw);
        try {
            Performances performances = new Performances();

            ArrayList<TechniqueSet> techniques = new ArrayList(); 
            techniques.add(getMinTS());

            boolean first = true;

            for (TechniqueSet ts : techniques) {
                System.out.println("Now " + ts.getFeatureSet().getConsoleString());
                for (String filename : getFiles()) {
                    Dataset ds = new Dataset(filename);
                    performances.addNewDataset(ds);
                    testMusicFile(ds,ts, filename);
                }
                performances.addNewTechniqueSet(ts);
                performances.printPerformances(bw, first);
                performances.resetDatasets();
                first = false;
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
            bw.close();
        }
    }

    private static ArrayList<TechniqueSet> getDiffFS(int numSets) throws Exception {
        ArrayList<TechniqueSet> ts = new ArrayList();
        int maxLength =12;
        
        for (int i =0; i < numSets; i++) {
            int length = (int) (Math.random()*maxLength);
            if (length <2) length =2;

            String randomString = SAXAttribute.getRandomString(length);
            System.out.println("eventually: " + randomString);
            TechniqueSet a = getMinTS();
            FeatureSet fs = new FeatureSet("fs");
           // fs.addFeaturesFromConsole("sax-"+randomString, "*", "*");
            fs.addFeaturesFromConsole("slope^mean","*","*");
            a.resetTechnique(fs);
            ts.add(a);
        }
        return ts;
    }
    public static TechniqueSet getMinTS() throws Exception {

        TechniqueSet ts = new TechniqueSet("Nothing");

        //.. add ML, featureSet
        ts.addTechnique(new WekaClassifier(WekaClassifier.MLType.smo));
        FeatureSet fs = new FeatureSet("fs");
        fs.addFeaturesFromConsole("slope^mean^absmean^largest^smallest^sax-cmkfd^sax-lcbom", "*", "*");

        ts.addTechnique(fs);
        ts.addTechnique(new AttributeSelection(AttributeSelection.ASType.none, 150));
        ts.addTechnique(new Transformation(Transformation.TransformationType.None));
        ts.addTechnique(new PassFilter());

        return ts;
    }
    public static void testMusic() throws Exception {
        TechniqueSet ts = getMusicTS("bajs");
        ArrayList<String> files = getFiles();
         PrintStream out = System.out;
        System.setOut(new PrintStream(new OutputStream() {  @Override public void write(int arg0) throws IOException { }}));
        
        
        for(String filename : files ){
            Dataset ds = new Dataset(filename);
            testMusicFile(ds, ts, filename);
        
        }
                System.setOut(out);

        System.out.println("Average = " + ts.getAverage());
    }
    
    
    //.. NOTE no features here
    private static TechniqueSet getMusicTS(String id) throws Exception{
        TechniqueSet ts = new TechniqueSet(id);

        //.. add ML, featureSet
        ts.addTechnique(new WekaClassifier(WekaClassifier.MLType.smo));
        FeatureSet fs = new FeatureSet("fs");
        //fs.addFeaturesFromConsole("slope^mean^absmean^largest^smallest^sax-cmkfd^sax-lcbom", "*", "*");

        fs.addFeaturesFromConsole("mean", "*", "*");
        ts.addTechnique(fs);
        ts.addTechnique(new AttributeSelection(ASType.none, 150));
        ts.addTechnique(new PassFilter(PassFilter.FilterType.LowPass, 1));
        ts.addTechnique(new Transformation(Transformation.TransformationType.None));
        
        return ts;

    }
    public static void testMusicFile(Dataset ds, TechniqueSet ts, String filename ) {
         try {
             //.. read
            TSTuftsFileReader f = new TSTuftsFileReader();
            f.readEvery = 1;
            f.FRAMESIZE= 0.09;
            ChannelSet cs = f.readData(",", filename);
            cs.manipulate(ts, false);
            //cs.printStream();
           
            //.. split
            Experiment e = cs.splitByLabel("condition");
            ArrayList<String> toKeep = new ArrayList();
            toKeep.add("easy"); toKeep.add("hard");
            e = e.removeAllClassesBut(toKeep);
           // e.printStream();
           // e2 = e2.anchorToZero(false);
            
            //.. evaluate
            e.evaluate(ts, ds,-1);
            System.out.println(filename + ":: Average %CRCT: "+ds.getAverage()); 
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
   
    public static void testMulti() {
        try {
            MultiExperiment multi = new MultiExperiment();
            TechniqueSet ts = getMusicTS("a");
            for (String file : getFiles()) {
                Experiment e = getExperiment(file);
                multi.addExperiment(e);
            }

            ArrayList<String> toKeep = new ArrayList();
            toKeep.add("easy");
            toKeep.add("hard");

            multi.removeAllClassesBut(toKeep);

            multi.evaluateX(ts);
            System.out.println(ts.getId() + ":: Average %CRCT: " + ts.getAverage());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Experiment getExperiment(String file) throws Exception {
        TSTuftsFileReader f = new TSTuftsFileReader();
        f.readEvery = 10;
        ChannelSet cs = f.readData(",", file);
        Experiment a = cs.splitByLabel("condition");
        Dataset DSa = new Dataset(file);
        a.setDataset(DSa);

        return a;
    }

    public static ArrayList<String> getFiles() {
        ArrayList<String> files = new ArrayList();
        files.add("input/bestemusic/bestemusic01.csv");
        files.add("input/bestemusic/bestemusic02.csv");
        files.add("input/bestemusic/bestemusic03.csv");
        files.add("input/bestemusic/bestemusic04.csv");
        files.add("input/bestemusic/bestemusic05.csv");
        files.add("input/bestemusic/bestemusic06.csv");
        files.add("input/bestemusic/bestemusic07.csv");
        files.add("input/bestemusic/bestemusic08.csv");
       // files.add("input/bestemeusic/bestemusic09.csv");
        files.add("input/bestemusic/bestemusic10.csv");
        files.add("input/bestemusic/bestemusic11.csv");
        files.add("input/bestemusic/bestemusic12.csv");
        files.add("input/bestemusic/bestemusic13.csv");
        files.add("input/bestemusic/bestemusic14.csv");
        files.add("input/bestemusic/bestemusic15.csv");
        System.out.println("From Beste : " + files.size());
        return files;
    }
    
    public static ArrayList<String> getBestCross() {
        ArrayList<String> files = new ArrayList();
        files.add("input/bestemusic/bestemusic02.csv");
        files.add("input/bestemusic/bestemusic04.csv");
        files.add("input/bestemusic/bestemusic05.csv");
        files.add("input/bestemusic/bestemusic06.csv");
        files.add("input/bestemusic/bestemusic11.csv");
        files.add("input/bestemusic/bestemusic12.csv");
        files.add("input/bestemusic/bestemusic13.csv");
        files.add("input/bestemusic/bestemusic14.csv");
        files.add("input/bestemusic/bestemusic15.csv");
        return files;
    }
    
   
}
