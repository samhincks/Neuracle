/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package timeseriestufts.evaluation.featureextraction;
import java.util.ArrayList;
import weka.core.FastVector;

/**
 *
 * @author Sam
 */
public class Attributes {
    public ArrayList<Attribute> attributeList;
    
    public Attributes() {
        attributeList = new ArrayList();
    }
    public void addAttribute(Attribute attribute) {
        attributeList.add(attribute);
    }

    public void printAttributes () {
        for (int i =0; i<attributeList.size(); i++) {
            Attribute thisAttribute = attributeList.get(i);
            if (!(thisAttribute instanceof NominalAttribute))
                System.out.println("Attribute " + thisAttribute.name + " : " + thisAttribute.numValue);
            else
                System.out.println("Attribute " + thisAttribute.name + " : " + thisAttribute.nomValue);
                
        }
    }
    

    //.. 'Extract' each individual attribute. Given its description and data, set the attributes value
    public void extract() throws Exception{
        for (Attribute attribute : attributeList) {
             attribute.extract();
        }
    }

    public FastVector getWekaAttributes() throws Exception {
        FastVector attrs = new FastVector();
        for (Attribute a : attributeList) {
            attrs.addElement(a.getWekaAttribute());
        }
        return attrs;
    }
    
   


    

}
