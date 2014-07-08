/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeseriestufts.kth.streams.uni;

import ddf.minim.effects.BandPass;
import ddf.minim.effects.HighPassSP;
import ddf.minim.effects.LowPassSP;
import edu.hawaii.jmotif.datatype.TPoint;
import edu.hawaii.jmotif.datatype.TSException;
import edu.hawaii.jmotif.datatype.Timeseries;
import edu.hawaii.jmotif.logic.sax.SAXFactory;
import edu.hawaii.jmotif.logic.sax.alphabet.Alphabet;
import edu.hawaii.jmotif.logic.sax.alphabet.NormalAlphabet;
import java.util.ArrayList;
import org.JMathStudio.DataStructure.Vector.Vector;
import org.JMathStudio.SignalToolkit.FilterTools.IIRFilter;
import org.JMathStudio.SignalToolkit.FilterTools.IIRFilterMaker;
import org.apache.commons.math.stat.regression.SimpleRegression;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;

import org.apache.commons.math3.transform.TransformType;
import org.apache.commons.math.util.MathUtils;

/** A collection of floats in one channel, with a particular offset and a consistent
 * framesize determining intervals between points
 *
 * @author Sam Hincks
 */
public class Channel extends UnidimensionalLayer  {
    protected double framesize;    
    private Complex [] transformed = null;
    private FrequencyDomain frequencyDomain = null;
    
    /**The SynchedChannel class stores the raw data from input files along with corresponding timestamps.    
     * @param _offset - the first index
     * @param _framesize - the interval between readings
     */
    public Channel(double framesize, int maxLength) {
        this.framesize = framesize;    
        data = new float[maxLength];
    }
    
    public Channel(double framesize, float [] data){
        this.data = data;
        this.numPoints = data.length;
    }
    public Channel(double framesize) {
        this.framesize = framesize;
        data2 = new ArrayList();
        this.synchedWithDatabase = true;
    }
    
    public Channel(double framesize, ArrayList<Float> data) {
        this.framesize = framesize;
        super.data2 = data;
        super.synchedWithDatabase = true;
        super.numPoints = data.size();
    }
    
    /** Get the frame size, interval between readings 
     * @return framesize -- the interval between readings
     */
    public double getFramesize() {
        return framesize;
    }    
    
  
   
    //------------------------------------------------------------------------
    /** Returns a new FullChannel, a sample of current channel with copies of requested points.
     * Get() throws an exception if any point is out of range
     * @param middleTimestamp - the middle of the sample channel
     * @param framesize - interval between points
     * @param before - number of points before X
     * @param after -- number of points after X
     * @return SynchedChannel
     */
    public Channel getSample(int start, int end) throws Exception { 
        if (start > end || end > numPoints) throw new Exception("Illegal arguments" + start + "  > " + end + " or " + end + " > " + numPoints);
        //.. extract total and firstTimestamp
        int total = end - start; //.. +1 since we include the middle frame
        
        
        float [] samplePoints = new float[total];
        int index =0;
        //.. add each of the points the new channel
        for (int i = start; i < end; i++) {
            samplePoints[index] = super.getPointOrNull(i);
            index++;
        }

        Channel sample = new Channel(framesize, samplePoints);
        sample.setId(id + "-" + start + "to" + end);

        return sample;
    }
 
      
    
    /**Prints statistics about the channel.      * 
     */
    public void printStats() {
       System.out.println("----------------");
       System.out.println("Printing Stats...");
       System.out.println("Total readings: " + getCount());       
       System.out.println("Framesize : " + getFramesize());       
       System.out.println("----------------");
    }
    
   /**Returns a SAX representation of the data : http://www.cs.ucr.edu/~eamonn/SAX.htm
    * @param stringLength length of returned SAX string
    * @param alphabetSize more precision of SAX string if larger
    * @return String a SAXRepresentation of the timeseries
    * @throws TSException 
    */
   public String getSAXRepresentation(int stringLength, int alphabetSize) throws TSException {
        Timeseries timeseries = new Timeseries();
        //... Create a TPoint for each Point and add to SAX timeseries
        for (int i = 0; i < numPoints; i++) {
            Float p = super.getPointOrNull(i);
            TPoint tPoint = new TPoint(p, i); 
            timeseries.add(tPoint);
        }

        //.. Get SAX-representation of this channel
        Alphabet alphabet = new NormalAlphabet();
        String SAXRepresentation = SAXFactory.ts2string(timeseries, stringLength,
                alphabet, alphabetSize);   
        
        return SAXRepresentation;
    }

   /**Returns the difference, an int, between to SAX strings. The smaller, the closer
    * @param stringLength
    * @param alphabetSize
    * @param otherString An existing SAX represenation
    * @return distance 
    * @throws TSException 
    */
    public int getSAXDistanceTo(Channel otherChannel, int stringLength, int alphabetSize) throws TSException {
        String thisSAXRep = getSAXRepresentation(stringLength, alphabetSize);
        String otherSAXRep = otherChannel.getSAXRepresentation(stringLength, alphabetSize);
        
        //.. return distance
        return SAXFactory.strDistance(otherSAXRep.toCharArray(), thisSAXRep.toCharArray());
    }
    
    /**Get the distance between two strings when we know one of the strings representation already*/
    public int getSAXDistanceFrom(String otherSAXRep, int stringLength, int alphabetSize) throws TSException {
        String thisSAXRep = getSAXRepresentation(stringLength, alphabetSize);
        int distance= SAXFactory.strDistance(otherSAXRep.toCharArray(), thisSAXRep.toCharArray());
        return distance;
    }
    
  
   
   /** Return a new channel with original copies of the data that is the moving
     * average of at "readingsBack" points. The first "readingsBack" points go as
     * far back as they can. If copy = true, return this original layer
     */
    public Channel movingAverage(int readingsBack, boolean copy) throws Exception{
        if (copy){
            Channel sc = new Channel(this.framesize, this.numPoints);
            sc.setId(id+"ma"+readingsBack);
            int index =0;
           
            //.. calculate moving average for each point
            for (int i =0; i < numPoints; i++) {
               int start = ((index - readingsBack) > 0) ? (index - readingsBack) : 0; //.. the largest of 0 and index-readingsBack
               double avg = super.getMean(start, index);
               sc.addPoint((float)avg);
               index++;
            }
            return sc;
        }
        else {
            int index =0;

            //.. calculate moving average for each point
            for (int i =0 ; i < numPoints; i++) {
               int start = ((index - readingsBack) > 0) ? (index - readingsBack) : 0; //.. the largest of 0 and index-readingsBack
               double avg = super.getMean(start, index);
               super.setPoint(i,(float)avg);
               index++;
            }
            return this;
        }
    }
    
    /** Return a new Channel with original copies of the data that is a bandpassed filtered version 
     * at a specified cutoff point.
     * UNTESTED; I have not verified that this works as intended. 
     */
    public Channel bandpass(float lowCut, float highCut, boolean copy) throws Exception{
        if (this.framesize ==0) throw new Exception("Cannot filter when framesize is 0");
        if (highCut < lowCut) throw new Exception("High cut must be larger than lowCut");
        int sampleRate = (int) ((double)1 /this.framesize);
        if (sampleRate ==0 ) sampleRate =1; //.. round to 1
        
        //.. set variables for bandpass filter.
        float band = highCut - lowCut;
        float center =  band/2.0f;
        
        if (center ==0) throw new Exception("high cut and low cut too close together");
        
        BandPass bp = new BandPass(center, band, sampleRate);
        float [] signal = new float[numPoints];
        
        //.. add each point to an array of floats
        for (int i =0; i < numPoints; i++){
            signal[i] = super.getPointOrNull(i);
        }
        bp.process(signal);
        
        //.. If shuold be a copy then create a new channel
        if (copy){
             Channel sc = new Channel(this.framesize, numPoints);
             sc.setId(id+"bp"+lowCut+highCut);
        
            //.. add the filtered points to the new synched channel
            for(int i=0; i< signal.length; i++){
                sc.addPoint(signal[i]);
            }
            return sc;
        }
        
        else {
            //.. add the filtered points as new values to the channel
            for(int i=0; i< signal.length; i++){
               super.setPoint(i,signal[i]);
            }
            return this;
        }
    }
    
    /** Return a new Channel with original copies of the data that is a lowpass filtered version 
     * at a specified cutoff point. Only passes frequences lower than specified amount
     * UNTESTED; I have not verified that this works as intended. 
     */
    public Channel lowpass(float freq, boolean copy) throws Exception{
        if (this.framesize ==0) throw new Exception("Cannot filter when framesize is 0");
    
        //.. set variables for bandpass filter.
        int sampleRate = (int) (1 /this.framesize);
        LowPassSP lp = new LowPassSP(freq, sampleRate);
        
        float [] signal = new float[numPoints];
        
        //.. add each point to an array of floats
        for (int i=0; i <numPoints; i++) {
            signal[i] = super.getPointOrNull(i);
        }
        lp.process(signal);
        
        if (copy) {    
            Channel sc = new Channel(this.framesize, this.numPoints);
            sc.setId(id+"lp"+freq);
        
            //.. add the filtered points to the new synched channel
            for(int i=0; i< signal.length; i++){
                sc.addPoint(signal[i]);
            }
            return sc;
        }
        else {
            for(int i=0; i< signal.length; i++){
                super.setPoint(i, signal[i]);
            }
            return this;
        }
    }
    
    /** Return a new Channel with original copies of the data that is a highpass filtered version.
     * It passes frequencies above cutoff point and attenuates those beneath.
     * 
     * UNTESTED; I have not verified that this works as intended. 
     */
    public Channel highpass(float freq,boolean copy) throws Exception{
        if (this.framesize ==0) throw new Exception("Cannot filter when framesize is 0");
       
        //.. set variables for bandpass filter.
        int sampleRate = (int) (1 /this.framesize);
        HighPassSP hp = new HighPassSP(freq, sampleRate);
        
        float [] signal = new float[this.numPoints];
        int index =0;
        
        //.. add each point to an array of floats
        for (int i =0; i < numPoints; i++) {
            signal[index] = super.getPointOrNull(i);
            index++; 
        }
        hp.process(signal);
        if (copy){
             Channel sc = new Channel(this.framesize, this.numPoints);
             sc.setId(id+"bp"+freq);

             //.. add the filtered points to the new synched channel
            for(int i=0; i< signal.length; i++){
                sc.addPoint(signal[i]);
            }
            return sc;

        }
        else {
            for(int i=0; i< signal.length; i++){
                super.setPoint(i,signal[i]);
            }
            return this;
        }
    }
    
   /**Z-Score the data; subtract the mean, and divide by standard deviation for each*/
    public Channel zScore (boolean copy) throws Exception{
        if (copy) {
            double avg = this.getMean();
            double std = this.getStdDev();
            Channel sc = new Channel(this.framesize, this.numPoints);
            sc.setId(id + "zscore");

            //.. add the filtered points to the new synched channel
            for (int i = 0; i < numPoints; i++) {
                 sc.addPoint((float) ((super.getPointOrNull(i) -avg) / std));
            }
            return sc;
        }
        else {
            double avg = this.getMean();
            double std = this.getStdDev();
            
            for (int i=0; i< numPoints; i++) {
                super.setPoint(i, ((float) ((super.getPointOrNull(i) -avg) / std)));
            }
            return this;
        }
    }
    
    /**Average the values of the other channel with this one. If they are different size, keep it
     * to the size of this layer
     */
    public void merge(Channel ic) throws Exception{
        //.. if we haven't added any data yet, copy over the other point
        if (numPoints ==0) {
            for (int i=0; i< ic.numPoints; i++) {
                super.addPoint(ic.getPointOrNull(i));
            }
        }
            
        //.. average the value between each point, changing the actual value in this structure
        for (int i =0; i< numPoints; i++) {
            Float thisPoint = getPointOrNull(i);
            Float otherPoint = ic.getPointOrNull(i);
            
            //.. average them if they both exist
            if ( otherPoint != null)
                super.setPoint(i, (thisPoint + otherPoint) / 2);
        }
    }
      /**Set the first point in the timeseries to 0 and all other relative to this new value,
     * so the original series 8, 10, 12 would be translated to 0, 2, 4. 
     * True = Returns a copy with new points.
     * False = Returns direct manipulation on this dataset
     */
    public Channel anchorToZero(boolean copy) throws Exception{
        
        //.. get first point, set a copy to 0 and add it
        Float fp = this.getFirstPoint();
        
        //.. if we want a copy, create a new instance channel with brand new points
        if (copy) {
            Channel ic = new Channel(this.framesize, this.numPoints);
            ic.addPoint(0);

            //.. add a copy of each of the following points, where the value is the difference to first
            for (int i = 1; i < numPoints; i++) {
                Float oldP = super.getPoint(i);
                Float np = (oldP- fp); //.. new value is difference to first
                ic.addPoint(np);
            }

            return ic;
        }
        else {
            //.. save first value then set it to 0
            this.setPoint(0, 0);
            
            //.. for each subsequent point set it as difference to first
            for (int i = 1; i < numPoints; i++) {
                Float oldP = this.getPoint(i);
                this.setPoint(i, oldP - fp); //.. new value is difference to first
            }
            return this;
        }
    }
    
    public FrequencyDomain getFrequencyDomain() throws Exception{
        if (frequencyDomain != null) return frequencyDomain;
        
        int sampleRate = (int) (1.0/ this.framesize);
        frequencyDomain = new FrequencyDomain(FFT(), sampleRate );
        return frequencyDomain;
    }
    
    public Complex [] FFT() throws Exception{
        if (transformed != null) return transformed; 
        
        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.UNITARY);
        double [] dbldata = new double[numPoints];
        //.. ugh, must convert each piece to double first
        
        for (int i =0; i < numPoints; i++) {dbldata[i] = super.getPoint(i); }
        
        double logbase2 =  MathUtils.log(2,numPoints);
       // System.out.println(logbase2 %1);
        
        //.. pad what up to next power of 2 with zeros
        if(logbase2 % 1 !=0) {
            double nextPower = Math.ceil(logbase2);
            int newLength = (int)Math.pow(2, nextPower);
            double [] newArray = new double [newLength];
            
            //.. copy old values
            for (int i =0; i < numPoints; i++) {newArray[i] = super.getPoint(i); }
            
            //.. add the new bogus zero values
            for (int i = numPoints; i < newLength; i++) {
                newArray[i] = 0;
            }
            dbldata = newArray;
        }
        
        //.. Transform to frequency domain
        transformed = fft.transform(dbldata, TransformType.FORWARD);
      
        return transformed;
    }
    
    /**Adjust by the fluctation that was present in the a different sample (for instance baseline)
     y is corrected by x.
     Throw error if trial is too far off from baseline in length, otherwise pad
     **/
    public  Channel detrend(float[] baseline, double maxDiff, boolean copy) throws Exception{ 
        System.err.println("NO longer works if synchronized, you will have to implement a method to copy baseline to arraylist");
        SimpleRegression regression = new SimpleRegression();
        
        //.. if the baseline was longer than this trial
        if (baseline.length > numPoints) {
           //.. if difference is less than 10% pad
            if (baseline.length < numPoints *maxDiff) {
                
                for (int i = 0; i < numPoints; i++) {
                   regression.addData(baseline[i], super.getPoint(i));
                }
                
                for (int i =numPoints-1; i < baseline.length; i++) {
                    regression.addData(baseline[i], super.getLastPoint());
                }
            }
            
            //.. unresolvable difference, so throw an error
            else {
                throw new IllegalArgumentException("detrending error: baseline is " +
                        baseline.length + " instance-channel is " + numPoints );
            }
        }
        
        //.. or if this channel is longer than baseline
        else if (numPoints > baseline.length) {
             //.. if difference is less than 10% pad
            if (numPoints < baseline.length *maxDiff) {
                
                for (int i = 0; i < baseline.length; i++) {
                   regression.addData(baseline[i], super.getPoint(i));
                }
                
                for (int i =baseline.length-1; i < numPoints; i++) {
                    regression.addData(baseline[baseline.length-1], super.getPoint(i));
                }
            }
            
            //.. unresolvable difference, so throw an error
            else {
                throw new IllegalArgumentException(this.getId() + " detrending error: baseline is " +
                        baseline.length + " instance-channel is " + numPoints );  }
        }
        
        //.. otherwise, its perfect. baseline and data match up perfectly
        else {       
           regression = new SimpleRegression();

           for (int i = 0; i < numPoints; i++) {
              regression.addData(baseline[i], super.getPoint(i));
           }
        }
        double slope = regression.getSlope();
        double intercept = regression.getIntercept();
        
        //.. where do we put new data? In a copy or replace value
        Channel retChannel = this;
        if (copy) {
            retChannel = new Channel(this.framesize, this.numPoints);
        }
        
        for (int i = 0; i < numPoints; i++) {
            //y -= intercept + slope * x 
            float curPoint = super.getPoint(i);
            float baselinePt;
            
            ///. in case the baseline was not long enough
            if (i < baseline.length)
                 baselinePt = baseline[i];
            else
                 baselinePt = baseline[baseline.length-1];
            
            
            curPoint -= intercept + (baselinePt *slope);
            if (copy)
                retChannel.addPoint(curPoint);
            
            else
                super.setPoint(i, curPoint);
        }
        return retChannel;
    }
    
    
    public Channel bwBandpass(int order, float fl, float fh) throws Exception{
        IIRFilter iirFilter = IIRFilterMaker.butterWorthBandPass(order, fl, fh);
        Vector v = new Vector(numPoints);
        for (int i=0; i < numPoints; i++) {
            v.setElement(super.getPoint(i), i);
        }
        
        v = iirFilter.filter(v);
        
        for (int i=0; i <v.length(); i++) {
            super.setPoint(i, v.getElement(i));
        }
        
        return this;
        
    }
   
    
     public static Channel generate(int numReadings) {
         ///. if 0, then generate actual readings
         if (numReadings ==0) {
             Channel c = new Channel(1, 30); 
             c.addPoint(16.800f);c.addPoint(16.110f);c.addPoint(16.380f);c.addPoint(16.570f);c.addPoint(16.680f);
             c.addPoint(16.110f);c.addPoint(15.960f);c.addPoint(16.080f);c.addPoint(16.140f);c.addPoint(16.170f);
             c.addPoint(16.430f);c.addPoint(16.570f);c.addPoint(16.320f);c.addPoint(16.030f);c.addPoint(16.110f);
             c.addPoint(16.800f);c.addPoint(16.110f);c.addPoint(16.380f);c.addPoint(16.570f);c.addPoint(16.680f);
             c.addPoint(16.110f);c.addPoint(15.960f);c.addPoint(16.080f);c.addPoint(16.140f);c.addPoint(16.170f);
             c.addPoint(16.430f);c.addPoint(16.570f);c.addPoint(16.320f);c.addPoint(16.030f);c.addPoint(16.110f);
             
             return c;
         }
         
        //.. contrive an instance channel
        Channel c = new Channel(1, numReadings);
        for(int i=0; i< numReadings; i++) {
             c.addPoint((float)Math.random()*10);
        }
        return c;
    }
   
     
     
    public static void main(String [] args) {
        try{ 
            int numPoints = 0;
            Channel c = generate(numPoints);
            Channel b = generate(numPoints);
            
            int TEST =13; //.. we have our datalayer, now set what we want to test
            c.printStream();
            
            if (TEST ==13) {
                c.bwBandpass(4, 0.1f, 0.2f);
                c.printStream();
            }
            
            if (TEST ==12) {
                System.out.println(c.getSlope());
                System.out.println(c.getBestFit());
            }
            if (TEST ==11) {
                Channel d = c.detrend(b.data, 1.1, true);
                d.printStream();
            }
            
            if(TEST ==0) {
                Channel moving = c.movingAverage(3,true);
                moving.printStream();
                c.printStream();
            }
            
            if (TEST ==1) {
                Channel bp = c.lowpass(0.3f,true);
                bp.printStream();
                c.printStream();
            }
            
            if (TEST ==2) {
                Channel bp = c.bandpass(0.1f, 0.8f,true);
                bp.printStream();
                c.printStream();
            }
            
            if (TEST== 3) {
                c.printDescriptiveStats();
            }
            
            if (TEST ==4) {
                Channel fc = c.getSample(2,9);
                fc.data[0] = 222.4f;
                fc.printStream();
                c.printStream();
            }
            
            if (TEST ==5) {
                b.printStream();
                b.merge(c);
                b.printStream();
            }
            if (TEST ==6) {
                Channel a = c.anchorToZero(true);
                c.printStream();
                a.printStream();
            }
            if (TEST ==7) {
                String sax = c.getSAXRepresentation(3, 3);
                System.out.println(sax);
            }
            
            //.. test fourier transform
            if (TEST ==8) {
                Complex [] transformed = c.FFT();
                FrequencyDomain fd = new FrequencyDomain(transformed, 11);
                
                System.out.println("XXXXXXXXXXXXXXXXX");
            }
            
            if (TEST ==9) {
                c.getFullWidthAtHalfMaximum();
            }
            if (TEST ==10) {
                c.printStream();
                System.out.println("STD: "+ c.getStdDev());
            }
        }
        catch (Exception e) {e.printStackTrace();} 
        
    }

    
    
}
