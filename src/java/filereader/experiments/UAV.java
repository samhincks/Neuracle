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
public class UAV  extends EvaluationInterface {
      public static void main(String[] args) {
        try {
            UAV nr = new UAV();
            nr.evaluateSingles();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String condition = "condition";
    public ArrayList<String> toKeep = new ArrayList();

    public UAV() {
        toKeep.add("easy");
        //toKeep.add("1-back");
        toKeep.add("hard");
    }
    
    
    /** (1) getTechniqueSetsByML() 
     * SMo: 64%
     * (2) getTechniqueSetsByFeature()
     * slope best at 59%; all good but secondder
     * (3) getTechniquSetsByAS()
     * none : 63%
     * (4) getTechniqueSetsByFilter(PassFilter.FilterType.LowPass, 5, 0.01)
     *  0.31 -> 67% 
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
        String folder = "input/UAV_processed/";
        for (int i = 0; i < 28; i++) {
            
            String number = String.valueOf(i);
            
           if (i!=1) //.. 1 has only one class, which is to be expected from he markers file 
           files.add(folder + number + ".csv"); ///. add .csv
        }
        return files;
    }
    
    public static ArrayList<String> getBest(boolean strict) {
        ArrayList<String> files = new ArrayList();
         String folder = "input/UAV_processed/";
        for (int i = 0; i < 28; i++) {
            
            String number = String.valueOf(i);
           if (strict) {
                if (i!=1 && i != 3 && i !=6 && i!=7 && i!=13 && i != 14 && i !=14 && i != 15 && i!= 17 && i!= 18 && i != 19 && i !=20 && i != 21 && i!= 22 && i!=23 && i!=24) //.. 1 has only one class, which is to be expected from he markers file 
                   files.add(folder + number + ".csv"); ///. add .csv
                
           }
           else {
               if (i != 1 && i != 3 && i != 7 && i != 13 && i != 15 && i != 10 && i!= 19&& i != 22 && i != 24)
                   files.add(folder + number + ".csv"); ///. add .csv
           }
        }
        
        System.out.println("Testing on " + files.size());
        return files;
    }
    
    
     
    public static ArrayList<String> getBest2() {
        ArrayList<String> files = new ArrayList();
         String folder = "input/UAV_processed/";
        for (int i = 0; i < 28; i++) {
            
            String number = String.valueOf(i);
            if (i!= 1 && i!= 20 && i != 26 && i !=21 && i !=19 && i != 13 && i!= 23 && i !=11 && i!= 2 && i != 15 && i !=14 && i!= 6 && i !=7)
               files.add(folder + number + ".csv"); ///. add .csv
                
        }
        
        System.out.println("From Dan " + files.size());
        return files;
    }
    
    public static ArrayList<String> getBest2Cross() {
        ArrayList<String> files = new ArrayList();
         String folder = "input/UAV_processed/";
        for (int i = 0; i < 28; i++) {
            
            String number = String.valueOf(i);
            if (i!= 1 && i!= 20 && i != 26 && i !=21 && i !=19 && i != 13 && i!= 23 && i !=11 && i!= 2 && i != 15 && i !=14 && i!= 6 && i !=7) {
                if (i ==4 || i == 9 || i == 10 || i == 12 || i== 16 || i==22 || i ==24 || i == 25 || i == 27)
                    files.add(folder + number + ".csv"); ///. add .csv
            }
                
        }
        
        System.out.println("From Dan " + files.size());
        return files;
    }
    
}
