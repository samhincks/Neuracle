package timeseriestufts.evaluatable;

import java.util.ArrayList;
import org.apache.commons.math3.distribution.NormalDistribution;
import timeseriestufts.kth.streams.bi.ChannelSet;
import timeseriestufts.kth.streams.uni.Channel;

/**
 * A different sort of classifier. Not trained on an experiment, instead trained on a plain ChannelSet.
 * It doesn't care what the class is. It moves through the data at leaps of x. It records the observation
 * of the feature it sees. In the end it has some kind of idea of the distribution of the data it sees.
 * And then you feed it a new span of data. It compares it to what it sees, and returns the probability
 * of seeing it.
 * To start off -- we will only use slope
 * @author shincks
 */

public class StreamingClassifier extends ClassificationAlgorithm {
    
    public StreamingClassifier(String id) {
        this.id = id;
    }
    NormalDistribution [] slopes;
    int leap;
    int window;
    Transformations transformations;
    
    /** Figure out a probability density function for observing a particular slope
     **/
    public void train(ChannelSet cs, int leap, int window, Transformations transformations) throws Exception{
        this.leap = leap;
        this.window = window;
        this.transformations = transformations; 
        slopes= new NormalDistribution[cs.streams.size()]; //.. one for each channel, as large as the number of observations
        int chan =0;
        for (Channel c : cs.streams) {
            Channel observations = new Channel(1);  //.. Just a 'fake' channel for computing mean and sd
            int splits = c.numPoints / leap;
            int start = 0;
            for (int i = 0; i < splits; i++) {
                if (start + window < c.numPoints) {
                    Channel sample = c.getSample(start, start + window, true);
                    observations.addPoint((float) sample.getSlope());
                    start+=leap;
                }
            }
            
            NormalDistribution n = new NormalDistribution(observations.getMean(), observations.getStdDev());
            slopes[chan] = n;
            chan++;
        }
        super.timesTrained++;
    }
    
    /** This is actually PERCENTILE, not probability
     **/
    public double getProbability(int channel, double observation) {
        double prob =  slopes[channel].cumulativeProbability(observation);
        double dis = Math.abs( 0.5 -prob);
        
        if (prob > 0.5) //.. then 0.9 should be 0.1
            prob = 1 - prob;
        return prob; 
    }  
    
    public double[] getProbabilityTuple(double [] observations) {
        double [] ret = new double[slopes.length];
        for (int i = 0; i < slopes.length; i++) {
            ret[i] = getProbability(i, observations[i]);
        }
        return ret;
    }
    
    public String[] getLastSlopes(ChannelSet cs) throws Exception {
        String[] ret = new String[slopes.length];
        int i=0;
        
        //.. if the trained channelset was manipulated, then so should this one be!
        if (transformations != null) for (Transformation t: transformations.transformations) 
             cs = cs.manipulate(t, true);
       
        if (cs.streams.size() != slopes.length ) throw new Exception("ChannelSet must have as many channels as the one trained on");

        for(Channel c : cs.streams) {
            Channel sample = c.getSample(c.numPoints - window, c.numPoints, true);
            double slope =  sample.getSlope();
            double prob = getProbability(i, slope);  
            String s = Float.toString((float)slope);
            String p = Float.toString((float)prob);
            System.out.println(s);
            ret[i] = s+"%"+p;
            i++;
        }
        return ret;
    }
    public static void main(String[] args) {
        try {
            ChannelSet cs = ChannelSet.generate(1, 150);
            StreamingClassifier sc = new StreamingClassifier("slope");
            sc.train(cs, 5, 10, null);
            System.out.println(sc.getProbability(0, 3));
            sc.getLastSlopes( ChannelSet.generate(1, 150));
        } catch (Exception e) {e.printStackTrace();}
    }

}
