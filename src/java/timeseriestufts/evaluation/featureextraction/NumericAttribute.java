/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeseriestufts.evaluation.featureextraction;

import org.apache.commons.math3.complex.Complex;
import timeseriestufts.evaluatable.FeatureDescription;
import timeseriestufts.evaluatable.FeatureDescription.Statistic;
import timeseriestufts.kth.streams.DataLayer;
import timeseriestufts.kth.streams.uni.Channel;
import timeseriestufts.kth.streams.uni.FrequencyDomain;
import weka.core.Attribute;

/**
 *
 * @author XPS1640
 */
public class NumericAttribute extends TSAttribute {
    
   public Channel channel;
   public Statistic stat;
   public String window;
   
   public NumericAttribute(Channel channel, Statistic stat, String window) throws Exception{
       this.channel = channel;
       this.stat = stat;
       this.window = window;
       this.setName();
   }
   
   /**The name of the attribute, must be unique for each. If two attributes have the same name,
    then they should be identical*/
   @Override
   public String setName() throws Exception{     
        type = "NUMERIC";
        name = "CHANNEL-"+channel.id+"-"+stat.stat+"-"+window+"-"; 
        if (stat.stat == Statistic.Stat.sax)
            name += stat.getAlphaLength()+"-"+stat.getNumLetters();
        else if (stat.stat == Statistic.Stat.saxdist)
            name += stat.getSaxString();
        return name;
    }

    /**TODO: Implement timeIndex.
     * Implement 1st, 2nd derivative
     * Add standard deviation, etc; get creative 
     */
    @Override
    public void extract() throws Exception{
        setName();        
        switch (stat.stat) {
            case mean :
                numValue = channel.getMean();
                break;
                
            case smallest :
                numValue = channel.getMin();
                break;
                     
            case largest :
                numValue = channel.getPeakPoint();
                break;
                     
            case slope :
                numValue =channel.getSlope();
                break;
                     
            case secondder :
                numValue = channel.getSecondDerivative();
                break;
                     
            case t2p :
                numValue = channel.getPeakIndex();
                break;
                     
            case absmean :
                numValue = Math.abs(channel.getMean());
                break;   
                
            case fwhm : 
                numValue = channel.getFullWidthAtHalfMaximum();
                break;
            
            case absslope:
                numValue = Math.abs(channel.getSlope());
                break;
            
            case stddev:
                numValue = channel.getStdDev();
                break;
                
            case saxdist :
                numValue = channel.getSAXDistanceFrom(stat.getSaxString(), stat.getNumLetters(), stat.getAlphaLength());
                break;
                
             case bestfit:
                numValue = channel.getBestFit();
                break;
                   
             case bfintercept:
                numValue = channel.getBestFitIntercept();
                break;
                 
             case freq :
                 FrequencyDomain freq = channel.getFrequencyDomain();
                 numValue = freq.getMagAtFreq(stat.getFreqIndex());
                 break;
        }   
        
    }
    
    
    public static void main(String [] args) {
        //.. contrive an instance channel
        Channel c = Channel.generate(10);
         c.printStream();
        try {
            //.. 1) Test mean; should be mean of sum(0,2,4,..., 18)/10 =9
            NumericAttribute attr = new NumericAttribute(c, new Statistic(Statistic.Stat.mean),"x");
            attr.extract();
            System.out.println(attr.name + " has " + attr.numValue);

            //.. 2) Test smallest; should be 0
             attr = new NumericAttribute(c, new Statistic(Statistic.Stat.smallest),"x");
            attr.extract();
            System.out.println(attr.name + " has " + attr.numValue);

            //..3) Test largest; should be 18
             attr = new NumericAttribute(c, new Statistic(Statistic.Stat.largest),"x");
            attr.extract();
            System.out.println(attr.name + " has " + attr.numValue);

            //..4) Test slope; should be 2
            attr = new NumericAttribute(c, new Statistic(Statistic.Stat.slope),"x");
            attr.extract();
            System.out.println(attr.name + " has " + attr.numValue);
           
            //..5) Test second derivative; should be 0
            attr = new NumericAttribute(c, new Statistic(Statistic.Stat.secondder),"x");
            attr.extract();
            System.out.println(attr.name + " has " + attr.numValue);
            
            //..6) Test peak index 
            attr = new NumericAttribute(c, new Statistic(Statistic.Stat.t2p),"x");
            attr.extract();
            System.out.println(attr.name + " has " + attr.numValue);
            
            
            attr = new NumericAttribute(c, new Statistic(Statistic.Stat.freq, 10),"x");
            attr.extract();
            System.out.println(attr.name + " has " + attr.numValue);
        }
        
        catch(Exception e) {e.printStackTrace();}
        
    }

    @Override
    public weka.core.Attribute getWekaAttribute() {
        return new weka.core.Attribute(this.name);
    }
}
