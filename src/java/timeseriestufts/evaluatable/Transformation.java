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
    public static enum TransformationType{zscore, anchor, movingaverage, calcoxy, highpass, lowpass, bandpass, bwbandpass, none, averagedcalcoxy, subtract};
    public TransformationType type;
    public float [] params;
    public boolean for3D =false;
    
    /**If initialized from console, first parameter is command name
     * @param parameters 
     */
    public Transformation(String [] parameters) {
        params = new float[parameters.length-1];
        type = TransformationType.valueOf(parameters[0]);
        for (int i =1; i < parameters.length; i++) {
            params[i-1] = Float.parseFloat(parameters[i]);
        }
    }
    public Transformation(String name, String [] parameters) {
        params = new float[parameters.length];
        type = TransformationType.valueOf(name);
        int index =0;
        for (String s : parameters) {
            params[index] = Float.parseFloat(s);
            index++;
        }
        
    }
    public Transformation(TransformationType type) {
        this.type = type;
        this.id = type.toString();
    }
  
    
    public Transformation(TransformationType type, float a) {
        this.type = type;
        this.id = type.toString();
        params = new float[1];

        params[0] =a;
    }
    public Transformation(TransformationType type, float a , float b) {
        this.type = type;
        this.id = type.toString();
        params = new float[1];
        params[0] = a;
        params[1] = b;
    }
   
}
