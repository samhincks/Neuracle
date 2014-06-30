/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeseriestufts.evaluation.experiment;

import filereader.Labels;
import java.util.ArrayList;
import java.util.Arrays;
import weka.core.Attribute;
import weka.core.FastVector;

/**
 *
 * @author Sam
 */

//.. Basically just a condition. Set the name, the type, and its values
//.. If subcomparison != null, then the first condition is the way to divide up trials
//... but subcomparison is the trial we're predicting
public class Classification {
    public String name; //.. the name of the class (e.g Flower has values daisy, rose etc)
    public String wekaString; //.. the values of the class listed as a string for an arff file "daisy, flower, rose"
    public ArrayList<String> values; //.. the values of the class
    public String id;
    
    /**Initialize a classification directly and smoothly from a labels object*/
    public  Classification(Labels l) {
        this.name = l.labelName;
        this.wekaString = l.getWekaString();
        this.values = l.getAvailableLabels();
    }
    
    public Classification(ArrayList<String> values, String name){
        this.values = values;
        this.name = name;
        this.wekaString = getDesiredConditions();
    }
    
    /**Return true if the other classification has exactly the same name and conditions*/
    public boolean isEqual(Classification c) {
        if (!(this.name.equals(c.name))) return false; //.. false if names arent equal
        
        //.. other one has all of this ones conditions
        for (String condition : values) {
            if (!c.hasCondition(condition)) return false;//.. and false if other condition misses one
        }
        
        //.. this one has all of other ones conditions
        for (String condition : c.values) {
            if (!this.hasCondition(condition)) return false;//.. and false if other condition misses one
        }
        
        return true; //.. passed the test
    }
    
    /**Return true if we have this class*/
    public boolean hasCondition(String value) {
        if (values.contains(value))
            return true;
        
        return false;
    }

    //.. Return "CONA,CONB"
   public String getDesiredConditions() {
        String retString ="";    
        for (int i =0; i < values.size(); i++) {
            retString += values.get(i);
            if(i!= values.size()-1)
                retString += ",";
        }
        return retString;
    }
    
   /**Return a new classification with the specified value  removed.*/
    public Classification removeClass(String className) throws Exception{
        ArrayList<String> newValues = new ArrayList(); //.. new values, one size smaller
        for (String val : values) {
            if(!val.equals(className)){
                newValues.add(val);
            }
        }
        
        if (newValues.size() != values.size()-1) {
            System.err.println("Did not remove one class from this classification. Probably couldn't find " +className);
        }
        
        return new Classification(newValues, this.name);
    }
    
    /**Remove all instances except these. */
    public Classification removeAllBut(ArrayList<String> toKeep) {
        //.. make case insensitive
        for (int i=0; i< toKeep.size(); i++) {
           toKeep.set(i, toKeep.get(i).toLowerCase());
        }
        
        return new Classification(toKeep, this.name);
    }

    /**Return the id. Name+each value*/
    public String getId() {
        if (id == null) {
            this.id = this.name;
            for (String s : values) {
                this.id+=s;
            }
        }
        return this.id;
    }

    /**Return size of condition with most characters*/
    public int getMaxLabelValLength() {
        int maxLength =-1;
        for (String s : values) {
            if (s.length() > maxLength) {
                maxLength = s.length();
            }
        }
        return maxLength;
    }

    /**Return the index position of the specified condition*/
    public int getIndex(String condition) throws Exception{
        int index =0;
        for (String val : values) {
            if (val.equals(condition)) return index;
            index++;
        }
        throw new Exception(condition + " not found");
    }
    
    /**Return the condition at the specified index*/
    public String getCondition(int index) throws Exception{
        if (index > (values.size() -1)) throw new Exception(index + " out of range of conditions");
        return values.get(index);
                
    }

    public weka.core.Attribute getWekaClassAttr() {
        FastVector fv = new FastVector();
        for (String s: values) {
            fv.addElement(s);
        }
                
        return new weka.core.Attribute(name, fv);

    }

}
