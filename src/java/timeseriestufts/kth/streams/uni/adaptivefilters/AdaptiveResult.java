package timeseriestufts.kth.streams.uni.adaptivefilters;


//This class is used to encapsulate the adaptive results
// into an object for return to the calling method.
public class AdaptiveResult {

    public double[] filterArray;
    public double output;
    public double err;

    //Constructor
    public AdaptiveResult(double[] filterArray,
            double output,
            double err) {
        this.filterArray = filterArray;
        this.output = output;
        this.err = err;
    }//end constructor
}//end class AdaptiveResult
//=======================================================//