/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dao.techniques;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import timeseriestufts.evaluatable.FeatureSet;
import timeseriestufts.evaluatable.Technique;
import timeseriestufts.evaluatable.WekaClassifier;
import timeseriestufts.evaluatable.performances.FoldPerformance;
import timeseriestufts.evaluatable.performances.Predictions;

/**What interactions are relevant with respect to the EvaluationTechnique?
 * We want to know its stats, its name. We need to be able to set its value 
 * 
 * @author samhincks
 */
public class TechniqueDAO {
    public Technique technique;
    
    public TechniqueDAO(Technique technique)  {
       this.technique = technique;
       this.setId(technique.getId());
    }
    public String getId() {
        return technique.getId();
    }
    
    public void setId(String id)  {
        technique.setId(id);
    }
    
    /** A representation of statistics of this technique
    */
    public JSONObject getJSON()  throws Exception{
        JSONObject jsonObj = new JSONObject();
        
         //.. no matter what, return a basic description of the data layer even if it hasnt been evaluated
         JSONObject descObj = new JSONObject();
         descObj.put("id", technique.getId());
         descObj.put("type", technique.getClass().getSimpleName());

         //.. if machine learning algorithm or 
         if(technique instanceof WekaClassifier){
             WekaClassifier wc = (WekaClassifier) technique;
             descObj.put("value", wc.mlAlgorithm);
             jsonObj.put("trained", wc.timesTrained);
         }
         
         else if (technique instanceof FeatureSet) {
             
             FeatureSet fs = (FeatureSet) technique;
             descObj.put("value", fs.getFeatureDescriptionString());
             
             //.. arrange featureset infogain calculation as jsonobj if it exists
             if (fs.infogainsAdded >0){
                 HashMap<String, Double> info = fs.infogain;
                 JSONArray attrs = new JSONArray();
                 for (Map.Entry<String, Double> m : info.entrySet()) {
                     JSONObject attr = new JSONObject();
                     attr.put("label", m.getKey());
                     attr.put("expected", 100); //.. change this
                     attr.put("value", m.getValue() / fs.infogainsAdded);
                     attrs.put(attr);
                 }
                 
                 jsonObj.put("attributes", attrs);
             }
         }
         
        //.. what more - maybe available layers? then we might have to know what type of layer it is
        jsonObj.put("description", descObj);
        if (technique.predictions != null) {
            JSONObject pObj = new JSONObject();

            double average = technique.getAverage();
            double expected = technique.getExpected();
            pObj.put("value", average); 
            pObj.put("expected", expected);
            pObj.put("label", technique.getId());
        
            JSONArray subVals = new JSONArray();

            /**ERROR: each fold performance is what it should be, we get 20. We want the average over one condition, a set of 10
             */
            for (Predictions f : technique.getGroupedPerformances()) {
                JSONObject subPerformance = new JSONObject();
                subPerformance.put("value", f.getPctCorrect()); 
                subPerformance.put("expected", f.getExpected());
                subPerformance.put("label", f.getId());
                subVals.put(subPerformance);
            }

            if (subVals.length() >1)
                jsonObj.put("subValues", subVals);
            
            jsonObj.put("performance", pObj);
        }
        
        return jsonObj;
        
    }
}
