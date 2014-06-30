/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package filereader;

import java.util.ArrayList; 
import timeseriestufts.evaluatable.PassFilter;
import timeseriestufts.evaluatable.TechniqueSet;

/**
 *
 * @author samhincks
 */
public class NoRestExperiment extends EvaluationInterface {

    public static void main(String[] args) {
        try {
            NoRestExperiment nr = new NoRestExperiment();
            nr.evaluateSingles();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String condition = "condition";
    public ArrayList<String> toKeep = new ArrayList();

    public NoRestExperiment() {
        toKeep.add("easy");
        //toKeep.add("1-back");
        toKeep.add("hard");
    }

    /**
     * Evaluation conclusions, 1. Use SMO. 2. LowPass: 0.0468 = 57%; 0.09375 =
     * 56%; Highpass does not help 3. FS: use best, but not max 4. AS: 115, not
     * much better than none 5. SAX: fr. 58%, kb: 57%, kq: 62%, hdbb: 60%, ip:
     * 59%, gl: 59%, [ogj, hn,hq, ac, fk, kq, is, hm >60%)
   *
     */
    public void evaluateSingles() throws Exception {
        ArrayList<TechniqueSet> techniques = new ArrayList();
        techniques.add(getTechniqueSet());
        testMulti(techniques, getFiles(), condition, toKeep);
    }

    public static ArrayList<String> getFiles() {
        ArrayList<String> files = new ArrayList();
        String folder = "input/no_rest/";
        for (int i = 1; i < 16; i++) {
            String number = String.valueOf(i);
            if (i < 10) {
                number = "0" + i;
            }
            files.add(folder + number + "_R3.csv"); ///. add .csv


        }
        return files;
    }
}
