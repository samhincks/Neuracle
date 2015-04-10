/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package filereader.experiments;

import filereader.TSTuftsFileReader;
import java.util.ArrayList;
import timeseriestufts.kth.streams.bi.ChannelSet;

/**
 *
 * @author samhincks
 */
public class Honda2015 {
    public static void main(String[] args) {
        try{
           read();
        }
        catch(Exception e) { e.printStackTrace();}
    }
   
    
   private static void read() throws Exception {
        ArrayList<String> files = Honda2015.getSpecific();
        for (String s : files) {
            ChannelSet cs = getChannelSet(s);
            cs.printInfo();
        }
    }
    private static ChannelSet getChannelSet(String s) throws Exception {
        TSTuftsFileReader f = new TSTuftsFileReader();
        f.FRAMESIZE = 0.09;

        ChannelSet cs = f.readData("\t", s,1);
        return cs;
    }
    public static ArrayList<String> getSpecific() {
        ArrayList<String> files = new ArrayList();
        String folder = "input/Honda/";
   
        files.add(folder +"dat_out.xls"); //.. NEXT READ THE DAT_OUT file, and implement tab separation
        return files;
    }
               
    
}
