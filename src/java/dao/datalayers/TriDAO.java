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

     /** Return a JSONObject representing a 3D dataset in frequency domain reprsentation
      * the x axis are the different frequencies and the y axis the power of the frequencies.
      * Options: we could make this a transitioning graph it returns all the different trials,
      * and the amplitutde of the various frequencies in them, or we could average here
      * If there were 50 trials than each bar would need to be microscopic. Maybe play around in D3 a bit?
      * This will match the performance JSON, in 
      **/
     public JSONObject getFreqDomainJSON() throws Exception {
         JSONObject jsonObj = new JSONObject();

         //.. no matter what, return a basic description of the data layer even if it hasnt been evaluated
         JSONObject descObj = new JSONObject();
         descObj.put("id", "a");
         descObj.put("type", "b");

         //.. if machine learning algorithm or 
         descObj.put("value", "c");
         jsonObj.put("trained", "d");

         //.. what more - maybe available layers? then we might have to know what type of layer it is
         jsonObj.put("description", descObj);
         JSONArray frequencies = new JSONArray();
         for (int k =0; k < 5; k++) {
            JSONObject fObj = new JSONObject();

            fObj.put("value", 0.2);
            fObj.put("expected", 0.3);
            fObj.put("label", "e");

            JSONArray subVals = new JSONArray();

            /**
             * ERROR: each fold performance is what it should be, we get 20. We
             * want the average over one condition, a set of 10
             */
            for (int i=0; i <3; i++) {
                JSONObject subPerformance = new JSONObject();
                subPerformance.put("value", 0.6);
                subPerformance.put("expected", 0.5);
                subPerformance.put("label", "f");
                subVals.put(subPerformance);
            }

            if (subVals.length() > 1) {
                fObj.put("subValues", subVals);
            }

            frequencies.put(fObj);

         }
         jsonObj.put("frequency", frequencies);
         return jsonObj;

     }
}
