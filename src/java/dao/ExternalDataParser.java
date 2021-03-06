
package dao;

import dao.datalayers.BiDAO;
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
import stripes.ext.ThisActionBeanContext;
import timeseriestufts.kth.streams.DataLayer;
import timeseriestufts.kth.streams.bi.ChannelSet;
import timeseriestufts.kth.streams.tri.Experiment;
import timeseriestufts.kth.streams.uni.Channel;

/**
 * For reading and writing to external databases
 * @author samhincks
 */
public class ExternalDataParser extends Parser{

    public ExternalDataParser(ThisActionBeanContext ctx) {
        super(ctx);
        commands = new Hashtable();
        
        //-- SAVE
        Command command = new Command("save");
        command.documentation = "Saves the selected database";
        command.debug = "Boxia implemented, untested; I've gone through it once, but am not confident";
        commands.put(command.id, command);
        
        //-- SYNCHRONIZE
         command = new Command("synchronize");
        command.documentation = "Instantiates a new local dataset to a remote database, or re-synchronizes it"
                + " if it already exists";
        command.action ="reload";
        command.parameters = "1. tablename = the name of the remote database table";
        command.selfcalibrate = "Great! Double click on this object to get an up-to-date visualization of activation patterns."
                + " It's difficult to draw any conclusions here, but it's amusing to consider that your brain is now processing information about itself! :: "
                + " We are about to study your brain at work and at relative rest. We will alternate between easy and hard editions of the cognitive workload induction task known as the nback. "
                + " First you will do the 0-back for thirty seconds, and simply repeat the number that is spoken. You will then rest for twenty seconds, before starting the 2-back, where you type "
                + " the number that was uttered two numbers ago. So, if we start off by saying 3, you say nothing. If we then say 5, you again say nothing. But when we say our third number, 4, then "
                + " you say 3, the first number, because this was the number stated two numbers ago. This will continue for thirty seconds. The console will tell you when what condition starts and periodically ask"
                + " for your personal opinion about your mental state in the previous trial. We will track and reward your accuracy. Good luck!::"
                + " Type streamlabel(easy%hard, 30%7%20) exactly to initiate 7 thirty-second 0-backs and 10 thirty-second 2-backs with 20-second rest. (You may copy-paste)"; 
        command.debug = "Works, but a new feature, so not entirely sure";
        commands.put(command.id, command);
         
        //-- STREAM
        command = new Command("stream");
        command.documentation = "Stream, in realtime, data from a database being updated and "
                + " return that data into a visualization";
        command.action = "csrefresh";
        command.debug = "Works, but axes are off. ";
        commands.put(command.id, command);
        
        
        //-- WRITE
        command = new Command("write");
        command.documentation = "Write the selected dataset to a file with the same name as its id";
        command.parameters = "1. suffix, 2. readEvery (write only every kth reading). 3. Make condition an integer";
        command.selfcalibrate = "Now we want to organize the data into two groups: one for the 0-back trials and one for the 2-back trials::"
                + "Double click on the rectangular object in the top-left, then Type realtime(easy,hard) to apply a series of manipulations to your data. ";
        commands.put(command.id, command);
        
         //-- PLAYBACK
        command = new Command("playback");
        command.documentation = "Write the selected dataset to a file with the same name as its id";
        command.action = "csrefresh";

        command.parameters = "1. filename (must be in input/)";
        commands.put(command.id, command);
        
        
        command = new Command("ff");
        command.documentation = "Fast forward to new position in Playback. Doesnt work for now";

        command.parameters = "1. Number of positions to advance";
        commands.put(command.id, command);
    }

    /**Executes the specified command
     * @param command
     * @param parameters
     * @param currentDataLayer
     * @param techDAO
     * @return
     * @throws Exception 
     */
    public JSONObject execute(String command, String[] parameters, 
            DataLayer currentDataLayer, TechniqueDAO techDAO) throws Exception {
        this.currentDataLayer = currentDataLayer;
        Command c = null;

        if (command.startsWith("save")) {
            c = commands.get("save");
            c.retMessage = this.save(parameters);
        } 
        
        else if (command.startsWith("synchronize")) {   
            c = commands.get("synchronize");  
            c.retMessage = this.synchronize(parameters);
        }
          
        else if (command.startsWith("stream(") || command.equals("stream")) {
            c = commands.get("stream");
            c.data = this.stream(parameters);   
        }      
        
        else if (command.startsWith("playback")) {
            c = commands.get("playback");
            c.data = this.playback(parameters);
        }
        
        else if (command.startsWith("ff")) {
            c = commands.get("ff");
            c.data = this.fastforward(parameters);
        }
        
        else if (command.startsWith("write")) {
            c = commands.get("write");
            c.retMessage = this.write(parameters);
        }

        if (c == null) {
            return null;
        }
        return c.getJSONObject(ctx.getTutorial(), ctx.getSelfCalibrate());
    }

    /**Save the selected dataset to a database 
     * @param parameters
     * @return
     * @throws Exception 
     */
     public String save(String[] parameters) throws Exception {
        if (ctx.dataLayersDAO.getDataLayers().size() > 0) {
            for (DataLayer dl : ctx.dataLayersDAO.getDataLayers()) {
                JSONObject obj = new JSONObject();
                String filename = dl.getId();
                DataLayerDAO dlGiver = ctx.dataLayersDAO.get(filename);

                //.. select the file
                filename = filename.split("-")[0] + ".csv";
                ChannelSet channelSet = (ChannelSet) dlGiver.dataLayer;
                ArrayList<Channel> rawValues = channelSet.streams;

                //.. connect to SQL
                MySqlDAO mydao = new MySqlDAO();
                String uuIdStr = UUID.randomUUID().toString();
                mydao.connSQL();

                //.. get the id of the user, a number
                String userId = (String) ctx.getRequest().getSession().getAttribute("userId");

                //.. Retrieve the datalayer that is associated with this user - but this assumes that there is only ONE
                ResultSet datalayerData = mydao.selectSQL("select * from datalayer where user_id='" + userId + "' and file_name like '" + filename.split(".cvs")[0] + "%'");
                while (datalayerData.next()) {
                    mydao.deleteSQL("delete from datalayer where id ='" + datalayerData.getString(1) + "'");
                    mydao.deleteSQL("delete from datalayer where parent_id ='" + datalayerData.getString(1) + "'");
                    mydao.deleteSQL("delete from datalayer_id where parent_id ='" + datalayerData.getString(1) + "'");
                }
                datalayerData.close();
                boolean num = mydao.insertSQL("insert into datalayer(id,user_id,parent_id,data,file_name) VALUES ('" + uuIdStr + "','" + userId + "','" + null + "','" + null + "', '" + filename + "')");
                mydao.deconnSQL();

                for (Channel rowValue : rawValues) {
                    StringBuffer flotArrays = new StringBuffer();
                    for (int l = 0; l < rowValue.numPoints; l++) {
                        flotArrays.append("," + rowValue.getPointOrNull(l));
                    }
                    mydao.connSQL();
                    Boolean result = mydao.insertSQL("insert into datalayer(id,user_id,parent_id,data,file_name) VALUES ('" + UUID.randomUUID().toString() + "','" + null + "','" + uuIdStr + "','" + flotArrays.toString().substring(1) + "', '" + rowValue.getId() + "')");
                    mydao.deconnSQL();
                }

                //.. Having built a channel structure and label structure, label the channel
                //... structure according to the label structure
                for (Markers markers : channelSet.markers) {
                    Labels labels = markers.saveLabels;
                    StringBuffer str = new StringBuffer();
                    for (Label label : labels.channelLabels) {
                        str.append("," + label.value);
                    }
                    mydao.connSQL();
                    mydao.insertSQL("insert into label(id,datalayer_id,labelName,channelLabels) VALUES ('" + UUID.randomUUID().toString() + "','" + uuIdStr + "','" + labels.labelName + "','" + str.toString().substring(1) + "')");
                    mydao.deconnSQL();
                }
            }
        } else {
            return "Saving the data failed. There is a database issue. ";
        }
        return "Succesfully saved the data to a database";
    }
     /**
     * Parses: synchronize(dataname). If there is a session-datalayer with the
     * name datalayer, it pings the database to make sure it is up-to-date; if
     * no such datalayer exists, it creates a new one.
     */
    public String synchronize(String [] parameters) throws Exception {
        String filename ;
        if (parameters.length ==0) filename = "realtime1";
        else filename = parameters[0];
        
        //.. if this is the first datalayer ever added
        if (ctx.dataLayersDAO == null) {
            ctx.dataLayersDAO = new DataLayersDAO();
        }

        BiDAO bDAO; //.. the datalayer we are building
   
        //.. either get bDAO or create it (create if this is the first request processed)
        if (ctx.dataLayersDAO.streams.containsKey(filename)) {
            bDAO = (BiDAO) ctx.dataLayersDAO.get(filename);
            int added = bDAO.synchronizeWithDatabase(filename);
            if(parameters.length>1) return null; //.. 2nd parameter means now output
            String ret = "Updated " + filename + " with " + added + " changes to each column";
            System.out.println(ret);
            return ret;
        } else {
            ChannelSet cs = new ChannelSet();
            cs.id = filename;
            bDAO = new BiDAO(cs);
            int added = bDAO.synchronizeWithDatabase(filename);
            ctx.dataLayersDAO.addStream(filename, bDAO);
            return "Made new datalayer " + filename + " added " + added;
        }
    }

    public JSONObject fastforward(String [] parameters) throws Exception {
        String filename = currentDataLayer.id;

        if (ctx.dataLayersDAO.streams.containsKey(filename)) {
            BiDAO bDAO = (BiDAO) ctx.dataLayersDAO.get(filename);
            if (parameters.length > 0) {
                int pos = Integer.parseInt(parameters[0]);
                bDAO.curPos += pos;
            }
            else {
                bDAO.curPos += 50;
            }
            
            ChannelSet cs = (ChannelSet) bDAO.dataLayer;
            BiDAO ancestor = (BiDAO) ctx.getAncestorOf(bDAO.getId());
            
            return ancestor.getPlaybackJSON(cs.transformations);
        }
        else {
            throw new Exception("Context does not contain datalayer " + filename);
        }
    }
    /**
     * Handle: playback(datalayername). This returns the most recent readings of
     * some dataset being read. It's like "csrefresh" or "stream" but instead of 
     * querying this streamed dataset, we give back the readings which have not been
     * read from some old dataset.
     */
    public JSONObject playback(String [] parameters) throws Exception {
        
        //TODO: Implement all code below. 
        if (currentDataLayer.id == null || currentDataLayer == null) throw new Exception("Must select a datalayer");
        String filename = currentDataLayer.id; 
        
        if (parameters.length > 0) filename = parameters[0];
        
        if (ctx.dataLayersDAO.streams.containsKey(filename)) {
            BiDAO bDAO = (BiDAO) ctx.dataLayersDAO.get(filename);
            
            //.. The selected datalayer may or may not be the actual thing which is synchronized with the database
            //.. It could have transformations applied to it, in which case we need to find its oldest ancestor,
            //.. synchronize with that, but apply the transformations specified in the layer selected
            ChannelSet cs = (ChannelSet)bDAO.dataLayer;
            BiDAO ancestor = (BiDAO) ctx.getAncestorOf(bDAO.getId());
            return ancestor.getPlaybackJSON(cs.transformations);           
        } else {
            throw new Exception("Context does not contain datalayer " + filename);
        }   
    }  
    /**
     * Handle: refresh(datalayername). Return the newest values from the
     * specified datalayer. This is a bit different from most requests in this
     * file since it's issued, typically, by code, a callback which periodically
     * issues requests according to a predefined script, ie not the user
     * herself. *
     */
    public JSONObject stream(String [] parameters) throws Exception {
        if (currentDataLayer.id == null || currentDataLayer == null) throw new Exception("Must select a datalayer");
        String filename = currentDataLayer.id; 
        
        if (parameters.length >0) filename = parameters[0];
        if (ctx.dataLayersDAO.streams.containsKey(filename)) {
            BiDAO bDAO = (BiDAO) ctx.dataLayersDAO.get(filename);
            
            //.. The selected datalayer may or may not be the actual thing which is synchronized with the database
            //.. It could have transformations applied to it, in which case we need to find its oldest ancestor,
            //.. synchronize with that, but apply the transformations specified in the layer selected
            ChannelSet cs = (ChannelSet)bDAO.dataLayer;
            BiDAO ancestor = (BiDAO) ctx.getAncestorOf(bDAO.getId());
            ancestor.synchronizeWithDatabase(ancestor.getId());  
            return ancestor.getLastUpdateJSON(cs.transformations);             
        } else {
            throw new Exception("Context does not contain datalayer " + filename);
        }   
    }  
    
    /**Write the channelset to the file.  
     * @param parameters   
     * @return    
     * @throws Exception 
     */   
    public String write(String [] parameters) throws Exception {
       String suffix = "";
       int readEvery =1;  
       boolean conToInt = false;
       if(parameters.length> 0) suffix = parameters[0];
       if(parameters.length >1) readEvery = Integer.parseInt(parameters[1]);
       if(parameters.length >2) conToInt = true;

       String file ="";  
       ArrayList<ChannelSet> chanSets = getChanSets(false);  
       if (chanSets != null) for (ChannelSet cs : chanSets) {  
            file = ctx.getServletContext().getRealPath("");
            file += "/output/" +suffix+".csv";
            if (file !=null) cs.writeToFile(file, readEvery, conToInt);
            else return "Cannot find folder: build/web/output/" +" "+ ctx.getServletContext().getRealPath(""); 
       }
       
       ArrayList<Experiment> es = getExperiments(false);
       if (es != null) for (Experiment e : es) {
           file = ctx.getServletContext().getRealPath("");
           file += "/output/" + suffix + ".csv";
           if (file != null) {
               e.writeToFile(file, readEvery, conToInt);
           } else {
               return "Cannot find folder: build/web/output/" + " " + ctx.getServletContext().getRealPath("");
           }

       }
         
       return "Successfully wrote  to  file";
    }
      
           
}
        