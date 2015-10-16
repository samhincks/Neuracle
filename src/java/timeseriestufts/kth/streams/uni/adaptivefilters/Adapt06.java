package timeseriestufts.kth.streams.uni.adaptivefilters;

/*File Adapt06.java
 Copyright 2005, R.G.Baldwin

 The purpose of this program is to illustrate the use of the
 general-purpose LMS adaptive engine named AdaptEngine01.

 This is the simplest program that I was able to devise to 
 illustrate the use of the adaptive engine. This program 
 adaptively designs a convolution filter transforms a cosine
 function into a sine function having the same amplitude and
 frequency.

 There are no input parameters.  Just compile and run.

 The following time series are plotted in color showing the 
 convergence of the adaptive algorithm:

 black: input to the filter - the cosine function
 red: output from the filter
 blue: adaptive target - a sine function
 green: error

 The filter has two coefficients, each of which is 
 initialized to a value of 0.

 The final values of the two filter coefficients are
 listed on the command-line screen. For the sinusoidal
 frequencies used by the program, when the program is
 allowed to run for 1000 iterations, the values for the two 
 coefficients converge to:

 1.4142064344006575
 -0.9999927187203118

 You might recognize that is the square root of two and
 minus 1.  For other frequencies, the coefficients
 converge to different values.

 This program requires the following classes in addition
 to Adapt06:

 Adapt06.class
 AdaptEngine01.class
 AdaptiveResult.class
 PlotALot05.class

 Tested using J2SE 5.0 and WinXP. J2SE 5.0 or later is 
 required.
 **********************************************************/
import static java.lang.Math.*;//J2SE 5.0 req

public class Adapt06 {

    public static void main(String[] args) {

        double feedbackGain = 0.0002;
        int numberIterations = 1000;
        int filterLength = 2;

    //Instantiate an object of the class and execute the
        // adaptive algorithm using the specified feedbackGain.
        new Adapt06().process(feedbackGain,
                numberIterations,
                filterLength);
    }//end main
    //-----------------------------------------------------//

    void process(double feedbackGain,
            int numberIterations,
            int filterLength) {

    //Instantiate object of the adaptive engine to provide 
        //adaptive behavior for the program.
        AdaptEngine01 adapter
                = new AdaptEngine01(filterLength, feedbackGain);

    //Instantiate a plotting object for four data channels.
        // This object will be used to plot the time series
        // data.
        PlotALot05 plotObj = new PlotALot05(
                "Time Series", 460, 155, 25, 5, 4, 4);

        //Declare and initialize working variables.
        double output = 0;
        double err = 0;
        double target = 0;
        double input = 0;
        double dataScale = 20;//Default data scale

        //Do the iterative adaptive process
        AdaptiveResult result = null;
        for (int cnt = 0; cnt < numberIterations; cnt++) {
      //Generate the data to be filtered and the adaptive
            // target on the fly.
            input = dataScale * cos(2 * cnt * PI / 8);
            target = dataScale * sin(2 * cnt * PI / 8);

            //Execute the adaptive behavior.
            result = adapter.adapt(input, target);

      //Get the results of the adaptive behavior for
            // plotting.
            err = result.err;
            output = result.output;

            //Feed the time series data to the plotting object.
            plotObj.feedData(input, output, target, err);

        }//end for loop

        //Cause the data to be plotted.
        plotObj.plotData();

        //List the values of the filter coefficients.
        System.out.println("nFinal filter coefficients:");
        double[] filter = result.filterArray;
        for (int cnt = 0; cnt < filter.length; cnt++) {
            System.out.println(filter[cnt]);
        }//end for loop

    }//end process method
    //-----------------------------------------------------//

}//end class Adapt06
