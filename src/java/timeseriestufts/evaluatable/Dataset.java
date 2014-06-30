/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeseriestufts.evaluatable;

import java.io.BufferedWriter;

/**
 *
 * @author samhincks
 */
public class Dataset extends Evaluatable {

    static Dataset generate() {
        return new Dataset("test");
    }
 
    public Dataset (String id) {super.id = id;}

    
    
}
