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
import filereader.Markers.Trial;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Random;
import org.json.JSONObject;
import realtime.AudioNBack;
import realtime.Client;
import realtime.LabelInterceptorTask;
import stripes.ext.ThisActionBeanContext;
import timeseriestufts.evaluatable.FeatureSet;
import timeseriestufts.kth.streams.DataLayer;
import timeseriestufts.kth.streams.bi.ChannelSet;
import timeseriestufts.kth.streams.bi.ChannelSet.Tuple;
import timeseriestufts.kth.streams.quad.MultiExperiment;
import timeseriestufts.kth.streams.tri.Experiment;
import timeseriestufts.kth.streams.tri.TridimensionalLayer;
  
/*For commands relating to converting data from one format to another. 
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
        command.documentation = "A 'risky' procedure unless 100% certain. Assigns labels according to a known pattern. "
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
        
        
        command = new Command("retrolabel");
        command.documentation = "Retroactively set the value of some existing or non-existing condition to a specific value, matching the length"
                + " of the trial in another condition k trials back";
        command.parameters = "1. newConditionName, 2. oldConditionName, 3. value, 4. kConditionsBack, 5=filename";
        commands.put(command.id, command);
        
        
        //-- KEEP
        command = new Command("keep");
        command.documentation = "With a split dataset selected, remove all datapoints except those "
                + " with labels specified comma-separated in parens ";
        command.parameters = "1.->n a list of labels to keep";
        command.action = "reload";
        command.tutorial = "Now you have an object with fewer trials -- specifically those which you may want to train"
                + " a machine learning algorithm on. Double click this new object, navigate through the various channels, "
                + " and click shift to merge the visualization into an areachart where each area represents the average"
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
                + " much longer than the others, and we aren't interested in the separating resting trials.::  "
                + " Type keep(easy,hard) to remove all trials except those with the desired conditions";
        commands.put(command.id, command);
        
        //-- PARTITION
        command = new Command("partition");
        command.documentation = "With a raw channel set selected, partition it into "
                + "a new channelset for each trial. Or if an experiment is selected, partition into different parts of the instance";
        command.parameters = "conditionName = the condition to partition on, if channelset. #partitions if experiment";
        command.action = "reload";
        commands.put(command.id, command);
        
        //-- PLUCK
        command = new Command("pluck");
        command.documentation = "With a 3D dataset selected, divide it into two new groups: one with all the "
                + "instances which exceed a feature-value, and one where it does not";
        command.parameters = "1. statistic, 2. channel, 3. slope, 4 a cut-off value";
        command.action = "reload";
        commands.put(command.id, command);
        
        // -- MERGE
        command = new Command("append");
        command.documentation = "With multiple channels selected, append into a single datalayer";
        command.tutorial = "Now it's as though all the data were read from one file. Notice that a larger"
                + " object means it has a larger size. When your surface appears too cluttered, it makes sense to "
                + " delete objects using the delete, clear, and hold commands to free up memory (The heap is finite!)::"
                + " Write im into the console, but dont hit enter just yet! Instead hit the tab-button to autocomplete the command"
                + " The tab button will print every command that matches the current input. If you hit tab without any input, "
                + " then it will give documentation for every command that exists. :: The command imagent executes a prepared"
                + " set of data manipulation procedures to the data, custom built for detecting cognitive workload"
                + " using the Imagent fNIRS. :: Select the largest object, then type imagent in the console. ";
        command.action = "reload";
        commands.put(command.id, command);
        
        // -- Clean
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
        
        
        
        else if (command.startsWith("retrolabel")) {
            c = commands.get("retrolabel");
            c.retMessage = this.retroLabel(parameters);
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
        
        else if (command.startsWith("pluck")) {
            c = commands.get("pluck");
            c.retMessage = this.pluck(parameters);
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
        return c.getJSONObject(ctx.getTutorial(), ctx.getSelfCalibrate());
    }
    
    /**Remove instances which are too long or too short
     * @return
     * @throws Exception 
     */
    private String clean() throws Exception {
        String retString = "";
        ArrayList<Experiment> experiments = getExperiments(true);
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
        ArrayList<ChannelSet> chanSets = getChanSets(true);
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
    
    /**Assign a label according to a pattern to the selected dataset
     * @param parameters
     * @return
     * @throws Exception 
     */
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
        String retString ="";
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
            String [] vals = labelValue.split("%");
            String name = vals[0];
            bDAO.setStreamedLabel(labelName, name);//.. for launching the nback we pass along this something after %
           
            //.. a little hack: start an nback if its one of these conditions
            if (labelValue.startsWith("easy") || labelValue.startsWith("hard")) {
                try{
                    //.. split up input
                    int time =  Integer.parseInt(labelValue.split("%")[1]); 
                    String condition = labelValue.split("%")[0];
                    
                    //.. initialize nback, remembering what file we play
                    AudioNBack nBack;
                    time -=1000;
                    int NUMFILES = 4;//.. err, little messy.
                    int sequence = (int) (Math.random() * NUMFILES);
                    nBack = new AudioNBack(-1, time,sequence);  
                    
                    if (!ctx.test) nBack.directory = ctx.getServletContext().getRealPath("WEB-INF/audio/") +"/";

                    //.. Initialize nBack and run it for specified duration. It will complain if theres not a server running
                    Thread t = new Thread(nBack);
                    t.start();
                    retString += "Initializing nback sequence-" + nBack.sequence + "-"+condition+"::";
                    
                    //.. finally, save the nback, so that we can interrupt it
                    ctx.setNback(nBack);

                }catch (Exception e ) { throw new Exception (e.getMessage() );}//.. if anything went wrong here that's fine. A good faith effort to start the audio}
            }
            
            //.. Back up the file
            // WRITE BACKUP HERE. 
            String file = ctx.getServletContext().getRealPath("");
            file += "/output/backup.csv";
            if (file != null) {
                ChannelSet cs = (ChannelSet)bDAO.dataLayer;
                try{
                     cs.writeToFile(file, 1, false);
                }
                catch(Exception e) {System.err.println("Ddidnt backup");}
            }

            return retString + "Starting: " + name;
        } else {
            throw new Exception("Could not find " + filename);
        }
    }
    
    /** With a label coming from another port, intercept and label the current data
     * @param parameters
     * @return
     * @throws Exception 
     */
    private String interceptLabel(String[] parameters) throws Exception {
        String dbName;
        String labelName; 
        int port;
        if (parameters.length < 3) {
            dbName = "realtime1";
            labelName = "condition";
            Random generator = new Random();
            port = generator.nextInt(1500) + 1000;
        }        
        else{
            dbName = parameters[0];
            labelName = parameters[1];
            port = Integer.parseInt(parameters[2]);
        }
        if (!(Parser.available(port))) throw new Exception("Port " + port + " is not available");
        
        //.. set the current port to this - but what if we do it twice (I think it's fine since we're only using it to pop the port for better interaction
        ctx.curPort = port;
        int pingDelay =1000; //.. this has got to be how often we read from the port
        
        //.. label everything retroactively as junk for this label. 
        label(new String []{dbName, labelName, "junk"});
        
        //.. open a port in a new thread to repeatedly check if theres a new way to label incoming data
        LabelInterceptorTask lt = new LabelInterceptorTask(port, dbName, labelName, this,pingDelay );
        Thread t = new Thread(lt);
        t.start();
       
        return "Initialized label interception at port " + port + " . " +dbName + "'s " + labelName + " will "
                + " potentially alter label based on the message every " + pingDelay + " ms. It will "
                + " shut down if it receives end";
    }
    
    /*Retroactively label a potentially streaming file, to have some new value for some potentially new condition.
    e.g retrolabel(skit, condition, kuke, 0, realtime1)*/
    private String retroLabel(String [] parameters) throws Exception {
        String thisCondition = parameters[0];
        String otherCondition = parameters[1];
        String conValue = parameters[2];
        int numBack = Integer.parseInt(parameters[3]);
        String fileName = parameters[4];
        
        //.. get appropriate dataset
        BiDAO bDAO = (BiDAO) ctx.dataLayersDAO.get(fileName);
        ChannelSet cs = (ChannelSet)bDAO.dataLayer;
        
        //.. get markers and labels
        Markers otherMarkers = cs.getMarkersWithName(otherCondition);
        Labels theseLabels; 
        int otherSize = otherMarkers.saveLabels.channelLabels.size();
        Trial otherTrial  =otherMarkers.getKthLastTrial(numBack);
        
        //.. if this is the first time we see this condition, make a copy of the old ones,
        if (!(cs.hasMarkersWithName(thisCondition))) {
            ///.. create a new label for each position in the old markers
            theseLabels = new Labels(thisCondition);
            for (int i = 0; i < otherSize; i++) {
                theseLabels.addLabel(new Label(thisCondition, "junk", i));
            }
            
            //.. and alter those which we wnat to alter
            for (int s = otherTrial.start; s < otherTrial.end; s++) {
                Label l = theseLabels.channelLabels.get(s);
                l.value = conValue;
            }
            //.. finally add the markers
            cs.addMarkers(new Markers(theseLabels));
        }
        
        //.. I wonder if this is thread safe :/ 
        //.. we have to preserve what was already there, and update to be as long as our markers
        else {
            theseLabels = cs.getMarkersWithName(thisCondition).saveLabels;
            int thisSize = theseLabels.channelLabels.size();
            //.. we probably have fewer of this than the other one, so we will junkify until we get the trial thats k back,
            //.. then make it that condition corresponding to the trial, then junkify the rest to
            if (thisSize < otherSize) {
                    
                //.. add junk up until the start of the trial we are padding as
                int startPadding = otherTrial.start - thisSize;
                for (int i = 0; i < startPadding; i++) {
                    theseLabels.addLabel(new Label(thisCondition, "junk", thisSize +i));
                }
                
                if (startPadding < 0) throw new Exception("Start padding is 0");
                
                //..  update size
                thisSize = theseLabels.channelLabels.size();
                
                if (thisSize > otherSize) throw new Exception("now we're bigger than the old one");
                
                //.. then add whatever we want the condition to be
                for (int i = 0; i < otherTrial.getLength(); i++) {
                    theseLabels.addLabel(new Label(thisCondition, conValue , thisSize + i));
                }
                
                //.. update size
                thisSize = theseLabels.channelLabels.size();
                int sizeDifference = otherSize - thisSize;

                if (sizeDifference <0) throw new Exception (" after adding trial we're bigger than the old");
                
                //.. finally pad the end to be as large
                for (int i = 0; i < sizeDifference; i++) {
                    theseLabels.addLabel(new Label(thisCondition, "junk", thisSize + i));
                }
                
                //.. and then add these new markers, replacing the old ones 
                Markers m = new Markers(theseLabels);
                cs.removeMarkerWithName(theseLabels.labelName);
                cs.addMarkers(m);
                return "Registered " + conValue + " to " + theseLabels.labelName;
            }
            else
                throw new Exception ("How can we be ahead of the other markers? Some bug");
            
        }
        return "Registered " + conValue + " to " + theseLabels.labelName;
        
    }
    
    /**
     * Handle: keep(x,y,z) removeAllBut(x,y,z) keeps only the instances with
     * classes x,y,z. Make a new experiment out of this and add to session
     */
    private String keep(String [] parameters) throws Exception {
        String retString = "";

        //.. remove listed classes from every experiment
        for (Experiment exp : super.getExperiments(true)) {
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
    
    /**Partition into two experiments: one with all instance that exceed a particular feature threshold, one which does not**/
    private String pluck(String [] parameters) throws Exception {
        String stat = "slope";
        String chan = "15";
        String window = "SECONDHALF";
        float cutOff = 0;  
        
        //.. get optional parameters
        if (parameters.length == 1) {
            cutOff = Float.parseFloat(parameters[0]);
        }   
        if (parameters.length >1) {
            cutOff = Float.parseFloat(parameters[0]);
            stat = parameters[1];
            chan = parameters[2];
            window = parameters[3];
        }
        
        //.. get feature descriptions
        FeatureSet fs = new FeatureSet("temp");
        fs.addFeaturesFromConsole(stat, chan, window);
        
        String retString = "Plucking out instances that meet " + fs.getConsoleString();
        //.. for each selected experiment
        ArrayList<Experiment> es = super.getExperiments(false);
        for (Experiment e : es) {
            Tuple<Experiment,Experiment> t = e.pluck( fs, new float[]{cutOff}); //.. currently we only support one feature descritpions
            
            if (t.x.matrixes.size() >0) {
                TriDAO pDAO = new TriDAO(t.x);
                ctx.dataLayersDAO.addStream(t.x.id, pDAO);
                t.x.setParent(e.id);
            }
            if (t.y.matrixes.size() > 0) {
                TriDAO pDAO = new TriDAO(t.y);
                ctx.dataLayersDAO.addStream(t.y.id, pDAO);
                t.y.setParent(e.id);
            }
            
            retString += "There were " + t.x.matrixes.size() + " which had feature > " + cutOff + " and " + t.y.matrixes.size() + " which did not.";
        }
        return retString;
    }
    
    /**Partition by a label, but instead of making a set of instances, make a set of channelsets
     * @param parameters
     * @return
     * @throws Exception 
     */
    private String partition(String [] parameters) throws Exception {
        String retString = "";
        String param = "condition";
        if (parameters.length >0){
            param = parameters[0];
            param = param.replace(")", ""); //.. remove )
            param = param.replace("\"", "");
        }
        
        //.. get all chansets
        ArrayList<ChannelSet> chanSets = getChanSets(false);
        if (chanSets != null) {
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
                retString += makeChannelSets(cs, param);
            }
        }
        else {
            if (parameters.length ==0) param = "2";
            ArrayList<Experiment> experiments = getExperiments(false);
            for (Experiment exp : experiments) {
                //.. param now means split into param chunks
                ArrayList<Experiment> subExperiments = exp.partition(Integer.parseInt(param));
                for (Experiment subE : subExperiments) {
                    //.. make a new data access object, and add it to our stream
                    TriDAO pDAO = new TriDAO(subE);
                    ctx.dataLayersDAO.addStream(subE.id, pDAO);
                    subE.setParent(exp.id);
                }
                retString += "Made " + subExperiments.size() + " new experiments";
            }
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
        ArrayList<ChannelSet> chanSets = getChanSets(true);
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
  