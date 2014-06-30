
package timeseriestufts.evaluation.featureextraction;

/**Mother class to GeneticFeature, GSRAttribute, and 
 */
public abstract class Attribute {
    public String name;
    public String type; //.. Numeric or Nominal
    public double numValue; //.. Set this if type is Numeric
    public String nomValue; //.. Set if this is a nominal value
    
    
    //.. given the description of the feature, extract it from the underlying channel
    public abstract void extract() throws Exception;
    
    public abstract String setName() throws Exception;

    public abstract weka.core.Attribute getWekaAttribute(); 
        
    
    
    
}
