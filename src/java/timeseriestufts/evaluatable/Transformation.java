/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeseriestufts.evaluatable;

/**
 *
 * @author samhincks
 */
public class Transformation extends Technique {
    public static enum TransformationType{ZScore, Anchor, MovingAverage, None};
    public TransformationType type;
    public int mAReadingsBack; 
    
    public Transformation(TransformationType type) {
        this.type = type;
        this.id = type.toString();
    }
    
    public Transformation(TransformationType type, int readingsBack) {
        this.type = type;
        this.id = type.toString();
        this.mAReadingsBack = readingsBack;
    }
}
