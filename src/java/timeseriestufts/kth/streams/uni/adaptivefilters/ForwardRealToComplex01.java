package timeseriestufts.kth.streams.uni.adaptivefilters;

/*File ForwardRealToComplex01.java
Copyright 2004, R.G.Baldwin
Rev 5/14/04

The static method named transform performs a real
to complex Fourier transform.

Does not implement the FFT algorithm. Implements
a straight-forward sampled-data version of the
continuous Fourier transform defined using
integral calculus.  See ForwardRealToComplexFFT01
for an FFT algorithm.

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
  int zero - the index of the incoming data
    sample that represents zero time
  double lowF - Low freq limit as fraction of
    sampling frequency
  double highF - High freq limit as fraction of
    sampling frequency

The frequency increment is the difference between
high and low limits divided by the length of
the magnitude array

The magnitude is computed as the square root of
the sum of the squares of the real and imaginary
parts.  This value is divided by the incoming
data length, which is given by data.length.

Returns a number of points in the frequency
domain equal to the incoming data length
regardless of the high and low frequency
limits.
************************************************/

public class ForwardRealToComplex01{

  public static void transform(
                              double[] data,
                              double[] realOut,
                              double[] imagOut,
                              double[] angleOut,
                              double[] magnitude,
                              int zero,
                              double lowF,
                              double highF){
    double pi = Math.PI;//for convenience
    int dataLen = data.length;
    double delF = (highF-lowF)/data.length;
    //Outer loop iterates on frequency
    // values.
    for(int i=0; i < dataLen;i++){
      double freq = lowF + i*delF;
      double real = 0.0;
      double imag = 0.0;
      double ang = 0.0;
      //Inner loop iterates on time-
      // series points.
      for(int j=0; j < dataLen; j++){
        real += data[j]*Math.cos(
                             2*pi*freq*(j-zero));
        imag += data[j]*Math.sin(
                             2*pi*freq*(j-zero));
      }//end inner loop
      realOut[i] = real/dataLen;
      imagOut[i] = imag/dataLen;
      magnitude[i] = (Math.sqrt(
                 real*real + imag*imag))/dataLen;

      //Calculate and return the phase
      // angle in degrees.
      if(imag == 0.0 && real == 0.0){ang = 0.0;}
      else{ang = Math.atan(imag/real)*180.0/pi;}

      if(real < 0.0 && imag == 0.0){ang = 180.0;}
      else if(real < 0.0 && imag == -0.0){
                                   ang = -180.0;}
      else if(real < 0.0 && imag > 0.0){
                                   ang += 180.0;}
      else if(real < 0.0 && imag < 0.0){
                                  ang += -180.0;}
      angleOut[i] = ang;
    }//end outer loop
  }//end transform method

}//end class ForwardRealToComplex01