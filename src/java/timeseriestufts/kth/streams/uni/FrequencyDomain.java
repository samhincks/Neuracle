/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeseriestufts.kth.streams.uni;

import org.apache.commons.math3.complex.Complex;

/**
 *
 * @author samhincks
 */
public class FrequencyDomain {
    public Triple [] freqDomain;
    public int sampleRate;
    
    public  FrequencyDomain(Complex[] complexData, int sampleRate) {
         this.sampleRate = sampleRate;
         complexToFreq(complexData);
     }
     
     
     /** First value is the highest frequency, which is N / 2T. N= # samples. T = # of seconds
      * Values are multiples of 1/T. 
      * f[i] = i * sampleRate / fftLength
      **/
     public  Triple [] complexToFreq(Complex [] complexData) {
         freqDomain = new Triple[complexData.length];
         int n = complexData.length;
         int t = n *sampleRate;
         double start = (double)n / (double)(2*t);
         double add = 1/(double)t;
         int i=0; 
         for (Complex c : complexData) {
             double magnitude = Math.sqrt(c.getReal()*c.getReal() + c.getImaginary()*c.getImaginary());
             double phase = Math.atan(c.getReal() / c.getImaginary()); //. arctan(a/b). This is a simplification. its different based on quadrant
             double frequency = (i * sampleRate) / (double)complexData.length;
             System.out.print(frequency+ ", ");
             // System.out.println(magnitude + " , " + phase + " , " + start +" , " + frequency + ", " + sampleRate + " , " + complexData.length + " , " +i);
             freqDomain[i] = new Triple(magnitude, phase, frequency);
             i++;
             start +=add;
         }
         System.out.println("");
         return freqDomain;
     }

    public double getMagAtFreq(int i) {
        return freqDomain[i].magnitude;
    }
    
    
    public double getPhaseAtFreq(int i) {
        return freqDomain[i].phase;
    }
     
     private class Triple  {
         public double magnitude, phase, frequency;
         public Triple(double magnitude, double phase, double frequency) {
             this.magnitude = magnitude; this.phase = phase; this.frequency = frequency;
         }
     }
}
