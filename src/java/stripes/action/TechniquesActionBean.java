/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stripes.action;

import dao.techniques.TechniqueDAO;
import java.io.StringReader;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import org.json.JSONArray;
import org.json.JSONObject;
import timeseriestufts.evaluatable.AttributeSelection;
import timeseriestufts.evaluatable.ClassificationAlgorithm;
import timeseriestufts.evaluatable.FeatureSet;
import timeseriestufts.evaluatable.Technique;
import timeseriestufts.evaluatable.WekaClassifier;
import timeseriestufts.kth.streams.DataLayer;

/**
 *
 * @author samhincks
 */
public class TechniquesActionBean extends BaseActionBean{
    
    @DefaultHandler
    public Resolution getAvailableTechniques() throws Exception{
        try{
            JSONObject jsonObj = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            for (TechniqueDAO t : ctx.techniquesDAO.getTechniques()){
                JSONObject obj = new JSONObject();    
                obj.put("id", t.getId());
                
                if (t.technique instanceof ClassificationAlgorithm) {
                    obj.put("type","Classifier"); 
                    WekaClassifier wc = (WekaClassifier) t.technique;
                    obj.put("trained", wc.timesTrained);
                }
                else if (t.technique instanceof FeatureSet)
                    obj.put("type","FeatureSet"); 
                else if (t.technique instanceof AttributeSelection)
                    obj.put("type","AttributeSelection"); 
                jsonArray.put(obj);
            }
            
            jsonObj.put("techniques", jsonArray);
            return new StreamingResolution("text", new StringReader(jsonObj.toString()));
        }
       
        catch(Exception e) {
            e.printStackTrace();
            System.err.println("Returning since  " + e.getMessage());
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("error", e.getMessage());
            return new StreamingResolution("text", new StringReader(jsonObj.toString()));
        }   
    }
    
    /**When */
}
