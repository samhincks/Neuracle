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
public class EvanVis extends EvaluationInterface{
     public static void main(String[] args) {
        try {
            EvanVis nr = new EvanVis();
            nr.evaluateSingles();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String condition = "condition";
    public ArrayList<String> toKeep = new ArrayList();

    public EvanVis() {
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
        testMulti(techniques, getBest(), condition, toKeep);
    }
    
    /**Bad part. = 1, 6, 14, 16, 17, 19, 20**/
    public static ArrayList<String> getFiles() {
        ArrayList<String> files = new ArrayList();
        String folder = "input/evan_vis/";
        for (int i = 1; i < 24; i++) {
            
            String number = String.valueOf(i);
            if (i < 10) {
                number = "0" + i;
            }
           if (!(i == 1 || i == 6 || i== 14 || i==16 || i ==17 || i ==19 ||i==20))
              files.add(folder + number + "_back3.csv"); ///. add .csv
        }
        return files;
    }
       /**Bad part. = 1, 6, 14, 16, 17, 19, 20**/
    public static ArrayList<String> getBest2Cross() {
        ArrayList<String> files = new ArrayList();
        String folder = "input/evan_vis/";
        for (int i = 1; i < 24; i++) {
            
            String number = String.valueOf(i);
            if (i < 10) {
                number = "0" + i;
            }
           if (!(i == 1 || i == 6 || i== 14 || i==16 || i ==17 || i ==19 ||i==20)) {
               
              if (i != 13 && i!= 9 && i !=4 && i != 22 && i != 15 && i != 8 && i != 3 && i !=21) {
                   if (i ==7 || i == 11 || i==12 || i== 18)
                      files.add(folder + number + "_back3.csv"); ///. add .csv
              }
           }
        }
        System.out.println("From Evan : " + files.size());
        return files;
    }
                      
     /**Bad part. = 1, 6, 14, 16, 17, 19, 20**/
    public static ArrayList<String> getBest2() {
        ArrayList<String> files = new ArrayList();
        String folder = "input/evan_vis/";
        for (int i = 1; i < 24; i++) {
            
            String number = String.valueOf(i);
            if (i < 10) {
                number = "0" + i;
            }
           if (!(i == 1 || i == 6 || i== 14 || i==16 || i ==17 || i ==19 ||i==20)) {
               
              if (i != 13 && i!= 9 && i !=4 && i != 22 && i != 15 && i != 8 && i != 3 && i !=21) {
                      files.add(folder + number + "_back3.csv"); ///. add .csv
              }
           }
        }
        System.out.println("From Evan : " + files.size());
        return files;
    }
    /**Bad part. = 1, 6, 14, 16, 17, 19, 20**/
    public static ArrayList<String> getBest() {
        ArrayList<String> files = new ArrayList();
        String folder = "input/evan_vis/";
        for (int i = 1; i < 24; i++) {
            
            String number = String.valueOf(i);
            if (i < 10) {
                number = "0" + i;
            }
           if (!(i == 1 || i == 6 || i== 14 || i==16 || i ==17 || i ==19 ||i==20)) {
               
              if (i!= 2 && i !=3 && i!= 8 && i!=11 && i!=21 && i!= 23 ) 
                files.add(folder + number + "_back3.csv"); ///. add .csv
           }
        }
        System.out.println(files.size());
        return files;
    }
    
}
