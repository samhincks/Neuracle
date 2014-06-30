/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeseriestufts.evaluation.crossvalidation;
import timeseriestufts.evaluatable.TechniqueSet;
import java.util.ArrayList;
import timeseriestufts.evaluatable.WekaClassifier;
import timeseriestufts.evaluatable.performances.FoldPerformance;
import timeseriestufts.evaluatable.performances.Predictions;
import timeseriestufts.evaluation.classifiers.weka.WekaData;
import timeseriestufts.kth.streams.tri.Experiment;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

/**Takes as input a set of training instances, a set of testing instances, and the name of a classifier.
 * Builds the classifier on the training instances. Then tests it, one by one, on the testing instances.
 * Stores its stats in classifier performance for retrieval
 * 
 * 
 * NOT REALLY IN USE ANY MORE. THAT IS, WE USE IT, BUT ALL WE NEED IS TRAINING / TESTING
 */
public class Fold {
    public Experiment training;
    public Experiment testing;
    
    
     public Fold (Experiment training, Experiment testing) {
        this.training = training;
        this.testing = testing;
    }
   
     
  
}
