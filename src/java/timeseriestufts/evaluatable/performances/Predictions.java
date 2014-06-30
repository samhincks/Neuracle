/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeseriestufts.evaluatable.performances;

import java.util.ArrayList;
import java.util.Arrays;
import timeseriestufts.evaluatable.Dataset;
import timeseriestufts.evaluatable.TechniqueSet;
import timeseriestufts.evaluation.experiment.Classification;
import timeseriestufts.kth.streams.bi.ChannelSet.Tuple;
import timeseriestufts.kth.streams.tri.Experiment;

/** A series of predictions made on one experiment.
 * @author samhincks
 */
public class Predictions {
    public Dataset dataset;
    public TechniqueSet techniqueSet;
    public Classification classification;
    public ArrayList<Prediction> predictions;
    private String id;
    
    public Predictions(Dataset dataset, TechniqueSet ts, Classification c) throws Exception{
        this.id =dataset.getId() + ts.getId() + c.id;
        this.dataset =dataset;
        this.techniqueSet = ts;
        this.classification = c;
        predictions = new ArrayList();
    }
    
    /**The pct correct*/
    public double getPctCorrect() {
        if (predictions == null) return 0;
        double total = predictions.size();
        double correct =0;
        for (Prediction p : predictions) {
            if (p.isCorrect()) correct++;
        }
        return correct/total;
    }
    
    /**Make a Prediction object out of the parts of a Weka prediction: 
     * guess, answer, and array of confidences
     */
    public void addPrediction(int guess, int answer, double [] confidences, int instanceIndex) {
        //.. retrieve string version of guess. note classification.values must match guess indexes
        String guessS = classification.values.get(guess);
        String answerS = classification.values.get(answer);
        
        //.. retrieve confidence scores
        double confidence = confidences[guess];
        double secondLargest = getSecondLargest(confidences);
        double pctGreater = confidence / secondLargest;
        
        //.. make new prediction and add
        Prediction prediction = new Prediction(guessS, answerS, confidence, pctGreater, instanceIndex);
        predictions.add(prediction);
    }
    
    /**Use this method to add a prediction that is made on n instance that is not 
     * 100% labeled as one instance**/
    public void addPrediction(int guess, int answer, double conditionPercentage, double[] confidences, int instanceIndex) {
        //.. retrieve string version of guess. note classification.values must match guess indexes
        String guessS = classification.values.get(guess);
        String answerS = classification.values.get(answer);

        //.. retrieve confidence scores
        double confidence = confidences[guess];
        double secondLargest = getSecondLargest(confidences);
        double pctGreater = confidence / secondLargest;

        //.. make new prediction and add
        Prediction prediction = new Prediction(guessS, answerS, conditionPercentage, confidence, pctGreater, instanceIndex);
        predictions.add(prediction);
    }
    
    public void addPrediction(int guess, int answer, int instanceIndex) {
        //.. retrieve string version of guess. note classification.values must match guess indexes
        String guessS = classification.values.get(guess);
        String answerS = classification.values.get(answer);
        
        //.. make new prediction and add
        Prediction prediction = new Prediction(guessS, answerS, instanceIndex);
        predictions.add(prediction);
    }
    
    public void addPrediction(Prediction p) {
        predictions.add(p);
    }
    /**Return the second largest double in the array**/
    private double getSecondLargest(double [] confidences) {
        Arrays.sort(confidences);
        return confidences[1];
    }
    
    public String getId() {return this.id;}
    
    /**Fuse another set of predictions with this one. NOTE may be an error if another set of 
     * predictions is evaluated over different classification?
     */
    public void swallowPredictions(ArrayList<Predictions> ps) {
        for(Predictions p : ps){
            predictions.addAll(p.predictions);
        }
    }
    
    /**Return pct correct you'd expect just by chance*/
    public double getExpected() {
        return 1.0 / classification.values.size();
    }
    
    /**Return new predictions object, with all predictions about some class excluded**/
    public Predictions getPredictionsWhereAnswerWasNot(String exclude) throws Exception{
        Predictions retPredictions = new Predictions(dataset, techniqueSet, classification.removeClass(exclude));
        
        for (Prediction p : predictions) {
            if (!(p.answer.equals(exclude))) 
                retPredictions.addPrediction(p);
        }
        return retPredictions;
    }

    public void printPredictions() {
        for (Prediction p : predictions) {
            p.printPrediction();
        }
    }
    
    public int getNumCorrect() {
        int correct =0;
        for (Prediction p : predictions) {
            if (p.isCorrect()) correct++;
        }
        return correct;
    }
    
    public int getNumGuesses() {
        return predictions.size();
    }

    /**Return the total number of occurences (not guesses) of this condition.**/
    public int getNumInstancesOf(String condition) {
        int total = 0;
        for (Prediction p : predictions) {
            if (p.answer.equals(condition))
                total++;
        }
        return total;
    }

    public Tuple<Double, Double> getCorrectAndTotalWithKConsecutive(int numConsecutive) {
        int sameInRow = 1;
        String lastGuess = predictions.get(0).prediction;
        double guesses=0;
        double correct =0;
        for (Prediction p : predictions) {
            
            //.. if its a consecutive prediction
            if (p.prediction.equals(lastGuess) || numConsecutive < 2) {
                
                //.. register guess if we've had this many in a row
                if (sameInRow >= numConsecutive) {
                    guesses++;
                    if(p.isCorrect()) correct++;
                }
                
                //.. regardless, increment how many we've seen in a row
                sameInRow++;

            }
            else
                sameInRow =1;
            
            lastGuess = p.prediction;
        }
        
        return new Tuple(correct,guesses);
    }

}
