
package timeseriestufts.evaluatable;

import java.util.ArrayList;

/**An ordered series of transformations that have been applied to a dataset
 * @author samhincks
 */
public class Transformations extends Technique{
    public ArrayList<Transformation> transformations;
    
    public Transformations() {
        transformations = new ArrayList();
    }
    
    public void addTransformation(Transformation t) {
        this.id += t.id;
        transformations.add(t);
    }
    
    public Transformations getCopy() {
        Transformations trans = new Transformations();
        for (Transformation t : transformations) {
            trans.addTransformation(t);
        }
        return trans;
    }
}
