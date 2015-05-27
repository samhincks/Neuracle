/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeseriestufts.kth.streams.tri;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import timeseriestufts.evaluatable.AttributeSelection;
import timeseriestufts.evaluatable.Dataset;
import timeseriestufts.evaluatable.FeatureSet;
import timeseriestufts.evaluatable.TechniqueSet;
import timeseriestufts.evaluatable.Transformation;
import timeseriestufts.evaluatable.Transformations;
import timeseriestufts.evaluatable.WekaClassifier;
import timeseriestufts.evaluatable.performances.Predictions;
import timeseriestufts.evaluation.crossvalidation.CrossValidation;
import timeseriestufts.evaluation.crossvalidation.Fold;
import timeseriestufts.evaluation.experiment.Classification;
import timeseriestufts.kth.streams.bi.BidimensionalLayer;
import timeseriestufts.kth.streams.bi.ChannelSet;
import timeseriestufts.kth.streams.bi.Instance;
import timeseriestufts.kth.streams.uni.Channel;
import timeseriestufts.kth.streams.uni.FrequencyDomain;
import timeseriestufts.kth.streams.uni.UnidimensionalLayer;
import weka.core.Attribute;
import weka.core.FastVector;

/**The construction of an experiment object, that is what needs to be done before
 * it is evaluated or a model is trained on it, currently has three stages:
 * 1) Initialization, where we know the corresponding class
 * 2) Adding instances to the temporary structure tempInstances, 
 * 3) Properly ordering these instances into the matrixes (makeInstances)
 *
 * 
 * @author samhincks
 */
public class Experiment extends TridimensionalLayer<Instance>{
    private Hashtable<String, Integer> amountOfEachCondition; //.. how many instances of each condition
    private TechniqueSet techniqueSet; //.. the machine learning, featureSet, attribute selection used
    
    //.. AttributeSelection algorithms that have been applied to this experiment
    public ArrayList<weka.filters.supervised.attribute.AttributeSelection> asAlgosApplied;
    
    //.. The class and class-values this particular experiment will be looking at
    public Classification classification;
    
    public boolean test = false; //.. If this is tutorial mode, we display extra information when this is ture
    
    //.. Dataset is another storehouse for the collection of performances, indexed by experiment
    private Dataset dataset; //.. Just a collection of stats
    
    public String filename;
    
    //.. NumFolds - how many distinct folds - testing/training partitions - to make
    public int numFolds=10; 
    
    //.. predictions, reinstantiated each time a new evaluation is run, but saved in dataset and techniqueset
    Predictions predictions;
    
    //.. the number of readings per second in this experiment
    public float readingsPerSec;
    
    //.. whether or not we've extracted the attributes already
    public boolean extracted = false; 
    
    public Transformations transformations; //.. A set of manipulations which may have been applied to the object that derived it, or to this experiments
    
    /**Note Experiment not properly constructed after initialization. Must call makeInstances()*/
    /**Use this constructor if its the first time we evaluate this experiment*/
    public Experiment(String filename, Classification c, float readingsPerSec) throws Exception{
        this.classification = c;
        this.filename = filename;
        this.id = filename + c.getId();
        this.matrixes  = new ArrayList();
        this.readingsPerSec = readingsPerSec;
    }
    
    /**If we know the classification and instances*/
    public Experiment(String filename, Classification c, ArrayList<Instance> instances, float readingsPerSec) {
        this.classification = c;
        this.id = filename + c.getId();
        this.filename = filename;
        this.matrixes  = new ArrayList();
        this.readingsPerSec = readingsPerSec;

        for (Instance i : instances) {
            super.addMatrix(i);
        }
    }
     
    
    
    /** Evaluate the data according to the parameters specified in the techniqueset. 
     * Specify num folds to evaluate on for a quasi random approach or -1 for leave-one-subject-out
     * */
    public void evaluate(TechniqueSet ts, Dataset d, int numFolds) throws Exception {
        if(matrixes == null)
            throw new Exception("This data structure has not yet been divided into instances.. Hint: makeInstances()!");
        if (!ts.isComplete()) 
            throw new Exception("Must set specify what machine learning, attribute selection, featureset and settings to use");
        
        this.techniqueSet =ts;
        this.dataset = d;
        this.predictions = new Predictions(getDataset(), getTechniqueSet(), this.classification);
        extractAttributes(ts.getFeatureSet()); //.. later consider moving this, re-extracting attributes for each fold

        //.. if input is -1 or 0 do leave subject out
        if(numFolds <1)
            this.numFolds = this.matrixes.size();
        else
            this.numFolds = numFolds;

        runCrossValidation();
    }
    
    public TechniqueSet getTechniqueSet() throws Exception{
        if (techniqueSet ==null) throw new Exception("This layer lacks a technique set");
        return this.techniqueSet;
    }
    
    /**Extract the attribtues of every instance.**/
    public void extractAttributes(FeatureSet fs) throws Exception{

        for (int i=0; i < matrixes.size(); i++) {
            Instance inst = (Instance)matrixes.get(i);
            inst.extractAttributes(fs);
        }
        extracted = true;
    }
    
    
    /**Returns the first instance. */
    public Instance getFirstInstance() throws Exception {
        if (matrixes.isEmpty()) throw new Exception("This experiment is empty");
        return (Instance)matrixes.get(0);
    }
  
    
    /**Assuming this experiment has been properly split into instances, write this
     * experiment to an arff file, according to the specifications set in build.
     * 1. Write out the labelLine, according to Weka's specifications
     * 2. Write out each instance, according to Weka's specifications
     */
    public void writeToArff(String arffFile) throws Exception{
        Instance firstInstance = getFirstInstance();
        
        //.. write out the arff file
        //.. open it up
        try {
            FileWriter fstream = new FileWriter(arffFile);
            BufferedWriter arff = new BufferedWriter(fstream);
            
            //.. Write out all the labels we will extract
            firstInstance.writeLabelLine(arff);
            
            //.. Now prepare to write the arff
            arff.write("\n@ATTRIBUTE CLASS { "+this.classification.wekaString+"}\n\n\n");
            arff.write("@DATA \n\n\n");
            
            //.. write out data
            for (int i =0; i < matrixes.size(); i++) {
                Instance thisInstance = (Instance) matrixes.get(i);

                //.. MODIFIED) now we ALWAYS write out the condition
                thisInstance.writeData(arff);
                arff.write("\n");
            }
            arff.close();

        }
        catch(Exception e) {e.printStackTrace();}
    }
   
    
    /** Create n folds (Settings.numFolds) with different instances in training/testing*/ 
    private void runCrossValidation() throws Exception {
        CrossValidation crossValidation = new CrossValidation(this);
        
        //.. calculate folds. If the num folds is same as size, then do leave one subejct out
        Fold [] folds;
        
        if (this.numFolds == this.matrixes.size()) //.. if we happen to have as many folds as matrixes, do leave one out
            folds =crossValidation.leaveOneOut();
        else //.. precisely taylor folds for how many we want
            folds =crossValidation.calculateFolds();
        
        //.. Do the rest of the work -- feature extraction - > machine learning within each fold
        for (int j =0; j < folds.length; j++) {
             Fold fold = folds[j];
             fold.training.extracted = extracted;
             WekaClassifier wc = fold.training.train(this.getTechniqueSet());
             
             wc.test(fold.testing,this.getTechniqueSet(), predictions, fold.training.asAlgosApplied, -1, false); 
             //System.out.println("Experiment.runCrossValidation() completed " +j);

        }
         
        //.. save the stats, then forget about them in the experiment
        techniqueSet.addPredictions(predictions);
        dataset.addPredictions(predictions);
        predictions = null;
    }
    
    /**Compute and return the number of instances of each condition**/
    public Hashtable<String, Integer> getAmountOfEachCondition() {
        if (amountOfEachCondition != null) return amountOfEachCondition;
        
        //.. otherwise calculate it 
        amountOfEachCondition = new Hashtable();
        for(int i =0; i < matrixes.size(); i++) {
            String thisCondition = ((Instance)matrixes.get(i)).condition;
            if (amountOfEachCondition.get(thisCondition)!=null) {
                amountOfEachCondition.put(thisCondition, amountOfEachCondition.get(thisCondition)+1);
            }
            
            //..otherwise this is the first time we encounter this conditon
            else 
                amountOfEachCondition.put(thisCondition, 1);
        }
        
        return amountOfEachCondition;
    }
    
    public Dataset getDataSet() {return this.dataset;}
    
    private void unitTestFold(Fold fold) {
            //.. check that they have approx same of each condition
            Hashtable ht = fold.testing.getAmountOfEachCondition();
            System.out.println("amount of good = " + ht.get("good") +
                    " amount of bad = " + ht.get("bad") );
            System.out.println("Testingsize = " + fold.testing.matrixes.size());
            System.out.println("trainingsize = " + fold.training.matrixes.size());
            System.out.println("TotalSize shuold be " + this.matrixes.size());
           
            /*System.out.println("TRAINING: -----------");
            fold.training.printStream();
            System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"); 
            System.out.println("TESTING: ----------------------");
            fold.testing.printStream();
            System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXX"); */ 
        
    }
    
    /**Make a new Experiment that has all classes except className
     */
    public Experiment removeInstancesByClass(String className) throws Exception{
        Classification newClassification = classification.removeClass(className);
        
        ArrayList<Instance> instances = new ArrayList();
        //.. make a new matrix of instances that does not have any of classname
        for (int i =0; i< matrixes.size(); i++) {
            Instance instance = (Instance)matrixes.get(i);
            if (!instance.condition.equals(className))
                instances.add(instance);
            
        }
        return new Experiment(filename, newClassification, instances,this.readingsPerSec);
        
    }

    /**Make a new Experiment that has only specified classes.
     Print a System.err. if the resulting array is empty*/
    public Experiment removeAllClassesBut(ArrayList<String> toKeep) {
        Classification newClassification = classification.removeAllBut(toKeep);
       
        //.. make a new matrix of instances that does not have any of classname
        ArrayList<Instance> instances = new ArrayList();

        for (int i =0; i< matrixes.size(); i++) {
            Instance instance = (Instance)matrixes.get(i);
            for (String keep : toKeep) {
                if (keep.equals(instance.condition)) {
                    instances.add(instance);
                    break;
                }
            }
        }
        Experiment newE = new Experiment(filename, newClassification, instances,this.readingsPerSec);
        if (instances.size() ==0 ) System.err.println("Warning - you just removed all the instances");
        newE.setDataset(this.getDataSet());
        return newE;
    }
    
    

    
    public void setTechniqueSet(TechniqueSet ts) {this.techniqueSet = ts;}
    
    public Dataset getDataset() {return this.dataset;}
    
    public Predictions getPredictions() { return this.predictions;}
        
    /**Change dataset, but it is impermissable to */
    public void setDataset(Dataset d) {
        if (this.dataset != null) {
            System.err.println("This experiment " + this.id + " already has a statistical dataset associated with it: " +d.getId());
            return; //.. cannot change dataset
        }
            this.dataset = d;
    }

    /** Return an array of channels with specified condition and channel
     */
    public ArrayList<Channel> getChannelsWithChannelIndexAndCondition(int index, String condition) throws Exception{
        ArrayList<Channel> ics = new ArrayList(); 
        
        //... retrieve the instance for each channel
        for (int i = 0; i < this.matrixes.size(); i++) {
            Instance instance = (Instance)this.matrixes.get(i);
            if(instance.condition.equals(condition)) {
                Channel c = instance.streams.get(index);
                ics.add(c);
            }
        }
        
        return ics;
    }
    
    
     /**Print each of the SynchedChannelSets. 
      */
     public void printStream() {
        for (int i =0 ; i < matrixes.size(); i++) {
            Instance thisInstance = (Instance) matrixes.get(i);
            System.out.println(i + ": ------P:"+thisInstance.condition+"---------");
            thisInstance.printStream();
        }
    }
   
    public static Experiment generate(int numInstances, int numChannels, int numReadings) {
        ArrayList<Instance> instances = new ArrayList();
        ArrayList<String> values = new ArrayList();
        values.add("a");
        values.add("b");

        for (int i =0; i < numInstances; i++) {
            Instance instance = Instance.generate(numChannels, numReadings);
            instance.condition = values.get(i % 2);
            instances.add(instance);
        }
        Experiment experiment = new Experiment("test", new Classification(values, "fakeclass"), instances, 1);
        return experiment;
    }

    /**Remove all instances between start and end. Start and end is inclusive*/
    public void removeInstancesInRange(int start, int end) {
        for (int i =0; i < matrixes.size(); i++) {
            Instance inst = matrixes.get(i);
            
            //.. remove if: instance starts before this ends
            if (inst.inRange(start, end)) {
                matrixes.remove(i);
                i--; 
            }
        }
    }

    public void printInfo() {
        for (int i = 0; i < matrixes.size(); i++) {
            Instance thisInstance = (Instance) matrixes.get(i);
            thisInstance.printInfo();
            System.out.println(i + ": ------P:" + thisInstance.condition + " from " + thisInstance.realStart +" to " + thisInstance.realEnd +"L: " + (thisInstance.realEnd - thisInstance.realStart) + " ---------");
        }
    }

    /**Trains a classification algorithm on the experiment, with the machine learning 
     * properties specified in ts**/
    public WekaClassifier train(TechniqueSet t) throws Exception {
        //.. if either channelset or experiment has been manipulated, save this information for later
        if (transformations != null) t.addTechnique(this.transformations);
        this.techniqueSet = t;
        
        //.. extract features
        if(!extracted) 
            this.extractAttributes(t.getFeatureSet());
        
        //.. get instances from my instance objects, with all attributes extracted
        weka.core.Instances instances = getWekaInstances(true); //.. try to extract attribtues if specified
        double [] attrs = instances.attributeToDoubleArray(0);
        
        //.. retrieve classifier and train it, then remember what classifiers were applied
        WekaClassifier wc = (WekaClassifier) t.getClassifier();
        wc.buildClassifier(instances);
        wc.lastInstanceLength = this.getFirstInstance().getNumPointsAtZero();
        wc.lastTrainedClassification = this.classification;
        wc.lastTechniqueTested = t;
        wc.timesTrained++;
        wc.lastAsAlgosUsed = this.asAlgosApplied; //... ergh, I hope this works :/
        
        //.. return it, trained
        return wc;
    }
    
    /**Return this datalayer structured as weka Instances. 
     * The attributes must have already been extracted.
     * Does not save any of the raw data, only the features. 
     * Sets class as the last index.
     * 
     * if applyAS = false, then don't apply as algorithms (for example if we want real time stream)
     **/
    public weka.core.Instances getWekaInstances(boolean applyAS) throws Exception  {
        FastVector wekaAttributes = this.getFirstInstance().getWekaAttributes(classification);
        weka.core.Instances instances = new weka.core.Instances(this.id, wekaAttributes, this.matrixes.size());
        
        //.. the class was the last extracted weka attribute, so set as last
        instances.setClassIndex(wekaAttributes.size()-1);
        
        //.. add each instance, and bind it to the attributes of instances
        for (Instance myInst : this.matrixes) {
            weka.core.Instance wInstance = myInst.getWekaInstance(instances);
            instances.add(wInstance);
        }
        
        //.. this is false when we don't want to the attribute selection algorithms specified to be applied
        if (applyAS){
            //-- APPLY ATTRIBUTE SELECTION ALGORITHM
            //.. reset any Attibute Selection algorithms applied (should be null)
            asAlgosApplied = new ArrayList();
            AttributeSelection as = techniqueSet.getAttributeSelection();

            //.. info gain first if specified
            if (as.doInfoGain()) {
                instances = as.infoGain(instances);
                this.asAlgosApplied.add(as.getMostRecentInfoFilter()); //.. this was just set
            }
            //.. cfs after if specified
            if (as.doCFSSubset()) {
                instances = as.cfsSubset(instances);
                this.asAlgosApplied.add(as.getMostRecentCFSFilter()); //.. this was just set
            }
        }
        return instances;
    }
    
    
    /**Return the instance with the least number of points in any channel**/
    public int minPoints() {
        int min =matrixes.get(0).minPoints();
        for(Instance i : matrixes) {
            int shortest = i.minPoints();
            if (shortest < min)
                min = shortest;
        }
        return min;
    }
    
    /**Detrend an experiment, factoring out change from the baseline to each trial.
     There must be a condition called baseline
     **/
    public Experiment detrend(double maxDiff, boolean copy) throws Exception{
        //.. find baseline 
        Instance baseline =null;
        for (Instance i : matrixes) {
            if (i.condition.equals("baseline"))
                baseline = i;
        }
        
        if (baseline == null) throw new Exception("Could not find the baseline");
        
        //.. if we want a new experiment
        if (copy) {
            ArrayList<Instance> instances = new ArrayList(); //.. to add to experiment

            //.. add new instances where each are corrected by the baseline 
            for (BidimensionalLayer bd : matrixes) {
                Instance instance = (Instance)bd;
                Instance newInstance = instance.detrendByBaseLine(baseline, maxDiff, true);
                instances.add(newInstance);
            }

            return new Experiment(filename, classification, instances,this.readingsPerSec);
        }
        
        //.. else if we want to manipulate the same experiment, perhaps to save memory
        else {
            for (Instance instance : matrixes) {
                instance.detrendByBaseLine(baseline, maxDiff, false);
            }
            return this;
        }
    }
     
    
    /**Trims the last few readings of instances which exceed the expected length
     */
    public int trimUnfitInstances(int expectedLength) {
        int trimmed = 0;
        for (Instance in : matrixes) {
           trimmed +=in.trimUnfitChannels(expectedLength);
        }
        return trimmed;
       
    }
    /** Remove instances that do not come within a band of accepted length
     * Any instance that is different from the expectedLength by more than acceptedDifference
     **/
    public Experiment removeUnfitInstances(int expectedLength, double acceptedDifference, boolean copy) {
          ArrayList<Instance> newInstances = new ArrayList(); //.. to add to experiment
          double pctLarger = 1 + acceptedDifference; 
          int removed =0;
          
          if (copy) {
            for(Instance in : matrixes) {
                if (!shouldRemove(in,pctLarger,expectedLength))
                    newInstances.add(in);
                else 
                    removed++;
            }
              return new Experiment(filename + "cleaned", classification, newInstances, this.readingsPerSec);

          }
          else {
              for (Iterator<Instance> iterator = matrixes.iterator(); iterator.hasNext();) {
                  Instance ins = iterator.next();
                  if (shouldRemove(ins, pctLarger, expectedLength))
                     iterator.remove();
              }
              return this;
          }
    }
    private boolean shouldRemove(Instance in, double pctLarger, int expectedLength ) {
        int maxp = in.getMaxPoints();
        int minp = in.getMinPoints();
        //System.out.println("min is " + minp + " max is " + maxp + " . and we are accepting " +(pctLarger*expectedLength));

        //.. first if an instances channels deviate significantly from each other, remove them
        if (maxp > minp * pctLarger)
            return true; 

        //.. but also if this instances length relative to the rest of the data is too long, we dont add
        if (maxp > pctLarger * expectedLength)
           return true;
        return false;
    }
    
    /**Return a ChanelSetSet, where each ChannelSet represents a condition
     * and each channel is the averaged magnitude at the 
     * assorted phases. The first channel of the channelset is the corresponding frequency 
     **/
    public ChannelSetSet getAveragedFourier(boolean highpass) throws Exception{
        ChannelSetSet css = new ChannelSetSet();
        css.id = this.id + "fourier";
        
        //..Create a new channel set for each condition 
        for(String condition : classification.values) {
            ChannelSet cs = new ChannelSet();
            cs.id = condition;
            int added = 0;
            Channel magnitudeAverage = null;

            //.. Create a new channel, that is the fourier averaged version
            for(int i=0; i< this.getChannelCount(); i++) {
                ArrayList<Channel> channels = this.getChannelsWithChannelIndexAndCondition(i, condition);
                //.. average each instances version of that channel together
                for (Channel c : channels) {
                    if (highpass) c = c.highpass(1.0f, true);
                    FrequencyDomain fd  =c.getFrequencyDomain();
                    
                    //.. the very first channel should be the frequencies
                    if (added ==0) {
                        cs.addStream(fd.frequencyChannel);
                        magnitudeAverage = fd.magnitudeChannel;
                    }
                    else { //.. otherwise average that channel's magnitude with existing
                        magnitudeAverage.merge(fd.magnitudeChannel);
                    }
                    added++;
                }
                cs.addStream(magnitudeAverage);
            }
            css.addChannelSet(cs);
        }
        return css;
    }
    
    /** Change classification and the value of all instances in the Experiment
     **/
    public void changeClassificationToAll(Classification c, String val) throws Exception {
        if (!(c.hasCondition(val))) throw new Exception(c.name + " doesnt support " + val);
        this.classification = c;
        for (Instance ins: this.matrixes) {
            ins.condition= val;
        }
    }
    
    public void addExperiment(Experiment e) throws Exception{
        if (e.classification != this.classification) throw new Exception("Experiments must have the same classifications");
        for (Instance ins : e.matrixes) {
            this.addMatrix(ins);
        }
    }
    
    /**Return the most common instance length**/
    public int getMostCommonInstanceLength() {
        HashMap<Integer, Integer> hm = new HashMap();
        for (Instance ins : matrixes) {
            int pts = ins.getMaxPoints();
            Integer len = hm.get(pts);
            if (len == null) hm.put(pts, 1);
            else  hm.put(pts, len+1); 
        }
        int maxC = -1;
        int maxV = -1;
        for (Map.Entry<Integer,Integer> m : hm.entrySet()) {
            if (m.getValue() > maxC) {
                maxV = m.getKey();
                maxC = m.getValue();
            }
        }
        return maxV;
    }
    
    /**Divide the experiment into n parts. Two ways to do this. Either keep instance length
     * the same, or divide into truly smaller parts. 
     * @param parts, # of parts to divide into
     * @param reduce, true if we want smaller instance lengths
     * @return a new experiment, a shallow copy of the points
     */
    public Experiment getBoundaryInstances(boolean reduce, int parts) throws Exception{
        ArrayList<String> values = new ArrayList(); 
        ArrayList<Instance> instances = new ArrayList();
        Experiment e;
        int regLength = this.getLeastPointsInAChannel()-1;
        int partLength = (int) Math.floor(regLength / parts) ;

        //.. divide each instance into separete parts
        if (reduce) {
            for (Instance ins : matrixes) {
                int position = 0;
                for (int i = 0; i < parts; i++) {
                    String condition = ins.condition+i;
                    Instance sub= ins.getInstance(position, position + partLength, condition);
                    position+=partLength;
                    if (!(values.contains(condition))) values.add(condition);
                    instances.add(sub);
                }
            }
        }
        
        //.. maintain length, but slide
        else { //.. now the parts parameter refers to how far up to advance after each
            for(int i =0; i < matrixes.size()-1; i++) {
                Instance a = matrixes.get(i);
                Instance b = matrixes.get(i+1);
                int position =0;
                //.. create a new instance for each sliding part
                for (int j = 0; j < parts; j++) {
                    int end = position + regLength;
                    int spillOver = end - regLength;
                    String conditionName =  ( (spillOver <=0) ? a.condition : a.condition +j+"/"+parts+b.condition);
                    Instance aPart = a.getInstance(position, regLength, conditionName);
                    
                    //.. if we need some of B
                    if (spillOver < b.getMinPoints()){
                        if (spillOver >0) aPart.appendChanSet(b.getChannelSetBetween(0, spillOver));
                        //.. advance token add position, and add classification
                        position+=partLength;
                        instances.add(aPart);
                        if (!(values.contains(conditionName))) values.add(conditionName);
                    }
                }                
            }
        }
        
        Classification c = new Classification(values, this.classification.name);
        e = new Experiment(this.filename, c, instances,this.readingsPerSec);
        e.transformations = this.transformations;
        return e;
    }
    
    /** Return a new set of experiments where each part refers to a different part of the instance
     **/
    public ArrayList<Experiment> partition(int parts) throws Exception {
        ArrayList<Experiment> ret = new ArrayList();
        //.. create parts new ArrayLists 
        for (int i = 0; i < parts; i++) {
            Experiment e = new Experiment(this.filename, this.classification, this.readingsPerSec);
            e.id = this.id +i+"-"+parts;
            ret.add(e);
        }
        
        for (Instance ins : matrixes) {
            int partLength = ins.getMinPoints() / parts;
            int curStart =0;
            for (int i = 0; i < parts; i++) {
               Instance sub = ins.getInstance(curStart, curStart +partLength, ins.condition );
               ret.get(i).addMatrix(sub);
               curStart += partLength;
            }
        }
        
        return ret;
    }

    
    /** Apply a specific method for altering the data; used when transforming data in
     * real time. Consider allowing for the copy parameter in the future 
     * @param t the manipulation to apply
     */
    public Experiment manipulate(Transformation t, boolean copy) throws Exception{
        ArrayList<Instance> instances = new ArrayList();
        Instance lastInstance= null;
        for (Instance ins : matrixes) {
            if (t.type == Transformation.TransformationType.subtract) { //.. calculate difference to previous instance
                t.params = new float[1];
                t.params[0] = (float) ((lastInstance ==null) ? ins.getMean() : lastInstance.getMean());   
            }

            if (!copy) ins.manipulate(t, copy);
            else {
                ChannelSet cs = ins.manipulate(t, copy);
                Instance ins2 = new Instance(ins.condition, ins.realStart, ins.realEnd);
                ins2.streams = cs.streams;
                instances.add(ins2);
            }
            lastInstance = ins; //.. only for the subtract transformation
        }
        Experiment ret;  
        String newId = this.id + t.type.name();  
        if(t.params != null) {
            if (t.params.length >0) newId += t.params[0];
            if (t.params.length >1) newId += t.params[1];
            if (t.params.length >2) newId += t.params[2];  
        }
  
        if (copy) {
            ret =new Experiment(/*this.filename+*/newId, this.classification, instances, this.readingsPerSec );
        }
        else ret = this;
        
        //.. add new transformation and return
        if (ret.transformations == null) ret.transformations = new Transformations();
        t.for3D = true;
        ret.transformations.addTransformation(t);
        return ret;
    }

    
    
    public static void main(String [] args) {
        try{
            Experiment e = Experiment.generate(3,1,10);
            System.out.println("experiment has " + e.getMostCommonInstanceLength() + " readings in a typical channel");
            int TEST =4;
            
            
            if (TEST ==4) {
                ArrayList<Experiment> r = e.partition(4);
                for(Experiment s : r) {
                    System.out.println(s.getMostCommonInstanceLength());
                }
            }
            if (TEST ==1) {
                //e.printStream();
                ChannelSetSet css = e.getAveragedFourier(false);
                css.printStream();
            }
            
            if (TEST ==2) {
                e =  Experiment.generate(2,4,6);
                e.evaluate(TechniqueSet.generate(), Dataset.generate(), -1);
            }
            if (TEST ==3) {
                //e.printStream();
                System.out.println(e.matrixes.size());
                System.out.println(e.getLeastPointsInAChannel());
                e = e.getBoundaryInstances(false, 5);
                //System.out.println("experiment has " + e.getMostCommonInstanceLength());
                System.out.println(e.matrixes.size());
                System.out.println("-------------");
                System.out.println("-----------");
               // e.printStream();
               // e.printInfo();
            }

        }
        catch(Exception e) {e.printStackTrace();}
    }
}

