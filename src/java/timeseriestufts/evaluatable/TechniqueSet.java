

package timeseriestufts.evaluatable;

import timeseriestufts.evaluatable.AttributeSelection;
import timeseriestufts.evaluation.experiment.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import timeseriestufts.evaluatable.performances.FoldPerformance;
import timeseriestufts.evaluatable.performances.Predictions;

/*Evaluation is the necessary information to evaluation process an experiment.
 * It consists of four parts:
 *  1) MLAlgorithm: a machine learning algorithm that will be used to learn features
 *  2) FeatureSet: a set of features that will be extracted to describe the data
 *  3) FeatureSelection: a set of procedures used to select features in the dataset
 *  4) Settings: any additional information that informs evaluation, ie num folds for evaluation
 * --
 * When it is run through an experiment, it creates a new subject and saves the accuracies for this subject.
 * In this way it persists through an experiment, as something that is added to the more experiments you run withit
 */
public class TechniqueSet extends Technique{

    public static TechniqueSet generate() throws Exception {
        TechniqueSet ts = new TechniqueSet("Test");
        //.. add ML, featureSet
        ts.addTechnique(new WekaClassifier(WekaClassifier.MLType.smo)); 
        FeatureSet fs = new FeatureSet("fs");
        fs.addFeaturesFromConsole("slope^mean", "*", "*");

        ts.addTechnique(fs);
        ts.addTechnique(new AttributeSelection(AttributeSelection.ASType.none, 3));
        ts.addTechnique(new PassFilter());
        ts.addTechnique(new Transformation(Transformation.TransformationType.none));

        return ts;
    }
    
    //.. Parameters that will be used during the Evaluation of an Experiment
    private ClassificationAlgorithm classifier; //.. what machine learning algorithm is used
    private FeatureSet featureSet; //.. What features will be extracted out of the isntance
    private AttributeSelection attributeSelection; //.. to be implemented
    private PassFilter filter;
    private Transformation transformation; 
    private Transformations transformations; //.. An ordered set of manipulations applied to the channelset

    //. GLOBALS
    public ArrayList<String> subjects;
    public String currentFileName = "default"; //.. the name of the current subject being mined, though a build merges many
    public double average =0; //.. the average over every single performance
   
    
    public TechniqueSet (String id) {
        this.id = id;
    }
    
    public TechniqueSet(ClassificationAlgorithm c,  AttributeSelection a,FeatureSet f ){
        classifier = c;
        featureSet = f;
        attributeSelection = a;
        
        //.. Make an id out of the various algorithms id's. 
        //... This is a critical step as it ensure a TechniqueSet will be unique within a session
        //.... in other words, we don't wind up creating two TechniqueSets which are the same in function but have different ids
        this.id = c.id +f.id+a.id; 
    }
    
    /*---Get methods for featureset. We don't want them set outside the class**/
    public ClassificationAlgorithm getClassifier(){return this.classifier;}
    public FeatureSet getFeatureSet(){return this.featureSet;}
    public AttributeSelection getAttributeSelection(){return this.attributeSelection;}
    public PassFilter getFilter(){return this.filter;}
    public Transformation getTransformation(){return this.transformation;}
    public Transformations getTransformations(){return this.transformations;}


    
    
    public boolean isComplete() {
        if (classifier == null || attributeSelection == null ||featureSet == null)
            return false;
        return true;
    }
    
    /**Add a technique. Throw an exception if this technique has already been added.
     Return false if we have already set this TechniqueType. (This should never be
     * allowed to happen, since the stats should pertain to a particular combination
     * techniques and it does not make sense to change them
     */
    public boolean addTechnique(Technique t) throws Exception{
        if(t instanceof ClassificationAlgorithm) {
            if (classifier!= null)return false; //.. if we've already set this value
            classifier =(ClassificationAlgorithm)t;
        }
        else if(t instanceof FeatureSet) {
            if (featureSet!= null)return false; //.. if we've already set this value
            featureSet =(FeatureSet)t;
            
        }
        else if(t instanceof AttributeSelection) {
            if (attributeSelection!= null)return false; //.. if we've already set this value
            attributeSelection =(AttributeSelection)t;
        }
        
        else if (t instanceof PassFilter) {
            if (filter!= null)return false; //.. if we've already set this value
            filter =(PassFilter)t;
        }
        
        else if (t instanceof Transformation) {
            if (transformation!= null)return false; //.. if we've already set this value
            transformation =(Transformation)t;
        }
        
        else if (t instanceof Transformations) {
            if (transformations!= null)return false; //.. if we've already set this value
            transformations =(Transformations)t;
        }
       
        //.. For example if we are adding a Filter or this class itself
        else {
            throw new Exception("Cannot add Technique " +t.id);
        }
        
        return true;
    }
    
    /**Like add technique but change underlying technique even if it already exists*/
    public void resetTechnique(Technique t) throws Exception{
        if(t instanceof ClassificationAlgorithm) {
            classifier =(ClassificationAlgorithm)t;
        }
        else if(t instanceof FeatureSet) {
            featureSet =(FeatureSet)t;
            
        }
        else if(t instanceof AttributeSelection) {
            attributeSelection =(AttributeSelection)t;
        }
        
        else if (t instanceof PassFilter) {
            filter =(PassFilter)t;
        }
        
        else if (t instanceof Transformation) {
            transformation =(Transformation)t;
        }
        else {
            throw new Exception("Cannot add Technique " +t.id);
        }
    }
    
   
   @Override 
   public void addPredictions(Predictions p) {
       classifier.addPredictions(p);
       featureSet.addPredictions(p);
       attributeSelection.addPredictions(p);
       super.addPredictions(p);

   }

   
    
}
