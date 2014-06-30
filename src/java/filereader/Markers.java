/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package filereader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import timeseriestufts.evaluation.experiment.Classification;
import timeseriestufts.kth.streams.bi.ChannelSet.Tuple;

/* A light-weight class which, given an index, tells you the class associated with that index.
 * We don't want to save the condition uniquely for each point when 500 consecutive points may have 
 * the same condition. In
 * @author samhincks
 */
public class Markers {
    public String name;
    public ArrayList<Trial> trials;
    public Labels saveLabels;
    private Classification classification = null; //.. set with first get
    
    public Markers(Labels labels) {
        this.saveLabels = labels;
        trials = new ArrayList();
        Label firstLabel = labels.channelLabels.get(0);
        this.name = firstLabel.name;
        
        //.. make a trial for each consecutive block of common condition-values
        Trial currentTrial = new Trial(firstLabel.value, 0);
        for (int i=1; i < labels.channelLabels.size(); i++) {
            Label thisLabel = labels.channelLabels.get(i);
            
            //.. if its a new label, set old ones end, add it, the make a new
            if(!(thisLabel.value.equals(currentTrial.name))) {
                currentTrial.end = i-1;
                trials.add(currentTrial);
                currentTrial = new Trial(thisLabel.value,i);
            }
        }
    }
    
    public Markers(String name, ArrayList<Trial> trials) {
        this.name = name;
        this.trials = trials;
    }
    
    /** Return a class+values pairing by examining each trial
     */
    public Classification getClassification() {
        if (classification!=null) return classification;
        ArrayList<String> values = new ArrayList();
        
        //.. find all unique values
        for (Trial t : trials) {
            boolean contains = false;
            for (String s : values) {
                if (s.equals(t.name)) contains =true;
            }
            if (!contains) values.add(t.name); //.. add if it doesnt exist
            
        }
        return new Classification(values, this.name);
    }
    
    public void printTrials() {
        for (Trial t : trials) {
            t.printTrial();
        }
    }
    
    public static Markers generate(int numSplits, int lengthPerTrial) {
        ArrayList<Trial> trials = new ArrayList();
        String [] conditions = {"a","b","c"};
        int index =0;
        for (int i = 0; i < numSplits; i++) {
            Trial t = new Trial(conditions[i%conditions.length], index);
            index+=lengthPerTrial;
            t.end = index;
            trials.add(t);
        }
        
        return new Markers("name", trials);
    }

    /** Return the majority condition between start and end, and what percentage
     * of that span was that condition
     **/
    public Tuple<String, Double> getConditionBetween(int start, int end) {
        HashMap<String, Integer> intersecting = new HashMap();//.. number of intersecting poitns for each condition
        
        //.. count intersection with each trial
        for (Trial t : trials) {
            int intersection = t.intersects(start, end);
            if (intersection >0) {
                //.. increment tally or create new 
                if (intersecting.containsKey(t.name))
                    intersecting.put(t.name, intersecting.get(t.name) + intersection);
                else
                    intersecting.put(t.name, intersection);
            }
        }
        
        //.. set these as we iterate through hashp
        int total =0;
        int largest =-1;
        String largestCondition = null;
       
        //.. compute total intersection points and find largest
        for (Entry<String, Integer> e : intersecting.entrySet()) {
           String con = e.getKey();
           Integer tally = e.getValue();
           total += tally;
           if (tally > largest){
               largest = tally;
               largestCondition = con;
           }
        }
        
        //.. return the lpercentage the largest had plus
        return new Tuple(largestCondition, (double)largest / (double)total);
    }
    
    public static class Trial { //.. wait, why is this static? just so I can access it form Generate?
        public String name;
        public int start;
        public int end;
        public Trial(String name, int start) {
            this.name = name;
            this.start =start;
        }
        
        public void printTrial() {
            System.out.println("name = " + name + " start = " + start + " end = "+end);
        }
        
        /**Return number of intersecting stamps if there is any intersecting 
         * timepoint between this trial and input start and end. Inclusive both start and end**/
        public int intersects(int start, int end) {
            //.. if input starts before trial and ends before trial, then its completely subsumed
            if (start >= this.start && end <= this.end) return (end -start)+1;
            
            //.. vice versa
            if (this.start >= start && this.end <= end) return (this.end-this.start)+1;
            
            ///.. if this trial starts after input but before end
            if (this.start >= start && this.start <= end) return (end - this.start)+1;
            
            //.. vice versa
            if (start >= this.start && start <= this.end) return (this.end -start);
            
            return 0;
        }
        
        public int getLength() {return end-start;}
    }

    public static void main(String []args ) {
        Trial t = new Trial("bajs", 10); t.end = 30;
        System.out.println(t.intersects(10, 12));
    }
    
}
