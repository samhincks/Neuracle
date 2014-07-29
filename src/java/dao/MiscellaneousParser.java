/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dao;

import dao.datalayers.DataLayerDAO;
import dao.datalayers.DataLayersDAO;
import dao.datalayers.MySqlDAO;
import dao.techniques.TechniqueDAO;
import filereader.Label;
import filereader.Labels;
import filereader.Markers;
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
    public MiscellaneousParser() {
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
        
    }
    
    public JSONObject execute(String command, String [] parameters, ThisActionBeanContext ctx, 
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
            ctx.dataLayersDAO.removeAll();
            c.action = "reload";
            c.retMessage =  "Clearing surface"; //... 
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
        
        //.. eventually we want to split it with a while loop that evaluates each datalayer of the split
        else if (command.startsWith("getcommands")) {
            c = commands.get("getcommands");
            c.retMessage = this.getCommands();
        }
        
        //.. eventually we want to split it with a while loop that evaluates each datalayer of the split
        else if (command.startsWith("nback")) {
            c = commands.get("nback");
            c.retMessage = this.nBack(parameters);
        }
        
        if (c ==null) return null;
        return c.getJSONObject();
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
    
    
    
    
}
