/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 * 
 * CHECK OUT http://opencsv.sourceforge.net/#what-is-opencsv
 */
package filereader;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import net.sourceforge.stripes.action.FileBean;
import timeseriestufts.evaluatable.AttributeSelection;
import timeseriestufts.evaluatable.FeatureSet;
import timeseriestufts.evaluatable.TechniqueSet;
import timeseriestufts.evaluatable.WekaClassifier;
import timeseriestufts.kth.streams.bi.ChannelSet;
import timeseriestufts.kth.streams.uni.Channel;

/**
 *
 * @author samhincks
 */
public class TSTuftsFileReader {
        
    private BufferedReader dataIn;
    private String filename;
    private String  delimeter;
    public int readingErrors =0;
    public int readEvery=1;
    public double FRAMESIZE =1;
    
    
    
    /** Read data: by default do ColumnWise Ch1,Ch2,Ch3\n 1,2,3 , but if this fails
     * try reading row wise, Ch1,1,2,3,4
     * 
     */
    public ChannelSet readData(String delimeter, String dataFile) throws Exception{
        filename = dataFile;
        System.out.println("trying to read" + filename);
        dataIn = new BufferedReader(new java.io.FileReader(dataFile));
        this.delimeter= delimeter;
        return readDataRowWise();
    }
     public ChannelSet readData(String delimeter, FileBean dataFile) throws Exception{
         filename = dataFile.getFileName();
         dataIn = new BufferedReader(dataFile.getReader());
         this.delimeter = delimeter;
         return readDataRowWise();

    }
    /**Reads input file where columns hold channels and labels.
     * 
     * -Determines whether rows refer to labels or values by examining first row and treating it
     * as channel if it can parse it as a double and the columnname does not start with mark
     * 
     * -After first nominal/class column, the remaining channels are treated as nominal
     * X,Y,Z,V,D
     * 1,2,3,x,1
     * 2,4,5,x ,0
     * D will be treated as nominal
     * 
     */
    public ChannelSet readDataRowWise() throws Exception {
        String line;
        
        ArrayList<ArrayList<Float>> rawValues = new ArrayList(); //.. which we will read into proper structure
        ArrayList<String> channelNames = new ArrayList();
        ArrayList<Labels> allLabels = new ArrayList();
       
        //.. read first row, which should be labels. But if they aren't fabricate labels
        
        String [] columnNames = getFirstOrFabricated();
        
        //.. throw exception if split return just one value
        if(columnNames.length ==0) throw new Exception("Must have at least one column. Found 0 with delimeter "+delimeter);
        
        //.. peak at first line, and determine whether each column refers to a number or a string
        //.. if column name starts with mark then we treat as nominal regardless
        line = dataIn.readLine();
        String [] firstValues = line.split(delimeter);
        
        //.. make an array with as many rows, holding true if this column refers to a channel
        boolean [] isChannel = new boolean[columnNames.length];
        boolean mustBeNominal = false; //.. use this variable  if we want to follow convention that nominals follow channels
        
        for (int i = 0; i < firstValues.length; i++) {
            String firstVal = firstValues[i];
            String colName= columnNames[i] = columnNames[i].trim().toLowerCase();
            
             //.. if its a number, make a new channel
            try { 
                 Float f = Float.parseFloat(firstVal); 
                 
                 //.. We may have succeeded in parsing it as a double, but make it a label anyway
                 if (colName.startsWith("mark") || mustBeNominal) {
                     isChannel[i] = false;
                     Labels labels = new Labels(colName);
                     allLabels.add(labels);
                     
                     //.. Also, add this label
                     Label label = new Label(colName, firstVal, 0);
                     labels.addLabel(label);
                     mustBeNominal= true;
                 }
                 
                 //.. else, this is a number and we're gonna treat it like a channel
                 else {
                     isChannel[i] = true;
                     //.. make a new channel for the values
                     ArrayList<Float> chan = new ArrayList(); //.. create new channel
                     chan.add(f);
                     rawValues.add(chan);
                     
                     //.. save the channel name for when we initialize our 1D structure
                     channelNames.add(colName);
                 }
                }

                //.. if its a String, treat as label
                catch(NumberFormatException n ) {
                     isChannel[i] = false;
                     Labels labels = new Labels(colName);
                     allLabels.add(labels);
                     
                     //.. Also, add this label
                     Label label = new Label(colName, firstVal, 0);
                     labels.addLabel(label);
                     mustBeNominal =true; //.. from here on it must be label
                }
        }
        
        //..Now we've initalized columns and added first. 
        int index =1; //.. since we added the first
        while ((line = dataIn.readLine()) != null) {
           
            String [] values = line.split(delimeter);
            int channelIndex=0;
            int allLabelsIndex =0;
            
            if (index % readEvery ==0){ //.. ONLY EVERY KTH if readEvery > 1!
                //.. for each value, add to appropriate structure: channel vs label
                for (int i =0; i < values.length; i++) {
                    String val = values[i];
                    String colName = columnNames[i]; 

                    //.. if we know this is a channel
                    if(isChannel[i]) {
                        //System.err.println(this.filename + " " + index + " , " + i);
                        Float f = Float.parseFloat(val); 
                        rawValues.get(channelIndex).add(f);
                        channelIndex++;
                    }

                    //.. based on reading first line, we know this is a label
                    else {
                        Labels labels = allLabels.get(allLabelsIndex);
                        Label label = new Label(colName, val, index);
                        labels.addLabel(label);
                        allLabelsIndex++;
                    }
                }
            }
            
            index++;
        }
        
        //.. Structure for holding ALL channels
        ChannelSet channelSet = new ChannelSet();
        channelSet.setId(filename);
        index =0;
        for (ArrayList<Float> raw : rawValues) {
            Channel c = new Channel(FRAMESIZE, raw.size()); //.. we didnt add directly to structure since we didnt know raw's size
            c.setId(channelNames.get(index));
            
            //.. add each value
            for (Float f: raw){
                c.addPoint(f);
            }
            
            channelSet.addStream(c);
            index++;
        } 
       
        //.. Having built a channel structure and label structure, label the channel
        //... structure according to the label structure
        for (Labels l : allLabels) {
            Markers markers = new Markers(l);
            channelSet.addMarkers(markers);
        }
        
        return channelSet;
    }

    /** Returns an array of column names in all cases.
     * If the first line is an array of column names, then that is returned.
     * If it isn't, it checks to see if it seems like an array of validly delimeted values, and returns that.
     * Otherwise it chews until it sees a delimited line. DOESNT WORK IF FIRST LINES HAVE THAT DELIMETER
     **/
    private String[] getFirstOrFabricated() throws Exception{
        String line = dataIn.readLine();
        String[] firstLine = line.split(delimeter);
        //.. Valid nominal line
        if(isNominal(firstLine)) return firstLine;
        //.. Crap we should chew away
        if (firstLine.length < 2) return getFirstOrFabricated(); //.. recursively chew away
        //. Fabricate columns, but we also need to save what we read
        String [] fabricated = new String[firstLine.length];
        for (int i = 0; i < fabricated.length; i++) {
            fabricated[i] = "ch"+i;
        }
        return fabricated;
        
    }
    
    public boolean isNominal(String [] line) {
        if (line.length< 2) return false; //.. ugh
        try{
            float f = Float.parseFloat(line[0]);
            return false;
        }
        catch(Exception e) {
            return true;
        }
                
    }
  
   
}
