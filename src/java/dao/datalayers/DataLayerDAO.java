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
import timeseriestufts.evaluatable.performances.Prediction;
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
    public Performances performances = null;

    
    public String getId() {
        return dataLayer.getId();
    }
    
    public void setId(String id) {
        dataLayer.setId(id);
    }

    public abstract JSONObject getJSON() throws Exception;

    public void addConnection(TechniqueDAO td) {
        if(!(tDAOs.contains(td)))
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
    
    /*Remove all references to this datalayer to free space. Ie calls delete 
     to all tiers beneath it */ 
    public void delete() {
        dataLayer.delete();
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
         Predictions pred = p.getPredictionSet(this.getId());
        
         //.. no matter what, return a basic description of the data layer even if it hasnt been evaluated
         JSONObject descObj = new JSONObject();
         descObj.put("id", dataLayer.id);
         descObj.put("channels", dataLayer.getChannelCount());
         descObj.put("points", dataLayer.getCount());

         //.. what more - maybe available layers? then we might have to know what type of layer it is
         jsonObj.put("description", descObj);
        
         //.. there is performance stats for this datalayer
         if (d!= null && p==null) { //. p == null is a little hack so that if its 2d set we look there
           JSONObject pObj = new JSONObject();
           pObj.put("label", d.getId());

           double average = d.getAverage();
           double expected = d.getExpected();
           pObj.put("value", average); 
           pObj.put("expected", expected);
         
           JSONArray subVals = new JSONArray();
        
            //.. ERROR: each fold performance is what it should be, we get 20. We want the average over one condition, a set of 10
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
         
        jsonObj.put("predictions", getClassificationsOld());
        return jsonObj;
    }
    
    public JSONObject getClassifications(double scale)  throws Exception{
        Predictions pred = performances.getPredictionSet(this.getId());
        JSONObject predObj = null;
        if (pred != null) {
            JSONArray predictions = new JSONArray();
            predObj = new JSONObject();
            predObj.put("classes", pred.classification.getJSON());
            predObj.put("length", pred.instanceLenth /scale);
            predObj.put("every", pred.everyK /scale);
            predObj.put("name", pred.techniqueSet.getClassifier().getId());
            String predictingClass =null;
            if(pred.classification.values.size() ==2){
               if(pred.classification.hasCondition("hard")) {
                    predObj.put("binaryClass", "hard");
                    predictingClass ="hard";
                }
               else {
                   predictingClass = pred.classification.values.get(0);
                   predObj.put("binaryClass", predictingClass);
               }
            }
            int position = 0;
            for (Prediction pr : pred.predictions) {
                JSONObject prediction = new JSONObject();
                prediction.put("value", pr.prediction);
                prediction.put("answer", pr.answer);
                prediction.put("confidence", pr.confidence);
                prediction.put("start", position);

                position += pred.everyK/scale;
                if (predictingClass != null) { //.. if its a binary prediction, set confidence to inverse if prediction is differnt
                    if (!(pr.prediction.equals(predictingClass)))prediction.put("confidence",1- pr.confidence);
                }
                predictions.put(prediction);
            }

            predObj.put("classifications", predictions);
        }
        return predObj;
    }
    public JSONObject getClassificationsOld()  throws Exception{
        Predictions pred = performances.getPredictionSet(this.getId());
        JSONObject predObj = null;
        if (pred != null) {
            JSONArray predictions = new JSONArray();
            predObj = new JSONObject();
            predObj.put("classes", pred.classification.getJSON());
            predObj.put("length", pred.instanceLenth);
            predObj.put("every", pred.everyK);
            for (Prediction pr : pred.predictions) {
                JSONObject prediction = new JSONObject();
                prediction.put("guess", pr.prediction);
                prediction.put("answer", pr.answer);
                prediction.put("confidence", pr.confidence);
                prediction.put("percentage", pr.conditionPercentage);
                predictions.put(prediction);
            }

            predObj.put("predictions", predictions);
        }
        return predObj;
    }
}
