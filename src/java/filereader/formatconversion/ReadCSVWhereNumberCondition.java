/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package filereader.formatconversion;

import filereader.experiments.EvanVis;
import filereader.experiments.NoRestExperiment;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
import timeseriestufts.kth.streams.tri.Experiment;

/**
 *
 * @author samhincks
 */
public class ReadCSVWhereNumberCondition {
    
    public static void main(String [] args) {
        /*
        String folder = "input/no_rest2/";
        String filename = "01_R.csv";
        String condition = "condition";
        */
        
        for (String s : EvanVis.getFiles()) {
            String outfilename = s +"2.csv";
            String infilename = s +".csv";
            String colfile = s+"_Labels" +".csv";
            removeFirstAndAddCol(infilename, outfilename, "condition", colfile, 2);
            
            
            String finalFilename = s +"3.csv";
            String [] oldVals = {"0","1","2","3","4"};
            String[] newVals  = {"junk", "baseline", "bogus","easy","hard"};
            GiveNominalConditionNames(outfilename, finalFilename,"condition", oldVals, newVals);
            
        }
    }
   
    /** Replace condition the values specified in old with the values in new (where indexes must match)
     * in the conditionName of fileName
     */
    public static void GiveNominalConditionNames(String filename, String newfilename, String conditionName, String [] oldVals, String [] newVals) {
        System.out.println("working on " + filename);
        try {
            BufferedReader br = new BufferedReader(new java.io.FileReader(filename));
            BufferedWriter bw = new BufferedWriter(new java.io.FileWriter(newfilename));
            
            String line = br.readLine();
            bw.write(line+"\n");
            String[] attributeNames = line.split(",");
            
            int CONDINDEX = -1;
            
            //.. find conditionName, and generate error if its not found
            int index =0;
            for (String s : attributeNames) {
                if (s.equals(conditionName)) {
                    CONDINDEX = index;
                    break;
                }
                index++;
            }
            
            if(CONDINDEX == -1) throw new Exception ("Could not find " + conditionName);
            
            //.. read each line and write it to the new file
            while ((line = br.readLine()) != null) {
                 String[] values = line.split(",");
                 String toChange = values[CONDINDEX];
                 String newVal = getReplacement(toChange, oldVals, newVals);
                 
                 for (int i =0; i <values.length-1; i++) {
                        bw.write(values[i] + ",");
                 }
                 bw.write(newVal +"\n");
                
                
            }
            bw.close();
            
            
        }
        catch(Exception e) {e.printStackTrace();}
       
    }
    
    private static String getReplacement(String current, String [] oldVals, String [] newVals) throws Exception{
        for (int i=0; i< oldVals.length; i++) {
            if (oldVals[i].equals(current))
                return newVals[i];
        }
        
        throw new Exception("Could not find " + current);
    }
    
    
    /**Add singular column from one file to the other. 
     Set removeFirst to n to remove the first n columns
     * colfFiles length should be dataFile's length -1 (it just lacks the column name)
     */
    public static void removeFirstAndAddCol(String dataFile, String outfilename, String colName, String colFile, int removeFirst){
        System.out.println("working on " + dataFile);
        try{
            BufferedReader br = new BufferedReader(new java.io.FileReader(dataFile));
            BufferedReader colr = new BufferedReader(new java.io.FileReader(colFile));
            BufferedWriter bw = new BufferedWriter(new java.io.FileWriter(outfilename));
            
            String line = br.readLine();
            
            String[] attributeNames = line.split(",");
            for (int i =0; i < attributeNames.length; i++) {
                if ( i>removeFirst) {
                    String thisCol = attributeNames[i];
                    thisCol = thisCol.replace(" ", "");
                    bw.write(thisCol +",");
                }
            }
            bw.write(colName+"\n");
            
            //.. read each line and write it to the new file
            while ((line = br.readLine()) != null) {
                 String[] values = line.split(",");
                 String col = colr.readLine();
                
                 for (int i =0; i <values.length; i++) {
                     if ( i > removeFirst) {
                        bw.write(values[i] + ",");
                     }
                 }
                 bw.write(col +"\n");
            }
            bw.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
