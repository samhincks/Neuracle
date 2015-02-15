/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeseriestufts.evaluatable.performances;

/**
 *
 * @author samhincks
 */
public class Prediction {
    public String prediction; 
    public String answer; 
    public double confidence;
    public double pctHigher=0;//.. how many pct greater than the second prediction
    public int instanceIndex;
    public double conditionPercentage =1; //.. what percent of the majority condition is this instance
    
    //.. SHOULDNT THERE BE A PREDICTION THAT DOESNT HAVE A CONFIDENCE OR AN ANSWER??
    
    public Prediction(String prediction, String answer, double confidence, double pctHigher, int instanceIndex) {
        this.prediction = prediction;
        this.answer = answer;
        this.confidence = confidence;
        this.pctHigher = pctHigher;
        this.instanceIndex = instanceIndex;
    }
    
    /**For predictions on an instance that is not 100% the same class**/
    public Prediction(String prediction, String answer, double conditionPercentage, double confidence, double pctHigher, int instanceIndex) {
        this.prediction = prediction;
        this.answer = answer;
        this.confidence = confidence;
        this.pctHigher = pctHigher;
        this.instanceIndex = instanceIndex;
        this.conditionPercentage = conditionPercentage;
    }
    public Prediction(String prediction, String answer, int instanceIndex) {
        this.prediction = prediction;
        this.answer = answer;
        this.instanceIndex = instanceIndex;
    }

    boolean isCorrect() {
        if (this.prediction.equals(this.answer)) return true;
        return false;
    }

    void printPrediction() {
        System.out.println("Prediction = " + prediction + " Answer = " + answer +"*"+conditionPercentage + " conf = " + pctHigher);
    }
    @Override
    public String toString() {
        return prediction + ";"+confidence+";"+pctHigher;
    }
}
