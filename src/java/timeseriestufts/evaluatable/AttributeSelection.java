/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeseriestufts.evaluatable;

import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.core.Instances;
import weka.filters.Filter;      
            
/**   
 *  
 * @author samhincks
 */  
public class AttributeSelection extends Technique{
    public int numAttributes =-1;//.. how many attribtues to leave
    public float percentToKeep = -1;
    //.. these must have been set when applied to training. This approach assumes that 
    private weka.filters.supervised.attribute.AttributeSelection infoFilter; //.. set when applied to training
    private weka.filters.supervised.attribute.AttributeSelection cfsFilter; //.. set when applied to training

    
    public static enum ASType {cfs, info, None};
    public ASType asType;
    
    /**(cfs,-1) -> Only CFS_Subset (very computationally expensive)
     * (cfs, 20) -> First use infogain down to 20 attribtues, then use cfs_subset
     * (info, 20) -> only use info gain down to 20 attributes
     */
    public AttributeSelection(ASType asType, int numAttributes) {
        super.id = asType.name()+numAttributes;
        this.numAttributes = numAttributes;
        this.asType =asType;
    }
    public AttributeSelection(String asType, int numAttributes) {
        super.id = asType+numAttributes;
        this.numAttributes = numAttributes;
        this.asType =ASType.valueOf(asType);
    }
    
    public AttributeSelection(ASType asType, float percentToKeep) {
        super.id = asType.name()+percentToKeep;
        this.percentToKeep = percentToKeep;
        this.numAttributes = 0;//.. set this later
        this.asType =asType;
    }
    public boolean doInfoGain() { return (!(asType == ASType.None) && numAttributes != -1); }; 
    public boolean doCFSSubset() {return asType == ASType.cfs; };
    
    
    /**Apply a computationally cheap algorithm which returns the n attributes with 
     most predictive value **/
    public Instances infoGain(weka.core.Instances instances) throws Exception{
        //.. Selection the [numAttributes] attributes with best Info Gain
        infoFilter = new weka.filters.supervised.attribute.AttributeSelection();
        InfoGainAttributeEval eval = new InfoGainAttributeEval();
        if(percentToKeep != -1 ) numAttributes = (int) ((double)instances.numAttributes() * percentToKeep);
        
        //.. use ranker search method, selecting [numAttributes] best
        Ranker search = new Ranker();
        search.setNumToSelect(numAttributes);
        infoFilter.setEvaluator(eval);
        infoFilter.setSearch(search);
        infoFilter.setInputFormat(instances);
        instances = Filter.useFilter(instances, infoFilter);
        
        return instances;
    }
    
    /**Apply a more computationally expensive algorithm which returns the subset of attribtues
     with most predictive value and least redundancy**/
    public weka.core.Instances cfsSubset(weka.core.Instances instances) throws Exception {
        //.. get attributes depending on their classification accuracy
         cfsFilter  = new weka.filters.supervised.attribute.AttributeSelection();  // package weka.filters.supervised.attribute!
        CfsSubsetEval eval = new CfsSubsetEval();

        //.. keep comparing classification accuracy with, and without
        GreedyStepwise search = new GreedyStepwise();
        search.setSearchBackwards(true);
        cfsFilter.setEvaluator(eval);
        cfsFilter.setSearch(search);
        cfsFilter.setInputFormat(instances);

        //.. and apply it to dataset
        instances = Filter.useFilter(instances, cfsFilter);
        return instances;
    }
    
    /**Get most recently applied cfs filter. Be careful since it might have been reset to
     a new instance**/
    public weka.filters.supervised.attribute.AttributeSelection getMostRecentCFSFilter() throws Exception{
        if (cfsFilter == null) throw new Exception("AS algo has not yet been applied");
        return cfsFilter;
    }
    
    /**Get most recently applied info filter. Be careful since it might have been reset to
     a new instance**/
    public weka.filters.supervised.attribute.AttributeSelection getMostRecentInfoFilter() throws Exception{
        if (infoFilter == null) throw new Exception("AS algo has not yet been applied");
        return infoFilter;
    }      
}
      