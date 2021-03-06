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
import timeseriestufts.kth.streams.uni.Channel;
import timeseriestufts.kth.streams.uni.UnidimensionalLayer; 


/** Experiment visualization
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
   
    
    public JSONObject getDebugJSON() throws Exception{
        jsonObj = new JSONObject();
        Experiment e = (Experiment) dataLayer;
        jsonObj.put("type", "debug");
        jsonObj.put("id", e.id);
        if (e.transformations != null) {
            jsonObj.put("manipulation", e.transformations.transformations.size());
        }
        return jsonObj;
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
                
                //.. Add channel names
                JSONArray channelids = new JSONArray();
                Instance first = exp.matrixes.get(0);
                for (Channel c : first.streams) {
                    channelids.put(c.id);
                }
                jsonObj.put("channelnames", channelids);
                
                
                //.. set constant max-points then compute how much we have to increment by if it is exceeded
                int MAXPOINTS =400; //.. the number of points to display
                int MINPOINTS =15; 
                int MAXMATRIXES = 250; //.. adjust for having a ton of matrixes.
                if (exp.matrixes.size() > MAXMATRIXES) { //.. this is all so that we dont blow up heap
                    int diff = exp.matrixes.size() - MAXMATRIXES;
                    int newSize = MAXPOINTS - diff;
                    MAXPOINTS = (newSize > MINPOINTS) ? newSize : MINPOINTS;
                }
                
                int pointsInc = 1;
                if (mostPoints > MAXPOINTS) {
                    pointsInc = mostPoints / MAXPOINTS;
                    jsonObj.put("maxPoints", MAXPOINTS);
                } 
                else jsonObj.put("maxPoints", mostPoints);

               int index =0;
                //.. for each instance
                for (BidimensionalLayer bd : exp.matrixes) {
                    Instance instance = (Instance)bd;
                    JSONArray rows = new JSONArray();
                    int maxRows = bd.maxPoints();
                    
                    //.. For each row, create an object with all the channels data
                    for (int i =0; i <maxRows; i+= pointsInc) {
                        JSONObject row = new JSONObject();
                        row.put("time", i);
                       
                        if(i==0){
                            row.put("condition", instance.condition); //.. this and index, its kind of a pain that we put it on every point but we need to for visualization tip
                            row.put("index", index);
                        }
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
                    index++;
                }
                jsonObj.put("instances", instances);
                jsonObj.put("type", "experiment");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        
        else if (dataLayer instanceof ChannelSetSet) {
            //jsonObj.put("error", "Cannot yet visualize multiple channelsets");
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

         Experiment e = (Experiment) super.dataLayer;
         ChannelSetSet fourier = e.getAveragedFourier(false);
         int numConditions = fourier.matrixes.size();
         int numChannels = fourier.matrixes.get(0).streams.size();
         Channel frequency = fourier.matrixes.get(0).streams.get(0);
         int numFrequencies = frequency.numPoints;
         float maxFrequency = frequency.getPointOrNull(frequency.numPoints-1);
         
         //.. In the outer layer, we want as many bars as frequencies
         //... some will have vastly more power than the others, so these are further zoomable
         int numBars = 6;
         float incr = maxFrequency / numBars;
         float curFreq = 0;
         float [] outerFrequencies = new float[numBars]; //.. [0][1],first. then [1][2] etc
         JSONArray frequenciesX = new JSONArray(); //.. also add to the object we return
         for (int i=0; i <numBars; i++) {
             frequenciesX.put(curFreq);
             outerFrequencies[i] = curFreq;
             curFreq += incr;
         }

         //.. convert these measurements into indexes by consulting the frequency channel
         int [] outerFrequencyIndexes = new int [numBars];
         for (int i = 0; i < outerFrequencyIndexes.length; i++) {
             outerFrequencyIndexes[i] = frequency.findIndexOf(outerFrequencies[i]);
         }
         
         //.. no matter what, return a basic description of the data layer even if it hasnt been evaluated
         JSONObject descObj = new JSONObject();
         descObj.put("frequenciesX", frequenciesX);
         descObj.put("id", fourier.id);
         descObj.put("type", "fourier");
         descObj.put("numConditions", numConditions); //.. treat the JS array as modulo 3
         
         float maxAverage =0;
         //.. outer layer is channels, even though this is not our datalayer reprsentation
         for (int i = 1; i < numChannels; i++) { //.. i =1 since the first is the magnitude
            JSONArray frequenciesY = new JSONArray();

            //.. add the value at each condition at each frequency
            for (int k =0; k < numBars-1; k++) { //.. these will represent the frequencies
               //.. and for each frequency we have k bars, one for each k conditions, but we group into same array
               for (int j = 0; j <numConditions; j++){
                   Channel c = fourier.matrixes.get(j).getChannel(i);
                   float average = (float) c.getMean(outerFrequencyIndexes[k], outerFrequencyIndexes[k+1]);
                   JSONObject fObj = new JSONObject();
                   fObj.put("value", average); 
                   fObj.put("expected", average/2);
                   fObj.put("condition", "c"+j);
                   
                   //.. subValues, individual components of the signal, get what is between the average
                   int subIncr = (int) ((outerFrequencyIndexes[k+1] -outerFrequencyIndexes[k]) / (float)numBars);
                   JSONArray subValues = new JSONArray();
                   JSONArray subFreqs = new JSONArray();
                   for (int l = outerFrequencyIndexes[k]; l < outerFrequencyIndexes[k+1]; l+=subIncr) {
                       float subAverage = (float) c.getMean(l, l+subIncr);
                       JSONObject sObj = new JSONObject();
                       sObj.put("value", subAverage);
                       sObj.put("expected", 0.5);
                       sObj.put("condition", "c" + j);
                       subValues.put(sObj);
                       subFreqs.put(frequency.getPointOrNull(l));
                   }
                   fObj.put("subValues", subValues);
                   fObj.put("subFreqs", subFreqs);

                   
                   frequenciesY.put(fObj);
                   
                   //.. if this is the largest average, save it so we know how to scale the chart
                   if (average > maxAverage) maxAverage = average;
               }
            }
            jsonObj.put("frequency", frequenciesY);
         }
         descObj.put("max", maxAverage);
         
         //.. what more - maybe available layers? then we might have to know what type of layer it is
         jsonObj.put("description", descObj);
         return jsonObj;

    }
     
    public JSONObject getCorrelationJSON() throws Exception {
        jsonObj = new JSONObject();
        Experiment ex = (Experiment) super.dataLayer;
        try {
            jsonObj.put("id", getId());
            JSONArray data = new JSONArray();
            jsonObj.put("data", data); //.. data is array of arrays, index corresponds to order we see channel
            
            int ALPHABET = 5;
            int LENGTH = 10;//50; //.. 50  
            
            boolean first = true;
            boolean additionEncountered = false;
            //.. if points = 153400, then these parameters take about 18 seconds
            int added =0;
            for (Instance ins : ex.matrixes){
                HashMap<String, JSONObject> hm = new HashMap(); //.. This hashmap makes it take half the time. EMPIRICALLY
                int index =0;
                for (Channel a  : ins.streams) { 
                     JSONArray correlations = new JSONArray();
                     for (Channel b : ins.streams) {  
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
                     //.. data is an array of correlations, where each object is what's above
                     //.. upon acquiring a new set of correlations from channel x in a particular condition,
                     //.. this function either adds that raw correlations object to data (if its the first instnace)
                     //.. or it iterates through each existing correlation object and adds to the total the value extracted
                    additionEncountered = true;
                    if (first) {
                        data.put(correlations);
                    }
                    else {
                        if (data.length() == 0) throw new Exception ("Bug its zero!");
                        JSONArray existingCorrelations = data.getJSONArray(index);
                        for (int i = 0; i < existingCorrelations.length(); i++) {
                            JSONObject existingCorr = existingCorrelations.getJSONObject(i);
                            JSONObject newCorr = correlations.getJSONObject(i);
                            Integer corr = newCorr.getInt("data");
                            Integer curr = existingCorr.getInt("data");
                            int total = corr + curr;
                            existingCorr.put("data", total);
                        }
                    }
                    added++;                        
                    index++;
                 }
                 if (additionEncountered) first = false;
            } 
            
            //.. finally divide the totals by the number added
            for (int i=0; i < data.length(); i++) {
                JSONArray corr = data.getJSONArray(i);
                for (int j=0; j < corr.length(); j++) {
                    JSONObject obj = corr.getJSONObject(j);
                    Integer da = obj.getInt("data");
                    float numAdded =  (added*1.0f/(corr.length()*1.0f));
                    int realValue = (int) (1.0f *da / numAdded);
                    //System.out.println(numAdded + " , " +da + " , "+ realValue);
                  //  obj.put("data",realValue);
                    //System.out.println("corr between " + i + ", "+ j + " = " + realValue);
                }
            }
            
            
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        jsonObj.put("type","correlation");
        return jsonObj;

    }


}
