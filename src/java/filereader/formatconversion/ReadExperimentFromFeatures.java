/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package filereader.formatconversion;

import filereader.Label;
import filereader.Labels;
import java.io.BufferedReader;
import java.util.ArrayList;
import timeseriestufts.evaluation.experiment.Classification;
import timeseriestufts.evaluation.featureextraction.Attribute;
import timeseriestufts.evaluation.featureextraction.Attributes;
import timeseriestufts.evaluation.featureextraction.NumericAttribute;
import timeseriestufts.evaluation.featureextraction.PresetAttribute;
import timeseriestufts.kth.streams.bi.ChannelSet;
import timeseriestufts.kth.streams.bi.Instance;
import timeseriestufts.kth.streams.tri.Experiment;
import timeseriestufts.kth.streams.uni.Channel;

/**
 *
 * @author samhincks
 */
public class ReadExperimentFromFeatures {
    BufferedReader br;
    int CONDINDEX;
    /*** First line name of attributes. Last column is name of condition*/
    public ReadExperimentFromFeatures(String filename) {
        try {
          br = new BufferedReader(new java.io.FileReader(filename));
        }
        catch(Exception e) {e.printStackTrace();}
        
    }
    
    public Experiment getExperiment() throws Exception{
        String line = br.readLine();
        String [] attributeNames = line.split(",");
        CONDINDEX = attributeNames.length-1;
        String conditionName = attributeNames[CONDINDEX];
        Labels labels = new Labels(conditionName);
        ArrayList<Instance> instances = new ArrayList();
        
        while ((line =br.readLine()) != null) {
            String [] attributes = line.split(",");
            //.. Keep track of unique label names, so that we can use this class to get the unique ones
            String conditionVal = attributes[CONDINDEX];
            labels.addLabel(new Label(conditionName, conditionVal, 0)); //.. hacky but does the trick
            
            Attributes attrs = new Attributes();
            
            
            for (int i =0; i < CONDINDEX; i++) {
                PresetAttribute attribute = new PresetAttribute();
                attribute.name = attributeNames[i];
                try {
                    attribute.numValue = Double.parseDouble(attributes[i]);
                }
                catch (Exception e) { 
                    if (attributes[i].contains("+")){
                        attribute.numValue= (Double.parseDouble(attributes[i].split("\\+")[0]));
                    }
                } 
                attrs.addAttribute(attribute);
            }
            Instance instance = new Instance(attrs, conditionVal);
            instance.extractAttributes(null);
            instances.add(instance);
        }
        Experiment e = new Experiment(br.toString(), new Classification(labels), instances, Channel.HitachiRPS); 
        
        return e;
    }
    
}
