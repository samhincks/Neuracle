/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeseriestufts.kth.streams.bi;

import filereader.Labels;
import filereader.Markers;
import filereader.Markers.Trial;
import filereader.experiments.Beste;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import timeseriestufts.evaluatable.FeatureDescription;
import timeseriestufts.evaluatable.PassFilter;
import timeseriestufts.evaluatable.TechniqueSet;
import timeseriestufts.evaluatable.Transformation;
import timeseriestufts.evaluatable.Transformations;
import timeseriestufts.evaluation.experiment.Classification;
import timeseriestufts.kth.streams.DataLayer;
import timeseriestufts.kth.streams.bi.BidimensionalLayer;
import timeseriestufts.kth.streams.tri.Experiment;
import timeseriestufts.kth.streams.uni.*;
import timeseriestufts.kth.streams.uni.Channel;
import timeseriestufts.kth.streams.uni.UnidimensionalLayer;

/**An fNIRS channel, but can be repurposed for other sensors
 * @author Sam
 */
public class ChannelSet extends BidimensionalLayer<Channel>{
    public ArrayList<Markers> markers = new ArrayList(); //.. markers describing where each condition starts and ends
    
    public Integer realEnd; //.. its position in a former layer. 
    public Integer realStart;//... If we're setting these its derived from another layer
    public float readingsPerSecond;
    public boolean test = false;//.. set to true if we fabricated this as a sample -- will change display message
    public Transformations transformations;
      
    public ChannelSet() {
        streams = new ArrayList();
        this.readingsPerSecond = Channel.HitachiRPS; 
    }
    
    /**Initialize Unidimensional Array and unidimensional stat representation. 
     */
    public ChannelSet(float readingsPerSecond) {
        streams = new ArrayList();
        this.readingsPerSecond = readingsPerSecond; 
    }
    
    /**
     * Initialize Unidimensional Array and unidimensional stat representation.
     */
    public ChannelSet(int realStart, int realEnd) {
        streams = new ArrayList();
        this.realStart = realStart;
        this.realEnd = realEnd;
        this.readingsPerSecond = Channel.HitachiRPS; //.. HACK
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
      
    /***Return a simple copy with a new name*/
    public ChannelSet getCopy(String id) throws Exception{
        ChannelSet cs = new ChannelSet();
        cs.setId(id);
        if (markers != null){ 
            for (Markers m : markers){
                cs.addMarkers(m);
            }
        }
        if (this.transformations!= null)
            cs.transformations = this.transformations.getCopy();
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
        if (this.transformations!= null)
            cs.transformations = this.transformations.getCopy();
        return cs;
    }
    
    /*Return markers with specified name*/
    public Markers getMarkersWithName(String name) throws Exception {
        for (Markers m : markers) {
            if (m.name.equals(name)) {
                return m;
            }
        }
        for (Markers m : markers) {
            System.out.println(m.name);
        }
        throw new Exception(name + " does not exist. " + " There are " + markers.size());
    }
    /**
     * A set of start-end pairings describing where what condition is where
     */
    public void addMarkers(Markers m) {
        if (markers == null) {
            markers = new ArrayList();
        }
        markers.add(m);
    }

    public void addOrReplaceMarkers(Markers m) {
        if (hasMarkersWithName(m.name)) {
            removeMarkerWithName(m.name);
            addMarkers(m);
        } else {
            addMarkers(m);
        }
    }

    public void removeMarkerWithName(String name) {
        for (int i = 0; i < markers.size(); i++) {
            Markers m = markers.get(i);
            if (m.name.equals(name)) {
                markers.remove(i);
            }
        }
    }

    public boolean hasMarkersWithName(String name) {
        if (markers == null) {
            return false;
        }
        for (Markers m : markers) {
            if (m.name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates an Experiment out of a collection of channels by iterating
     * through the channels, and splitting by the labels. First creates 1D
     * Instances channels, then combines 1D channels that share a channel,
     * creating a proper experiment
     */
    public Experiment splitByLabel(String labelName) throws Exception {
        if (markers == null) {
            throw new Exception("Must have markers to know where to split");
        }
        labelName = labelName.toLowerCase();
        Markers relevantMarkers = getMarkersWithName(labelName);
        Experiment experiment = new Experiment(this.id, relevantMarkers.getClassification(), this.readingsPerSecond);
        experiment.transformations = this.transformations;
        //.. Build a new Channel with new points for each trial
        for (Trial t : relevantMarkers.trials) {
            if (t.end - t.start > 2) {
                Instance instance = new Instance(t.name, t.start, t.end);

                for (Channel c : streams) {
                    Channel subChannel = c.getSample(t.start, t.end, false);
                    instance.addStream(subChannel);
                }
                experiment.addMatrix(instance);
            }
        }

        return experiment;
    }

    public ArrayList<ChannelSet> partitionByLabel(String labelName) throws Exception {
        if (markers == null) {
            throw new Exception("Must have markers to know where to split");
        }
        labelName = labelName.toLowerCase();
        Markers relevantMarkers = getMarkersWithName(labelName);
        ArrayList<ChannelSet> retSets = new ArrayList();
        //.. Build a new Channel with new points for each trial
        for (Trial t : relevantMarkers.trials) {
            ChannelSet cs = new ChannelSet(t.start, t.end);
            cs.transformations = this.transformations;
            
            //.. copy over each channel
            for (Channel c : streams) {
                Channel subChannel = c.getSample(t.start, t.end, false);
                cs.addStream(subChannel);
            }
            retSets.add(cs);
        }
 
        return retSets;
    }
    
    /**
     * Return two-part array, first is Experiment second is ChannelSet, the idea
     * being that you'll train the data on Experiment and then test on moving
     * stream, that is the remaining part. start = the start of where you want
     * your channel set, end = the end of where you want channelset
     */
    public Tuple<Experiment, ChannelSet> getExperimentAndStream(String condition, int start, int end) throws Exception {
        Experiment e = this.splitByLabel(condition);
        e.removeInstancesInRange(start, end);

        //.. get a stream which only has the data we took out of the experiment
        ChannelSet stream = this.getChannelSetBetween(start, end);
        return new Tuple(e, stream);
    }

    /**
     * Return a new channelset of this layer between start and end*
     */
    public ChannelSet getChannelSetBetween(int start, int end) throws Exception {
        if (start > end || end > this.getFirstChannel().numPoints) {
            throw new Exception("ChannelSet.getChannelSetBetween(): Invalid arguments. The first channel has  " + this.getFirstChannel().numPoints + ". You have requested " + start + " , " + end);
        }
        ChannelSet cs = this.getCopy(start, end);

        for (Channel c : this.streams) {
            cs.addStream(c.getSample(start, end, false));
        }
        return cs;
    }

    /**
     * Return a pair of Stream + Experiment for multiple partitions of the
     * channelset. Each stream is -length- so the number of partitions will be
     * channelLength / length
     *
     */
    public Tuple[] getExperimentAndStreamSet(int length, String condition) throws Exception {
        int numPartitions = super.minPoints() / length;
        Tuple[] retPairs = new Tuple[numPartitions]; //.. first layer is number of distinct layers, second is just pair of stream and experiment

        int index = 0; //.. where the stream should start (and what should be omitted from expierment)

        //.. make a new pairing for each partition
        for (int i = 0; i < retPairs.length; i++) {
            int end = index + length;
            Tuple t = this.getExperimentAndStream(condition, index, end);
            retPairs[i] = t;
            index += length;
        }
        return retPairs;
    }

    public static ChannelSet generate(int numChannels, int numReadings) {
        ChannelSet cs = new ChannelSet();
        for (int i = 0; i < numChannels; i++) {
            Channel c = Channel.generate(numReadings);
            cs.addStream(c);
        }
        return cs;
    }

    /**
     * Get condition between start and end, and set its value to conditionValue
     *
     * @param start
     * @param end
     * @param conditionValue
     * @return
     * @throws Exception
     */
    public Instance getInstance(int start, int end, String conditionValue) throws Exception {
        Instance instance = new Instance(conditionValue, start, end); //.. it has no condition, for instance if unlabeled data
        ChannelSet sample = this.getChannelSetBetween(start, end);

        for (Channel c : sample.streams) {
            instance.addStream(c);
        }
        return instance;
    }

    /**
     * Return an instance between start and end. if markerStart= start, then the
     * channelset hasn't been manipulated with at all. if its different then we
     * use this info to find what condition we are from the markers
     *
     */
    public Instance getInstance(String conditionName, int start, int end) throws Exception {
        //... However, all of this instance won't uniquely refer to one instance.
        ///... so we need to express it as a percentage
        ChannelSet sample = this.getChannelSetBetween(start, end);
        Instance instance;
        if (!(this.markers == null || this.markers.isEmpty()) && this.hasMarkersWithName(conditionName)) {
            //.. get the majority condition between start and end as a tuple, where 2nd part is pctange
            Markers theseMarkers = getMarkersWithName(conditionName);

            //.. We might be offset relative to the markers file so correct based on realstar
            int markerStart = start + getRealStart();
            int markerEnd = getRealStart() + end;
            Tuple<String, Double> condTuple = theseMarkers.getConditionBetween(markerStart, markerEnd);
            instance = new Instance(condTuple.x, start, end, condTuple.y);
        } else {
            instance = new Instance(null, start, end); //.. it has no condition, for instance if unlabeled data
        }
        for (Channel c : sample.streams) {
            instance.addStream(c);
        }

        return instance;
    }

    /**
     * Return an Experiment which, unlike the ordinary splitByLabel method, is
     * along a moving bad without clear delineations between instances. In many
     * uses of this class, there will be only one instance inside and it will
     * have a a majority class *
     */
    public Experiment getMovingExperiment(Classification c, int instanceLength, int everyK, boolean getEvery) throws Exception {
        ArrayList<Instance> myInstances = new ArrayList();
        //.. starting at instanceLength and advancing by everyK, build a new Instance and classify it
        for (int i = instanceLength; i < this.minPoints(); i += everyK) {
            int start = i - instanceLength, end = i;

            //.. get Instance between i and instancelength back
            Instance myInstance = this.getInstance (c.name, start, end);
            if (myInstance.condition == null ||getEvery || c.hasCondition(myInstance.condition)) {
                myInstances.add(myInstance);
            }
        }  
        return new Experiment("testingStream", c, myInstances, this.readingsPerSecond);
    }
    /**
     * Write out the data in its current format to a csv file
     * @param writeEvery : compress the file by making this larger
     */
    public void writeToFile(String filename, int writeEvery, boolean labelToInt) throws Exception {
        if (writeEvery < 1) throw new Exception("must write at least 1");
        File f = new File(filename);
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        int numPoints = this.getMinPoints();
        //.. Write out the column ids
        for (int i = 0; i < streams.size(); i++) {
            bw.write(streams.get(i).id);
            if (i != streams.size() - 1) {
                bw.write(",");
            }
        }

        //.. and then the marker ids
        if (!(markers.isEmpty())) {
            bw.write(",");
        }
        for (int j = 0; j < markers.size(); j += writeEvery) {
            Markers m = markers.get(j);
            bw.write(m.name);
            if (j != markers.size() - 1) {
                bw.write(",");
            }
        }
        bw.write("\n");

        //.. Write out each point, but omit points if specified
        for (int i = 0; i < numPoints; i += writeEvery) {
            //.. write out each column
            for (int j = 0; j < streams.size(); j++) {
                bw.write(String.valueOf(streams.get(j).getPoint(i)));
                if (j != streams.size() - 1 || (markers != null)) {
                    bw.write(",");
                }
            }

            //.. write out the label
            for (int j = 0; j < markers.size(); j += writeEvery) {
                Markers m = markers.get(j);
                Labels l = m.saveLabels;

                //.. a fixable error that occurs if we generate markers through generate
                if (m.saveLabels == null) {
                    throw new Exception("The way markers were instantiated does not permit writing file currently");
                }
                if (l.channelLabels.size() > numPoints) {
                    throw new Exception("NumPoints = " + numPoints
                            + " Labels = " + l.channelLabels.size() + " . They are misaligned");
                }

                if (i >= l.channelLabels.size()) {
                    bw.write("-1");
                } //.. write out the actual label value
                else if (!(labelToInt)) {
                    bw.write(l.channelLabels.get(i).value);
                } //.. write out a numeric representation, sometimes convenient for visualization
                else {
                    String conName = l.channelLabels.get(i).value;
                    String index = String.valueOf(m.getClassification().getIndex(conName));
                    bw.write(index);
                }
                if (j != markers.size() - 1) {
                    bw.write(",");
                }
            }
            bw.write("\n");
        }
        bw.close();
    }

    /** Append one chanset to the end of the other **/
    public void appendChanSet(ChannelSet cs2) throws Exception {
        if (cs2.streams.size() != this.streams.size()) {
            throw new Exception("Both channels must have as many channels");
        }
        for (int i = 0; i < this.streams.size(); i++) {
            Channel a = this.streams.get(i);
            Channel b = cs2.streams.get(i);
            a.append(b);
        }
        if (this.markers.size() != cs2.markers.size()) {
            throw new Exception("Both channelsets must have as many markers");
        }
        for (int i = 0; i < this.markers.size(); i++) {
            Labels l1 = this.markers.get(i).saveLabels;
            Labels l2 = cs2.markers.get(i).saveLabels;
            l1.append(l2);
            Markers m = new Markers(l1);
            this.markers.set(i, m); //.. replace old markers with new ones
        }

    }

    /* Return a deep copy of the channel set where the actual points have been copied*/
    public ChannelSet getDeepCopy() {
        ChannelSet cs = new ChannelSet(this.readingsPerSecond);
        for (Channel c : streams) {
            cs.addStream(c.getCopy());
        }
        ArrayList<Markers> m2 = new ArrayList();
        for (Markers m : markers) {
            Markers mc = m.getCopy();
            m2.add(mc);
        }
        cs.markers = m2;
        return cs;
    }

    /* ----------------------------------------------------------------------
              ALL DATA MANIPULATION METHODS CAN BE FOUND BELOW THIS LINE
    -------------------------------------------------------------------------
    */
    
    
    public ChannelSet removeFirst(int readingsBack, boolean copy) throws Exception {
         if (copy) {
            ChannelSet cs = getCopy(this.id + "removefirst" + readingsBack);
            for (UnidimensionalLayer u : streams) {
                Channel thisChan = (Channel) u;
                Channel sc = thisChan.removeFirst(readingsBack, true);
                cs.addStream(sc);
            }
            return cs;
        } else {
            for (UnidimensionalLayer u : streams) {
                Channel thisChan = (Channel) u;
                thisChan.removeFirst(readingsBack, false);
            }
            return this;
        }
    }
    /**
     * Return a new ChannelSet (with -actual copies- of underlying points) that
     * is a moving average of each of the channels in this dataset. None of the
     * data in this datalayer is altered. A moving average smooths out
     * short-term fluctations and highlights longer term trends. It is a type of
     * low-pass filter. Specifically, a moving average computes a new sequence
     * of values where each value represents the mean at n readings back.
     */
    public ChannelSet movingAverage(int readingsBack, boolean copy) throws Exception {
        if (copy) {
            ChannelSet cs = getCopy(this.id + "movingAverage" + readingsBack);
            for (UnidimensionalLayer u : streams) {
                Channel thisChan = (Channel) u;
                Channel sc = thisChan.movingAverage(readingsBack, true);
                cs.addStream(sc);
            }
            return cs;
        } else {
            for (UnidimensionalLayer u : streams) {
                Channel thisChan = (Channel) u;
                thisChan.movingAverage(readingsBack, false);
            }
            return this;
        }
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
    
    public ChannelSet normalize(boolean copy) throws Exception {
        if (copy) {
            ChannelSet cs = getCopy(this.id + "zscore");

            for (Channel c : streams) {
                Channel c2 = c.normalize(true);
                cs.addStream(c2);
            }

            return cs;
        } else {
            for (UnidimensionalLayer u : streams) {
                Channel thisChan = (Channel) u;
                thisChan.normalize(false);
            }
            return this;
        }
    }
    
    public ChannelSet zScore(boolean copy) throws Exception{
       if (copy) {
           ChannelSet cs = getCopy(this.id + "zscore");

           for (Channel c : streams) {
               Channel c2 =c.zScore(true);
               cs.addStream(c2);
           }
           
           return cs;
       }
       else {
            for(UnidimensionalLayer u : streams) {
                Channel thisChan = (Channel) u;
                thisChan.zScore(false);
            }
            return this;
        }
    }
    
   
    /**
     * Return a new ChannelSet where each channel is anchored to zero if second
     * parameter is null
     */
    public ChannelSet anchor(boolean copy, Float firstPoint) throws Exception {
        //.. if we want a new raw points
        if (copy) {
            ChannelSet cs = getCopy(this.id + "anchor");
            //.. apply anchor to each channel
            for (Channel c : streams) {
                Channel newC = c.anchor(true, firstPoint);
                cs.addStream(newC);
            }
            return cs;
        } //.. if we want to conserve memory and manipulate this dataset directly
        else {
            //.. apply anchor to each channel
            for (Channel c : streams) {
                c.anchor(false, firstPoint);
            }
            return this;
        }
    }
    
    /**
     * Return a new ChannelSet where each channel is anchored to zero if second
     * parameter is null
     */
    public ChannelSet trimFirst(int firstPoint) throws Exception {
        ChannelSet cs = getCopy(this.id + "trim");
        //.. apply anchor to each channel
        for (Channel c : streams) {
            Channel newC = c.getSample(firstPoint, c.numPoints, false);
            cs.addStream(newC);
        }
        return cs;
        
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
        
        if (ts.getTransformation().type == Transformation.TransformationType.zscore)
            retSet = this.zScore(copy);
        
        if (ts.getTransformation().type == Transformation.TransformationType.calcoxy)
            retSet = this.calcOxy(copy, null, null); //.. set 2nd parameter to empty if 830 comes first
        
        if (ts.getTransformation().type == Transformation.TransformationType.averagedcalcoxy)
            retSet = this.averagedCalcOxy(null, null); //.. set 2nd parameter to empty if 830 comes first
        
        if (ts.getTransformation().type == Transformation.TransformationType.movingaverage)
            retSet = this.movingAverage((int)ts.getTransformation().params[0], copy);
        
        
        
        return retSet;
    }
    
    
    
    
    /** Apply the specified transformation to the channelset, save it to its own Transformations object
     * @param t the transformation to be applied
     * @param copy whether or not to make a copy (not always supported)
     * @return 
     */
    public ChannelSet manipulate(Transformation ts, boolean copy) throws Exception{
        ChannelSet retSet = this; //.. will be set to something else if copy = true
        if (ts.type== Transformation.TransformationType.bandpass) 
            retSet = this.bandpass(((ts.params.length > 0)?ts.params[0] :0.1f), ((ts.params.length > 1)?ts.params[1] :1f), copy);
        
        else if (ts.type== Transformation.TransformationType.lowpass) {
          //  if(!copy)throw new Exception("lowpass currently bugged when copy = false");
            float cutoff = (ts.params.length > 0)?ts.params[0] :0.3f;
            retSet = this.lowpass(cutoff, copy);
            //.. lowpass filters mess with the first n readings, depending on the cutoff. Trim off that many radings
            int trim =0;
            if (cutoff >= 0.5) trim = 15;
            else if (cutoff >= 0.3f) trim = 20;
            else if (cutoff >=0.1f) trim = 46;
           //else throw new Exception("Filtering at frequency " + cutoff + " not yet supported. You must figure out trim values");
            //.. Trim  
            //retSet = retSet.trimFirst((trim));  
        }
            
        else if (ts.type == Transformation.TransformationType.subtractchannel) {
            int subtractIndex = 0;
            int [] fromIndexes = {1,2,3};
            if (ts.params != null && ts.params.length >0) {
                subtractIndex = (int)ts.params[0];
                for (int i = 1; i < ts.params.length; i++) {
                    fromIndexes[i-1] = (int)ts.params[i];
                }
            }
            retSet = this.subtractChannel(subtractIndex, fromIndexes);
        }
           
        
        else if (ts.type== Transformation.TransformationType.highpass) 
            retSet = this.highpass(((ts.params.length > 0)? ts.params[0] :1), copy);
        
        else if (ts.type== Transformation.TransformationType.bwbandpass) 
            retSet = this.bwBandpass((int)ts.params[0],ts.params[1], ts.params[2]);
        
        else if (ts.type== Transformation.TransformationType.zscore) 
            retSet = this.zScore(copy);
        
        else if (ts.type== Transformation.TransformationType.calcoxy) 
            retSet = this.calcOxy(copy, null, null);
        
        else if (ts.type == Transformation.TransformationType.averagedcalcoxy) 
            retSet = this.averagedCalcOxy(null, null);
                
        else if (ts.type == Transformation.TransformationType.anchor) 
            retSet = this.anchor(copy, null);
        
        else if (ts.type == Transformation.TransformationType.removefirst) 
            retSet = this.removeFirst((int) ts.params[0], copy);
        
        else if (ts.type == Transformation.TransformationType.trimfirst) 
            retSet = this.trimFirst((ts.params.length > 0)? (int)ts.params[0] : 100);
         
        else if (ts.type== Transformation.TransformationType.movingaverage) 
            retSet = this.movingAverage(((ts.params.length > 0)? (int)ts.params[0] : 10), copy);
        
        else if (transformations != null) retSet.transformations = transformations.getCopy(); //.. not a huge deal if we actually didnt need the deep copy
        
        //.. save it so that we remember what has been applied
        if (retSet.transformations == null) retSet.transformations = new Transformations();
        retSet.transformations.addTransformation(ts);
          
        //.. set id
        retSet.id = retSet.id + ts.type.name();
        if (ts.params != null) {
            if (ts.params.length >0) retSet.id += ts.params[0];
            if (ts.params.length >1) retSet.id += ts.params[1];
            if (ts.params.length >2) retSet.id += ts.params[2];
        }

        return retSet;
    }
   

    /**
     * Average together sets that correspond to the same measurement but at a different distance, to simplify.Always create a  new channel
     * @param sixNinetyCols
     * @param eightThirtyCols
     * @param copy
     * @return
     */
    public ChannelSet averagedCalcOxy(ArrayList<Integer> sixNinetyCols, ArrayList<Integer> eightThirtyCols) throws Exception{
        int quarter = this.streams.size() / 4;
        
        if (sixNinetyCols == null) {
            sixNinetyCols = this.getImagentChannels(true);
            eightThirtyCols = this.getImagentChannels(false);
        }
        
        //.. Create a channelset with four new channels
        //... a690, a830, b690, b830
        ArrayList<Channel> a690Chans = new ArrayList();
        ArrayList<Channel> b690Chans = new ArrayList();
        ArrayList<Channel> a830Chans = new ArrayList();
        ArrayList<Channel> b830Chans = new ArrayList();
        int index =0;
        
        //.. collect channels 
        for (Integer i : sixNinetyCols) {
            if (index <quarter)
                a690Chans.add(this.streams.get(i));
            else
                b690Chans.add(this.streams.get(i));
            index++;
        }      
        index = 0;  
        for (Integer i : eightThirtyCols) {
            if (index < quarter) 
                a830Chans.add(this.streams.get(i));
            else 
                b830Chans.add(this.streams.get(i));
            index++;
        }
        
        //.. The first appears to give slightly different data, then the others who show the same effect 
        a690Chans.remove(0);
        b690Chans.remove(0);
        a830Chans.remove(0);
        b830Chans.remove(0);

        //.. Why is it 0, 8?
        //.. create channels
        Channel a690 = new Channel(a690Chans);
        Channel b690 = new Channel(b690Chans);
        Channel a830 = new Channel(a830Chans);  
        Channel b830 = new Channel(b830Chans);
        
        ChannelSet cs = new ChannelSet();
        cs.markers = this.markers;
        cs.id = this.id +"averaged";
        
        //.. add streams in the order calc oxy will expect them 
        cs.addStream(a690);
        cs.addStream(a830);
        cs.addStream(b690);
        cs.addStream(b830);
        
        return cs.calcOxy(true, null, null);

    }  
    
    
    /**Get indexes for six ninety and eight thirty cols according to our device.
     Currently, I'm not 100% sure but I think 830 comes before 690*/
    private ArrayList<Integer> getImagentChannels(boolean sixNinety) throws Exception {
        if (this.streams.size() % 2 != 0) 
            throw new Exception("Double check columns, since # not divisible by two");
        
        int half = this.streams.size() / 2;
        int quarter = this.streams.size() / 4;

    
        ArrayList<Integer> ret = new ArrayList(); 
        /**
         * First four are 830, then 690
         */
        for (int i = 0; i < quarter; i++) {
            if (sixNinety)
                ret.add(i + quarter);      
            else ret.add(i);
        }
  
        //.. PROBE B
        for (int i = half; i < half + quarter; i++) {
            if (sixNinety)
                ret.add(i + quarter);
            else ret.add(i);
        }
        
        return ret; 
    }
    
    
    /**Subtract specified channel from the all others. For now, always copy**/
    private ChannelSet subtractChannel(int subtractIndex, int[] fromIndexes) throws Exception{
        String s = this.id +subtractIndex +"";
        ChannelSet cs = getCopy("subtracted");
        Channel c = this.streams.get(subtractIndex);
        cs.streams = this.streams; //.. so actually not a real copy now
        for (int i = 0; i < fromIndexes.length; i++) {
            Channel a = cs.streams.get(fromIndexes[i]);
            a= a.subtract(c, true);
            cs.streams.set(fromIndexes[i], a);
            s+= fromIndexes[i];
        }
        cs.setId(s);
        return cs;
    }
    /**Assume this is fNIRS data, and that the input is the intensity of reflected
     light at DC690 and DC830; apply the modified beer-lambert law to estimate the 
     quantity of oxygen. This amounts to combining corresponinding measurements when
     * the flickered light was at wavelength 690 and 830, multiplying by some constant,
     * and dividing by some other constant. 
     * @param copy : if true, make a deep copy, otherwise modify this
     * @param 690Cols : indexes of 690. if null, assume the first four are 690. if
     * @param 830 : indexes of 830. Same position in these arrays mean they are paired
     **/
    public ChannelSet calcOxy(boolean copy, ArrayList<Integer> sixNinetyCols, ArrayList<Integer> eightThirtyCols) throws Exception{
        if (this.streams.size() % 2 != 0) throw new Exception("Double check columns, since # not divisible by two");
        int half = this.streams.size() / 2;
        int quarter = this.streams.size() / 4;
        
        //.. assume the first ones pertain to sixenty, and that its a mirror
        /**EDIT: The first 4 are 830, the next four are 690. 
         * Then this repeats for probe B. 
         **/
        if (sixNinetyCols == null) {
            sixNinetyCols = this.getImagentChannels(true);
            eightThirtyCols = this.getImagentChannels(false);
        }
         
        
        if (sixNinetyCols.size() != eightThirtyCols.size()) throw new Exception("CALCOXY: There must be as many of both columns");
        
        //.. Constants, set by strokes of scientific genius
        float eo690=0.956f; //.. extinction coefficient of HbO mM^-1xcm^-1 (690 nm)
        float eo830=2.333f; //.. extinction coefficient of HbO mM^-1xcm^-1 (830 nm)
        float ed690=4.854f; //.. extinction coefficient of Hb mM^-1xcm^-1 (690 nm)
        float ed830=1.791f; //.. extinction coefficient of Hb mM^-1xcm^-1 (830 nm)
        float den=eo690*ed830-eo830*ed690;

        if (copy) {
            ChannelSet cs = getCopy(this.id + "calcOxy");
            
            for (int i = 0; i < half; i++) {
                Channel snCol = streams.get(sixNinetyCols.get(i));
                Channel etCol = streams.get(eightThirtyCols.get(i));
                
                int minSize =0;  
                if (snCol.getCount() > etCol.getCount()) minSize = etCol.getCount();
                else minSize = snCol.getCount();
                
                //.. HbO = (abs690*ed830-abs830*ed690)/den*1000; % concentration change (micromolar).
                Channel HbOChan = new Channel(snCol.getFramesize(), snCol.getCount());
                HbOChan.id ="HbO-";
                
                //.. Probe A or B? This is not necessarily generalizable, but its true for our setup
                if (i < quarter) HbOChan.id += "-A" +i;
                else HbOChan.id += "-B" +(i-quarter);
                
                for (int j =0; j < minSize; j++) {
                    float hbO = Math.abs(snCol.getPoint(j)) * ed830 - Math.abs(etCol.getPoint(j))*ed690;
                    hbO = hbO / (den *1000.0f);
                    HbOChan.addPoint(hbO);
                }   
                cs.addStream(HbOChan);    
    
                //.. Hb = (abs830 * eo690 - abs690 * eo830) / den * 1000; % concentration change(micromolar)
                Channel HbChan = new Channel(snCol.getFramesize(), snCol.getCount());
              
                HbChan.id = "Hb-";
                //.. Probe A or B? This is not necessarily generalizable, but its true for our setup
                if (i < quarter) HbChan.id +="A"+ (i);
                else HbChan.id +=  "-B" +(i-quarter);
                
                for (int j = 0; j < minSize; j++) {
                    float hb = Math.abs(etCol.getPoint(j)) * eo690 - Math.abs(snCol.getPoint(j)) * eo830;
                    hb = hb / (den * 1000.0f);
                    HbChan.addPoint(hb);
                }
                cs.addStream(HbChan);
            }

            return cs;
        }
        
        //.. Exact same thing, but not a deep copy
        else {
            
            for (int i = 0; i < half; i++) {
                Channel snCol = streams.get(sixNinetyCols.get(i));
                Channel etCol = streams.get(eightThirtyCols.get(i));

                int minSize = 0;
                if (snCol.getCount() > etCol.getCount()) {
                    minSize = etCol.getCount();
                } else {
                    minSize = snCol.getCount();
                }

                //.. HbO = (abs690*ed830-abs830*ed690)/den*1000; % concentration change (micromolar).
                snCol.id = "HbO-" +i;
                //.. Probe A or B? This is not necessarily generalizable, but its true for our setup
                if (i < quarter) snCol.id +="A" + i;
                else snCol.id += "B" + (i-quarter);
                
                for (int j = 0; j < minSize; j++) {
                    float hbO = Math.abs(snCol.getPoint(j)) * ed830 - Math.abs(etCol.getPoint(j)) * ed690;
                    hbO = hbO / (den * 1000.0f);
                    snCol.setPoint(j,hbO);
                }

                //.. Hb = (abs830 * eo690 - abs690 * eo830) / den * 1000; % concentration change(micromolar)
                etCol.id = "Hb-";
                //.. Probe A or B? This is not necessarily generalizable, but its true for our setup
                if (i < quarter) etCol.id += "-A" +i  ;
                else etCol.id += "-B" +(i-quarter);
                
                for (int j = 0; j < minSize; j++) {
                    float hb = Math.abs(etCol.getPoint(j)) * eo690 - Math.abs(snCol.getPoint(j)) * eo830;
                    hb = hb / (den * 1000.0f);
                    etCol.setPoint(j,hb);
                }
            }
            return this;
        }
    }
    
   
    public static void main(String [] args) {
        try{
            int numChannels = 2;
            int numReadings =30;
            ChannelSet cs = ChannelSet.generate(numChannels,numReadings);
            Markers markers = Markers.generate(3, 10);
            cs.addMarkers(markers);

            int TEST = 9;  
            
            if (TEST ==9) {
                cs = Beste.getChannelSet();
                cs.normalize(false);
                Transformation t = new Transformation(Transformation.TransformationType.subtractchannel);
               // cs.getChannel(1).getSample(100, 1000, true).printStream();
                
                cs.manipulate(t, true);
                cs.getChannel(1).getSample(100,1000, true).printStream();
            }
            //.. TEST CALCULATING OXY
            if (TEST == 8) {  
                cs = Beste.getChannelSet();
                Transformation t = new Transformation(Transformation.TransformationType.calcoxy);

                ChannelSet cs2 = cs.manipulate(t, true);
                cs2.printInfo();
               
                //cs2.printStream();
                cs.writeToFile("output/oxy.csv", 1, false);
            }            
            //.. Test applying and storing transformations, and the protocol for passing these between objects
            if (TEST==7) {
                Transformation t = new Transformation(Transformation.TransformationType.movingaverage);
                cs.manipulate(t, true);
            }
            
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
            
            //.. TEST CALCULATING OXY
            if (TEST ==4) {
                cs = Beste.getChannelSet();
                ChannelSet cs2 = cs.calcOxy(false, null, null);
                //cs2.printStream();
                cs.writeToFile("output/oxy.csv", 1, false);
            }
            if (TEST ==5) {
                cs = Beste.getChannelSet();
                cs.writeToFile("output/test.csv", 30, true);
            }
            if (TEST == 6) {
                markers = Markers.generate(3, 10);
                cs.addOrReplaceMarkers(markers);
                Experiment e = cs.splitByLabel("name");
                e.printInfo();
            }
        }
        catch(Exception e) {e.printStackTrace();}
    }

    public void printInfo() {
        System.out.println("There are " + this.getChannelCount() + " channels with at most " + this.maxPoints());
        String chanString ="";
        for (Channel c : streams)  {chanString += c.id +", ";}
        System.out.println(chanString);    
        if (markers!= null && markers.size()>0) {
            Markers first = markers.get(0);
            System.out.println("    Found " + markers.size() + " markers with realStart at " + getRealStart() + " and end at " + getRealEnd());
            Tuple<String, Double> majority = first.getConditionBetween(getRealStart(), getRealEnd());
            System.out.println("    Majority condition on " + first.name + " is " + majority.x + " with " + majority.y);
        }
        
        for (Channel c : streams ) { 
            System.out.println(c.id + ": " + c.getMean());
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
