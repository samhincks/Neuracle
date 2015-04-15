/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dao;

import dao.datalayers.BiDAO;
import dao.datalayers.DataLayerDAO;
import dao.datalayers.DataLayersDAO;
import dao.datalayers.MySqlDAO;
import dao.techniques.TechniqueDAO;
import filereader.Label;
import filereader.Labels;
import filereader.Markers;
import filereader.TSTuftsFileReader;
import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.UUID;
import org.json.JSONObject;
import realtime.AudioNBack;
import realtime.Client;
import stripes.ext.ThisActionBeanContext;
import timeseriestufts.evaluation.experiment.Classification;
import timeseriestufts.kth.streams.DataLayer;
import timeseriestufts.kth.streams.bi.ChannelSet;
import timeseriestufts.kth.streams.tri.Experiment;
import timeseriestufts.kth.streams.uni.Channel;

/**
 *
 * @author samhincks
 */
public class MiscellaneousParser extends Parser{ 
    public MiscellaneousParser(ThisActionBeanContext ctx) {
        super(ctx);
        commands = new Hashtable();
        /**--  Every command this Parser handles should be added to commands
         *     with a corresponding function for execution in the execute function--**/
        
        //-- LS 
        Command command = new Command("ls");
        command.documentation = "Enumerates datasets loaded in this session";
        commands.put(command.id, command);
        
        //-- READEVERY 
        command = new Command("readevery");
        command.documentation = "For the sake of faster file-reading, and to pass files that "
                + "exceed 42 megabytes, changes the system parameter that dictates how many "
                + "rows to skip";
        command.parameters = "1.samplingRate : read every x parameters ";
        commands.put(command.id, command);
        
        //-- CLEAR 
        command = new Command("clear");
        command.documentation = "Removes any loaded datasets from the present surface";
        command.debug = "This does not work - a more thorough reload of the context is  necessary";
        commands.put(command.id, command);
        
        
        // -- DELETE
        command = new Command("delete");
        command.documentation = "Removes the selected datalayer, freeing associated memory";
        commands.put(command.id, command);
       
        // -- HOLD
        command = new Command("hold");
        command.documentation = "Removes all but the selected datalayer";
        command.tutorial = " You've cleared your data analysis surface. "
                + " With the three datasets selected, type append to "
                + " merge them all into one selection";
        commands.put(command.id, command);
        
         // -- LOAD
        command = new Command("load");
        command.documentation = "Loads the specified folder, though it must reside in input on the server";
        command.parameters = "1. foldername";
        commands.put(command.id, command);

        
        //-- REGISTER
        command = new Command("register");
        command.documentation = "Registers the user to a database";
        command.debug = "Boxia implemented, untested";
        command.parameters = "1. username, 2. email, 3.password";
        commands.put(command.id, command);
        
        //-- LOGIN
        command = new Command("login");
        command.documentation = "Logs the user into the system";
        command.debug = "Boxia implemented, untested; I've gone through it once, but am not confident";
        command.parameters = "1. username, 2. password";
        commands.put(command.id, command);
        
        //-- GETLABELS
        command = new Command("getlabels");
        command.documentation = "Returns all available labels and conditions on selected dataset ";
        command.debug = "Unclear why there is not a proper error message when there are no labels";
        commands.put(command.id, command);
        
        //-- Get Commands
        command = new Command("getCommands");
        command.documentation = "Returns all implemented commands";
        commands.put(command.id, command);
        
        //-- Start N-Back
        command = new Command("nback");
        command.documentation = "Starts an audio n-back trial, and broadcasts the condition to specified port";
        command.parameters = "1. k-back. 2. durationInSeconds, 3. Port-num (optional)";
        commands.put(command.id, command);
        
        command = new Command("tutorial");
        command.documentation = "Loads a sample file, and executes basic instructions on it";   
        command.action = "reload";
        command.tutorial = "Double click on one of the files in the topleft corner to view the raw-data."
                + "Then, with the file selected, type split(condition) to group the data by common conditions";
        commands.put(command.id, command);
        
    }
    
    public JSONObject execute(String command, String [] parameters,
            DataLayer currentDataLayer,  TechniqueDAO techDAO ) throws Exception{
        this.ctx = ctx;
        this.currentDataLayer = currentDataLayer;
        Command c = null; 
        
        if (command.startsWith("ls") || command.startsWith("getdatalayers")){
            c = commands.get("ls");
            c.retMessage = ls(parameters);
        }
        
        else if (command.startsWith("readEvery")) {
            c = commands.get("readEvery");
            c.retMessage = readEvery(parameters);
        }
        
        else if (command.startsWith("clear")) {
            c = commands.get("clear");
            String [] params = new String[1];
            params[0] = "*";
            c.action = "reload";
            c.retMessage = delete(params); 
            
        }
        
        else if (command.startsWith("delete")) {
            c = commands.get("delete");
            c.action = "reload";
            c.retMessage = delete(parameters);// "Removing " + currentDataLayer.id ; 
        }
        else if (command.startsWith("hold")) {
            c = commands.get("hold");
            c.action = "reload";
            c.retMessage = hold();// "Removing " + currentDataLayer.id ; 
           
            currentDataLayer = null;
            
        }
        else if (command.startsWith("load")) {
            c = commands.get("load");
            c.action = "reload";
            c.retMessage = this.load(parameters);
        }
        
        else if (command.startsWith("register")) {
            c= commands.get("register");
            c.retMessage = this.register(parameters);
        }
        
        
        else if (command.startsWith("login")) {
            c = commands.get("login");
            c.retMessage = this.login(parameters);
        }
        
        //.. eventually we want to split it with a while loop that evaluates each datalayer of the split
        else if (command.startsWith("getlabels")) {
            c = commands.get("getlabels") ;
            c.retMessage = this.getLabels(parameters);
        }
        /*
        //.. eventually we want to split it with a while loop that evaluates each datalayer of the split
        else if (command.startsWith("getcommands")) {
            c = commands.get("getcommands");
            c.retMessage = this.getCommands();
        }*/
        
        //.. eventually we want to split it with a while loop that evaluates each datalayer of the split
        else if (command.startsWith("nback")) {
            c = commands.get("nback");
            c.retMessage = this.nBack(parameters);
        }
        
        else if (command.startsWith("tutorial")) {
            c = commands.get("tutorial");
            c.retMessage = this.tutorial();
        }
        
        if (c ==null) return null;
        return c.getJSONObject(ctx.getTutorial());
    }
    
    
    /**Deletes all but the selected dataset**/ 
    private String hold() throws Exception{
        String retMessage = "Deleted all except ";
        ArrayList<String> toKeep = new ArrayList();
        try {
            for (ChannelSet cs : super.getChanSets()) {
                toKeep.add(cs.id);
                retMessage += cs.id+",";

            }
        } catch (Exception e) {
        }

        try {
            for (Experiment e : super.getExperiments()) {
                toKeep.add(e.id);
                retMessage += e.id + ",";

            }
        } catch (Exception e) {}
        
        ctx.getDataLayers().deleteAllExcept(toKeep);
            
        return retMessage;
    }
    /**Delete selected, multiselectd, or all datalayers**/
    private String delete(String [] parameters) throws Exception {
        if (parameters.length >0) {
            //.. delete all
            ctx.getDataLayers().deleteAll();
            ctx.deselectLayer();

            return "Deleted all";
        }
        String retMessage = "Deleted ";   
        try{
            for (ChannelSet cs : super.getChanSets()) {
                retMessage +=  cs.id +", ";
                ctx.getDataLayers().removeStream(cs.id);

            }
        }
        catch(Exception e) {}

        try {
            for (Experiment e : super.getExperiments()) {
                retMessage += e.id + ", ";
                ctx.getDataLayers().removeStream(e.id);
            }
        } catch (Exception e) {};

        ctx.deselectLayer();

        return retMessage;
    }
    
    private String load(String [] parameters) throws Exception{
        String folderName = "GRProcessed";
        if (parameters.length >0)folderName = parameters[0];
        
        String folder = ctx.getServletContext().getRealPath(folderName);
        File folderF = new File(folder);
        File[] listOfFiles = folderF.listFiles();  
        int filesRead =0;
        
        if (listOfFiles == null) throw new Exception("No such folder " + folder);
        //.. Add everry file to the context
        for (int i = 0; i < listOfFiles.length; i++) {
            File fileName = listOfFiles[i];
            if (fileName.isFile()) {
                        
                BiDAO mDAO = new BiDAO();
                mDAO.make(fileName, ctx.getFileReadSampling());
                //.. if this is as yet uninitizliaed
                if (ctx.dataLayersDAO == null) {
                    ctx.dataLayersDAO = new DataLayersDAO();
                }
                //.. Add to the persistent context, and save by id 
                ctx.dataLayersDAO.addStream(mDAO.getId(), mDAO);
                ctx.setCurrentName(mDAO.getId());
                filesRead++;
            }
          
            
        }
        
        return "Read " + filesRead+ " files";
    }
    
    /** Enumerates datasets loaded in this session.**/
    private String ls(String [] parameters) throws Exception {
        String retString = "";
        DataLayersDAO dlDAOs = ctx.getDataLayers();
        for (int i = 0; i < dlDAOs.getDataLayers().size(); i++) {
            DataLayer dl = dlDAOs.getDataLayers().get(i);
            retString += dl.getId() + " with " + dl.getCount()+ " pts " + " and # channels " + dl.getChannelCount();
            if (i != dlDAOs.getDataLayers().size() - 1) {
                retString += "::";
            }
        }
        if (dlDAOs.getDataLayers().isEmpty()) return "There are no loaded datalayers";
        return retString;
    }
    
    /**
     * Handle: readyEvery(10). Make so that, when reading a file in the future, 
     * it only reads every kth point
     */
    private String readEvery(String [] parameters) throws Exception {
        String retString = "";
        int fileReadSampling = ctx.getFileReadSampling();

        //.. return current value regardless
        if (fileReadSampling == 1) {
            retString += "Currently file reads every row ";
        } else if (fileReadSampling == 2) {
            retString += "Currently file reads every other row";
        } else {
            retString += "Currently file reads every " + fileReadSampling + "th row ";
        }

        ///.. if theres a parameter change to that value
        if (parameters.length > 0) {
            int newSamplingRate = Integer.parseInt(parameters[0]);
            ctx.setFileReadSampling(newSamplingRate);
            retString += ":: Now it is " + newSamplingRate;
        }

        return retString;
    }
    
    private String register(String [] parameters) throws Exception {
        //.. if this is the first datalayer ever added
        if (ctx.dataLayersDAO == null) {
            ctx.dataLayersDAO = new DataLayersDAO();
        }

        //.. username email password
        if (parameters.length != 3) {
            throw new Exception("Not in the correct format. Correct format is 'register(username,password)'");
        }
        
        //.. UserName
        if (parameters[0].length() < 5) 
            throw new Exception("Username should be at least 5");
        
        //.. Email
        if (!parameters[1].matches("[\\w\\.\\-]+@([\\w\\-]+\\.)+[\\w\\-]+")) 
            throw new Exception("The Email is not in the correct format");
        
        //.. Password
        if (parameters[2].length() < 5) 
            throw new Exception("Password should be at least 5 characters");
        
        
        //.. Start Reg
        MySqlDAO mydao = new MySqlDAO();
        mydao.connSQL();
        
        //.. Check whether the repetition happens
        ResultSet rs = mydao.selectSQL("select * from userinfo where username='" + parameters[0] + "'");
        rs.last();
        int rowCount = rs.getRow();//Get total num
        rs.beforeFirst();
        
        if (rowCount == 0) {
            Boolean result = mydao.insertSQL("insert into userinfo(username,userpwd,email) VALUES ('" + parameters[0] + "','" + parameters[2] + "','" + parameters[1] + "')");
            System.out.println("userinfo:" + parameters[0] + "," + parameters[1] + "," + parameters[2]);
            mydao.deconnSQL();
            if (result) {
                return "Sucessful registration";
            } else {
                return "Insertion failed. Databases are not properly calibrated.";
            }
        } else {
            throw new Exception("Account already exists");
        }

    }
    
    
    public String login(String [] parts) throws Exception {
        if (parts.length != 2) 
            throw new Exception("Incorrect format; enter:  login(username, password)'");
        
        String userId = ctx.userDAO.login(parts[0], parts[1]);
        if (userId != null) {
            ctx.getRequest().getSession().setAttribute("userId", userId);
            ctx.getRequest().getSession().setAttribute("userName", parts[0]);
            ctx.getRequest().getSession().setAttribute("password", parts[1]);
            return "Succesfully logged in as " + parts[0];
        } else {
            return userId + " does not exist. ";
        }
    }
    
    /**
     * Command = GETLABELS; Returns the labels of this dataLayer
     */
    private String getLabels(String [] parameters) throws Exception {
        String content = "GETLABELS of " + currentDataLayer.getId() + "::";

        if (currentDataLayer instanceof Experiment) {
            Experiment e = (Experiment) currentDataLayer;
            content += e.classification.name + " : ";
            for (String labelVal : e.classification.values) {
                content += labelVal + ", "; //.. orka remove trailing comma
            }
        } else if (currentDataLayer instanceof ChannelSet) {
            ChannelSet bd = (ChannelSet) currentDataLayer;
           
            if (bd == null) throw new Exception("ChannelSet " + bd + " does not exist");

            if (bd.markers.isEmpty()) throw new Exception("There are no labels on this dataset");
                     
            ArrayList<Markers> markers = bd.markers;
            for (int i = 0; i < markers.size(); i++) {
                Markers m = markers.get(i);
                Classification c = m.getClassification();
                content = content + c.name + " : ";
                for (String labelVal : c.values) {
                    content += labelVal + ", ";
                }
                if (i != markers.size() - 1) {
                    content += "::";
                }
            }
        }

        return content;
    }
    /*
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
        
    } */ 
    
    /** Run a background n-back, and broadcast labels to specified port
     **/
    private String nBack(String [] parameters) throws Exception{
        if (AudioNBack.nbackRunning) throw new Exception("There is already an nback running");
        if (parameters.length <2) return "nBack takes 2 or3 parameters: n, duration, port";
        int n = Integer.parseInt(parameters[0]);
        if (n >2 || n <0) throw new Exception("Supported n-backs are 0,1,2");
        int seconds = Integer.parseInt(parameters[1]);
        int duration = seconds * 1000 - 7000; //.. since it takes 3000 to introduce it
        if (duration < 0) throw new Exception("Too short duration to play audio");
        int port =0;
        if (!(Parser.available(port))) throw new Exception("Port " + port + " is not available");

        AudioNBack nBack;
        if (parameters.length ==3) {
            port = Integer.parseInt(parameters[2]);
            nBack = new AudioNBack(n, duration, new Client(port));
        }
        else
            nBack = new AudioNBack(n, duration);
        
        if (!ctx.test) nBack.directory = ctx.getServletContext().getRealPath("WEB-INF/audio/") +"/";

        //.. Initialize nBack and run it for specified duration. It will complain if theres not a server running
        Thread t = new Thread(nBack);
        t.start();
        
        String retString = "Initialized " + n +"-back for " + seconds + "s";
        if(parameters.length ==3) retString += ". Broadcasting condition to " + port;
        
        return retString;
    }
    
    private String tutorial2() throws Exception {
        String retString = "In the topleft corner, you can see that we have created a sample random"
                + " dataset for you. To upload your own, click chose file, and then select a valid"
                + " CSV,value. The first row should contain comma-separated names; then subsequent rows"
                + " should contain time-ordered values that pertain to that column. The last k>1 columns should"
                + " be text -- a name for the trial. Subsequent rows with the same name belong to the same trial;; Now, "
                + " double click on the object to view the raw, unprocessed data. Then type split(condition) in order"
                + " to group the data by trials that pertain to the same condition ";
       
     
        ChannelSet cs = ChannelSet.generate(2, 100);
        cs.id = "Sample";
        cs.addMarkers(Markers.generate(10, 10));
        BiDAO mDAO = new BiDAO(cs);
        cs.test = true;

        //.. if this is as yet uninitizliaed
        if (ctx.dataLayersDAO == null) {
            ctx.dataLayersDAO = new DataLayersDAO();
        }
        //.. Add to the persistent context, and save by id 
        ctx.dataLayersDAO.addStream(mDAO.getId(), mDAO);
        ctx.setCurrentName(mDAO.getId());
        return retString;
    }
    
    private String tutorial() throws Exception { 
        ctx.setTutorial(true);
        ctx.inputParser.parseInput("load(tutorial)");
        return  "In the topleft corner, you can see that we have created sample"
                + " datasets for you from a real experiment. To upload your own, click chose file, and then select a valid"
                + " CSV,value. The first row should contain comma-separated names; then subsequent rows"
                + " should contain time-ordered values that pertain to that column. The last k>1 columns should"
                + " be text -- a name for the trial. Subsequent rows with the same name belong to the same trial. Alternatively, "
                + " if you have placed your folder inside build/web/input/foldername, then load(foldername) will open all files therin ";
    }
}
