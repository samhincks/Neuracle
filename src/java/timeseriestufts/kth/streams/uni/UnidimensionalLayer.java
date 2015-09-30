/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeseriestufts.kth.streams.uni;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import org.apache.commons.math.stat.regression.SimpleRegression;
import timeseriestufts.kth.streams.DataLayer;
import timeseriestufts.kth.streams.DataLayer;

/** A one dimensional DataLayer, holding a collection of points
 * Extended by Channel
 * @author Sam Hincks
 */
public abstract class UnidimensionalLayer extends DataLayer {
    protected float[] data; //.. save here to maximize space
    protected ArrayList<Float> data2;//.. save here to maximize time when synchronized;
    public boolean synchedWithDatabase = false; //.. if this true, we use data2
    public int numPoints =0;
   
    public float[] getData() {
        return data;
    }
    
    public ArrayList<Float> getData2(){
        return data2;
    }
    
    
    /**Add a discrete point, a float*/
    public void addPoint(float p) {
        if (!(synchedWithDatabase))
            data[numPoints] = p;
        else data2.add(p);
        numPoints++;
    }
    
   
    public void setPoint(int index, float val) throws Exception{
        if (index > numPoints) throw new Exception("Point at " + index + " has not yet been set");
        if (!(synchedWithDatabase))
            data[index] = val;
        else data2.set(index, val);
    }
    
    /**Get total number of data points in this channel 
     * @return itemCount = the total number of data points
    */
    @Override
    public int getCount() {
      return numPoints;      
    }
    
    
    public void popFirstX(int x) throws Exception {
        if (synchedWithDatabase) { //.. this is the situation when this sucks... 
            float[] data3 = new float[data.length-x];
            for (int i=x; i < data.length; i++) {
                data3[i-x] = data[x];
            }
            data = data3;
        }
        else {
            for (int i =0; i <x; i++) {
                data2.remove(0);
            } 
        }
        
    }
    
     /** Returns the last point in the data. Throws an exception if it does not exist
     * @return Point
     * @throws Exception 
     */
    public float getLastPoint() throws Exception {
        if (numPoints ==0) {
            throw new Exception("No data added yet"); 
        }
        if (!(synchedWithDatabase))
            return data[numPoints-1];  
        else return data2.get(numPoints-1);
    }
    
    /** Returns the first point in the data. Throws an exception if it does not exist
     * @return Point
     * @throws Exception 
     */
    public float getFirstPoint() throws Exception {
        if (numPoints ==0) {
            throw new Exception("No data added yet"); 
        }
        if (!(synchedWithDatabase))
            return data[0];     
        else
            return data2.get(0);
    }
    
    public float getPoint(int index) throws Exception {
        try {
            if (!(synchedWithDatabase))
              return data[index];
            else
                return data2.get(index);
        }
        catch (Exception e) {throw new Exception(index + " does not exist. ");}
    }
    
    public Float getPointOrNull(int index) {
         try {
             if (!(synchedWithDatabase)){
                return data[index];
             }
             else { 
                 return data2.get(index);
             }
        }
        catch (Exception e) {return null;}
    }
 
    /** Returns the sum of values in data
     * @return sum
     */
    @Override
    public double getSum() {
        double total =0;
        for (int i = 0; i < numPoints; i++) {
            total += getPointOrNull(i);
        }
        return total;
    }
    
    /**Returns the mean of the values in data
     * @return mean
     */
    @Override    
    public double getMean() {
        return getSum() / numPoints;
    }
   
    /**Returns the value in the 50th percentile of te data
     * @return median
     */
    @Override
    public  double getMedian() throws Exception {
        throw new Exception("Not yet implemented");
    }
    
    /**Returns the standard deviation of values in data
     * @return standard deviation 
     */
    @Override
    public  double getStdDev() throws Exception{
        if (numPoints == 0 ) return 0;
        float mean = (float)this.getMean();
        float total =0;
        for (int i = 0; i < numPoints; i++) {
            float thisVal = getPointOrNull(i);
            float minusMean = thisVal - mean; 
            float minusMeanSquared = minusMean * minusMean;
            total += minusMeanSquared;
        }
        float averageMMSquared = total / numPoints;
        double stddev= Math.sqrt(averageMMSquared);
        return stddev;
    }
    
    /** Returns the standard error of the mean
     * @return the standard error 
     */
    @Override
    public  double getStdErr() throws Exception{
        //.. standard error = standard deviation / squareRoot(N)
        return getStdDev() / (Math.sqrt(getCount()));
    }
   
    /**Returns the lowest value in the data
     * @return the lowest value in the data
     */
    @Override
    public double getMin() throws Exception{
        float smallest = getPointOrNull(0);
        for (int i = 0; i < numPoints; i++) {
            float thisVal = getPointOrNull(i);
            if (thisVal < smallest) smallest = thisVal;
        }
        return smallest;
    }
    
    /**Returns max value
     * @return the highest value in data
     */
    @Override
    public double getMax() {
        float largest = getPointOrNull(0);
        for (int i = 0; i < numPoints; i++) {
            float thisVal = getPointOrNull(i);
            if (thisVal > largest) largest = thisVal;
        }
        return largest;
    }
    
    /** Returns the mode, the most frequently occurring value. Home made function,
     * store value occurrences in a hashmap, then return the key with the highest value.
     * @return the most frequently occurring value
     */
    @Override
    public double getMode() {
        HashMap<Float, Integer> allNums = new HashMap();
        //.. Count occurences of the values
        for (int i =0; i< numPoints; i++) {
            Float thisVal = getPointOrNull(i);
            //.. If we have not encountered this value yet, initialize to zero
            if (!allNums.containsKey(thisVal))
                allNums.put(thisVal, 0);
            
            //.. otherwise, increment that value by one
            else
                allNums.put(thisVal, allNums.get(thisVal) +1);  
        }
       // System.err.println("VERIFY THIS WORKS!");
        return getMostFrequentInHashMap(allNums);        
    }
    
    /** Return the key with the highest Integer value
     * @param map,
     * @return the key with highest Integer value
     */
    private double getMostFrequentInHashMap(HashMap<Float, Integer> map) {
        Iterator itr = map.entrySet().iterator();
        
        //.. set these to the highest in the map
        int highest =0;
        double highestKey =0.0;
        
        //.. Find the highest Integer value
        while (itr.hasNext()) {
            Entry<Double, Integer> entry = (Entry)itr.next();
            
            //.. set counters if this is the highest value
            if (entry.getValue() > highest) {
                highest = entry.getValue();
                highestKey = entry.getKey();
            }
        }
        return highestKey;
    }
    
       
    /** Prints the data along with its timestamp.      * 
     */
    @Override
    public void printStream() {
        int incr =1;
        System.out.println("----------------");
        for (int i = 0; i < numPoints; i+=incr) {
            System.out.println(getPointOrNull(i));
        }        
       System.out.println("----------------");
    }

    
    @Override
    public int getChannelCount() {
        return numPoints;
    }
    
    public int getLayerSize() {
        return numPoints;
    }
    
    /**Extracts the slope between the first and last point of this layer. 
     */
    public double getSlope() throws Exception{
        float firstPoint = this.getFirstPoint();
        float lastPoint = this.getLastPoint();
        
        return getSlope(firstPoint, lastPoint, numPoints);
    }
    
    /**Get Slope between specified points*/
    public double getSlope(float firstPoint, float lastPoint, int distance) throws Exception {
        double deltaX = distance;
        if (deltaX ==0) throw new Exception("There is no difference as distance = " + distance);
       
        double deltaY = lastPoint - firstPoint;
        double slope = deltaY/deltaX;

        return slope;
    }
      public double getBestFitIntercept() {
        SimpleRegression regression = new SimpleRegression();
                
        for (int i = 0; i < numPoints; i++) {
            regression.addData(i, getPointOrNull(i));
        }
        return regression.getIntercept();
    }
      
    public double getBestFit() {
        SimpleRegression regression = new SimpleRegression();
                
        for (int i = 0; i < numPoints; i++) {
            regression.addData(i, getPointOrNull(i));
        }
        
        return regression.getSlope();
    }
    
    /**Return second derivative between start and end.
     */
    public double getSecondDerivative() throws Exception{
        if (numPoints < 2) return 0;
        double sumOfSlopeOfSlope =0;
        double lastSlope =getSlope(getPointOrNull(0), getPointOrNull(1),1);
        for (int i=1; i < numPoints-1; i++) {
            //.. get 1st derivative between 2 points
            float current = getPointOrNull(i);
            float next =getPointOrNull(i+1);
            
            //.. calculate slope bewteen this point and the ntext
            double slope = getSlope(current, next,1);
           
            
            double slopeOfSlope = getSlope((float)lastSlope, (float)slope, 1);
            sumOfSlopeOfSlope += slopeOfSlope;
            
            lastSlope = slope;
        }
       // System.err.println("VERIFY THIS WORKS");
        return sumOfSlopeOfSlope / numPoints;
    }
    
    /**Returns point with largest value*/ 
    public Float getPeakPoint () {
        Float peak = getPointOrNull(0);
        for (int i =0; i <numPoints; i++) {
            Float p = getPointOrNull(i);
            if (p > peak || peak ==null){
                peak = p;
            }
        }
        return peak;
    }
    /**Returns the index of the largest value.*/
    public int getPeakIndex() {
        int peakIndex =0;
        float peak =getPointOrNull(0);
        for (int i =0; i <numPoints; i++) {
            Float p = getPointOrNull(i);
            if (p > peak){
                peak = p;
                peakIndex =i;
            }
        }
        return peakIndex;
        
    }

    
    /**Returns the distance between the two points that most closely
     approximate half the peak on its left and right side*/
    public double getFullWidthAtHalfMaximum() throws Exception{
        //.. Get Peak, and calcualte half of peak
        int peakIndex = getPeakIndex();
        float peakPoint = getPeakPoint();
        float halfPeak = peakPoint/2.0f;

         int leftHalfIndex = this.findClosestToVal(halfPeak, 0, peakIndex);
        int rightHalfIndex = this.findClosestToVal(halfPeak, peakIndex, numPoints);
        return rightHalfIndex - leftHalfIndex;
    }
    
    /**Returns the Index closest to the value between start and end*/
    public int findClosestToVal(float val, int start, int end) throws Exception{
        if (end < start || end > numPoints) throw new Exception(end + " < " + start+ "|| " +end +" > " + numPoints);
        int retIndex = -1;
        double closestDistance =0;
        
        //.. find half peak on left side
        for (int i=start; i< end; i++) {
            float p = getPointOrNull(i);
            double distToHalf = Math.abs(val - p);
            if (retIndex == -1 || distToHalf <closestDistance){
                closestDistance = distToHalf;
                retIndex =i;
            } 
        }
        if (retIndex !=-1) return retIndex;
        return (start);
    }

    
    /**Return the mean between points from and upTo.
     Throw an exception if it is out of range*/
    public double getMean(int fromIndex, int upToIndex) throws Exception {
        if (upToIndex > numPoints) throw new Exception("Illegal argument");
        double total=0;
        for (int i=fromIndex; i < upToIndex+1; i++) {
             float p  = this.getPoint(i);
             total += p;
        }
        
        return total / (upToIndex -fromIndex +1);
    }
    
    /** Return the index of the point closest to the target float **/
    public int findIndexOf(float target) {
        double minDistance = this.getMax(); //.. you can never have a frequency larer than number of points
        int minIndex = -1;
        for (int i = 0; i < numPoints; i++) {
            float thisFreq = getPointOrNull(i);
            float thisDistance = Math.abs(thisFreq - target);
            if (thisDistance < minDistance) {
                minIndex = i;
                minDistance = thisDistance;
            }
        }
        return minIndex;
    }
    
    /** Return the index of the maximum value between specified values
     **/
    public int getIndexOfMaxBetween(int startIndex, int endIndex) {
        float largest = getPointOrNull(startIndex);
        int index =0;
        for (int i = startIndex; i < endIndex; i++) {
            float thisVal = getPointOrNull(i);
            if (thisVal > largest) {
                largest = thisVal;
                index =i;
            }
        }
        return index;
    }

    public boolean trim(int expectedLength) {
        if (data.length > expectedLength) {
           data = Arrays.copyOf(data, expectedLength);
           numPoints = data.length;
           return true;
        }
       
        return false;
    }
    
    public void delete() {
        data = null;
    }
    
}
