

package dao;

import dao.datalayers.BiDAO;
import dao.datalayers.DataLayerDAO;
import dao.datalayers.DataLayersDAO;
import dao.datalayers.TriDAO;
import dao.techniques.TechniqueDAO;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import org.json.JSONArray;
import org.json.JSONObject;
import stripes.ext.ThisActionBeanContext;
import timeseriestufts.evaluatable.FeatureSet;
import timeseriestufts.evaluatable.Transformation;
import timeseriestufts.kth.streams.DataLayer;
import timeseriestufts.kth.streams.bi.BidimensionalLayer;
import timeseriestufts.kth.streams.bi.ChannelSet;
import timeseriestufts.kth.streams.bi.Instance;
import timeseriestufts.kth.streams.quad.MultiExperiment;
import timeseriestufts.kth.streams.tri.ChannelSetSet;
import timeseriestufts.kth.streams.tri.Experiment;
import timeseriestufts.kth.streams.tri.TridimensionalLayer;

/**An abstract class; the basic flow for one of its implementing children is that
 * you define a set of commands, place them in a commands upon initialization, and
 * then implement them with the execute method. 
 * @author samhincks
 */
public abstract class Parser {
    public Hashtable<String, Command> commands;
    public ThisActionBeanContext ctx;
    public DataLayer currentDataLayer;
    
    public Parser (ThisActionBeanContext ctx) {
        this.ctx = ctx;
    }
    
    public abstract JSONObject execute(String command, String[] parameters, 
            DataLayer currentDataLayer, TechniqueDAO techDAO) throws Exception; 
    
    
    /**
     * For multi-selection operations, it is most often a pain in the butt to
     * define a new operation to operate on the higher dimension object rather
     * than repeatedly on the lower dimension object. What we want is to add the
     * individual layers to the context, so we need to break the temporary 4d
     * abstraction right after anyway.
     */
    protected ArrayList<Experiment> getExperiments(boolean moan) throws Exception {
        if (!(currentDataLayer instanceof Experiment || currentDataLayer instanceof MultiExperiment)) {
            if (moan) throw new Exception("The selected dataset must be a 3D-Experiment");
            else return null;
        }
        ArrayList<Experiment> retExperiments = new ArrayList();

        //.. if multi experiment then return each experiment individually
        if (currentDataLayer instanceof MultiExperiment) {
            MultiExperiment multi = (MultiExperiment) currentDataLayer;
            for (TridimensionalLayer t : multi.piles) {
                retExperiments.add((Experiment) t);
            }
        }

        //.. else return an array of size 1
        if (currentDataLayer instanceof Experiment) {
            retExperiments.add((Experiment) currentDataLayer);
        }

        return retExperiments;
    }
    
    /**
     * If we know that currentDataLayer is either a Channelset or a
     * ChannelSetSet return an arraylist with each channelset.
     */
    protected ArrayList<ChannelSet> getChanSets(boolean moan) throws Exception {
        if (!(currentDataLayer instanceof ChannelSet) && !(currentDataLayer instanceof ChannelSetSet)) {
            if (moan) throw new Exception("The selected dataset " + currentDataLayer.id +" must be a Channel Set or ChannelSetSet");
            else return null;
        }

        //.. add all chansets to ChanSets
        ArrayList<ChannelSet> chanSets = new ArrayList();
        if (currentDataLayer instanceof ChannelSetSet) {
            ChannelSetSet css = (ChannelSetSet) currentDataLayer;
            for (BidimensionalLayer bd : css.matrixes) {
                chanSets.add((ChannelSet) bd);
            }
        } else {
            chanSets.add((ChannelSet) currentDataLayer);
        }
        return chanSets;
    }

    /**Get all channelsets irrespective of whether or not they're selected
     */
    protected ArrayList<ChannelSet> getAllChanSets() throws Exception {
        return ctx.getDataLayers().getAllChannelSets();
    }
    
     /**Get all channelsets irrespective of whether or not they're selected
     */
    protected ArrayList<Experiment> getAllExperiments() throws Exception {
        return ctx.getDataLayers().getAllExperiments();
    }
    
    protected Experiment getExperiment(ChannelSet cs, String labelName) throws Exception {
        Experiment e = cs.splitByLabel(labelName);
        e.setId(cs.getId() + labelName);
        e.setParent(cs.getId()); //.. set parent to what we derived it from
        e.test = cs.test; //.. part of the tutorial

        //.. EVERYTIME WE ADD AN EXPERIMENT, WE WANT TO UPDATE THE FEATURESET (nice idea thinking this should be in a different thread, but orka do thread safety
        for (TechniqueDAO td : ctx.getTechniques().getTechniques()) {
            if (td.technique instanceof FeatureSet) {
                FeatureSet fs = (FeatureSet) td.technique;
                e.extractAttributes(fs);
                try {
                    fs.addExperimentToInfogain(e.getWekaInstances(false));
                } catch (Exception ex) {
                    //.. This is fine. A valiant effort to add more information to the featureset
                    //.. if the extra datalayer pertained to the same experiment as the first..

                }
            }
        }
        return e;
    }
    
    protected String makeExperiment(ChannelSet cs, String labelName) throws Exception {
        Experiment e = cs.splitByLabel(labelName);
        e.setId(cs.getId() + labelName);
        e.setParent(cs.getId()); //.. set parent to what we derived it from
        e.test = cs.test; //.. part of the tutorial
        
        //.. make a new data access object, and add it to our stream
        TriDAO pDAO = new TriDAO(e);
        ctx.dataLayersDAO.addStream(e.id, pDAO);
        
        //.. EVERYTIME WE ADD AN EXPERIMENT, WE WANT TO UPDATE THE FEATURESET (nice idea thinking this should be in a different thread, but orka do thread safety
        for (TechniqueDAO td  : ctx.getTechniques().getTechniques()) {
            if (td.technique instanceof FeatureSet) {
                FeatureSet fs = (FeatureSet) td.technique;
                e.extractAttributes(fs);
                try{
                    fs.addExperimentToInfogain(e.getWekaInstances(false));
                }
                catch (Exception ex){
                    //.. This is fine. A valiant effort to add more information to the featureset
                    //.. if the extra datalayer pertained to the same experiment as the first..
                          
                 }
            }
        }
       
        //.. Generate a console message which includes num instance, num of each condition, and color
        String retString = "Created : " + e.getId() + " with " + e.matrixes.size() + " instances"
                + "::" + getColorsMessage(e);
        return retString;
    }
      
    protected String makeChannelSets(ChannelSet cs, String labelName) throws Exception {
        ArrayList<ChannelSet> channelSets  = cs.partitionByLabel(labelName);
        String retString ="";
        int index =0;
        for (ChannelSet c : channelSets ){ 
            c.setId(cs.getId() + labelName + index);
            c.setParent(cs.getId()); //.. set parent to what we derived it from
            c.test = cs.test;
            //.. make a new data access object, and add it to our stream
            BiDAO bDAO = new BiDAO(c);
            ctx.dataLayersDAO.addStream(c.id, bDAO);

            //.. Generate a console message which includes num instance, num of each condition, and color
            retString += "Created : " + c.getId();
            index++;
        }
        return retString;
    }
    
    protected String getColorsMessage(Experiment e) {
        String retString ="";
        String[] colors = new String[]{"blue", "orange", "green", "red", "purple", "yellow","black"};
        int index = 0;
        String lastCondition = "";
        ArrayList<String> added = new ArrayList();
        
        //.. Iterate through all instances, counting each occurrence of a new condition
        //... and assigning it a color based on the order 
        for (Instance i : e.matrixes) {
            if ((!i.condition.equals(lastCondition))) {
                if (!added.contains(i.condition)){
                    retString += i.condition + " = " + colors[index %colors.length] + ", ";
                    index++;
                    added.add(i.condition);
                }
                lastCondition = i.condition;
            }
        }
        return retString;
    }
    
    /**Return true if the port is available
     * @param port   
     * @return 
     */
    public static boolean available(int port) {
        try (Socket ignored = new Socket("localhost", port)) {
            return false;
        } catch (IOException ignored) {
            return true;
        }
    }
    
    /**Return all implemented commands
     * @return
     * @throws Exception 
     */
    public ArrayList<JSONObject> getCommands() throws Exception{
        ArrayList<JSONObject> rObj = new ArrayList(); 
       
        for (Command c : commands.values()) {
            rObj.add(c.getJSONObject(ctx.getTutorial(), ctx.getSelfCalibrate()));
        }
        return rObj;   
    }
    
    /**Return documentation, nicely structured for all commands
     * @param param
     * @return 
     */
    public String getDocumentation(String param) {
        String retString ="";
        for (Command c : commands.values()){
            if (param == null || c.id.startsWith(param)){
                retString += c.id +"::";
                retString += "     " + c.documentation +"::";
                if(c.parameters !=null)
                    retString += "     " + c.parameters+"";
                if (c.debug != null)
                    retString += "::     " + c.debug;
                retString += ";;";
            }
        }
        return retString;
    }
    
    /**Add a new file, and select it**/
    public void addFile(File fileName) throws Exception{ 
        BiDAO mDAO = new BiDAO();
        mDAO.make(fileName, ctx.getFileReadSampling());
        //.. if this is as yet uninitizliaed
        if (ctx.dataLayersDAO == null) {
            ctx.dataLayersDAO = new DataLayersDAO();
        }

        //.. Add to the persistent context, and save by id 
        ctx.dataLayersDAO.addStream(mDAO.getId(), mDAO);
        ctx.setCurrentName(mDAO.getId());
    }
    /**
     * Add a new file, given an inputstream and select it* */
    public void addFile(InputStream is, String name) throws Exception {
        BiDAO mDAO = new BiDAO();
        mDAO.make(is, ctx.getFileReadSampling(), name);
        //.. if this is as yet uninitizliaed
        if (ctx.dataLayersDAO == null) {
            ctx.dataLayersDAO = new DataLayersDAO();
        }

        //.. Add to the persistent context, and save by id 
        ctx.dataLayersDAO.addStream(mDAO.getId(), mDAO);
        ctx.setCurrentName(mDAO.getId());
    }
}
