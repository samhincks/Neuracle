/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dao.datalayers;

import filereader.Label; 
import filereader.Labels;
import filereader.Markers;
import java.sql.ResultSet;
import java.util.ArrayList;  
import java.util.Arrays;
import java.util.HashMap;   
import java.util.Iterator;  
import java.util.List;
import java.util.Map;
import stripes.ext.ThisActionBeanContext;
import timeseriestufts.kth.streams.DataLayer;
import timeseriestufts.kth.streams.bi.ChannelSet;
import timeseriestufts.kth.streams.quad.MultiExperiment;
import timeseriestufts.kth.streams.tri.ChannelSetSet;
import timeseriestufts.kth.streams.tri.Experiment;
import timeseriestufts.kth.streams.uni.Channel;

/**Framework for accessing the DataLayers.
 * @author Samuel Hincks
 */
public class DataLayersDAO {
    public HashMap<String, DataLayerDAO> streams = new HashMap(); //.. map to individual structures
    public double FRAMESIZE =1;
    /**Add a new DataLayer
     * @param key
     * @param stream 
     */
    public void addStream(String key, DataLayerDAO stream) {
        streams.put(key, stream);
    }
     
    /**Get the datalayer associated with this key. It could be a collection of datalayers
     * separated by colons. In this case, return a new PileDAO, that is the merging of all the layers
     */
    public DataLayerDAO get(String key) throws Exception{
        if (streams.containsKey(key))
            return streams.get(key);
        
        //.. If its a set of 2D datalayers, merge them all together under one synched pile
        //... we  recreate this datalayer if it has already been merged in the past. We don' add it to our context
        else if(key.contains(":")) {
            //.. We don't yet know whether this multi-selection consists of multiple channel sets or Experiments
            //.. Peak at first key and let that decide
            String [] keys = key.split(":");
            String first = keys[0];
            DataLayerDAO firstDL = this.get(first);
            
            //.. this should be a collection of ChannelSets
            if(firstDL.dataLayer instanceof ChannelSet) {
                ChannelSetSet css = new ChannelSetSet();
                css.setId(key);

                //.. add each individual id, but throw an exception if it deosnt exist
                for (String id : keys) {
                    DataLayerDAO dlDAO = this.get(id);
                    if(!(dlDAO.dataLayer instanceof ChannelSet)) throw new Exception("You cannot mix a selection of 2D-Channelsets and 3D-Experiments" );
                    css.addChannelSet((ChannelSet)dlDAO.dataLayer);
                }
                
                //.. make new DAO
                TriDAO pDAO = new TriDAO(css);
                pDAO.setId(key);
                return pDAO;
            }
            
            //.. otherwise this must be a collection of Experiments
             //.. this should be a collection of ChannelSets
           else {//if(firstDL.dataLayer instanceof Experiment) {
                MultiExperiment multi = new MultiExperiment();
                multi.setId(key);
                ArrayList<TriDAO> piles = new ArrayList(); //.. save the underyling piles
                //.. add each individual id, but throw an exception if it deosnt exist
                for (String id : keys) {
                    DataLayerDAO dlDAO = this.get(id);
                    if(!(dlDAO.dataLayer instanceof Experiment)) throw new Exception("You cannot mix a selection of 2D-Channelsets and 3D-Experiments");
                    multi.addExperiment((Experiment)dlDAO.dataLayer);
                    piles.add((TriDAO)dlDAO);
                }
                
                //.. make new DAO
                QuadDAO qDAO = new QuadDAO(multi,piles);
                return qDAO;
            }

            
        }
        else
            throw new Exception("DataLayerDAO does not contain key " + key);
    }
   
    /**Get all DataLayers, structured as List. 
     * Also "sorts" them. Non-derived layers appear first in the stream
     * @return 
     */
    public List<DataLayer> getDataLayers() throws Exception {
        try {
            Iterator itr = streams.values().iterator();
            ArrayList<DataLayer> retList = new ArrayList();
            while (itr.hasNext()) {
                DataLayerDAO stream = (DataLayerDAO) itr.next();
                
                //.. put at beginning if its not derived; otherwise put at end
                if(stream.dataLayer.lacksParent())
                    retList.add(0, stream.dataLayer);
                else 
                    retList.add(stream.dataLayer);
            }
            return retList;
        }
        catch(NullPointerException n) {throw new Exception("No streams added");}
    }
    
    public void removeStream(String key) throws Exception{
        DataLayerDAO dl = streams.get(key);
        dl.dataLayer.delete(); //.. does this purge all memory to this?
        dl.dataLayer = null;
        streams.remove(key);
        dl = null;
    }
   
  
    public void deleteAll()throws Exception{
        ArrayList<String> toDelete =  new ArrayList();
        for (Map.Entry<String, DataLayerDAO> e : streams.entrySet()) {
            toDelete.add(e.getKey());
        }
        for (String s : toDelete) {removeStream(s);}
    
    }

    /**Each datalayer has a set of connections to techniques which inform how 
     they should be evaluated. Each time we evaluate reset connections before we 
     repopulate them*/
    public void resetTConnections() {
        Iterator itr = streams.values().iterator();
        while (itr.hasNext()) {
            DataLayerDAO stream = (DataLayerDAO) itr.next();
            stream.resetTConections();
        }
    }
   public void addDataByDB(ThisActionBeanContext ctx) throws Exception{
       //.. Structure for holding ALL channels
       DataLayer dataLayer;
        MySqlDAO mydao=new MySqlDAO();
        mydao.connSQL();
        String userId = (String) ctx.getRequest().getSession().getAttribute("userId");
        if (userId == null) {
            return ;
        }
        ResultSet rs=mydao.selectSQL("select * from datalayer where user_id='"+userId+"'");
        while(rs.next()){ //rs.next()??????????boolean???????????false
            ChannelSet channelSet = new ChannelSet();
            channelSet.setId(rs.getString(5));
             MySqlDAO datalayer = new MySqlDAO();
             datalayer.connSQL();
             ResultSet datalayerData = datalayer.selectSQL("select * from datalayer where parent_id = '"+rs.getString(1)+"'");
             ResultSet datalayerCount = datalayer.selectSQL("select count(*) from datalayer where parent_id = '"+rs.getString(1)+"'");
             datalayerCount.last();
             int num = datalayerCount.getInt(1);
             datalayerCount.close();
             while(datalayerData.next()){
                  List<String> strs = Arrays.asList(datalayerData.getString(4).split(","));
                  Channel c = new Channel(FRAMESIZE, strs.size()); //.. we didnt add directly to structure since we didnt know raw's size
                  c.setId(datalayerData.getString(5));
                  for (String str : strs) {
                      c.addPoint(Float.valueOf(str));
                  }
                  channelSet.addStream(c);
              }
              datalayerData.close();
               ResultSet labelData = datalayer.selectSQL("select * from label where datalayer_id = '"+rs.getString(1)+"'");
               while(labelData.next()){
                  List<String> strs = Arrays.asList(labelData.getString(4).split(","));
                  String colName = labelData.getString(3);
                  Labels labels =  new Labels(colName);
                  for (String str : strs) {
                       Label label = new Label(colName, str, 0);
                       labels.addLabel(label);
                  }
                  Markers markers = new Markers(labels);
                  channelSet.addMarkers(markers);
              }
              labelData.close();
              datalayer.deconnSQL();
              BiDAO mDAO = new BiDAO(); 
              mDAO.dataLayer = channelSet;
              mDAO.dataLayer.setId(rs.getString(5)+"fs"+ctx.getFileReadSampling()); //.. No extension
              mDAO.dataLayer.setStatsMap();    
            
            //.. if this is as yet uninitizliaed
            if (ctx.dataLayersDAO == null) {
                ctx.dataLayersDAO = new DataLayersDAO();
            }
              ctx.dataLayersDAO.addStream(mDAO.getId(), mDAO);
              ctx.setCurrentName(mDAO.getId());
        }
        rs.close();
        mydao.deconnSQL();
//        for (ArrayList<Float> raw : rawValues) {
//            Channel c = new Channel(FRAMESIZE, raw.size()); //.. we didnt add directly to structure since we didnt know raw's size
//            c.setId(channelNames.get(index));
//            
//            //.. add each value
//            for (Float f: raw){
//                c.addPoint(f);
//            }
//            
//            channelSet.addStream(c);
//            index++;
//        } 
       
        //.. Having built a channel structure and label structure, label the channel
        //... structure according to the label structure
//        for (Labels l : allLabels) {
//            Markers markers = new Markers(l);
//            channelSet.addMarkers(markers);
//        }
   }
    
}
