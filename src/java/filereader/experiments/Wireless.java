/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package filereader.experiments;

import filereader.EvaluationInterface;
import static filereader.EvaluationInterface.getTechniqueSet;
import filereader.TSTuftsFileReader;
import java.util.ArrayList;
import java.util.Enumeration;
import timeseriestufts.evaluatable.Dataset;
import timeseriestufts.evaluatable.TechniqueSet;
import timeseriestufts.evaluatable.Transformation;
import timeseriestufts.kth.streams.bi.ChannelSet;
import timeseriestufts.kth.streams.tri.Experiment;

/**
 *
 * @author samhincks
 */
    /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author samhincks
 */
public class Wireless extends EvaluationInterface{
   
    public static void main(String[] args) {
        try {
            Wireless w = new Wireless();
            //nr.evaluateSingles();
            w.testPreprocess();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String condition = "Condition";
    public ArrayList<String> toKeep = new ArrayList();

    public Wireless() {
    }
  
    
    public void testPreprocess() throws Exception {
         try {
            //.. read
            TSTuftsFileReader f = new TSTuftsFileReader();
            String filename = "input/ThousandMedMul.csv";
            f.FRAMESIZE= frameSize;
            ChannelSet cs = f.readData(",", filename,1);
            System.out.println(cs.getChannel(1).getFrequencyDomain().getPulse());
            Experiment e = cs.splitByLabel("Condition");
            System.out.println(e.matrixes.size());
            TechniqueSet ts = TechniqueSet.generate(); 
            Dataset ds = new Dataset("bajs");
            e.evaluate(ts, ds, -1);
            e.printStream();
            System.out.println(filename + ":: Average %CRCT: " + ds.getAverage()); 
           
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    
    }
    


}
