/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeseriestufts.kth.streams.bi;

import java.util.ArrayList;
import timeseriestufts.kth.streams.DataLayer;
import timeseriestufts.kth.streams.uni.Channel;
import timeseriestufts.kth.streams.uni.UnidimensionalLayer;
import timeseriestufts.kth.streams.tri.Experiment;

/** A collection of UnidimensionalLayers 
 * @author Sam Hincks
 */
public abstract class BidimensionalLayer<T extends UnidimensionalLayer> extends DataLayer{    
    public ArrayList<T> streams; //.. 2D Organization    
   
    
    /**Returns the index # of the channel with input id. If the id is a number and
     we don't have a channel with this id, then return the number itself*/
    public int getChannelIndexById(String id) throws Exception{
        for(int i =0; i< streams.size(); i++ ) {
            UnidimensionalLayer u = streams.get(i);
            if(u.id.equals(id))
                return i;
        }
        try { //.. try to parse it as integer and return the index itself
           return Integer.parseInt(id);  
        }
        catch(Exception n){
           throw new Exception(this.id + " does not have a channel with id " +id );
        }

    }
    
    /**Get a channel by its ID.  */
    public T getChannelById(String channel) throws Exception{
        return streams.get(this.getChannelIndexById(channel));
    }
    
    /**General stream adding method (overridable in subclasses)
     * @param stream 
     */
    public void addStream(T stream) {
        if (streams == null) streams = new ArrayList();
        streams.add(stream);
    }
    
    
    public String [] getColumnNames() {
        String [] retVals = new String[streams.size()];
        int index =0;
        for (UnidimensionalLayer u : streams) {
            retVals[index] = u.id;
            index++;
        }
        return retVals;
    }
  
 
    
    /** Returns the total number of points
     * 
     * @return total number of points
     */
    @Override
    public int getCount() {
        int total =0;
        for (T u : streams){
            total += u.numPoints;
        }
        return total;
    }
    
   /**Returns sum of all values
    * @return sum
    */
    @Override
    public double getSum() {
       int total =0;
        for (T u : streams){
            total += u.getSum();
        }
        return total;
    }
   
    /**Returns the mean of the values in data
     * @return mean
     */
    @Override   
    public double getMean() {
        if (this.getCount() == 0) return -1;
        int total =0;
        for (T u : streams){
            total += u.getMean();
        }
        return total/ this.getCount();
    }
    
    @Override
    public double getMedian(){
<<<<<<< Updated upstream
       // System.err.println("Not built");
=======
>>>>>>> Stashed changes
        return -1;
    }
     /**Returns the standard deviation of values in data
     * @return standard deviation 
     */
    @Override
    public  double getStdDev() {
<<<<<<< Updated upstream
     //   System.err.println("Not built");
=======
>>>>>>> Stashed changes
        return -1;
    }   
    
    /** Returns the standard error of the mean
     * @return the standard error 
     */
    @Override
    public  double getStdErr() {
<<<<<<< Updated upstream
       // System.err.println("Not built");
=======
>>>>>>> Stashed changes
        return -1;
    }
    
    /**Returns the lowest value in the data
     * @return the lowest value in the data
     */
    @Override
    public double getMin() {
<<<<<<< Updated upstream
      //  System.err.println("Not built");
=======
>>>>>>> Stashed changes
        return -1;
    }   
   
    /**Returns max value
     * @return the highest value in data
     */
    @Override
    public double getMax() {
<<<<<<< Updated upstream
       // System.err.println("Not built");
=======
>>>>>>> Stashed changes
        return -1;
    }
    
    /**Returns the mode, the most frequently occurring value.
     * @return mode
     */
    @Override
    public double getMode() {
<<<<<<< Updated upstream
       // System.err.println("Not built");
=======
>>>>>>> Stashed changes
        return -1;      
    }
    
     /**Prints a each of the unidimensions
      */ 
    @Override
    public void printStream() {
        System.out.println("Printing stream for each channel");
        for (UnidimensionalLayer c : streams) {
            System.out.println(c.id + " has " + c.numPoints);
            c.printStream();
            System.out.println(".    ...    ....    ...    .");
        }
    }
    
    
    
    @Override
    public int getChannelCount() {
        return streams.size();
    }
    @Override
    public int getLayerSize() {
        return streams.size();
    }
    
    
    /*Returns the total number of points in the largest unidimensionallayers*/
    public int maxPoints() {
        int max =-1;
        for (UnidimensionalLayer u : streams) {
            if (u.getCount() > max) max = u.getCount();
        }
        return max;
    }

    /** Returns the number of points in the unilayer with least points
     **/
    public int minPoints() {
        int min = this.streams.get(0).getCount();
        for (UnidimensionalLayer u : streams) {
            if (u.getCount() < min) {
                min = u.getCount();
            }
        }
        return min;
    }
    
    public int getNumPointsAtZero() throws Exception{
        if (streams.size() ==0) throw new Exception();
        return streams.get(0).numPoints;
        
    }
    
    /**Append the float value to specified stream**/
    public void appendToStream(int index, float value) {
        UnidimensionalLayer u = streams.get(index);
        u.addPoint(value);
    }
    
    
}
