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
import realtime.LabelInterceptorTask;
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
    
    public TransformationParser(ThisActionBeanContext ctx) {
        super(ctx);
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
        
        // -- ASSIGNLABEL
        command = new Command("assignlabel");
        command.documentation = "A risky procedure unless 100% certain. Assigns labels according to a known pattern. "
                + " The first specified condition is presumed to start at the index of the first parameter and end y locations after"
                + " with input (condition:y), where the second condition continues. This pattern repeats through the entire dataset"
                + " unless an end location is specified within a colon-delineated first parameter  ";
        command.parameters = "assignlabel(132, easy:100, rest:30,hard:100) 1. Index of first relevant reading (everything else becomes baseline). 2. condition:length. 3. condition:length [...]";
        commands.put(command.id, command);
        
        //-- LABEL
        command = new Command("label");
        command.documentation = "Typically called by a JS script and not the user. Alters the label being"
                + " assigned to incoming data, and if called for the first time labels all previous "
                + " data as baseline";
        command.parameters = "1. filename = the length of the trial, 2. labelName = the name of the label "
                + " 3. labelValue = the value of the label";
        commands.put(command.id, command);
        
        command = new Command("interceptlabel");
        command.documentation = "Opens a server at specified port, so that when a label is pushed"
                + " to this location, future data pushed into specified database is labeled accordingly";
        command.parameters = "1. Dataset, 2. Labelname, 3. PortNum";
        commands.put(command.id, command);
        
        //-- KEEP
        command = new Command("keep");
        command.documentation = "With a split dataset selected, remove all datapoints except those "
                + " with labels specified comma-separated in parens ";
        command.parameters = "1.->n a list of labels to keep";
        command.action = "reload";
        command.tutorial = "Now you have an object with fewer trials -- specifically those which you may want to train"
                + " a machine learning algorithm on. Double click this new object, navigate through the various channels, "
                + " and click shift, to merge the visualization into an areachart where each area represents the average"
                + " signal of all the channels, with a height of one standard deviation. You will notice that the data appears"
                + " quite noisy. Maybe that's a problem, but maybe not. Some of those rapid osillations represent the user's "
                + " respiration and heart rate -- potential information for our machine learning algorithm. :: Drag this"
                + " new object so that it intersects the three tiny objects in the topright corner, which represent"
                + " choice of machine learning, attribute selection, and featureset.::  Then type evaluate()  ";
        commands.put(command.id, command);
        
        //-- SPLIT
        command = new Command("split");
        command.documentation = "With a raw channel set selected, split it, to transform it into "
                + " '3D' dataset organized in terms labels and channels";
        command.parameters = "conditionName = the condition to split by";
        command.action = "reload"; 
        command.tutorial = "If you double click on this newly derived object, you will see a visualization "
                + " of all the trials from where it started to where it ended. The baseline trial is "
                + " much longer than the others, and we aren't interested in the in between resting trials.::  "
                + " Type keep(easy,hard) to remove all trials except those with the desired conditions";
        commands.put(command.id, command);
        
        //-- PARTITION
        command = new Command("partition");
        command.documentation = "With a raw channel set selected, partition it into "
                + "a new channelset for each trial";
        command.parameters = "conditionName = the condition to partition on";
        command.action = "reload";
        commands.put(command.id, command);
        
        // -- MERGE
        command = new Command("append");
        command.documentation = "With multiple channels selected, append into a single datalayer";
        command.action = "reload";
        commands.put(command.id, command);
        
        // -- MERGE
        command = new Command("clean");
        command.documentation = "With an experiment selected, remove any instances which are longer than the mode";
        command.action = "reload";
        commands.put(command.id, command);
    }

    public JSONObject execute(String command, String[] parameters,
            DataLayer currentDataLayer,TechniqueDAO currentTechnique) throws Exception {
        this.currentDataLayer = currentDataLayer;
        Command c = null;

        if (command.startsWith("randomlylabel")) {
            c = commands.get("randomlylabel");
            c.retMessage = this.randomlyLabel(parameters);
        }

        if (command.startsWith("assignlabel")) {
            c = commands.get("assignlabel");
            c.retMessage = this.assignLabel(parameters);
        }
      
        else if (command.startsWith("label")) {
            c = commands.get("label");
            c.retMessage = this.label(parameters);
        }
        
        else if (command.startsWith("interceptlabel")) {
            c = commands.get("interceptlabel");
            c.retMessage = this.interceptLabel(parameters);
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

        //.. split(label2) transform a Channel Set to an Experiment by dividing
        else if (command.startsWith("partition")) {
            c = commands.get("partition");
            c.retMessage = this.partition(parameters);
        }
        
        //.. split(label2) transform a Channel Set to an Experiment by dividing
        else if (command.startsWith("partition")) {
            c = commands.get("partition");
            c.retMessage = this.partition(parameters);
        }
        
        else if (command.startsWith("append")) {
            c = commands.get("append");
            c.retMessage = this.append(parameters);
        }
        
        else if (command.startsWith("clean")) {
            c = commands.get("clean");
            c.retMessage = this.clean();
        }
        
        if (c == null) {
            return null;
        }
        return c.getJSONObject(ctx.getTutorial());
    }
    
    private String clean() throws Exception {
        String retString = "";
        ArrayList<Experiment> experiments = getExperiments();
        for (Experiment e : experiments) {
            Experiment e2 =  e.removeUnfitInstances(e.getMostCommonInstanceLength(), 0.1, true);
            ctx.addDataLayer(e2.id, new TriDAO(e2));
            e2.setParent(e.id);
            retString += "Cleaned " + e.id + ", transforming from " + e.matrixes.size() + " to " + e2.matrixes.size();
        }
        return retString;
    }
     
    /**Merge a set of selected channels into one**/
    private String append(String [] parameters) throws Exception {
        ArrayList<ChannelSet> chanSets = getChanSets();
        if (chanSets.size() <=1) chanSets =super.getAllChanSets();
        ChannelSet cs = chanSets.get(0).getDeepCopy();
        
        cs.id = "Merged" + chanSets.get(0).id;
        for (int i = 1; i < chanSets.size(); i++) {
            ChannelSet cs2 = chanSets.get(i);
            cs.appendChanSet(cs2);
            cs.id +=  "-"+cs2.id;
        }
        //.. make a new data access object, and add it to our stream
        BiDAO bDAO = new BiDAO(cs);
        ctx.dataLayersDAO.addStream(cs.id, bDAO);
        return "Appended  a total of " + chanSets.size() + " channelsets into" + cs.id;
      
    }
    
    private String assignLabel(String [] parameters) throws Exception {
        String baseline = parameters[0];
        String[] values = baseline.split(":");
        int start = Integer.parseInt(values[0]);
        String retMessage = "Assigning labels to channelset ";

        int end=-1; //.. where to stop
        if (values.length > 1) 
            end = Integer.parseInt(values[1]);
       
        ArrayList<ChannelSet> css = super.getAllChanSets();
        for (ChannelSet cs : css) {
            Markers m;
            if (end == -1) {
                end = cs.getMinPoints();
                m = Markers.make(start, end, end, Arrays.copyOfRange(parameters, 1, parameters.length));
            }
            else {
                m = Markers.make(start, end, cs.getMinPoints(), Arrays.copyOfRange(parameters, 1, parameters.length));
            }
            BiDAO bDAO = (BiDAO) ctx.dataLayersDAO.get(cs.id);
            bDAO.addMarkers(m);
            retMessage += bDAO.getId() +"  ";
        }
        
        return retMessage;
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
    public String label(String [] parameters) throws Exception {
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
                for (int i = 0; i < bDAO.getNumReadings()-1; i++) {
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
    
    private String interceptLabel(String[] parameters) throws Exception {
        if (parameters.length < 3) 
            throw new Exception("Command requires three parameters. databasename, labelName, portnum");
        String dbName = parameters[0];
        String labelName = parameters[1];
        int port = Integer.parseInt(parameters[2]);
        if (!(Parser.available(port))) throw new Exception("Port " + port + " is not available");
        
        int pingDelay =1000;
        label(new String []{dbName, labelName, "junk"});
        LabelInterceptorTask lt = new LabelInterceptorTask(port, dbName, labelName, this,pingDelay );

        Thread t = new Thread(lt);
        t.start();

        return "Initialized label interception at port " + port + " . " +dbName + "'s " + labelName + " will "
                + " potentially alter label based on the message every " + pingDelay + " ms. It will "
                + " shut down if it receives end";
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
            
            if (exp.test) retString+= ":: Again, you can try to see if your naked eye"
                    + " can distinguish meaningful differences in any of the trial-averaged channels of"
                    + " this random dataset. You can also use machine learning to evaluate any differences. ;; "
                    + " Drag the red, blue, and green circles so that they intersect the most recently created "
                    + " instance-grouped dataset. Then type evaluate .";
        }
        return retString;
    }
    
    private String partition(String [] parameters) throws Exception {
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

            //.. make the ChannelSets layer
            retString += makeChannelSets(cs, labelName);
            
            

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
            if (cs.test) {
                retString += ";;  Your former dataset has inherited a child with three groups of instances. "
                        + " If you double click this dataset, you'll see each trial organized as a trial, colored by its"
                        + " condition. Press shift to average these trials together to visualize any condition-dependent trends;; "
                        + " Once you've done this - try limiting your analysis to the conditions a and b, by typing keep(a,b). ";                       
            }
            
        }
        return retString;
    }

    
   


    
}
