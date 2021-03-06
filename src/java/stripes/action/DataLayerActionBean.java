/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stripes.action;

import dao.datalayers.BiDAO;
import dao.datalayers.DataLayerDAO;
import dao.datalayers.DataLayersDAO;
import dao.datalayers.TriDAO;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.Validate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import static stripes.action.BaseActionBean.INDEX;
import timeseriestufts.evaluatable.performances.Performances;
import timeseriestufts.kth.streams.DataLayer;

/** This is what is called when we load a file. We directly communicate with this file through
 * HTML. Ie, unlike everything else in the project the interface between java and javascript
 * here happens through Stripes alone. Rather than the hacked message passing protocol in
 * the file javainterface.js
 * Abstract class with implementations for each 'dataLayer', channel, matrix etc
 * @author Sam Hincks
 */
public class DataLayerActionBean extends BaseActionBean{  
    
    public Resolution load() { 
        return new ForwardResolution(INDEX);
    }
    
      //.. FILE
    @Validate(required = true, on = "registerFile")
    private FileBean newAttachment;
    public FileBean getNewAttachment() { return newAttachment;}
    public void setNewAttachment(FileBean newAttachment) {this.newAttachment = newAttachment; }
   
    //.. Test Text
    private String testText; 
    public String getTestText() {return testText;}
    public void setTestText(String test) {this.testText = test; }
    
    public Resolution reload() { 
       return new ForwardResolution(INDEX);
    }
    /**Called when we read a file. Redirect to screen, but add thsi layer 'as a box'. 
     * What does it mean to have registered a file? It means that we have it saved in context.
     * Specifially, in ctx.dataLayersDAO. 
     */
    public Resolution registerFile() {
        System.out.println(newAttachment.getFileName() + " , " + newAttachment.toString());
        BiDAO mDAO = new BiDAO(); 
        try {
            mDAO.make(newAttachment, ctx.getFileReadSampling());
            
            //.. if this is as yet uninitizliaed
            if (ctx.dataLayersDAO == null) {
                ctx.dataLayersDAO = new DataLayersDAO();
            }
            //.. Add to the persistent context, and save by id 
            ctx.dataLayersDAO.addStream(mDAO.getId(), mDAO);
            ctx.setCurrentName(mDAO.getId());
        }  
        catch (Exception e) {
            e.printStackTrace();  
            ctx.addError(e.toString());   
        } 

        return new ForwardResolution(INDEX);
    }
    
    public String getCurrentClassName() { return getCurrentDataLayer().getClass().getSimpleName();}
    
    /**Returns a map of stats about the datalayer, e.g (Mean, 2.0)
     * @return Map of stats
     */
    public Map<String,Double> getCurrentStatsMap() { return getCurrentDataLayer().statsMap;}   
    
    /**Override this function to make new data structure and redirect
    * @return Resolution
    */
    protected DataLayer currentDataLayer; //.. The DataLayer we are displaying    
    public DataLayer getCurrentDataLayer(){return currentDataLayer;}
    
    private static String receiver ="unnamed";
    public void setReceiver(String name) {this.receiver = name;}
    public String getReceiver() { return receiver;}

    private static String giver ="unnamed";
    public String getGiver() { return giver;}
    public void setGiver(String name) {this.giver = name;}
    
    
    private static boolean stats =false;
    public boolean getStats() { return stats;}
    public void setStats(boolean stats) {this.stats = stats;}
    
    private static boolean debug =false;
    public boolean getDebug() { return debug;}
    public void setDebug(boolean debug) {this.debug = debug;}
    
    private static boolean frequency =false;
    public boolean getFrequency() { return frequency;}
    public void setFrequency(boolean frequency) {this.frequency = frequency;}
    
    private static boolean correlation =false;
    public boolean getCorrelation() { return correlation;}
    public void setCorrelation(boolean correlation) {this.correlation = correlation;}
    
    private static boolean prediction =false;
    public boolean getPrediction() { return prediction;}
    public void setPrediction(boolean prediction) {this.prediction = prediction;}
    
    
    private boolean merge = false; //.. hacky, but set this to true if the channels should be merged
    public void setMerge(boolean merge) {this.merge = merge;} //.. set to false when its done
    public boolean getMerge() {return merge; }

    @DefaultHandler
    
    /*Return a Streaming Resolution of the datalayer as a json object.
     * The "giver" is the is of the object currently being touched
     */
    public Resolution getJSON() throws JSONException{    
        try {
          /*  System.out.println(ctx.getRequest().getRemoteAddr() + "  " + ctx.getRequest().getSession().getId());
            System.out.println(ctx.getServletContext().getServerInfo());
            System.out.println(ctx.getRequest().getRemoteUser());*/

            ctx.setCurrentName(giver);
            DataLayerDAO dlGiver = ctx.dataLayersDAO.get(giver);         
            JSONObject jsonObj;
            dlGiver.performances = ctx.getPerformances(); //.. fine if its null
           
            //.. correlation
            if (correlation) {
                if ((dlGiver instanceof BiDAO)) {
                    BiDAO bDAO = (BiDAO) dlGiver;    
                    jsonObj = bDAO.getCorrelationJSON();
                }
                else if (dlGiver instanceof TriDAO) {
                    TriDAO tDAO = (TriDAO) dlGiver;
                    jsonObj = tDAO.getCorrelationJSON();
                }
                else throw new Exception(); //. fail silenyl
            }
            
            //.. return a streaming view of the frequencies present in the data
            else if(frequency) {
                if (!(dlGiver instanceof TriDAO)) throw new Exception(); //.. fail silently
                else{
                    TriDAO tDAO = (TriDAO) dlGiver;
                    jsonObj = tDAO.getFreqDomainJSON();
                }
            } 
            
            else if(prediction) {
                if (!(dlGiver instanceof BiDAO)) {
                    throw new Exception(); //.. fail silently
                } else {
                    Performances p = ctx.getPerformances();
                    jsonObj = dlGiver.getPerformanceJSON(p);   
                }
            }  
          
            //.. print stats about the dataset into the console
            else if(debug){
                if (dlGiver instanceof BiDAO)
                    jsonObj = ((BiDAO) dlGiver).getDebugJSON();
                else if (dlGiver instanceof TriDAO)
                    jsonObj = ((TriDAO) dlGiver).getDebugJSON();
                
                else throw new Exception(); //.. fail silently, but add to this later
            }
            
            //.. THE ONLY EFFECT WAS TO CHANGE THE CTX.CURRENTNAME. A BIT ANNOYING THAT THIS NEEDED TO USE THE BACKEND
            else { //.. STATS
                 jsonObj = dlGiver.getJSON();
            }
           
            return new StreamingResolution("text", new StringReader(jsonObj.toString()));
        }
        catch (Exception e) {
            //System.err.println(e.getMessage());
            //return new ForwardResolution(INDEX);
            e.printStackTrace(); 
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("error", e.getMessage());
            return new StreamingResolution("text", new StringReader(jsonObj.toString()));
        }
    }
    
    
    public Resolution getLatestStream() throws JSONException {
        try{
            ctx.setCurrentName(giver);
            DataLayerDAO dlGiver = ctx.dataLayersDAO.get(giver);
            JSONObject jsonObj;
            jsonObj = dlGiver.getJSON();
            
            return new StreamingResolution("text", new StringReader(jsonObj.toString()));
  
        }
        catch (Exception e) {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("error", e.getMessage());
            return new StreamingResolution("text", new StringReader(jsonObj.toString()));
        }
    }
    
    //.. not in use, I'm pretty sure
    public Resolution splitLayer() throws Exception {
        System.out.println("splitting " +giver );
        try {
            ctx.setCurrent("giver", ctx.dataLayersDAO.get(giver));
        } 
        catch (Exception ex) {
            System.err.println(giver + " does not exist in streams ");
        }
        System.out.println("going to bajs.jsp");
        return new RedirectResolution("/Bajs.action");
    }
    
    /**A List of DataLayers in the entire session
     * @return List
     */
    public List<DataLayer> getDataLayersList() {
        try {
            return ctx.dataLayersDAO.getDataLayers();
        }
        catch(Exception e) {
            SimpleError s = new SimpleError(e.getMessage());
            ctx.getValidationErrors().addGlobalError(s); 
            return null;
        }
    }
    
   
    
}
