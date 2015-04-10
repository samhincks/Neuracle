/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeseriestufts.kth.streams.tri;

import timeseriestufts.kth.streams.DataLayer;
import java.util.ArrayList;
import timeseriestufts.kth.streams.bi.BidimensionalLayer;
import timeseriestufts.kth.streams.uni.UnidimensionalLayer;

/** A Collection of ChannelSets, 3D organizaiton. 
 * @author Samuel Hincks
 */
public abstract class TridimensionalLayer<T extends BidimensionalLayer> extends DataLayer {
    public ArrayList<T> matrixes; //.. 3D organization, array of 2D objects
    protected UnidimensionalLayer unidimension; //.. 1D Organization (for stats)
    
    
    public void addMatrix(T t) {
        if (matrixes == null) matrixes = new ArrayList();
        matrixes.add(t);
    }
    
    /**Returns the total number of points in the 1D channel with most points
     **/
    public int getMostPointsInAChannel() {
        int largest =0;
        for (BidimensionalLayer bd : matrixes) {
            int maxPoints = bd.maxPoints();
            if (maxPoints > largest) largest = maxPoints;
        }
        return largest;
    }
    
   
    /** Returns the total number of points
     * 
     * @return total number of points
     */
    @Override
    public int getCount() {
        int sum =0;
        for (BidimensionalLayer bd : matrixes) {
            sum += bd.getCount();
        }
        return sum;
    }
    
   /**Returns sum of all values
    * @return sum
    */
    @Override
    public double getSum() {
        int sum = 0;
        return 1;
    }
   
    /**Returns the mean of the values in data
     * @return mean
     */
    @Override   
    public double getMean() {
        return 1;
    }
    
    /**Returns the value in the 50th percentile of te data
     * @return median
     */
    @Override
    public  double getMedian() {
        return 1;
    }
    
     /**Returns the standard deviation of values in data
     * @return standard deviation 
     */
    @Override
    public  double getStdDev() {
        return 1;
    }   
    
    /** Returns the standard error of the mean
     * @return the standard error 
     */
    @Override
    public  double getStdErr() {
        return 1;
    }
    
    /**Returns the lowest value in the data
     * @return the lowest value in the data
     */
    @Override
    public double getMin() {
        return 1;
    }   
   
    /**Returns max value
     * @return the highest value in data
     */
    @Override
    public double getMax() {
        return 1;
    }
    
    /**Returns the mode, the most frequently occurring value.
     * @return mode
     */
    @Override
    public double getMode() {
        return 1;
    }
    
    @Override
    public int getChannelCount() {
        return matrixes.get(0).streams.size();
    }
    
    
    @Override
   public  int getLayerSize() {
       return matrixes.size();
   }
   
   public  void delete() {
       for (BidimensionalLayer bd : matrixes) {
           bd.delete();
       }
       matrixes = null;
   }
}
