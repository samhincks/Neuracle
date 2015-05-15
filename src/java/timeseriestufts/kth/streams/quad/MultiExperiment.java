/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeseriestufts.kth.streams.quad;

import java.util.ArrayList;
import timeseriestufts.evaluatable.TechniqueSet;
import timeseriestufts.evaluatable.WekaClassifier;
import timeseriestufts.evaluatable.performances.FoldPerformance;
import timeseriestufts.evaluatable.performances.Predictions;
import timeseriestufts.evaluation.crossvalidation.Fold;
import timeseriestufts.kth.streams.bi.Instance;
import timeseriestufts.kth.streams.tri.Experiment;
import timeseriestufts.kth.streams.tri.TridimensionalLayer;

/**
 *
 * @author samhincks
 */
public class MultiExperiment extends QuaddimensionalLayer{
    
    public MultiExperiment() {
        piles = new ArrayList();
    }
    
    /**Add an experiment. The experiment must have the same classifications **/
    public void addExperiment(Experiment e) throws Exception{
        //.. if it passed the test, add it
        piles.add(e);
    }
    
    /**Three options for evaluating an experiment.
     * 1) Evaluate each separately. Each has a distinct dataset(for stats) but a common TechniqueSet
     * 2) Jumble it all together and evaluate. Treat as one experiment
     * 3) Train on X and test on Y. Force instances from the same experiment to always appear in training or testing
     **/
    
    /** Evaluate each experiment separately. Each must already have a performance dataset attached to it
     */
    public void evaluate(TechniqueSet ts, int numFolds) throws Exception{
        for (TridimensionalLayer  t : piles) {
            Experiment e = (Experiment) t;
            if (e.getDataset() == null) throw new Exception("Must add a Evaluatable Dataset to experiment "+ e.id);
            
            e.evaluate(ts, e.getDataSet(), numFolds);
        }
    }
    
    /**Evaluate the data by making a fold for every subject and training on the others*/
    public void evaluateX(TechniqueSet ts) throws Exception {
        //.. extract attribtues for each dataset according to techniqueset
        extractAttributes(ts);
        
        //.. leave one out exactly once 
        for (int i =0; i < piles.size(); i++) {
            Experiment testingE = (Experiment) piles.get(i);
            if (testingE.getDataset() == null) {
                throw new Exception("Must add a Evaluatable Dataset to experiment " + testingE.id);
            }
            
            //.. add all the other experiments to a single experiment, the training
            Experiment allTraining = new Experiment("Multi", testingE.classification, testingE.readingsPerSec);
            for (int j =0; j < piles.size(); j++) {
                if (i!= j){ //.. make sure  not to add testing
                    Experiment trainingE =  (Experiment)piles.get(j);
                    allTraining.matrixes.addAll(trainingE.matrixes); //.. add all its instances
                }
            }
            
            Predictions predictions = new Predictions(testingE.getDataSet(), ts, testingE.classification);
               
            WekaClassifier wc = allTraining.train(ts);
            wc.test(testingE,ts, predictions, allTraining.asAlgosApplied, -1, false);
            System.out.println("-");
            //.. save the stats, then forget about them in the experiment
            ts.addPredictions(predictions);
            testingE.getDataSet().addPredictions(predictions);
            predictions = null;
            
        }
        
    }
    
      /**Extract the attribtues of this instance.**/
     public void extractAttributes(TechniqueSet ts) throws Exception{
        for (int i=0; i < piles.size(); i++) {
            Experiment e = (Experiment)piles.get(i);
            e.setTechniqueSet(ts);
            e.extractAttributes(ts.getFeatureSet());
        }
    }
     
     public void removeAllClassesBut(ArrayList<String> toKeep) {
         ArrayList<TridimensionalLayer> tempPiles = new ArrayList();
         for (int i=0; i < piles.size(); i++) {
            Experiment e = (Experiment)piles.get(i);
            Experiment e2 = e.removeAllClassesBut(toKeep);

            tempPiles.add(e2);
         }
         piles = tempPiles;
     }
}
