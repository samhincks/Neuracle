    /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeseriestufts.kth.streams;

import java.util.ArrayList;
import timeseriestufts.*;
import java.util.HashMap;
import java.util.Map;

/** Abstract class for a collection of Points
 * Implemented by UnidimensionalLayer, BidimensionalLayer, and TridimensionalLayer. 
 * 
 * All streams have: 
 * 1) Basic stats method: count, sum, mean, median, stddev, stderr, min, max, mode
 * 2) Write and storage methods
 * 3) Read methods
 * @author Sam Hincks
 */
public abstract class DataLayer {
    public int trial; //.. trial num 

    public abstract int getCount();
    public abstract int getChannelCount(); //.. number of channels (obv. 1 for uni)
    public abstract double getSum() throws Exception;
    public abstract double getMean() throws Exception;
    public abstract double getMedian() throws Exception;
    public abstract double getStdDev() throws Exception;    
    public abstract double getStdErr() throws Exception;
    public abstract double getMin() throws Exception;    
    public abstract double getMax() throws Exception;
    public abstract double getMode() throws Exception;
    public abstract void printStream();
    
     /**Print basic descriptive statistics.
     */
    public void printDescriptiveStats()  throws Exception{
        System.out.println("---------------------------");
        System.out.println("Printing Descriptive Statistics...");
        System.out.println("Mean: " + getMean());
        System.out.println("Mode: " + getMode());
        System.out.println("Max: " + getMax());        
        System.out.println("Min: " + getMin());               
        System.out.println("Std. dev: " + getStdDev());             
        System.out.println("Std. err: " + getStdErr());                
        System.out.println("Count: " + getCount());                        
        System.out.println("Sum: " + getSum());
        System.out.println("----------------------------");
    }
    
    public String id = "Unnamed";
    public void setId(String id) {
        this.id =id.toLowerCase();
        this.id = this.id.replace(".","-");
    }
    public String getId() {return this.id;}
    
    public String parent = "Motherless";
    public void setParent(String id) {this.parent =id;}
    public String getParent() {return this.parent;}
    
    /**Return true if the layers parent id has not been set*/
    public boolean lacksParent() {
            if(parent.equals("Motherless")) return true;
            return false;
     }

    /**Return the size of the immediate datalayer (matrixes.size(), data.size(), etc)*/
    public abstract int getLayerSize();
    
    public Map<String, Double> statsMap;
    public void setStatsMap()  throws Exception{
        statsMap = new HashMap();
        statsMap.put("MEAN", getMean());
        statsMap.put("MODE", getMode());
        statsMap.put("MEDIAN", getMedian());
        statsMap.put("MAX", getMax());
        statsMap.put("MIN", getMin());
        statsMap.put("STD.DEV", getStdDev());
        statsMap.put("STD.ERR", getStdErr());
        statsMap.put("COUNT", (double)getCount());
        statsMap.put("SUM", getSum());
    }
    
    public abstract void delete();

    

}
