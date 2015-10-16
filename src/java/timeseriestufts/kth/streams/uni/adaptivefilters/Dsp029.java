package timeseriestufts.kth.streams.uni.adaptivefilters;

/**
 *
 * @author shincks
 */

/* File Dsp029.java
 Copyright 2004, R.G.Baldwin
 Rev 5/6/04

 Generates and displays up to five sinusoids
 having different frequencies and amplitudes. Very
 useful for providing a visual illustration of the
 way in which frequencies above half the sampling
 frequency fold back down into the area bounded
 by zero and half the sampling frequency (the
 Nyquist folding frequency).

 Gets input parameters from a file named
 Dsp029.txt.  If that file doesn't exist in the
 current directory, the program uses a set of
 default parameters.

 Each parameter value must be stored as characters
 on a separate line in the file named Dsp029.txt.
 The required parameters are as follows:

 Data length as type int
 Number of sinusoids as type int.  Max value is 5.
 List of sinusoid frequency values as type double.
 List of sinusoid amplitude values as type double.

 The number of values in each of the lists must
 match the value for the number of spectra.

 Note:  All frequency values are specified as a
 double representing a fractional part of the
 sampling frequency.

 Here is a set of sample parameter values.  Don't
 allow blank lines at the end of the data in the
 file.

 400.0
 5
 0.1
 0.9
 1.1
 1.9
 2.1
 90
 90
 90
 90
 90

 The plotting program that is used to plot the
 output data from this program requires that the
 program implement GraphIntfc01.  For example,
 the plotting program named Graph06 can be used
 to plot the data produced by this program.  When
 it is used, the usage information is:

 java Graph06 Dsp029

 Tested using SDK 1.4.2 under WinXP.
 ************************************************/
import java.util.*;
import java.io.*;



class Dsp029 implements GraphIntfc01 {

    final double pi = Math.PI;//for simplification

    //Begin default parameters
    int len = 400;//data length
    int numberSinusoids = 5;
    //Frequencies of the sinusoids
    double[] freq = {0.1, 0.25, 0.5, 0.75, 0.9};
    //Amplitudes of the sinusoids
    double[] amp = {75, 75, 75, 75, 75};
  //End default parameters

  //Following arrays will be populated with
    // sinusoidal data to be plotted
    double[] data1 = new double[len];
    double[] data2 = new double[len];
    double[] data3 = new double[len];
    double[] data4 = new double[len];
    double[] data5 = new double[len];

    public Dsp029() {//constructor

    //Get the parameters from a file named
        // Dsp029.txt.  Use the default parameters
        // if the file doesn't exist in the current
        // directory.
        if (new File("Dsp029.txt").exists()) {
            getParameters();
        }//end if

    //Note that this program always generates
        // five sinusoids, even if fewer than five
        // were requested as the input parameter
        // for numberSinusoids.  In that case, the
        // extras are generated using default values
        // and simply ignored when the results are
        // plotted.
    //Create the raw data.  Note that the
        // argument for a sinusoid at half the
        // sampling frequency would be (2*pi*x*0.5).
        // This would represent one half cycle or pi
        // radians per sample.
        for (int n = 0; n < len; n++) {
            data1[n] = amp[0] * Math.cos(2 * pi * n * freq[0]);
            data2[n] = amp[1] * Math.cos(2 * pi * n * freq[1]);
            data3[n] = amp[2] * Math.cos(2 * pi * n * freq[2]);
            data4[n] = amp[3] * Math.cos(2 * pi * n * freq[3]);
            data5[n] = amp[4] * Math.cos(2 * pi * n * freq[4]);
        }//end for loop

    }//end constructor
    //-------------------------------------------//

  //This method gets processing parameters from
    // a file named Dsp029.txt and stores those
    // parameters in instance variables belonging
    // to the object of type Dsp029.
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
                                    "Dsp029.txt"));
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
        numberSinusoids = (int) Double.parseDouble(
                data[cnt++]);
        for (int fCnt = 0; fCnt < numberSinusoids;
                fCnt++) {
            freq[fCnt] = Double.parseDouble(
                    data[cnt++]);
        }//end for loop

        for (int aCnt = 0; aCnt < numberSinusoids;
                aCnt++) {
            amp[aCnt] = Double.parseDouble(
                    data[cnt++]);
        }//end for loop

        //Print parameter values.
        System.out.println();
        System.out.println("Data length: " + len);
        System.out.println(
                "Number sinusoids: " + numberSinusoids);
        System.out.println("Frequencies");
        for (cnt = 0; cnt < numberSinusoids; cnt++) {
            System.out.println(freq[cnt]);
        }//end for loop
        System.out.println("Amplitudes");
        for (cnt = 0; cnt < numberSinusoids; cnt++) {
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
        return numberSinusoids;
    }//end getNmbr
    //-------------------------------------------//

    public double f1(double x) {
        int index = (int) Math.round(x);
        if (index < 0
                || index > data1.length - 1) {
            return 0;
        } else {
            return data1[index];
        }//end else
    }//end function
    //-------------------------------------------//

    public double f2(double x) {
        int index = (int) Math.round(x);
        if (index < 0
                || index > data2.length - 1) {
            return 0;
        } else {
            return data2[index];
        }//end else
    }//end function
    //-------------------------------------------//

    public double f3(double x) {
        int index = (int) Math.round(x);
        if (index < 0
                || index > data3.length - 1) {
            return 0;
        } else {
            return data3[index];
        }//end else
    }//end function
    //-------------------------------------------//

    public double f4(double x) {
        int index = (int) Math.round(x);
        if (index < 0
                || index > data4.length - 1) {
            return 0;
        } else {
            return data4[index];
        }//end else
    }//end function
    //-------------------------------------------//

    public double f5(double x) {
        int index = (int) Math.round(x);
        if (index < 0
                || index > data5.length - 1) {
            return 0;
        } else {
            return data5[index];
        }//end else
    }//end function
    //-------------------------------------------//

}//end class Dsp029
