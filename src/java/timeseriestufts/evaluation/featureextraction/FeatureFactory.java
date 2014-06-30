/*What we're doing here is building one class for feeature building and one class for feature extraction. 
 * Right now, I'm deliberating over whetehr to place nominal in biulder first, or shuving it all in extractor
 */
package timeseriestufts.evaluation.featureextraction;

import java.util.ArrayList;
import java.util.Collection;
import timeseriestufts.evaluatable.FeatureDescription;
import timeseriestufts.evaluatable.FeatureDescription.FSDataLayer;
import timeseriestufts.evaluatable.FeatureDescription.FSTimeWindow;
import timeseriestufts.evaluatable.FeatureSet;
import timeseriestufts.evaluatable.TechniqueSet;
import timeseriestufts.kth.streams.DataLayer;
import timeseriestufts.kth.streams.bi.BidimensionalLayer;
import timeseriestufts.kth.streams.bi.ChannelSet;
import timeseriestufts.kth.streams.bi.Instance;
import timeseriestufts.kth.streams.uni.Channel;
import timeseriestufts.kth.streams.uni.UnidimensionalLayer;
import timeseriestufts.kth.streams.tri.Experiment;


/** 
 * The Feature Factory takes an Instance and a collection of feature descriptions
 * and generates the corresponding set of attributes.
 **/
public class FeatureFactory {    
    public Attributes attributes;
    FeatureSet featureSet;
    Instance instance;
    
    /** Given an instance and a description of features, extract those features */
    public FeatureFactory(Instance instance, FeatureSet fs)  {
        this.instance = instance;
        featureSet = fs;
    }
    
    /**Generate Attribute array from feature descriptions. 
     */
    public Attributes generateAttributes() throws Exception{  
        if (featureSet.featureDescriptions == null) throw new Exception("No features have been added. "
                + "Add features by highlighting the feature object and writing addfeatures(*,*,*) in the console. ");
        Collection<FeatureDescription> descriptions =  featureSet.featureDescriptions.values();
        attributes = new Attributes();

        //.. Create an attribute for each description (Note we may have more attribtues than description
        //... since we didn't know how many channels the dataset had when we made the descriptions
        for(FeatureDescription fd : descriptions) {
            //.. 1) Determine what channel we're refering to
            DataLayer d = determineDataLayer(fd.datalayer);
            ArrayList<UnidimensionalLayer> channels = new ArrayList();
            
            //.. if we returned a set of channels, make a new attribtue for each
            if (d instanceof BidimensionalLayer) {
                BidimensionalLayer bd = (BidimensionalLayer) d;
                channels = bd.streams;
            }
            //.. add just a single channel
            else if (d instanceof UnidimensionalLayer) { //.. must be a UnidimesionalLayer
                channels.add((UnidimensionalLayer)d);
            }
            
            //.. slice out correct timewindow and add statistic for each
            for (UnidimensionalLayer u : channels) {
                Channel c = (Channel)u;
               
                //.. 2) Further restrict our datalayer by determining what window we're refering to
                c  = determineWindow(fd.window, c); 
                Attribute attribute;
                //..3) Finally create the attribute and add it
                if (fd.statistic.isNumeric())
                     attribute = new NumericAttribute(c, fd.statistic, fd.window.getTimeString());
                else
                    attribute = new SAXAttribute(c,fd.window.getTimeString(), fd.statistic.getAlphaLength(), fd.statistic.getNumLetters());
                attributes.addAttribute(attribute);
            }
        }
        return attributes;             
    }
    
    /**Extract the correct window from a datalayer
     * Slight complexity in that windows are timestamped according to where they fell in original dataset
     */
    private Channel determineWindow(FSTimeWindow ftw, Channel channel) throws Exception {
        Channel retLayer =null;
        int start = 0;
        int end = start + (int) channel.getCount(); 
        int mid =  (int) (start + channel.getCount() /2.0);
        switch (ftw.timewindow) {
             case WHOLE:
                  retLayer = channel;
                  break;
             case FIRSTHALF:
                  retLayer =   channel.getSample(start, mid);
                  break;
             case SECONDHALF:
                  retLayer =  channel.getSample(mid, end);
                  break;
             case SPECIFIC:
                 int startTS = ftw.startTS +start;
                 int endTS = ftw.endTS /*+ end*/;
                 retLayer =  channel.getSample(startTS, endTS);
                 break;
                 
             case PERCENTAGE :
                  startTS = start + (int)(ftw.startPct * channel.getCount());
                 endTS = start + (int)(ftw.endPct * channel.getCount());
                 retLayer = channel.getSample(startTS, endTS);
                 break;
        }
        
        return retLayer;
    }
    
    /**Extract an actual datalayer given a description of it.
     Implement: merge and between*/
    private DataLayer determineDataLayer(FSDataLayer fdl) throws Exception{
        DataLayer retLayer =null;
        switch (fdl.type) {
            case ALL:
                retLayer = this.instance; //.. don't filter out any
                break;
            case MERGED: 
                retLayer = mergeLayers(fdl.channels);
                break;
            case SEVERAL: 
                retLayer = this.instance.removeAllChannelsExceptBetween(fdl.channel, fdl.endChannel);
                break;
            case SINGLETON: 
                try{
                    retLayer = this.instance.getChannelById(fdl.channel);
                }
                //.. if it didn't have a channel and this is an integer, retrieve it by index
                catch (Exception e) {
                    int channelIndex;
                    try {
                        channelIndex = Integer.parseInt(fdl.channel); 
                         retLayer = this.instance.getChannel(channelIndex);
                    }
                    catch (Exception f){ 
                        //.. throw the above exception. this was just experimental
                        throw new Exception(e.getMessage());
                    }
                }
                break;
        }
        if (retLayer == null) throw new Exception ("Cannot extract the pointed to datalayer");
        return retLayer;
    }
    
    
    
    /**Given a set of channel id's (or indexes if they're numbers and those number's dont
     exist as id's), merge these into a single datalayer (an InstanceChannel)*/
    private Channel mergeLayers(String [] channels) throws Exception {
        ArrayList<Channel> ics = new ArrayList(); 
        String id ="";
        int maxPoints = 0;
        //.. retrieve the specified channels
        for (String s : channels) {
            Channel ic = instance.getChannelById(s);
            
            //.. keep track of the largest one, so that we allocate enough space
            if (ic.getCount() > maxPoints) maxPoints = ic.getCount();
            
            //.. add to the collection that will be merged
            ics.add(ic);
            id += s;
        }
       
        Channel mergedChannel = new Channel(1, maxPoints);
        mergedChannel.id = id;
        
        //.. merge the channels
        for (Channel ic : ics) {
            mergedChannel.merge(ic);
        }
        
        return mergedChannel;
        
    }
    
    
    public static void main(String []args ) {
        Instance instance = Instance.generate(5,10);
        Experiment exp = Experiment.generate(2, 3, 10);
       // instance.printStream();
        FeatureSet fs = new FeatureSet("test");
        try{
            int TEST =4;
            if (TEST ==0){
                fs.addFeaturesFromConsole("mean", "0", "SECONDHALF");
                FeatureFactory ff = new FeatureFactory(instance, fs);
                ff.generateAttributes();
                ff.attributes.extract();
                ff.attributes.printAttributes();
            }
            if (TEST ==1) {
                fs.addFeaturesFromConsole("sax-2-2", "*", "WHOLE");
                FeatureFactory ff = new FeatureFactory(instance, fs);
                ff.generateAttributes();
                ff.attributes.printAttributes();
            }
            if (TEST ==2 ) {
                fs.addFeaturesFromConsole("sax-2-2", "*", "WHOLE");
                exp.extractAttributes(fs);
                
                exp.writeToArff("FFTest.arff");
                
            }
            if (TEST == 3) {
                fs.addFeaturesFromConsole("sax-abbb", "*", "WHOLE");
                exp.extractAttributes(fs);

                exp.writeToArff("FFTest.arff");

            }
            if (TEST ==4) {
                fs.addFeaturesFromConsole("mean", "*", "0.0%0.9");
                FeatureFactory ff = new FeatureFactory(instance, fs);
                ff.generateAttributes();
                ff.attributes.extract();
                ff.attributes.printAttributes();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
