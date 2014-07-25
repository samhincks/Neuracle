/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dao;

import dao.datalayers.BiDAO;
import dao.datalayers.TriDAO;
import dao.techniques.TechniqueDAO;
import filereader.Label;
import filereader.Labels;
import filereader.Markers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import org.json.JSONObject;
import stripes.ext.ThisActionBeanContext;
import timeseriestufts.kth.streams.DataLayer;
import timeseriestufts.kth.streams.bi.ChannelSet;
import timeseriestufts.kth.streams.quad.MultiExperiment;
import timeseriestufts.kth.streams.tri.Experiment;
import timeseriestufts.kth.streams.tri.TridimensionalLayer;

/**
 *
 * @author samhincks
 */
public class TransformationParser extends Parser{
    
    public TransformationParser(){
        commands = new Hashtable();
        /**
         * -- Every command this Parser handles should be added to commands with
         * a corresponding function for execution in the execute function--*
         */
        
        //-- RANDOMLY LABEL
        Command command = new Command("randomlylabel");
        command.documentation = "Randomly labels the selected dataset, without meaning (for testing)";
        command.parameters = "1[Optional]. trialLength = the length of the trial";
        commands.put(command.id, command);
        
        //-- LABEL
        command = new Command("label");
        command.documentation = "Typically called by a JS script and not the user. Alters the label being"
                + " assigned to incoming data, and if called for the first time labels all previous "
                + " data as baseline";
        command.parameters = "1. filename = the length of the trial, 2. labelName = the name of the label "
                + " 3. labelValue = the value of the label";
        commands.put(command.id, command);
        
        //-- KEEP
        command = new Command("keep");
        command.documentation = "With a split dataset selected, remove all datapoints except those "
                + " with labels specified comma-separated in parens ";
        command.parameters = "1.->n a list of labels to keep";
        command.action = "reload";
        commands.put(command.id, command);
        
        //-- SPLIT
        command = new Command("split");
        command.documentation = "With a raw channel set selected, split it, to transform it into "
                + " '3D' dataset organized in terms labels and channels";
        command.parameters = "conditionName = the condition to split by";
        command.action = "reload";
        commands.put(command.id, command);
        
    }

    public JSONObject execute(String command, String[] parameters, ThisActionBeanContext ctx, 
            DataLayer currentDataLayer,TechniqueDAO currentTechnique) throws Exception {
        this.ctx = ctx;
        this.currentDataLayer = currentDataLayer;
        Command c = null;

        if (command.startsWith("randomlylabel")) {
            c = commands.get("randomlylabel");
            c.retMessage = this.randomlyLabel(parameters);
        }

        else if (command.startsWith("label")) {
            c = commands.get("label");
            c.retMessage = this.label(parameters);
        }
        
        //... Makes a new experiment with only these instances
        else if (command.startsWith("keep")) {
            c = commands.get("keep");
            c.retMessage = this.keep(parameters);
        }
        //.. split(label2) transform a Channel Set to an Experiment by dividing
        else if (command.startsWith("split")) {
            c = commands.get("split");
            c.retMessage = this.split(parameters);
        }

        if (c == null) {
            return null;
        }
        return c.getJSONObject();
    }
    
     /**
     * Label the specified dataset at random. If specified, the second parameter
     * is how long each trial should be
     */
    private String randomlyLabel(String [] parameters) throws Exception {
        String filename = currentDataLayer.id;
        int trialLength = 10;
        if (parameters.length > 0) {
            trialLength = Integer.parseInt(parameters[0]);
        }

        if (ctx.dataLayersDAO.streams.containsKey(filename)) {
            BiDAO bDAO = (BiDAO) ctx.dataLayersDAO.get(currentDataLayer.id);

            if (bDAO.synchronizedWithDatabase) {
                bDAO.synchronizeWithDatabase(filename);
            }

            int numReadings = bDAO.getNumReadings();
            int numTrials = (int) (numReadings / (double) trialLength);
            Markers markers = Markers.generate(numTrials, trialLength);
            bDAO.addMarkers(markers);
            return "Added " + numTrials + " each consisting of" + numReadings;

            //TODO: Synchronize labels with database
        } else {
            throw new Exception("Context does not contain datalayer " + filename);
        }

    }
    
        /**
     * Handle: label(filename, curLabelName, curLabelValue). This is a like the
     * below method, typically called by a callback, but a user could also
     * trigger it. Makes so that data coming in receives the input label*
     */
    private String label(String [] parameters) throws Exception {
        if (parameters.length < 3) throw new Exception("Command requires three parameters. filename, curLabelName, curLabelValue");
        //.. In this new way of doing things, we are going to need to have created a Markers object,
        //.. which would always have a Markers object that was set equal to the number of datapoints
        String filename = parameters[0];
        String labelName = parameters[1];
        String labelValue = parameters[2];
        if (ctx.dataLayersDAO.streams.containsKey(filename)) {
            BiDAO bDAO = (BiDAO) ctx.dataLayersDAO.get(filename);
            if (bDAO.synchronizedWithDatabase) {
                bDAO.synchronizeWithDatabase(filename);
            }

            //.. the channelset associated with this object may or may not have markers, associated with it
            Labels labels = bDAO.getLabelsWithName(labelName);

            //.. if there is no markers yet, we need instantiate it and populate it with 
            //... # of reading corresponding to number of values, so that the new ones are in synch
            if (labels == null) {
                labels = new Labels(labelName);

                //.. bring it up to date with junk values 
                for (int i = 0; i < bDAO.getNumReadings(); i++) {
                    labels.addLabel(new Label(labelName, "junk", i));
                }
                bDAO.addLabels(labels);
            }
            bDAO.setStreamedLabel(labelName, labelValue);
            return "Added " + labelName + " " + labelValue;
        } else {
            throw new Exception("Could not find " + filename);
        }
    }
    
     /**
     * Handle: keep(x,y,z) removeAllBut(x,y,z) keeps only the instances with
     * classes x,y,z. Make a new experiment out of this and add to session
     */
    private String keep(String [] parameters) throws Exception {
        String retString = "";

        //.. remove listed classes from every experiment
        for (Experiment exp : super.getExperiments()) {
            Experiment e = exp.removeAllClassesBut(new ArrayList(Arrays.asList(parameters)));
            e.setId(exp.getId() + e.classification.id);
            e.setParent(exp.getId()); //.. set parent to what we derived it from

            //.. make a new data access object, and add it to our stream
            TriDAO pDAO = new TriDAO(e);
            ctx.dataLayersDAO.addStream(e.id, pDAO);

            //.. Generate a console message which includes num instance, num of each condition
            retString = "Created --" + e.getId() + "-- with " + e.matrixes.size() + " instances:: "
                    + " " + super.getColorsMessage(e); 
        }
        return retString;
    }
    
    /**
     * Command = SPLITBYLABEL(LabelName) or just Split(.... Make a bidimensional
     * layer or collection of bidimensional layer into experiment
     */
    private String split(String [] parameters) throws Exception {
        //.. second half should be "(xxxx)"
        String labelName = parameters[0];
        labelName = labelName.replace(")", ""); //.. remove )
        labelName = labelName.replace("\"", "");
        
        
        //.. get all chansets
        ArrayList<ChannelSet> chanSets = getChanSets();
        String retString = "";
        for (ChannelSet cs : chanSets) {
           //.. In case there is a channelset which has labels, but not markers, add markers
            //... the labels would be saved in the biDOA and this would only be used in conjunction with a DB
            BiDAO biDAO = (BiDAO) ctx.getDataLayers().get(cs.id);
            if (biDAO.labels != null) {
                for (Labels l : biDAO.labels) {
                    Markers m = new Markers(l);

                    //.. INVARIANT: # of markers should equal number of rows in each col
                    biDAO.addMarkers(m);
                }
            }
            
            //.. make the triplet layer
            retString += makeExperiment(cs, labelName);
        }

        return retString;
    }
    
   


    
}
