
package dao;

import dao.datalayers.BiDAO;
import dao.datalayers.DataLayerDAO;
import dao.datalayers.DataLayersDAO;
import dao.datalayers.MySqlDAO;
import dao.datalayers.TriDAO;
import dao.techniques.TechniqueDAO;
import filereader.Label;
import filereader.Labels;
import filereader.Markers;
import filereader.TSTuftsFileReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;  
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.UUID;
import org.json.JSONObject;
import realtime.AudioNBack;
import realtime.Client;
import stripes.ext.ThisActionBeanContext;
import timeseriestufts.evaluatable.TechniqueSet;
import timeseriestufts.evaluatable.Transformation;
import timeseriestufts.evaluation.experiment.Classification;
import timeseriestufts.kth.streams.DataLayer;
import timeseriestufts.kth.streams.bi.ChannelSet;
import timeseriestufts.kth.streams.tri.Experiment;
import timeseriestufts.kth.streams.uni.Channel;

/** Parse and process an assortment of miscellaneous commands
 * @author samhincks
 */
public class MiscellaneousParser extends Parser{ 
    public MiscellaneousParser(ThisActionBeanContext ctx) {
        super(ctx);
        commands = new Hashtable();
        
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
        command.documentation = "Loads the specified folder, though it must reside in web/input on the server";
        command.parameters = "1. foldername";
        commands.put(command.id, command);
        
        command = new Command("loadfiles");
        command.documentation = "Loads the specified files, in the web/ folder";
        command.parameters = "A comma-separated set of files";
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
        
        //-- slope
        command = new Command("stat");
        command.documentation = "Returns the stat at the the last x readings";
        command.parameters = "0. stat = slope, secondder, or bestfit, 1. datalayername, 2. channelIndex 3. readingsBack ";
        command.action = "stat";
        commands.put(command.id, command);
        
        //-- Custom
        command = new Command("custom");
        command.documentation = "A set of procedures which I change to suit my current needs";
        command.action = "reload";
        commands.put(command.id, command);
        
        
        command = new Command("custom2");
        command.documentation = "A set of procedures which I change to suit my current needs";
        command.action = "reload";
        commands.put(command.id, command);
        
        command = new Command("tutorial");
        command.documentation = "Loads a sample file, and executes basic instructions on it";   
        command.action = "reload";
        command.tutorial = "Double click on one of the files in the topleft corner to view the raw-data."
                + " Then, with the file selected, type split(condition) to group the data by common conditions";
        commands.put(command.id, command);  
        
        command = new Command("selfcalibrate");
        command.documentation = "Demonstrates how to self-calibrate a cognitive workload detection algorithm. ";   
        command.action = "reload";
        commands.put(command.id, command);

        command = new Command("interruptnback");
        command.documentation = "Stalls the current nback";
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
        
        else if (command.equals("clear") || command.equals("clear(") || command.equals("clear()")) {
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
        else if (command.startsWith("loadfiles")) {
            c = commands.get("loadfiles");
            c.action = "reload";
            c.retMessage = this.loadFiles(parameters);
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
        
        else if (command.startsWith("custom2")) {
            c = commands.get("custom2");
            c.retMessage = this.custom2(parameters);
        }
        else if (command.startsWith("custom")) {
            c = commands.get("custom");
            c.retMessage = this.custom(parameters);
        }
        else if (command.startsWith("nback")) {
            c = commands.get("nback");
            c.retMessage = this.nBack(parameters);
        }
        else if (command.startsWith("stat")) {
            c = commands.get("stat");
            c.retMessage = this.stat(parameters);
            c.action = "stat-" +this.currentDataLayer.id;

        }
        else if (command.startsWith("tutorial")) {
            c = commands.get("tutorial");
            c.retMessage = this.tutorial();
        }
        
        else if (command.startsWith("selfcalibrate")) {
            c = commands.get("selfcalibrate");
            c.retMessage = this.selfCalibrate();
        }
        
        else if (command.startsWith("interruptnback")) {
            c = commands.get("interruptnback");
            c.retMessage = this.interruptNback(parameters);
        }
        
        if (c ==null) return null;
        return c.getJSONObject(ctx.getTutorial(), ctx.getSelfCalibrate());
    }
    
    
    /**Deletes all but the selected dataset**/ 
    private String hold() throws Exception{
        String retMessage = "Deleted all except ";
        ArrayList<String> toKeep = new ArrayList();
        try {
            for (ChannelSet cs : super.getChanSets(true)) {
                toKeep.add(cs.id);
                retMessage += cs.id+",";

            }
        } catch (Exception e) {  }

        try {
            for (Experiment e : super.getExperiments(true)) {
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
            for (ChannelSet cs : super.getChanSets(true)) {
                retMessage +=  cs.id +", ";
                ctx.getDataLayers().removeStream(cs.id);

            }
        }
        catch(Exception e) {}

        try {
            for (Experiment e : super.getExperiments(true)) {
                retMessage += e.id + ", ";
                ctx.getDataLayers().removeStream(e.id);
            }
        } catch (Exception e) {};

        ctx.deselectLayer();

        return retMessage;
    }
    
    /**Loads the specified files, provided they are in the appropriate folder 
     * @param parameters
     * @return
     * @throws Exception 
     */
    private String loadFiles(String [] parameters ) throws Exception {
        String retMessage = "";
        for (String s : parameters) {
            InputStream is;
            try{
                File initialFile = new File(s);
                is = new FileInputStream(initialFile);
            }
            catch(Exception e) {
                if (!(s.startsWith("/")))  s = "/" + s;
                is = ctx.getServletContext().getResourceAsStream(s);
            }
            //.. extract name 
            String [] vals = s.split("/");
            String name = vals[vals.length-1];
            
            //.. add file if it exists
            if (is != null) 
                super.addFile(is, name);
                 
            else
                retMessage += "Couldn't read " + s + ". ";
        }
               
        return "Attempted to load " + parameters.length + " file(s). " +  retMessage;  
    }
    
    /**Loads an entire folder of files
     * @param parameters
     * @return  
     * @throws Exception   
     */       
    private String load(String [] parameters) throws Exception{
        String folderName = "GRProcessed";
        if (parameters.length >0)folderName = parameters[0];
        String folder = ctx.getServletContext().getRealPath(folderName);
        if (folder == null) return "No such folder "  + folderName + " in " + ctx.getServletContext().getRealPath("GRProcessed");
        File folderF = new File(folder);
        if (folderF == null) throw new Exception("Cannot find folder" + folder);
        File[] listOfFiles = folderF.listFiles();  
        int filesRead =0;
          
        if (listOfFiles == null) throw new Exception("No such folder " + folder);
        //.. Add everry file to the context
        for (int i = 0; i < listOfFiles.length; i++) {
            File fileName = listOfFiles[i];  
            if (fileName.isFile()) {
                super.addFile(fileName);
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
    
    /**Register as a user, if databases are currently in use. 
     * @param parameters
     * @return
     * @throws Exception 
     */
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
    
    /**If registered and using databases, login to the server
     * @param parts
     * @return
     * @throws Exception 
     */
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
   
    
    /** Run a background n-back, and broadcast labels to specified port
     **/
    private String nBack(String [] parameters) throws Exception{
        if (AudioNBack.nbackRunning) throw new Exception("There is already an nback running");
        
        int n=0;
        int seconds = 30;
        Integer port = null;
        if (ctx.curPort != null) port = ctx.curPort;
        if (parameters.length ==3) {
            n = Integer.parseInt(parameters[0]);
            if (n >2 || n <0) throw new Exception("Supported n-backs are 0,1,2");
            seconds = Integer.parseInt(parameters[1]);
            port = Integer.parseInt(parameters[2]);
        }
        
        else if (parameters.length ==1 || parameters.length ==2) {
            n = Integer.parseInt(parameters[0]);
            if (n > 2 || n < 0) 
                throw new Exception("Supported n-backs are 0,1,2");
            
            if (parameters.length == 2) 
                seconds = Integer.parseInt(parameters[1]);
            
        }     
        
        int duration = seconds * 1000 -2000; 
        if (duration < 0) throw new Exception("Too short duration to play audio");

        //.. initialize nback
        AudioNBack nBack;
        int NUMFILES =4;//.. err, little messy.
        int sequence = (int) (Math.random() * NUMFILES);
        if (ctx.curPort == null && parameters.length <3)
            nBack = new AudioNBack(n, duration, sequence);  
        else nBack = new AudioNBack(n, duration, new Client(port));        
        
        if (!ctx.test) nBack.directory = ctx.getServletContext().getRealPath("WEB-INF/audio/") +"/";

        //.. Initialize nBack and run it for specified duration. It will complain if theres not a server running
        Thread t = new Thread(nBack);
        t.start();
        
        String retString = "Initialized " + n +"back for " + seconds + "s.";
        if (port != null)
            retString += ". Broadcasting condition to " + port; 
        retString += "Nback Sequence: ";   
        return retString;    
    }  
    
    private String stat(String [] parameters) throws Exception{
        DataLayer dl = this.currentDataLayer;
        int channel = 0; 
        int channel2 = -1; //. set this to some other channel if it exists
        int readingsBack = 500;
        String stat = "slope";
        if (parameters.length >0) stat = parameters[0];
        if (parameters.length >1) {
            dl = ctx.getDataLayers().get(parameters[1]).dataLayer;
            this.currentDataLayer = dl;
        }
        if (parameters.length >2) channel = Integer.parseInt(parameters[2]);
        if (parameters.length >3) readingsBack = Integer.parseInt(parameters[3]);
        if (parameters.length >4) {
            channel2 = Integer.parseInt(parameters[3]);
            readingsBack = Integer.parseInt(parameters[4]);
        }
        
        if (dl instanceof ChannelSet) {
            //.. The selected datalayer may or may not be the actual thing which is synchronized with the database
            //.. It could have transformations applied to it, in which case we need to find its oldest ancestor,
            //.. synchronize with that, but apply the transformations specified in the layer selected
            BiDAO bDAO;
            BiDAO ancestor = null;
            ChannelSet cs = (ChannelSet) dl;
            if (ctx.dataLayersDAO.streams.containsKey(dl.id)) {
                bDAO = (BiDAO) ctx.dataLayersDAO.get(dl.id);
        
                ancestor = (BiDAO) ctx.getAncestorOf(dl.getId());
                ancestor.synchronizeWithDatabase(ancestor.getId());
            }
            //.. if this is a manipulated dataset, then apply manipulation to subset needed (readingsBack),
            //.. and compute on this instead 
            if (cs.transformations != null) {
                ChannelSet original = (ChannelSet) ancestor.dataLayer;
                Transformation t = cs.transformations.transformations.get(0);
                ChannelSet chanSet = original.getChannelSetBetween(original.getMaxPoints() - readingsBack -1, original.getMaxPoints()-1);
                chanSet = chanSet.manipulate(t, true);
                Channel c = original.streams.get(channel);
                Channel c2 = null;
                if (channel2 != -1)
                    c2 = original.streams.get(channel2);
                
                
                if (stat.equals("slope")) return "slope-" + c.getSlope()+"";
                if (stat.equals("bestfit")) return "bestfit-" +c.getBestFit()+"";
                if (stat.equals("secondder")) return "secondder-" +c.getSecondDerivative() +"";
                if( stat.equals("hrv")) return "hrv-"+c.getHRVariability()+"";
                if( stat.equals("hr")) return "hr-"+c.getPulse()+"";
                if (stat.equals("corr")) return c.getCorrelationTo(c2)+"";
            } 

            else{
                Channel c = cs.streams.get(channel);
                Channel sub = c.getSample(c.numPoints - readingsBack - 1, c.numPoints - 1, true);
                
                Channel sub2 = null;
                if (channel2 != -1) {
                    Channel c2 = cs.streams.get(channel2);
                     sub2 = c2.getSample(c.numPoints - readingsBack - 1, c.numPoints - 1, true);
                }
                
                if (stat.equals("slope")) return "slope-"+ sub.getSlope()+"";
                if (stat.equals("bestfit")) return "bestfit-"+ sub.getBestFit()+"";
                if (stat.equals("secondder")) return "secondder-"+ sub.getSecondDerivative() +"";
                if( stat.equals("hrv")) return "hrv-"+sub.getHRVariability()+"";
                if( stat.equals("hr")) return "hr-"+sub.getPulse()+"";
                if( stat.equals("corr")) return "corr-"+sub.getCorrelationTo(sub2)+"";

            }
           
        }
        throw new Exception("Must be a channelset");
    }
    
    /**Run a tutorial for the user to familiarize them with basic commands
     * @return
     * @throws Exception 
     */
    private String tutorial() throws Exception { 
        ctx.setTutorial(true);
        ctx.setFileReadSampling(3); //.. only read every three values, and hope that will make things fast enough
        ctx.inputParser.parseInput("loadfiles(tutorial/11.csv, tutorial/12.csv, tutorial/13.csv, tutorial/14.csv)");
      
        return  "In the topleft corner, you can see that we have created sample"
                + " datasets for you from a real experiment. To upload your own, click chose file, and then select a valid"
                + " tab-or-comma-separated-value file. The first row should contain set of names; then subsequent rows"
                + " should contain time-ordered values that pertain to that column. The last k>=0 columns should"
                + " be text -- a name for the trial. Subsequent rows with the same name belong to the same trial. Alternatively, "
                + " if you have manually placed your folder inside build/web/input/foldername, then load(foldername) will open all files therein ";
                
    }
    private String selfCalibrate() throws Exception {
        ctx.setSelfCalibrate(true);
        return " As a first step, we want to load realtime "
                + "measurements of oxygenation changes in the frontal lobe "
                + "of your brain. :: Type synchronize() in the console. You will see a rectangular representation of this dataset in the top left corner "
                + " of the screen.";
    }
    
    /*Interrupts any ongoing nback*/
    private String interruptNback(String [] parameters) {
        int duration = -1; //.. shut it off
        if (parameters.length >0) duration = Integer.parseInt(parameters[0]);
        ctx.getNback().interrupt(duration);
        return "Pausing for " + duration/1000 + "s... please start over";
    }
     
    private String custom2(String [] parameters) throws Exception {
       ctx.inputParser.parseInput("load(idselected)");
       ctx.setCurrentName("id-oxy-1-csvfs1");
       ctx.inputParser.parseInput("split(mark)");
        ctx.setCurrentName("id-oxy-1-csvfs1mark");
        ctx.inputParser.parseInput("keep(addition)");



       return "custom2";

    }
    private String chicustom(String[] parameters) throws Exception{
        ctx.inputParser.parseInput("load(chi)");
        ctx.inputParser.parseInput("loadfiles(output/cheatmaptbtappended.csv)");
        ctx.inputParser.parseInput("loadfiles(output/chififelthi.csv)");

        ctx.setCurrentName("cheatmaptbtappended-csvfs1");
        ctx.inputParser.parseInput("split(condition)");
        ctx.setCurrentName("cheatmaptbtappended-csvfs1condition");
        ctx.inputParser.parseInput("keep(easy,hard)");


  //      ctx.inputParser.parseInput("realtime(vizeasy,vizhard)");
        return "custom2";
    }
     private String startInterceptor( String [] parameters)  throws Exception{
         ctx.inputParser.parseInput("interceptlabel(realtime1,task, 1327)");
        Thread.sleep(300);
        ctx.inputParser.parseInput("interceptlabel(realtime1,event,1444)");
        return "custom2";
     }
      
    
    private String custom(String[] parameters) throws Exception{
        
        trainMachineLearningOnAttention(); 
        return "custom";  
    }
    
    /** For Attention Experiment - 
     *   select realtime1, split on condition, apply 
     **/
    private void trainMachineLearningOnAttention() {
        
    }
    private void loadNewProbe() throws Exception{
         ctx.inputParser.parseInput("loadfiles(output/derek.csv)");
         ctx.setCurrentName("derek-csvfs1");  
         ctx.inputParser.parseInput("split(condition)");
         ctx.setCurrentName("derek-csvfs1condition");  
         ctx.inputParser.parseInput("keep(easy,hard)");
         
         /**
          ctx.inputParser.parseInput("interceptlabel(realtime1,task, 1327)");
        Thread.sleep(300);

        ctx.inputParser.parseInput("interceptlabel(realtime1,event,1444)");**/
         

         
      
    }
    
    private void vizTest() throws Exception {
        ctx.inputParser.parseInput("load(viz)");
        ctx.inputParser.parseInput("append");
        ctx.setCurrentName("mergedrealtime1viz-csvfs1-realtime1viz2-csvfs1");
        //ctx.inputParser.parseInput("hold");
        ctx.inputParser.parseInput("realtime(vizeasy,vizhard)");


    }
    private void pluckTest() throws Exception {
        ctx.inputParser.parseInput("load(grprocessed)");
        ctx.inputParser.parseInput("append");
        ctx.setCurrentName("merged13-csvfs1-05-csvfs1-04-csvfs1-14-csvfs1-01-csvfs1-08-csvfs1-09-csvfs1-10-csvfs1-02-csvfs1-16-csvfs1-18-csvfs1-03-csvfs1-15-csvfs1-06-csvfs1-07-csvfs1-11-csvfs1-12-csvfs1-19-csvfs1");
        ctx.inputParser.parseInput("hold");
        ctx.inputParser.parseInput("realtime");
      //  ctx.setCurrentName("merged13-csvfs1-05-csvfs1-04-csvfs1-14-csvfs1-01-csvfs1-08-csvfs1-09-csvfs1-10-csvfs1-02-csvfs1-16-csvfs1-18-csvfs1-03-csvfs1-15-csvfs1-06-csvfs1-07-csvfs1-11-csvfs1-12-csvfs1-19-csvfs1conditioneasyhardaveragedcalcoxyconditioneasyhardlowpass0-3conditioneasyhard");;
        //ctx.inputParser.parseInput("hold");  
        //ctx.inputParser.parseInput("pluck");

    }
    private void slopeClassifier() throws Exception { 
        ctx.inputParser.parseInput("load(baseline)");
        ctx.setCurrentName("baseline2-csvfs1");
        ctx.inputParser.parseInput("manipulate(averagedcalcoxy)");
        ctx.inputParser.parseInput("synchronize)");
        ctx.setCurrentName("realtime1");
        ctx.inputParser.parseInput("makeml(slope)");
    }  
    
     private String loadData()  throws Exception {
         ctx.inputParser.parseInput("load(hincks)");
         ctx.inputParser.parseInput("append");
         ctx.setCurrentName("mergedtest2-csvfs1-test3-csvfs1-test9-csvfs1-test7-csvfs1-test8-csvfs1");
         ctx.inputParser.parseInput("realtime");
         return "";
     }
     private String manipulateTest() throws Exception {
        ctx.inputParser.parseInput("load(hincks)");
        ctx.inputParser.parseInput("append");
        
        ctx.setCurrentName("mergedtest2-csvfs1-test3-csvfs1-test9-csvfs1-test7-csvfs1-test8-csvfs1");
        ctx.inputParser.parseInput("hold");
        ctx.inputParser.parseInput("realtime");

        //ctx.inputParser.parseInput("split(condition)");
        //ctx.setCurrentName("mergedtest8-csvfs1-test3-csvfs1-test9-csvfs1-test2-csvfs1-test7-csvfs1condition");
        //ctx.inputParser.parseInput("keep(easy,hard)");
       // ctx.setCurrentName("mergedtest8-csvfs1-test3-csvfs1-test9-csvfs1-test2-csvfs1-test7-csvfs1conditionconditioneasyhard");
       // ctx.inputParser.parseInput("manipulate(zscore)");
        //ctx.inputParser.parseInput("mergedtest8-csvfs1-test3-csvfs1-test9-csvfs1-test2-csvfs1-test7-csvfs1mergedtest8-csvfs1-test3-csvfs1-test9-csvfs1-test2-csvfs1-test7-csvfs1conditionconditioneasyhardzscoreconditioneasyhard"); 
        //ctx.inputParser.parseInput("manipulate(anchor)");;
        // ctx.inputParser.parseInput("makeml(*)");      
        //ctx.inputParser.parseInput("makefs(slope^bestfit^smallest,*,*)");

        return "loaded hincks";
    }

}
