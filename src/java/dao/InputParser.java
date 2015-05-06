package dao;

import dao.datalayers.BiDAO;
import dao.datalayers.DataLayerDAO;
import dao.datalayers.DataLayersDAO;
import dao.datalayers.MySqlDAO;
import dao.datalayers.QuadDAO;    
import dao.datalayers.TriDAO;
import dao.datalayers.UserDAO;
import dao.techniques.TechniqueDAO;
import dao.techniques.TechniquesDAO;
import filereader.Label;  
import filereader.Labels;
import filereader.Markers;
import filereader.experiments.BesteExperiment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader; 
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;  
import java.util.UUID;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.StreamingResolution;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import realtimereceiver.Client;
import stripes.ext.ThisActionBeanContext;
import timeseriestufts.evaluatable.*;
import timeseriestufts.evaluatable.performances.Performances;
import timeseriestufts.evaluatable.performances.Predictions;
import timeseriestufts.evaluation.experiment.Classification;
import timeseriestufts.kth.streams.DataLayer;
import timeseriestufts.kth.streams.bi.BidimensionalLayer;
import timeseriestufts.kth.streams.bi.ChannelSet;
import timeseriestufts.kth.streams.quad.MultiExperiment;
import timeseriestufts.kth.streams.tri.*;
import timeseriestufts.kth.streams.uni.Channel;

/** Handles input from the client
 * @author samhincks
 */
public class InputParser {
    public ArrayList<String> oldInput = new ArrayList();
    private DataLayer currentDataLayer;
    private Technique currentTechnique;
    private ThisActionBeanContext ctx;
    public double FRAMESIZE =1;
    
    private MiscellaneousParser miscParser;
    private ExternalDataParser dataParser;
    private TransformationParser transformationParser;
    private DataManipulationParser mlParser;
   
    public InputParser(ThisActionBeanContext ctx) {
        miscParser = new MiscellaneousParser(ctx);
        dataParser = new ExternalDataParser(ctx);
        transformationParser = new TransformationParser(ctx);
        mlParser = new DataManipulationParser(ctx);
        this.ctx = ctx;
    } 
    
    /** Parse the input, using one of the other parsers.
     * @param input
     * @return
     * @throws JSONException
     * @throws Exception 
     */
    public JSONObject parseInput(String input) throws JSONException, Exception {
        JSONObject jsonObj = null; 
        try {
            //.. remove upper letters; trim spaces
            input = input.toLowerCase();
            input = input.trim();
            input = input.replace(" ", "");
            input = input.replace("\"", "");
            
            //.. retrieve the data layer that's currently selected
            DataLayerDAO dl = ctx.getCurrentDataLayer();
            if (dl != null) {
                currentDataLayer = dl.dataLayer;
            } 
   
            //.. retreive the techinique that's currently selected
            TechniqueDAO techDAO = ctx.getCurrentTechnique();
            if (techDAO != null) {
                currentTechnique = techDAO.technique;
            }
            
            //String command = input.split("(")[0];
            String [] parameters = this.getParameters(input);
            jsonObj = this.rapidExecute(input,parameters, ctx, currentDataLayer,techDAO);
            if (jsonObj ==null){
                jsonObj = miscParser.execute(input,parameters, currentDataLayer,techDAO); } 
            
            if (jsonObj == null) {
                jsonObj = dataParser.execute(input,parameters,currentDataLayer,techDAO); }
                
            if (jsonObj ==null){
                jsonObj = transformationParser.execute(input, parameters,currentDataLayer,techDAO);  }
            
            if (jsonObj ==null){
                jsonObj = mlParser.execute(input, parameters,currentDataLayer,techDAO);}
            
             
             //.. finally if nothing recognized this command, complain at user
             if (jsonObj == null) {
                 jsonObj = new JSONObject();
                 jsonObj.put("error", "No command " + input + " exists");
             }
                 
             return jsonObj;
         }
        
         catch (Exception e) {
            e.printStackTrace(); //.. comment this out when deliver a stable version
            jsonObj = new JSONObject();
            jsonObj.put("error", e.getMessage());
            return jsonObj;
        }
    }
 
    /** Rapidly test a command, without creating documentation for it. Ie, parse it here,
     * as opposed to in one of the subdirectories. 
     * @param command
     * @param parameters
     * @param ctx
     * @param currentDataLayer
     * @param techDAO
     * @return
     * @throws Exception 
     */
   public JSONObject rapidExecute(String command, String[] parameters, ThisActionBeanContext ctx,
            DataLayer currentDataLayer, TechniqueDAO techDAO) throws Exception {
        this.currentDataLayer = currentDataLayer;
        Command c = null;

        if (command.startsWith("debug")) {
             c = new Command("debug");
             c.retMessage = "selected = " + currentDataLayer.id;
        }
        else if (command.startsWith("exdebug")) {
             c = new Command("debug2");
             throw new Exception("Here is the message that should be displayed in red");
        }    
        
        else if (command.startsWith("readevery")) {
            c = new Command("readevery");
            if (parameters.length ==0) c.retMessage = "readevery = " +ctx.getFileReadSampling();
            else {
                int readevery = Integer.parseInt(parameters[0]);
                ctx.setFileReadSampling(readevery);
                c.retMessage = "Files will only record every " + readevery +"th reading";
            }
        } 
        else if(command.startsWith("rthelp")){   
            c = new Command("rthelp");
            c.retMessage =" Do the following in order::" 
                    + "0. python3 samImagentRealtime.py (with mysql connected)::"
                    + "1. synchronize(realtime1), stream(realtime1)::"
                    + "2. interceptlabel(realtime1, condition, port), ::"
                    + "2.5  clearstream() because streaming visualization is expensive. (it will still be updated)"
                    + "3. nback(n, duration, port) *however many, but dont overlap!::" 
                    +" 4  split(condition). getlabels() + keep(x,y,z)"
                    + "5. train(dbname) with techniques intersected::"
                    + "6. classifylast() with db selected";
        }
        
        else if (command.startsWith("getcommands") || command.startsWith("help")) {
            c = new Command("getcommands");
            c.action = command;
            c.data = new JSONObject();
            JSONArray commands = new JSONArray();
            c.data.put("commands", commands);
            
            //.. add each command 
            for (JSONObject com : miscParser.getCommands()){commands.put(com);}
            for (JSONObject com : transformationParser.getCommands()){commands.put(com);}
            for (JSONObject com : dataParser.getCommands()){commands.put(com);}
            for (JSONObject com : mlParser.getCommands()){commands.put(com);}
            return c.getJSONObject(ctx.getTutorial());
        }

        if (c == null) {
            return null;
        }
        return c.getJSONObject(ctx.getTutorial());
    }
    
    
    /**Return what is enumerated inside the parens, ie x, y and z from function(x,y,z)*
     * if no params return an empty array. */
    public static String [] getParameters(String input) throws Exception{
        String [] params;
        String betweenParen = getBetweenParen(input);
        if (betweenParen.equals("")) return new String[0];
        params = betweenParen.split(",");
        return params;
    }
    
    /** Input: aksdasl(xxxx,yy,xx)
     Return: xxx,yy,xx*
     * If there's nothign inbetween the parens return nothing*/ 
    private static String getBetweenParen(String input) throws Exception{
        String[] values = input.split("\\(");
        if (values.length <2) return "";
        String betweenParen = values[1];
        betweenParen = betweenParen.replace(" ", "");
        betweenParen  = betweenParen.replace(")", ""); //.. remove )
        return betweenParen;

    }
    
  
    /***Test what can be tested via simulation**/
    public static void main(String[] args) {
        
        //.. The necessary parts of a test: a context, a datalayer, and a datalayerDAO 
        ThisActionBeanContext ctx = new ThisActionBeanContext(true);
        InputParser ip = new InputParser(ctx);

        try{
         
            //.. . Fabricate fake data with 1 channel and 200 readings
            ChannelSet b = ChannelSet.generate(2000, 10);
            ChannelSet c = ChannelSet.generate(2000, 10);

            //.. And add some real data
            Experiment realE = BesteExperiment.getExperiment("input/bestemusic/bestemusic15.csv");
            ctx.addDataLayer("reale", new TriDAO(realE));
            ChannelSet cs = BesteExperiment.getChannelSet(13);
            ctx.addDataLayer("input/bestemusic/bestemusic15-csv", new BiDAO(cs));

            //.. connect experiment and 
            ctx.setCurrentName("reale");
            TriDAO tDAO = (TriDAO) ctx.getCurrentDataLayer();
            TechniqueSet ts = TechniqueSet.generate();
            
            //..  associate technique with experiment
            TechniqueDAO wc = new TechniqueDAO(ts.getClassifier());
            tDAO.addConnection(wc);
            tDAO.addConnection(new TechniqueDAO(ts.getFeatureSet()));
            tDAO.addConnection(new TechniqueDAO(ts.getAttributeSelection()));
            
            
            
            int TEST =8;
                
            String test = "realtimeclass";
            JSONObject response = new JSONObject();
            
            if (test.equals("multiclasstrain")) {
                response = ip.parseInput("train(-2)");
            }
            
            if (test.equals("realtimeclass")) {
                ctx.setCurrentName("input/bestemusic/bestemusic15-csv");
                response = ip.parseInput("manipulate(zscore)");
                response = ip.parseInput("split(condition");
                ctx.setCurrentName("input/bestemusic/bestemusic15-csvcondition");
                
                tDAO = (TriDAO) ctx.getCurrentDataLayer();
                tDAO.addConnection(wc);
                tDAO.addConnection(new TechniqueDAO(ts.getFeatureSet()));
                tDAO.addConnection(new TechniqueDAO(ts.getAttributeSelection()));
                response = ip.parseInput("train"); //.. After this, the associated weka classifier is trained

                //.. Having trained, now test
                response = ip.parseInput("synchronize(realtime1)");
                ctx.setCurrentName("realtime1");
                BiDAO bDAO = (BiDAO) ctx.getCurrentDataLayer();
                bDAO.addConnection(wc);
                
                response = ip.parseInput("classifylast");
            }
            
            if (test.equals("MARKERVIZ")) {
                response = ip.parseInput("synchronize(realtime1)");
                ctx.setCurrentName("realtime1");

                response = ip.parseInput("randomlylabel()");
                response = ip.parseInput("synchronize(realtime1)");
                response = ip.parseInput("stream()");
                System.out.println(response);

                Thread.sleep(2000);
                response = ip.parseInput("synchronize(realtime1)");
                response = ip.parseInput("stream()");
                System.out.println(response);
    
                Thread.sleep(2000);  

                response = ip.parseInput("synchronize(realtime1)");
                response = ip.parseInput("stream()");
                System.out.println(response);

                Thread.sleep(2000);
                response = ip.parseInput("synchronize(realtime1)");
                response = ip.parseInput("stream()");
                System.out.println(response);

                
            }
            if(test.equals("RPS")) {
                ctx.setCurrentName("reale");
                tDAO = (TriDAO) ctx.getCurrentDataLayer();
                JSONObject jo = tDAO.getJSON();
                System.out.println(jo);
            }
            
            if (test.equals("LOAD")) {
                String foldername = "GRProcessed";
                response = ip.parseInput("load(" + foldername);
            }

            if (test.equals("LOAD")) {
                String foldername = "GRProcessed";
                response = ip.parseInput("load("+foldername);
            }
            
            
            if (TEST == 7) {
                ChannelSet a1 = ChannelSet.generate(275000, 1000);
                ctx.addDataLayer("a1", new BiDAO(a1));
                response = ip.parseInput("delete");
                a1 =null;
                ChannelSet a2 = ChannelSet.generate(200000, 1000);
            }
            
            
            
            if (TEST ==0) 
                 response= ip.parseInput("removeallbut(a,b)");
            
            if (TEST ==1) {
                response = ip.parseInput("intercept(bestemusic07.csv, bajs, bajs)");
            }
            
            if (TEST ==2) {
                response = ip.parseInput("save(");
            }
            
            if (TEST ==3) {
                response = ip.parseInput("label(");
            }
           
            
            if (TEST ==6) {
                ctx.setCurrentName("realcs");
                response = ip.parseInput("glassroutes");
            }
            
           
            System.out.println(response.get("content"));
           // System.out.println(response.get("action"));
           // System.out.println(response.get("error"));

        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    

}
