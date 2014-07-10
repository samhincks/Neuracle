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
    
<<<<<<< Updated upstream
    private MiscellaneousParser miscParser;
    private ExternalDataParser dataParser;
    private TransformationParser transformationParser;
    private DataManipulationParser mlParser;
   
    public InputParser() {
        miscParser = new MiscellaneousParser();
        dataParser = new ExternalDataParser();
        transformationParser = new TransformationParser();
        mlParser = new DataManipulationParser();
    } 
    
    public JSONObject parseInput(String input, ThisActionBeanContext ctx) throws JSONException, Exception {
        JSONObject jsonObj = null; 
        try {
            //.. remove upper letters; trim spaces
            input = input.toLowerCase();
            input = input.trim();
            input = input.replace(" ", "");
            input = input.replace("\"", "");
=======
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
>>>>>>> Stashed changes
            
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
<<<<<<< Updated upstream
=======
            
            String command = input.split("(")[0];
            String [] parameters = this.getParameters(input);
            
            jsonObj = miscParser.execute(command, parameters, ctx, currentDataLayer);
             
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
>>>>>>> Stashed changes
            
            //String command = input.split("(")[0];
            String [] parameters = this.getParameters(input);
            jsonObj = this.rapidExecute(input,parameters, ctx, currentDataLayer,techDAO);
            
            if (jsonObj ==null)
                jsonObj = miscParser.execute(input,parameters, ctx, currentDataLayer,techDAO);
            
            if (jsonObj == null)
                jsonObj = dataParser.execute(input,parameters,ctx,currentDataLayer,techDAO);
                
            if (jsonObj ==null)
                jsonObj = transformationParser.execute(input, parameters,ctx,currentDataLayer,techDAO); 
            
            if (jsonObj ==null)
                jsonObj = mlParser.execute(input, parameters,ctx,currentDataLayer,techDAO); 
            
             
             //.. finally if nothing recognized this command, complain at user
             if (jsonObj == null) {
                 jsonObj = new JSONObject();
                 jsonObj.put("error", "No command " + input + " exists");
             }
                 
<<<<<<< Updated upstream
             return jsonObj;
         }
=======
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
>>>>>>> Stashed changes
        
         catch (Exception e) {
            e.printStackTrace(); //.. comment this out when deliver a stable version
            jsonObj = new JSONObject();
            jsonObj.put("error", e.getMessage());
            return jsonObj;
        }
    }
 
   public JSONObject rapidExecute(String command, String[] parameters, ThisActionBeanContext ctx,
            DataLayer currentDataLayer, TechniqueDAO techDAO) throws Exception {
        this.ctx = ctx;
        this.currentDataLayer = currentDataLayer;
        Command c = null;

        if (command.startsWith("debug")) {
             c = new Command("debug");
             c.retMessage = "selected = " + currentDataLayer.id;
        }
        else if (command.startsWith("exdebug")) {
             c = new Command("debug2");
             throw new Exception("Here is the message that should be displayed in red");
        }

        if (c == null) {
            return null;
        }
        return c.getJSONObject();
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
    
  
    
    
    
<<<<<<< Updated upstream
=======
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
>>>>>>> Stashed changes
    
    
    /***Eventually I should write test classes but it is looking difficult to simulate the session
     context without stripes dispatchment**/
    public static void main(String[] args) {
        InputParser ip = new InputParser();
        
        //.. The necessary parts of a test: a context, a datalayer, and a datalayerDAO 
        ThisActionBeanContext ctx = new ThisActionBeanContext(true);

        try{
            //.. put experiment in tridao
            ChannelSet b = ChannelSet.generate(1, 200);
            ChannelSet c = ChannelSet.generate(1, 200);
            
            Markers m = Markers.generate(10, 20);
            b.addMarkers(m);
            b.id = "b";
            c.id = "c";
            c.addMarkers(m);
            BiDAO bDAO = new BiDAO(b);
            BiDAO cDAO = new BiDAO(c);

            //.. set the ctx's current dao. 
            ctx.addDataLayer("b", bDAO);
            ctx.addDataLayer("c", cDAO);

            
            JSONObject response = new JSONObject();
            int TEST =5;
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
            if (TEST ==4) {
                TechniqueSet ts = TechniqueSet.generate();
                ctx.setCurrentName("b");
                response = ip.parseInput("split(name)",ctx);
                ctx.setCurrentName("bname");
                TriDAO tDAO = (TriDAO) ctx.getCurrentDataLayer();
                
                TechniqueDAO wc = new TechniqueDAO(ts.getClassifier());
                tDAO.addConnection(wc);
                tDAO.addConnection(new TechniqueDAO(ts.getFeatureSet()));
                tDAO.addConnection(new TechniqueDAO(ts.getAttributeSelection()));
                
                response = ip.parseInput("train", ctx);
                System.out.println(response);
                
                //.. Having trained, now test
                ctx.setCurrentName("c");
                bDAO = (BiDAO) ctx.getCurrentDataLayer();
                bDAO.addConnection(wc);
                 
                response = ip.parseInput("classify", ctx);
                System.out.println(response);
            }
           
            
            System.out.println(response.get("content"));
            System.out.println(response.get("action"));
            System.out.println(response.get("error"));

        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    

}
