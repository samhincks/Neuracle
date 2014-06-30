/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package filereader;

import java.util.ArrayList;
import timeseriestufts.evaluatable.TechniqueSet;

/**
 *
 * @author samhincks
 */
    /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author samhincks
 */
public class Beste extends EvaluationInterface{
   
    public static void main(String[] args) {
        try {
            Beste nr = new Beste();
            nr.evaluateSingles();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String condition = "condition";
    public ArrayList<String> toKeep = new ArrayList();

    public Beste() {
        toKeep.add("easy");
        //toKeep.add("1-back");
        toKeep.add("hard");
    }
    
    /** (1) getTechniqueSetsByML() 
     * SMo: 63%
     * (2) getTechniqueSetsByFeature()
     * all ~50%
     * (3) getTechniquSetsByAS()
     * none : 63%
     * (4) getTechniqueSetsByFilter(PassFilter.FilterType.LowPass, 5, 0.01)
     *  5 -> 64%, 0.256 64%
     * (5) getTechniqueSetsBySAX() 
     **/

    public void evaluateSingles() throws Exception {
        ArrayList<TechniqueSet> techniques = new ArrayList();
        techniques.add(getTechniqueSet());
        testMulti(techniques, getFiles(), condition, toKeep);
    }
    
   
    /**Bad part. = 1, 6, 14, 16, 17, 19, 20**/
    public static ArrayList<String> getFiles() {
       ArrayList<String> files = new ArrayList();
        files.add("input/bestemusic/bestemusic01.csv");
        files.add("input/bestemusic/bestemusic02.csv");
        files.add("input/bestemusic/bestemusic03.csv");
        files.add("input/bestemusic/bestemusic04.csv");
        files.add("input/bestemusic/bestemusic05.csv");
        files.add("input/bestemusic/bestemusic06.csv");
        files.add("input/bestemusic/bestemusic07.csv");
        files.add("input/bestemusic/bestemusic08.csv");
      //  files.add("input/bestemusic09.csv");
        files.add("input/bestemusic/bestemusic10.csv");
        files.add("input/bestemusic/bestemusic11.csv");
        files.add("input/bestemusic/bestemusic12.csv");
        files.add("input/bestemusic/bestemusic13.csv");
        files.add("input/bestemusic/bestemusic14.csv");
        files.add("input/bestemusic/bestemusic15.csv");

        return files;
    }
    


}
