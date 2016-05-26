package timeseriestufts.kth.streams.uni.adaptivefilters;

/**
 *
 * @author shincks
 */
/*File Adapt01.java.java
 Copyright 2005, R.G.Baldwin
 This program illustrates one aspect of time-
 adaptive signal processing.
 Two sampled time series, chanA and chanB,
 are presented to the an adaptive algorithm. Each
 time series contains the same wide band signal
 plus white noise that is uncorrelated between the
 two channels.
 The signal in chanB may be delayed or advanced by
 up to 6 samples relative to the signal in chanA.
 A 9-point convolution operator is developed
 adaptively.  When the adaptive process converges
 successflly, the time series produced by applying
 the convolution operator to chanA matches the
 signal on chanB.
 The user provides the following information as
 command line parameters:
 timeShift - A negative value delays chanB
 relative to chanA and a positive value advances
 chanB relative to chanA.  If no command line
 parameters are provided, a default timeShift
 value of -4 is used.  This causes a four-sample
 delay on chanB relative to chanA.  Because the
 convolution operator has only nine points, time
 shifts greater than plus or minus four samples
 cannot be resolved and an adaptive solution will
 not be found.  Time shifts greater than six
 samples cause the program to terminate.
 feedbackGain - Controls the convergence rate of
 the adaptive process.  If the value is very low,
 the process will take a long time to converge. 
 If the value is too high, the process will become
 unstable.  If no command line parameters are
 provided, a feedbackGain value of 0.001 is used.
 Depending on the random noise level, the process
 appears to be stable for feedbackGain values as
 large as 0.004, but goes unstable for a
 feedbackGain value of 0.005.
 noiseLevel - Controls the amount of uncorrelated
 white noise that is added to the signal on each
 of the channels.  If no command line parameters
 are provided, the default noise level is 0.0  The
 noise level is provided as a decimal fraction of
 the signal level.  For example, a noise level
 of 0.1 causes the level of the noise that is
 added to each of the channels to be one tenth of
 the signal level on that channel.
 numberIterations - The number of adaptive
 iterations performed before the adaptive process
 terminates and all of the data that has been
 saved is plotted.  If no command line parameters
 are provided, the default is 100 iterations.
 The following time series are plotted in color
 showing the convergence of the adaptive
 algorithm:
 black: input to the filter
 red: output from the filter
 blue: adaptive target
 green: error
 In addition, the frequency response of the filter
 at the beginning and at the end of every tenth
 iteration is computed and displayed when the
 adaptive process terminates.  Both the amplitude
 and the phase response of the filter are computed
 and plotted.  Also, the filter is plotted as a
 time series on the same iterations that the
 frequency response is computed.  Thus, the shape
 of the filter can be compared with the frequency
 response of the filter.
 The filter is initialized with a single
 coefficient value of 1 at the center and 0 for
 all of the other coefficient values.  The ideal
 solution is a single coefficient value of 1 at a
 location in the filter that matches the time
 shift between chanA and the target.  The value
 of 1 can be seen to progress from the center of
 the filter to the correct location in the filter
 as the program iterates.  In addition, the phase
 response can be seen to change appropriately as
 the program iterates.
 Tested using J2SE 5.0 and WinXP
 J2SE 5.0 or later is required.
 ************************************************/
import static java.lang.Math.*;

public class Adapt01 {

    public static void main(String[] args) {
        //Default values
        int timeShift = -4;
        double feedbackGain = 0.001;
        double noiseLevel = 0.0;
        int numberIterations = 100;

        if (args.length != 4) {
            System.out.println(
                    "Usage: java Adapt01 "
                    + "timeShift feedbackGain "
                    + "noiseLevel numberIterations");
            System.out.println(
                    "Negative timeShift is delay");
            System.out.println(
                    "Using -4 sample shift by default");
            System.out.println(
                    "Using 0.001 feedbackGain by default");
            System.out.println(
                    "noiseLevel is a decimal fraction");
            System.out.println("Using 0.0 by default");
            System.out.println(
                    "numberIterations is an int");
            System.out.println("Using 100 by default");
        } else {//Command line params were provided.
            //Convert String to int
            timeShift = Integer.parseInt(args[0]);
            System.out.println(
                    "timeShift: " + timeShift);
            //Convert String to double
            feedbackGain = Double.parseDouble(args[1]);
            System.out.println(
                    "feedbackGain: " + feedbackGain);
            //Convert String to double
            noiseLevel = Double.parseDouble(args[2]);
            System.out.println(
                    "noiseLevel: " + noiseLevel);
            //Convert String to int
            numberIterations
                    = Integer.parseInt(args[3]);
            System.out.println(
                    "numberIterations: " + numberIterations);
        }//end else

        if (abs(timeShift) > 6) {
            System.out.println(
                    "Time shift magnitude > 6 not allowed.");
            System.out.println("Terminating");
            System.exit(0);
        }//end if

    //Instantiate an object of the class and
        // execute the adaptive algorithm using the
        // specified feedbackGain and other
        // parameters.
        new Adapt01().process(timeShift,
                feedbackGain,
                noiseLevel,
                numberIterations);
    }//end main
    //-------------------------------------------//

    void process(int timeShift,
            double feedbackGain,
            double noiseLevel,
            int numberIterations) {
    //The process begins with a filter having
        // the following initial coefficients.
        double[] filter = {0, 0, 0, 0, 1, 0, 0, 0, 0};

    //Create array objects that will be used as
        // delay lines.
        double[] rawData = new double[13];
        double[] chanA = new double[9];
        double[] chanB = new double[9];

    //Instantiate a plotting object for four
        // data channels.  This object will be used
        // to plot the time series data.
        PlotALot05 plotObj = new PlotALot05(
                "Time Series", 398, 250, 25, 5, 4, 4);

    //Instantiate a plotting object for two
        // channels of filter frequency response
        // data.  One channel is used to plot the
        // amplitude response in db and the other
        // channel is used to plot the phase on a
        // scale that extends from -180 degrees to
        // +180 degrees.
        PlotALot03 freqPlotObj= new PlotALot03("Freq", 264, 487, 20, 2, 0, 0);
    //Instantiate a plotting object to display
        // the filter as a short time series at
        // intervals during the adaptive  process.
        // Note that the minimum allowable width
        // for a Frame is 112 pixels under WinXP.
        // Therefore, the following display doesn't
        // synchronize properly for filter lengths
        // less than 25 coefficients.  However, the
        // code that feeds the filter data to the
        // plotting object later in the program
        // extends the length of the filter to
        // cause it to synchronize and to plot one
        // set of filter coefficients on each axis.
        PlotALot01 filterPlotObj = new PlotALot01(
                "Filter", (filter.length * 4) + 8,
                487, 40, 4, 0, 0);

    //Display frequency response of initial
        // filter computed at 128 points between zero
        // and the folding frequency.
        displayFreqResponse(filter,
                freqPlotObj,
                128,
                filter.length - 5);

    //Display the initial filter as a time series
        // on the first axis.
        for (int cnt = 0; cnt < filter.length; cnt++) {
            filterPlotObj.feedData(30 * filter[cnt]);
        }//end for loop
        //Extend the filter with a value of 2.5 for
        // plotting to cause it to synchronize
        // properly with the plotting software.  See
        // earlier comment on this topic.  Note that
        // this will not cause the plot to
        // synchronize properly on an operating
        // system for which the sum of the left and
        // right insets on a Frame object are
        // different from 8 pixels.
        if (filter.length <= 26) {
            for (int cnt = 0; cnt < (26 - filter.length);
                    cnt++) {
                filterPlotObj.feedData(2.5);
            }//end for loop
        }//end if

    //Declare and initialize variables used in
        // the adaptive process.
        double output = 0;
        double err = 0;
        double target = 0;
        double input = 0;
        double dataScale = 25;//Default data scale

        //Do the iterative adaptive process
        for (int cnt = 0; cnt < numberIterations;
                cnt++) {
      //Add new input data to the delay line
            // containing the raw input data.
            flowLine(rawData, Math.random() - 0.5);

      //Extract the middle sample from the input
            // data delay line, add some random noise,
            // and insert it into the delay line
            // containing the data for chanA.
            flowLine(chanA, dataScale * rawData[6]
                    + noiseLevel * dataScale * (Math.random()
                    - 0.5));

      //Extract data with a time shift from the
            // input data delay line, add some random
            // noise, and insert it into the delay line
            // containing the data for chanB.
            flowLine(chanB,
                    dataScale * rawData[6 + timeShift]
                    + noiseLevel * dataScale * (Math.random()
                    - 0.5));
      //Get the middle sample from the chanA
            // delay line for plotting.
            input = chanA[chanA.length / 2];

      //Apply the current filter coefficients to
            // the chanA data contained in the delay
            // line.
            output = dotProduct(filter, chanA);

      //Get the middle sample from the chanB
            // delay line and use it as the adaptive
            // target.  In other words, the adaptive
            // process will attempt to cause the
            // filtered output to match the value in
            // the middle of the chanB delay line.
            target = chanB[chanB.length / 2];

      //Compute the error between the current
            // filter output and the target.
            err = output - target;

            //Update the filter coefficients
            for (int ctr = 0; ctr < filter.length; ctr++) {
                filter[ctr]
                        -= err * chanA[ctr] * feedbackGain;
            }//end for loop
            //This is the end of the adaptive process.
            // The code beyond this point is used to
            // display information about the adaptive
            // process.
            //Feed the time series data to the plotting
            // object.
            plotObj.feedData(input, output, target, err);

      //Compute and plot the frequency response
            // and plot the filter as a time series
            // every 10 iterations.
            if (cnt % 10 == 0) {
                displayFreqResponse(filter,
                        freqPlotObj,
                        128,
                        filter.length - 5);

        //Plot the filter coefficient values.
                // Scale the coefficient values by 30
                // to make them compatible with the
                // plotting software.
                for (int ctr = 0; ctr < filter.length;
                        ctr++) {
                    filterPlotObj.feedData(30 * filter[ctr]);
                }//end for loop
                //Extend the filter with a value of 2.5
                // for plotting to cause it to
                // synchronize with one filter on each
                // axis.  See explanatory comment
                // earlier.
                if (filter.length <= 26) {
                    for (int count = 0;
                            count < (26 - filter.length);
                            count++) {
                        filterPlotObj.feedData(2.5);
                    }//end for loop
                }//end if
            }//end if on cnt%10

        }//end for loop

    //Cause all the data to be plotted in the
        // screen locations specified.
        plotObj.plotData();
        freqPlotObj.plotData(0, 201);
        filterPlotObj.plotData(265, 201);

    }//end process
    //-------------------------------------------//

  //This method simulates a tapped delay line.
    // It receives a reference to an array and
    // a value.  It discards the value at
    // index 0 of the array, moves all the other
    // values by one element toward 0, and
    // inserts the new value at the top of the
    // array.
    void flowLine(double[] line, double val) {
        for (int cnt = 0; cnt < (line.length - 1);
                cnt++) {
            line[cnt] = line[cnt + 1];
        }//end for loop
        line[line.length - 1] = val;
    }//end flowLine
    //-------------------------------------------//

  //This method receives two arrays and treats
    // the first n elements in each array as a pair
    // of vectors.  It computes and returns the
    // vector dot product of the two vectors.  If
    // the length of one array is greater than the
    // length of the other array, it considers the
    // number of dimensions of the vectors to be
    // equal to the length of the smaller array.
    double dotProduct(double[] v1, double[] v2) {
        double result = 0;
        if ((v1.length) <= (v2.length)) {
            for (int cnt = 0; cnt < v1.length; cnt++) {
                result += v1[cnt] * v2[cnt];
            }//end for loop
            return result;
        } else {
            for (int cnt = 0; cnt < v2.length; cnt++) {
                result += v1[cnt] * v2[cnt];
            }//end for loop
            return result;
        }//end else
    }//end dotProduct
    //-------------------------------------------//

  //This method receives a reference to a double
    // array containing a convolution filter
    // along with a reference to a plotting object
    // capable of plotting two channels of data.
    // It also receives a value specifying the
    // number of frequencies at which a DFT is
    // to be performed on the filter, along with
    // the sample number that represents the zero
    // time location in the filter.  The method
    // uses this information to perform a DFT on
    // the filter from zero to the folding
    // frequency.  It feeds the amplitude spectrum
    // and the phase spectrum to the plotting
    // object for plotting.
    void displayFreqResponse(double[] filter,
            PlotALot03 plot,
            int len,
            int zeroTime) {
    //Create the arrays required by the Fourier
        // Transform.
        double[] timeDataIn = new double[len];
        double[] realSpect = new double[len];
        double[] imagSpect = new double[len];
        double[] angle = new double[len];
        double[] magnitude = new double[len];

        //Copy the filter into the timeDataIn array.
        System.arraycopy(filter, 0, timeDataIn, 0,
                filter.length);
    //Compute DFT of the filter from zero to the
        // folding frequency and save it in the
        // output arrays.
        ForwardRealToComplex01.transform(timeDataIn,
                realSpect,
                imagSpect,
                angle,
                magnitude,
                zeroTime,
                0.0,
                0.5);

    //Plot the magnitude data.  Convert to
        // normalized decibels before plotting.
        //Eliminate or change all values that are
        // incompatible with log10 method.
        for (int cnt = 0; cnt < magnitude.length;
                cnt++) {
            if ((magnitude[cnt] == Double.NaN)
                    || (magnitude[cnt] <= 0)) {
                magnitude[cnt] = 0.0000001;
            } else if (magnitude[cnt]
                    == Double.POSITIVE_INFINITY) {
                magnitude[cnt] = 9999999999.0;
            }//end else if
        }//end for loop

        //Now convert magnitude data to log base 10
        for (int cnt = 0; cnt < magnitude.length;
                cnt++) {
            magnitude[cnt] = log10(magnitude[cnt]);
        }//end for loop

    //Note that from this point forward, all
        // references to magnitude are referring to
        // log base 10 data, which can be thought of
        // as scaled decibels.
        //Find the absolute peak value
        double peak = -9999999999.0;
        for (int cnt = 0; cnt < magnitude.length;
                cnt++) {
            if (peak < abs(magnitude[cnt])) {
                peak = abs(magnitude[cnt]);
            }//end if
        }//end for loop
        //Normalize to 50 times the peak value and
        // shift up the screen by 50 units to make
        // the values compatible with the plotting
        // program.  Recall that adding a constant to
        // log values is equivalent to scaling the
        // original data.
        for (int cnt = 0; cnt < magnitude.length;
                cnt++) {
            magnitude[cnt]
                    = 50 * magnitude[cnt] / peak + 50;
        }//end for loop
        //Now feed the normalized decibel data to the
        // plotting object.  The angle data ranges
        // from -180 to +180.  Scale it down by a
        // factor of 20 to make it compatible with
        // the plotting format being used.
        for (int cnt = 0; cnt < magnitude.length;
                cnt++) {
            plot.feedData(
                    magnitude[cnt], angle[cnt] / 20);
        }//end for loop

    }//end displayFreqResponse
    //-------------------------------------------//
}//end class Adapt01

