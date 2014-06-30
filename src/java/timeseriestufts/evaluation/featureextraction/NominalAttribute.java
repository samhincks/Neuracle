/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeseriestufts.evaluation.featureextraction;

import java.util.ArrayList;



public abstract class NominalAttribute extends TSAttribute {
    public String [] possibilities;

    public String getPossibilitiesString() throws Exception{
        if (possibilities ==null) throw new Exception("No possibilities added yet to nominal attribute");
        String retString = "{";
        for(int i=0; i < possibilities.length; i++) {
            retString += possibilities[i];
            if(i!= possibilities.length-1)
                retString += ",";
        }
        
        return retString +"}";
                
    }
    
    
}
