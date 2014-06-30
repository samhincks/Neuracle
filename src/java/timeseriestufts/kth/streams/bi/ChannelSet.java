/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeseriestufts.kth.streams.bi;

import filereader.Markers;
import filereader.Markers.Trial;
import timeseriestufts.kth.streams.uni.*;
import timeseriestufts.kth.streams.uni.UnidimensionalLayer;
import timeseriestufts.kth.streams.uni.Channel;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import timeseriestufts.evaluatable.PassFilter;
import timeseriestufts.evaluatable.TechniqueSet;
import timeseriestufts.evaluatable.Transformation;
import timeseriestufts.evaluation.experiment.Classification;
import timeseriestufts.kth.streams.DataLayer;
import timeseriestufts.kth.streams.bi.BidimensionalLayer;
import timeseriestufts.kth.streams.tri.Experiment;

/**A collection of ChannelSet
 * @author Sam
 */
public class ChannelSet extends BidimensionalLayer<Channel>{
    public ArrayList<Markers> markers; //.. markers describing where each condition starts and ends
    
    public Integer realEnd; //.. its position in a former layer. 
    public Integer realStart;//... If we're setting these its derived from another layer
    
    /**Initialize Unidimensional Array and unidimensional stat representation. 
     */
    public ChannelSet() {
        streams = new ArrayList();
    }
    /**
     * Initialize Unidimensional Array and unidimensional stat representation.
     */
    public ChannelSet(int realStart, int realEnd) {
        streams = new ArrayList();
        this.realStart = realStart;
        this.realEnd = realEnd;
    }
    
    /*if this is a derived layer with different start and end points, return that 
     Otherwise return 0
    */
    public Integer getRealStart() {
        if (realStart == null) return 0;
        return realStart;
    }
    
    public Integer getRealEnd() {
        if (realEnd == null) return super.maxPoints();
        return realEnd;
    }
    
    public int getMaxPoints() {
        return super.maxPoints();
    }
    public int getMinPoints() {
        return super.minPoints();
    }
    
    /**Get the first channel; throw an exception if it does not exist.
     * @return the FirstChannel 
     */
    public Channel getFirstChannel() throws Exception  {        
        if (streams.isEmpty())
            throw new Exception("No Channels added yet");
        else
            return (Channel) streams.get(0);
               
    }
  

 
    /**Returns the channel at index
     * @param index
     * @return
     * @throws Exception 
     */
    public Channel getChannel(int index) throws Exception {
        if (index >= streams.size())
            throw new Exception("Requested channel Index ("+index +") >= streams.size ("+streams.size()+")");
        
        return (Channel)streams.get(index);
    }

    /**Return a new ChannelSet (with -actual copies- of underlying points) that is 
     * a moving average of each of the channels in this dataset. None of the data in this 
     * datalayer is altered. A moving average smooths out short-term fluctations and 
     * highlights longer term trends. It is a type of low-pass filter. Specifically, a moving average
     * computes a new sequence of values where each value represents the mean at n readings back.
     */
    public ChannelSet movingAverage(int readingsBack, boolean copy) throws Exception{
        if (copy) {
            ChannelSet cs = getCopy(this.id + "movingAverage"+readingsBack);
            for(UnidimensionalLayer u : streams) {
                Channel thisChan = (Channel) u;
                Channel sc = thisChan.movingAverage(readingsBack, true);
                cs.addStream(sc);
            }
            return cs;
        }
        else {
            for(UnidimensionalLayer u : streams) {
                Channel thisChan = (Channel) u;
                thisChan.movingAverage(readingsBack, false);
            }
            return this;
        }

    }
    /***Return a simple copy with a new name*/
    private ChannelSet getCopy(String id) throws Exception{
        ChannelSet cs = new ChannelSet();
        cs.setId(id);
        if (markers != null){ 
            for (Markers m : markers){
                cs.addMarkers(m);
            }
        }
        return cs;
    }
    /***Return a simple copy, and specify realStart / realEnd,
     so that it remembers in its new instance where its being referred to
     and the markers still make sense*/
    private ChannelSet getCopy(int start, int end) throws Exception {
        ChannelSet cs = new ChannelSet(start, end);
        cs.setId(this.id + start+"-"+end);
        if (markers != null) {
            for (Markers m : markers) {
                cs.addMarkers(m);
            }
        }
        return cs;
    }
    
     /**Return a new ChannelSet (with -actual copies- of underlying points) that is 
     * a lowpass filtered version of each of the channels in this dataset. 
     */
    public ChannelSet lowpass(float freq, boolean copy) throws Exception{
        if (copy){
            ChannelSet cs = getCopy(this.id + "lowpass"+freq);

            for(UnidimensionalLayer u : streams) {
                Channel thisChan = (Channel) u;
                Channel sc = thisChan.lowpass(freq, true);
                cs.addStream(sc);
            }
            return cs;
        }
        else {
            for(UnidimensionalLayer u : streams) {
                Channel thisChan = (Channel) u;
                thisChan.lowpass(freq, false);
            }
            return this;
        }
    }
     /**Return a new ChannelSet (with -actual copies- of underlying points) that is 
     * a highpass filtered version of each of the channels in this dataset. 
     */
    public ChannelSet highpass(float freq, boolean copy) throws Exception{
        if(copy) {
            ChannelSet cs = getCopy(this.id + "highpass"+freq);

            for(UnidimensionalLayer u : streams) {
                Channel thisChan = (Channel) u;
                Channel sc = thisChan.highpass(freq, true);
                cs.addStream(sc);
            }

            return cs;
        }
        else {
            for(UnidimensionalLayer u : streams) {
                Channel thisChan = (Channel) u;
                thisChan.highpass(freq, false);
            }

            return this;
        }
    }
     /**Return a new ChannelSet (with -actual copies- of underlying points) that is 
     * a bandpass filtered version of each of the channels in this dataset. 
     */
    public ChannelSet bandpass(float low, float high, boolean copy) throws Exception{
        if (copy) {
            ChannelSet cs = getCopy(this.id + "bandpass"+low+high);

            for(UnidimensionalLayer u : streams) {
                Channel thisChan = (Channel) u;
                Channel sc = thisChan.bandpass(low,high,true);
                cs.addStream(sc);
            }

            return cs;
        }
        else {
            for(UnidimensionalLayer u : streams) {
                Channel thisChan = (Channel) u;
                thisChan.bandpass(low,high, false);
            }

            return this;
        }
    }
    
    public ChannelSet bwBandpass(int order, float low, float high) throws Exception{
         for(UnidimensionalLayer u : streams) {
                Channel thisChan = (Channel) u;
                thisChan.bwBandpass(order, low, high);
         }
         
         return this;
    }
    
    private ChannelSet zScore(boolean copy) throws Exception{
       if (copy) throw new Exception("copy=true not yet implemented");
       else {
            for(UnidimensionalLayer u : streams) {
                Channel thisChan = (Channel) u;
                thisChan.zScore(false);
            }

            return this;
        }
    }
    
    
    public ChannelSet detrend(ChannelSet baseline, double maxDiff, boolean copy) throws Exception{
         if (copy) {
            ChannelSet cs = getCopy(this.id + "detrend");

            int index =0;
            for(Channel c : streams) {
                Channel dc = c.detrend(baseline.getChannel(index).getData(),maxDiff, copy);
                cs.addStream(dc);
                index++;
            }

            return cs;
        }
        else {
            int index = 0;
            for(Channel c : streams) {
                c.detrend(baseline.getChannel(index).getData(),maxDiff,  false);
                index++;
            }
            return this;
        }
    }
    

    /**Apply the Techniques listed in the technique set to the ChannelSet.
     * Return a copy, with fresh points if copy = true
     */
    public ChannelSet manipulate(TechniqueSet ts, boolean copy) throws Exception{
        ChannelSet retSet = this; //.. will be set to something else if copy = true
       
        if (ts.getFilter().filterType == PassFilter.FilterType.BandPass) 
            retSet = this.bandpass((float)ts.getFilter().lowPass,(float) ts.getFilter().highPass, copy);
        
        if (ts.getFilter().filterType == PassFilter.FilterType.LowPass) 
           retSet = this.lowpass((float)ts.getFilter().pass, copy);

        if (ts.getFilter().filterType == PassFilter.FilterType.HighPass) 
           retSet = this.highpass((float)ts.getFilter().pass, copy);
        
        if (ts.getFilter().filterType == PassFilter.FilterType.bwBandPass) 
           retSet = this.bwBandpass(ts.getFilter().order, (float)ts.getFilter().lowPass, (float)ts.getFilter().highPass);
        
        if (ts.getTransformation().type == Transformation.TransformationType.ZScore)
            retSet = this.zScore(copy);
        
        if (ts.getTransformation().type == Transformation.TransformationType.MovingAverage)
            retSet = this.movingAverage(ts.getTransformation().mAReadingsBack, copy);
        
        return retSet;
    }

    /**A set of start-end pairings describing where what condition is where*/
    public void addMarkers(Markers m) {
        if (markers == null) markers = new ArrayList();
        markers.add(m);
    }
    
    public Markers getMarkersWithName(String name) throws Exception{
        for (Markers m : markers) {
            if (m.name.equals(name)) return m;
        }
        for(Markers m : markers) {
            System.out.println(m.name);
        }
        throw new Exception(name + " does not exist. " + " There are " + markers.size());
    }
    
    /**Creates an Experiment out of a collection of channels by
     iterating through the channels, and splitting by the labels. First creates
     * 1D Instances channels, then combines 1D channels that share a channel,
     * creating a proper experiment
     */
    public Experiment splitByLabel(String labelName) throws Exception{
        if (markers ==null) throw new Exception("Must have markers to know where to split");
        labelName = labelName.toLowerCase();
        Markers relevantMarkers = getMarkersWithName(labelName);
        Experiment experiment = new Experiment(this.id, relevantMarkers.getClassification());

        //.. Build a new Channel with new points for each trial
        for (Trial t : relevantMarkers.trials) {
            Instance instance = new Instance(t.name, t.start, t.end);

            for (Channel c : streams) {
                Channel subChannel = c.getSample(t.start, t.end);
                instance.addStream(subChannel);
            }
            experiment.addMatrix(instance);
        }
        
        return experiment;
    }
  
    /**Return two-part array, first is Experiment second is ChannelSet, the idea
     being that you'll train the data on Experiment and then test on moving stream,
     * that is the remaining part. 
     * start = the start of where you want your channel set, end = the end of where you want channelset
     */
    public Tuple<Experiment,ChannelSet> getExperimentAndStream(String condition, int start, int end) throws Exception{
        Experiment e = this.splitByLabel(condition);
        e.removeInstancesInRange(start, end);
        
        //.. get a stream which only has the data we took out of the experiment
        ChannelSet stream = this.getChannelSetBetween(start, end);
        return new Tuple(e, stream);
    }
    
    /**Return a new channelset of this layer between start and end**/
    public ChannelSet getChannelSetBetween(int start, int end) throws Exception{
        if (start > end || end > this.getFirstChannel().numPoints) throw new Exception ("Invalid arguments");
        ChannelSet cs = this.getCopy(start, end);
        
        for (Channel c : this.streams) {
            cs.addStream(c.getSample(start, end));
        }
        return cs;
    }
    
    /** Return a pair of Stream + Experiment for multiple partitions of the channelset.
     * Each stream is -length- so the number of partitions will be channelLength / length
     **/
    public Tuple[] getExperimentAndStreamSet(int length, String condition) throws Exception{
        int numPartitions = super.minPoints() / length;
        Tuple [] retPairs = new Tuple[numPartitions]; //.. first layer is number of distinct layers, second is just pair of stream and experiment
        
        int index =0; //.. where the stream should start (and what should be omitted from expierment)
        
        //.. make a new pairing for each partition
        for (int i = 0; i < retPairs.length; i++) {
            int end = index +length;
            Tuple t = this.getExperimentAndStream(condition, index, end);
            retPairs[i] =t;
            index+=length;
        }
        return retPairs;
    }
    public static ChannelSet generate(int numChannels, int numReadings) {
        ChannelSet cs = new ChannelSet();
        for (int i = 0; i< numChannels; i++) {
            Channel c = Channel.generate(numReadings);
            cs.addStream(c);
        }
        return cs;
    }
    
    /**Return an instance between start and end.
     if markerStart= start, then the channelset hasn't been manipulated with at all.
     if its different then we use this info to find what condition we are from the markers
     **/
    public Instance getInstance(String conditionName, int start, int end) throws Exception{
        //... However, all of this instance won't uniquely refer to one instance.
        ///... so we need to express it as a percentage
        ChannelSet sample = this.getChannelSetBetween(start, end);

        //.. get the majority condition between start and end as a tuple, where 2nd part is pctange
        Markers theseMarkers = getMarkersWithName(conditionName);
      
        //.. We might be offset relative to the markers file so correct based on realstar
        int markerStart = start + getRealStart();
        int markerEnd = getRealStart()+end;
        Tuple<String, Double> condTuple = theseMarkers.getConditionBetween(markerStart, markerEnd); 
        Instance instance = new Instance(condTuple.x, start, end, condTuple.y);

        for (Channel c : sample.streams) {
            instance.addStream(c);
        }
        
        return instance;
    }
    
    /**Return an Experiment which, unlike the ordinary splitByLabel method, is along a moving
     bad without clear delineations between instances. In many uses of this class, there will be 
     only one instance inside and it will have a a majority class **/
    public Experiment getMovingExperiment(Classification c, int instanceLength, int everyK) throws Exception{
        ArrayList<Instance> myInstances = new ArrayList();
        //.. starting at instanceLength and advancing by everyK, build a new Instance and classify it
        for (int i = instanceLength; i < this.minPoints(); i += everyK) {
            int start = i - instanceLength, end = i;
            //.. get Instance between i and instancelength back
            Instance myInstance = this.getInstance(c.name, start, end);
            if( c.hasCondition(myInstance.condition))
                myInstances.add(myInstance);
        }
        return new Experiment("testingStream", c, myInstances);
    }
    
    public static void main(String [] args) {
        try{
            int numChannels = 1;
            int numReadings =40;
            ChannelSet cs = ChannelSet.generate(numChannels,numReadings);
            Markers markers = Markers.generate(5, 8);
            cs.addMarkers(markers);

            int TEST = 3;
            
            if (TEST ==0){
                Experiment e = cs.splitByLabel(markers.name);
                e.printStream();
            }
            
            if(TEST ==1) {
                Experiment e = cs.splitByLabel(markers.name);
                Tuple<Experiment,ChannelSet> dl = cs.getExperimentAndStream(markers.name, 5, 12);
                Experiment e2 = dl.x;
                e2.printStream();
                ChannelSet stream = dl.y;
                stream.printStream();
            }
            
            if (TEST ==2) {
                Tuple<Experiment,ChannelSet>[] dl = cs.getExperimentAndStreamSet(5, markers.name); 
                for (int i = 0; i < dl.length; i++) {
                    Experiment e  = dl[i].x;
                    ChannelSet stream =  dl[i].y;
                    e.printInfo();
                    stream.printInfo();
                    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
                }
            }
            
            //.. test getting an instance out of an arbitrary point of a channel set
            if (TEST ==3) {
                markers.printTrials();
                Instance i = cs.getInstance(markers.name, 0, 15);
            }
        }
        catch(Exception e) {e.printStackTrace();}
    }

    public void printInfo() {
        System.out.println("There are " + this.getChannelCount() + " channels with at most " + this.maxPoints());
        if (markers!= null && markers.size()>0) {
            Markers first = markers.get(0);
            System.out.println("    Found " + markers.size() + " markers with realStart at " + getRealStart() + " and end at " + getRealEnd());
            Tuple<String, Double> majority = first.getConditionBetween(getRealStart(), getRealEnd());
            System.out.println("    Majority condition on " + first.name + " is " + majority.x + " with " + majority.y);
        }
    }

 
    
    public static class Tuple<X, Y> {

        public final X x;
        public final Y y;

        public Tuple(X x, Y y) {
            this.x = x;
            this.y = y;
        }
    }
 }
