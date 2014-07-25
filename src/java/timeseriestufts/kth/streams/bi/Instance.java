/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeseriestufts.kth.streams.bi;

import java.io.BufferedWriter;
import java.util.ArrayList;
import timeseriestufts.evaluatable.FeatureSet;
import timeseriestufts.evaluation.featureextraction.Attribute;
import timeseriestufts.evaluation.featureextraction.Attributes;
import timeseriestufts.evaluation.featureextraction.FeatureFactory;
import timeseriestufts.evaluation.featureextraction.NumericAttribute;
import timeseriestufts.evaluatable.TechniqueSet;
import timeseriestufts.evaluation.experiment.Classification;
import timeseriestufts.evaluation.featureextraction.NominalAttribute;
import timeseriestufts.kth.streams.DataLayer;
import timeseriestufts.kth.streams.uni.Channel;
import timeseriestufts.kth.streams.uni.UnidimensionalLayer;
import weka.core.FastVector;
import weka.core.Instances;



/**A collection of synchronized channels with common start and end points and a common class
 */
public class Instance extends ChannelSet{
    public String condition; //.. label.value
    public Attributes attributes; //.. Holds the attributes pertaining to this channel mean, max etc
    public double conditionPercentage = 1; //.. by default, this is onehundred percent that condition
    public Instance(String condition) {
         this.condition = condition;   
    }
    
    /**Make instance from a line a, b,c,d attributes**/
    public Instance(Attributes attributes, String condition) {
        this.attributes = attributes;
        this.condition = condition;
    }
    
    public Instance(String condition, int realStart, int realEnd) {
         this.condition = condition;   
         this.realStart=realStart;
         this.realEnd= realEnd;
    }
    
    /**Use this constructor for the real-time simulation when an instance
     * is not entirely of one condition**/
    public Instance (String condition, int realStart, int realEnd, double conditionPercentage) {
        this.condition = condition;
        this.realStart = realStart;
        this.realEnd = realEnd;
        this.conditionPercentage = conditionPercentage;
    }
    /**Prepare a set of attributes according to a description; then extract these attributes.
     If they've already been preset don't redo*/
    public void extractAttributes(FeatureSet fs) throws Exception{ 
        if (super.minPoints() < 2) throw new Exception("Unable to extract attributes. "
                + " There needs to be at least 2 readings in every channel " + super.getParent() + 
                " violates this. Condition is " + this.condition);
       // if (attributes != null) return; //.. if they've already been set do nothing
        FeatureFactory ff = new FeatureFactory(this, fs);
        attributes = ff.generateAttributes();
        attributes.extract();
    }

    /**Write out the labels to an arffFile which will be used to mine the data.
     NOTE: this absolutely has to be changed for a robust online applicaiton*/ 
    public void writeLabelLine(BufferedWriter arff) throws Exception{
        if (attributes == null) throw new Exception ("Must Extract attributes before writing label line of instance");
        arff.write("@RELATION fNIRS \n\n");

        for (int i =0; i < attributes.attributeList.size(); i++) {
            Attribute thisAttribute = attributes.attributeList.get(i);                

             // When we implement nominal attributes
            if (thisAttribute instanceof NominalAttribute) {  //.. @ATTRIBUTE NAME {NOMVAL1, NOMVAL2} if nominal
                NominalAttribute nomAttribute = (NominalAttribute) thisAttribute;
                arff.write("@ATTRIBUTE " + nomAttribute.name + " " + nomAttribute.getPossibilitiesString()+ "\n");   
            }

            //.. @ATTRIBUTE NAME NUMERIC, if numeric attribute
            if (thisAttribute instanceof NumericAttribute) { 
                arff.write("@ATTRIBUTE " + thisAttribute.name + " " + thisAttribute.type+ "\n");
            }
       }
    }

    public void writeData(BufferedWriter arff) throws Exception{
        if (attributes == null)  throw new Exception("Must Extract attributes before writing label line of instance");

        //.. Write out all data to buffered writer
        for (Attribute thisAttribute: attributes.attributeList) {
            String output;

            //.. numeric or nom?
            if (thisAttribute instanceof NominalAttribute)
                output = thisAttribute.nomValue;

            else //if (thisAttribute instanceof NumericAttribute)
               output = Double.toString(thisAttribute.numValue);   

            arff.write(output);
            arff.write(", ");
       }
        arff.write(condition);
    }

    /**Remove all channels except channel, endchannel and all those indexed between.
     throw an exception if either channel does not exist.
     it can be either an int or an id*/
    public ChannelSet removeAllChannelsExceptBetween(String firstChannel, String endChannel) throws Exception{
            ChannelSet cs = new ChannelSet();
            int startIndex = this.getChannelIndexById(firstChannel);
            int lastIndex = this.getChannelIndexById(endChannel);
           
            //.. add all points between start and end
            for (int i=startIndex; i <= lastIndex; i++) {
                cs.addStream(this.streams.get(i));
            }
            return cs;
    }

 
    public static Instance generate(int numChannels, int numReadings) {
        Instance instance = new Instance("Test");
        
        for (int i =0; i< numChannels; i++) {
            Channel ic = Channel.generate(numReadings);
            instance.addStream(ic);
        }
        return instance;
    }
    
    
    
    /**Return a new ChannelSet where each channel is anchored to zero
     */
     public Instance anchorToZero(boolean copy) throws Exception {
         //.. if we want a new raw points
         if (copy) {
            Instance instance = new Instance(this.condition);

            //.. apply anchor to each channel
            for(Channel c : streams) {
               Channel newC = c.anchorToZero(true);
               instance.addStream(newC);
           }

           return instance;
         }
         //.. if we want to conserve memory and manipulate this dataset directly
         else {
            //.. apply anchor to each channel
            for(Channel c : streams) {
               c.anchorToZero(false);
            }
            return this;
         }
     }

     /**Return true if any part of this instance overlaps with start and end, that is
      if thisstart > start and thisend <end
      * if thisstart < end and this thisend < start
      */
    public boolean inRange(int start, int end) {
        if (this.realStart >= start && this.realEnd <= end) return true;
        if(this.realEnd <= end && this.realEnd >= start) return true;
        if(this.realStart <= end && this.realEnd >= start) return true;
        return false;
    }
    
    @Override
    public void printStream() {
        System.out.println("Condition: " + condition + " From " + realStart + " to " +realEnd);
        for(Channel c: streams) {c.printStream();}
    }

    public int getNumAttributes() {
        if (attributes==null) return 0;
        return attributes.attributeList.size();
    }

    /**Get weka representation of an instance (denseInstance v sparseInstance?)
     Extract the attributes
     * parameter Instances = the reference to the class that knows about attributes
     **/
    public weka.core.Instance getWekaInstance(Instances dataset) throws Exception{
        if (attributes == null) throw new Exception("Must extract attributes first");
        
        //.. make a new weka instance with num attributes + 1
        int attrsToAdd = getNumAttributes();
        if (condition!=null) attrsToAdd++;
        weka.core.Instance wekaInstance=  new weka.core.Instance(attrsToAdd); //.. +1 for class
       
        //.. set its reference to the dataset
        wekaInstance.setDataset(dataset);
        
        //.. set the value of each attribute
        for (int i = 0; i < attributes.attributeList.size(); i++) {
            Attribute attr = attributes.attributeList.get(i);
            if (attr instanceof NumericAttribute)
                wekaInstance.setValue(i, attr.numValue);
            else if (attr instanceof NominalAttribute)
                wekaInstance.setValue(i, attr.nomValue);
            else //.. isntance of preset
                wekaInstance.setValue(i, attr.numValue);
        }
        
        //.. then set value of class and return
        if (condition != null)
            wekaInstance.setValue(getNumAttributes(), condition);
        return wekaInstance;
    }

    /**Return non-extracted (ie, non-instance bound) attributes, including class */
    public FastVector getWekaAttributes(Classification classification) throws Exception{
         if (attributes == null) throw new Exception("Must extract attributes first");
         
         //.. add data-attributes
         FastVector attrs= attributes.getWekaAttributes();
        
         //.. add class attribtue
         weka.core.Attribute classAttr = classification.getWekaClassAttr();
         attrs.addElement(classAttr);
         return attrs;

    }
    
    
    /****/
    public Instance detrendByBaseLine(Instance base, double maxDiff, boolean copy) throws Exception{
        if (copy) {
            Instance instance = new Instance(this.condition);

            for (int i =0; i < streams.size(); i++){
                Channel c = streams.get(i);
                Channel b = base.streams.get(i);

                Channel newChan = c.detrend(b.getData(), maxDiff, copy);
                instance.addStream(newChan);
            }

           return instance;
           
        }
        
        //.. changes affect current data structures
        else { 
            for (int i =0; i < streams.size(); i++){
                Channel c = streams.get(i);
                Channel b = base.streams.get(i);

                c.detrend(b.getData(), maxDiff, copy);
            }
            
            return this;
        }
    }
    
}
