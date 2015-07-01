/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeseriestufts.evaluatable;

import java.text.DecimalFormat;
import java.util.HashMap;
import timeseriestufts.evaluation.featureextraction.SAXAttribute;

/**
 *
 * @author samhincks
 */
public class FeatureDescription extends Technique{
    public Statistic statistic;
    public FSTimeWindow window;
    public FSDataLayer datalayer;
    
    public FeatureDescription(Statistic s, FSTimeWindow w, FSDataLayer d) {
        statistic = s;
        window = w;
        datalayer =d;
        this.id = s.id+ w.id +d.id;
    }
  
    /* what statistic?
     * Performances: 
     * mean: 2958. Misleading. First: is always the slowest
     * smallest: 1981
     * largest: 1553
     * fwhm: 1542
     * slope: 1508
     * secondder: 2654
     * t2p: 2654
     * absmean: 1605
     * 
     * sax-3-2 - means sax with 3 alphabet size and 2 length
     * sax-abcdd - means find distance to canonical form abcdd (alpha =4, length =5)
     */
    public static class Statistic extends Technique {

        public boolean isNumeric() {
            return (!(stat == Stat.sax || stat == Stat.granger));
        }
        
        public boolean isPair() {
            return (stat == Stat.saxpair || stat == Stat.granger);
        }

        
        public static enum Stat {mean, smallest, largest, fwhm, slope, absslope, 
            stddev, secondder, t2p, absmean, sax, saxdist, bestfit, bfintercept, freq,
            granger, saxpair};
        
        public Stat stat;
        private int alphabetLength; //.. only for Nominal SAX
        private int numLetters; //.. only for Nominal SAX
        private int freqInt; //. only for frequency domain
        private int lag; //.. only for granger
        private String saxString;
        
        /*regular stat*/
        public Statistic(Stat type) {
            this.stat = type;
            this.id = type.toString();
        }
        public Statistic(Stat type, int intParam) throws Exception{
            if (type == Stat.freq ) 
                this.freqInt = intParam;
            else if (type == Stat.granger ) 
                this.lag = intParam;
            else throw new Exception ("Must be Freq or Granger");
            stat = type; //.. No choice here!

        }
        /**PARSE: SAX-2-3 for nominal feature with a class for each possible string of alpha 2 and numletters 3*/
        public Statistic(Stat type, int alphabetLength, int numLetters) throws Exception{
            System.out.println(type);
            if (type != Stat.sax && type != Stat.saxpair) throw new Exception ("Must be SAX or SAXDist");
            stat = type; //.. No choice here!
            this.alphabetLength = alphabetLength;
            this.numLetters = numLetters;
        }
        /** PARSE: and SAX-abccc to compute distance to string abccc */
        public Statistic(Stat type, int alphabetLength, int numLetters, String saxString) throws Exception {
            if (type!= Stat.saxdist) throw new Exception("Must be SAX or SAXDist");
            
            stat = type; 
            this.alphabetLength = alphabetLength;
            this.numLetters = numLetters;
            this.saxString = saxString;
        }
        
        public int getFreqIndex() throws Exception {
            if (!(stat == Stat.freq)) 
                throw new Exception("freq index only applies to frequency");

            return this.freqInt;
        }
        
        public int getLag() throws Exception {
            if (!(stat == Stat.granger)) 
                throw new Exception("lag only applies to granger");
            return this.lag;
        }

        public int getAlphaLength() throws Exception {
            if (!(stat == Stat.sax || stat == Stat.saxdist || stat == Stat.saxpair)) throw new Exception("Alphalength only applies to SAX");
            return this.alphabetLength;
        }
        
        public int getNumLetters() throws Exception {
            if (!(stat == Stat.sax || stat == Stat.saxdist || stat == Stat.saxpair)) throw new Exception("NumLetters only applies to SAX");
            return this.numLetters;
        }
        
        public String getSaxString() throws Exception{
            if (!(stat == Stat.saxdist))  throw new Exception("NumLetters only applies to SAX");
            return saxString;
        }
        public Stat getStat() {return stat;}
        
        
        
        /**Given a string return the enum type statistic associated with it*/
        public static Statistic getStatFromString (String stat) throws Exception{
            try {
               Statistic retStat;
               if (stat.startsWith("sax")) { //.. parse sax-2-2 = alpha =2 length =2
                   String [] parts =stat.split("-"); //.. 0 = SAX,
                   
                   //.. two cases: a) sax-2-3, b) sax-abcdd
                   if( parts.length ==3) {
                       int alpha = Integer.parseInt(parts[1]);
                       int numLetters = Integer.parseInt(parts[2]);
                       if(stat.startsWith("saxpair")) 
                           retStat = new Statistic(Stat.saxpair, alpha, numLetters);
                       else
                           retStat = new Statistic(Stat.sax, alpha, numLetters);
                   }
                   
                   else if(parts.length==2) {
                       String saxString = parts[1];
                       int numLetters = saxString.length();
                       int alpha = SAXAttribute.getLatestLetter(saxString);
                       retStat = new Statistic(Stat.saxdist, alpha, numLetters, saxString);
                   }
                   else
                       throw new Exception("Wrong number of parameters for sax stat: " + stat);
               }
               else if (stat.startsWith("freq")) {
                   String [] parts =stat.split("-");
                   int index = Integer.parseInt(parts[1]);
                   retStat = new Statistic(Stat.freq, index);
               }
               
               else if(stat.startsWith("granger")) {
                   String[] parts = stat.split("-");
                   int index = Integer.parseInt(parts[1]);
                   retStat = new Statistic(Stat.granger, index);
               }
               else{
                  retStat = new Statistic(Stat.valueOf(stat)); //.. make more advanced
               }
               return retStat;
            }catch (IllegalArgumentException p) {
                
                throw new Exception("There is not Stat called " + stat);
            }
        }
    }
    
    /**The window of this class*/
    public static class FSTimeWindow extends Technique {
       //.. time frame
       public int startTS=0; 
       public int endTS;
       
       public double startPct=0;
       public double endPct =1;
       public Timewindow timewindow; 
       public static enum Timewindow {FIRSTHALF, SECONDHALF, WHOLE, SPECIFIC, PERCENTAGE};
       
       public FSTimeWindow(int start, int end) {
            this.startTS = start;
            this.endTS = end;
            this.timewindow = Timewindow.SPECIFIC;
            this.id = start +"-"+end;
        }
        
        public FSTimeWindow(Timewindow tw) {
            this.timewindow = tw;
            this.id = tw.toString();
        }
        
        public FSTimeWindow(double start, double end) {
            this.startPct = start;
            this.endPct = end;
            this.id = start +"%"+end;
            this.timewindow = Timewindow.PERCENTAGE;
        }
        
        /**Get time window from a string. Throw an exception if it does not exist*/
        public static Timewindow getTimewindow(String tw) throws Exception{
           try {
             return Timewindow.valueOf(tw); //.. make more advanced
           }catch (IllegalArgumentException p) {
               throw new Exception("There is not timewindow called " + tw + " . There is FIRSTHALF, SECONDHALF, WHOLE");
           }
        }
        
        public String getTimeString() {
            if (timewindow == Timewindow.SPECIFIC)
                return startTS+ "-"+endTS;
            else return
                timewindow.toString();
        }
        
        /**minInc is the minimum gap of the string. Then we increment by the minimum and
         give each at that gap*/
        public static String getWithIncInc(float minInc) {
            float incStart = minInc;
            String timeString ="";
            while (incStart <= 1) {
              //  if (((1/incStart) == (int)(1/incStart))) //.. 
                timeString += FSTimeWindow.getWithInc(incStart);
                incStart += minInc;
                incStart = roundTwoDecimals(incStart);
                if (/*((1/incStart) == (int)(1/incStart)) &&*/ incStart <= 1) timeString += "^";
            }
            return timeString;
        }
        
       public static String getWithInc(float inc) {
          String ret = "";
          float start = 0;
         
          while (start +inc <= 1) {
               float end = start + inc;
               end = roundTwoDecimals(end);
               ret += start + "%"+end;
               start += inc;
               start = roundTwoDecimals(start);
               if (start +inc <= 1) ret += "^"; 
           }
           
           return ret;
       }
       public static float  roundTwoDecimals(float d) {
            DecimalFormat twoDForm = new DecimalFormat("#.##");
            return Float.valueOf(twoDForm.format(d));
        }
    }

    public static void main(String [] args) {
        System.out.println("s " +FSTimeWindow.getWithIncInc(0.25f));
        
       // float x = FSTimeWindow.roundTwoDecimals(2.3333f);
    }
    /**
     
     **/
    
    /**  A little messy, but this bridge structure is necessary
     for dynamic feature creation that does not know the underlying datalayer**/
    public static class FSDataLayer extends Technique{
       public static enum Type {ALL, MERGED, SINGLETON, PAIR}
       public Type type;
       public String channel; 
       public String endChannel;
       public String [] channels;
       
       public FSDataLayer(String[] channels){
           this.channels = channels;
           this.type = Type.MERGED;
           for (String s : channels) {
             this.id += s;
           }
       }
       
       /**Use for range of channels as well as pair of channels**/
       public FSDataLayer(String start, String end, Type type){
           this.channel = start;
           this.endChannel = end;
           this.type = type;
           this.id += start+"-"+end;
       }
       
       public FSDataLayer(String channel) {
           this.channel = channel;
           this.id = channel;
           if(channel.equals("ALL")) this.type =Type.ALL;
           else this.type = Type.SINGLETON;
       }
       
       
    }
    
    
}
