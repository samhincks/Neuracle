/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeseriestufts.evaluation.featureextraction;

/**
 *
 * @author samhincks
 */
public class PresetAttribute extends Attribute{

    
    @Override
    public void extract() throws Exception {
        System.out.println("Extracting");
    }

    @Override
    public String setName() throws Exception {
        return name;
    }

    @Override
    public weka.core.Attribute getWekaAttribute() {
        return new weka.core.Attribute(this.name);
    }
    
}
