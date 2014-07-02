 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import dao.techniques.TechniquesDAO;
import dao.techniques.TechniqueDAO;
import dao.datalayers.TriDAO; 
import dao.datalayers.BiDAO;
import dao.datalayers.DataLayerDAO;
import dao.datalayers.DataLayersDAO;
import dao.datalayers.MySqlDAO;
import dao.datalayers.QuadDAO;    
import dao.datalayers.UserDAO;
import filereader.Label;
import filereader.Labels;
import filereader.Markers;
import java.io.StringReader; 
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;  
import java.util.UUID;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.StreamingResolution;
import org.json.JSONException;
import org.json.JSONObject;
import realtimereceiver.Client;
import stripes.ext.ThisActionBeanContext;
import timeseriestufts.evaluatable.*;
import timeseriestufts.evaluatable.performances.Performances;
import timeseriestufts.evaluation.experiment.Classification;
import timeseriestufts.kth.streams.DataLayer;
import timeseriestufts.kth.streams.bi.BidimensionalLayer;
import timeseriestufts.kth.streams.bi.ChannelSet;
import timeseriestufts.kth.streams.quad.MultiExperiment;
import timeseriestufts.kth.streams.tri.*;
import timeseriestufts.kth.streams.uni.Channel;

/**
 *
 * @author samhincks
 */
public class InputParser {
    public ArrayList<String> oldInput = new ArrayList();
    private DataLayer currentDataLayer;
    private Technique currentTechnique;
    private ThisActionBeanContext ctx;
    public double FRAMESIZE =1;
    
    
    private String getHelp() {
        return "Figure it out for yourself!";
    }
    
    private String getCommands() {
        String retString = "";
        
        retString += "LS -> list all loaded datasets::";
        retString += "SPLIT(MARKER) -> With a 2D dataset in view, converts it into a collection of instances by partitioning the data according to where the points where the MARKER-value changes::";
        retString += "GETLABELS ->  returns all the label-names of the dataset (which is the input for split)::";
        retString += "COMMANDS -> What you just wrote!::";
        retString += "REMOVE ALL BUT(x,y,z) -> With an experiment selected, removes all instances except those with class-value x,y,z::";
        retString += "NEW FS(statistic,channel,windo) --> makes a new feature set. See ADDFEATURES::";
        retString += "NEW AS(cfs or info, numAttributes) --> makes a new attribute selection::";
        retString += "NEW ML(name) --> makes a new machine learnign algorithm::";
       
        retString += "ADDFEATURES(statistic,channel,window) --> With a feature set selected, adds a set of feature descriptions. " 
                + "     Takes three parameters: " 
                + " 1) statistic:  mean, slope, secondder, fwhm, largest, smallest, absmean, t2p"
                + " 2) channel: referencable by id or index. 0^1^2 makes attributes for the first three channels."
                + " 0+1+2+3 averages the values at the first four channels, effectively creating a region"
                + " 3) time-window: what part of the instance: FIRSTHALF, SECONDHALF or WHOLE. "
                + " The parameter 6:10 would constrict the features to points between index 6 and 10::";
      
        retString += "FILTER.MOVINGAVERAGE(readingsBack)--> apply a moving average a channel set with specified window length ::";
        retString += "FILTER.LOWPASS(x) + FIlTER.HIGHPASS(x) or FILTER.BANDPASS(x,y)::";  

        retString += "EVALUATE --> With a 3D dataset (a collection of instances) selected and connected to"
               + " at least one of each TechniqueSet (feature set, attribute selection, machine learning, and settings,"
               + " evaluates the dataset by creating a machine learning algorithm on different partitions of the data"
               + " and evaluating it on unseen instances ::";


        return retString;
        
    }
    private MiscellaneousParser miscParser;
    public InputParser() {
        miscParser = new MiscellaneousParser();
    } 
    public JSONObject parseInput(String input, ThisActionBeanContext ctx) throws JSONException, Exception {
        JSONObject jsonObj = null; 
        try {
            
            //.. retrieve the data layer that's currently selected
            DataLayerDAO dl = ctx.getCurrentDataLayer();
            if (dl != null) {
                currentDataLayer = dl.dataLayer;
                System.out.println("Just got currentdatalayer" + currentDataLayer.id);
            } else {
                System.err.println("No loaded datalayers");
            }

            //.. retreive the techinique that's currently selected
            TechniqueDAO techDAO = ctx.getCurrentTechnique();
            if (techDAO != null) {
                currentTechnique = techDAO.technique;
            }
            
           // String command = input.split("(")
            
            //jsonObj = miscParser.execute(input, ctx, currentDataLayer);
             
            /* if (jsonObj == null)
                 jsonObj = transformationParser.execute(input); */ 
             
             
             //.. finally if nothing recognized this command, complain at user
             if (jsonObj == null) {
                 jsonObj = new JSONObject();
                 jsonObj.put("error", "No command " + input + " exists");
             }
             
             
                 
             return jsonObj;

         }
         catch (Exception e) {
            e.printStackTrace();
            jsonObj.put("error", e.getMessage());
            return jsonObj;
        }
    }
 
    /**Generate an appropriate response.
     * jsonObj.action == any actions we want to take on the JS side, for example changing view
     * jsonObj.content == any messages we want to display
     * jsonObj.error == any error messages we want to display
     */
    public JSONObject parseInput2(String input, ThisActionBeanContext ctx) throws JSONException, Exception {
        this.ctx = ctx;
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("content", "");
        jsonObj.put("action", "");
        System.out.println(input);
       
        //.. if we are reading a file, bypass all the other stuff. This is a different interaction,
        //.. file data is streamed as a string. This must come before the other stuff, since
        //.. trimming etc. won't apply. We need to prserve the data. NO LONGER IN USE!
        System.out.println(input);
        if (input.startsWith("filemessage")) {
            try {
                System.out.println(input);
                boolean finished = registerFileFromString(input);
                if (finished) {
                    jsonObj.put("content", "Completed reading file");
                    JSONObject actionObj = new JSONObject();
                    actionObj.put("id", "reload"); 
                    jsonObj.put("action", actionObj);
                }
                else 
                    jsonObj.put("content", "More data awaiting");

                return jsonObj;
            } catch (Exception e) {
                 e.printStackTrace();
                 jsonObj.put("error", e.getMessage());
                 
                 //.. we must also remove this file from our context
                 try{
                     String [] parts = input.split("%%%"); String filename = parts[3];
                     ctx.getDataLayers().removeStream(filename);
                 }catch (Exception e2) {System.err.println("unable to remove");}; //.. let this exception slide
                 
                 return jsonObj;
            }
        }
        
     
        
       
        //.. remove upper letters; trim spaces
        input = input.toLowerCase();
        input = input.trim();
        input = input.replace(" ", "");
        input = input.replace("\"", "");
        
        //.. parse the input, setting the content of the json object
        try{
             //.. retrieve the data layer that's currently selected
             DataLayerDAO dl = ctx.getCurrentDataLayer();
             if (dl!= null){
                currentDataLayer = dl.dataLayer;
                System.out.println("Just got currentdatalayer" + currentDataLayer.id); 
             }
             else System.err.println("No loaded datalayers");
             
             //.. retreive the techinique that's currently selected
             TechniqueDAO techDAO = ctx.getCurrentTechnique();
             if (techDAO != null)
                   currentTechnique = techDAO.technique;
            
             if(input.startsWith("help")) {
                 jsonObj.put("content", getHelp());
             }
             if (input.startsWith("commands") || input.startsWith("showcommands")) {
                 jsonObj.put("content", getCommands());
             }
             //.. eventually we want to split it with a while loop that evaluates each datalayer of the split
             if(input.startsWith("getlabels")) {
                 jsonObj.put("content", getLabels(input));
                 return jsonObj;
             }
            
            //.. open a COM3 communication port, and pass to a new database
            if (input.startsWith("synchronize(")) {
                jsonObj.put("content", synchronizeData(input));
                JSONObject actionObj = new JSONObject();
                actionObj.put("id", "reload"); 
                jsonObj.put("action", actionObj);            
            } 
            
            
            if (input.startsWith("randomlylabel(")) {
                jsonObj.put("content", randomlyLabel(input)); 
            }
            
            if (input.startsWith("label")){
                jsonObj.put("content", label(input));
                return jsonObj;
            }
        
            if (input.startsWith("stream(")) {
                JSONObject actionObj = new JSONObject();
                actionObj.put("id", "csrefresh");
                actionObj.put("data", refreshData(input));
                jsonObj.put("action", actionObj);
            }
             
             ///.. a bit different from most commands. Change a global setting
             else if (input.startsWith("readevery")){
                 jsonObj.put("content", changeFileReadSampling(input));
             }
             
             else if (input.startsWith("createuser")) {
                 //.. Create user! 
                 
             }
             //.. filter.movingaverage(), .lowpass()
             else if (input.startsWith("filter.")) {
                 jsonObj.put("content", applyFilter(input));
                 
                 JSONObject actionObj = new JSONObject();
                 actionObj.put("id", "reload"); 
                 jsonObj.put("action", actionObj);
 
             }
             
             //.. addFeatures(*,*,*)
             else if (input.startsWith("addfeatures(")){
                     jsonObj.put("content", addFeatures(input));
                     return jsonObj;
             }
            
             //.. removeAllBut(classA, classB,classC
             //... Makes a new experiment with only these instances
             else if (input.startsWith("removeallbut(") || input.startsWith("keep(")){
                 jsonObj.put("content", removeAllBut(input));
                 JSONObject actionObj = new JSONObject();
                 actionObj.put("id", "reload");
                 jsonObj.put("action", actionObj);
                 return jsonObj;
             }
             
            
            //.. split(label2) transform a Channel Set to an Experiment by dividing
            //... the channel set at the break points
            else  if(input.startsWith("splitbylabel") || input.startsWith("split(")) {
                 jsonObj.put("content", splitByLabel(input));
                 JSONObject actionObj = new JSONObject();
                 actionObj.put("id", "reload");
                 jsonObj.put("action", actionObj);
                 return jsonObj;
             }
             
             //.. MakeML, makeFS, makeAS, makeSettings
            else  if(input.startsWith("make") || input.startsWith("new")) {
                 jsonObj.put("content", makeTechnique(input, ctx.getTechniques()));
                 JSONObject actionObj = new JSONObject();
                 actionObj.put("id", "reloadT");
                 jsonObj.put("action", actionObj);
             }
             
             else if(input.startsWith("evaluate")) {
                 jsonObj.put("content", evaluateExperiment(input, dl, ctx.getPerformances()));
             }
             
             else if (input.startsWith("saxsearch")) {
                 jsonObj.put("content", "Searching...");
                 JSONObject actionObj = new JSONObject();
                 actionObj.put("id", "sax");
                 actionObj.put("data","no longer in use. Dig up code from old version" /*getSAXSearchJSON(input)*/);
                 jsonObj.put("action", actionObj);
                 System.err.println("done parsing SAX");
                 return jsonObj;    
             }
             
             else if (input.startsWith("saxnn")) {
                 jsonObj.put("content", "Searching...");
                 JSONObject actionObj = new JSONObject();
                 actionObj.put("id", "sax");
                 actionObj.put("data", "no longer in use. Dig up code from old version");
                 jsonObj.put("action", actionObj);
                 return jsonObj;
             }
             
             else if(input.equals("showall") || input.equals("ls")) {
                 jsonObj.put("content", getDataLayers());
             }
             else if (input.startsWith("anchor") || input.startsWith("anchorToZero")){
                 jsonObj.put("content", anchorExperiment(input));
                 JSONObject actionObj = new JSONObject();
                 actionObj.put("id", "reload");
                 jsonObj.put("action", actionObj);
                 return jsonObj;
             }
             else if (input.equals("clear")) {
                 ctx.dataLayersDAO.removeAll();
                 JSONObject actionObj = new JSONObject();
                 actionObj.put("id", "reload");
                 jsonObj.put("action", actionObj);
                 jsonObj.put("content", "Clearing surface.. ");
             }
            
            //.. startsWith register
            if (input.startsWith("register")) {
                try {
                    System.out.println("str reg：" + input);
                    boolean finished = registerUserFromString(input);
                    if (finished) {
                        jsonObj.put("content", "Registration successful!");
                        JSONObject actionObj = new JSONObject();
                        actionObj.put("id", "register");
                        jsonObj.put("action", actionObj);
                    } else {
                        jsonObj.put("content", "Registration failed,please try again!");
                        JSONObject actionObj = new JSONObject();
                        actionObj.put("id", "register");
                        jsonObj.put("action", actionObj);
                    }
                } catch (Exception ex) {
                    //System.err.println("unable to register");
                    System.err.println(ex.getMessage());
                    jsonObj.put("content", ex.getMessage());
                    JSONObject actionObj = new JSONObject();
                    actionObj.put("id", "register");
                    jsonObj.put("action", actionObj);
                }
                return jsonObj;
            }
            if (input.startsWith("save")) {
                //String userId = (String) ctx.getRequest().getSession().getAttribute("userId");
                String userId = "fnirs196";
                if (userId == null) {
                    jsonObj.put("content", "Please login, and then save!");
                } else {
                    boolean finished = saveDataLayer();
                    if (finished) {
                        jsonObj.put("content", "Save successful!");
                    } else {
                        jsonObj.put("content", "There is no data to be saved!");
                    }
                }
            }
            if (input.startsWith("login")) {
                if (input.length() <= 8) {
                    throw new Exception("Please enter the account and password! like 'login(username,password)'!Please try again");
                }
                String strInfo = input.substring(6, input.length() - 1);//.. delete command
                String[] parts = strInfo.split(",");
                String userId = ctx.userDAO.login(parts[0], parts[1]);
                if (userId != null) {
                    ctx.getRequest().getSession().setAttribute("userId", userId);
                    ctx.getRequest().getSession().setAttribute("userName", parts[0]);
                    ctx.getRequest().getSession().setAttribute("password", parts[1]);
                    jsonObj.put("content", "loginSuccess");
                } else {
                    jsonObj.put("content", "Account number or password is wrong!");
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            jsonObj.put("error", e.getMessage());
        }
        return jsonObj;
    }
    
    
    
    /** Label the specified dataset at random. If specified, the
     * second parameter is how long each trial should be
     **/
    private String randomlyLabel(String input) throws Exception{
        String [] parts = this.getParameters(input);
        String filename = parts[0];
        int trialLength =10;
        if (parts.length > 1)
            trialLength = Integer.parseInt(parts[2]);
        
        if (ctx.dataLayersDAO.streams.containsKey(filename)) {
            BiDAO bDAO = (BiDAO) ctx.dataLayersDAO.get(filename);
            
            if(bDAO.synchronizedWithDatabase)
                bDAO.synchronizeWithDatabase(filename);
            
            int numReadings = bDAO.getNumReadings();
            int numTrials = (int) (numReadings / (double)trialLength);
            Markers markers = Markers.generate(numTrials, trialLength);
            bDAO.addMarkers(markers);
            return "Added " + numTrials + " each consisting of" +numReadings;
            
            //TODO: Synchronize labels with database
            

        } else {
            throw new Exception("Context does not contain datalayer " + filename);
        }
        
    }
    
    
    /**Handle: label(filename, curLabelName, curLabelValue). This is a like the below method, typically called by
     a callback, but a user could also trigger it. Makes so that data coming in receives
     the input label**/
    private String label(String input) throws Exception {
        //.. In this new way of doing things, we are going to need to have created a Markers object,
        //.. which would always have a Markers object that was set equal to the number of datapoints
        String [] parts = this.getParameters(input);
        String filename = parts[0];
        String labelName = parts[1];   
        String labelValue = parts[2];     
        if (ctx.dataLayersDAO.streams.containsKey(filename)) { 
            BiDAO bDAO = (BiDAO) ctx.dataLayersDAO.get(filename);   
            if(bDAO.synchronizedWithDatabase) bDAO.synchronizeWithDatabase(filename);
           
            //.. the channelset associated with this object may or may not have markers, associated with it
            Labels labels = bDAO.getLabelsWithName(labelName);
            
            //.. if there is no markers yet, we need instantiate it and populate it with 
            //... # of reading corresponding to number of values, so that the new ones are in synch
            if (labels == null) {
                labels = new Labels(labelName); 
                
                //.. bring it up to date with junk values 
                for (int i =0 ;i< bDAO.getNumReadings(); i++) {
                    labels.addLabel(new Label(labelName, "junk", i));
                }
                bDAO.addLabels(labels); 
            }
            bDAO.setStreamedLabel(labelName, labelValue);
            return "Added " + labelName + " " + labelValue; 
        }
        else{
            throw new Exception("Could not find " + filename);
        }
    }
    
    /**Handle: refresh(datalayername). 
     * Return the newest values from the specified datalayer. This is a bit different
     from most requests in this file since it's issued, typically, by code, a callback
     which periodically issues requests according to a predefined script, ie not the user herself. **/
    public JSONObject refreshData(String input) throws Exception {
        String [] parts = this.getParameters(input);
        String filename = parts[0];
        
        if (ctx.dataLayersDAO.streams.containsKey(filename)) {
            BiDAO bDAO = (BiDAO) ctx.dataLayersDAO.get(filename);
            bDAO.synchronizeWithDatabase(filename);
            return bDAO.getLastUpdateJSON(); 
        }
        
        else {
            throw new Exception("Context does not contain datalayer " + filename);
        }
        
    }
    
    /**Parses: synchronize(dataname). If there is a session-datalayer with the name
     datalayer, it pings the database to make sure it is up-to-date; if no such
     datalayer exists, it creates a new one **/
    public String synchronizeData(String input) throws Exception{
        String [] parts = this.getParameters(input);
        String filename = parts[0];
       /* String port1 = parts[1];
        String port2 = null;
        if (parts.length == 3) {
            port2 = parts[2];
        }*/ 
        //.. if this is the first datalayer ever added
        if (ctx.dataLayersDAO == null) {
            ctx.dataLayersDAO = new DataLayersDAO();
        }
        
        BiDAO bDAO; //.. the datalayer we are building
        
        //.. either get bDAO or create it (create if this is the first request processed)
        if (ctx.dataLayersDAO.streams.containsKey(filename)) {
             bDAO = (BiDAO) ctx.dataLayersDAO.get(filename);
             int added = bDAO.synchronizeWithDatabase(filename);
             return "Updated " + filename + " with " + added + " changes to each column";
        }
        
        else {
            ChannelSet cs = new ChannelSet();
            cs.id = filename;
            bDAO = new BiDAO(cs);
            int added = bDAO.synchronizeWithDatabase(filename);
            ctx.dataLayersDAO.addStream(filename, bDAO);
            return "Made new datalayer " + filename + " added " + added;
        }
       
    }
    
    /** Parses the attempt to build a datalayer from a sequence of strings. 
     * Encoding is separated by three percent signs. (if the raw data contains %%% in it, there will be data loss)
     * Structure: filemessage%%%index%%%totalSize%%%filename%%%restofdata
     * First would be: filemessage%%%test.csv%%%0%%%43%%%1,2,3,4\n. 
     * Last would happen when numAdded to MakeChannelSetFromStrings == totalAmount
     * This complexity is necessary since I'm not sure I can guarantee that a series of 
     * asynchronous passes preserves the order in which the requests were sent. 
     * 
     * But what if two messages are passed concurrently, each is recognized as being the first
     * and a new BiDAO is therefore created. Can this happen in this single thread operation?
     * Only one way to find out.  
     * NO LONGER IN USE: 1) We need pinging between client and server to know that datalayer
     * has been initialized and when to complete the layer. 2) Some data seems lost, like \ns. 
     **/
    private boolean registerFileFromString(String input) throws Exception{
        //.. if this is the first datalayer ever added
        if (ctx.dataLayersDAO == null) {
            ctx.dataLayersDAO = new DataLayersDAO();
        }
        
        //.. we use the percentage file to separate between data and metadata 
        String [] parts = input.split("%%%");
        if (parts.length != 5) throw new Exception("This command requires 5 parameters, separated by %%% "
                + "filemessage%%%index%%%totalSize%%%filename%%%restofdata");
        
        int index = Integer.parseInt(parts[1]),  totalSize = Integer.parseInt(parts[2]);
        String filename = parts[3],  data = parts[4];
        
        BiDAO bDAO; //.. the datalayer we are building
        
        //.. either get bDAO or create it (create if this is the first request processed)
        if (ctx.dataLayersDAO.streams.containsKey(filename))
             bDAO = (BiDAO) ctx.dataLayersDAO.get(filename);
        else {
            System.out.println("HA TO MAKE A NEW BIDAO for " + input);
            bDAO = new BiDAO(filename);
            ctx.dataLayersDAO.addStream(filename, bDAO);
        }
        
        bDAO.addMessage(index, totalSize, filename, data);
        if (bDAO.dataLayer != null) return true; //done
        return false;//.. not yet done
        
        
    }
    /**
     * Register
     * @param input
     * @return
     * @throws  
     */
    private Boolean registerUserFromString(String input) throws Exception{
        //.. if this is the first datalayer ever added
        if (ctx.dataLayersDAO == null) {
            ctx.dataLayersDAO = new DataLayersDAO();
        }
        
        //.. split parameters
        if(input.length()<=8) throw new Exception ("Please enter the account,email and password! like 'register(username,email,password)'!Please try again");
        String strInfo=input.substring(9, input.length()-1);//.. delete command
        String [] parts = strInfo.split(",");
        //.. username email password
        if(parts.length!=3) throw new Exception ("Sorry,Not in the correct format!Just like 'register(username,password)'!Please try again");
        //.. UserName
        if(parts[0].length()<5) throw new Exception ("Sorry,UserName at least 5");
        System.out.println("username:"+parts[0]);
        //.. Email
        if (!parts[1].matches("[\\w\\.\\-]+@([\\w\\-]+\\.)+[\\w\\-]+")) throw new Exception ("Sorry,The Email is not in the correct format,Please try again");
        //.. Password
        if(parts[2].length()<5) throw new Exception ("Sorry,Password at least 5");
        //.. Start Reg
        MySqlDAO mydao=new MySqlDAO();
        mydao.connSQL();
        //.. Check whether the repetition happens
        ResultSet rs=mydao.selectSQL("select * from userinfo where username='"+parts[0]+"'");
        rs.last();   
        int rowCount = rs.getRow();//Get total num
        rs.beforeFirst(); 
        if(rowCount==0){
            Boolean result=mydao.insertSQL("insert into userinfo(username,userpwd,email) VALUES ('"+parts[0]+"','"+parts[2]+"','"+parts[1]+"')");
        System.out.println("userinfo:"+parts[0]+","+parts[1]+","+parts[2]);
        mydao.deconnSQL();
            if(result){
                return true;
            }else{
                return false;
            }
        }else throw new Exception ("Sorry,Account already exists!");
        
    }
    
    /**Handle: readyEvery(10). Make so that, when reading a file in the future */
    private String changeFileReadSampling(String input) throws Exception {
        String retString ="";
        int fileReadSampling = ctx.getFileReadSampling();
        
        //.. return current value regardless
        if (fileReadSampling ==1) retString += "Currently file reads every row ";
        else if (fileReadSampling ==2) retString += "Currently file reads every other row";
        else retString +="Currently file reads every " + fileReadSampling + "th row ";
        
        ///.. if theres a parameter change to that value
        String [] parameters = this.getParameters(input);
        if (parameters.length>0){
            int newSamplingRate = Integer.parseInt(parameters[0]);
            ctx.setFileReadSampling(newSamplingRate);
            retString += ":: Now it is " + newSamplingRate;
        }
        
        return retString;
    }
    /**Handle; anchor(); Make a new experiment with the start point of each instance set at 0
     anchor(t) = make a new copy. anchor(false) = manipulate same level and all its derived from 
     This produces a massive BUG if we run it with false. Even though
     * it appears to ahve manipulated the datalayer, it in fact makes
     * it identical in evaluation to the non-anchored technique. This is 
     * a controller problem as things work fine with this in the backend. 
     * The problem is related either to how ARFF files are written out in the web mode
     * (I dont even know where they go) or the fac that we arent changing ids or
     * some mysterious copy lingering in the server that gets used when we try to evaluate.
     */
    private String anchorExperiment(String input) throws Exception {
        String retString = "";
        String [] parameters =this.getParameters(input);
        boolean copy = true;
        
        //.. if first parameter starts with t, set copy to true else false
        if (parameters.length >0) {
            String tOrF = parameters[0];
            if (tOrF.startsWith("t")) copy = true;
            else copy = false;
        }
        
        //.. for every selected experiment
        for (Experiment exp : this.getExperiments()){
            Experiment e = exp.anchorToZero(copy);
            if (copy) {
                e.setId(exp.id +"start0");
                e.setParent(exp.getId()); //.. set parent to what we derived it from

                //.. make a new data access object, and add it to our stream
                TriDAO pDAO = new TriDAO(e);    
                ctx.dataLayersDAO.addStream(e.id,pDAO);
                retString += "Created : " + e.getId() +" with " + e.matrixes.size() + " instances::";
            }
            else retString += "Changed raw values of " + e.getId() + " and its parents. ";
        }
        return retString;
    }
    
    /** Handle : filter.xxx(parameter). 
     * Apply a filter to a channelset or a channelsetset.*/
    private String applyFilter(String input) throws Exception{
        //.. chanSets will be 1 or more ChannelSets, each of which we will apply the filte to
        ArrayList<ChannelSet> chanSets = getChanSets();
        String retString = "";

        for (ChannelSet cs : chanSets) {
            if(input.startsWith("filter.movingaverage")) {
                retString += applyMovingAverage(cs, input);
            }
            
            else if (input.startsWith("filter.lowpass")) {
                retString += applyPassFilter(cs, input, 0);
            }
            else if (input.startsWith("filter.highpass")) {
                retString += applyPassFilter(cs, input, 1);
            }
            else if (input.startsWith("filter.bandpass")) {
                retString += applyPassFilter(cs, input, 2);
            }
        }

        return retString;
    }
    
    /** If we know that currentDataLayer is either a Channelset or a ChannelSetSet
     * return an arraylist with each channelset. 
     */
    private ArrayList<ChannelSet> getChanSets() throws Exception{
        if(!(currentDataLayer instanceof ChannelSet) && !(currentDataLayer instanceof ChannelSetSet))
            throw new Exception("The selected dataset must be a Channel Set or ChannelSetSet");
        
        //.. add all chansets to ChanSets
        ArrayList<ChannelSet> chanSets =  new ArrayList();
        if (currentDataLayer instanceof ChannelSetSet) {
            ChannelSetSet css = (ChannelSetSet)currentDataLayer;
            for (BidimensionalLayer bd : css.matrixes) {
                chanSets.add((ChannelSet)bd);
            } 
        }
        else {
            chanSets.add((ChannelSet)currentDataLayer);
        }
        return chanSets; 
    }
    
    /**Handle: filter.movingaverage(numBack) where numBack is how many readings back of the moving average
     */
    private String applyMovingAverage(ChannelSet cs, String input) throws Exception{
        //.. extract the one parameter : how many readings back. Default is 5
        String [] parameters = this.getParameters(input); 
        int readingsBack = 5;
        if (parameters.length > 0)
             readingsBack = Integer.parseInt(parameters[0]);
        
        ChannelSet filteredSet = cs.movingAverage(readingsBack, true); //.. we want a copy
        filteredSet.setParent(cs.id);
        BiDAO mDAO = new BiDAO(filteredSet);
        ctx.dataLayersDAO.addStream(filteredSet.id,mDAO);

        return "Created " + filteredSet.id + " a copy of "+cs.id + " with new values "+
                " representing the moving average at " + readingsBack + " points.;;";
    }
    
     /**Handle: filter.lowpass(freq), filter.highpass(freq), filter.bandpass(low, high) 
      * type==0 : lowpass
      * type==1: highpass
      * type==2: bandpass

     */
    private String applyPassFilter(ChannelSet cs, String input, int type) throws Exception{
        //.. extract the one parameter : how many readings back. Default is 5
        String [] parameters = this.getParameters(input); 
        float freq = 0.5f;
        float freq2 = 0.8f;
        if (parameters.length > 0) 
             freq = Float.parseFloat(parameters[0]);
        
        if (parameters.length >1) //.. if its bandpass there can be two parameters;
            freq2 = Float.parseFloat(parameters[1]);
        
        
        ChannelSet filteredSet;
        if (type ==0)
             filteredSet = cs.lowpass(freq, true); 
        else if (type ==1)
             filteredSet = cs.highpass(freq, true); 
        else //.. type ==2 
             filteredSet = cs.bandpass(freq, freq2, true); 

        
        filteredSet.setParent(cs.id);
        BiDAO mDAO = new BiDAO(filteredSet);
        ctx.dataLayersDAO.addStream(filteredSet.id,mDAO);

        return "Created " + filteredSet.id + " a copy of "+cs.id;
    }
    
    
    
    
    /** Handle: keep(x,y,z) removeAllBut(x,y,z) keeps only the instances with classes x,y,z.
     * Make a new experiment out of this and add to session
     **/
    private String removeAllBut(String input) throws Exception {
        String [] parameters = this.getParameters(input);
        String retString="";
        
        //.. remove listed classes from every experiment
        for (Experiment exp : getExperiments()) {
            Experiment e = exp.removeAllClassesBut(new ArrayList(Arrays.asList(parameters)));
            e.setId(exp.getId() + e.classification.id);
            e.setParent(exp.getId()); //.. set parent to what we derived it from

            //.. make a new data access object, and add it to our stream
            TriDAO pDAO = new TriDAO(e);
            ctx.dataLayersDAO.addStream(e.id, pDAO);

            //.. Generate a console message which includes num instance, num of each condition
             retString = "Created : " + e.getId() + " with " + e.matrixes.size() + " instances::";
        }
        return retString;
    }
    
    /**For multi-selection operations, it is most often a pain in the butt to define
     * a new operation to operate on the higher dimension object rather than repeatedly
     * on the lower dimension object. What we want is to add the individual layers to the context,
     * so we need to break the temporary 4d abstraction right after anyway.
     */
    private ArrayList<Experiment> getExperiments() throws Exception{
        if(!(currentDataLayer instanceof Experiment || currentDataLayer instanceof MultiExperiment)) {
             throw new Exception("The selected dataset must be a 3D-Experiment");
        }
        ArrayList<Experiment> retExperiments = new ArrayList();
        
        //.. if multi experiment then return each experiment individually
        if (currentDataLayer instanceof MultiExperiment){
            MultiExperiment multi = (MultiExperiment) currentDataLayer;
            for (TridimensionalLayer t : multi.piles) {
                retExperiments.add((Experiment)t);
            }
        }
        
        //.. else return an array of size 1
        if (currentDataLayer instanceof Experiment)
            retExperiments.add((Experiment)currentDataLayer);

        return retExperiments;
    }
    
    /**HANDLE:
     * addFeature(mean, 1, :) or (mean, 1, WHOLE)
     * addFeature(slope^mean,1^2, WHOLE^
     * Immensely complex.**/
    private String addFeatures(String input) throws Exception{
        if (!(currentTechnique instanceof FeatureSet))
            throw new Exception("The selected technique must be a feature set");
        
        FeatureSet fs = (FeatureSet) currentTechnique;
        
        String [] parameters = this.getParameters(input);
        if (parameters.length != 3) throw new Exception("Must have exactly three parameters (statistic, channel, window");
        
        //.. stat = 0; channeId =1; window =3
        String stat = parameters[0];
        String channel = parameters[1];
        String window = parameters[2];
        fs.addFeaturesFromConsole(stat, channel, window);
        return "Successfully added  " + fs.featureDescriptions.values().size() + " feature descripions";
    }
    
    /**Handle: 
     * makeML("Jrip")
     * makeFS("name")
     * makeAS(type, numatts) type = none, cfs, info. numatts = integer or ? for unknown
     * makeSettings("").
     * 
     * Machine learning algorithm is trivial. Rest will require considerable thought.
     * I imagine we will want them to be structured in a comparable way. You make them by specifying
     * ID. Select them, and then add to them 
     */
    private String makeTechnique(String input, TechniquesDAO techniquesDAO) throws Exception{
        String [] parameters = this.getParameters(input);
        if(parameters.length ==0) throw new Exception("Must specify parameter makeXX(something)");
        String id = parameters[0];
        
        if(input.startsWith("makeml(") ||input.startsWith("newml(")){
            makeMLAlgorithm(id,techniquesDAO); //.. id is the name: jRip, J48, 
            return ("Successfully made machine learning algorithm " + id);
        }
        
        else if(input.startsWith("makefs(") || input.startsWith("newfeatureset(")) {
            TechniqueDAO tDAO = new TechniqueDAO(new FeatureSet(id));
            techniquesDAO.addTechnique(id, tDAO);
            return ("Successfully made Feature Set " + id);
        }
        
        //. MAKE Attribute Selection - Handle
        else if(input.startsWith("makeas(") ||input.startsWith("newattributeselection(")) {
            if (parameters.length <2  && (!(parameters[0].startsWith("none")))) throw new Exception("Must specify how many attribtues to keep as second parameter");
            String numAtts = "?"; //.. default if this is no attribute selection
            if(parameters.length >1)
                 numAtts = parameters[1];
            int attrs;
            
            //.. its either ? or a number. Try to parse as integer
            try {
                attrs = Integer.parseInt(numAtts);
            }
            //.. if we failed, then set as -1
            catch(Exception e) {attrs = -1;}
            
            TechniqueDAO tDAO;
            
            //.. the input may not be permissable, we only allow none, cfs and info
            try{
                tDAO = new TechniqueDAO(new AttributeSelection(id, attrs));
            }
           catch (IllegalArgumentException e) {
               throw new Exception ("There is no Attribute Selection algorithm titled " + id + " For now, there is cfs, info, and none" );
           }
          techniquesDAO.addTechnique(tDAO.getId(), tDAO);
            return ("Successfully made AttributeSelection " +tDAO.getId());
        }
        
        throw new Exception("Unable to parse input " + input);
        
    }
    
    /**Parse: makeML("jrip), etc*/
    private void makeMLAlgorithm(String id, TechniquesDAO techniquesDAO) throws Exception{
        TechniqueDAO tDAO;
        try{
            tDAO = new TechniqueDAO(new WekaClassifier(id));
        }
        catch (IllegalArgumentException e) {
            throw new Exception("There is no machine learning algorithm titled "+ id +" . For now, there is"
                    + "jrip, j48, lmt, nb, tnn, smo, simple, logistic, adaboost");
        }
        
        techniquesDAO.addTechnique(id, tDAO);
    }
    
    /**Handle evaluate(), when the selected element is an Experiment.
     * evaluate(5) for evaluation across 5 folds
     * Assume Techniques have been populated via the interface.
     * 
     * Bugs: multi-analysis. The retString classification accuracy doesnt owrk
     */
    private String evaluateExperiment(String input, DataLayerDAO dDAO, Performances performances) throws Exception{
        if (!(currentDataLayer instanceof Experiment || currentDataLayer instanceof MultiExperiment)) 
            throw new Exception("You must split the data into instances first, e.g. (split(labelName)");
        
        //.. Get parameters if any -- how many folds. by default: -1, leave one out
        int numFolds =-1;
        String [] parameters = this.getParameters(input); 
        if (parameters.length > 0) numFolds = Integer.parseInt(parameters[0]);
        
        //.. Add technique description to return string
        String retString = "Evaluating an experiment... ";
       /* ArrayList<TechniqueDAO> tDAOs = dDAO.tDAOs;
        for(TechniqueDAO tDAO : tDAOs){
            retString += "Technique:  " +tDAO.getId() +",";
        }*/
        
        
        //.. currently Experiment could either be MultiExperiment or Experiment
        if (currentDataLayer instanceof Experiment){
            if(!dDAO.hasOneOfEachTechnique())    
                throw new Exception(dDAO.getId() + " does not appear to be connected to all the necessary Evaluation Techniques. "
                    + "Please overlap the dataset with one of each"); 
        
            //.. Get a Technique Set for every connected technique
            ArrayList<TechniqueSet> techniquesToEvaluate= this.getTechniquesForEvaluations(dDAO, performances);
        
            Experiment experiment = (Experiment) currentDataLayer;

            //.. get Dataset - either a new one or one stored in performancs
            Dataset dataset = getDatasetForEvaluations(experiment.id, performances);
            
            double total =0;
           
            //.. finally, evaluate each techniqueset
            for (TechniqueSet t : techniquesToEvaluate) {
                System.out.println("Using " + t.getFeatureSet().getId() + " " + t.getFeatureSet().getConsoleString() + " " + t.getFeatureSet().getFeatureDescriptionString());
                
                experiment.evaluate(t, dataset, numFolds);
                total+= t.getMostRecentAverage(); 
            }
            retString += "::Across all, %CORR: "+(total/techniquesToEvaluate.size()); 

            return retString;
        }
        
        else if (currentDataLayer instanceof MultiExperiment) {
            QuadDAO qDAO = (QuadDAO)dDAO; 
            MultiExperiment multi = (MultiExperiment)currentDataLayer;
           
            //.. set daoWithTechniques to the first datalayer that is connected 
            DataLayerDAO daoWithTechniques =null;
            for (TriDAO pDAO : qDAO.piles) {
                if (pDAO.hasOneOfEachTechnique()){
                    //.. throw an exception if more than one complete technique set is connected
                    if (daoWithTechniques != null) throw new Exception("At least two datalayers are connected to a complete set of techniques. Please connect only one and the rest will be evaluated using the same TechniqueSet");
                    
                    daoWithTechniques = pDAO;
                }
            }
            
            //.. throw exception if none are connected
            if (daoWithTechniques == null)
                throw new Exception("None of the selected datalayers appear to be connected to all the necessary Evaluation Techniques. "
                    + "Please overlap the dataset with one of each");
            
            //.. Get a Technique Set for every connected technique with the one that is connected 
            ArrayList<TechniqueSet> techniquesToEvaluate= this.getTechniquesForEvaluations(daoWithTechniques, performances);
           
             //.. get a dataset for each experiment to be evaluated. Attach it to the underlying experiment (this is the non-obvious part)
             for (TriDAO pDAO : qDAO.piles) {
                 Experiment thisE = (Experiment) pDAO.dataLayer;
                 thisE.setDataset(getDatasetForEvaluations(thisE.id, performances));
             }

            //.. evaluate each techniqueset on each experiment
            for (TechniqueSet t : techniquesToEvaluate) {
                multi.evaluate(t, numFolds);
            }
            double total =0; //.. pct correct
             
            //.. retrieve each of the selected datasets and sum their stats
            for (TriDAO pDAO : qDAO.piles) {
                 Experiment thisE = (Experiment) pDAO.dataLayer;
                 Dataset d = thisE.getDataSet();
                 total += d.getMostRecentAverage();
             }
            
            retString += "::Across all, %CORR: "+(total/qDAO.piles.size()); 
            return retString;
        }
        return "Unexpected evaluation failure. Actually, unreachable statement"; 
    }
    /**See if an experiment has been evaluated before if it has use that Dataset, otherwise return a new one.*/
    private Dataset getDatasetForEvaluations(String id, Performances performances) throws Exception{
        //.. see if an experiment with this id has been evaluated before
        //... and if not create a new Dataset for it
        Dataset dataset = performances.getDataSet(id);
        if (dataset == null){
            dataset = new Dataset(id);
            performances.addNewDataset(dataset);
        }
        return dataset;
    }
    
    /**EvaluateExperiment helper -- Create a TechniqueSet for each of the 
     * techniques which intersect with the datalayer. 
     **/
    private ArrayList<TechniqueSet> getTechniquesForEvaluations(DataLayerDAO dDAO, Performances performances) throws Exception{
        
        //.. All the techniques connected to this datalayer organized by sort
        //... For each techniqueset we want one of each
        ArrayList<ClassificationAlgorithm> classifiers = dDAO.getClassifiers();
        ArrayList<AttributeSelection> attributeSelections = dDAO.getAttributeSelections();
        ArrayList<FeatureSet> fSets = dDAO.getFeatureSets();
        ArrayList<TechniqueSet> techniqueSets = new ArrayList();

        //.. Create a techniqueSet for each combination.
        //... Only one will be created if we have one of each
        for (ClassificationAlgorithm classifier : classifiers) {
            for(AttributeSelection aSelection : attributeSelections) {
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
            }
            
            //.. otherwise extract it from performances, and replace the one we just created with it
            else {
                newTSet = existingTSet;
            }
            
           techniquesToEvaluate.add(newTSet);
        }
        
        return techniquesToEvaluate;
    }
   
    
    /**Return what is enumerated inside the parens, ie x, y and z from function(x,y,z)*
     * if no params return an empty array. */
    public String [] getParameters(String input) throws Exception{
        String [] params;
        String betweenParen = getBetweenParen(input);
        if (betweenParen.equals("")) return new String[0];
        params = betweenParen.split(",");
        return params;
    }
    
    /** Input: aksdasl(xxxx,yy,xx)
     Return: xxx,yy,xx*
     * If there's nothign inbetween the parens return nothing*/ 
    private String getBetweenParen(String input) throws Exception{
        String[] values = input.split("\\(");
        if (values.length <2) return "";
        String betweenParen = values[1];
        betweenParen = betweenParen.replace(" ", "");
        betweenParen  = betweenParen.replace(")", ""); //.. remove )
        return betweenParen;

    }
    
    /**Command = GETLABELS;  Returns the labels of this dataLayer*/ 
    private String getLabels(String input) throws Exception{
           String content ="GETLABELS of " + currentDataLayer.getId() +"::";
           
           if (currentDataLayer instanceof Experiment) {
               Experiment e = (Experiment)currentDataLayer;
               content += e.classification.name + " : ";
               for (String labelVal : e.classification.values) {
                     content += labelVal +", "; //.. orka remove trailing comma
               }
           }
           
           else if (currentDataLayer instanceof ChannelSet){
               ChannelSet bd = (ChannelSet )currentDataLayer;
               ArrayList<Markers> markers = bd.markers;
               for(int i =0; i < markers.size(); i++) {
                    Markers m  = markers.get(i);
                     Classification c = m.getClassification();
                    content = content + c.name + " : ";
                    for (String labelVal : c.values) {
                        content += labelVal +", ";
                    }
                    if (i!= markers.size()-1)
                       content += "::";
               }
           }
          
          return content;
    }
    
    /**Command = SPLITBYLABEL(LabelName) or just Split(....
     * Make a bidimensional layer or collection of bidimensional layer into experiment
     */
    private String splitByLabel(String input) throws Exception{        
        String [] values  = input.split("\\(");
        //.. second half should be "(xxxx)"
        String labelName = values[1];
        labelName = labelName.replace(")", ""); //.. remove )
        labelName = labelName.replace("\"","");
        
        //.. get all chansets
        ArrayList<ChannelSet> chanSets = getChanSets();
        String retString = "";
        for (ChannelSet cs : chanSets) {
           //.. In case there is a channelset which has labels, but not markers, add markers
           //... the labels would be saved in the biDOA and this would only be used in conjunction with a DB
           BiDAO biDAO = (BiDAO) ctx.getDataLayers().get(cs.id);
           if (biDAO.labels!= null) {
               for (Labels l : biDAO.labels) {
                   Markers m = new Markers(l);
                   
                   //.. INVARIANT: # of markers should equal number of rows in each col
                   biDAO.addMarkers(m); 
               }
           }
           
           //.. make the triplet layer
           retString+= makeExperiment(cs, labelName);
        }
        
        return retString;
    }
    
    private String makeExperiment(ChannelSet cs, String labelName) throws Exception{
        
        Experiment e = cs.splitByLabel(labelName); 
        e.setId(cs.getId() + labelName);
        e.setParent(cs.getId()); //.. set parent to what we derived it from
        
        //.. make a new data access object, and add it to our stream
        TriDAO pDAO = new TriDAO(e);    
        ctx.dataLayersDAO.addStream(e.id,pDAO);

        //.. Generate a console message which includes num instance, num of each condition
        return "Created : " + e.getId() +" with " + e.matrixes.size() + " instances::";
    }
    
    private String getDataLayers() throws Exception {
        String retString = "";
        DataLayersDAO dlDAOs = ctx.getDataLayers();
        for (int i = 0; i < dlDAOs.getDataLayers().size(); i++) {
            DataLayer dl = dlDAOs.getDataLayers().get(i);
            retString += dl.getId() + " with " + dl.getCount() + " pts " + " and mean of " + dl.getMean();
            if (i != dlDAOs.getDataLayers().size() - 1) {
                retString += "::";
            }
        }

        return retString;
    }
    
    
    /***Eventually I should write test classes but it is looking difficult to simulate the session
     context without stripes dispatchment**/
    public static void main(String[] args) {
        InputParser ip = new InputParser();
        
        //.. The necessary parts of a test: a context, a datalayer, and a datalayerDAO 
        ThisActionBeanContext ctx = new ThisActionBeanContext();

        //.. put experiment in tridao
        Experiment e = Experiment.generate(6, 1, 20);
        TriDAO tDAO = new TriDAO(e);
        
        //.. set the ctx's current dao. 
//        ctx.addDataLayer("e", tDAO);
        try{
            JSONObject response = new JSONObject();
            int TEST =3;
            if (TEST ==0) 
                 response= ip.parseInput("removeallbut(a,b)", ctx);
            
            if (TEST ==1) {
                response = ip.parseInput("intercept(bestemusic07.csv, bajs, bajs)", ctx);
            }
            
            
            if (TEST ==2) {
                response = ip.parseInput("save(", ctx);
            }
            
            if (TEST ==3) {
                response = ip.parseInput("label(", ctx);
            }
            
            
            System.out.println(response.get("content"));
            System.out.println(response.get("action"));
            System.out.println(response.get("error"));

        }
        catch (Exception ex) {
            
        }

    }
    
    
    public boolean saveDataLayer() throws Exception{
        if (ctx.dataLayersDAO.getDataLayers().size() > 0) {
            for (DataLayer dl :ctx.dataLayersDAO.getDataLayers()){
                   System.out.println("thats  " + dl.id);
                   JSONObject obj = new JSONObject();
                   String filename =  dl.getId();
                   DataLayerDAO dlGiver = ctx.dataLayersDAO.get(filename); 
            
                    //.. select the file
                    filename = filename.split("-")[0]+".csv";
                    ChannelSet channelSet = (ChannelSet) dlGiver.dataLayer;
                    ArrayList<Channel> rawValues =  channelSet.streams;
                    
                    //.. connect to SQL
                    MySqlDAO mydao=new MySqlDAO();
                    String uuIdStr= UUID.randomUUID().toString();
                    mydao.connSQL();
                    
                    //.. get the id of the user, a number
                    String userId = (String) ctx.getRequest().getSession().getAttribute("userId");
                   
                    //.. Retrieve the datalayer that is associated with this user - but this assumes that there is only ONE
                    ResultSet datalayerData = mydao.selectSQL("select * from datalayer where user_id='"+userId+"' and file_name like '" + filename.split(".cvs")[0]+"%'");
                    while(datalayerData.next()) {
                        mydao.deleteSQL("delete from datalayer where id ='"+datalayerData.getString(1)+"'");
                        mydao.deleteSQL("delete from datalayer where parent_id ='"+datalayerData.getString(1)+"'");
                        mydao.deleteSQL("delete from datalayer_id where parent_id ='"+datalayerData.getString(1)+"'");
                    }
                    datalayerData.close();
                    boolean num = mydao.insertSQL("insert into datalayer(id,user_id,parent_id,data,file_name) VALUES ('"+uuIdStr+"','"+userId+"','"+null+"','"+null+"', '"+filename+"')");
                    mydao.deconnSQL();

                    for(Channel rowValue : rawValues){
                        StringBuffer flotArrays = new StringBuffer();
                        for (int l = 0; l < rowValue.numPoints; l++){
                         flotArrays.append(","+rowValue.getPointOrNull(l));
                        }
                        mydao.connSQL();
                        Boolean result=mydao.insertSQL("insert into datalayer(id,user_id,parent_id,data,file_name) VALUES ('"+UUID.randomUUID().toString()+"','"+null+"','"+uuIdStr+"','"+flotArrays.toString().substring(1)+"', '"+rowValue.getId()+"')");
                        mydao.deconnSQL();
                  } 

                    //.. Having built a channel structure and label structure, label the channel
                    //... structure according to the label structure
                    for (Markers markers : channelSet.markers) {
                        Labels labels = markers.saveLabels;
                        StringBuffer str = new StringBuffer();
                        for (Label label : labels.channelLabels) {
                            str.append(","+label.value);
                        }
                        mydao.connSQL();
                        mydao.insertSQL("insert into label(id,datalayer_id,labelName,channelLabels) VALUES ('"+ UUID.randomUUID().toString()+"','"+uuIdStr+"','"+labels.labelName+"','"+str.toString().substring(1)+"')");
                        mydao.deconnSQL();
                    }
            }
        } else {
            return false;
        }
        return true;
    }

}
