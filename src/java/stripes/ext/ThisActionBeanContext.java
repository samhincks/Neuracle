package stripes.ext; 

import dao.datalayers.DataLayerDAO;
import dao.datalayers.DataLayersDAO;
import dao.datalayers.UserDAO;
import dao.techniques.TechniqueDAO;
import dao.techniques.TechniquesDAO;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.ValidationError;
import timeseriestufts.evaluatable.performances.Performances;

/**
 *
 * @author Sam Hincks
 */
public class ThisActionBeanContext extends ActionBeanContext{

    public void addError(String error) {        
        ValidationError s = new SimpleError(error);
        this.getValidationErrors().addGlobalError(s);
    }
 
    private static String currentDataLayer;
    private static String currentTechnique;
    public static DataLayersDAO dataLayersDAO = new DataLayersDAO(); //.. may want to save to ctx
    public static TechniquesDAO techniquesDAO = new TechniquesDAO();
    public static UserDAO userDAO = new UserDAO();
    public static Performances performances = new Performances();
    private static int fileReadSampling =1; //.. set to two and we read every other row from file
    
    /**Set current name of the data layer, which is the one currently being visualized
     * @param name 
     */
    public void setCurrentName(String name) {
        currentDataLayer = name;
    }
    
    public void setCurrentTechnique(String technique) {
       currentTechnique = technique;
    }
    
    public TechniqueDAO getCurrentTechnique() throws Exception{
        if (currentTechnique == null) return null;
        TechniqueDAO techDAO = this.techniquesDAO.get(currentTechnique);
        return techDAO;
    }

   /**Add a new dataLayer to dataLayersDAO
    * @param key
    * @param dataLayerDAO 
    */
    public void addDataLayer(String key, DataLayerDAO dataLayerDAO) {
        //.. Remeber the current DataLayerName
        currentDataLayer = key;
        setCurrent(currentDataLayer, dataLayerDAO);  
        dataLayersDAO.addStream(key, dataLayerDAO);
    }

    /**Get the DAO of the datalayer currently being browsed (as saved by 'currentDataLayer')
    * @return DataLayerDAO
    * @throws NullPointerException 
    */
    public DataLayerDAO getCurrentDataLayer() throws Exception {
        if (currentDataLayer == null) return null;
        try {
            DataLayerDAO currentLayer = this.dataLayersDAO.get(currentDataLayer);
            return currentLayer;
        }
        catch (Exception e) {
            //.. if this is a merged layer
            if (currentDataLayer.contains(":")) throw new Exception("Incompatible selection. Are the selected layers identical?");
            throw new Exception("The datalayer " + currentDataLayer +" is unrecognized");
        }

    }
    
    /**Get dataLayersDAO
     * @return 
     */
    public DataLayersDAO getDataLayers() throws Exception{
        if (dataLayersDAO == null) throw new Exception("There are no datasets loaded");
        return dataLayersDAO;
    }
    
    public TechniquesDAO getTechniques() {
        return techniquesDAO;
    }
  
    /**Save the object to the session
     * @param key
     * @param value 
     */
    public void setCurrent(String key, Object value) {
        getRequest().getSession().setAttribute(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getCurrent(String key) {
        T value = (T)getRequest().getSession().getAttribute(key);      
        return value; 
    }

    public Performances getPerformances() {
        return performances;
    }

    public int getFileReadSampling() {
        return fileReadSampling;
    }
    
    public void setFileReadSampling(int _) {
        fileReadSampling = _;
    }


}