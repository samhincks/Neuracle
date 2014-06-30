/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dao.datalayers;

import dao.techniques.TechniqueDAO;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import timeseriestufts.evaluatable.AttributeSelection;
import timeseriestufts.evaluatable.ClassificationAlgorithm;
import timeseriestufts.evaluatable.Dataset;
import timeseriestufts.evaluatable.FeatureSet;
import timeseriestufts.evaluatable.performances.FoldPerformance;
import timeseriestufts.evaluatable.performances.Performances;
import timeseriestufts.evaluatable.performances.Predictions;
import timeseriestufts.kth.streams.DataLayer;

/**For now, just Stripes method of accessing a dataLayer, with a boolean
 * added, for whether we want to make a new construction with this channel. 
 * @author Sam Hincks
 */
public abstract class DataLayerDAO {
    public boolean add = false;
    public DataLayer dataLayer;
    public ArrayList<TechniqueDAO> tDAOs = new ArrayList(); //.. Techniques that inform evaluation
    
    
    public String getId() {
        return dataLayer.getId();
    }
    
    public void setId(String id) {
        dataLayer.setId(id);
    }

    public abstract JSONObject getJSON() throws Exception;

    public void addConnection(TechniqueDAO td) {
        tDAOs.add(td);
    }
    public void resetTConections() {
        tDAOs = new ArrayList();
    }
    
     public ArrayList<ClassificationAlgorithm> getClassifiers() {
        ArrayList<ClassificationAlgorithm> retArray = new ArrayList();
        for(TechniqueDAO tDAO : tDAOs) {
            if(tDAO.technique instanceof ClassificationAlgorithm) 
                retArray.add((ClassificationAlgorithm)tDAO.technique);
        }
        return retArray;
    }

  

    public ArrayList<FeatureSet> getFeatureSets() {
        ArrayList<FeatureSet> retArray = new ArrayList();
        for(TechniqueDAO tDAO : tDAOs) {
            if(tDAO.technique instanceof FeatureSet) 
                retArray.add((FeatureSet)tDAO.technique);
        }
        return retArray;
    }

    public ArrayList<AttributeSelection> getAttributeSelections() {
        ArrayList<AttributeSelection> retArray = new ArrayList();
        for(TechniqueDAO tDAO : tDAOs) {
            if(tDAO.technique instanceof AttributeSelection) 
                retArray.add((AttributeSelection)tDAO.technique);
        }
        return retArray;
    }

    public boolean hasOneOfEachTechnique() {
        int aSize =this.getAttributeSelections().size();
        int cSize = this.getClassifiers().size();
        int fSize = this.getFeatureSets().size();
        System.err.println(aSize + " , " + cSize +  " , " + fSize);
        if (aSize ==0 || cSize == 0 || fSize ==0) 
            return false;
        return true;
    }
    
    /**Return a JSON Object that has the stats for performances. */
    public JSONObject getPerformanceJSON(Performances p) throws Exception{
         JSONObject jsonObj = new JSONObject(); 
         Dataset d = p.getDataSet(this.getId());
         
         //.. no matter what, return a basic description of the data layer even if it hasnt been evaluated
         JSONObject descObj = new JSONObject();
         descObj.put("id", dataLayer.id);
         descObj.put("channels", dataLayer.getChannelCount());
         descObj.put("points", dataLayer.getCount());

         //.. what more - maybe available layers? then we might have to know what type of layer it is
         jsonObj.put("description", descObj);
        
         //.. there is performance stats for this datalayer
         if (d!= null) {
           JSONObject pObj = new JSONObject();
           pObj.put("label", d.getId());

           double average = d.getAverage();
           double expected = d.getExpected();
           pObj.put("value", average); 
           pObj.put("expected", expected);
         
           JSONArray subVals = new JSONArray();
        
            /**ERROR: each fold performance is what it should be, we get 20. We want the average over one condition, a set of 10
             */
            for (Predictions f : d.getGroupedPerformances()) {
                JSONObject subPerformance = new JSONObject();
                subPerformance.put("value", f.getPctCorrect()); 
                subPerformance.put("expected", f.getExpected());
                subPerformance.put("label", f.getId());
                subVals.put(subPerformance);
            }

            if (subVals.length() >1)
                pObj.put("subValues", subVals);
            
            jsonObj.put("performance", pObj);
         }
         return jsonObj;
    }

    
    
}
