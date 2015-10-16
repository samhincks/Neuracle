package timeseriestufts.kth.streams.uni.adaptivefilters;

/**
 *
 * @author shincks
 */


/* File GraphIntfc01.java
 Copyright 2004, R.G.Baldwin
 Rev 5/14/04

 This interface must be implemented by classes
 whose objects produce data to be plotted by
 programs such as Graph03 and Graph06.

 Tested using SDK 1.4.2 under WinXP.
 ************************************************/
public interface GraphIntfc01 {

    public int getNmbr();

    public double f1(double x);

    public double f2(double x);

    public double f3(double x);

    public double f4(double x);

    public double f5(double x);
}//end GraphIntfc01
