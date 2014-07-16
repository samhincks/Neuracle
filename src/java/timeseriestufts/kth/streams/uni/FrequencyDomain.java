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
    public Channel magnitudeChannel;
    public Channel phaseChannel;
    public Channel frequencyChannel;
    public Complex [] complexData;
    public float sampleRate;
    
    public  FrequencyDomain(float sampleRate) {
         this.sampleRate = sampleRate;
     }
     
     
     /** First value is the highest frequency, which is N / 2T. N= # samples. T = # of seconds
      * Values are multiples of 1/T. 
      * f[i] = i * sampleRate / fftLength
      **/
     public  Triple [] complexToFreq(Complex [] complexData) throws Exception{
         int numReadings = complexData.length /2; //.. Inexplicably, after half the data points, the rest is just a copy. Don't think its related to padding with 0 
      //  freqDomain = new Triple[numReadings];
         magnitudeChannel = new Channel(numReadings);
         frequencyChannel = new Channel(numReadings);
         phaseChannel = new Channel(numReadings);

         for (int i=0; i < numReadings; i++){
             Complex c = complexData[i];
             double magnitude = Math.sqrt(c.getReal()*c.getReal() + c.getImaginary()*c.getImaginary());
             double phase = Math.atan(c.getReal() / c.getImaginary()); //. arctan(a/b). This is a simplification. its different based on quadrant
             double frequency = (i * (sampleRate / (double)complexData.length));
             // System.out.println(magnitude + " , " + phase + " , " + start +" , " + frequency + ", " + sampleRate + " , " + complexData.length + " , " +i);
             //freqDomain[i] = new Triple(magnitude,phase, frequency);
             magnitudeChannel.addPoint((float) magnitude);
             phaseChannel.addPoint((float) phase);
             frequencyChannel.addPoint((float)frequency);
         }
         
         magnitudeChannel.normalize(false);
         return freqDomain;
     }

    public double getMagAtFreq(int i) {
        return magnitudeChannel.getPointOrNull(i);
    }
    
    
    public double getPhaseAtFreq(int i) {
        return frequencyChannel.getPointOrNull(i);
    }
    
    public Float getAverageMagnitudeBetween(float startFrequency, float endFrequency) throws Exception{
        int startIndex = magnitudeChannel.findIndexOf(startFrequency);
        int endIndex =magnitudeChannel.findIndexOf(endFrequency);
        
        if (startIndex >= endIndex) throw new Exception("No data between " + startFrequency + " and " + endFrequency);
        System.out.println(startIndex + " , " + endIndex);
        float total =0;
        for (int i= startIndex; i < endIndex; i++) {
            total+= magnitudeChannel.getPointOrNull(i);
        }
        return total / (endIndex -startIndex);
    }
    
   
    
    public void printComplex() {
        int i = 0;
        for (Complex c : complexData) {
            Double real = c.getReal();
            Double imaginary = c.getImaginary();

            System.out.print(i + ", ");
            if (real < 0.00001) {
                System.out.print(0);
            } else {
                System.out.print(real);
            }
            System.out.print(",");
            if (imaginary < 0.00001) {
                System.out.print(0);
            } else {
                System.out.print(imaginary);
            }
            System.out.println("");
            i++;
        }
    }
    public void print() {
       magnitudeChannel.printStream();
       // frequencyChannel.printStream();

    }
     
     private class Triple  {
         public double magnitude, phase, frequency;
         public Triple(double magnitude, double phase, double frequency) {
             this.magnitude = magnitude; this.phase = phase; this.frequency = frequency;
         }
         
         public void print(){
             System.out.println("magnitude: " + magnitude + " , phase: " + phase + " , frequency: " + frequency);
         }
         
     }
     public static void main(String[] args) {
        Channel.main(args);
    }
}
