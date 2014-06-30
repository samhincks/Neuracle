/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dao.techniques;

import dao.datalayers.DataLayerDAO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import timeseriestufts.evaluatable.AttributeSelection;
import timeseriestufts.evaluatable.AttributeSelection.ASType;
import timeseriestufts.evaluatable.ClassificationAlgorithm;
import timeseriestufts.evaluatable.ClassificationAlgorithm;
import timeseriestufts.evaluatable.FeatureSet;
import timeseriestufts.evaluatable.WekaClassifier;
import timeseriestufts.kth.streams.DataLayer;

/**Methods for interfacing with evaluation and preprocessing techniques.
 * 
 * @author samhincks
 */
public class TechniquesDAO {
    
     public HashMap<String, TechniqueDAO> techniques = new HashMap(); //.. map to individual structures
     
     public TechniquesDAO() {
         //.. Add default machine learning, settings, featureset and attribute selection
         //... which should appear in view once we've crated an experiment
         TechniqueDAO td = getDefaultML();
         techniques.put(td.getId(), td);
        
         try{td = getDefaultFeatureSet();}catch(Exception e){e.printStackTrace();};//.. error only if wrong string input
         techniques.put(td.getId(), td);
         
         td = getDefaultAttributeSelection();
         techniques.put(td.getId(), td);
     }
     
     
     private TechniqueDAO getDefaultFeatureSet() throws Exception {
          FeatureSet fs = new FeatureSet("defaultFS");
          fs.addFeaturesFromConsole("mean^slope", "*", "WHOLE");
          TechniqueDAO td = new TechniqueDAO(fs);
          return td;
     }
     
      private TechniqueDAO getDefaultAttributeSelection()  {
          AttributeSelection as = new AttributeSelection(ASType.info, 5);
          TechniqueDAO td = new TechniqueDAO(as);
          return td;
     }
    
     private TechniqueDAO getDefaultML() {
         WekaClassifier classifier = new WekaClassifier(WekaClassifier.MLType.j48);
         TechniqueDAO td = new TechniqueDAO(classifier);
         return td;
     } 
     
      private TechniqueDAO getAdditionalML() throws Exception {
         ClassificationAlgorithm classifier = new WekaClassifier(WekaClassifier.MLType.jrip);
         TechniqueDAO td = new TechniqueDAO(classifier);
         return td;
     } 
      
    /**Add a new DataLayer
     * @param key
     * @param stream 
     */
    public void addTechnique(String key, TechniqueDAO technique) throws Exception{
        if (techniques.containsKey(key)) throw new Exception("There is already a Technique with id " + key +
                ". Maybe the one you want to create already exists. Try another name");
        techniques.put(key, technique);
    }
     
    public TechniqueDAO get(String key) throws Exception{
        if (techniques.containsKey(key))
            return techniques.get(key);
        else
            throw new Exception("TechniqueDAO does not contain key " + key);
    }
    
    
    /**Get all DataLayers, structured as List. 
     * Also "sorts" them. Non-derived layers appear first in the stream
     * @return 
     */
    public List<TechniqueDAO> getTechniques() throws Exception {
        try {
            Iterator itr = techniques.values().iterator();
            ArrayList<TechniqueDAO> retList = new ArrayList();
            while (itr.hasNext()) {
                TechniqueDAO technique = (TechniqueDAO) itr.next();
                retList.add(technique);
            }
            return retList;
        }
        catch(NullPointerException n) {throw new Exception("No Techniques added");}
    }
    

    public void removeStream(DataLayerDAO dlDAO) {
        techniques.remove(dlDAO);
    }

    public void removeAll(){
        techniques = new HashMap();
    }

   
}
