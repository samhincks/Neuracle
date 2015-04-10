/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeseriestufts.kth.streams.quad;

import java.util.ArrayList;
import timeseriestufts.kth.streams.DataLayer;
import timeseriestufts.kth.streams.bi.BidimensionalLayer;
import timeseriestufts.kth.streams.uni.UnidimensionalLayer;
import timeseriestufts.kth.streams.tri.TridimensionalLayer;

/**
 *
 * @author samhincks
 */
public class QuaddimensionalLayer extends DataLayer{
     public ArrayList<TridimensionalLayer> piles; //.. 3D organization, array of 2D objects
    protected UnidimensionalLayer unidimension; //.. 1D Organization (for stats)
  
    /** Returns the total number of points
     * 
     * @return total number of points
     */
    @Override
    public int getCount() {
        System.err.println("Not yet implemented");
        return 1;
    }
    
   /**Returns sum of all values
    * @return sum
    */
    @Override
    public double getSum() {
        System.err.println("Not yet implemented");
        return 1;
    }
   
    /**Returns the mean of the values in data
     * @return mean
     */
    @Override   
    public double getMean() {
        System.err.println("Not yet implemented");
        return 1;
    }
    
    /**Returns the value in the 50th percentile of te data
     * @return median
     */
    @Override
    public  double getMedian() {
        System.err.println("Not yet implemented");
        return 1;
    }
    
     /**Returns the standard deviation of values in data
     * @return standard deviation 
     */
    @Override
    public  double getStdDev() {
        System.err.println("Not yet implemented");
        return 1;
    }   
    
    /** Returns the standard error of the mean
     * @return the standard error 
     */
    @Override
    public  double getStdErr() {
        System.err.println("Not yet implemented");
        return 1;
    }
    
    /**Returns the lowest value in the data
     * @return the lowest value in the data
     */
    @Override
    public double getMin() {
        System.err.println("Not yet implemented");
        return 1;
    }   
   
    /**Returns max value
     * @return the highest value in data
     */
    @Override
    public double getMax() {
        System.err.println("Not yet implemented");
        return 1;
    }
    
    /**Returns the mode, the most frequently occurring value.
     * @return mode
     */
    @Override
    public double getMode() {
        System.err.println("Not yet implemented");
        return 1;
    }
    
    @Override
    public int getChannelCount() {
        return piles.get(0).getChannelCount();
    }
    
    
    @Override
   public  int getLayerSize() {
       return piles.size();
   }
    
    public void printStream() {
        for(TridimensionalLayer t : piles) {
            t.printStream();
            System.out.println("****************************");
        }
    }
    
    public void delete() {
        for (TridimensionalLayer tri : piles) {
            tri.delete();
        }
        piles = null;
    }
}
