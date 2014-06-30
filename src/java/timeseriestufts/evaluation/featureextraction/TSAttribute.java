/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeseriestufts.evaluation.featureextraction;



/** An fNIRS attribute. But should generalize to many different classes*/
public abstract class TSAttribute extends Attribute{
    
    public int startIndex;
    public int endIndex;
    public int length;
    
    public AttributeType attributeType;
    public static enum AttributeType {CHANNEL, REGION, NOMINAL};
    
    
}
