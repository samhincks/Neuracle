/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeseriestufts.evaluatable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.math3.util.Pair;
import timeseriestufts.evaluatable.FeatureDescription.FSDataLayer;
import timeseriestufts.evaluatable.FeatureDescription.FSTimeWindow;
import timeseriestufts.evaluatable.FeatureDescription.Statistic;
import timeseriestufts.evaluation.featureextraction.Attribute;
import timeseriestufts.evaluation.featureextraction.Attributes;
import timeseriestufts.evaluation.featureextraction.FeatureFactory;
import timeseriestufts.kth.streams.bi.Instance;
import timeseriestufts.kth.streams.tri.Experiment;

/**Holds all features that we want to extract in this particular setup
 */
public class FeatureSet extends Technique{
    public HashMap<String, FeatureDescription> featureDescriptions;
    public HashMap<String, Double> infogain; //.. info gain of all attributes
    public int infogainsAdded = 0; //.. increment each time we add new
    private String consoleString = null;
    
    //.. DEFAULT, presume we add everything 
    public FeatureSet(String id)  {       
        this.id = id;
    }
    
    /**Add the instances in another experiment to this infogain. If it already exists,
     then average with existing.  **/
    public void addExperimentToInfogain(weka.core.Instances instances) throws Exception {
       HashMap<String, Double> infogaintemp = new HashMap();
        
        for (Map.Entry<weka.core.Attribute, Double> m : AttributeSelection.infoGainRanker(instances).entrySet()) {
            weka.core.Attribute attr = m.getKey();
            infogaintemp.put(attr.name(), m.getValue());
        }
        
        if (infogain ==null) infogain = infogaintemp; 
        else { 
            for (Map.Entry<String, Double> m : infogaintemp.entrySet()) {
                String id = m.getKey();
                
                //.. MAKE SURE THIS ATTRIBUTE ALREADY EXISTS OTHERWISE ITS AN ERROR
                if (!(infogain.containsKey(id))) throw new Exception ("Attemping to merge infogains, "
                        + "but attribute names don't match up." + id + " doesn't exist");
                
                infogain.put(id, (m.getValue() + infogain.get(id))); //.. replace with average 
            }
        }
        infogainsAdded++;
    }
    

    public void printInfoGain() {
        for (Map.Entry<String, Double> m : infogain.entrySet()) {
            String id = m.getKey();
            Double val = m.getValue();
            System.out.println(id + " : " + val);
        }
    }
   
    
    /**Given an array of statistics, datalayer-pointer and window-directives,
     * produce every single attribute. 
     * Notice that the actual number of attributes cannot be calculated by the size of 
     * attributes since some attributes will be determined based on how many channels there are.
     * These are a special type of multiplicative attribute, which multiply when extracted
     */
    public void addFeatureDescriptions(ArrayList<Statistic> stats , ArrayList<FSDataLayer> layers,
            ArrayList<FSTimeWindow> windows) {
       
        if (featureDescriptions == null) featureDescriptions = new HashMap();
        
        for(Statistic s: stats) {
            for (FSDataLayer l : layers) {
                for(FSTimeWindow w : windows) {
                    FeatureDescription fd = new FeatureDescription(s,w,l);
                    featureDescriptions.put(fd.id, fd);
                }
            }
        }
    }
    
    /**Return a String of each feature description
     **/
    public String getFeatureDescriptionString() {
        String retString ="";
        if (featureDescriptions ==null) return "";
        for (FeatureDescription fd : featureDescriptions.values()) {
            retString += fd.getId() +",";
        }
        
        return retString;
    }
    
    /**Get console string if it is set; otherwise return feature description string*/
    public String getConsoleString() {
        if (consoleString!= null) return consoleString;
        else return this.getFeatureDescriptionString();
    }
    /**-------------
     LOGIC FOR PARSING COMMANDS FROM CONSOLE
     ---------------*/
    
    /**Channel: 
        *  '*' means all
        * 1:23 means 1 through 23 if name of channel is listed by index 
        * 1^23 means 1 and 23
        * 1+23 means merge channel 1 23
     * Stat:
        *  '*' means all
        *   mean^stddev^slope means mean and standard deviation and slope
     * Window: 
        * '*' means WHOLE FIRSTHALF, SECONDHALF
        * 2:23 means timestamp 2 through 23
        * WHOLE, FIRSTHALF or SECONDHALF 
     */
    public void addFeaturesFromConsole(String stat, String channel, String window) throws Exception{
        if (predictions!=null) throw new Exception ("Cannot add more features to this Feature Set since it has already been in evaluated in " + predictions.size() + " tests");
        ArrayList<Statistic> stats = parseStatString(stat);
        ArrayList<FSDataLayer> layers = parseDataLayers(channel); //..note, just because array is lenght one does not mean it refers to one channel
        ArrayList<FSTimeWindow> windows = parseWindows(window); //..note, just because array is lenght one does not mean it refers to one channel
        addFeatureDescriptions(stats, layers, windows);
        consoleString = stat +"-"+channel+"-"+window;
    }
    
    
    /**Parse window component*/
    public ArrayList<FSTimeWindow> parseWindows(String window)  throws Exception{
         ArrayList<FSTimeWindow> windows = new ArrayList();
         
         String [] windowVals = window.split("\\^");
         for (String windowVal : windowVals) {
            //.. add all windows
            if (windowVal.equals("*")) {
                FSTimeWindow FSWindow = new FSTimeWindow(FSTimeWindow.Timewindow.FIRSTHALF);
                windows.add(FSWindow);
                FSWindow = new FSTimeWindow(FSTimeWindow.Timewindow.SECONDHALF);
                windows.add(FSWindow);
                FSWindow = new FSTimeWindow(FSTimeWindow.Timewindow.WHOLE);
                windows.add(FSWindow);
            }

            //.. specified indexes
            else if (windowVal.contains(":")) {
                String[] values = windowVal.split(":");
                if (values.length != 2) 
                    throw new Exception("Found more than 2 values referenced between : when interpreting "
                            + " the datalayer parameter");
                int start;
                int end;
                try {
                    start = Integer.parseInt(values[0]);
                    end = Integer.parseInt(values[1]);
                }
                catch(NumberFormatException n) {
                    throw new Exception(values[0]+ " or "+ values[1] +" does not appear to be a number and therefore"
                    + " cannot be interpreted as a correct timestamp");
                }

                FSTimeWindow FSWindow = new FSTimeWindow(start, end);
                windows.add(FSWindow);

            }
            else if(windowVal.contains("%")) {
                String[] values = windowVal.split("%");
                if (values.length != 2) 
                    throw new Exception("Found more than 2 values referenced between : when interpreting "
                            + " the datalayer parameter");

                double start;
                double end;
                start = Double.parseDouble(values[0]);
                end = Double.parseDouble(values[1]);

                if (start >= end || end > 1 || start < 0) throw new Exception("Invalid input for percentage segments. " + start + " , " + end);

                FSTimeWindow FSWindow = new FSTimeWindow(start, end);
                windows.add(FSWindow);
            }

            //.. a specific window
            else {
                FSTimeWindow FSWindow = new FSTimeWindow(FSTimeWindow.getTimewindow(windowVal));
                windows.add(FSWindow);
            }
         }
         
         return windows;
    }
    
    
    /*** Deduce the set of channels referenced to. */
     private ArrayList<FSDataLayer> parseDataLayers(String channel) throws Exception {
         ArrayList<FSDataLayer> layers = new ArrayList();
         
         if (channel.equals("*")) {
             FSDataLayer fsDataLayer = new FSDataLayer("ALL");
             layers.add(fsDataLayer);
         }
         
         //.. 3:12 means channel 3 through 12. 
         else if (channel.contains(":")){
             String [] values = channel.split(":");
             if (values.length!=2) 
                 throw new Exception("Found more than 2 values referenced between : when interpreting "
                         + " the datalayer parameter");
             FSDataLayer fsDataLayer = new FSDataLayer(values[0], values[1],FSDataLayer.Type.PAIR);
             layers.add(fsDataLayer);
         }
         
         //.. 2^4^7^8
         else if (channel.contains("^")) {
              String [] values = channel.split("\\^");
              
              for (String chan : values) {
                FSDataLayer fsDataLayer = new FSDataLayer(chan);
                layers.add(fsDataLayer);
              }
         }
         
         //.. 2+3+4. Merge these channels
         else if (channel.contains("+")) {
             String [] values = channel.split("\\+");
              
             FSDataLayer fsDataLayer = new FSDataLayer(values);
             layers.add(fsDataLayer);
         }
         //.. just a singular channel
         else {
             FSDataLayer fsDataLayer = new FSDataLayer(channel);
             layers.add(fsDataLayer);
         }
         
         return layers;
         
         
     }
    
    /**Parse Stat part*/
    private ArrayList<Statistic> parseStatString(String stat) throws Exception{
        ArrayList<Statistic> stats = new ArrayList();
        
        if(stat.equals("*")) {
            for (Statistic.Stat t : Statistic.Stat.values()) {
                Statistic statistic;
                System.out.println("FeatureSet.parseStatString(): " +t);
                
                //.. exclude the stats that cannot be simply instantiated
                if((t == Statistic.Stat.sax  ))
                     statistic = new Statistic(t,2,3);
                else if (t ==Statistic.Stat.saxdist)
                    statistic = new Statistic(t, 20, 3, "ata");
                else if (t ==Statistic.Stat.granger)
                    statistic = new Statistic(t, 5);
                else if (t == Statistic.Stat.saxpair)
                    statistic = new Statistic(t, 2,3);
                else
                    statistic = new Statistic(t); //.. default = alpha 3, length =2
               if (!(t == Statistic.Stat.freq))
                  stats.add(statistic);
            }
            return stats;
        }
   
        String [] betweenAnd = stat.split("\\^");
        for (String s : betweenAnd){ 
            Statistic st = Statistic.getStatFromString(s);
            stats.add(st);
        }
        
        return stats;
    }
    
    
    /**Test console input*/
    public static void main(String [] args) {
        FeatureSet fs = new FeatureSet("bajs");
        try{
            Instance instance = Instance.generate(1,10);
            Experiment exp = Experiment.generate(2, 3, 10);
           
            instance.printStream();
            // instance.printStream();
            fs.addFeaturesFromConsole("mean", "0", "0:2^2:4");
            FeatureFactory ff = new FeatureFactory(instance, fs);
            ff.generateAttributes();
            ff.attributes.extract();
            ff.attributes.printAttributes();
            
        }
        catch(Exception e) {e.printStackTrace();}
    }

    
    
  
    
    
}
