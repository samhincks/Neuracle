package timeseriestufts.kth.streams.uni.adaptivefilters;

/* File Graph06.java
Copyright 2002, R.G.Baldwin
Revised 5/15/04

Very similar to Graph03, except that
each point is displayed as a
rectangle, centered on the sample.
Can be used to explain integration
through summation of the sample values.

Note:  This program requires access to
the interface named GraphIntfc01.

This is a plotting program.  It is
designed to access a class file, which
implements GraphIntfc01, and to plot up
to five functions defined in that class
file.  The plotting surface is divided
into the required number of equally
sized plotting areas, and one function
is plotted on cartesian coordinates in
each area.

The methods corresponding to the
functions are named f1, f2, f3, f4,
and f5.

The class containing the functions must
also define a method named
getNmbr(), which takes no parameters
and returns the number of functions to
be plotted.  If this method returns a
value greater than 5, a
NoSuchMethodException will be thrown.

Note that the constructor for the class
that implements GraphIntfc01 must not
require any parameters due to the
use of the newInstance method of the
Class class to instantiate an object
of that class.

If the number of functions is less
than 5, then the absent method names
must begin with f5 and work down toward
f1.  For example, if the number of
functions is 3, then the program will
expect to call methods named f1, f2,
and f3.  It is OK for the absent
methods to be defined in the class.
They simply won't be invoked.

The plotting areas have alternating
white and gray backgrounds to make them
easy to separate visually.

All curves are plotted in black.  A
cartesian coordinate system with axes,
tic marks, and labels is drawn in red
in each plotting area.

The cartesian coordinate system in each
plotting area has the same horizontal
and vertical scale, as well as the
same tic marks and labels on the axes.

The labels displayed on the axes,
correspond to the values of the extreme
edges of the plotting area.

The program also compiles a sample
class named junk, which contains five
methods and the method named getNmbr.
This makes it easy to compile and test
this program in a stand-alone mode.

At runtime, the name of the class that
implements the GraphIntfc01 interface
must be provided as a command-line
parameter.  If this parameter is
missing, the program instantiates an
object from the internal class named
junk and plots the data provided by
that class.  Thus, you can test the
program by running it with no
command-line parameter.

This program provides the following
text fields for user input, along with
a button labeled Graph.  This allows
the user to adjust the parameters and
replot the graph as many times with as
many plotting scales as needed:

xMin = minimum x-axis value
xMax = maximum x-axis value
yMin = minimum y-axis value
yMax = maximum y-axis value
xTicInt = tic interval on x-axis
yTicInt = tic interval on y-axis
xCalcInc = calculation interval

The user can modify any of these
parameters and then click the Graph
button to cause the five functions
to be re-plotted according to the
new parameters.

Whenever the Graph button is clicked,
the event handler instantiates a new
object of the class that implements
the GraphIntfc01 interface.  Depending
on the nature of that class, this may
be redundant in some cases.  However,
it is useful in those cases where it
is necessary to refresh the values of
instance variables defined in the
class (such as a counter, for example).

Tested using JDK 1.4.0 under Win 2000.

This program uses constants that were
first defined in the Color class of
v1.4.0.  Therefore, the program
requires v1.4.0 or later to compile and
run correctly.
**************************************/

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;

class Graph06{
  public static void main(
          String[] args)
          throws NoSuchMethodException,
                ClassNotFoundException,
                InstantiationException,
                IllegalAccessException{
    if(args.length == 1){
      //pass command-line paramater
      new GUI(args[0]);
    }else{
      //no command-line parameter given
      new GUI(null);
    }//end else
  }// end main
}//end class Graph06 definition
//===================================//

class GUI extends JFrame
             implements ActionListener{

  //Define plotting parameters and
  // their default values.
  double xMin = 0.0;
  double xMax = 400.0;
  double yMin = -100.0;
  double yMax = 100.0;

  //Tic mark intervals
  double xTicInt = 20.0;
  double yTicInt = 20.0;

  //Tic mark lengths.  If too small
  // on x-axis, a default value is
  // used later.
  double xTicLen = (yMax-yMin)/50;
  double yTicLen = (xMax-xMin)/50;

  //Calculation interval along x-axis
  double xCalcInc = 1.0;

  //Text fields for plotting parameters
  JTextField xMinTxt =
             new JTextField("" + xMin);
  JTextField xMaxTxt =
             new JTextField("" + xMax);
  JTextField yMinTxt =
             new JTextField("" + yMin);
  JTextField yMaxTxt =
             new JTextField("" + yMax);
  JTextField xTicIntTxt =
          new JTextField("" + xTicInt);
  JTextField yTicIntTxt =
          new JTextField("" + yTicInt);
  JTextField xCalcIncTxt =
         new JTextField("" + xCalcInc);

  //Panels to contain a label and a
  // text field
  JPanel pan0 = new JPanel();
  JPanel pan1 = new JPanel();
  JPanel pan2 = new JPanel();
  JPanel pan3 = new JPanel();
  JPanel pan4 = new JPanel();
  JPanel pan5 = new JPanel();
  JPanel pan6 = new JPanel();

  //Misc instance variables
  int frmWidth = 408;
  int frmHeight = 430;
  int width;
  int height;
  int number;
  GraphIntfc01 data;
  String args = null;

  //Plots are drawn on the canvases
  // in this array.
  Canvas[] canvases;

  //Constructor
  GUI(String args)throws
                NoSuchMethodException,
                ClassNotFoundException,
                InstantiationException,
                IllegalAccessException{

    if(args != null){
      //Save for use later in the
      // ActionEvent handler
      this.args = args;
      //Instantiate an object of the
      // target class using the String
      // name of the class.
      data = (GraphIntfc01)
                   Class.forName(args).
                         newInstance();
    }else{
      //Instantiate an object of the
      // test class named junk.
      data = new junk();
    }//end else

    //Create array to hold correct
    // number of Canvas objects.
    canvases =
            new Canvas[data.getNmbr()];

    //Throw exception if number of
    // functions is greater than 5.
    number = data.getNmbr();
    if(number > 5){
      throw new NoSuchMethodException(
                "Too many functions.  "
                  + "Only 5 allowed.");
    }//end if

    //Create the control panel and
    // give it a border for cosmetics.
    JPanel ctlPnl = new JPanel();
    ctlPnl.setLayout(//?rows x 4 cols
                  new GridLayout(0,4));
    ctlPnl.setBorder(
                   new EtchedBorder());

    //Button for replotting the graph
    JButton graphBtn =
                  new JButton("Graph");
    graphBtn.addActionListener(this);

    //Populate each panel with a label
    // and a text field.  Will place
    // these panels in a grid on the
    // control panel later.
    pan0.add(new JLabel("xMin"));
    pan0.add(xMinTxt);

    pan1.add(new JLabel("xMax"));
    pan1.add(xMaxTxt);

    pan2.add(new JLabel("yMin"));
    pan2.add(yMinTxt);

    pan3.add(new JLabel("yMax"));
    pan3.add(yMaxTxt);

    pan4.add(new JLabel("xTicInt"));
    pan4.add(xTicIntTxt);

    pan5.add(new JLabel("yTicInt"));
    pan5.add(yTicIntTxt);

    pan6.add(new JLabel("xCalcInc"));
    pan6.add(xCalcIncTxt);

    //Add the populated panels and the
    // button to the control panel with
    // a grid layout.
    ctlPnl.add(pan0);
    ctlPnl.add(pan1);
    ctlPnl.add(pan2);
    ctlPnl.add(pan3);
    ctlPnl.add(pan4);
    ctlPnl.add(pan5);
    ctlPnl.add(pan6);
    ctlPnl.add(graphBtn);

    //Create a panel to contain the
    // Canvas objects.  They will be
    // displayed in a one-column grid.
    JPanel canvasPanel = new JPanel();
    canvasPanel.setLayout(//?rows,1 col
                  new GridLayout(0,1));

    //Create a custom Canvas object for
    // each function to be plotted and
    // add them to the one-column grid.
    // Make background colors alternate
    // between white and gray.
    for(int cnt = 0;
                  cnt < number; cnt++){
      switch(cnt){
        case 0 :
          canvases[cnt] =
                     new MyCanvas(cnt);
          canvases[cnt].setBackground(
                          Color.WHITE);
          break;
        case 1 :
          canvases[cnt] =
                     new MyCanvas(cnt);
          canvases[cnt].setBackground(
                     Color.LIGHT_GRAY);
          break;
        case 2 :
          canvases[cnt] =
                     new MyCanvas(cnt);
          canvases[cnt].setBackground(
                          Color.WHITE);
          break;
        case 3 :
          canvases[cnt] =
                     new MyCanvas(cnt);
          canvases[cnt].setBackground(
                     Color.LIGHT_GRAY);
          break;
        case 4 :
          canvases[cnt] =
                     new MyCanvas(cnt);
          canvases[cnt].
            setBackground(Color.WHITE);
      }//end switch
      //Add the object to the grid.
      canvasPanel.add(canvases[cnt]);
    }//end for loop

    //Add the sub-assemblies to the
    // frame.  Set its location, size,
    // and title, and make it visible.
    getContentPane().
                   add(ctlPnl,"South");
    getContentPane().
             add(canvasPanel,"Center");

    setBounds(0,0,frmWidth,frmHeight);

    if(args == null){
      setTitle("Graph06, " +
                 "Copyright 2002, " +
                 "Richard G. Baldwin");
    }else{
      setTitle("Graph06/" + args +
                 " Copyright 2002, " +
                 "R. G. Baldwin");
    }//end else

    setVisible(true);

    //Set to exit on X-button click
    setDefaultCloseOperation(
                        EXIT_ON_CLOSE);

    //Guarantee a repaint on startup.
    for(int cnt = 0;
                  cnt < number; cnt++){
      canvases[cnt].repaint();
    }//end for loop

  }//end constructor
  //---------------------------------//

  //This event handler is registered
  // on the JButton to cause the
  // functions to be replotted.
  public void actionPerformed(
                      ActionEvent evt){
    //Re-instantiate the object that
    // provides the data
    try{
      if(args != null){
        data = (GraphIntfc01)Class.
           forName(args).newInstance();
      }else{
        data = new junk();
      }//end else
    }catch(Exception e){
      //Known to be safe at this point.
      // Otherwise would have aborted
      // earlier.
    }//end catch

    //Set plotting parameters using
    // data from the text fields.
    xMin = Double.parseDouble(
                    xMinTxt.getText());
    xMax = Double.parseDouble(
                    xMaxTxt.getText());
    yMin = Double.parseDouble(
                    yMinTxt.getText());
    yMax = Double.parseDouble(
                    yMaxTxt.getText());
    xTicInt = Double.parseDouble(
                 xTicIntTxt.getText());
    yTicInt = Double.parseDouble(
                 yTicIntTxt.getText());
    xCalcInc = Double.parseDouble(
                xCalcIncTxt.getText());

    //Calculate new values for the
    // length of the tic marks on the
    // axes.  If too small on x-axis,
    // a default value is used later.
    xTicLen = (yMax-yMin)/50;
    yTicLen = (xMax-xMin)/50;

    //Repaint the plotting areas
    for(int cnt = 0;
                  cnt < number; cnt++){
      canvases[cnt].repaint();
    }//end for loop

  }//end actionPerformed
  //---------------------------------//


//This is an inner class, which is used
// to override the paint method on the
// plotting surface.
class MyCanvas extends Canvas{
  int cnt;//object number
  //Factors to convert from double
  // values to integer pixel locations.
  double xScale;
  double yScale;

  MyCanvas(int cnt){//save obj number
    this.cnt = cnt;
  }//end constructor

  //Override the paint method
  public void paint(Graphics g){
    //Get and save the size of the
    // plotting surface
    width = canvases[0].getWidth();
    height = canvases[0].getHeight();

    //Calculate the scale factors
    xScale = width/(xMax-xMin);
    yScale = height/(yMax-yMin);

    //Set the origin based on the
    // minimum values in x and y
    g.translate((int)((0-xMin)*xScale),
               (int)((0-yMin)*yScale));
    drawAxes(g);//Draw the axes
    g.setColor(Color.BLACK);

    //Get initial data values
    double xVal = xMin;
    int oldX = getTheX(xVal);
    int oldY = 0;
    //Use the Canvas obj number to
    // determine which method to
    // invoke to get the value for y.
    switch(cnt){
      case 0 :
        oldY = getTheY(data.f1(xVal));
        break;
      case 1 :
        oldY = getTheY(data.f2(xVal));
        break;
      case 2 :
        oldY = getTheY(data.f3(xVal));
        break;
      case 3 :
        oldY = getTheY(data.f4(xVal));
        break;
      case 4 :
        oldY = getTheY(data.f5(xVal));
    }//end switch

    //Now loop and plot the points
    while(xVal < xMax){
      int yVal = 0;
      //Get next data value.  Use the
      // Canvas obj number to
      // determine which method to
      // invoke to get the value for y.
      switch(cnt){
        case 0 :
          yVal =
                getTheY(data.f1(xVal));
          break;
        case 1 :
          yVal =
                getTheY(data.f2(xVal));
          break;
        case 2 :
          yVal =
                getTheY(data.f3(xVal));
          break;
        case 3 :
          yVal =
                getTheY(data.f4(xVal));
          break;
        case 4 :
          yVal =
                getTheY(data.f5(xVal));
      }//end switch1


      //Convert the x-value to an int
      // and draw the next horizontal
      // line segment

      int x = getTheX(xVal+xCalcInc/2);
      g.drawLine(oldX,yVal,x,yVal);

      //Draw a vertical line at the
      // old x-value
      int yZero = getTheY(0);
      g.drawLine(oldX,yZero,oldX,yVal);

      //Draw a vertical line at the
      // new y-value
      g.drawLine(x,yZero,x,yVal);

      //Increment along the x-axis
      xVal += xCalcInc;

      //Save end point to use as start
      // point for next line segment.
      oldX = x;
      oldY = yVal;
    }//end while loop

  }//end overridden paint method
  //---------------------------------//

  //Method to draw axes with tic marks
  // and labels in the color RED
  void drawAxes(Graphics g){
    g.setColor(Color.RED);

    //Lable left x-axis and bottom
    // y-axis.  These are the easy
    // ones.  Separate the labels from
    // the ends of the tic marks by
    // two pixels.
    g.drawString("" + (int)xMin,
                 getTheX(xMin),
                 getTheY(xTicLen/2)-2);
    g.drawString("" + (int)yMin,
                  getTheX(yTicLen/2)+2,
                        getTheY(yMin));

    //Label the right x-axis and the
    // top y-axis.  These are the hard
    // ones because the position must
    // be adjusted by the font size and
    // the number of characters.
    //Get the width of the string for
    // right end of x-axis and the
    // height of the string for top of
    // y-axis
    //Create a string that is an
    // integer representation of the
    // label for the right end of the
    // x-axis.  Then get a character
    // array that represents the
    // string.
    int xMaxInt = (int)xMax;
    String xMaxStr = "" + xMaxInt;
    char[] array = xMaxStr.
                         toCharArray();

    //Get a FontMetrics object that can
    // be used to get the size of the
    // string in pixels.
    FontMetrics fontMetrics =
                    g.getFontMetrics();
    //Get a bounding rectangle for the
    // string
    Rectangle2D r2d =
           fontMetrics.getStringBounds(
               array,0,array.length,g);
    //Get the width and the height of
    // the bounding rectangle.  The
    // width is the width of the label
    // at the right end of the
    // x-axis.  The height applies to
    // all the labels, but is needed
    // specifically for the label at
    // the top end of the y-axis.
    int labWidth =
                 (int)(r2d.getWidth());
    int labHeight =
                (int)(r2d.getHeight());

    //Label the positive x-axis and the
    // positive y-axis using the width
    // and height from above to
    // position the labels.  These
    // labels apply to the very ends of
    // the axes at the edge of the
    // plotting surface.
    g.drawString("" + (int)xMax,
                getTheX(xMax)-labWidth,
                 getTheY(xTicLen/2)-2);
    g.drawString("" + (int)yMax,
              getTheX(yTicLen/2)+2,
              getTheY(yMax)+labHeight);

    //Draw the axes
    g.drawLine(getTheX(xMin),
                         getTheY(0.0),
                         getTheX(xMax),
                         getTheY(0.0));

    g.drawLine(getTheX(0.0),
                        getTheY(yMin),
                        getTheX(0.0),
                        getTheY(yMax));

    //Draw the tic marks on axes
    xTics(g);
    yTics(g);
  }//end drawAxes

  //---------------------------------//

  //Method to draw tic marks on x-axis
  void xTics(Graphics g){
    double xDoub = 0;
    int x = 0;

    //Get the ends of the tic marks.
    int topEnd = getTheY(xTicLen/2);
    int bottomEnd =
                   getTheY(-xTicLen/2);

    //If the vertical size of the
    // plotting area is small, the
    // calculated tic size may be too
    // small.  In that case, set it to
    // 10 pixels.
    if(topEnd < 5){
      topEnd = 5;
      bottomEnd = -5;
    }//end if

    //Loop and draw a series of short
    // lines to serve as tic marks.
    // Begin with the positive x-axis
    // moving to the right from zero.
    while(xDoub < xMax){
      x = getTheX(xDoub);
      g.drawLine(x,topEnd,x,bottomEnd);
      xDoub += xTicInt;
    }//end while

    //Now do the negative x-axis moving
    // to the left from zero
    xDoub = 0;
    while(xDoub > xMin){
      x = getTheX(xDoub);
      g.drawLine(x,topEnd,x,bottomEnd);
      xDoub -= xTicInt;
    }//end while

  }//end xTics
  //---------------------------------//

  //Method to draw tic marks on y-axis
  void yTics(Graphics g){
    double yDoub = 0;
    int y = 0;
    int rightEnd = getTheX(yTicLen/2);
    int leftEnd = getTheX(-yTicLen/2);

    //Loop and draw a series of short
    // lines to serve as tic marks.
    // Begin with the positive y-axis
    // moving up from zero.
    while(yDoub < yMax){
      y = getTheY(yDoub);
      g.drawLine(rightEnd,y,leftEnd,y);
      yDoub += yTicInt;
    }//end while

    //Now do the negative y-axis moving
    // down from zero.
    yDoub = 0;
    while(yDoub > yMin){
      y = getTheY(yDoub);
      g.drawLine(rightEnd,y,leftEnd,y);
      yDoub -= yTicInt;
    }//end while

  }//end yTics

  //---------------------------------//

  //This method translates and scales
  // a double y value to plot properly
  // in the integer coordinate system.
  // In addition to scaling, it causes
  // the positive direction of the
  // y-axis to be from bottom to top.
  int getTheY(double y){
    double yDoub = (yMax+yMin)-y;
    int yInt = (int)(yDoub*yScale);
    return yInt;
  }//end getTheY
  //---------------------------------//

  //This method scales a double x value
  // to plot properly in the integer
  // coordinate system.
  int getTheX(double x){
    return (int)(x*xScale);
  }//end getTheX
  //---------------------------------//

}//end inner class MyCanvas
//===================================//

}//end class GUI
//===================================//

//Sample test class.  Required for
// compilation and stand-alone
// testing.
class junk implements GraphIntfc01{
  public int getNmbr(){
    //Return number of functions to
    // process.  Must not exceed 5.
    return 4;
  }//end getNmbr

  public double f1(double x){
    return (x*x*x)/200.0;
  }//end f1

  public double f2(double x){
    return -(x*x*x)/200.0;
  }//end f2

  public double f3(double x){
    return (x*x)/200.0;
  }//end f3

  public double f4(double x){
    return 50*Math.cos(x/10.0);
  }//end f4

  public double f5(double x){
    return 100*Math.sin(x/20.0);
  }//end f5

}//end sample class junk