package timeseriestufts.kth.streams.uni.adaptivefilters;

/*File PlotALot04.java 
 Copyright 2005, R.G.Baldwin
 This program is an update to the program named 
 PlotALot03.  This program is designed to plot 
 large amounts of time-series data for three 
 channels on separate horizontal axes.  One set
 of data is plotted using the color black.  The 
 second set of data is plotted using the color 
 red.  The third set of data is plotted using the
 color blue.
 See PlotALot03 for a class that plots two
 channels of data in black and red on alternating
 axes.
 See PlotALot02 for a class that plots two 
 channels of data in black and red superimposed on
 the same axes.  
 See PlotALot01.java for a one-channel program.
 The class provides a main method so that the
 class can be run as an application to test
 itself.
 There are three steps involved in the use of this
 class for plotting time series data:
 1. Instantiate a plotting object of type 
 PlotALot04 using one of two overloaded 
 constructors.
 2. Feed triplets of data values that are to be 
 plotted to the plotting object by invoking the
 feedData method once for each triplet of data 
 values.  The first value in the triplet will 
 be plotted in black on one axis.  The second 
 value in the triplet will be plotted in red on
 an axis below that axis.  The third value in
 the triplet will be plotted in blue on an axis
 below that one.
 3. Invoke one of two overloaded plotData methods 
 on the plotting object once all of the data 
 has been fed to the object.  This causes all
 of the data to be plotted.
   
 A using program can instantiate as many plotting 
 objects as are needed to plot all of the
 different triplets of data that need to be 
 plotted.  Each plotting object can be used to 
 plot as many triplts of data values as need be 
 plotted until the program runs out of available 
 memory.
 The plotting object of type PlotALot04 owns one 
 or more Page objects that extend the Frame class.
 The plotting object can own as many Page objects 
 as are necessary to plot all of the triplets of 
 data that are fed to that plotting object.
 The program produces a graphic output consisting 
 of a stack of Page objects on the screen, with 
 the data plotted on a Canvas object contained by 
 the Page object.  The Page showing the earliest 
 data is on the top of the stack and the Page 
 showing the latest data is on the bottom of the 
 stack.  The Page objects on the top of the stack 
 must be physically moved in order to see the 
 Page objects on the bottom of the stack.
 Each Page object contains three or more 
 horizontal axes on which the data is plotted. The
 program will terminate if the number of axes on 
 the page is not evenly divisable by 3.
 The three time series are plotted on separate 
 axes with the data from one time series being 
 plotted in black on one axis, the data from 
 the second time series being plotted in red on 
 the axis below that axis, and the data from the
 third time series being plotted in blue on the
 axis below that axis.
 The earliest data is plotted on the three axes 
 nearest the top of the Page moving from left to 
 right across the page.  Positive data values
 are plotted above the axis and negative values 
 are plotted below the axis.  When the right end 
 of an axis is reached, the next data value is 
 plotted on the left end of the third axis  
 below it skipping two axes in the process.  When 
 the right end of the last triplet of axes on the 
 Page is reached, a new Page object is created and
 the next triplet of data values are plotted at 
 the left end of the top three axes on that new 
 Page object.
 A mentioned above, there are two overloaded 
 versions of the constructor for the PlotALot04
 class. One overloaded version accepts several 
 incoming parameters allowing the user to control
 various aspects of the plotting format. A second 
 overloaded version accepts a title string only 
 and sets all of the plotting parameters to 
 default values. You can easily modify these
 default values and recompile the class if you
 prefer different default values.
 The parameters for the version of the constructor
 that accepts plotting format information are:
 String title: Title for the Frame object. This
 title is concatenated with the page number and 
 the result appears in the banner at the top of 
 the Frame.
 int frameWidth:The Frame width in pixels.
 int frameHeight: The Frame height in pixels.
 int traceSpacing: Distance between trace axes in
 pixels.
 int sampSpace: Number of pixels dedicated to each
 data sample in pixels per sample.  Must be 1 or
 greater.
 int ovalWidth: Width of an oval that is used to 
 mark each sample value on the plot.
 int ovalHeight: Height of an oval that is used to
 mark each sample value on the plot.
 For test purposes, the main method instantiates a
 single plotting object and feeds three time 
 series to that plotting object.  Plotting 
 parameters are specified for the plotting object 
 by using the overloaded version of the 
 constructor that accepts plotting parameters.
 The data that is fed to the plotting object is 
 white random noise. One of the time series is 
 the sequence of values obtained from a random 
 number generator.  The other two time series are
 the same as the first.  Thus, the triplets of 
 black, red, and blue time series that are plotted
 should have the same shape making it easy to 
 confirm that the process of plotting the three 
 time series is behaving the same in all three
 cases.
 Fifteen of the data values for each time series 
 are not random.  Seven of the values for each of 
 the time series are set to values of 0,0,25,-25,
 25,0,0.  This is done to confirm the proper 
 transition from the end of one page to the
 beginning of the next page.
 In addition, eight of the values for each time 
 series are set to 0,0,20,20,-20,-20,0,0.  This 
 is done in order to confirm the proper transition
 from one trace to the next trace on the same 
 page.
 These specific values and the locations in the 
 data where they are placed provide visible 
 confirmation that the transitions mentioned above
 are handled correctly. Note, however that these 
 are the correct locations for an AWT Frame object
 under WinXP. A Frame may have different inset 
 values under other operating systems, which may 
 cause these specific locations to be incorrect 
 for that operating system.  In that case, the 
 values will be plotted but they won't confirm 
 the proper transition.
 The following information about the plotting 
 parameters is displayed on the command line 
 screen when the class is used for plotting.  The 
 values shown below result from the execution of 
 the main method of the class for test purposes.
 Title: A
 Frame width: 158
 Frame height: 270
 Page width: 150
 Page height: 243
 Trace spacing: 36
 Sample spacing: 5
 Traces per page: 6
 Samples per page: 60
 There are two overloaded versions of the plotData
 method. One version allows the user to specify 
 the location on the screen where the stack of 
 plotted pages will appear. This version requires 
 two parameters, which are coordinate values in 
 pixels.  The first parameter specifies the 
 horizontal coordinate of the upper left corner of
 the stack of pages relative to the upper left 
 corner of the screen.  The second parameter 
 specifies the vertical coordinate of the upper 
 left corner of the stack of pages relative to the
 upper left corner of the screen. Specifying 
 coordinate values of 0,0 causes the stack to be 
 located in the upper left corner of the screen.  
 The other overloaded version of plotData places 
 the stack of pages in the upper left corner of 
 the screen by default.  The main method in this 
 class uses the second version causing the stack 
 of pages to appear in the upper left corner of 
 the screen by default.
 
 Each page has a WindowListener that will 
 terminate the program if the user clicks the 
 close button on the Frame.
 The program was tested using J2SE 5.0 and WinXP.
 Requires J2SE 5.0 to support generics.
 ************************************************/
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class PlotALot04 {
  //This main method is provided so that the
    // class can be run as an application to test
    // itself.

    public static void main(String[] args) {
    //Instantiate a plotting object using the
        // version of the constructor that allows for
        // controlling the plotting parameters.
        PlotALot04 plotObjectA
                = new PlotALot04("A", 158, 270, 36, 5, 4, 4);

    //Feed triplets of data values to the 
        // plotting object.
        for (int cnt = 0; cnt < 115; cnt++) {
      //Plot some white random noise. Note that
            // fifteen of the values for each time
            // series are not random.  See the opening
            // comments for a discussion of the reasons
            // why.
            double valBlack = (Math.random() - 0.5) * 25;
            double valRed = valBlack;
            double valBlue = valBlack;
      //Feed triplets of values to the plotting
            // object by invoking the feedData method
            // once for each triplet of data values.
            if (cnt == 57) {
                plotObjectA.feedData(0, 0, 0);
            } else if (cnt == 58) {
                plotObjectA.feedData(0, 0, 0);
            } else if (cnt == 59) {
                plotObjectA.feedData(25, 25, 25);
            } else if (cnt == 60) {
                plotObjectA.feedData(-25, -25, -25);
            } else if (cnt == 61) {
                plotObjectA.feedData(25, 25, 25);
            } else if (cnt == 62) {
                plotObjectA.feedData(0, 0, 0);
            } else if (cnt == 63) {
                plotObjectA.feedData(0, 0, 0);
            } else if (cnt == 26) {
                plotObjectA.feedData(0, 0, 0);
            } else if (cnt == 27) {
                plotObjectA.feedData(0, 0, 0);
            } else if (cnt == 28) {
                plotObjectA.feedData(20, 20, 20);
            } else if (cnt == 29) {
                plotObjectA.feedData(20, 20, 20);
            } else if (cnt == 30) {
                plotObjectA.feedData(-20, -20, -20);
            } else if (cnt == 31) {
                plotObjectA.feedData(-20, -20, -20);
            } else if (cnt == 32) {
                plotObjectA.feedData(0, 0, 0);
            } else if (cnt == 33) {
                plotObjectA.feedData(0, 0, 0);
            } else {
                plotObjectA.feedData(valBlack,
                        valRed,
                        valBlue);
            }//end else
        }//end for loop
        //Cause the data to be plotted in the default
        // screen location.
        plotObjectA.plotData();
    }//end main
    //-------------------------------------------//
    String title;
    int frameWidth;
    int frameHeight;
    int traceSpacing;//pixels between traces
    int sampSpacing;//pixels between samples
    int ovalWidth;//width of sample marking oval
    int ovalHeight;//height of sample marking oval

    int tracesPerPage;
    int samplesPerPage;
    int pageCounter = 0;
    int sampleCounter = 0;
    ArrayList<Page> pageLinks
            = new ArrayList<Page>();

  //There are two overloaded versions of the
    // constructor for this class.  This
    // overloaded version accepts several incoming
    // parameters allowing the user to control
    // various aspects of the plotting format. A
    // different overloaded version accepts a title
    // string only and sets all of the plotting
    // parameters to default values.
    PlotALot04(String title,//Frame title
            int frameWidth,//in pixels
            int frameHeight,//in pixels
            int traceSpacing,//in pixels
            int sampSpace,//in pixels per sample
            int ovalWidth,//sample marker width
            int ovalHeight)//sample marker hite
    {//constructor
        //Specify sampSpace as pixels per sample.
        // Should never be less than 1.  Convert to
        // pixels between samples for purposes of
        // computation.
        this.title = title;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.traceSpacing = traceSpacing;
        //Convert to pixels between samples.
        this.sampSpacing = sampSpace - 1;
        this.ovalWidth = ovalWidth;
        this.ovalHeight = ovalHeight;
    //The following object is instantiated solely
        // to provide information about the width and
        // height of the canvas. This information is
        // used to compute a variety of other
        // important values.
        Page tempPage = new Page(title);
        int canvasWidth = tempPage.canvas.getWidth();
        int canvasHeight
                = tempPage.canvas.getHeight();
    //Display information about this plotting
        // object.
        System.out.println("\nTitle: " + title);
        System.out.println(
                "Frame width: " + tempPage.getWidth());
        System.out.println(
                "Frame height: " + tempPage.getHeight());
        System.out.println(
                "Page width: " + canvasWidth);
        System.out.println(
                "Page height: " + canvasHeight);
        System.out.println(
                "Trace spacing: " + traceSpacing);
        System.out.println(
                "Sample spacing: " + (sampSpacing + 1));
        if (sampSpacing < 0) {
            System.out.println("Terminating");
            System.exit(0);
        }//end if
        //Get rid of this temporary page.
        tempPage.dispose();
        //Now compute the remaining important values.
        tracesPerPage
                = (canvasHeight - traceSpacing / 2)
                / traceSpacing;
        System.out.println("Traces per page: "
                + tracesPerPage);
        if ((tracesPerPage == 0)
                || (tracesPerPage % 3 != 0)) {
            System.out.println("Terminating program");
            System.exit(0);
        }//end if
        samplesPerPage = canvasWidth * tracesPerPage
                / (sampSpacing + 1) / 3;
        System.out.println("Samples per page: "
                + samplesPerPage);
    //Now instantiate the first usable Page
        // object and store its reference in the
        // list.
        pageLinks.add(new Page(title));
    }//end constructor
    //-------------------------------------------//

    PlotALot04(String title) {
    //Invoke the other overloaded constructor
        // passing default values for all but the
        // title.
        this(title, 400, 410, 50, 2, 2, 2);
    }//end overloaded constructor
    //-------------------------------------------//

  //Invoke this method once for each triplet of 
    // data values to be plotted.
    void feedData(double valBlack,
            double valRed,
            double valBlue) {
        if ((sampleCounter) == samplesPerPage) {
      //if the page is full, increment the page
            // counter, create a new empty page, and
            // reset the sample counter.
            pageCounter++;
            sampleCounter = 0;
            pageLinks.add(new Page(title));
        }//end if
        //Store the sample values in the MyCanvas
        // object to be used later to paint the
        // screen.  Then increment the sample
        // counter.  The sample values pass through
        // the page object into the current MyCanvas
        // object.
        pageLinks.get(pageCounter).putData(
                valBlack,
                valRed,
                valBlue,
                sampleCounter);
        sampleCounter++;
    }//end feedData
    //-------------------------------------------//

  //There are two overloaded versions of the
    // plotData method.  One version allows the
    // user to specify the location on the screen
    // where the stack of plotted pages will
    // appear.  The other version places the stack
    // in the upper left corner of the screen.
  //Invoke one of the overloaded versions of
    // this method once when all data has been fed
    // to the plotting object in order to rearrange
    // the order of the pages with page 0 at the
    // top of the stack on the screen.
  //For this overloaded version, specify xCoor
    // and yCoor to control the location of the
    // stack on the screen.  Values of 0,0 will
    // place the stack at the upper left corner of
    // the screen.  Also see the other overloaded
    // version, which places the stack at the upper
    // left corner of the screen by default.
    void plotData(int xCoor, int yCoor) {
        Page lastPage
                = pageLinks.get(pageLinks.size() - 1);
        //Delay until last page becomes visible.
        while (!lastPage.isVisible()) {
            //Loop until last page becomes visible
        }//end while loop

        Page tempPage = null;
        //Make all pages invisible
        for (int cnt = 0; cnt < (pageLinks.size());
                cnt++) {
            tempPage = pageLinks.get(cnt);
            tempPage.setVisible(false);
        }//end for loop

    //Now make all pages visible in reverse order
        // so that page 0 will be on top of the
        // stack on the screen.
        for (int cnt = pageLinks.size() - 1; cnt >= 0;
                cnt--) {
            tempPage = pageLinks.get(cnt);
            tempPage.setLocation(xCoor, yCoor);
            tempPage.setVisible(true);
        }//end for loop
    }//end plotData(int xCoor,int yCoor)
    //-------------------------------------------//

  //This overloaded version of the method causes
    // the stack to be located in the upper left
    // corner of the screen by default
    void plotData() {
        plotData(0, 0);//invoke overloaded version
    }//end plotData()
    //-------------------------------------------//
    //Inner class.  A PlotALot04 object may
    // have as many Page objects as are required
    // to plot all of the data values.  The 
    // reference to each Page object is stored
    // in an ArrayList object belonging to the
    // PlotALot04 object.

    class Page extends Frame {

        MyCanvas canvas;
        int sampleCounter;

        Page(String title) {//constructor
            canvas = new MyCanvas();
            add(canvas);
            setSize(frameWidth, frameHeight);
            setTitle(title + " Page: " + pageCounter);
            setVisible(true);

      //---------------------------------------//
            //Anonymous inner class to terminate the
            // program when the user clicks the close
            // button on the Frame.
            addWindowListener(
                    new WindowAdapter() {
                        public void windowClosing(
                                WindowEvent e) {
                                    System.exit(0);//terminate program
                                }//end windowClosing()
                    }//end WindowAdapter
            );//end addWindowListener
            //---------------------------------------//
        }//end constructor
        //=========================================//

    //This method receives a triplet of sample
        // values of type double and stores each of
        // them in a separate array object belonging
        // to the MyCanvas object.
        void putData(double valBlack,
                double valRed,
                double valBlue,
                int sampleCounter) {
            canvas.blackData[sampleCounter] = valBlack;
            canvas.redData[sampleCounter] = valRed;
            canvas.blueData[sampleCounter] = valBlue;
      //Save the sample counter in an instance
            // variable to make it available to the
            // overridden paint method. This value is
            // needed by the paint method so it will
            // know how many samples to plot on the
            // final page which probably won't be full.
            this.sampleCounter = sampleCounter;
        }//end putData

    //=========================================//
        //Inner class
        class MyCanvas extends Canvas {

            double[] blackData
                    = new double[samplesPerPage];
            double[] redData
                    = new double[samplesPerPage];
            double[] blueData
                    = new double[samplesPerPage];

            //Override the paint method
            public void paint(Graphics g) {
        //Draw horizontal axes, one for each
                // trace.
                for (int cnt = 0; cnt < tracesPerPage;
                        cnt++) {
                    g.drawLine(0,
                            (cnt + 1) * traceSpacing,
                            this.getWidth(),
                            (cnt + 1) * traceSpacing);
                }//end for loop

        //Plot the points if there are any to be
                // plotted.
                if (sampleCounter > 0) {
                    for (int cnt = 0; cnt <= sampleCounter;
                            cnt++) {

            //Begin by plotting the values from
                        // the blackData array object.
                        g.setColor(Color.BLACK);

            //Compute a vertical offset to locate
                        // the black data on every third axis
                        // on the page.
                        int yOffset
                                = ((1 + cnt * (sampSpacing + 1)
                                / this.getWidth()) * 3 * traceSpacing)
                                - 2 * traceSpacing;
            //Draw an oval centered on the sample
                        // value to mark the sample in the
                        // plot. It is best if the dimensions
                        // of the oval are evenly divisable
                        // by 2 for  centering purposes.
                        //Reverse the sign of the sample
                        // value to cause positive sample
                        // values to be plotted above the
                        // axis.
                        g.drawOval(cnt * (sampSpacing + 1)
                                % this.getWidth() - ovalWidth / 2,
                                yOffset - (int) blackData[cnt]
                                - ovalHeight / 2,
                                ovalWidth,
                                ovalHeight);

            //Connect the sample values with
                        // straight lines.  Do not draw a
                        // line connecting the last sample in
                        // one trace to the first sample in
                        // the next trace.
                        if (cnt * (sampSpacing + 1)
                                % this.getWidth()
                                >= sampSpacing + 1) {
                            g.drawLine(
                                    (cnt - 1) * (sampSpacing + 1)
                                    % this.getWidth(),
                                    yOffset - (int) blackData[cnt - 1],
                                    cnt * (sampSpacing + 1)
                                    % this.getWidth(),
                                    yOffset - (int) blackData[cnt]);
                        }//end if
                        //Now plot the data stored in the
                        // redData array object.
                        g.setColor(Color.RED);
            //Compute a vertical offset to locate
                        // the red data on every third axis
                        // on the page.
                        yOffset = (1 + cnt * (sampSpacing + 1)
                                / this.getWidth()) * 3 * traceSpacing
                                - traceSpacing;

                        //Draw the ovals as described above.
                        g.drawOval(cnt * (sampSpacing + 1)
                                % this.getWidth() - ovalWidth / 2,
                                yOffset - (int) redData[cnt]
                                - ovalHeight / 2,
                                ovalWidth,
                                ovalHeight);

            //Connect the sample values with
                        // straight lines as described above.
                        if (cnt * (sampSpacing + 1)
                                % this.getWidth()
                                >= sampSpacing + 1) {
                            g.drawLine(
                                    (cnt - 1) * (sampSpacing + 1)
                                    % this.getWidth(),
                                    yOffset - (int) redData[cnt - 1],
                                    cnt * (sampSpacing + 1)
                                    % this.getWidth(),
                                    yOffset - (int) redData[cnt]);

                        }//end if

            //Now plot the data stored in the
                        // blueData array object.
                        g.setColor(Color.BLUE);
            //Compute a vertical offset to locate
                        // the blue data on every third axis
                        // on the page.
                        yOffset = (1 + cnt * (sampSpacing + 1)
                                / this.getWidth()) * 3 * traceSpacing;

                        //Draw the ovals as described above.
                        g.drawOval(cnt * (sampSpacing + 1)
                                % this.getWidth() - ovalWidth / 2,
                                yOffset - (int) blueData[cnt]
                                - ovalHeight / 2,
                                ovalWidth,
                                ovalHeight);

            //Connect the sample values with
                        // straight lines as described above.
                        if (cnt * (sampSpacing + 1)
                                % this.getWidth()
                                >= sampSpacing + 1) {
                            g.drawLine(
                                    (cnt - 1) * (sampSpacing + 1)
                                    % this.getWidth(),
                                    yOffset - (int) blueData[cnt - 1],
                                    cnt * (sampSpacing + 1)
                                    % this.getWidth(),
                                    yOffset - (int) blueData[cnt]);
                        }//end if          
                    }//end for loop
                }//end if for sampleCounter > 0
            }//end overridden paint method
        }//end inner class MyCanvas
    }//end inner class Page
}//end class PlotALot04
//=============================================//
