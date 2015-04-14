/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeseriestufts.evaluatable.performances;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashMap;
import timeseriestufts.evaluatable.*;
import timeseriestufts.evaluation.experiment.Classification;

/**This is the collective output of an ongoing datamining session.
 * Contains two methods for accessing statistics: 
 * 1) By dataset: we might want to know how a dataset fared with different tools
 * 2) By technique: we might also want to know how a technique fared over different datasets
 * @author samhincks
 */
public class Performances {     
    public HashMap<String, Dataset>  dataSets = new HashMap(); //.. make this hashmap since values is so convenient
    public HashMap<String, TechniqueSet> techniqueSets = new HashMap();
    public HashMap<String, Predictions> predictionSets = new HashMap();
    
    public void addNewDataset(Dataset ds) throws Exception{
        if (dataSets.containsValue(ds)) 
            throw new Exception("Trying to add new Dataset " + ds.getId() + ", but it already  exists");
        
        dataSets.put(ds.getId(), ds);
    }
    
    public void addNewTechniqueSet(TechniqueSet ts) throws Exception{
        if(techniqueSets.containsValue(ts))
              throw new Exception("Trying to add new TechniqueSet " + ts.getId() + ", but it already  exists");
        
        techniqueSets.put(ts.getId(), ts);
    }
    
    public void addNewPredictionsSet(Predictions ps) throws Exception{
        predictionSets.put(ps.getId(), ps); //.. IN THIS CASE WE WANT TO DELETE IF IT ALREADY EXISTS
    }
    
    public String getDatasetStats(String id) {
        return this.dataSets.get(id).getStatsString();
    }
    public ArrayList<String> getDatasetStats() {
        ArrayList<String> dsStats = new ArrayList(); 
        for (Dataset ds : dataSets.values()) {
            dsStats.add(ds.getStatsString());
        }
        
        return dsStats;
    }
    
    /**Average a bunch of performances into a single number*/
    public double getAverageDSStats() {
        double total =0;
        for (Dataset ds : dataSets.values()) {
             total += ds.getAverage();
        }
        return total/ dataSets.values().size();
    }

    public TechniqueSet getTechniqueSet(String id) {
        if (!techniqueSets.containsKey(id)) return null;
        return techniqueSets.get(id);
    }
    
    public Dataset getDataSet(String id) {
        return dataSets.get(id);
    }
    
    public Predictions getPredictionSet(String id) {
        if (!predictionSets.containsKey(id)) return null;
        return predictionSets.get(id);
    }
    
    
    /**Print performance for each dataset. Note we're assuming the dataset is UNIQUE 
     each time its being run on a new technique set which is an awful assumption
     In the future, redesign FoldPerformance and make each performance a grouping of dataset
     *  and techniqueset
     */
    public void printPerformances(BufferedWriter bw, boolean first) throws Exception{
        if (first){
            bw.write("Technique,Average,");
            for (Dataset ds: dataSets.values()){
                System.out.print(ds.getId() +",");
                bw.write(ds.getId() +",");
            }
            System.out.println();
            bw.write("\n");
        }
        for (TechniqueSet ts : techniqueSets.values()){
            String tsTitle = "ML: " +ts.getClassifier().getId();
            tsTitle+= "FS: " +ts.getFeatureSet().getConsoleString();
            tsTitle += "AS: " + ts.getAttributeSelection().getId();
            tsTitle += "Filter: " + ts.getFilter().getId();
            tsTitle += "Transformation: " + ts.getTransformation().getId();

            System.out.print(tsTitle + " , " + ts.getAverage());
            bw.write(tsTitle + " , " + ts.getAverage() + ",");
            for (Dataset ds : dataSets.values()) {
                double avg = ds.getAverage();
                
                System.out.print(avg+",");
                bw.write(avg+",");
            }
            System.out.println();
            bw.write("\n");
         }
    }
    
    /**Forget about prior datasets; reset so that a dataset can be read*/
    public void resetDatasets() {
        dataSets = new HashMap();
    }
    
    /**How does statistic collection work broadly across a session? 
     * Each time you create a new evaluation technique,the instructions for how to 
     * carry out a procedure along with a stats-holder are stored in the same object. 
     * For different experiments that use the same evaluation technique, these are stored together
     * Everytime we evaluate a new experiment, we create a new dataset, which extends the same
     * Evaluatable object as the technique. If the same experiment is evaluated on different
     * techniques this object is still the same. 
     * 
     * Performances consists of a collection of dataset and Technique evaluatables. 
     * Each of these evaluatables in turn consist of a collection of a collection of FoldPerformances
     * A FoldPerformance is a series of correct or incorrect answers over a fold. It is an
     * archaic, super-bulky object that knows everything we might be interested in about a fold, 
     * saving what trials went into testing, training, confusion matrixes etc.
     */
    public static void main(String [] args) {
        try{
            Dataset ds = new Dataset("a");
            Dataset dsB = new Dataset("b");

            WekaClassifier wc = new WekaClassifier("j48");
            AttributeSelection as = new AttributeSelection("cfs",5);
            FeatureSet fs = new FeatureSet("s");
            TechniqueSet ts = new TechniqueSet("ts");
            ts.addTechnique(wc);ts.addTechnique(fs);ts.addTechnique(as);
            
            ArrayList<String> classes = new ArrayList();classes.add("a"); classes.add("b");
            Classification c = new Classification(classes, "test" );
            
            Predictions predictions = new Predictions(ds, ts, c);
            
            //.. then an experiment might start with 10 folds and 3 predictions in each fold
            int numFolds =10;
            for (int i = 0; i < numFolds; i++) {
                
                for (int j = 0; j < 7; j++) {
                    predictions.addPrediction(1,1,j);
                }
                
                for (int j = 0; j < 3; j++) {
                    predictions.addPrediction(1, 0,j);
                }
                
            }
            ds.addPredictions(predictions);
            ts.addPredictions(predictions);
            
            //.. now a techniqueset and a dataset has been evaluated over 10 folds, we can query for stats
            System.out.println("num Correct: " + ts.getAverage() + " = "+ts.getMostRecentAverage());
            System.out.println(".. Shouuld be same as num Correct: " + ds.getAverage() + " = "+ds.getMostRecentAverage());
            
             predictions = new Predictions(dsB, ts, c);
            //.. then imagine we evaluate the same techniqueset over a different dataset
             numFolds =20;
            for (int i = 0; i < numFolds; i++) {

                //.. numCorrect = i, numWrong = numFolds -i
                for (int j = 0; j < 8; j++) {
                    predictions.addPrediction(1, 1, j);
                }

                for (int j = 0; j < 2; j++) {
                    predictions.addPrediction(1, 0, j);
                }
            }
            
            dsB.addPredictions(predictions);
            ts.addPredictions(predictions);
            
            //.. now the first dataset should be teh same as it was before
            System.out.println(".. The old % for first average Correct: " + ds.getAverage() );
            
            //.. the techniqueset should know its overall stats and a more recent average 
            System.out.println("num Correct: " + ts.getAverage() + " != the more recnt "+ts.getMostRecentAverage());
            System.out.println(".. Shouuld be same as num Correct: " + dsB.getAverage() + " = "+dsB.getMostRecentAverage());
            
            
            //.. We add the techniquesets and datasets to this Performances object
            Performances performances = new Performances();
            performances.addNewDataset(ds);performances.addNewDataset(dsB);
            performances.addNewTechniqueSet(ts);

        }
        
        
        
        catch(Exception e) {e.printStackTrace();}
    }

}
