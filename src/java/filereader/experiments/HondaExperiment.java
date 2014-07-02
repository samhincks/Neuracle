/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package filereader.experiments;

import filereader.EvaluationInterface;
import java.util.ArrayList;
import timeseriestufts.evaluatable.PassFilter;
import timeseriestufts.evaluatable.TechniqueSet;

/**
 *
 * @author samhincks
 */
public class HondaExperiment extends EvaluationInterface {
  public static  void main(String [] args) {
      try{
        HondaExperiment honda = new HondaExperiment();
        honda.evaluateSingles();
      }
      catch (Exception e) {
          e.printStackTrace();
      }
  }
  
  public String condition = "condition";
  public ArrayList<String> toKeep = new ArrayList(); 
 
  
  public HondaExperiment () {
      toKeep.add("0-back");
      //toKeep.add("1-back");
      toKeep.add("2-back");
  }
  
  /** Evaluation conclusions, 
   * 1. Use LMT. 
   **/
  public void evaluateSingles() throws Exception{
      ArrayList<TechniqueSet> techniques = new ArrayList();
      techniques.add(getTechniqueSet());
      testMulti(techniques, getFiles(), condition, toKeep);
  }
  
  public static ArrayList<String> getFiles() {
       ArrayList<String> files = new ArrayList();
        String folder = "input/HondaNBack/";
        for (int i = 1; i < 9; i++) {
            if (i !=2)
                files.add(folder+"Participant"+i+".csv");
        }
        return files;
    }
}
