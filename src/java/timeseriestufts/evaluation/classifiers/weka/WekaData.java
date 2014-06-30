/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package timeseriestufts.evaluation.classifiers.weka;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.core.Instances;
import weka.filters.supervised.attribute.AttributeSelection; //.. Note! import this, not weka.attributeSelection!!
import weka.filters.*;

/**
 * Takes an arff file, and converts it into weka instances format.
 * You can also apply a filter to this data.
 */
public class WekaData {
    
    public String fileName;
    public Instances data;
    public Instances initialData;
    public boolean filtered = false; //.. by default, no filter has occurred
    public String filtersApplied =""; //.. keep appending filters applied here
    public ArrayList<String> bestAttributes;
    public AttributeSelection filterA;
    public AttributeSelection filterB;
    public int numAttributes;
    
    /***On a rainy sunday, CHANGE this. It should not read from file it should directly
     convert to a Weka Instance for VASTLY improved performance and more online
     reliability**/
    public WekaData(String fileName) {
        //.. open the file, and let Weka read it into appropriate class
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            data = new Instances(reader);
            reader.close();

            //.. last attribute is class
            data.setClassIndex(data.numAttributes() -1);
            numAttributes = data.numAttributes() -1;
            initialData = data;
      } catch(IOException e) {e.printStackTrace();};
    }

    public void InfoGain(int numAttributes) throws Exception {
        //.. Selection the [numAttributes] attributes with best Info Gain
         filterA = new AttributeSelection();
         InfoGainAttributeEval eval = new InfoGainAttributeEval();

         //.. use ranker search method, selecting [numAttributes] best
         Ranker search = new Ranker();
         search.setNumToSelect(numAttributes);
         filterA.setEvaluator(eval);
         filterA.setSearch(search);
         filterA.setInputFormat(data);
         data = Filter.useFilter(data, filterA);

         //.. remember what's been done
         filtered = true;
         filtersApplied = filtersApplied +"-[InfoGain: "+numAttributes+" ]";

         //.. store the best attributes
         bestAttributes = new ArrayList();
         Enumeration attributes = data.enumerateAttributes();
         while(attributes.hasMoreElements()) {
            String attr = attributes.nextElement().toString();
            String [] split = attr.split(" ");
            String name = split[1];
            bestAttributes.add(name);
         }

    }

    public void cfsSubset() throws Exception {
         //.. get attributes depending on their classification accuracy
         filterB = new AttributeSelection();  // package weka.filters.supervised.attribute!
         CfsSubsetEval eval = new CfsSubsetEval();

         //.. keep comparing classification accuracy with, and without
         GreedyStepwise search = new GreedyStepwise();
         search.setSearchBackwards(true);
         filterB.setEvaluator(eval);
         filterB.setSearch(search);
         filterB.setInputFormat(data);

         //.. and apply it to dataset
         data = Filter.useFilter(data, filterB);

         //.. remember what's been done
         filtered = true;
         filtersApplied = filtersApplied +"-[CfsSubset-GreedyStepWise]";

         //.. store the best attributes
         bestAttributes = new ArrayList();
         Enumeration attributes = data.enumerateAttributes();
         while(attributes.hasMoreElements()) {
            String attr = attributes.nextElement().toString();
            String [] split = attr.split(" ");
            String name = split[1];
            bestAttributes.add(name);
         }
    }

    public void printBestAttributes() {
        Iterator itr = bestAttributes.iterator();
        System.out.println("-----BEST ATTRIBUTES----");
        while (itr.hasNext()) {
            System.out.println(itr.next());
        }

    }

    public void batch(AttributeSelection filter) throws Exception {
        data = Filter.useFilter(data, filter);
    }




}
