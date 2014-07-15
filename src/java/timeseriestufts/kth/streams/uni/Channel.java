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
import filereader.TSTuftsFileReader;
import filereader.experiments.Beste;
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
import timeseriestufts.kth.streams.bi.ChannelSet;
import timeseriestufts.kth.streams.tri.Experiment;

/** A collection of floats in one channel, with a particular offset and a consistent
 * framesize determining intervals between points
 *
 * @author Sam Hincks
 */
public class Channel extends UnidimensionalLayer  {
    protected double framesize;  
    public float sampleRate; //.. num samples persecond
    public static float HitachiRPS = 11.7925f; //.. readings per second for the hitachi
    private Complex [] transformed = null;
    private FrequencyDomain frequencyDomain = null;
    
    /**The SynchedChannel class stores the raw data from input files along with corresponding timestamps.    
     * @param _offset - the first index
     * @param _framesize - the interval between readings
     */
    public Channel(double framesize, int maxLength) {
        this.framesize = framesize;   
        //.. TEMP
        this.framesize = 1 / HitachiRPS;
        this.sampleRate = HitachiRPS;
        data = new float[maxLength];
    }
    
    public Channel(double framesize, float [] data){
        this.data = data;
        this.numPoints = data.length;
        //.. TEMP
        this.framesize = 1 / HitachiRPS;
        this.sampleRate = HitachiRPS;
    }
    public Channel(double framesize) {
        this.framesize = framesize;
        data2 = new ArrayList();
        this.synchedWithDatabase = true;
        //.. TEMP
        this.framesize = 1 / HitachiRPS;
        this.sampleRate = HitachiRPS;
    }
    
    public Channel(double framesize, ArrayList<Float> data) {
        this.framesize = framesize;
        super.data2 = data;
        super.synchedWithDatabase = true;
        super.numPoints = data.size();
        //.. TEMP
        this.framesize = 1 / HitachiRPS;
        this.sampleRate = HitachiRPS;
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
        if (true) throw new Exception("Sampling at " + freq + " , sr " + sampleRate + " fs " + this.framesize);
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
    
    /**Set all points between 0 and 1. Largest will be 1 and smallest will be 0. 
     The rest are **/
    public Channel normalize(boolean copy) throws Exception{
       float smallest = (float) this.getMin();
       float largest = (float) this.getMax();
       float range = (largest - smallest);
       float incr = 1.0f/ range;
       
       if (copy) throw new Exception("Copy not yet implemented");
       else {
           for(int i =0; i < numPoints; i++) {
               Float p = this.getPointOrNull(i);
               p = (p-smallest)*incr;
               this.setPoint(i, p);
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
                 float p =(float) ((super.getPointOrNull(i) -avg) / std);
                 sc.addPoint(p);
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
    
    /**Return the frequency domain representation of this channel, 3 channels
     * where magnitudeChannel holds the normalized magnitidues at the frequencies
     * in frequencyChannel and phaseChannel holds the phase.  
     **/
    public FrequencyDomain getFrequencyDomain() throws Exception{
        if (frequencyDomain != null) return frequencyDomain;
        frequencyDomain = new FrequencyDomain(this.sampleRate);
        frequencyDomain.complexToFreq(FFT());
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
            for (int i =0; i < numPoints; i++) {newArray[i] = super.getPoint(i)*Channel.hamming(i,newLength); }
            
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
   
    /**Resolves fourier issues when channel's not a power of 2. Unclear how effective**/
    private static double hamming(int n, int N) {
        // assert that the absolute value is >= 0
        assert (n <= N) : "Window sample: " + n + " is beyond expected window range: " + N;
        double out = 0.54 - 0.46 * Math.cos(2 * Math.PI * (double) n / (N - 1));
        return out;
    }
    
    /**Return a deep copy of this channel**/
    public Channel getCopy() {
        Channel channel = new Channel(this.framesize, this.numPoints);
        for (int i = 0; i < numPoints; i++) {
            channel.addPoint(this.getPointOrNull(i));
        }
        return channel;
    }
    
     public static Channel generate(int numReadings) {
         
         if (numReadings == -1) {
             Channel c = new Channel(1, 128);
             int MIN_RATE =7;
             for (int i = 0; i < c.data.length; i++) {
                  float point =(float) (Math.sin(i * Math.PI * 2 / MIN_RATE) + 0.0
                          * Math.sin(i * Math.PI * 4 / MIN_RATE) + 0.0
                          * Math.sin(i * Math.PI * 15.3 / MIN_RATE));
                  
                 // point = (float) (point *hamming(i, 128));
                  c.addPoint(point);
                  //if (i %255 ==0)System.out.println(c.getPointOrNull(i));
             }
             return c;
         }
         
         ///. if 0, then generate actual readings
         if (numReadings ==0) {
             Channel c = new Channel(1, 30); 
             c.addPoint(1);c.addPoint(2);c.addPoint(3);c.addPoint(4);c.addPoint(5);
             c.addPoint(4);c.addPoint(3);c.addPoint(2);c.addPoint(1);c.addPoint(0);
             c.addPoint(1);c.addPoint(2);c.addPoint(3);c.addPoint(4);c.addPoint(5);
             c.addPoint(4);c.addPoint(3);c.addPoint(2);c.addPoint(1);c.addPoint(0);
             c.addPoint(1);c.addPoint(2);c.addPoint(3);c.addPoint(4);c.addPoint(5);
             c.addPoint(4);c.addPoint(3);c.addPoint(2);c.addPoint(1);c.addPoint(0);
             
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
            int numPoints = -1;
            Channel c = generate(numPoints);
            Channel b = generate(87);
            
            int TEST =87; //.. we have our datalayer, now set what we want to test
            
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
                 //c = c.zScore(true);
                 //c.printStream();
                Complex [] transformed = c.FFT();
                FrequencyDomain fd = new FrequencyDomain(c.numPoints);
                fd.complexToFreq(transformed);
                fd.print();
               // System.out.println("XXXXXXXXXXXXXXXXX");
            
               // System.out.println("XXXXXXXXXXXXXXXXX");
            }
            if (TEST ==86) {
                String filename = "input/sinWave1at4hzp3at12hz.csv";
                //.. read
                TSTuftsFileReader f = new TSTuftsFileReader();
                ChannelSet cs = f.readData(",", filename);
                //cs.printStream();
                Channel ch = cs.getFirstChannel();
                Complex[] transformed = ch.FFT();
                FrequencyDomain fd = new FrequencyDomain(1);
                fd.complexToFreq(transformed);
                fd.print();

            }
            //.. test fourier transform
            if (TEST ==87) {
                ChannelSet beste = Beste.getChannelSet();
                Experiment e = beste.splitByLabel("condition");
                Channel b1 = e.matrixes.get(3).getChannel(0);
                b1 = b1.zScore(false);
               // b1.printStream();
               // b1.printStream();
                Complex [] transformed = b1.FFT();
                FrequencyDomain fd = new FrequencyDomain((int) Channel.HitachiRPS);
                fd.complexToFreq(transformed);
                fd.print();
               // System.out.println("XXXXXXXXXXXXXXXXX");
            }
            if (TEST ==88) {
                short [] data = new short[c.data.length];
                int index=0;
                for (float s : c.data) {
                    data[index] = (short)s;
                    index++;
                }
                double[]  powers = SignalProcessing.getSpectrum(data);
                for(Double d: powers) {
                    System.out.println(d);
                }
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
