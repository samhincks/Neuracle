package timeseriestufts.kth.streams.uni.adaptivefilters;

/*File PlotALot01.java 
 Copyright 2005, R.G.Baldwin
 This program is designed to plot large amounts of
 time-series data for a single channel.  See
 PlotALot02.java for a two-channel program.
 Note that by carefully adjusting the plotting
 parameters, this program could also be used to
 plot large quantities of spectral data in a
 waterfall display.
 The class provides a main method so that the
 class can be run as an application to test
 itself.
 There are three steps involved in the use of this
 class for plotting time series data:
 1. Instantiate a plotting object of type 
 PlotALot01 using one of two overloaded 
 constructors.
 2. Feed data that is to be plotted to the 
 plotting object by invoking the feedData 
 method once for each data value.
 3. Invoke one of two overloaded plotData methods 
 on the plotting object once all of the data 
 has been fed to the object.  This causes all
 of the data to be plotted.
   
 A using program can instantiate as many 
 plotting objects as are needed to plot all of the
 different time series that need to be plotted.
 Each plotting object can be used to plot as many
 data values as need be plotted until the program
 runs out of available memory.
 The plotting object of type PlotALot01 owns one 
 or more Page objects that extend the Frame class.
 The plotting object can own as many Page objects 
 as are necessary to plot all of the data that is 
 fed to that plotting object.
 The program produces a graphic output consisting 
 of a stack of Page objects on the screen, with 
 the data plotted on a Canvas object contained by 
 the Page object.  The Page showing the earliest 
 data is on the top of the stack and the Page 
 showing the latest data is on the bottom of the 
 stack.  The Page objects on the top of the stack 
 must be physically moved in order to see the 
 Page objects on the bottom of the stack.
 Each Page object contains one or more horizontal 
 axes on which the data is plotted.  The earliest 
 data is plotted on the axis nearest the top of 
 the Page moving from left to right across the 
 axis.  Positive data values are plotted above
 the axis and negative values are plotted below
 the axis.  When the right end of an axis is 
 reached, the next data value is plotted on the 
 left end of the axis immediately below it.  When 
 the right end of the last axis on the Page is 
 reached, a new Page object is created and the 
 next data value is plotted at the left end of the
 top axis on that Page object.
 A mentioned above, there are two overloaded 
 versions of the constructor for the PlotALot01
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
 mark the sample value on the plot.
 int ovalHeight: Height of an oval that is used to
 mark the sample value on the plot.
 For test purposes, the main method instantiates 
 and feeds two independent plotting objects. 
 Plotting parameters are specified for the first 
 plotting object. Default plotting parameters are 
 accepted for the second plotting object.
 
 The data that is fed to each plotting object is 
 white random noise. However, for the first
 plotting object, fifteen of the data values are 
 not random.  Rather, seven of the values are set
 to values of 0,0,25,-25,25,0,0 to confirm the 
 proper transition from the end of one page to the
 beginning of the next page. In addition, eight of
 the values are set to 0,0,20,20,-20,-20,0,0 in
 order to confirm the proper transition from one 
 trace to the next trace on the same page.
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
 parameters for each plotting object is displayed 
 on the command line screen when the class is used
 for plotting.  The values shown below result from
 the execution of the main method of the class for
 test purposes. One of the plotting objects 
 instantiated by the main method is entitled "A" 
 and the other is entitled "B".
 Title: A
 Frame width: 158
 Frame height: 237
 Page width: 150
 Page height: 210
 Trace spacing: 36
 Sample spacing: 5
 Traces per page: 5
 Samples per page: 150
 Title: B
 Frame width: 400
 Frame height: 410
 Page width: 392
 Page height: 383
 Trace spacing: 50
 Sample spacing: 2
 Traces per page: 7
 Samples per page: 1372
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

public class PlotALot01 {
  //This main method is provided so that the
    // class can be run as an application to test
    // itself.

    public static void main(String[] args) {
    //Instantiate two independent plotting
        // objects.  Control plotting parameters for
        // the first object.  Accept default plotting
        // parameters for the second object.
        PlotALot01 plotObjectA
                = new PlotALot01("A", 158, 237, 36, 5, 4, 4);
        PlotALot01 plotObjectB = new PlotALot01("B");

        //Feed the data to the first plotting object.
        for (int cnt = 0; cnt < 275; cnt++) {
      //Plot some white random noise in the first
            // object using specified plotting
            // parameters. Note, that fifteen of the
            // following values are not random.  Seven
            // values are set to 0,0,25,-25,25,0,0
            // specifically to confirm the proper
            // transition from the end of one page to
            // the beginning of the next page.  Eight
            // values are set to 0,0,20,20,-20,-20,0,0
            // to confirm the proper transition from
            // one trace to the next trace on the same
            // page.  Note that these are the correct
            // values for an AWT Frame object under
            // WinXP.  However, a Frame may have 
            // different inset values on other
            // operating systems, which may cause these
            // specific values to be incorrect.
            if (cnt == 147) {
                plotObjectA.feedData(0);
            } else if (cnt == 148) {
                plotObjectA.feedData(0);
            } else if (cnt == 149) {
                plotObjectA.feedData(25);
            } else if (cnt == 150) {
                plotObjectA.feedData(-25);
            } else if (cnt == 151) {
                plotObjectA.feedData(25);
            } else if (cnt == 152) {
                plotObjectA.feedData(0);
            } else if (cnt == 153) {
                plotObjectA.feedData(0);
            } else if (cnt == 26) {
                plotObjectA.feedData(0);
            } else if (cnt == 27) {
                plotObjectA.feedData(0);
            } else if (cnt == 28) {
                plotObjectA.feedData(20);
            } else if (cnt == 29) {
                plotObjectA.feedData(20);
            } else if (cnt == 30) {
                plotObjectA.feedData(-20);
            } else if (cnt == 31) {
                plotObjectA.feedData(-20);
            } else if (cnt == 32) {
                plotObjectA.feedData(0);
            } else if (cnt == 33) {
                plotObjectA.feedData(0);
            } else {
                plotObjectA.feedData(
                        (Math.random() - 0.5) * 25);
            }//end else
        }//end for loop
        //Cause the data to be plotted.
        plotObjectA.plotData(401, 0);

    //Plot white random noise in the second
        // plotting object using default plotting
        // parameters.
        //Feed the data to the second plotting
        // object.
        for (int cnt = 0; cnt < 2600; cnt++) {
            plotObjectB.feedData(
                    (Math.random() - 0.5) * 25);
        }//end for loop
        //Cause the data to be plotted.
        plotObjectB.plotData();

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
    PlotALot01(String title,//Frame title
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
        if (tracesPerPage == 0) {
            System.out.println("Terminating program");
            System.exit(0);
        }//end if
        samplesPerPage = canvasWidth * tracesPerPage
                / (sampSpacing + 1);
        System.out.println("Samples per page: "
                + samplesPerPage);
    //Now instantiate the first usable Page
        // object and store its reference in the
        // list.
        pageLinks.add(new Page(title));
    }//end constructor
    //-------------------------------------------//

    PlotALot01(String title) {
    //Invoke the other overloaded constructor
        // passing default values for all but the
        // title.
        this(title, 400, 410, 50, 2, 2, 2);
    }//end overloaded constructor
    //-------------------------------------------//

  //Invoke this method for each point to be
    // plotted.
    void feedData(double val) {
        if ((sampleCounter) == samplesPerPage) {
      //if the page is full, increment the page
            // counter, create a new empty page, and
            // reset the sample counter.
            pageCounter++;
            sampleCounter = 0;
            pageLinks.add(new Page(title));
        }//end if
        //Store the sample value in the MyCanvas
        // object to be used later to paint the
        // screen.  Then increment the sample
        // counter.  The sample value passes through
        // the page object into the current MyCanvas
        // object.
        pageLinks.get(pageCounter).putData(
                val, sampleCounter);
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
    // this method once when all of the data has
    // been fed to the plotting object in order to
    // rearrange the order of the pages with
    // page 0 at the top of the stack on the
    // screen.
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
    //Inner class.  A PlotALot01 object may
    // have as many Page objects as are required
    // to plot all of the data values.  The 
    // reference to each Page object is stored
    // in an ArrayList object belonging to the
    // PlotALot01 object.

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

    //This method receives a sample value of type
        // double and stores it in an array object
        // belonging to the MyCanvas object.
        void putData(double sampleValue,
                int sampleCounter) {
            canvas.data[sampleCounter] = sampleValue;
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

            double[] data
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
            //Compute a vertical offset to locate
                        // the data on a particular trace.
                        int yOffset
                                = (1 + cnt * (sampSpacing + 1)
                                / this.getWidth()) * traceSpacing;
            //Draw an oval centered on the sample
                        // value to mark the sample.  It is 
                        // best if the dimensions of the oval
                        // are evenly divisable by 2 for 
                        // centering purposes.
                        //Reverse the sign on sample value to
                        // cause positive sample values to go
                        // up on the screen
                        g.drawOval(cnt * (sampSpacing + 1)
                                % this.getWidth() - ovalWidth / 2,
                                yOffset - (int) data[cnt]
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
                                    yOffset - (int) data[cnt - 1],
                                    cnt * (sampSpacing + 1)
                                    % this.getWidth(),
                                    yOffset - (int) data[cnt]);
                        }//end if
                    }//end for loop
                }//end if for sampleCounter > 0
            }//end overridden paint method
        }//end inner class MyCanvas
    }//end inner class Page
}//end class PlotALot01
//=============================================//
