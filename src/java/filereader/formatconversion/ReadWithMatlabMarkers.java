/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package filereader.formatconversion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**An independent component. Use it to translate the raw output of a matlab file 
 * to the fNIRS format the program wants. 
 * 
 * @author samhincks
 */
public class ReadWithMatlabMarkers {
    private BufferedReader fnirsDataIn;
    private BufferedReader markerDataIn;
    private String fnirsFile;
    private String markerFile;
    
    public ArrayList<ReadWithMatlabMarkers.Trial> trials; //.. an array of start end points for labeling 
    public String [] header;
    public ArrayList<String []> data; 
    int TSINDEX =0; //.. where point index is in markers
    int LABELINDEX = 5; //.. where label index is in markers
    int FIRSTPROBEINDEX = 4;
    int STIMSTRINGINDEX = 3; //.. where String index is has for x;y;z;g
    int CONDINSTIM = 1; //.. where inside the STIMSTRING condition is
    String DELIMETER = ",";
    
    public ReadWithMatlabMarkers(String markerFile, String fnirsFile) throws Exception{
         this.fnirsFile = fnirsFile;
         this.markerFile = markerFile;
         fnirsDataIn = new BufferedReader(new java.io.FileReader(fnirsFile));
         if(markerFile!= null)markerDataIn = new BufferedReader(new java.io.FileReader(markerFile));
    }
    
    public ReadWithMatlabMarkers() {
        
    }
    
    /**  RMCRCM until participant 9 and from 9 forward we changed to RCMRMC. (R = rest, M = meditation, C = control).
     **/
    public  ArrayList<ReadWithMatlabMarkers.Trial> readMarkerFileLeanne(int index) throws Exception {
         trials = new ArrayList();
        boolean readingStart = true; //.. set to false after we have read start
        HashMap<Integer, String> hm = new HashMap();
        
        if (index == 3 || index == 5 || index == 6 || index == 7 || index == 8 ) {
            hm.put(0, "Junk");
            hm.put(1, "Rest");
            hm.put(2, "Meditation");
            hm.put(3, "Control");
            hm.put(4, "Rest");
            hm.put(5, "Control");
            hm.put(6, "Meditation");
        }
        else {
            hm.put(0, "Junk");
            hm.put(1, "Rest");
            hm.put(2, "Control");
            hm.put(3, "Meditation");
            hm.put(4, "Rest");
            hm.put(5, "Meditation");
            hm.put(6, "Control");
        }
        
        int start =0;
        int end =0;
        String line;
        int markerIndex = 16;
        int curIndex =2;
        int lastMarker = -1;
        fnirsDataIn.readLine(); //.. read away markers
        fnirsDataIn.readLine(); //.. read away markers
        Trial lastTrial = new Trial(curIndex, "junk"); 

        while ((line = fnirsDataIn.readLine()) != null) {
            String [] vals = line.split(DELIMETER);
            int marker = Integer.parseInt(vals[markerIndex]);
            if (marker != 0) {
                if (hm.containsKey(marker)){
                   lastTrial.end = curIndex;
                   trials.add(lastTrial);
                   start = curIndex;
                   lastTrial = new Trial(start, hm.get(marker));
                }
            }
           
            lastMarker = marker;
            curIndex++;
        }
        
        return trials;
    }
    
   
    /**
     */
    public ArrayList<ReadWithMatlabMarkers.Trial> readMarkerFileDan() throws Exception{
        String line;
        chew(markerDataIn, "NbPoints");
        int errors =0; 
        trials = new ArrayList();
        boolean readingStart = true; //.. set to false after we have read start
        int start =0;
        int end =0;
        
        while ((line = markerDataIn.readLine()) != null) {
            String [] vals = line.split("\t");
            if (readingStart) {
                int cur = Integer.parseInt(vals[TSINDEX]);
                String label = vals[STIMSTRINGINDEX];
                String[] semic = label.split(";");
                label = semic[CONDINSTIM];
                if (label.contains("startClassifying")) {
                    markerDataIn.close();
                    return trials;
                }
                else if(label.contains("start")) {
                    readingStart = true;
                    start = cur;
                }
                else if(label.contains("passed")) {
                    ReadWithMatlabMarkers.Trial trial = new ReadWithMatlabMarkers.Trial(cur, cur+5, "bogus");
                    trials.add(trial);
                }
               
                else  { //.. something to denote its ending
                   ReadWithMatlabMarkers.Trial trial = new ReadWithMatlabMarkers.Trial(start, cur, label);
                   trials.add(trial);
                    
                }  
            }
        }
        markerDataIn.close();
        if (errors>0)
            System.err.println("There were " + errors + " errors, with either unstarted ends or unended starts");
        return trials;

    }
    /**Read marker file that robusly handles error cases
     * There is an end without a start --> discard and continue reading
     * There is a start without an end --> discard and continue reading
     */
    public ArrayList<ReadWithMatlabMarkers.Trial> readMarkerFileBeste() throws Exception{
        String line;
        chew(markerDataIn, "NbPoints");
        int errors =0; 
        trials = new ArrayList();
        boolean readingStart = true; //.. set to false after we have read start
        int start =0;
        int end =0;
        
        while ((line = markerDataIn.readLine()) != null) {
            String [] vals = line.split("\t");
            if (readingStart) {
                start = Integer.parseInt(vals[TSINDEX]);
                String startLabel = vals[LABELINDEX];

                if (!(startLabel.contains("start"))) { //.. this is a start file where we expected end
                    readingStart =true; //.. so next should be start anyway, and we will ignore this
                    errors++;
                }
                else { //.. as expected this is a start, so next read an end poitn
                    readingStart = false;
                }
            }
            
            else { //.. reading end
                 end = Integer.parseInt(vals[TSINDEX]);
                 String endLabel = vals[LABELINDEX];
                if (!(endLabel.contains("end"))) { //.. we expected an end point but read a start point
                    readingStart =false; //.. We must save this start point and treat next as the end to this
                    start =end; //.. save the start point
                    errors++;
                }
                else {//.. now we have a complete start and end, save to trials
                    readingStart = true;
                    ReadWithMatlabMarkers.Trial trial = new ReadWithMatlabMarkers.Trial(start, end, endLabel);
                    trials.add(trial);
               }
            }
        }
        markerDataIn.close();
        if (errors>0)
            System.err.println("There were " + errors + " errors, with either unstarted ends or unended starts");
        return trials;

    }
    
    /**advances bufferedreader to line beyond specified string*/
    public void chew(BufferedReader bf, String until) throws Exception{
        String line;
        while ((line = bf.readLine()) != null) {
            if (line.startsWith(until)) return; //.. now buffered reader is in desired location
        }
        
        throw new Exception("Could not find text " + until);
    }
    
    
    /**Read data into data (ArrayList<String []>) and header into header[]*/
    public void readFNIRSFile(boolean chew) throws Exception{
        if (trials == null) throw new Exception("read marker file first");
       fnirsDataIn = new BufferedReader(new java.io.FileReader(fnirsFile));

        if (chew)chew(fnirsDataIn, "Time:");
        fnirsDataIn.readLine(); //.. read white space
        data = new ArrayList();
        
        //.. read column file
        String line = fnirsDataIn.readLine();
        header =line.split(DELIMETER);
        
        while ((line = fnirsDataIn.readLine()) != null) {
             String [] row = line.split(DELIMETER);
             data.add(row);
        }
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
            for (int i = FIRSTPROBEINDEX; i < row.length-1; i++) {
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
        for (ReadWithMatlabMarkers.Trial t : trials){
            if (t.start <= index && index < t.end) 
                return t.label;
        }
        
        return null;
    }
    
    public ArrayList<ReadWithMatlabMarkers.Trial> readMarkerFileRemote(BufferedWriter bw,File f,int chanOfInterest, int markerIndex) throws Exception {
        fnirsDataIn = new BufferedReader(new java.io.FileReader(f));
        String line; 
        int index =0;
        String condition = "t"+index;
        while ((line = fnirsDataIn.readLine()) != null) {
            String[] vals = line.split(DELIMETER);
            int marker = Integer.parseInt(vals[markerIndex]);
            String chanVal = vals[chanOfInterest];
            bw.write(chanVal+","+condition+"\n");
            if (marker != 0) {
                index++; 
                condition = "t"+ index;
            }
            

        }

        return trials;
    }
    
    public static void main(String[] args) {
        Remote();
    }
    public static void Remote() {
        String folderName = "input/Remote_processed/E3/csv";
        File folder = new File(folderName);
        String outputName = "input/Remote_processed/E3/P1E3.csv";

        try {
            ReadWithMatlabMarkers reader = new ReadWithMatlabMarkers();
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputName));
            bw.write("chan" + 0 + ",condition\n");

            for (File f : folder.listFiles()) {
                System.out.println(f.getName());
                if(!(f.getName().startsWith("."))) {
                    if (f.getName().startsWith("P1_E3_T3_ISSDataHbO")){
                        reader.readMarkerFileRemote(bw,f,0,8);
                    }
                    if (f.getName().startsWith("P1_E3_T2_CMOS_dataHbO")) {
                        reader.readMarkerFileRemote(bw, f, 0, 4);
                    } 
                    if (f.getName().startsWith("P1_E3_T1_PIXIS_dataHbO")) {
                        reader.readMarkerFileRemote(bw, f, 0, 4);
                    }
                }
                
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void Meditation() {
        String folder = "input/meditation/";
        try {
            for (int i = 1; i < 14; i++) {
                if (i == 1) {
                   // String filename = folder +"p"+i+"_HbO_Hb.txt";
                    String filename = folder + "oxy_deoxy_p1_meditation.csv";
                     String output = "input/meditation_processed/"+i  + ".csv";
                     System.out.println("Making" + output);
                     ReadWithMatlabMarkers reader = new ReadWithMatlabMarkers(null, filename);
                     ArrayList<ReadWithMatlabMarkers.Trial> trials = reader.readMarkerFileLeanne(i);
                     reader.readFNIRSFile(false);
                     reader.writeToFile(output);
                     for (Trial t : trials) {
                            System.out.println("start: " + t.start + " - " + t.end + " .. "+t.label);
                     }
                }

                 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static void GR() {
        String folder = "input/Glassroutes/";
        try {
            for (int i = 1; i < 20; i++) {
                if(i!= 17){
                    String subjNum = (i<10) ? "0" +i : i+"";
                    String subjFolder = "glassroutes_S2"+subjNum;
                    String input = folder +subjFolder+ "/fnirsData.txt";
                    String marker = folder + subjFolder + "/markers.txt";
                    String output = "input/" + subjNum + ".csv";
                    System.out.println("Making" + input);

                    ReadWithMatlabMarkers reader = new ReadWithMatlabMarkers(marker, input);
                    ArrayList<ReadWithMatlabMarkers.Trial> trials = reader.readMarkerFileDan();
                    reader.readFNIRSFile(true);
                    reader.writeToFile(output);
                    for (Trial t : trials) {
                      //     System.out.println("start: " + t.start + " - " + t.end + " .. "+t.label);
                    }
                }
                
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

    }
    public static void UAV () {
        String folder = "input/UAV_selected/";
        try{
            for (int i =0; i < 28; i++){
                int subjNum=i;
                
                String input=folder+subjNum+"/fnirsData.txt";
                String marker=folder+subjNum+"/markers.txt";
                String output = "input/UAV_processed/"+subjNum+".csv";
                System.out.println("Making" + output);

                ReadWithMatlabMarkers reader = new ReadWithMatlabMarkers(marker,input);
                ArrayList<ReadWithMatlabMarkers.Trial> trials = reader.readMarkerFileDan();
                reader.readFNIRSFile(true);
                reader.writeToFile(output);


                for (Trial t : trials) {
                 //   System.out.println("start: " + t.start + " - " + t.end + " .. "+t.label);
                }
                
            
                    
             }
              //  break;
            
        }
        catch(Exception e) {e.printStackTrace();}
    }

    
    public class Trial {
        int start;
        int end;
        String label;
        public Trial (int start, String label) {
            this.start = start;
            this.label = label;
        }
        public Trial(int start, int end, String label) {
            this.start = start;
            this.end = end;
            this.label = label;
            //this.label = label.replace("end", "");
            //this.label = this.label.replace("start", "");
        }
        
       
    }
    
}
