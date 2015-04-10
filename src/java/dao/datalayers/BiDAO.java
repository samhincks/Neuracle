  /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dao.datalayers;

import filereader.ChannelSetFromStringsMaker;
import filereader.Label;
import filereader.Labels;
import filereader.Markers;
import filereader.TSTuftsFileReader;
import java.awt.Point;      
import java.sql.ResultSet;  
import java.sql.ResultSetMetaData;     
import java.util.ArrayList;
import java.util.HashMap;                 
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;   
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sourceforge.stripes.action.FileBean;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import timeseriestufts.kth.streams.DataLayer;
import timeseriestufts.kth.streams.bi.BidimensionalLayer;
import timeseriestufts.kth.streams.bi.ChannelSet;
import timeseriestufts.kth.streams.uni.Channel;
import timeseriestufts.kth.streams.uni.UnidimensionalLayer;

/**
 *
 * @author Sam Hincks
 */
public class BiDAO extends DataLayerDAO {
    ArrayList<DataLayer> channels;
    String id; 
    private ChannelSetFromStringsMaker csMaker; //.. if we are creating a datalyer from a series of strings
    
    //protected SemiSynchedChannelSet SSChannelSet; 
    protected JSONObject jsonObj;
    public boolean synchronizedWithDatabase = false; //.. if this layer changes in response to a database
    public int numSynchronizations =0; //.. number of times this has been synchronized
    public int addedInLastSynchronization =0;
    public int curPos = 0; //.. DELETE THIS LATER
    public Hashtable<String,String> curLabels = null; //.. when data is streamed in
    public ArrayList<Labels> labels = null; //.. a collection of labels describing condition of corresponding points
    public BiDAO(ArrayList<DataLayer> channels) {
        this.channels = channels;
    }
    
    public BiDAO(BidimensionalLayer dl) throws Exception{
        dataLayer = dl;
        dataLayer.setId(dl.id); 
        this.id = dl.id;
        dataLayer.setStatsMap();
    }
    public BiDAO(){}      
    
    public BiDAO(String id) {this.id = id;}
    
    
    //**Create new dataLayer from input file, with a specified sampling rate*/
    public void make (FileBean fb, int fileSampling) throws Exception {        
        TSTuftsFileReader fileReader = new TSTuftsFileReader();
        try{
            dataLayer = fileReader.readData(",",fb); //.. try to do csv, but fall back on \t
        }
        catch(Exception e) {
            dataLayer = fileReader.readData("\t", fb); //.. then if this doesn't work, give up and moan

        }
        dataLayer.setId(fb.getFileName()+"fs"+fileSampling); //.. No extension
        dataLayer.setStatsMap();        
    }
    
  
    
    /**String together a datalayer from a series of messages; meant to act as a proxy
     for reading from a file, but ultimately this has been deprecated.  ***/
    public void addMessage(int index,int totalSize, String filename, String data) throws Exception{
        //.. case 0: its the first we get 
        if (csMaker == null) csMaker = new ChannelSetFromStringsMaker();
        
        //.. in all cases, add the corresponding data
        csMaker.addMessage(index, totalSize, filename, data);
        
        //.. csMaker will make the channelset if what has been promised is added
        if (csMaker.channelSet != null) {
            System.err.println("Now finally adding the channelset");
            dataLayer = csMaker.channelSet;
            dataLayer.setId(id);
            dataLayer.setStatsMap();
        }
    }
    
    
    /**Add all the channels to a channelSet (they may be unsynched)
     * @param name 
     */
    public void makeMatrix(String name) throws Exception {
        ChannelSet channelSet = new ChannelSet(); 
        for (DataLayer channel : channels) {
            channelSet.addStream((Channel)channel);
        }
        dataLayer = channelSet;        
        dataLayer.setId(name );
        //dataLayer.setStatsMap();
    }
    
    /**  
     Return a correlation matrix between each pairwise channel . 
     * Either SAX correlation 
    **/    
    public JSONObject getCorrelationJSON() throws Exception {
        jsonObj = new JSONObject();
        ChannelSet channelSet = (ChannelSet)dataLayer;
        try {
            jsonObj.put("id", getId());
            JSONArray data = new JSONArray();
            jsonObj.put("data", data); //.. data is array of arrays, index corresponds to order we see channel
            
            int ALPHABET = 5;
            int LENGTH = 50; //.. 50  
            
            if (channelSet.getCount() > 250000)
                LENGTH =25;
            
            //.. if points = 153400, then these parameters take about 18 seconds
            HashMap<String, JSONObject> hm = new HashMap(); //.. This hashmap makes it take half the time. EMPIRICALLY
            
            for (Channel a  : channelSet.streams) { 
                 JSONArray correlations = new JSONArray();
                 data.put(correlations); // each channel has correlations to all other channels   
                 for (Channel b : channelSet.streams) {  
                     String comboId = a.id + b.id;
                     String comboId2 = b.id +a.id;//.. it will be in either one of those
                     JSONObject aFirstVal = hm.get(comboId);
                     JSONObject bFirstVal = hm.get(comboId2);
                     
                     if (aFirstVal != null) 
                         correlations.put(aFirstVal);
                     
                     
                     else if (bFirstVal != null)  //.. I think this is the only one that is active, prove it and you can delete a
                         correlations.put(bFirstVal);
                     
                     else{ //.. With the hashmap this should only happen 50% of the time
                        int diff = b.getSAXDistanceTo(a,LENGTH, ALPHABET); //.. MAGIC PARAMETER! 750 takes super long but what ive been doing
                        JSONObject o = new JSONObject();
                        o.put("data", diff);
                        o.put("i", a.id);
                        o.put("j", b.id);
                        
                        hm.put(comboId, o);
                        correlations.put(o);

                     }
                 }
             }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        jsonObj.put("type","correlation");
        return jsonObj;

    }
    
     
    /**Return JavaScriptObject Notation of the datalayer
     * --Notation for movingLinegraph-- 
     * var data = {
     *  "start": 0 // The x of the first point
     *  "end": // The x of the last point
     *  "step":// The leap in x between points
     *   "names":["a","b","c","d"] // The id of channels
     * "values":[[2,4,6,7][1,2,3,4], ] // array of array of channels

     * @return JSONObject
     */
     public JSONObject getJSON() throws Exception  {    
      jsonObj = new JSONObject();
      ChannelSet channelSet = (ChannelSet)dataLayer;
      if (this.synchronizedWithDatabase) synchronizeWithDatabase(this.id);
       try { 
            jsonObj.put("id", getId());
            JSONObject data = new JSONObject();
            jsonObj.put("data", data);
            
            //.. data for properly aligning x axis
            int mostPoints = channelSet.getMaxPoints();
            jsonObj.put("actualNumPoints", mostPoints);
            jsonObj.put("readingsPerSec", channelSet.readingsPerSecond);
            
            //.. default variables
            data.put("start", 1);
            
            for (String s : channelSet.getColumnNames()) {
            }
            
            JSONArray values = new JSONArray();
            
            //.. only show some channels if we have absurdly many, and only 
            //... show so many points if we have absurdly many
            int MAXCHANNELS = 7;
            int numChannels = channelSet.getChannelCount();
            int chanInc = 1;
            if (numChannels> MAXCHANNELS)
                chanInc = numChannels / MAXCHANNELS;
            
            //.. Condense the data, setting maximum points to stream
            int MAXPOINTS = 300;     
            int numPoints;    
            
            //.. add id of each column as label, then add to the data obj
            JSONArray names = new JSONArray();
            
            //.. Create each channel
            for (int i=0; i< numChannels; i+= chanInc) {
                UnidimensionalLayer channel = channelSet.getChannel(i);
                names.put(channel.id);

                //.. Add each point in data to JSONArray
                //... BUT DO NOT ADD MORE THAN MAX POINTS
                int pointsInc = 1;
                numPoints = channel.numPoints;
                if (numPoints > MAXPOINTS)
                    pointsInc = numPoints / MAXPOINTS;
                data.put("step", pointsInc);
                data.put("end", numPoints);

                JSONArray channelData = new JSONArray();
                
                //.. add points at specified increments
                for (int j=0;j < numPoints; j+=pointsInc) { //. 0, numPoints, change later
                    float p = channel.getPointOrNull(j);
                    channelData.put(p);
                }
                values.put(channelData);
            }

            //... Add numerically visualizable markers
            for (int i=0; i< channelSet.markers.size(); i++ ) {
                JSONArray channelData = new JSONArray();
                Markers m = channelSet.markers.get(i);

                //.. Add each point in data to JSONArray
                //... BUT DO NOT ADD MORE THAN MAX POINTS
                int pointsInc = 1;
                numPoints = m.saveLabels.channelLabels.size();
                if (numPoints > MAXPOINTS) {
                    pointsInc = numPoints / MAXPOINTS; 
                }
                data.put("step", pointsInc);
                data.put("end", numPoints);

                //.. add points at specified increments
                for (int j = 0; j < numPoints; j += pointsInc) { //. 0, numPoints, change later
                    String conName = m.saveLabels.channelLabels.get(j).value;
                    int index = m.getClassification().getIndex(conName);
                    channelData.put(index);
                }
                values.put(channelData);  
                names.put(m.name);
            }
            
            data.put("names", names);
            
            //.. save this array
            data.put("values", values);

            jsonObj.put("type", "channelset");
        }
        catch (JSONException e) {e.printStackTrace();}
        
        return jsonObj;
    } 
     
    /**
     * Potenitally, this datalayer is periodically updated with respect to a
     * changing database; If such an update has just occurred, then calling this
     * function will return only those points which were added in the last
     * refresh, structured as a JSON obj as below
     */
    public JSONObject getLastUpdateJSON() throws Exception{
        jsonObj = new JSONObject();
        ChannelSet channelSet = (ChannelSet) dataLayer;
      
        try {
            JSONObject data = new JSONObject();

            //.. add id of each column as label, then add to the data obj
            JSONArray names = new JSONArray();
            for (String s : channelSet.getColumnNames()) {
                names.put(s);
            }
            data.put("names", names);

            /*Values should be an an array of arrays, the inner values are
             timestamp at each channel and the outer values are the timestamps
             */
            JSONArray values = new JSONArray();
            int pointsInc =1;
            
            //.. Add each point in data to JSONArray
            //... BUT DO NOT ADD MORE THAN MAX POINTS
            int MAXPOINTS = 30;
            int numPoints = channelSet.getMinPoints();
            if (this.addedInLastSynchronization > MAXPOINTS) {
                pointsInc = this.addedInLastSynchronization / MAXPOINTS;
            }
            int startingPoint = numPoints - this.addedInLastSynchronization;
             
            //.. add points at specified increments
            for (int j = startingPoint; j < numPoints; j += pointsInc) {
                JSONArray timeData = new JSONArray(); //.. each channel's value at the timestamp
             //   if (channelSet.getChannelCount()!= 2) System.out.println("not 2 : " +channelSet.getChannelCount());
                for (int i = 0; i < channelSet.getChannelCount(); i++) {
                    UnidimensionalLayer channel = channelSet.getChannel(i);
                    //if (channel.numPoints != numPoints) throw new Exception("Misaligned channels. First has " + numPoints + " . " +i + " has " + channel.numPoints);
                    Float p = channel.getPointOrNull(j);          
                    if (p!= null){   
                        JSONArray arr = new JSONArray();   
                        arr.put(p);      
                        timeData.put(arr);
                    }
                }
                values.put(timeData);        
            }          
                
            int mostPoints = channelSet.getMaxPoints();
            float maxTime = (mostPoints / channelSet.readingsPerSecond);
            data.put("maxTime", maxTime);
            data.put("start", startingPoint);   
            data.put("end", numPoints);
            data.put("step", pointsInc);
            data.put("values", values);
            
            //.. save this array
            jsonObj.put("id", getId());
            jsonObj.put("data", data);
            jsonObj.put("type", "csrefresh");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObj;
    }

    
    /**Return JavaScriptObject Notation of the datalayer
     * data[i].timestamp = Point.timestamp,
     * data[i].value = Point.value
     * id = id
     * @return JSONObject
     */
     public JSONObject getJSON2() throws Exception  {    
      jsonObj = new JSONObject();
      ChannelSet channelSet = (ChannelSet)dataLayer;
     
      /**WE use to do this when we cared about providing a perfectly accurate view for 
       * unsynched Datasets. Now we just asssume framesize is equal for a datalayer
       double minFramesize = channelSet.getSmallestFramesize();
       SSChannelSet =  channelSet.getSemiSynchedChannelSet(minFramesize);**/
       try { 
            jsonObj.put("id", getId());
            JSONArray allChannels = new JSONArray();
            
            //.. only show some channels if we have absurdly many, and only 
            //... show so many points if we have absurdly many
            int MAXCHANNELS = 7;
            int numChannels = channelSet.getChannelCount();
            int chanInc = 1;
            if (numChannels> MAXCHANNELS)
                chanInc = numChannels / MAXCHANNELS;
            for (int i=0; i< numChannels; i+= chanInc) {
                UnidimensionalLayer channel = channelSet.getChannel(i);
                JSONObject channelObj = new JSONObject();
               
                //.. Add each point in data to JSONArray
                //... BUT DO NOT ADD MORE THAN MAX POINTS
                int MAXPOINTS = 300;
                int numPoints = channel.numPoints;
                int pointsInc = 1;
                if (numPoints > MAXPOINTS)
                    pointsInc = numPoints / MAXPOINTS;
                JSONArray data = new JSONArray();
                
                //.. add points at specified increments
                for (int j=0;j < numPoints; j+=pointsInc) {
                    float p = channel.getPointOrNull(j);
                    JSONObject obj = new JSONObject();                    
                    obj.put("timestamp", j);
                    obj.put("value", p);
                    data.put(obj);
                }
                //.. add data/id to chanenl obj
                channelObj.put("id", channel.id);
                channelObj.put("data", data);

                //.. add this channel obj to array of channels
                allChannels.put(channelObj);

            }
            //.. save this array
            jsonObj.put("channels", allChannels);
            
            jsonObj.put("type", "channelset");

            //.. add an array in which we put timestamps that should be highlighted
            jsonObj.put("scopestamps", new JSONArray());
        }
        catch (JSONException e) {e.printStackTrace();}
        
        return jsonObj;
    } 
    
    
     /**Read data from the database with the corresponding name, and synchronize with a
      * ChannelSet
      **/
    public int synchronizeWithDatabase(String databaseName) throws Exception{
        if (!(dataLayer instanceof ChannelSet || dataLayer ==null)) throw new Exception("Must be channelset");
        ChannelSet cs = (ChannelSet) dataLayer;
        this.synchronizedWithDatabase = true;
        this.id = databaseName;
        this.addedInLastSynchronization =0;
        this.numSynchronizations++;
        boolean refreshMarkers = false; //.. set to true if there are new markers to synchronize with db


        MySqlDAO mydao=new MySqlDAO();
        mydao.connSQL();
        ResultSet datalayerData = mydao.selectSQL("select * from " + databaseName);
        if (datalayerData == null) throw new Exception("No database called " +databaseName);
        ResultSetMetaData meta = datalayerData.getMetaData();
       
        //.. if this channelset's streams is empty, we have yet to synchronize it,
        //.. and we are obligated to initialize its columns
        if (cs.streams.isEmpty()) {
            //.. for each column in the database
            for (int i =1; i < meta.getColumnCount()+1; i++) { //..  first is just row#
                datalayerData.first();
                ArrayList<Float>  data = new ArrayList();
                //.. add the data from every row to a corresponding buffer
                while (datalayerData.next()) {
                    float val =Float.parseFloat(datalayerData.getString(i));
                    data.add(val);
                }
                Channel c = new Channel(1, data); //. framesize is technically wrong
                c.id = meta.getColumnLabel(i);
                cs.addStream(c);
                datalayerData.first();
                addedInLastSynchronization = data.size();
            } 
            
        }
        
        //.. if this has already been initialized, add data to corresponding channels,
        //... advance rs cursor to point of length of column, then add whatever else is there
        else {
            int curPoints = cs.getNumPointsAtZero();
            if(meta.getColumnCount() != 2) System.out.println("NOT 2 .. :" + meta.getColumnCount());

            //.. for each column in the database
            for (int i =1; i < meta.getColumnCount()+1; i++) { //..  first is just row#
                datalayerData.last();
                
                int numRows = datalayerData.getRow();
                addedInLastSynchronization = numRows - curPoints;
                
                //.. ADD the amount to add backwards. 
                for (int j = 0; j < addedInLastSynchronization; j++) {
                    cs.appendToStream(i-1, Float.parseFloat(datalayerData.getString(i)));
                    datalayerData.previous();
                }
                datalayerData.last();  
                //.. only for the first column, add any labels if any exist
                if (i==1 && curLabels != null && labels != null) {
                    for (Labels l : labels) {
                        for (int j = 0; j < addedInLastSynchronization; j++) {
                            String val = curLabels.get(l.labelName);
                            l.addLabel(new Label(l.labelName, val, l.channelLabels.size()));
                            refreshMarkers =true;
                        }
                    }
                }
            }
        }
        if (refreshMarkers) {    
            for (Labels l : labels) { 
                Markers m = new Markers(l);
                cs.addOrReplaceMarkers(m); 
            } 
        }
        mydao.deconnSQL();
        return addedInLastSynchronization;
            
    }
    /** Save this datalayer to a database
     **/
    public void saveToDatabase(String userId) throws Exception{
        if (!(dataLayer instanceof ChannelSet)) throw new Exception("Must be a channelset");
        ChannelSet cs = (ChannelSet) dataLayer;
        
        MySqlDAO mydao = new MySqlDAO();
        String uuIdStr = UUID.randomUUID().toString(); //.. what is this?
        mydao.connSQL();

        //.. Retrieve the datalayer that is associated with this user - but this assumes that there is only ONE
        mydao.createTable(cs.id, userId, cs.getColumnNames());
        for (Channel c : cs.streams) {
            mydao.addColumnToTable(cs.id, c.id, c);
        }
        mydao.deconnSQL();
        
       // boolean num = mydao.insertSQL("insert into datalayer(id,user_id,parent_id,data,file_name) VALUES ('"+uuIdStr+"','"+userId+"','"+null+"','"+null+"', '"+filename+"')");
        
        /** Next step : clean up what was done and make so that the save command adheres
         * to the new structure. Then see if we can establish a connection between D3 or some
         * other moving stream JS library that is just ticking along. The communication
         * between backend and front end is no longer stripes but through databases 
         **/
                           
    }
    
  
    public static void main(String[] args) {
        try {
            int TEST =1;
            ChannelSet cs = ChannelSet.generate(1, 30);
            BiDAO bcs = new BiDAO(cs);
            
            if (TEST ==0)
                bcs.saveToDatabase("samhincks");
            
            if (TEST ==1) {
                cs = new ChannelSet();
                bcs = new BiDAO(cs);
                bcs.synchronizeWithDatabase("realtime1");
                cs.printStream();
                
            }
            
            
        
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int getNumReadings() throws Exception{
        ChannelSet cs = (ChannelSet) dataLayer;
        return cs.getNumPointsAtZero();
    }

    public void addMarkers(Markers markers) {
        ChannelSet cs = (ChannelSet) dataLayer;
        cs.addMarkers(markers);
    }

    /**Set the current label of data streamed in, so that any fresh data
     receives that label**/
    public void setStreamedLabel(String labelName, String labelValue) {
        System.out.println("        Current label is " + labelValue + " of " + labelName);
        if(this.curLabels == null) this.curLabels = new Hashtable();
        this.curLabels.put(labelName, labelValue);
    }

    /**If this channelset has markers with specified name, return it, otherwise
     return null**/
    public Markers getMarkersWithName(String labelName) {
        ChannelSet cs = (ChannelSet) dataLayer;
        if (cs.markers != null) {
            for (Markers m : cs.markers){
                if (m.name.equals(labelName))
                    return m;
            }
        }
        return null;
    }
    
    public Labels getLabelsWithName(String labelName) {
         if (labels != null) {
            for (Labels l : this.labels){
                if (l.labelName.equals(labelName))
                    return l;
            }
        }
        return null;
    }

    /** Add labels that will eventually be added to the dataset
     **/
    public void addLabels(Labels labels) {
        if (this.labels == null) this.labels = new ArrayList();
        this.labels.add(labels);
    }

   


   
}
