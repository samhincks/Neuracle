/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dao;

import dao.datalayers.TriDAO;
import dao.techniques.TechniqueDAO;
import java.util.ArrayList;
import java.util.Hashtable;
import org.json.JSONObject;
import stripes.ext.ThisActionBeanContext;
import timeseriestufts.kth.streams.DataLayer;
import timeseriestufts.kth.streams.bi.BidimensionalLayer;
import timeseriestufts.kth.streams.bi.ChannelSet;
import timeseriestufts.kth.streams.bi.Instance;
import timeseriestufts.kth.streams.quad.MultiExperiment;
import timeseriestufts.kth.streams.tri.ChannelSetSet;
import timeseriestufts.kth.streams.tri.Experiment;
import timeseriestufts.kth.streams.tri.TridimensionalLayer;

/**
 *
 * @author samhincks
 */
public abstract class Parser {
    public Hashtable<String, Command> commands;
    public ThisActionBeanContext ctx;
    public DataLayer currentDataLayer;
    public TechniqueDAO techDAO;
    
    public Parser () {
        
    }
    
    public abstract JSONObject execute(String command, String[] parameters, ThisActionBeanContext ctx,
            DataLayer currentDataLayer, TechniqueDAO techDAO) throws Exception; 
    
    /**
     * For multi-selection operations, it is most often a pain in the butt to
     * define a new operation to operate on the higher dimension object rather
     * than repeatedly on the lower dimension object. What we want is to add the
     * individual layers to the context, so we need to break the temporary 4d
     * abstraction right after anyway.
     */
    protected ArrayList<Experiment> getExperiments() throws Exception {
        if (!(currentDataLayer instanceof Experiment || currentDataLayer instanceof MultiExperiment)) {
            throw new Exception("The selected dataset must be a 3D-Experiment");
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
    protected ArrayList<ChannelSet> getChanSets() throws Exception {
        if (!(currentDataLayer instanceof ChannelSet) && !(currentDataLayer instanceof ChannelSetSet)) {
            throw new Exception("The selected dataset " + currentDataLayer.id +" must be a Channel Set or ChannelSetSet");
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

    protected String makeExperiment(ChannelSet cs, String labelName) throws Exception {
        Experiment e = cs.splitByLabel(labelName);
        e.setId(cs.getId() + labelName);
        e.setParent(cs.getId()); //.. set parent to what we derived it from

        //.. make a new data access object, and add it to our stream
        TriDAO pDAO = new TriDAO(e);
        ctx.dataLayersDAO.addStream(e.id, pDAO);
       
        //.. Generate a console message which includes num instance, num of each condition, and color
        String retString = "Created : " + e.getId() + " with " + e.matrixes.size() + " instances"
                + "::" + getColorsMessage(e);
        return retString;
        
    }
    
    protected String getColorsMessage(Experiment e) {
        String retString ="";
        String[] colors = new String[]{"blue", "orange", "green", "red", "purple"};
        int index = 0;
        String lastCondition = "";
        ArrayList<String> added = new ArrayList();
        
        //.. Iterate through all instances, counting each occurrence of a new condition
        //... and assigning it a color based on the order 
        for (Instance i : e.matrixes) {
            if ((!i.condition.equals(lastCondition))) {
                if (!added.contains(i.condition)){
                    retString += i.condition + " = " + colors[index] + ", ";
                    index++;
                    added.add(i.condition);
                }
                lastCondition = i.condition;
            }
        }
        return retString;
    }
    
}