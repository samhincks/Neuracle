/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeseriestufts.kth.streams.uni;

import filereader.TSTuftsFileReader;
import filereader.experiments.AJExperiment;
import org.apache.commons.math3.complex.Complex;
import timeseriestufts.kth.streams.bi.ChannelSet;
import timeseriestufts.kth.streams.tri.Experiment;

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
    
    /** Return the pulse, the frequency of the maximum magnitude within a specified band,
     * ie where you'd expect the pulse. 
     * We hardcode the minimum and maximum possible pulses, say 30 and 180, which translates
     * to 0.5 hz and 3 hz. 
     * Before getting pulse, do a highpass filter at 0.75f and normalize the data
     **/
    public int getPulse() {
        float minPulse = 0.75f;
        float maxPulse = 3f;
        int startIndex = frequencyChannel.findIndexOf(minPulse);
        int endIndex =frequencyChannel.findIndexOf(maxPulse);
        
        //.. get the index with the maximum between these frequencies
        int maxMagIndex = magnitudeChannel.getIndexOfMaxBetween(startIndex, endIndex);
        
        float maxFrequency = frequencyChannel.getPointOrNull(maxMagIndex);
        
        //.. 30cycles = 0.5hz since 60*0.5 = 30, so cycles = freq*60
        int cycles = (int) (maxFrequency * 60);
        return cycles;
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
      // magnitudeChannel.printStream();
       frequencyChannel.printStream();
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
         try{
            String [] fnirsFiles = AJExperiment.getFiles(true);
            String [] hrFiles =AJExperiment.getFiles(false);
            float avgDif = 0;
            float avgDif2 =0;
            for (int k =0; k < fnirsFiles.length; k++){
                //System.out.println(fnirsFiles[k] + " , " + hrFiles[k]);

                TSTuftsFileReader f = new TSTuftsFileReader();
                ChannelSet cs = f.readData(",", fnirsFiles[k],1);
                 f = new TSTuftsFileReader();
                ChannelSet cs2 = f.readData(",", hrFiles[k],1);
                //System.out.println("xxxxxxxxxxzxzxxxxxxxxxxxx");
                //System.out.println("Now: " +fnirsFiles[k]);
                Experiment e = cs.splitByLabel("condition");
                Channel test = new Channel(16);
                for (int i =0; i < 15; i++){
                   // Channel b1 = e.matrixes.get(i).getChannel(0);
                    Channel b1 = cs.getChannel(i);
                    //b1 = b1.normalize(false);
                    b1 = b1.highpass(0.75f, false);
                    b1 = b1.normalize(false);
                    
                    //b1 = b1.movingAverage(2,false);
                    //System.out.print(e.matrixes.get(i).streams.get(3).data.length+",");
                    //b1.printStream();
                    //System.out.print(e.matrixes.get(i).condition+",");
                    FrequencyDomain fd = new FrequencyDomain(Channel.HitachiRPS);
                    fd.complexToFreq(b1.FFT());
                    fd.print();
                    
                    test.addPoint(fd.getPulse());
                    break;
                   // fd.print();
                // System.out.println("XXXXXXXXXXXXXXXXX");
                }
                
                System.out.println(test.getMean() + " , " + test.getStdDev() + 
                        " , " + cs2.getChannel(0).getMean() + " , " +(test.getMean() - cs2.getChannel(0).getMean()));
                
                avgDif +=Math.abs(test.getMean() - cs2.getChannel(0).getMean());
                avgDif2 += test.getMean() - cs2.getChannel(0).getMean();

            }
             System.out.println("-------");
             System.out.println((avgDif / 16.0));
             System.out.println((avgDif2 / 16.0));

         }
         catch(Exception e){e.printStackTrace();}
    }
}
