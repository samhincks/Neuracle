package filereader.formatconversion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author shincks
 */


public class CodyFileReader {
    private BufferedReader fnirsDataIn;
    private BufferedReader markerDataIn;
    private String fnirsFile;
    private String markerFile;
    

    public ArrayList<CodyFileReader.Trial> trials; //.. an array of start end points for labeling 
    public String[] header;
    public ArrayList<String[]> data;
    int TSINDEX = 0; //.. where point index is in markers
    int LABELINDEX = 5; //.. where label index is in markers
    int FIRSTPROBEINDEX = 4;
    int STIMSTRINGINDEX = 3; //.. where String index is has for x;y;z;g
    int CONDINSTIM = 1; //.. where inside the STIMSTRING condition is
    
    private float READINGSPERSECOND =2;
    private float startCode = 0; //.. this must be set by reading the fnirs file
    public HashMap<String,String> conditions = new HashMap();

    public CodyFileReader(String markerFile, String fnirsFile) throws Exception {
        this.fnirsFile = fnirsFile;
        this.markerFile = markerFile;
        fnirsDataIn = new BufferedReader(new java.io.FileReader(fnirsFile));
        markerDataIn = new BufferedReader(new java.io.FileReader(markerFile));

    }
      
    /**Write out each data with correct labels as determined in marker file*/
    public void writeToFile(String outputName) throws Exception{
        if (trials == null || data == null) throw new Exception("Must read marker and data file before writing it out");
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputName));
        
        //.. write out the headers
        for (int i= FIRSTPROBEINDEX; i < header.length; i++) {
            bw.write(header[i]);
            bw.write(",");
        }
        
        //.. write out condition
        bw.write("condition");
        
        bw.write("\n");
        int rowNum =0;
        
        //.. write out actual data
        for (String [] row : data) {
            if (row.length!= header.length) System.out.println("Row has " + row.length + " whereas header has " + header.length);
            String label =null;
            for (int i = FIRSTPROBEINDEX; i < row.length; i++) {
                 label = getLabel(rowNum); //.. will be null if it doesn't exist
                 
                 if (label != null ) {
                    bw.write(row[i]);
                    bw.write(",");
                 }
            }
            if (label!=null) {
                //System.out.println("Writing " + getLabel(rowNum));
                String condition = getLabel(rowNum);
                bw.write(condition);
                bw.write("\n");
                
                //.. this is a hack since my program gives an error if a conditon jsut has one line
                if (condition.equals("passed")) duplicateLastLine(bw, row, condition,20 );
                
            }
            rowNum++;
        }
        
        bw.close();
    }
    
    private void duplicateLastLine(BufferedWriter bw, String[] row, String condition, int dup) throws Exception{
        for (int j = 0; j < dup; j++) {
            for (int i = FIRSTPROBEINDEX; i < row.length; i++) {
                bw.write(row[i]);
                bw.write(",");
            }
             bw.write(condition);
             bw.write("\n");
        }
    }
  
    //.. returns the label associated with this index as read in marker file
    private String getLabel(int index) throws Exception{
        for (CodyFileReader.Trial t : trials){
            if (t.start <= index && index < t.end) 
                return t.label;
        }
        
        return null;
    }
      /**Read data into data (ArrayList<String []>)*/
    public void readFNIRSFile(boolean keepFirst) throws Exception{
        if (trials == null) throw new Exception("read marker file first");
        
        chew(fnirsDataIn, "Other:"); //.. change this depending on file
        data = new ArrayList();
        
        String line;
        
        //.. read the file
        while ((line = fnirsDataIn.readLine()) != null) {
             String [] row = line.split("\t");
             if(keepFirst) data.add(row);
             else data.add( Arrays.copyOfRange(row, 1, row.length)); //. first might be time
        }
    }
    /**advances bufferedreader to line beyond specified string*/
    public void chew(BufferedReader bf, String until) throws Exception{
        String line;
        while ((line = bf.readLine()) != null) {
            if (line.startsWith(until)) return; //.. now buffered reader is in desired location
        }
        
        throw new Exception("Could not find text " + until);
    }
    
    public ArrayList<CodyFileReader.Trial> readMarkerFileCody() throws Exception{
        String line;
        int errors =0; 
        trials = new ArrayList();
                
        String condition = null;
        float lastStartTime =0;
        while ((line = markerDataIn.readLine()) != null) {
            String [] vals = line.split("\t");
            
            //.. set the previous trial if there was one
            float startTime = Float.parseFloat(vals[0]);
            if (condition!= null) {
                Trial t = new Trial(this.getIndexFromTime(lastStartTime), this.getIndexFromTime(startTime), condition);
                trials.add(t);
            }
            else { //.. its baseline, or junk -- what we dont have markers for, assuming fnirs start code is before actual
                Trial t = new Trial(0, this.getIndexFromTime(startTime), "junk");
                trials.add(t);
            }
            lastStartTime = startTime;
            
            condition = conditions.get(vals[1]);
            if (condition == null) {
                condition = "Con"+vals[1];
                conditions.put(vals[1], condition);
            }
            
            
        }
        markerDataIn.close();
        if (errors>0)
            System.err.println("There were " + errors + " errors, with either unstarted ends or unended starts");
        return trials;
    }
    
    /** The startcode time stamp has index 0. Everything
     **/
    public int getIndexFromTime(float time) {
        return (int) ((int)(time - this.startCode) * this.READINGSPERSECOND);
    }
    
    /** Advance reading in fnirs file until we see Start Code: xxx
     * When we do, stop reading, and return. If we dont see it throw an exception
     **/
    public void setStartCode() throws Exception {
        String line;
        while ((line = this.fnirsDataIn.readLine()) != null) {
            if (line.startsWith("Start Code:")) {
                String [] vals = line.split("\t");
                this.startCode = Float.parseFloat(vals[1]);
                return;
            } //.. now buffered reader is in desired location
        }
        throw new Exception("Could not find line Start Code:");
    }
    
    public void setHeaders(int columns) {
        header = new String[columns];
        for (int i = 0; i < header.length; i++) {
           header[i] = "CH"+i;
        }
    }
    
    public static void main(String[] args) {
        try{
            String folder = "input/Honda/";
            String marker = folder + "dat_out_C.mrk";
            String input = folder +"dat_out.xls.oxy";
            String output = "input/Honda/" + "codyConverted" + ".csv";
            System.out.println("Making " + output);
            
            //.. initializes new reader, and if desired, specify synonym for marker. It must be non-interpretable as a number to work with Neuracle 
            CodyFileReader reader = new CodyFileReader(marker, input);
            reader.conditions.put("101", "A");
            reader.conditions.put("102", "B");
           
            //.. search and set Start code, advancing position in fnirs file
            reader.setStartCode();
            reader.setHeaders(32); //.. if conditions don't have names in the file, 
            
            ArrayList<CodyFileReader.Trial> trials = reader.readMarkerFileCody();
            reader.readFNIRSFile(false); //.. we dont want the first value
             reader.writeToFile(output);

            for (CodyFileReader.Trial t : trials) {
                //   System.out.println("start: " + t.start + " - " + t.end + " .. "+t.label);
            }
        }
        catch (Exception e) {e.printStackTrace();}
    }
       
    public class Trial {
        int start;
        int end;
        String label;
        public Trial(int start, int end, String label) {
            this.start = start;
            this.end = end;
            this.label = label.replace("end", "");
            this.label = this.label.replace("start", "");
        }
        
       
    }
}
