/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeseriestufts.evaluation.crossvalidation;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;
import timeseriestufts.evaluatable.performances.FoldPerformance;
import timeseriestufts.kth.streams.bi.Instance;
import timeseriestufts.kth.streams.tri.Experiment;

/**Given a collection of instances, return them partitioned into N Folds (a collection
 * of training/testing instances s.t. no isntances are in both. 
 * 
 * Consider modifying size of testing so that we get right amount to have equal amount of each condition 
 * @author shincks
 * 
 * What happens if A) We have an extremely unequal distribution of one condition?  Then 
 *                      
 */
public class CrossValidation {
    int testingSize; //.. how many instances we want in testing 
    public int numFolds; //.. how many folds to create
    public Fold [] folds;
    public ArrayList<Integer> blocked; //.. all the instances we have already added
    public Experiment experiment; 
    private ArrayList<String> allConditions; //.. all the conditions we want to extract
  
    public CrossValidation(Experiment experiment) throws Exception{
        numFolds = experiment.numFolds;
        
        ///.. Calculate testing size so that we get approximately the correct amount in each
        testingSize =  (int)( Math.round((double)experiment.matrixes.size() / (double)numFolds));    

        //.. if this rounds to 0, then make it 1
        if (testingSize ==0) testingSize = 1;

        //.. if it rounds to greater than half, then make it 33% (ie, we only have 1 or two folds) (33% a standard split
        if (testingSize > (experiment.matrixes.size()/2))
            testingSize = (int)(experiment.matrixes.size() * .333);
        
        //testingSize =1;
        folds = new Fold[numFolds];
        blocked = new ArrayList();
        this.experiment = experiment;
        allConditions = experiment.classification.values;
        
    }
    /**Create a fold that puts each instance in testing exactly once and trains on the remaining.
     * numFolds must be set to the size of the matrixes
     */
    public Fold [] leaveOneOut() throws Exception {
        if (numFolds != experiment.matrixes.size()) throw new Exception("Set experiment.numFolds to size of matrix");
        
        //.. iterate through all training leaving i out
        for (int i = 0; i < experiment.matrixes.size(); i++) {
            Experiment training = new Experiment(experiment.filename, experiment.classification);
            Experiment testing = new Experiment(experiment.filename, experiment.classification); 
            
            //.. add the testing
            testing.addMatrix(experiment.matrixes.get(i));
            
            //.. add the training trials (all but j)
            for (int j = 0; j < experiment.matrixes.size(); j++) {
                if (j!= i){
                    training.addMatrix(experiment.matrixes.get(j));
                }
            }
             Fold fold = new Fold(training, testing);
             folds[i] = fold;
        }   
        return folds;
    }
    
    
    /**------------AFTER THIS, NO LONGER IN USE. The next set of functions leaves an arbitrary amount in testing
     but hasn't been used in a while***/
    
    /**Create an array of folds that 
        a) has the right number of folds
        b) has the approximately correct testing size, so that instances only appear in testing once
        c) has approximately equal distribution of conditions
        d) violates b) before c) to create the right number of folds (resets the used-instance-array)*/
    public Fold [] calculateFolds() throws Exception {
        checkConditionQuantities();
        
        int numAdded =0; //.. how many testingInstances we've added
        
        //.. Create a new fold
        for (int i = 0 ; i <numFolds; i++) {
            Experiment training = new Experiment(experiment.filename, experiment.classification);
            Experiment testing = new Experiment(experiment.filename, experiment.classification); 
            
            //.. pick the testing instances, and add to testing
            ArrayList<Integer> testingTrials = new ArrayList();
            ArrayList<Integer> trainingTrials = new ArrayList();
            for (int j=0; j < testingSize; j++ ) {
                ///.. pick a random permissable testingTrial
                int trialNum = pickRandomPermissableTesting(numAdded);
                testing.matrixes.add(experiment.matrixes.get(trialNum)); //.. add to testing
                blocked.add(trialNum); //.. and block it, so we never add it again
                testingTrials.add(trialNum);
                numAdded++;
            }
            
            //.. Add the remaining trials to testing (ie, all but testingSize
            for (int j =0; j < experiment.matrixes.size(); j++) {
                Instance thisInstance = (Instance) experiment.matrixes.get(j);
                if (!testingTrials.contains(j)) {
                    training.matrixes.add(thisInstance);
                    trainingTrials.add(j);
                }
            }
            
            //.. Now make an actual Fold, first initializing a stats file
             Fold fold = new Fold(training,testing);
             checkTestingTrainingSizes(fold); //.. throw exception if we have too few of any
             folds[i] = fold;
        }
        return folds;
    }
    
     /**Pick an index for testing
      * WARNING: Infinite loop if testingSize < numConditions of each /numConditions**/
     private int pickRandomPermissableTesting(int numAdded) {
         //.. We want our trial to be this condition, but if it isn't that's OK
         String condition = allConditions.get(numAdded % allConditions.size());
         Random r = new Random();
         int randomStartSearch = r.nextInt(experiment.matrixes.size());
         int retIndex = findTrialWithCondition(condition, randomStartSearch);
         
         if (retIndex != -1)
             return retIndex;
         
         //.. Our search showed up negative. In that case, reset our blocked matrix, and restart
          blocked = new ArrayList();
          return pickRandomPermissableTesting(numAdded);
     }
     
     /**Given a condition and a random start pointReturn a non-blocked trial with this condition; -1 if non exists */
     private int findTrialWithCondition(String condition, int startSearch) {
         //.. search from startIndex to finish
         for (int i =startSearch ;i < experiment.matrixes.size(); i++) {
             Instance thisInstance = (Instance)experiment.matrixes.get(i);
             if ((!blocked.contains(i)) &&thisInstance.condition.equals(condition))
                 return i;
         }
         
         //.. search from realStart to searchStart
         for (int i =0; i < startSearch; i++) {
             Instance thisInstance = (Instance)experiment.matrixes.get(i);
             //.. if instance is not blocked and conditiosn equal
             if ((!blocked.contains(i)) &&thisInstance.condition.equals(condition)) {
                 return i;
             }
         }
         return -1;
     }
     
     /**How does this fit in?*/
     private FoldPerformance getNewClassifierPerformance(int foldNum, ArrayList<Integer> trainingTrials, ArrayList<Integer> testingTrials) throws Exception{ 
        FoldPerformance thisPerformance = new FoldPerformance(experiment.classification.wekaString, 
                experiment.id+experiment.getTechniqueSet().getId(), foldNum, trainingTrials, testingTrials);        
        thisPerformance.foldNum = foldNum;        
        thisPerformance.trainingTrials = trainingTrials;        
        thisPerformance.testingTrials = testingTrials;
        return thisPerformance;
    }
     
     
    public static void main(String []args) {
        ArrayList<Integer> b = new ArrayList();
        b.add(2);
        if (b.contains(2)) System.out.println("true");
    }

    /**Throw an exception if we have too few of any quantitity. 
     How should this be calculated? **/
    private void checkConditionQuantities() throws Exception{
        //.. Throw an exception if we have too few of any of the instances
        int minInTraining =2;
        int minimumAmount = (testingSize / allConditions.size()) + minInTraining;
        Hashtable<String, Integer> amountOfEach = experiment.getAmountOfEachCondition();
      
        //.. compare to what we actually have   
        for (String condition : allConditions) {
            int amount = amountOfEach.get(condition);
            if (amount< minimumAmount) {
                throw new Exception("We don't have enough of condition " + condition + " Needed : " + minimumAmount + " ; Have: " + amount);
            }
        }  
        
    }
    /**Throws an exception if we have too few of any instances in either testing or training. */
    private void checkTestingTrainingSizes(Fold fold) throws Exception {
        int minInTesting = 1;
        int minInTraining = 10; //.. This may change
        
        if (fold.training.matrixes.size() < 10)
            throw new Exception("We don't have enough of trainingInstacnes .Needed : " + minInTraining + " ; Have: " + fold.training.matrixes.size());
        
        
        if (fold.testing.matrixes.size() < 1)
            throw new Exception("We don't have enough of testingInstacnes .Needed : " + minInTesting + " ; Have: " + fold.testing.matrixes.size());

    }
} 
