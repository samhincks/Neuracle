package timeseriestufts.kth.streams.uni.adaptivefilters;


/* File Dsp030.java
 Copyright 2004, R.G.Baldwin
 Rev 5/14/04

 Uses an FFT algorithm to compute and display the
 magnitude of the spectral content for up to five
 sinusoids having different frequencies and
 amplitudes.

 See the program named Dsp028 for a program that
 does not use an FFT algorithm.

 Gets input parameters from a file named
 Dsp030.txt.  If that file doesn't exist in the
 current directory, the program uses a set of
 default parameters.

 Each parameter value must be stored as characters
 on a separate line in the file named Dsp030.txt.
 The required parameters are as follows:

 Data length as type int
 Number of spectra as type int.  Max value is 5.
 List of sinusoid frequency values as type double.
 List of sinusoid amplitude values as type double.

 CAUTION: THE DATA LENGTH MUST BE A POWER OF TWO.
 OTHERWISE, THIS PROGRAM WILL FAIL TO RUN
 PROPERLY.

 The number of values in each of the lists must
 match the value for the number of spectra.

 Note:  All frequency values are specified as a
 double representing a fractional part of the
 sampling frequency.  For example, a value of 0.5
 specifies a frequency that is half the sampling
 frequency.

 Here is a set of sample parameter values.  Don't
 allow blank lines at the end of the data in the
 file.

 128.0
 5
 0.1
 0.2
 0.3
 0.4
 0.45
 60
 70
 80
 90
 100

 The plotting program that is used to plot the
 output data from this program requires that the
 program implement GraphIntfc01.  For example,
 the plotting program named Graph03 can be used
 to plot the data produced by this program.  When
 it is used, the usage information is:

 java Graph03 Dsp030

 A static method named transform belonging to the
 class named ForwardRealToComplexFFT01 is used to
 perform the actual spectral analysis.  The method
 named transform implements an FFT algorithm.  The
 FFT algorithm requires that the data length be
 a power of two.

 Tested using SDK 1.4.2 under WinXP.
 ************************************************/
import java.util.*;


import java.io.*;

class Dsp030 implements GraphIntfc01 {

    final double pi = Math.PI;//for simplification

    //Begin default parameters
    int len = 128;//data length
    int numberSpectra = 5;
    //Frequencies of the sinusoids
    double[] freq = {0.1, 0.2, 0.3, 0.4, 0.5};
    //Amplitudes of the sinusoids
    double[] amp = {60, 70, 80, 90, 100};
  //End default parameters

  //Following arrays will contain data that is
    // input to the spectral analysis process.
    double[] data1;
    double[] data2;
    double[] data3;
    double[] data4;
    double[] data5;

  //Following arrays receive information back
    // from the spectral analysis that is not used
    // in this program.
    double[] real;
    double[] imag;
    double[] angle;

  //Following arrays receive the magnitude
    // spectral information back from the spectral
    // analysis process.
    double[] magnitude1;
    double[] magnitude2;
    double[] magnitude3;
    double[] magnitude4;
    double[] magnitude5;

    public Dsp030() {//constructor

    //Get the parameters from a file named
        // Dsp030.txt.  Use the default parameters
        // if the file doesn't exist in the current
        // directory.
        if (new File("Dsp030.txt").exists()) {
            getParameters();
        }//end if

    //Note that this program always processes
        // five sinusoids, even if fewer than five
        // were requested as the input parameter
        // for numberSpectra.  In that case, the
        // extras are processed using default values
        // and simply ignored when the results are
        // plotted.
    //Create the raw data.  Note that the
        // argument for a sinusoid at half the
        // sampling frequency would be (2*pi*x*0.5).
        // This would represent one half cycle or pi
        // radians per sample.
        //First create empty array objects.
        double[] data1 = new double[len];
        double[] data2 = new double[len];
        double[] data3 = new double[len];
        double[] data4 = new double[len];
        double[] data5 = new double[len];
        //Now populate the array objects
        for (int n = 0; n < len; n++) {
            data1[n] = amp[0] * Math.cos(2 * pi * n * freq[0]);
            data2[n] = amp[1] * Math.cos(2 * pi * n * freq[1]);
            data3[n] = amp[2] * Math.cos(2 * pi * n * freq[2]);
            data4[n] = amp[3] * Math.cos(2 * pi * n * freq[3]);
            data5[n] = amp[4] * Math.cos(2 * pi * n * freq[4]);
        }//end for loop
        //Compute magnitude spectra of the raw data
        // and save it in output arrays.  Note that
        // the real, imag, and angle arrays are not
        // used later, so they are discarded each
        // time a new spectral analysis is performed.
        magnitude1 = new double[len];
        real = new double[len];
        imag = new double[len];
        angle = new double[len];
        ForwardRealToComplexFFT01.transform(data1,
                real, imag, angle, magnitude1);

        magnitude2 = new double[len];
        real = new double[len];
        imag = new double[len];
        angle = new double[len];
        ForwardRealToComplexFFT01.transform(data2,
                real, imag, angle, magnitude2);

        magnitude3 = new double[len];
        real = new double[len];
        imag = new double[len];
        angle = new double[len];
        ForwardRealToComplexFFT01.transform(data3,
                real, imag, angle, magnitude3);

        magnitude4 = new double[len];
        real = new double[len];
        imag = new double[len];
        angle = new double[len];
        ForwardRealToComplexFFT01.transform(data4,
                real, imag, angle, magnitude4);

        magnitude5 = new double[len];
        real = new double[len];
        imag = new double[len];
        angle = new double[len];
        ForwardRealToComplexFFT01.transform(data5,
                real, imag, angle, magnitude5);
    }//end constructor
    //-------------------------------------------//

  //This method gets processing parameters from
    // a file named Dsp030.txt and stores those
    // parameters in instance variables belonging
    // to the object of type Dsp030.
    void getParameters() {
        int cnt = 0;
    //Temporary holding area for strings.  Allow
        // space for a few blank lines at the end
        // of the data in the file.
        String[] data = new String[20];
        try {
            //Open an input stream.
            BufferedReader inData
                    = new BufferedReader(new FileReader(
                                    "Dsp030.txt"));
      //Read and save the strings from each of
            // the lines in the file.  Be careful to
            // avoid having blank lines at the end,
            // which may cause an ArrayIndexOutOfBounds
            // exception to be thrown.
            while ((data[cnt]
                    = inData.readLine()) != null) {
                cnt++;
            }//end while
            inData.close();
        } catch (IOException e) {
        }

    //Move the parameter values from the
        // temporary holding array into the instance
        // variables, converting from characters to
        // numeric values in the process.
        cnt = 0;
        len = (int) Double.parseDouble(data[cnt++]);
        numberSpectra = (int) Double.parseDouble(
                data[cnt++]);
        for (int fCnt = 0; fCnt < numberSpectra;
                fCnt++) {
            freq[fCnt] = Double.parseDouble(
                    data[cnt++]);
        }//end for loop
        for (int aCnt = 0; aCnt < numberSpectra;
                aCnt++) {
            amp[aCnt] = Double.parseDouble(
                    data[cnt++]);
        }//end for loop

        //Print parameter values.
        System.out.println();
        System.out.println("Data length: " + len);
        System.out.println(
                "Number spectra: " + numberSpectra);
        System.out.println("Frequencies");
        for (cnt = 0; cnt < numberSpectra; cnt++) {
            System.out.println(freq[cnt]);
        }//end for loop
        System.out.println("Amplitudes");
        for (cnt = 0; cnt < numberSpectra; cnt++) {
            System.out.println(amp[cnt]);
        }//end for loop

    }//end getParameters
    //-------------------------------------------//
    //The following six methods are required by the
    // interface named GraphIntfc01.  The plotting
    // program pulls the data values to be plotted
    // by invoking these methods.

    public int getNmbr() {
    //Return number of functions to
        // process.  Must not exceed 5.
        return numberSpectra;
    }//end getNmbr
    //-------------------------------------------//

    public double f1(double x) {
        int index = (int) Math.round(x);
        if (index < 0
                || index > magnitude1.length - 1) {
            return 0;
        } else {
            return magnitude1[index];
        }//end else
    }//end function
    //-------------------------------------------//

    public double f2(double x) {
        int index = (int) Math.round(x);
        if (index < 0
                || index > magnitude2.length - 1) {
            return 0;
        } else {
            return magnitude2[index];
        }//end else
    }//end function
    //-------------------------------------------//

    public double f3(double x) {
        int index = (int) Math.round(x);
        if (index < 0
                || index > magnitude3.length - 1) {
            return 0;
        } else {
            return magnitude3[index];
        }//end else
    }//end function
    //-------------------------------------------//

    public double f4(double x) {
        int index = (int) Math.round(x);
        if (index < 0
                || index > magnitude4.length - 1) {
            return 0;
        } else {
            return magnitude4[index];
        }//end else
    }//end function
    //-------------------------------------------//

    public double f5(double x) {
        int index = (int) Math.round(x);
        if (index < 0
                || index > magnitude5.length - 1) {
            return 0;
        } else {
            return magnitude5[index];
        }//end else
    }//end function
    //-------------------------------------------//

}//end class Dsp030
