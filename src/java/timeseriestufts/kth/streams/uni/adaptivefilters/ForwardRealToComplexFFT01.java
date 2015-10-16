package timeseriestufts.kth.streams.uni.adaptivefilters;

/**
 *
 * @author shincks
 */

/*File ForwardRealToComplexFFT01.java
 Copyright 2004, R.G.Baldwin
 Rev 5/14/04

 The static method named transform performs a real
 to complex Fourier transform using a crippled
 complex-to-complex FFT algorithm.  It is crippled
 in the sense that it is not being used to its
 full potential as a complex-to-complex forward
 or inverse FFT algorithm.

 See ForwardRealToComplex01 for a slower but more
 general approach that does not use an FFT
 algorithm.

 Returns real, imag, magnitude, and phase angle in
 degrees.

 Incoming parameters are:
 double[] data - incoming real data
 double[] realOut - outgoing real data
 double[] imagOut - outgoing imaginary data
 double[] angleOut - outgoing phase angle in
 degrees
 double[] magnitude - outgoing amplitude
 spectrum

 The magnitude is computed as the square root of
 the sum of the squares of the real and imaginary
 parts.  This value is divided by the incoming
 data length, which is given by data.length.

 CAUTION: THE INCOMING DATA LENGTH MUST BE A
 POWER OF TWO. OTHERWISE, THIS PROGRAM WILL FAIL
 TO RUN PROPERLY.

 Returns a number of points in the frequency
 domain equal to the incoming data length.  Those
 points are uniformly distributed between zero and
 one less than the sampling frequency.
 ************************************************/
public class ForwardRealToComplexFFT01 {

    public static void transform(
            double[] data,
            double[] realOut,
            double[] imagOut,
            double[] angleOut,
            double[] magnitude) {
        double pi = Math.PI;//for convenience
        int dataLen = data.length;
    //The complexToComplex FFT method does an
        // in-place transform causing the output
        // complex data to be stored in the arrays
        // containing the input complex data.
        // Therefore, it is necessary to copy the
        // input data to this method into the real
        // part of the complex data passed to the
        // complexToComplex method.
        System.arraycopy(data, 0, realOut, 0, dataLen);

    //Perform the spectral analysis.  The results
        // are stored in realOut and imagOut. The +1
        // causes it to be a forward transform. A -1
        // would cause it to be an inverse transform.
        complexToComplex(1, dataLen, realOut, imagOut);

    //Compute the magnitude and the phase angle
        // in degrees.
        for (int cnt = 0; cnt < dataLen; cnt++) {
            magnitude[cnt]
                    = (Math.sqrt(realOut[cnt] * realOut[cnt]
                            + imagOut[cnt] * imagOut[cnt])) / dataLen;

            if (imagOut[cnt] == 0.0
                    && realOut[cnt] == 0.0) {
                angleOut[cnt] = 0.0;
            }//end if
            else {
                angleOut[cnt] = Math.atan(
                        imagOut[cnt] / realOut[cnt]) * 180.0 / pi;
            }//end else

            if (realOut[cnt] < 0.0
                    && imagOut[cnt] == 0.0) {
                angleOut[cnt] = 180.0;
            } else if (realOut[cnt] < 0.0
                    && imagOut[cnt] == -0.0) {
                angleOut[cnt] = -180.0;
            } else if (realOut[cnt] < 0.0
                    && imagOut[cnt] > 0.0) {
                angleOut[cnt] += 180.0;
            } else if (realOut[cnt] < 0.0
                    && imagOut[cnt] < 0.0) {
                angleOut[cnt] += -180.0;
            }//end else

        }//end for loop

    }//end transform method
    //-------------------------------------------//

  //This method computes a complex-to-complex
    // FFT.  The value of sign must be 1 for a
    // forward FFT.
    public static void complexToComplex(
            int sign,
            int len,
            double real[],
            double imag[]) {
        double scale = 1.0;
    //Reorder the input data into reverse binary
        // order.
        int i, j;
        for (i = j = 0; i < len; ++i) {
            if (j >= i) {
                double tempr = real[j] * scale;
                double tempi = imag[j] * scale;
                real[j] = real[i] * scale;
                imag[j] = imag[i] * scale;
                real[i] = tempr;
                imag[i] = tempi;
            }//end if
            int m = len / 2;
            while (m >= 1 && j >= m) {
                j -= m;
                m /= 2;
            }//end while loop
            j += m;
        }//end for loop

        //Input data has been reordered.
        int stage = 0;
        int maxSpectraForStage, stepSize;
    //Loop once for each stage in the spectral
        // recombination process.
        for (maxSpectraForStage = 1, stepSize = 2 * maxSpectraForStage;
                maxSpectraForStage < len;
                maxSpectraForStage = stepSize, stepSize = 2 * maxSpectraForStage) {
            double deltaAngle
                    = sign * Math.PI / maxSpectraForStage;
            //Loop once for each individual spectra
            for (int spectraCnt = 0;
                    spectraCnt < maxSpectraForStage;
                    ++spectraCnt) {
                double angle = spectraCnt * deltaAngle;
                double realCorrection = Math.cos(angle);
                double imagCorrection = Math.sin(angle);

                int right = 0;
                for (int left = spectraCnt;
                        left < len; left += stepSize) {
                    right = left + maxSpectraForStage;
                    double tempReal
                            = realCorrection * real[right]
                            - imagCorrection * imag[right];
                    double tempImag
                            = realCorrection * imag[right]
                            + imagCorrection * real[right];
                    real[right] = real[left] - tempReal;
                    imag[right] = imag[left] - tempImag;
                    real[left] += tempReal;
                    imag[left] += tempImag;
                }//end for loop
            }//end for loop for individual spectra
            maxSpectraForStage = stepSize;
        }//end for loop for stages
    }//end complexToComplex method

}//end class ForwardRealToComplexFFT01
