/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dao;

import dao.datalayers.BiDAO;
import dao.datalayers.DataLayerDAO;
import dao.datalayers.QuadDAO;
import dao.datalayers.TriDAO;
import dao.techniques.TechniqueDAO;
import dao.techniques.TechniquesDAO;
import java.util.ArrayList;
import java.util.Hashtable; 
import org.json.JSONArray;
import org.json.JSONObject;
import stripes.ext.ThisActionBeanContext;
import timeseriestufts.evaluatable.AttributeSelection;
import timeseriestufts.evaluatable.ClassificationAlgorithm;
import timeseriestufts.evaluatable.Dataset;
import timeseriestufts.evaluatable.FeatureSet;
import timeseriestufts.evaluatable.Technique;
import timeseriestufts.evaluatable.TechniqueSet;
import timeseriestufts.evaluatable.WekaClassifier;
import timeseriestufts.evaluatable.performances.Performances;
import timeseriestufts.evaluatable.performances.Prediction;
import timeseriestufts.evaluatable.performances.Predictions;
import timeseriestufts.kth.streams.DataLayer;
import timeseriestufts.kth.streams.bi.ChannelSet;
import timeseriestufts.kth.streams.quad.MultiExperiment;
import timeseriestufts.kth.streams.tri.ChannelSetSet;
import timeseriestufts.kth.streams.tri.Experiment;
import timeseriestufts.kth.streams.uni.Channel;
import timeseriestufts.kth.streams.uni.FrequencyDomain;

/**
 *
 * @author samhincks
 */
public class DataManipulationParser extends Parser{
    public Technique currentTechnique;
    public DataManipulationParser(){
        commands = new Hashtable();
   
        Command command = new Command("gethrv");
        command.documentation = "Returns the average standard deviation of RR intervals"
                + " of the channelset, or if an experiment";
        command.parameters = "Channel to search for RR interval, if any. None for all, * for each"
                + " separately";
        commands.put(command.id, command);
        
        command = new Command("getpulse");
        command.documentation = "Returns the average pulse of the channelset, or if an experiment"
                + " the pulse at different readings";
        command.parameters = "Channel to search for pulse, if any. None for all, * for each separately";
        commands.put(command.id, command);
        
        //-- FILTER
        command = new Command("filter");
        command.documentation = "Applies a filter to the selected channelset, returning a deep copy";
        command.parameters = "1.FILTER.MOVINGAVERAGE(readingsBack)--> apply a moving average a channel set with specified window length ::"
                + "2.FILTER.LOWPASS(x)  3.FIlTER.HIGHPASS(x) or 4.FILTER.BANDPASS(x,y)::";
        command.debug = "Not entirely clear how x,y translates into filter hz values "
                + "If I say filter.movingaverage(1), it should 'almost' take away the pulse, but"
                + " I need to say filter.movingaverage(0.005) ";
        commands.put(command.id, command);
        
        //-- calcoxy 
        command = new Command("calcoxy");
        command.documentation = " With a 2D channelset selected, multiply 690 and 830 cols"
                + " to produce rows with oxygenated and deoxygenated hemoglobin values";
        command.debug = "Only works with hard-coded channel locations";
        commands.put(command.id, command);
        
        //-- zscore 
        command = new Command("zscore");
        command.documentation = " With a 2D channelset selected, replace each value by its standard"
                + "score, the difference between it and the global average, divided by the standard deviation";
        commands.put(command.id, command);

        
        //--ADD FEATURES
        command = new Command("addfeatures");
        command.documentation = " With a feature set selected, adds a set of feature descriptions. ";
        command.parameters = "ADDFEATURES(statistic,channel,window) -->    Takes three parameters: "
                + " 1) statistic:  mean, slope, secondder, fwhm, largest, smallest, absmean, t2p"
                + " 2) channel: referencable by id or index. 0^1^2 makes attributes for the first three channels."
                + " 0+1+2+3 averages the values at the first four channels, effectively creating a region"
                + " 3) time-window: what part of the instance: FIRSTHALF, SECONDHALF or WHOLE. "
                + " The parameter 6:10 would constrict the features to points between index 6 and 10::";
        commands.put(command.id, command);
        
        //--MAKE 
        command = new Command("make");
        command.documentation = "Make a new technique, a part of a method, for how a classifier should be built on this data ";
        command.parameters =  "MAKEFS(statistic,channel,window) --> makes a new feature set. See ADDFEATURES::"
                + "MAKEAS(cfs or info, numAttributes) --> makes a new attribute selection::"
                + "MAKEML(name) --> makes a new machine learnign algorithm::";
        commands.put(command.id, command);
        
        //--EVALUATE 
        command = new Command("evaluate");
        command.documentation = " With a 3D dataset (a collection of instances) selected and connected to"
               + " at least one of each TechniqueSet (feature set, attribute selection, machine learning, and settings,"
               + " evaluates the dataset by creating a machine learning algorithm on different partitions of the data"
               + " and evaluating it on unseen instances ::";
        command.parameters = "1[OPTIONAL] fodls =  The number of folds for the crossfold validation";
        command.debug = "Unclear if numFolds still works";
        commands.put(command.id, command);
        
        //--ANCHOR 
        command = new Command("anchor");
        command.documentation = "With a 3D dataset (a split dataset) selected, set the starting point at zero"
                + " and all subsequent points as differences from 0";
        commands.put(command.id, command);

        //--TRAIN
        command = new Command("train");
        command.documentation = " With a 3D dataset (a collection of instances) selected and connected to\"\n" +
"               + \" at least one of each TechniqueSet (feature set, attribute selection, machine learning, and settings," +
                " trains a classifier which can be applied to any other arbitrary channelset, that is synchronized or not";
        commands.put(command.id, command);
        
        //-- CLASSIFY 
        command = new Command("classify");
        command.documentation =" With a 2D channelset selected and intersecting a trained machine learning "
                + " algorithm, classifies the 2D channelset with an instance length matching that length"
                + " in the training; if the machine learning algorithm supports confidence, also provides a confidence.";
        command.parameters = "1[OPTIONAL] k = provide a new classification every kth reading";
        commands.put(command.id, command);
        
        //-- CLASSIFYLAST
        command = new Command("classifylast");
        command.documentation = " With a 2D channelset selected and intersecting a trained machine learning "
                + " algorithm, classifies the last points of the channelset equal to the length of the training trial ";
        commands.put(command.id, command);
        
        //-- fnirs 
        command = new Command("fnirs");
        command.documentation = " Applies a range of data-manipulations to the selected datasets, "
                + " ultimate putting it into the best visualizable form";
        commands.put(command.id, command);
        
        //-- fnirs 
        command = new Command("wireless");
        command.documentation = " Applies a range of data-manipulations to the selected datasets, "
                + " ultimate putting it into the best visualizable form";
        commands.put(command.id, command);
        
        command = new Command("glassroutes");
        command.documentation = " Applies a range of data-manipulations to the selected datasets, "
                + " ultimate putting it into the best visualizable form, customizedd for the glassroutes experimetn";
        commands.put(command.id, command);
        
        //-- granger 
        command = new Command("granger");
        command.documentation = "Estimates causality between all pairwise voxels, returning a visualization ";
        command.parameters = "LAG = distance to estimate causation between channels";
        commands.put(command.id, command);
        
        
    }

    public JSONObject execute(String command, String[] parameters, ThisActionBeanContext ctx,
            DataLayer currentDataLayer, TechniqueDAO techDAO) throws Exception {
        this.ctx = ctx;
        this.currentDataLayer = currentDataLayer;
        if(techDAO != null)
            this.currentTechnique = techDAO.technique;
        
        Command c = null;
        
        if (command.startsWith("gethrv")) {
            c = commands.get("gethrv");
            c.retMessage = getHRV(parameters);
        }
        
        else if (command.startsWith("getpulse")) {
            c = commands.get("getpulse");
            c.retMessage = getPulse(parameters);
        }
        
        //.. filter.movingaverage(), .lowpass()
        else if (command.startsWith("filter")) {
            c = commands.get("filter");
            c.retMessage = filter(command, parameters);
            c.action = "reload";
        } 
        
        else if (command.startsWith("calcoxy")) {
            c = commands.get("calcoxy");
            c.retMessage = calcOxy(parameters);
            c.action = "reload";
        }
        
        else if (command.startsWith("zscore")) {
            c = commands.get("zscore");
            c.retMessage = zScore(parameters);
            c.action = "reload";
        }
        
          
        //.. addFeatures(*,*,*)
        else if (command.startsWith("addfeatures")) {
            c = commands.get("addfeatures");
            c.retMessage = addFeatures(parameters);
        } 

        //.. MakeML, makeFS, makeAS, makeSettings
        else if (command.startsWith("make")) {
            c = commands.get("make");
            c.retMessage = make(command, parameters,ctx.getTechniques());
            c.action = "reloadT";
        } 
        else if (command.startsWith("evaluate")) {
            c = commands.get("evaluate");
            c.retMessage = evaluate(parameters, ctx.getCurrentDataLayer(), ctx.getPerformances());
        } 
        
        else if (command.startsWith("anchor") ) {
            c = commands.get("anchor");
            c.retMessage = anchor(parameters);
            c.action = "reload";
        }
        
        else if (command.startsWith("train")) {
            c = commands.get("train");
            c.retMessage = train(parameters, ctx.getCurrentDataLayer(), ctx.getPerformances());
            c.action = "reloadT";
        }
        else if (command.startsWith("classifylast")) {
            c = commands.get("classifylast");
            c.retMessage = classifyLast(parameters, ctx.getCurrentDataLayer());
        }
        else if (command.startsWith("classify")) {
            c = commands.get("classify");
            c.retMessage = classify(parameters, ctx.getCurrentDataLayer(), ctx.getPerformances());
        }
        
        else if (command.startsWith("fnirs")) {
            c = commands.get("fnirs");
            c.retMessage = fnirs(parameters);
            c.action = "reload";
        }
        
        else if (command.startsWith("glassroutes")) {
            c = commands.get("glassroutes");
            c.retMessage = glassroutes(parameters);
            c.action = "reload";
        }
        
        else if (command.startsWith("wireless")) {
            c = commands.get("wireless");
            c.retMessage = wireless(parameters);
            c.action = "reload";
        }
        
        else if (command.startsWith("granger")) {
            c = commands.get("granger");
            c.action = "cmatrix";
            c.data = granger(parameters);
        }


        if (c == null) {
            return null;
        }
        return c.getJSONObject();
    }

    /**
     * Granger : return a JSONObject showing correlation between channels *
     */
    public JSONObject granger(String[] parameters) throws Exception{
        int LAG =155;
        if (parameters.length >0) LAG = Integer.parseInt(parameters[0]);
        
        if (!(currentDataLayer instanceof ChannelSet)) throw new Exception("Selected layer must be a 2D Channelset");
        
        ChannelSet channelSet = (ChannelSet)currentDataLayer;
        
        JSONObject jsonObj = new JSONObject();        
        jsonObj.put("id", channelSet.getId());
        JSONArray data = new JSONArray();
        jsonObj.put("data", data); //.. data is array of arrays, index corresponds to order we see channel
       
        //.. get granger correlation between all channels 
        for (Channel a : channelSet.streams) {
            JSONArray correlations = new JSONArray();
            data.put(correlations); // each channel has correlations to all other channels   
            for (Channel b : channelSet.streams) {
                double diff = a.granger(b, LAG); 
                correlations.put(diff);
            }
        }
       
        jsonObj.put("type", "correlation");
        return jsonObj;
        
    }
    private String getHRV(String [] parameters) throws Exception {
        int channel = 0;
        String retString = "";
        if (parameters.length > 0) {
            if (parameters[0].equals("*")) {
                channel = -2;
            } else {
                channel = Integer.parseInt(parameters[0]);
            }
        } else {
            channel = -1; //.. no parameters, then take average
        }
        if (currentDataLayer instanceof Experiment) {
            Experiment e = (Experiment) currentDataLayer;

            //..Create a new channel set for each condition 
            for (String condition : e.classification.values) {
                if (channel >= 0) {
                    ArrayList<Channel> channels = e.getChannelsWithChannelIndexAndCondition(channel, condition);
                    float averageHRV = 0;
                    for (Channel c : channels) {
                        averageHRV += c.getHRVariability();
                    }
                    averageHRV = averageHRV / channels.size();
                    retString += condition + " measured pulse at channel " + channel + " is " + averageHRV + " ms ;;";
                } else if (channel == -1) {
                    float averageHRV = 0;
                    int tests = 0;
                    for (int i = 0; i < e.getChannelCount(); i++) {
                        ArrayList<Channel> channels = e.getChannelsWithChannelIndexAndCondition(i, condition);
                        for (Channel c : channels) {
                            averageHRV += c.getHRVariability();
                            tests++;
                        }
                    }
                    averageHRV = averageHRV / (float) tests;
                    retString += condition + " measured pulse at all channels is " + averageHRV + " ms ;;";
                } else {
                    for (int i = 0; i < e.getChannelCount(); i++) {
                        ArrayList<Channel> channels = e.getChannelsWithChannelIndexAndCondition(i, condition);
                        float averageHRV = 0;
                        for (Channel c : channels) {
                            averageHRV += c.getHRVariability();
                        }
                        averageHRV = averageHRV / channels.size();
                        retString += condition + " measured pulse at channel " + i + " is " + averageHRV + " ms ;;";
                    }
                }
            }
            return retString;
        } else if (currentDataLayer instanceof ChannelSet) {
            ChannelSet cs = (ChannelSet) currentDataLayer;
            //cs = cs.calcOxy(true, null, null);
            if (channel >= 0) {
                Channel c = cs.getChannel(channel);
                retString = "Measured pulse at channel " + channel + " is " + c.getHRVariability()+ " ms";
                return retString;
            } else if (channel == -1) {
                float averageHRV = 0;
                for (int i = 0; i < cs.getChannelCount(); i++) {
                    Channel c = cs.getChannel(i);
                    averageHRV += c.getHRVariability();
                }
                retString += "Measured pulse at all channels is " + (averageHRV / (float) cs.getChannelCount()) + " ms ";
                return retString;
            } else {
                for (int i = 0; i < cs.getChannelCount(); i++) {
                    Channel c = cs.getChannel(i);
                    retString += "Measured pulse at channel " + i + " is " + c.getHRVariability() + " ms ;;";
                }
                return retString;
            }
        } else {
            throw new Exception("Datalayer must be either channelset or experiment");
        }
    }
    
    private String getPulse(String [] parameters) throws Exception{
        int channel= 0;
        String retString = "";
        if (parameters.length > 0) {
            if (parameters[0].equals("*")) channel =-2;
            else channel = Integer.parseInt(parameters[0]);
        }
        else {
            channel = -1; //.. no parameters, then take average
        }
        if (currentDataLayer instanceof Experiment) {
            Experiment e = (Experiment) currentDataLayer;

            //..Create a new channel set for each condition 
            for (String condition : e.classification.values) {
                if (channel >=0) {
                    ArrayList<Channel> channels = e.getChannelsWithChannelIndexAndCondition(channel, condition);
                    float averagePulse = 0;
                    for (Channel c : channels) {
                        c = c.highpass(0.75f, true);
                        c = c.normalize(true);
                        averagePulse+= c.getFrequencyDomain().getPulse();
                    }
                    averagePulse = averagePulse / channels.size();
                    retString+= condition + " measured heart rate variability at channel " + channel+ " is " + averagePulse + " bpm ;;";
                }
                else if (channel == -1) {
                    float averagePulse = 0;
                    int tests =0;
                    for (int i=0; i< e.getChannelCount(); i++){
                        ArrayList<Channel> channels = e.getChannelsWithChannelIndexAndCondition(i, condition);
                        for (Channel c : channels) {
                            c = c.highpass(0.75f, true);
                            c = c.normalize(true);
                            averagePulse += c.getFrequencyDomain().getPulse();
                            tests++;
                        }
                    }
                    averagePulse = averagePulse / (float)tests;
                    retString += condition + " measured heart rate variability at all channels is " + averagePulse + " bpm ;;";
               }
                else {
                    for (int i = 0; i < e.getChannelCount(); i++) {
                        ArrayList<Channel> channels = e.getChannelsWithChannelIndexAndCondition(i, condition);
                        float averagePulse = 0;
                        for (Channel c : channels) {
                            c = c.highpass(0.75f, true);
                            c = c.normalize(true);
                            averagePulse += c.getFrequencyDomain().getPulse();
                        }
                        averagePulse = averagePulse / channels.size();
                        retString += condition + " measured heart rate variability at channel " + i + " is " + averagePulse + " bpm ;;";
                    }
                }
            }
           return retString; 
        }
        else if (currentDataLayer instanceof ChannelSet) {
            ChannelSet cs = (ChannelSet)currentDataLayer;
            //cs = cs.calcOxy(true, null, null);
            if (channel >=0){
                Channel c = cs.getChannel(channel);
                c = c.highpass(0.75f, true);
                c = c.normalize(true);
                FrequencyDomain fd = c.getFrequencyDomain();
                retString= "Measured heart rate variability at channel " + channel +" is " + fd.getPulse() +" bpm";
                return retString;
            }
            else if (channel ==-1) {
                float avgPulse =0;
               for(int i=0; i <cs.getChannelCount(); i++) {
                    Channel c = cs.getChannel(i);
                    c = c.highpass(0.75f, true);
                    c = c.normalize(true);
                    FrequencyDomain fd = c.getFrequencyDomain();
                    avgPulse += fd.getPulse();
                }
                retString += "Measured heart rate variability at all channels is "  + (avgPulse / (float) cs.getChannelCount()) +" bpm ";
                return retString;
            }
            else {
                for(int i=0; i <cs.getChannelCount(); i++) {
                    Channel c = cs.getChannel(i);
                    c = c.highpass(0.75f, true);
                    c = c.normalize(true);
                    FrequencyDomain fd = c.getFrequencyDomain();
                    retString +=  "Measured heart rate variability at channel " + i + " is " + fd.getPulse() + " bpm ;;";
                }
                return retString;
            }
        }
        else throw new Exception("Datalayer must be either channelset or experiment");
    }
    
    /**
     * Handle : filter.xxx(parameter). Apply a filter to a channelset or a channelsetset.
     */
    private String filter(String input, String [] parameters) throws Exception {
        //.. chanSets will be 1 or more ChannelSets, each of which we will apply the filte to
        ArrayList<ChannelSet> chanSets = getChanSets();
        String retString = "";

        for (ChannelSet cs : chanSets) {
            if (input.startsWith("filter.movingaverage")) {
                retString += applyMovingAverage(cs, parameters);
            } else if (input.startsWith("filter.lowpass")) {
                retString += applyPassFilter(cs, parameters, 0);
            } else if (input.startsWith("filter.highpass")) {
                retString += applyPassFilter(cs, parameters, 1);
            } else if (input.startsWith("filter.bandpass")) {
                retString += applyPassFilter(cs, parameters, 2);
            }
        }

        return retString;
    }

    /**
     * Handle: filter.movingaverage(numBack) where numBack is how many readings
     * back of the moving average
     */
    private String applyMovingAverage(ChannelSet cs, String [] parameters) throws Exception {
        //.. extract the one parameter : how many readings back. Default is 5
        int readingsBack = 10;
        if (parameters.length > 0) {
            readingsBack = Integer.parseInt(parameters[0]);
        }

        ChannelSet filteredSet = cs.movingAverage(readingsBack, true); //.. we want a copy
        filteredSet.setParent(cs.id);
        BiDAO mDAO = new BiDAO(filteredSet);
        ctx.dataLayersDAO.addStream(filteredSet.id, mDAO);

        return "Created " + filteredSet.id + " a copy of " + cs.id + " with new values "
                + " representing the moving average at " + readingsBack + " points.;;";
    }

    /**
     * Handle: filter.lowpass(freq), filter.highpass(freq), filter.bandpass(low,
     * high) type==0 : lowpass type==1: highpass type==2: bandpass
     *
     */
    private String applyPassFilter(ChannelSet cs,  String [] parameters, int type) throws Exception {
        float freq = 0.5f;
        float freq2 = 0.8f;
        if (parameters.length > 0) {
            freq = Float.parseFloat(parameters[0]);
        }

        if (parameters.length > 1) //.. if its bandpass there can be two parameters;
        {
            freq2 = Float.parseFloat(parameters[1]);
        }

        ChannelSet filteredSet;
        if (type == 0) {
            filteredSet = cs.lowpass(freq, true);
        } else if (type == 1) {
            filteredSet = cs.highpass(freq, true);
        } else //.. type ==2 
        {
            filteredSet = cs.bandpass(freq, freq2, true);
        }

        filteredSet.setParent(cs.id);
        BiDAO mDAO = new BiDAO(filteredSet);
        ctx.dataLayersDAO.addStream(filteredSet.id, mDAO);

        return "Created " + filteredSet.id + " a copy of " + cs.id;
    }
    
  
    private String calcOxy(String [] parameters) throws Exception{
        ArrayList<ChannelSet> chanSets = getChanSets();

        for (ChannelSet cs : chanSets) {
            ChannelSet filteredSet = cs.calcOxy(true,null,null); //.. we want a copy
            filteredSet.setParent(cs.id);
            BiDAO mDAO = new BiDAO(filteredSet);
            ctx.dataLayersDAO.addStream(filteredSet.id, mDAO);
        }
        
        return "Modified " + chanSets.size() + ";;" + "0->7 : Probe A. 8->15;; :"
                + " ProbeB;; 0->3&8->12 : HbO;;... 0,4,8,12: closest;; 3,7,11,15 : farthest";
    }
    
    private String zScore(String[] parameters) throws Exception {
        ArrayList<ChannelSet> chanSets = getChanSets();

        for (ChannelSet cs : chanSets) { 
            ChannelSet filteredSet = cs.zScore(true); //.. we want a copy
            filteredSet.setParent(cs.id);
            BiDAO mDAO = new BiDAO(filteredSet);
            ctx.dataLayersDAO.addStream(filteredSet.id, mDAO);
        }

        return "ZScored " + chanSets.size() + "";
    }
    /**
     * HANDLE: addFeature(mean, 1, :) or (mean, 1, WHOLE)
     * addFeature(slope^mean,1^2, WHOLE^ Immensely complex.*
     */
    private String addFeatures(String [] parameters) throws Exception {
        if (!(currentTechnique instanceof FeatureSet)) {
            throw new Exception("The selected technique must be a feature set " );
        }

        FeatureSet fs = (FeatureSet) currentTechnique;

        if (parameters.length != 3) {
            throw new Exception("Must have exactly three parameters (statistic, channel, window");
        }

        //.. stat = 0; channeId =1; window =3
        String stat = parameters[0];
        String channel = parameters[1];
        String window = parameters[2];
        fs.addFeaturesFromConsole(stat, channel, window);
        return "Successfully added  " + fs.featureDescriptions.values().size() + " feature descripions";
    }
    
    /**
     * Handle: makeML("Jrip") makeFS("name") makeAS(type, numatts) type = none,
     * cfs, info. numatts = integer or ? for unknown makeSettings("").
     *
     * Machine learning algorithm is trivial. Rest will require considerable
     * thought. I imagine we will want them to be structured in a comparable
     * way. You make them by specifying ID. Select them, and then add to them
     */
    private String make(String input, String [] parameters, TechniquesDAO techniquesDAO) throws Exception {
        if (parameters.length == 0) {
            throw new Exception("Must specify parameter makeXX(something)");
        }
        String id = parameters[0];

        if (input.startsWith("makeml(") || input.startsWith("newml(")) {
            makeMLAlgorithm(id, techniquesDAO); //.. id is the name: jRip, J48, 
            return ("Successfully made machine learning algorithm " + id);
        } 
        
        else if (input.startsWith("makefs(") || input.startsWith("newfeatureset(")) {
            TechniqueDAO tDAO = new TechniqueDAO(new FeatureSet(id));
            techniquesDAO.addTechnique(id, tDAO);
            return ("Successfully made Feature Set " + id);
        } 

        //. MAKE Attribute Selection - Handle
        else if (input.startsWith("makeas(") || input.startsWith("newattributeselection(")) {
            if (parameters.length < 2 && (!(parameters[0].startsWith("none")))) {
                throw new Exception("Must specify how many attributes to keep as second parameter");
            }
            String numAtts = "?"; //.. default if this is no attribute selection
            if (parameters.length > 1) {
                numAtts = parameters[1];
            }
            int attrs;

            //.. its either ? or a number. Try to parse as integer
            try {
                attrs = Integer.parseInt(numAtts);
            } //.. if we failed, then set as -1
            catch (Exception e) {
                attrs = -1;
            }

            TechniqueDAO tDAO;

            //.. the input may not be permissable, we only allow none, cfs and info
            try {
                tDAO = new TechniqueDAO(new AttributeSelection(id, attrs));
            } catch (IllegalArgumentException e) {
                throw new Exception("There is no Attribute Selection algorithm titled " + id + " For now, there is cfs, info, and none");
            }
            techniquesDAO.addTechnique(tDAO.getId(), tDAO);
            return ("Successfully made AttributeSelection " + tDAO.getId());
        }

        throw new Exception("Unable to parse input " + input);

    }
    /**
     * Parse: makeML("jrip), etc
     */
    private void makeMLAlgorithm(String id, TechniquesDAO techniquesDAO) throws Exception {
        TechniqueDAO tDAO;
        try {
            tDAO = new TechniqueDAO(new WekaClassifier(id));
        } catch (IllegalArgumentException e) {
            throw new Exception("There is no machine learning algorithm titled " + id + " . For now, there is"
                    + "jrip, j48, lmt, nb, tnn, smo, simple, logistic, adaboost");
        }

        techniquesDAO.addTechnique(id, tDAO);
    }
    
     /**
     * Handle evaluate(), when the selected element is an Experiment.
     * evaluate(5) for evaluation across 5 folds Assume Techniques have been
     * populated via the interface.
     *
     * Bugs: multi-analysis. The retString classification accuracy doesnt owrk
     */
    private String evaluate(String [] parameters, DataLayerDAO dDAO, Performances performances) throws Exception {
        if (!(currentDataLayer instanceof Experiment || currentDataLayer instanceof MultiExperiment)) {
            throw new Exception("You must split the data into instances first, e.g. (split(labelName)");
        }

        //.. Get parameters if any -- how many folds. by default: -1, leave one out
        int numFolds = -1;
        if (parameters.length > 0) {
            numFolds = Integer.parseInt(parameters[0]);
        }

        //.. Add technique description to return string
        String retString = "Evaluating an experiment... ";
        /* ArrayList<TechniqueDAO> tDAOs = dDAO.tDAOs;
         for(TechniqueDAO tDAO : tDAOs){
         retString += "Technique:  " +tDAO.getId() +",";
         }*/

        //.. currently Experiment could either be MultiExperiment or Experiment
        if (currentDataLayer instanceof Experiment) {
            if (!dDAO.hasOneOfEachTechnique()) {
                throw new Exception(dDAO.getId() + " does not appear to be connected to all the necessary Evaluation Techniques. "
                        + "Please overlap the dataset with one of each");
            }

            //.. Get a Technique Set for every connected technique
            ArrayList<TechniqueSet> techniquesToEvaluate = this.getTechniquesForEvaluations(dDAO, performances);

            Experiment experiment = (Experiment) currentDataLayer;

            //.. get Dataset - either a new one or one stored in performancs
            Dataset dataset = getDatasetForEvaluations(experiment.id, performances);

            double total = 0;

            //.. finally, evaluate each techniqueset
            for (TechniqueSet t : techniquesToEvaluate) {
                System.out.println("Using " + t.getFeatureSet().getId() + " " + t.getFeatureSet().getConsoleString() + " " + t.getFeatureSet().getFeatureDescriptionString());

                experiment.evaluate(t, dataset, numFolds);
                total += t.getMostRecentAverage();
            }
            retString += "::Across all, %CORR: " + (total / techniquesToEvaluate.size());
            if (experiment.test) retString += ":: The percentage above reflects the average classification accuracy"
                    + " when the classifier was trained on all but one instance, which it used as a testing case, repeating"
                    + " this procedure once for each instance. The likely poor score reflects the fact that this data is in fact random"
                    + " ;;  "
                    + "You're on your own now!  Type help to get all the implemented commands.  "; 

            return retString;
        } else if (currentDataLayer instanceof MultiExperiment) {
            QuadDAO qDAO = (QuadDAO) dDAO;
            MultiExperiment multi = (MultiExperiment) currentDataLayer;

            //.. set daoWithTechniques to the first datalayer that is connected 
            DataLayerDAO daoWithTechniques = null;
            for (TriDAO pDAO : qDAO.piles) {
                if (pDAO.hasOneOfEachTechnique()) {
                    //.. throw an exception if more than one complete technique set is connected
                    if (daoWithTechniques != null) {
                        throw new Exception("At least two datalayers are connected to a complete set of techniques. Please connect only one and the rest will be evaluated using the same TechniqueSet");
                    }

                    daoWithTechniques = pDAO;
                }
            }

            //.. throw exception if none are connected
            if (daoWithTechniques == null) {
                throw new Exception("None of the selected datalayers appear to be connected to all the necessary Evaluation Techniques. "
                        + "Please overlap the dataset with one of each");
            }

            //.. Get a Technique Set for every connected technique with the one that is connected 
            ArrayList<TechniqueSet> techniquesToEvaluate = this.getTechniquesForEvaluations(daoWithTechniques, performances);

            //.. get a dataset for each experiment to be evaluated. Attach it to the underlying experiment (this is the non-obvious part)
            for (TriDAO pDAO : qDAO.piles) {
                Experiment thisE = (Experiment) pDAO.dataLayer;
                thisE.setDataset(getDatasetForEvaluations(thisE.id, performances));
            }

            //.. evaluate each techniqueset on each experiment
            for (TechniqueSet t : techniquesToEvaluate) {
                multi.evaluate(t, numFolds);
            }
            double total = 0; //.. pct correct

            //.. retrieve each of the selected datasets and sum their stats
            for (TriDAO pDAO : qDAO.piles) {
                Experiment thisE = (Experiment) pDAO.dataLayer;
                Dataset d = thisE.getDataSet();
                total += d.getMostRecentAverage();
            }

            retString += "::Across all, %CORR: " + (total / qDAO.piles.size());
            return retString;
        }
        return "Unexpected evaluation failure. Actually, unreachable statement";
    }

    /**
     * See if an experiment has been evaluated before if it has use that
     * Dataset, otherwise return a new one.
     */
    private Dataset getDatasetForEvaluations(String id, Performances performances) throws Exception {
        //.. see if an experiment with this id has been evaluated before
        //... and if not create a new Dataset for it
        Dataset dataset = performances.getDataSet(id);
        if (dataset == null) {
            dataset = new Dataset(id);
            performances.addNewDataset(dataset);
        }
        return dataset;
    }

    /**
     * EvaluateExperiment helper -- Create a TechniqueSet for each of the
     * techniques which intersect with the datalayer. 
     *
     */
    private ArrayList<TechniqueSet> getTechniquesForEvaluations(DataLayerDAO dDAO, Performances performances) throws Exception {

        //.. All the techniques connected to this datalayer organized by sort
        //... For each techniqueset we want one of each
        ArrayList<ClassificationAlgorithm> classifiers = dDAO.getClassifiers();
        ArrayList<AttributeSelection> attributeSelections = dDAO.getAttributeSelections();
        ArrayList<FeatureSet> fSets = dDAO.getFeatureSets();
        ArrayList<TechniqueSet> techniqueSets = new ArrayList();

        //.. Create a techniqueSet for each combination.
        //... Only one will be created if we have one of each
        for (ClassificationAlgorithm classifier : classifiers) {
            for (AttributeSelection aSelection : attributeSelections) {
                for (FeatureSet fs : fSets) {
                    TechniqueSet ts = new TechniqueSet(classifier, aSelection, fs);
                    techniqueSets.add(ts);
                }
            }
        }

        ArrayList<TechniqueSet> techniquesToEvaluate = new ArrayList();

        //.. We've created a technique set for every permissable permutation, but
        //... some of these TechniqueSets may have been duplicates of an existing techniqueset
        //.... in which case we did not want to create a new object, but just wanted to 
        //..... extract its id. So replace the techniquesets withthe same id with existing
        //...... techniquesets in performance
        for (TechniqueSet newTSet : techniqueSets) {
            TechniqueSet existingTSet = performances.getTechniqueSet(newTSet.getId());

            //.. if this is the first evaluation with this technique set, add it to performances
            if (existingTSet == null) {
                performances.addNewTechniqueSet(newTSet);
            } //.. otherwise extract it from performances, and replace the one we just created with it
            else {
                newTSet = existingTSet;
            }

            techniquesToEvaluate.add(newTSet);
        }

        return techniquesToEvaluate;
    }

        /**
     * Handle; anchor(); Make a new experiment with the start point of each
     * instance set at 0 anchor(t) = make a new copy. anchor(false) = manipulate
     * same level and all its derived from This produces a massive BUG if we run
     * it with false. Even though it appears to ahve manipulated the datalayer,
     * it in fact makes it identical in evaluation to the non-anchored
     * technique. This is a controller problem as things work fine with this in
     * the backend. The problem is related either to how ARFF files are written
     * out in the web mode (I dont even know where they go) or the fac that we
     * arent changing ids or some mysterious copy lingering in the server that
     * gets used when we try to evaluate.
     */
    private String anchor(String [] parameters) throws Exception {
        String retString = "";
        boolean copy = true;

        //.. if first parameter starts with t, set copy to true else false
        if (parameters.length > 0) {
            String tOrF = parameters[0];
            if (tOrF.startsWith("t")) {
                copy = true;
            } else {
                copy = false;
            }
        }

        //.. for every selected experiment
        for (Experiment exp : this.getExperiments()) {
            Experiment e = exp.anchorToZero(copy);
            if (copy) {
                e.setId(exp.id + "start0");
                e.setParent(exp.getId()); //.. set parent to what we derived it from

                //.. make a new data access object, and add it to our stream
                TriDAO pDAO = new TriDAO(e);
                ctx.dataLayersDAO.addStream(e.id, pDAO);
                retString += "Created : " + e.getId() + " with " + e.matrixes.size() + " instances::";
            } else {
                retString += "Changed raw values of " + e.getId() + " and its parents. ";
            }
        }
        return retString;
    }
    
    
    /**
     * Handle train(), when the selected element is an Experiment.
     * evaluate(5) for evaluation across 5 folds Assume Techniques have been
     * populated via the interface.
     *
     * Bugs: multi-analysis. The retString classification accuracy doesnt owrk
     */
    private String train(String[] parameters, DataLayerDAO dDAO, Performances performances) throws Exception {
        
        if (!(currentDataLayer instanceof Experiment || currentDataLayer instanceof MultiExperiment)) {
            throw new Exception("You must split the data into instances first, e.g. (split(labelName)");
        }

        //.. Add technique description to return string
        String retString = "Training classifer ... ";
       
        //.. currently Experiment could either be MultiExperiment or Experiment
        if (currentDataLayer instanceof Experiment) {
            if (!dDAO.hasOneOfEachTechnique()) {
                throw new Exception(dDAO.getId() + " does not appear to be connected to all the necessary Evaluation Techniques. "
                        + "Please overlap the dataset with one of each");
            }

            //.. Get a Technique Set for every connected technique
            ArrayList<TechniqueSet> techniquesToEvaluate = this.getTechniquesForEvaluations(dDAO, performances);
            
            if (techniquesToEvaluate.size() > 1) throw new Exception("Since it's connected to more than one of every technique"
                    + " it is ambiguous which is connected to what");
            Experiment experiment = (Experiment) currentDataLayer;

            //.. get Dataset - either a new one or one stored in performancs
            Dataset dataset = getDatasetForEvaluations(experiment.id, performances);

            double total = 0;

            //.. Train the classifier but also evaluate internally, so the user has some notion of
            //... how good it is
            for (TechniqueSet t : techniquesToEvaluate) {
                // System.out.println("Using " + t.getFeatureSet().getId() + " " + t.getFeatureSet().getConsoleString() + " " + t.getFeatureSet().getFeatureDescriptionString());
                experiment.evaluate(t, dataset, -1);
                WekaClassifier wc = experiment.train(t);
                total += t.getMostRecentAverage();
            }
            retString += "The internal accuracy of this classifier in leave-one-out was " + (total / techniquesToEvaluate.size());

            return retString;
        } 
        
        else if (currentDataLayer instanceof MultiExperiment) {
            if (true) throw new Exception(" NOT YET IMPLEMENTED ");
            
            QuadDAO qDAO = (QuadDAO) dDAO;
            MultiExperiment multi = (MultiExperiment) currentDataLayer;

            //.. set daoWithTechniques to the first datalayer that is connected 
            DataLayerDAO daoWithTechniques = null;
            for (TriDAO pDAO : qDAO.piles) {
                if (pDAO.hasOneOfEachTechnique()) {
                    //.. throw an exception if more than one complete technique set is connected
                    if (daoWithTechniques != null) {
                        throw new Exception("At least two datalayers are connected to a complete set of techniques. Please connect only one and the rest will be evaluated using the same TechniqueSet");
                    }
 
                    daoWithTechniques = pDAO;
                }
            }

            //.. throw exception if none are connected
            if (daoWithTechniques == null) {
                throw new Exception("None of the selected datalayers appear to be connected to all the necessary Evaluation Techniques. "
                        + "Please overlap the dataset with one of each");
            }

            //.. Get a Technique Set for every connected technique with the one that is connected 
            ArrayList<TechniqueSet> techniquesToEvaluate = this.getTechniquesForEvaluations(daoWithTechniques, performances);

            //.. get a dataset for each experiment to be evaluated. Attach it to the underlying experiment (this is the non-obvious part)
            for (TriDAO pDAO : qDAO.piles) {
                Experiment thisE = (Experiment) pDAO.dataLayer;
                thisE.setDataset(getDatasetForEvaluations(thisE.id, performances));
            }

            //.. evaluate each techniqueset on each experiment
            for (TechniqueSet t : techniquesToEvaluate) {
                multi.evaluate(t, -1);
            }
            double total = 0; //.. pct correct

            //.. retrieve each of the selected datasets and sum their stats
            for (TriDAO pDAO : qDAO.piles) {
                Experiment thisE = (Experiment) pDAO.dataLayer;
                Dataset d = thisE.getDataSet(); 
                total += d.getMostRecentAverage();
            }

            retString += "::Across all, %CORR: " + (total / qDAO.piles.size());
            return retString;
        }
        return "Unexpected evaluation failure. Actually, unreachable statement";
    }
    
    private String classify(String [] parameters, DataLayerDAO dDAO, Performances performances) throws Exception{
        
        if(!(currentDataLayer instanceof ChannelSet)) throw new Exception("The command classify only "
                + "applies to 2D Channelsets " + currentDataLayer.id + " doesn't fit that bill");
        ChannelSet cs = (ChannelSet) currentDataLayer;
        
        
        //.. retrieve the hovered over classifier, and bitch if somethings wrong
        ArrayList<ClassificationAlgorithm> classifiers =  dDAO.getClassifiers();
        if (classifiers.size() >1) throw new Exception("It is ambiguous which classifier you want to use");
        if (classifiers.isEmpty()) throw new Exception("You must connect the dataset with a trained classifier");

        WekaClassifier classifier = (WekaClassifier) classifiers.get(0);
        
        //.. By default, read every shuold be as long as there are instnces
        int readEvery = classifier.lastInstanceLength;
        if (parameters.length>0) readEvery = Integer.parseInt(parameters[0]);
        if(cs.getMinPoints() < readEvery) throw new Exception("There is not enough space to create even one instance. Testing must be larger");

       //..Classify the  
       Predictions p = classifier.testRealStream(classifier.lastTrainedClassification,
               classifier.lastTechniqueTested, this.getDatasetForEvaluations(dDAO.getId(), 
               performances), cs, classifier.lastInstanceLength, readEvery, classifier.lastAsAlgosUsed); 
        //.. Some time a very long time ago, I thought it would be OK to set this to null, and not 
       //.. remind myself that this would fuck up attribute selection. Today I paid the hard price for that. 
       
       p.setId(dDAO.getId());//.. this is an exception, since here we actualyl do want to set the id
       performances.addNewPredictionsSet(p); 
       return "Successfully classified this dataset, and made " + p.predictions.size() + " predictions, saved in" + performances.getPredictionSet(dDAO.getId()).getId();
    }
    
    /**Predicts the last K readings of the dataset, and returns confidence and value if possible.
     * If this is used in conjunction with a database, synchronize with database first. 
     **/
    private String classifyLast(String [] parameters, DataLayerDAO dDAO) throws Exception{

        if (!(currentDataLayer instanceof ChannelSet)) {
            throw new Exception("The command classify only "
                    + "applies to 2D Channelsets " + currentDataLayer.id + " doesn't fit that bill");
        }
        ChannelSet cs = (ChannelSet) currentDataLayer;

        //.. retrieve the hovered over classifier, and bitch if somethings wrong
        ArrayList<ClassificationAlgorithm> classifiers = dDAO.getClassifiers();
        if (classifiers.size() > 1) 
            throw new Exception("It is ambiguous which classifier you want to use");
        
        if (classifiers.isEmpty()) 
            throw new Exception("You must connect the dataset with a trained classifier");
        

        WekaClassifier classifier = (WekaClassifier) classifiers.get(0);

        //..Classify the  
         Prediction p = classifier.getLastPrediction(cs );
         return p.toString();
    }
    public String glassroutes(String[] parameters) throws Exception {
        float lowpass = 0;
        float highpass = 0;
        if (parameters.length > 1) {
            lowpass = Float.parseFloat(parameters[0]);
            highpass = Float.parseFloat(parameters[1]);
        } else if (parameters.length > 0) {
            lowpass = Float.parseFloat(parameters[0]);  
        }
        ArrayList<ChannelSet> chanSets = getChanSets();
        String retString = "";
        for (ChannelSet cs : chanSets) {
            ChannelSet filteredSet = cs.calcOxy(false, null, null); //.. we want a copy;
            retString += "Applied CalcOxy, so that 0->7 : Probe A. 8->15"
                    + "                + \" ProbeB:: 0->3&8->11 : HbO.:: 4->7, 12->15: Hb lower values within"
                    + " the probe correspond to closer distances to the source:::";
            //if (lowpass == 0) {
                filteredSet = filteredSet.movingAverage(10, false);
                retString += "Applied MovingAverage, 10 readings back::";
            //}

            if (lowpass > 0 && highpass == 0) {
                filteredSet = filteredSet.lowpass(lowpass, false);
                retString += "Applied Lowpass; Removed frequencies oscillating at above " + lowpass + "hz ::";
            } else if (highpass > 0 && lowpass == 0) {
                filteredSet = filteredSet.highpass(highpass, false);
                retString += "Applied Highpass; Removed frequencies oscillating below " + highpass + "hz ::";
            } else if (lowpass > 0 && highpass > 0) {
                filteredSet = filteredSet.bandpass(lowpass, highpass, false);
                retString += "Applied Bandpass; kept frequencies oscillating between " + lowpass + " and " + highpass + "hz ::";
            }
     
            filteredSet = filteredSet.zScore(false);
            retString += "Z scored the data, so that each value is replaced by the difference between "
                    + " it and the channel's corresponding mean, divided by the standard deviation::";

            //.. Split into an experiment - of course this is not perfectly generalizable, so condition name should be parameter               
            Experiment e = super.getExperiment(filteredSet, "condition");
            ArrayList<String> toKeep = new ArrayList();
            toKeep.add("easy");
            toKeep.add("hard");
            e = e.removeAllClassesBut(toKeep);

            //.. remove instances 10 percent larger than the average
            int instLength = e.getMostCommonInstanceLength();
            int origSize = e.matrixes.size();
            e = e.removeUnfitInstances(instLength, 0.1, false);
            int trimmed = e.trimUnfitInstances(instLength);
            int newSize = e.matrixes.size();
            if (origSize != newSize) {
                retString += "Experiment changed from " + origSize + " to " + newSize + " instances::";
            }

            //.. anchor it, setting start to zero
            e = e.anchorToZero(false);
            e.setParent(cs.getId()); //.. set parent to what we derived it from

            e.setId(e.id + "-l" + lowpass + "-h" + highpass);
            
            //.. make a new data access object, and add it to our stream
            TriDAO pDAO = new TriDAO(e);

            ctx.dataLayersDAO.addStream(e.id, pDAO);

            retString += " Creating : " + e.getId() + " with " + e.matrixes.size() + " instances::"
                    + super.getColorsMessage(e);
        }
        return retString;
    }
    public String fnirs(String [] parameters) throws Exception{
        float lowpass =0;
        float highpass=0;
        if (parameters.length > 1) {
            lowpass = Float.parseFloat(parameters[0]);
            highpass = Float.parseFloat(parameters[1]);
        }
        else if (parameters.length >0) {
            lowpass = Float.parseFloat(parameters[0]);
        }
        ArrayList<ChannelSet> chanSets = getChanSets();
        String retString = "";
        for (ChannelSet cs : chanSets) {
            ChannelSet filteredSet = cs.calcOxy(false, null, null); //.. we want a copy;
            retString += "Applied CalcOxy, so that 0->7 : Probe A. 8->15" +
"                + \" ProbeB:: 0->3&8->12 : HbO at even positions, and Hb at odd if zero-indexed; lower values within"
                    + " the probe correspond to closer distances to the source:::";
            if(lowpass ==0){ 
                filteredSet = filteredSet.movingAverage(10, false);
                retString += "Applied MovingAverage, 10 readings back::";
            }
              
            if(lowpass >0 && highpass ==0){
                filteredSet = filteredSet.lowpass(lowpass, false);
                retString += "Applied Lowpass; Removed frequencies oscillating at above " +lowpass + "hz ::";
            }
            
            else if(highpass >0 && lowpass ==0) {
                filteredSet = filteredSet.highpass(highpass, false);
                retString += "Applied Highpass; Removed frequencies oscillating below " + highpass + "hz ::";
            } 
            
            else if(lowpass >0 && highpass >0) {
                filteredSet = filteredSet.bandpass(lowpass,highpass, false);
                retString += "Applied Bandpass; kept frequencies oscillating between " + lowpass +" and " + highpass + "hz ::";
            }
           
            
            filteredSet = filteredSet.zScore(false);
            retString += "Z scored the data, so that each value is replaced by the difference between "
                    + " it and the channel's corresponding mean, divided by the standard deviation::";
            
            //.. Split into an experiment - of course this is not perfectly generalizable, so condition name should be parameter
            Experiment e = filteredSet.splitByLabel("condition");
            ArrayList<String> toKeep = new ArrayList();
            toKeep.add("easy");
            toKeep.add("hard");
            toKeep.add("rest"); 
            e = e.removeAllClassesBut(toKeep);
          
            //.. remove instances 10 percent larger than the average
            int instLength = e.getMostCommonInstanceLength();
            int origSize = e.matrixes.size();
            e = e.removeUnfitInstances(instLength, 0.1, false);
            int newSize = e.matrixes.size();
            if(origSize != newSize)
                retString += "Experiment changed from " + origSize +" to " + newSize+ " instances::";
            
            //.. anchor it, setting start to zero
            e = e.anchorToZero(false);
            e.setParent(cs.getId()); //.. set parent to what we derived it from

            e.setId(e.id + "-l"+lowpass+"-h"+highpass);
            //.. make a new data access object, and add it to our stream
            TriDAO pDAO = new TriDAO(e);
            
            ctx.dataLayersDAO.addStream(e.id, pDAO);
            
            retString += " Creating : " + e.getId() + " with " + e.matrixes.size() + " instances::" +
                    super.getColorsMessage(e);
        }
        return retString;
    }
    
    public String wireless(String[] parameters) throws Exception {
        float lowpass = 0;
        float highpass = 0;
        if (parameters.length > 1) {
            lowpass = Float.parseFloat(parameters[0]);
            highpass = Float.parseFloat(parameters[1]);
        } else if (parameters.length > 0) {
            lowpass = Float.parseFloat(parameters[0]);
        }
        ArrayList<ChannelSet> chanSets = getChanSets();
        String retString = "";
        for (ChannelSet cs : chanSets) {
            
            ChannelSet filteredSet = cs.zScore(true);
            if (lowpass == 0) {
                filteredSet = filteredSet.movingAverage(10, false);
                retString += "Applied MovingAverage, 10 readings back::";
            }

            if (lowpass > 0 && highpass == 0) {
                filteredSet = filteredSet.lowpass(lowpass, false);
                retString += "Applied Lowpass; Removed frequencies oscillating at above " + lowpass + "hz ::";
            } else if (highpass > 0 && lowpass == 0) {
                filteredSet = filteredSet.highpass(highpass, false);
                retString += "Applied Highpass; Removed frequencies oscillating below " + highpass + "hz ::";
            } else if (lowpass > 0 && highpass > 0) {
                filteredSet = filteredSet.lowpass(lowpass, false);
                filteredSet = filteredSet.highpass(highpass, false);
                retString += "Applied Bandpass; kept frequencies oscillating between " + lowpass + " and " + highpass + "hz ::";
            }

            retString += "Z scored the data, so that each value is replaced by the difference between "
                    + " it and the channel's corresponding mean, divided by the standard deviation::";
 
            Experiment e = filteredSet.splitByLabel("Condition");
            ArrayList<String> toKeep = new ArrayList();
            toKeep.add("meditation");
            toKeep.add("multiplication");
            e = e.removeAllClassesBut(toKeep);

            e = e.anchorToZero(false);
            e.setParent(cs.getId()); //.. set parent to what we derived it from

            e.setId(e.id + "-l" + lowpass + "-h" + highpass);
            //.. make a new data access object, and add it to our stream
            TriDAO pDAO = new TriDAO(e);

            ctx.dataLayersDAO.addStream(e.id, pDAO);

            retString += " Creating : " + e.getId() + " with " + e.matrixes.size() + " instances::"
                    + super.getColorsMessage(e);
        }
        return retString;
    }
    
}
