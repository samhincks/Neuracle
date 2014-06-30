/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package filereader;

import java.util.ArrayList;
import timeseriestufts.kth.streams.bi.ChannelSet;
import timeseriestufts.kth.streams.uni.Channel;

/**Class for building a ChannelSet from a series of asynchronously passed strings. 
 * Not in use -- too unreliable. We don't know first comes first, and when to complete datalayer. 
 * 
 * @author samhincks
 */
public class ChannelSetFromStringsMaker {
    
    /**If we are building from a series of strings passed to the server **/
    private String [] messages; //.. build this one up with messages passed from the server
    int totalMessages; 
    int added =0;
    public ChannelSet channelSet;
    
    private String delimeter;
    private String filename;
    public int readingErrors =0;
    public int readEvery=1;
    public double FRAMESIZE =1;
    
    
    /**
     * Reads a file given data organized as a set of lines and not as a file;
     * call this method for example if we are passing data message by message
     * from a client through strings asynchronously. A bit sloppy that this is
     * copmletely distinct code from the above method, but oh well. Really only
     * three lines change, all of which pertain to reading the file
     */
    public ChannelSet readDataRowWise(String delimeter, String[] lines) throws Exception {
        String line;

        ArrayList<ArrayList<Float>> rawValues = new ArrayList(); //.. which we will read into proper structure
        ArrayList<String> channelNames = new ArrayList();
        ArrayList<Labels> allLabels = new ArrayList();

        //.. read first row, which should be labels
        line = lines[0];
        String[] columnNames = line.split(delimeter);

        //.. throw exception if split return just one value
        if (columnNames.length == 0) {
            throw new Exception("Must have at least one column. Found 0 with delimeter " + delimeter);
        }
        
        //.. peak at first line, and determine whether the column refers to a number or a string
        //.. if column name starts with mark then we treat as nominal regardless
        if (lines.length <2) throw new Exception ("Must have atleast two lines in file " + filename + " there are " + lines.length + " lines");
        line = lines[1];
        String[] firstValues = line.split(delimeter);
       
        //.. make an array with as many rows, holding true if this column refers to a channel
        boolean[] isChannel = new boolean[columnNames.length];
        boolean mustBeNominal = false; //.. use this variable  if we want to follow convention that nominals follow channels

        for (int i = 0; i < firstValues.length; i++) {
            String firstVal = firstValues[i];
            String colName = columnNames[i] = columnNames[i].trim().toLowerCase();

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
                    mustBeNominal = true;
                } //.. else, this is a number and we're gonna treat it like a channel
                else {
                    isChannel[i] = true;
                    //.. make a new channel for the values
                    ArrayList<Float> chan = new ArrayList(); //.. create new channel
                    chan.add(f);
                    rawValues.add(chan);

                    //.. save the channel name for when we initialize our 1D structure
                    channelNames.add(colName);
                }
            } //.. if its a String, treat as label
            catch (NumberFormatException n) {
                isChannel[i] = false;
                Labels labels = new Labels(colName);
                allLabels.add(labels);

                //.. Also, add this label
                Label label = new Label(colName, firstVal, 0);
                labels.addLabel(label);
                mustBeNominal = true; //.. from here on it must be label
            }
        }

        //..Now we've initalized columns and added first. 
        for (int index = 2; index < lines.length; index++) {
            line = lines[index];
            String[] values = line.split(delimeter);
            int channelIndex = 0;
            int allLabelsIndex = 0;

            if (index % readEvery == 0) { //.. ONLY EVERY KTH if readEvery > 1!
                //.. for each value, add to appropriate structure: channel vs label
                for (int i = 0; i < values.length; i++) {
                    String val = values[i];
                    String colName = columnNames[i];

                    //.. if we know this is a channel
                    if (isChannel[i]) {
                        Float f = Float.parseFloat(val);
                        rawValues.get(channelIndex).add(f);
                        channelIndex++;
                    } //.. based on reading first line, we know this is a label
                    else {
                        Labels labels = allLabels.get(allLabelsIndex);
                        Label label = new Label(colName, val, index);
                        labels.addLabel(label);
                        allLabelsIndex++;
                    }
                }
            }

        }

        //.. Structure for holding ALL channels
        channelSet = new ChannelSet();
        channelSet.setId(filename);
        int index = 0;
        for (ArrayList<Float> raw : rawValues) {
            Channel c = new Channel(FRAMESIZE, raw.size()); //.. we didnt add directly to structure since we didnt know raw's size
            c.setId(channelNames.get(index));

            //.. add each value
            for (Float f : raw) {
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

    /**
     * If we are building a file from a series of strings, call this method to
     * initialize the file. If its the first added, initalize, if its the last, 
     * then complete the file. 
     * When this procedure is complete, ie, added = totalSize, then channelset will 
     * no longer be null. 
     */
    public void addMessage(int index,int totalSize, String filename, String data) throws Exception {
        //.. if this is the first one added (regardless of index), we must initialize messages 
        if (added ==0){
            this.filename = filename;
            this.delimeter = ",";
            messages = new String[totalSize]; 
        }
        
        //.. if the filename is different from the one promised, throw an error
        if (!(this.filename.equals(filename))) throw new Exception("Filename differs from the first added");
        
        messages[index] = data;
        added++;
        System.err.println("added = " + added + "ts " + totalSize);
        //.. when we have added each part, make into a chennelset
        if (added == totalSize) {
            String fullMessage = "";
            System.out.println(fullMessage);
            for (String message : messages) {
                fullMessage += message;
            }
            //.. this is a critical error point. And I'm gonna have to leave a message
            //.. bug : the \n is not passed so must replace with cr13
            String[] lines = fullMessage.split("cr13"); //.. are there other possible new line separators? 
            for (String s: lines) {System.out.println(s);}
            channelSet =readDataRowWise(delimeter, lines);
        }
    }


    /**Next step is to unit test this part. I've set it up so that files are passed as a set of messages,
     and I'm currently working so that  in the asynchronous system, it doesnt matter if one request
     reaches the destination before a prior one. Next step is to verify this all works in here,
     and then to add support in BiDao and InputParser, before I will test things within
     the actual interfacel **/
    public static void main(String[] args) {
        try {
            ChannelSetFromStringsMaker tt = new ChannelSetFromStringsMaker();
            int rows =6;
            tt.addMessage(0, rows, "bajs", "a\n");
            for (int i = 1; i < rows; i++) {
                tt.addMessage(i, rows, "bajs", String.valueOf(i) +"\n");
            }
            ChannelSet cs = tt.channelSet;
            cs.printStream();
            
            tt = new ChannelSetFromStringsMaker();
            tt.addMessage(2, 3, "test2", "1,2\n");
            tt.addMessage(0, 3, "test2", "a,b");
            tt.addMessage(1, 3, "test2", "\n");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
