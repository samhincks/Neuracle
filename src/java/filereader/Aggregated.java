
package filereader;

import filereader.experiments.UAV;
import filereader.experiments.BesteExperiment;
import filereader.experiments.EvanVis;
import filereader.experiments.AJExperiment;
import java.util.ArrayList;
import timeseriestufts.evaluatable.PassFilter;
import timeseriestufts.evaluatable.TechniqueSet;  

/** A temporary class for evaluating data
 * @author samhincks
 */
public class Aggregated extends EvaluationInterface{   
    
   public static void main(String[] args)   {
        try {
            Aggregated ag = new Aggregated();
            ag.evaluateSingles();    
           // ag.evaluateCross();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String condition = "condition";
    public ArrayList<String> toKeep = new ArrayList();

    public Aggregated() {
        toKeep.add("easy");
        toKeep.add("hard");
    }
    
    
    public void evaluateCross() throws Exception {
        ArrayList<TechniqueSet> techniques = new ArrayList();
        //techniques.addAll(getTechniqueSetsByChannel());
        techniques.add(getTechniqueSet());

        System.out.println("tset is " + techniques.size());
        testCross(techniques, getBestFiles(), condition, toKeep);
    }
    
    
    public void evaluateSingles()throws Exception {
        ArrayList<TechniqueSet> techniques = new ArrayList();
        //techniques.addAll(getTechniqueSetsByChannel());
        techniques.addAll(getBandPassByFilter(5));

        //techniques.add(getTechniqueSet());

        System.out.println("tset is " + techniques.size());
        testMulti(techniques, getBestFiles(), condition, toKeep);
    
    }
    
    public ArrayList getQuick() {
        ArrayList one = new ArrayList();
        one.add("input/bestemusic/bestemusic04.csv");
        return one;

    }
    
    /** 90 files**/
    public ArrayList getFiles() {
        ArrayList agg = new ArrayList();
        agg.addAll(BesteExperiment.getFiles());
        agg.addAll(EvanVis.getFiles());
      //  agg.addAll(AJExperiment.getFiles());
        agg.addAll(UAV.getFiles());
        
        System.out.println(agg.size());
        return agg;
    }
    
    /**47 files**/
    public ArrayList getBestFiles() {
        ArrayList agg = new ArrayList();
        agg.addAll(BesteExperiment.getFiles());
        agg.addAll(EvanVis.getBest2());
        agg.addAll(AJExperiment.getBestFiles());
        agg.addAll(UAV.getBest2());
        
        System.out.println(agg.size());
        return agg;
    }
}
