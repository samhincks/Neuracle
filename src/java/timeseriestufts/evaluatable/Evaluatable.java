/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeseriestufts.evaluatable;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.Entry;
import timeseriestufts.evaluatable.performances.FoldPerformance;
import timeseriestufts.evaluatable.performances.Prediction;
import timeseriestufts.evaluatable.performances.Predictions;
import timeseriestufts.kth.streams.bi.ChannelSet.Tuple;

/**Extend this class if it is a technique or dataset for which you might ask:
 * how many correct and incorrect predictions has this generated over multiple runs?
 * @author samhincks
 */
public abstract class Evaluatable {
   protected String id;
   
   public ArrayList<Predictions> predictions; 
   
   public void addPredictions(Predictions p ) {
       if (predictions == null) predictions = new ArrayList();
       predictions.add(p);
   }

   
   public void printPredictions() {
       System.out.println("Printing " + predictions.size());
       for (Predictions p : predictions) {
           p.printPredictions();
       }
   }
   
   /**Return the stats as a String*/
   public String getStatsString() { 
       String retString ="";
       for(Predictions p : predictions) {
           retString += p.getPctCorrect() +"\n";
       }
       return retString;
   }
   
   /**Return the average of the most recent num performances
    **/
   public double getMostRecentAverage() {
       if (predictions == null) return 0;
       return this.predictions.get(this.predictions.size()-1).getPctCorrect();
   }
   
   /**Returns the average of all the performances collected*/
   public double getAverage() {
       if (predictions == null) return -1;
       
       double totalCorrect = 0;
       double totalGuesses =0;
       for (Predictions p : predictions) {
          totalCorrect += p.getNumCorrect();
          totalGuesses += p.getNumGuesses();
       }
       
       return totalCorrect / totalGuesses;
   }
   
   public double getAverageWhereAnswerWasNot(String exclude) throws Exception{
       if (predictions == null) {
           return -1;
       }
       double totalGuesses=0;
       double totalCorrect =0;
       
       //.. sum up a sample of each prediction object
       for (Predictions p : predictions) {
           Predictions subP = p.getPredictionsWhereAnswerWasNot(exclude);
           totalGuesses += subP.getNumGuesses();
           totalCorrect += subP.getNumCorrect();
       }

       return totalCorrect / totalGuesses;
   }
   /*Get performances grouped together grouped by id. Return an array that holds one performance
    which sums each of these underlying id-common performances
    * IMPORTANT: the FoldPerformanes we are interested in grouping must have a common id*/
    public ArrayList<Predictions> getGroupedPerformances() throws Exception{
        //.. group all common performances into a hash
        Hashtable<String, ArrayList<Predictions>> hashedPerformances = new Hashtable();
        for (Predictions p : predictions) {
            //.. add  new array if first occurrence
            if (hashedPerformances.get(p.getId()) == null) hashedPerformances.put(p.getId(), new ArrayList()); 
            hashedPerformances.get(p.getId()).add(p);
        }
        
        ArrayList<Predictions> retPerformances = new ArrayList();
        
        //.. group the performances with a common id together
        for (ArrayList<Predictions> hashedP : hashedPerformances.values()) {
            Predictions first = hashedP.get(0);
            Predictions mergedPredictions = new Predictions(first.dataset,first.techniqueSet, first.classification);
            mergedPredictions.swallowPredictions(hashedP);
            retPerformances.add(mergedPredictions);
        }
        
        return retPerformances;
   }
   /** Return the amount of correct predictions you'd expect solely by chance
    */
   public double getExpected() {
       if (predictions == null) return -1;

       double total = 0;
       for (Predictions p : predictions) {
            total += p.getExpected();
       }
       return total / predictions.size();

   }
   
   public String getId() {return id;}
 
   public void setId(String id)  {
       this.id = id;
   }

    /**
     * Return a hash showing how much each condition appeared as an answer*
     */
    public HashMap<String, Integer> getNumInstancesOfEach(String classificationName) {
        HashMap<String, Integer> retMap = new HashMap();
        for (Predictions p : predictions) {
            if (p.classification.name.equals(classificationName)) {
                for (String condition : p.classification.values) {
                    int occurrences = p.getNumInstancesOf(condition);
                    if (retMap.containsKey(condition)) 
                        retMap.put(condition, retMap.get(condition) + occurrences);
                    else //.. initialize array
                        retMap.put(condition, occurrences);
                }
            }
        }
        return retMap;
    }
   
    /**Print how many times each condition appears*/
    public void printNumInstancesOfEach(String classificationName) {
        HashMap<String, Integer> occurrences = this.getNumInstancesOfEach(classificationName);
        for(Entry<String, Integer> e : occurrences.entrySet()) {
            System.out.print(e.getKey() + " : " + e.getValue() +",");
        }
        System.out.println("");

    }
    
    /**Return the average when prediction is only counted if the classifier has 
     guessed numConsecutive in a row. Assumption: for this to be an interesting statistic,
     * the instances its predicting (and the array of predictions) must be lined up truly in time
     **/
    public Tuple<Double,Double> getAverageAndTotalWithKConsecutive(int numConsecutive) {
        double correct =0;
        double total = 0;
        for (Predictions p : predictions) {
            Tuple<Double,Double> corAndTotal = p.getCorrectAndTotalWithKConsecutive(numConsecutive);
            correct+= corAndTotal.x;
            total += corAndTotal.y;
        }
        return new Tuple((correct/total),total);
    }

    public void printInfo() {
        System.out.println("ID: " + id + " . ");
    }
}

