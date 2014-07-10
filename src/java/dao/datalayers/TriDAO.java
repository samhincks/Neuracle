package dao.datalayers;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import timeseriestufts.evaluation.experiment.Classification;
import timeseriestufts.kth.streams.DataLayer;
import timeseriestufts.kth.streams.bi.BidimensionalLayer;
import timeseriestufts.kth.streams.bi.ChannelSet;
import timeseriestufts.kth.streams.bi.Instance;
import timeseriestufts.kth.streams.tri.ChannelSetSet;
import timeseriestufts.kth.streams.tri.Experiment;
import timeseriestufts.kth.streams.tri.TridimensionalLayer;
import timeseriestufts.kth.streams.uni.UnidimensionalLayer;


/**
 *
 * @author Sam Hincks
 */
public class TriDAO extends DataLayerDAO {
    
    private ChannelSet channelSet;
    JSONObject jsonObj;
    public static HashMap<String, DataLayer> subLayers = new HashMap(); 
    public DataLayer getSubLayer(String name) {
        return subLayers.get(name);
    }
 
    
    public TriDAO(TridimensionalLayer layer) {
        dataLayer = layer;
        dataLayer.setId(layer.id);
        this.setId(layer.getId());
    }
   
     
     /**Return a JSON representation of an Experiment for visualization in D3.
      * Experiment.id,.type, .instances
      * .instances is an array of instances, where each instance is a collection
      * of rows. Each row has a time, condition, and an array of channels
      */
     public JSONObject getJSON() throws Exception {
        jsonObj = new JSONObject();
        
        if ( dataLayer instanceof Experiment) { 
            Experiment exp = (Experiment) dataLayer;

            try {
                jsonObj.put("id", getId());
                JSONArray instances = new JSONArray();
                int mostPoints = exp.getMostPointsInAChannel();
                jsonObj.put("actualNumPoints", mostPoints);
                jsonObj.put("readingsPerSec", exp.readingsPerSec);
                
                //.. set constant max-points then compute how much we have to increment by if it is exceeded
                int MAXPOINTS = 300; //.. the number of points to display
                int pointsInc = 1;
                if (mostPoints > MAXPOINTS) {
                    pointsInc = mostPoints / MAXPOINTS;
                    jsonObj.put("maxPoints", MAXPOINTS);
                } 
                else jsonObj.put("maxPoints", mostPoints);

                //.. for each instance
                for (BidimensionalLayer bd : exp.matrixes) {
                    Instance instance = (Instance)bd;
                    JSONArray rows = new JSONArray();
                    int maxRows = bd.maxPoints();
                    
                    //.. For each row, create an object with all the channels data
                    for (int i =0; i <maxRows; i+= pointsInc) {
                        JSONObject row = new JSONObject();
                        row.put("time", i);
                        row.put("condition", instance.condition);
                        JSONArray channels = new JSONArray();

                        //.. add the value at each channel
                        for (UnidimensionalLayer channel : instance.streams){
                            Float val =null;
                            
                            //.. get it if we can; if we can't, no problem just put in null
                            try{
                                 val = channel.getPointOrNull(i);
                           
                            }catch(Exception e) {}

                            channels.put(val); //.. it might be missing
                        }
                        row.put("channels", channels);

                        //.. add to rows array
                        rows.put(row);
                    }
                    instances.put(rows);

                }
                jsonObj.put("instances", instances);
                jsonObj.put("type", "experiment");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        
        else if (dataLayer instanceof ChannelSetSet) {
            jsonObj.put("error", "Cannot yet visualize multiple channelsets");
        }
        return jsonObj;
     }

    
}
