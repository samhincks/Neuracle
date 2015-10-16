package filereader.experiments;

import filereader.EvaluationInterface;
import filereader.TSTuftsFileReader;
import java.io.File;
import java.util.ArrayList;
import timeseriestufts.kth.streams.bi.ChannelSet;

/**
 *
 * @author shincks
 */


public class CHI2016Experiment  extends EvaluationInterface{
    public static double obs =0;
    public static void main(String [] args) {
        try{
            ArrayList<ChannelSet> files = CHI2016Experiment.getFiles();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static ArrayList<ChannelSet> getFiles() throws Exception{
        String folder = "build/web/chi";
        File folderF = new File("build/web/chi");
        if (folderF == null) {
            throw new Exception("Cannot find folder" + folder);
        }
        ArrayList<ChannelSet> ret = new ArrayList();
        File[] listOfFiles = folderF.listFiles();
        for (File f : listOfFiles) {
            //System.out.println(f.getName());
            TSTuftsFileReader reader = new TSTuftsFileReader();
            reader.FRAMESIZE = 0.09;
            ChannelSet cs = reader.readData(",", f, 1);
            ret.add(cs);
        }
        return ret;
    }
    
}
