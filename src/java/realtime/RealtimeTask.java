/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package realtime;

import dao.InputParser;
import dao.datalayers.BiDAO;
import dao.datalayers.TriDAO;
import dao.techniques.TechniqueDAO;
import filereader.Label;
import filereader.Labels;
import filereader.Markers;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONObject;
import stripes.ext.ThisActionBeanContext;
import timeseriestufts.evaluatable.TechniqueSet;
import timeseriestufts.kth.streams.bi.ChannelSet;
import timeseriestufts.kth.streams.tri.Experiment;

/**
 * @author samhincks
 */
public abstract class RealtimeTask  implements Runnable{
    static final String DBNAME ="realtime1";
    static final String CNAME = "condition";
    static String WCNAME; //.. THIS IS SET IN CODE
    static String EXPNAME = DBNAME + CNAME;
    static final String keep = "easy,hard";
    
    static final int REFRESHRATE = 1000; //.. how often should we synchronize with the database
    static final int CLASSIFICATIONDELAY = 3000; //.. how often do we want a classification
    static final int MOVINGWINDOW =2000; //.. how many readings back do we want to CLASSIFY FOR
    
    
    public ThisActionBeanContext ctx;
    public InputParser ip;
    public int ticks =0;
    String [] conditions = new String[]{"easy","hard"};
    
    public RealtimeTask(ThisActionBeanContext ctx, InputParser ip) {
        this.ctx = ctx;
        this.ip = ip;
    }  
    
    public static void main(String [] args) {
        try{
            //.. Initialize the components of the server, a hack, emulating hte interface
            ThisActionBeanContext ctx = new ThisActionBeanContext(true);
            InputParser ip = new InputParser();
            
            //.. Synchronize with the database, labeling all the existing parts junk
            String command = "synchronize("+DBNAME+")"; 
            String resp = ip.parseInput(command, ctx).getString("content");
            System.out.println(resp);
            
            //.. As new data is streamed in, assign a meaningful label to it
            TrainingTask rt = new TrainingTask(ctx, ip);
            Thread.sleep(REFRESHRATE);
            while(rt.ticks < 4){
                Thread t = new Thread(rt);
                t.run();
                Thread.sleep(REFRESHRATE);
            }
            
            //.. TRAIN a classifier, setting its identity in the context to WCNAME
            rt.train();
            
            //.. Set label of subsequent values to 'to-be-classified' or unknown
            command = "label("+DBNAME+","+CNAME+",unknown)";
            resp =  ip.parseInput(command, ctx).getString("content");
            
            //.. With a trained classsifier, classify data as it comes in
            ClassifyingTask ct = new ClassifyingTask(ctx, ip);
            while (ct.ticks < 5) {
                Thread t = new Thread(ct);
                t.run();
                Thread.sleep(CLASSIFICATIONDELAY);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public static class TrainingTask extends RealtimeTask{

        public TrainingTask(ThisActionBeanContext ctx, InputParser ip) {
            super(ctx, ip);
        }
        @Override
        public void run() {
            try{
                //.. Synchronize the last datapoints, and alter how the next ones will get labeled
                String command = "synchronize("+DBNAME+")";
                String resp = ip.parseInput(command, ctx).getString("content");
                System.out.println(resp);
                
                //.. For now, just alternate labels everyother, eventually listen to another program
                String con = conditions[ticks %conditions.length];
                command = "label("+DBNAME+","+CNAME+","+con+")";
                resp =  ip.parseInput(command, ctx).getString("content");
                System.out.println(resp);
                ticks++;
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        /**Train a classifier, allowing the trained classifier to be accessed from wcname
         **/
        public void train() throws Exception{
            TechniqueSet ts = TechniqueSet.generate();
            ctx.setCurrentName(DBNAME);
            
            //.. Having modified what we're selecting, split into an experiment
            String command = "split("+CNAME+")";
            JSONObject response = ip.parseInput(command, ctx);
            ctx.setCurrentName(EXPNAME);
            command = "keep("+keep+")";
            response = ip.parseInput(command, ctx);
            
            //.. after removing some instances the experiment name has changed, so hack a bit
            EXPNAME = response.getString("content").split("--")[1];
            ctx.setCurrentName(EXPNAME);

            //.. set techniques
            TriDAO tDAO = (TriDAO) ctx.getCurrentDataLayer();
            Experiment e = (Experiment) tDAO.dataLayer;
            e.printInfo();
            
            //.. Set intersecting techniques, so that the system knows how we want to train the ML
            TechniqueDAO wc = new TechniqueDAO(ts.getClassifier());
            tDAO.addConnection(wc);
            tDAO.addConnection(new TechniqueDAO(ts.getFeatureSet()));
            tDAO.addConnection(new TechniqueDAO(ts.getAttributeSelection()));
            
            //.. save the name of the classifier, and save it to the context
            WCNAME = wc.getId();
            ctx.getTechniques().addTechnique(wc.getId(), wc);
            
            //.. Train the machine learnign algorithm to the WC algorithm
            command = "train()";
            response = ip.parseInput(command, ctx);
        }
    }
    
    
    public static class ClassifyingTask extends RealtimeTask{
        public ClassifyingTask(ThisActionBeanContext ctx, InputParser ip) {
            super(ctx, ip);
        }
        @Override
        public void run() {
            try{
                //.. Retrieve the channel where data is still being streamed
                ctx.setCurrentName(DBNAME);
                BiDAO bDAO = (BiDAO) ctx.getCurrentDataLayer();
                
                //.. Retrieve the technique, and intersect it with the database to classify
                TechniqueDAO wc = ctx.getTechniques().get(WCNAME);
                bDAO.addConnection(wc);

                //.. Synchronize the dataset, making sure our classifications is up-to-date
                String command = "synchronize(" + DBNAME + ")";
                String resp = ip.parseInput(command, ctx).getString("content");

                //.. classify the specified last band of data
                command = "classifylast()";
                JSONObject response = ip.parseInput(command, ctx);
                System.out.println(response.get("content"));
                ticks++;
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
   
}
