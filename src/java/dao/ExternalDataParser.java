
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
        
        else if (command.startsWith("write")) {
            c = commands.get("write");
            c.retMessage = this.write(parameters);
        }

        if (c == null) {
            return null;
        }
        return c.getJSONObject(ctx.getTutorial());
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
     * no such datalayer exists, it creates a new one *
     */
    public String synchronize(String [] parameters) throws Exception {
        String filename = parameters[0];
        
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
            return "Updated " + filename + " with " + added + " changes to each column";
        } else {
            ChannelSet cs = new ChannelSet();
            cs.id = filename;
            bDAO = new BiDAO(cs);
            int added = bDAO.synchronizeWithDatabase(filename);
            ctx.dataLayersDAO.addStream(filename, bDAO);
            return "Made new datalayer " + filename + " added " + added;
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
        if (currentDataLayer.id == null) throw new Exception("Must select a datalayer");
        String filename = currentDataLayer.id; 
 
        if (ctx.dataLayersDAO.streams.containsKey(filename)) {
            BiDAO bDAO = (BiDAO) ctx.dataLayersDAO.get(filename);
            bDAO.synchronizeWithDatabase(filename);
            return bDAO.getLastUpdateJSON();
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
       ArrayList<ChannelSet> chanSets = getChanSets();
       for (ChannelSet cs : chanSets) {
            file = ctx.getServletContext().getRealPath("output/"+cs.id + suffix+".csv");
           cs.writeToFile(file, readEvery, conToInt);
       }
       
       return "Successfully wrote " + chanSets.size() + " file(s) to " + file;
    }
    

}
