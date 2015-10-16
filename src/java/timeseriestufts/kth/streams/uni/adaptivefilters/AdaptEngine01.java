package timeseriestufts.kth.streams.uni.adaptivefilters;

/*File AdaptEngine01.java
 Copyright 2005, R.G.Baldwin

 General purpose LMS adaptive algorithm.

 An object of this class is a general purpose adaptive 
 engine that implements the classical LMS adaptive 
 algorithm.

 The adaptive algorithm is implemented by the instance 
 method belonging to the object named adapt.

 Each time the adapt method is called, it receives one 
 sample from each of two different time series.  One time 
 series is considered to be the data that is to be filtered.
 The other time series is considered to be a target.

 The purpose of the adapt method is to adaptively create a 
 convolution filter which, when applied to the data time 
 series, will transform it into the target time series.

 Each time the method is called, it performs a dot product 
 between the current version of the filter and the contents 
 of a delay line in which historical data samples have been 
 saved. The result of that dot product is compared with the 
 target sample to produce an error value. The error value is
 produced by subtracting the value of the target sample from
 the result of the dot product. The error value is then used
 in a classical LMS adaptive algorithm to adjust the filter 
 coefficients.

 The objective is to produce a set of filter coefficients 
 that will drive the error to zero over time.

 This adaptive engine can be used as the solution to a 
 variety of different signal processing problems, depending 
 on the selection of time series that are provided as data 
 and target.

 The constructor for the class receives two parameters:
 filterLength
 feedbackGain

 The filter length is used to construct two arrays.  One 
 array is used later to contain the filter coefficients.

 The other array is used later as a tapped delay line to 
 contain the historical data samples and to precess them by
 one element each time the method is called.

 The feedback gain is used in the LMS adaptive algorithm to
 compute the new filter coefficients.

 Tested using J2SE 5.0 and WinXP.
 **********************************************************/
public class AdaptEngine01 {

    double[] filterArray;//filter coefficients stored here
    double[] dataArray;//historical data is stored here
    double feedbackGain;//used in LMS adaptive algorithm

    //Constructor
    public AdaptEngine01(int filterLength,
            double feedbackGain) {
        //Construct the two arrays and save the feedback gain.
        filterArray = new double[filterLength];
        dataArray = new double[filterLength];
        this.feedbackGain = feedbackGain;
    }//end constructor
    //-----------------------------------------------------//

  //This method implements a classical LMS adaptive
    // algorithm to create and to apply a convolution filter.
    // The filter output, the error, and a reference to the
    // array containing the filter coefficients are 
    // encapsulated in an object of type AdaptiveResult and
    // returned to the calling method.
    public AdaptiveResult adapt(double rawData, double target) {

    //Insert the incoming data value into the data delay
        // line.
        flowLine(dataArray, rawData);
        //System.out.println(dataArray.length + " , " +feedbackGain + " , " + filterArray.length);
        //Apply the current filter coefficients to the data.
        double output = dotProduct(filterArray, dataArray);
        //Compute the error.
        double err = output - target;

        //Use the error to update the filter coefficients.
        for (int ctr = 0; ctr < filterArray.length; ctr++) {
            filterArray[ctr] -= err * dataArray[ctr] * feedbackGain;
        }//end for loop.

    //Construct and return an object containing the filter
        // output, the error, and a reference to the array
        // object containing the current filter coefficients.
        return new AdaptiveResult(filterArray, output, err);
    }//end adapt
    //-----------------------------------------------------//

  //This method simulates a tapped delay line. It receives
    // a reference to an array and a value.  It discards the
    // value at index 0 of the array, moves all the other
    // values by one element toward 0, and inserts the new
    // value at the top of the array.
    void flowLine(double[] line, double val) {
        for (int cnt = 0; cnt < (line.length - 1); cnt++) {
            line[cnt] = line[cnt + 1];
        }//end for loop
        line[line.length - 1] = val;
    }//end flowLine
    //-----------------------------------------------------//

  //This method receives two arrays and treats the first N 
    // elements in each of the two arrays as a pair of
    // vectors.  It computes and returns the vector dot
    // product of the two vectors.  If the length of one
    // array is greater than the length of the other array,
    // it considers the number of dimensions of the vectors
    // to be equal to the length of the smaller array.
    double dotProduct(double[] v1, double[] v2) {
        double result = 0;
        if ((v1.length) <= (v2.length)) {
            for (int cnt = 0; cnt < v1.length; cnt++) {
                result += v1[cnt] * v2[cnt];
            }//emd for loop
            return result;
        } else {
            for (int cnt = 0; cnt < v2.length; cnt++) {
                result += v1[cnt] * v2[cnt];
            }//emd for loop
            return result;
        }//end else
    }//end dotProduct
    //-----------------------------------------------------//
}//end class AdaptEngine01
//=======================================================//

