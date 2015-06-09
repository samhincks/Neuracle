/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package timeseriestufts.evaluatable;


/**Includes basic information about the classifier, including name, stats, parameters
 * A bit of a misleading name since this isn't actually a classifier, only the name, stats, etcs
 */
public abstract class ClassificationAlgorithm extends Technique{
    //.. Used if a classifier is trained, and saved, to be used for later, potentially realtime classification
    public int timesTrained =0;   
    public TechniqueSet lastTechniqueTested;

}
