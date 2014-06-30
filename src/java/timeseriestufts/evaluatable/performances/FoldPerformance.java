/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeseriestufts.evaluatable.performances;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 *
 * @author samhincks
 */
public class FoldPerformance {
    public double percentCorrect = -100;//. set to percentCorrect
    public int foldNum; 
    public String id;
    public ArrayList <Integer> trainingTrials; //.. eventually place the id of the training here
    public ArrayList <Integer> testingTrials;
  
    //... store amount each condition is guessed
    public Hashtable<Integer, Integer> guessOfEach; //.. holds how many times it guessed each condition 
    public Hashtable<Integer, Integer> answerOfEach; //.. holds how many times each condition appeared
    public Hashtable<Integer, Integer> incorrectOfEach; //.. holds how many times each condition was incorrectly guessed
   
    //.. Deal with complexity that weka's prediction is 0,1,2,3, etc based on class label list
    public Hashtable<String, Integer> map; //.. holds the input condition. Not what Weka transforms it
    public Hashtable<String, Double> pctCorrectOfEach = new Hashtable(); //.. once done, divide guessOfeach 1-incorrectOf each 
   
    public ArrayList<Integer> answers = new ArrayList(); //.. all predictions
    public ArrayList<Integer> predictions = new ArrayList();
    ArrayList<String> allConditions = new ArrayList();
    
    public double correct =0; //.. 
    public int wrong =0;
    public int total =0;
    
    /**
     */
    public FoldPerformance(String desiredConditions, String id, int foldNum, 
            ArrayList<Integer> trainingTrials, ArrayList<Integer> testingTrials) {
        //.. set parameters
        this.id  = id;
        this.foldNum = foldNum;
        this.trainingTrials = trainingTrials;
        this.testingTrials = testingTrials;
        String [] conditions  = desiredConditions.split(",");
        
        //.. initialize arrays
        guessOfEach = new Hashtable();
        map = new Hashtable();
        incorrectOfEach = new Hashtable();
        answerOfEach = new Hashtable();
        
        //.. initiatialize guessofEach, and the mapping of weka condition to our condition
        for (int i =0; i < conditions.length; i++) {
            guessOfEach.put(i, 0);
            incorrectOfEach.put(i,0);
            answerOfEach.put(i,0);
            String thisCondition =conditions[i];
            allConditions.add(thisCondition);
            map.put(thisCondition, i);
        }
    }
    
    public FoldPerformance(String id) {
        this.id = id;
    }    
    
    /*Return a String describing stats for this fold
     */
    public String getStatsString() {
        return "Pct correct for id " + this.id + " is " + getAndCompileStats() +"%";
    }
    //.. return how many we got a right of a particular condition
    public int getNumGuessOf(int condition) {
        int wekaCondition = map.get(condition);
        int numGuesses = guessOfEach.get(wekaCondition);
        return numGuesses;
    }

    //.. When we make a correct prediciton call this function
    public void correctPlusPlus(int guess, int answer) {
        guessOfEach.put(guess, guessOfEach.get(guess) +1);  
        answerOfEach.put(answer, answerOfEach.get(answer) +1);  
        predictions.add(guess);
        //System.out.println("RIGHT!:     total guesses: " + guessOfEach.get(guess)  + " ..... guess: " + guess + " .. answer: " + answer);
        answers.add(answer);
        correct++;
        total++;
    }
    
    //.. when we make a false prediction call this function
    public void wrongPlusPlus(int guess, int answer) {
        guessOfEach.put(guess, guessOfEach.get(guess) +1);          
        answerOfEach.put(answer, answerOfEach.get(answer) +1); 
        
      //  System.out.println("WRONG!:     incorrect guesses: " + numTimesIncorrect  + " ..... guess: " + guess + " .. answer: " + answer);
        incorrectOfEach.put(answer, incorrectOfEach.get(answer)+1);
        predictions.add(guess);
        answers.add(answer);
        wrong++;
        total++;
    }

    //.. for use in statistics package where we aggreagete many classifier performance
    public void addAccuracy(double _pctCorrect) {
        correct += _pctCorrect;
        total++;
    }

    /**Calculate stats based on num correct*/
    public double getAndCompileStats() {
        percentCorrect = (double) correct / (double)total;
    //    setPctCorrectOfEach();
        return percentCorrect;
    }

    void printNumGuessesOfEachCondition() {
        for (int i =0; i < allConditions.size(); i++) {
            String thisCondition = allConditions.get(i);
            int wekaCondition = map.get(thisCondition); 
            int timesGuessed = guessOfEach.get(wekaCondition);
           System.out.println("    Condition " + thisCondition + " guessed : " +timesGuessed +"x" );
        }
    }
    
    void printNumGuessesOfEachCondition(BufferedWriter output) throws IOException {
        for (int i =0; i < allConditions.size(); i++) {
            String thisCondition = allConditions.get(i);
            int wekaCondition = map.get(thisCondition); 
            int timesGuessed = guessOfEach.get(wekaCondition);
            output.write("    Condition " + thisCondition + " guessed : " +timesGuessed +"x" + "\n");
        }
    }

    void printPredictions(BufferedWriter output) throws IOException {
        for (int i =0; i < predictions.size(); i++) {
            int prediction = predictions.get(i);
            int answer = answers.get(i);
            output.write("          Prediction: " + prediction + ". Answer : " +answer +"\n");
        }
    }
    
    //.. Having made all our predictions, go through arrays of right, wrong, guess, and update statistics
    public void setPctCorrectOfEach() {        
        for (int i = 0;  i<  allConditions.size(); i++) {
            //.. extract int version of the condition 
            String stringCondition = allConditions.get(i);
            int intCondition = map.get(stringCondition);
            
            //.. see how many incorrect times it was guessed
            int incorrectOfCondition = incorrectOfEach.get(intCondition);
            int totalOfEach = answerOfEach.get(intCondition);
            
            double pctCorrect = (double) (totalOfEach -incorrectOfCondition) / (double) totalOfEach; 
            pctCorrectOfEach.put(stringCondition, pctCorrect);
        }
    }

    /** Use when we sum a bunch of folds into one comprehensive performance. 
    ** Later */
    public void mergePerformance(FoldPerformance otherPerformance) {        
         this.correct+= otherPerformance.correct;
         this.wrong+= otherPerformance.wrong;
         this.total+= otherPerformance.total;
         
         //.. adjust how many times each condition is guessed
         for (int i = 0;  i<  allConditions.size(); i++) {    
            //.. get mapping
            String stringCondition = allConditions.get(i);
            int intCondition = map.get(stringCondition);
           
            //.. get stats for other performance
            int otherIncorrectOfCondition = otherPerformance.incorrectOfEach.get(intCondition);
            int otherGuessOfEach = otherPerformance.guessOfEach.get(intCondition);
            int otherAnswerOfEach = otherPerformance.answerOfEach.get(intCondition);
            
            //.. get stats for this performance
            int thisIncorrectOfCondition = this.incorrectOfEach.get(intCondition);
            int thisGuessOfEach = this.guessOfEach.get(intCondition);
            int thisAnswerOfEach = this.answerOfEach.get(intCondition);
            
            //.. adapt this performance
            this.incorrectOfEach.put(intCondition, thisIncorrectOfCondition + otherIncorrectOfCondition);
            this.guessOfEach.put(intCondition, thisGuessOfEach + otherGuessOfEach);
            this.answerOfEach.put(intCondition, thisAnswerOfEach + otherAnswerOfEach);            
         }
    }
    
    
    /**This fold performance may be hte sum of some other fold performances in which case you should sunm them all together*/
    public void swallowPerformances(ArrayList<FoldPerformance> otherPerformances) {
        this.allConditions = otherPerformances.get(0).allConditions;
        for (FoldPerformance f : otherPerformances) {
            this.swallowPerformance(f);
        }
    }
    
    /**Add the stats of an individual performances to this*/
    public void swallowPerformance(FoldPerformance p) {
        this.correct += p.correct;
        this.total += p.total;
        this.wrong += p.wrong;
        
        if (p.allConditions.size() != this.allConditions.size()) System.err.println("Trying to swall performance where the condition-sizes do not line up");
    }
    
    /**The expected number of correct predictions is just the number of conditions*/
    public double getExpected() {
        return 1.0/ (double)allConditions.size();
    }
}
