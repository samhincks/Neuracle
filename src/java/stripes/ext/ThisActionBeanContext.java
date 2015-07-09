package stripes.ext; 

import dao.InputParser;
import dao.datalayers.BiDAO;
import dao.datalayers.DataLayerDAO;
import dao.datalayers.DataLayersDAO;
import dao.datalayers.UserDAO;
import dao.techniques.TechniqueDAO;
import dao.techniques.TechniquesDAO;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.ValidationError;
import realtime.AudioNBack;
import realtime.Client;
import realtime.Server;
import timeseriestufts.evaluatable.performances.Performances;
import timeseriestufts.kth.streams.DataLayer;

/**The context established between user and server. Arguably, some data that is stored
 * in context should be in session. 
 * @author Sam Hincks
 */
public class ThisActionBeanContext extends ActionBeanContext{

    public void addError(String error) {        
        ValidationError s = new SimpleError(error);
        this.getValidationErrors().addGlobalError(s);
    }
    
    public InputParser inputParser;
  
   /** Action bean context is reinitialized every 
    **/
    public ThisActionBeanContext() { 
        inputParser = new InputParser(this);
    }
    public ThisActionBeanContext(boolean test) {
        inputParser = new InputParser(this);
        this.test = test;
    }
    public boolean test = false;
 
    private static String currentDataLayer;
    private static String currentTechnique;
    public static DataLayersDAO dataLayersDAO = new DataLayersDAO(); //.. may want to save to ctx
    public static TechniquesDAO techniquesDAO = new TechniquesDAO();
    public static UserDAO userDAO = new UserDAO();
    public static Performances performances = new Performances();
    private static int fileReadSampling =1; //.. set to two and we read every other row from file
    private static boolean tutorial = false; //.. True if we're running a tutorial. 
    public static Integer curPort =null; //.. random number. this is used in an exchange between intercept label and nback
    private static Server fnirsServer;
    private static AudioNBack nback; //.. so that we can interrupt an nback we've begun
    
    public void printState() throws Exception{
        System.out.println("\t Datalayer " + currentDataLayer);
        System.out.println("\t Technique " + currentTechnique);
        getCurrentTechnique().technique.printInfo();
        getCurrentDataLayer().dataLayer.printStream();
        System.out.println("xxxxxxxxxxxxxxx");
    }
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
        if(!(test))
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
    
    /*Return oldest ancestory of a datalayer. Used, for instance, in external data parser so that
      we can stream a synched datalayer, and have transfomrations applied to it as well*/
    public DataLayerDAO getAncestorOf(String id) throws Exception{
        DataLayerDAO dDAO = this.dataLayersDAO.get(id);
        try {
            DataLayerDAO parent = this.dataLayersDAO.get(dDAO.dataLayer.parent);
            return getAncestorOf(parent.getId());
        }
        catch(Exception e) {
            return dDAO;
        }
    }
    
    /**Remove the specified datalayer. Confirmed to work**/
    public void deleteCurrent() throws Exception{
       //.. 1) Delete any references to the current one in datalayersDAO
        this.dataLayersDAO.removeStream(currentDataLayer);
    }
    public void deselectLayer() throws Exception{
       currentDataLayer =null;
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
    
    public void setFileReadSampling(int fr) {
        this.fileReadSampling = fr;
    }

    public void setTutorial(boolean b) {
        tutorial = b;
    }
       
    public boolean getTutorial() {
        return tutorial;
    }
    
    public void setNback(AudioNBack nBack) {
        this.nback = nBack;
    }
    
    public AudioNBack getNback() {
        return this.nback;
    }

    public Server getfNIRSClient(int port) throws Exception {
        if (fnirsServer == null) {
            fnirsServer = new Server(port);
            Thread t  = new Thread(fnirsServer);
            t.start(); //.. opens it up and waits until theres a connection
            Thread.sleep(500);
        }
        return fnirsServer;
    }


}