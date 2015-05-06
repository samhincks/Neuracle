/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package filereader.experiments;

import filereader.TSTuftsFileReader;
import filereader.formatconversion.ReadExperimentFromFeatures;
import filereader.formatconversion.ReadWithMatlabMarkers;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import org.apache.commons.math3.complex.Complex;
import timeseriestufts.evaluatable.AttributeSelection;
import timeseriestufts.evaluatable.Dataset;
import timeseriestufts.evaluatable.FeatureSet;
import timeseriestufts.evaluatable.PassFilter;
import timeseriestufts.evaluatable.TechniqueSet;
import timeseriestufts.evaluatable.Transformation;
import timeseriestufts.evaluatable.WekaClassifier;
import timeseriestufts.evaluatable.performances.Performances;
import timeseriestufts.evaluation.featureextraction.SAXAttribute;
import timeseriestufts.kth.streams.bi.ChannelSet;
import timeseriestufts.kth.streams.quad.MultiExperiment;
import timeseriestufts.kth.streams.tri.Experiment;
import timeseriestufts.kth.streams.tri.TridimensionalLayer;
import timeseriestufts.kth.streams.uni.Channel;
import timeseriestufts.kth.streams.uni.FrequencyDomain;

/**
 *
 * @author samhincks
 */
public class AJExperiment {
    
    public static void main(String [] args) {
        try {
            testAllAJ();
          // testMulti();
         //   testCrossSubj();
        }
        catch(Exception e) {e.printStackTrace();}
        
    }
     
    /** ------------------------------------------------------
     * MULTI SESSIONEXPERIMENT
     * ------------------------------------------------------
     **/
    public static void testMulti() {
        try {
           // for (int i = 1; i < 8; i++) {
            for (ArrayList<String> files : getPairedSession(2)) {

                TechniqueSet ts = getMinTS();

                MultiExperiment multi = new MultiExperiment();

                //for (String file : getSession(2)) {
                for (String file : files ){
                    System.out.println(file);
                    Experiment e = getExperiment(file);
                    multi.addExperiment(e);
                }
                
                
                ArrayList<String> toKeep = new ArrayList();
                toKeep.add("easy");
                toKeep.add("hard");

                multi.removeAllClassesBut(toKeep);

                multi.evaluateX(ts);
                for (TridimensionalLayer l : multi.piles) {
                    Experiment e = (Experiment) l;
                    System.out.println("When " + e.getDataSet().getId() + " left out, " + "%avg = " + e.getDataSet().getAverage());
                }
                System.out.println(ts.getId() + ":: Average %CRCT: " + ts.getAverage());
            }
            

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
     public static void testCrossSubj() {
        try {
                TechniqueSet ts = getMinTS();

                MultiExperiment multi = new MultiExperiment();

                for (String file : getFiles()) {
                    Experiment e = getExperiment(file);
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
                
            

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Experiment getExperiment(String file) throws Exception {
        TSTuftsFileReader f = new TSTuftsFileReader();
        ChannelSet cs = f.readData(",", file,1);
        Experiment a = cs.splitByLabel("condition");
        Dataset DSa = new Dataset(file);
        a.setDataset(DSa);

        return a;
    }
    /** ------------------------------------------------------
     * TEST ALL OR INDIVIDUAL FILES
     * ------------------------------------------------------
     **/
    
     public static void testAllAJ() throws Exception {
        FileWriter fw = new FileWriter("ajtest.csv");
        BufferedWriter bw = new BufferedWriter(fw);
        try {
            Performances performances = new Performances();

            ArrayList<TechniqueSet> techniques = new ArrayList();
            techniques.add(getMinTS());

            boolean first = true;

            for (TechniqueSet ts : techniques) {
                for (String filename : getFiles()) {
                    Dataset ds = new Dataset(filename);
                    performances.addNewDataset(ds);
                    testAJFile(ds, ts, filename);
                }
                performances.addNewTechniqueSet(ts);
                performances.printPerformances(bw, first);
                System.out.println("AVERAGE-------------- " +  performances.getAverageDSStats());
                performances.resetDatasets();
                first = false;
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
            bw.close();
        }
    }
     /** ------------------------------------------------------
     * TEST INDIVIDUAL FILE
     * ------------------------------------------------------
     **/
  
    
    public static void testAJFile(Dataset ds, TechniqueSet ts, String filename ) {
         try {
             //.. read
            TSTuftsFileReader f = new TSTuftsFileReader();
            f.FRAMESIZE= 0.09;
            ChannelSet cs = f.readData(",", filename,1);
            cs.manipulate(ts, false);
           // cs.getChannel(0).printStream();
            Complex [] transformed = cs.getChannel(0).FFT();
             FrequencyDomain fd = new FrequencyDomain(11);
             fd.complexToFreq(transformed);
             
            //.. split
            Experiment e = cs.splitByLabel("condition");
            ArrayList<String> toKeep = new ArrayList();
            toKeep.add("easy"); toKeep.add("hard");
            e = e.removeAllClassesBut(toKeep);
//            e.printInfo();
           // e2 = e2.anchorToZero(false);
            
            //.. evaluate
            e.evaluate(ts, ds,-1);
            System.out.println(filename + ":: Average %CRCT: "+ds.getAverage()); 
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
 
    
    /** ------------------------------------------------------
     *  GET MULTI OR SINGLE TECHNIQUE-SET
     * ------------------------------------------------------
     **/
    
    public static TechniqueSet getMinTS() throws Exception {

        TechniqueSet ts = new TechniqueSet("Average for ts");

        //.. add ML, featureSet
        ts.addTechnique(new WekaClassifier(WekaClassifier.MLType.smo));
        FeatureSet fs = new FeatureSet("fs");
        fs.addFeaturesFromConsole("slope^mean^absmean^largest^smallest^sax-cmkfd^sax-lcbom", "*", "*");

        ts.addTechnique(fs);
        ts.addTechnique(new AttributeSelection(AttributeSelection.ASType.info, 220));
        ts.addTechnique(new Transformation(Transformation.TransformationType.none));
        ts.addTechnique(new PassFilter(PassFilter.FilterType.LowPass,0.2)); //. no filter

        return ts;
    }
    /** ------------------------------------------------------
     * GET FILE
     * ------------------------------------------------------
     **/
    
      public static ArrayList<String> getBestFiles() {
        ArrayList<String> files = new ArrayList();
        String folder = "input/ajprocessed/";
        for (int i = 1; i < 8; i++) {
            int subjNum = i;
            String[] sess = {"A", "B", "C", "D", "E"};
            for (String s : sess) {
                if (!(s.equals("E")) || (!(i == 1 || i == 4))) {
                    String output = folder + "ajms" + subjNum + s + ".csv";
                    if (i ==1 || i==2)
                    files.add(output);
                }
            }
        }

          System.out.println("From AJ: 2 subjects " + "  and " + files.size());
        return files;
    }  
       
      
      public static ArrayList<String> getCrossBestFiles() {
        ArrayList<String> files = new ArrayList();
        String folder = "input/ajprocessed/";
        for (int i = 1; i < 8; i++) {
            int subjNum = i;
            String[] sess = {"A", "B", "C", "D", "E"};
            for (String  s : sess) {
                if (!(s.equals("E")) || (!(i == 1 || i == 4))) {
                    String output = folder + "ajms" + subjNum + s + ".csv";
                    if (i ==1 || i==2){
                       if ((i ==1 && s.equals("A")) || (i ==2 && (s.equals("A") || s.equals("C") || s.equals("D") || s.equals("E"))))
                          files.add(output);
                    }
                }
            }
        }

          System.out.println("From AJ: 2 subjects " + "  and " + files.size());
        return files;
    }  
    

 
    public static ArrayList<String> getFiles() {
        ArrayList<String> files = new ArrayList();
        String folder = "input/ajprocessed/";
        for (int i = 1; i < 8; i++) {
            int subjNum = i;
            String[] sess = {"A", "B", "C", "D", "E"};
            for (String s : sess) {
                if (!(s.equals("E")) || (!(i == 1 || i == 4))) {
                    String output = folder + "ajms" + subjNum + s + ".csv";
                    files.add(output);
                }
            }
        }


        return files;
    }
    public static ArrayList<ArrayList<String>> getPairedSession(int subjId) {
        ArrayList<ArrayList<String>> files = new ArrayList();

        String[] sess = {"A", "B", "C", "D", "E"};
        for (String s : sess) {
            for (String s2 : sess) {
                if (!(s.equals(s2))) {
                    files.add(getSession(subjId, s,s2));
                }
                    
            }
        }
        return files;
    }
    
    public static ArrayList<String>  getSession(int subjId, String a, String b) {
        ArrayList<String> files = new ArrayList();
        String folder = "input/ajprocessed/";
        String[] sess = {"A", "B", "C", "D", "E"};
        String output = folder + "ajms" + subjId + a + ".csv";

        files.add(output);
         output = folder + "ajms" + subjId + b + ".csv";
        files.add(output);

        return files;
        
    }
    public static ArrayList<String>  getSession(int subjId) {
        ArrayList<String> files = new ArrayList();
        String folder = "input/ajprocessed/";
        String[] sess = {"A", "B", "C", "D", "E"};
        for (String s : sess) {
            if (!(s.equals("E")) || (!(subjId == 1 ||subjId == 4))) {
                String output = folder + "ajms" + subjId + s + ".csv";
                files.add(output);
            }
        }
        
        return files;
        
    }

    
     /** ------------------------------------------------------
     * FEATURE DEFINITION VERIFICATION
     * ------------------------------------------------------
     **/
    
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
       
    
    
    public static void testFromDFFile() {
        try {
            Performances performances = new Performances();

            for (String filename : getFiles()) {
                Dataset ds = new Dataset(filename);
                System.out.println(filename);
                performances.addNewDataset(ds);
                ReadExperimentFromFeatures rf = new ReadExperimentFromFeatures(filename);
                Experiment e = rf.getExperiment();
                e.evaluate(getMinTS(), ds, -1);
                System.out.println("av = " + ds.getAverage());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public static ArrayList<String> getAJFDFiles() {
        ArrayList<String> files = new ArrayList();
        String folder = "input/AJ/";
        for (int i = 1; i < 8; i++) {
            int subjNum = i;
            String[] sess = {"A", "B", "C", "D"};
            for (String s : sess) {
                String subfolder = "/Multisess_" + subjNum + s + "/";
                String filename = "Defined_Features_And_Values.csv";
                if (i != 5 && (!(s.equals("C")))) {
                    files.add(folder + subfolder + filename);
                }
            }
        }


        return files;
    }
    
    public static void fromMatlab() {
        String folder = "input/AJ";
        try{
            for (int i =1; i < 8; i++){
                int subjNum=i;
                String [] sess = {"A","B","C","D","E"};
                
                for (String s : sess) {
                    if (!(s.equals("E")) || (!(i==1 || i ==4))) {
                        String input=folder+"/multisess_"+subjNum+s+"/fnirsData.txt";
                        String marker=folder+"/multisess_"+subjNum +s+"/markers.txt";
                        String output = "input/ajprocessed/ajms"+subjNum+s+".csv";
                        System.out.println("Making" + output);

                        ReadWithMatlabMarkers reader = new ReadWithMatlabMarkers(marker,input);
                        ArrayList<ReadWithMatlabMarkers.Trial> trials = reader.readMarkerFileBeste();
                        reader.readFNIRSFile();
                        reader.writeToFile(output);
                        for (ReadWithMatlabMarkers.Trial t : trials) {
                         //   System.out.println("start: " + t.start + " - " + t.end + " .. "+t.label);
                        }
                        //break;
                    }
                    
                }
              //  break;
            }
        }
        catch(Exception e) {e.printStackTrace();}
    }
    public static Channel getChannel() throws Exception{
         String filename = "input/data_for_sam3/1/iaps/labeled_fNIRSdata.csv";
          // String filename = "input/iaps_hr_raw.csv";

          //.. NEXT: SEE IF ITS JUST AN OFF-BY-X ERROR, then try a different approach.
          //.... I CAN COUNT the number of oscillations. 
          TSTuftsFileReader f = new TSTuftsFileReader();
          ChannelSet cs = f.readData(",", filename ,1);
          Experiment e = cs.splitByLabel("condition");
          Channel b1 = e.matrixes.get(1).getChannel(0);
          return cs.getChannel(0); 
    }
    public static String [] getFiles(boolean fnirs){
            String filenameA = "labeled_fNIRSdata.csv";
            String filenameB = "iaps_rr_raw.csv";
            String folder = "input/data_for_sam3/";
            String [] files = new String [16];
            int i =1;
            for (int k=0; k < files.length; k+=2) {
                if(fnirs){
                    files[k] = folder+i+"/iaps/"+filenameA;
                    files[k+1] = folder + i + "/iaps/" + filenameA;

                }
                else {
                    files[k] = folder+i+"/iaps/" + filenameB;
                    files[k+1] = folder + i + "/iaps/" + filenameB;
                }
                i++;
            }
            return files;
    }
 
}
