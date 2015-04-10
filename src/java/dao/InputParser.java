 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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

/**
 *
 * @author samhincks
 */
public  class InputParser {
    public ArrayList<String> oldInput = new ArrayList();
    private DataLayer currentDataLayer;
    private Technique currentTechnique;
    private ThisActionBeanContext ctx;
    public double FRAMESIZE =1;
    
    private MiscellaneousParser miscParser;
    private ExternalDataParser dataParser;
    private TransformationParser transformationParser;
    private DataManipulationParser mlParser;
   
    public InputParser() {
        miscParser = new MiscellaneousParser();
        dataParser = new ExternalDataParser();
        transformationParser = new TransformationParser();
        mlParser = new DataManipulationParser();
    } 
    
    public JSONObject parseInput(String input, ThisActionBeanContext ctx) throws JSONException, Exception {
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
            
            if (jsonObj ==null)
                jsonObj = miscParser.execute(input,parameters, ctx, currentDataLayer,techDAO);
            
            if (jsonObj == null)
                jsonObj = dataParser.execute(input,parameters,ctx,currentDataLayer,techDAO);
                
            if (jsonObj ==null)
                jsonObj = transformationParser.execute(input, parameters,ctx,currentDataLayer,techDAO); 
            
            if (jsonObj ==null)
                jsonObj = mlParser.execute(input, parameters,ctx,currentDataLayer,techDAO); 
            
             
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
 
   public JSONObject rapidExecute(String command, String[] parameters, ThisActionBeanContext ctx,
            DataLayer currentDataLayer, TechniqueDAO techDAO) throws Exception {
        this.ctx = ctx;
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
                    + "1. streamsynch(dbname)::"
                    + "2. interceptlabel(dbname, condition, port)::"
                    + "3. nback(n, duration, port) *however many, but dont overlap!::" 
                    +" 4 split(condition). getlabels() + keep(x,y,z)"
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
            return c.getJSONObject();

            
        }
        

        if (c == null) {
            return null;
        }
        return c.getJSONObject();
    }
    
    
    /**Return what is enumerated inside the parens, ie x, y and z from function(x,y,z)*
     * if no params return an empty array. */
    public String [] getParameters(String input) throws Exception{
        String [] params;
        String betweenParen = getBetweenParen(input);
        if (betweenParen.equals("")) return new String[0];
        params = betweenParen.split(",");
        return params;
    }
    
    /** Input: aksdasl(xxxx,yy,xx)
     Return: xxx,yy,xx*
     * If there's nothign inbetween the parens return nothing*/ 
    private String getBetweenParen(String input) throws Exception{
        String[] values = input.split("\\(");
        if (values.length <2) return "";
        String betweenParen = values[1];
        betweenParen = betweenParen.replace(" ", "");
        betweenParen  = betweenParen.replace(")", ""); //.. remove )
        return betweenParen;

    }
    
  
    /***Eventually I should write test classes but it is looking difficult to simulate the session
     context without stripes dispatchment**/
    public static void main(String[] args) {
        InputParser ip = new InputParser();
        
        //.. The necessary parts of a test: a context, a datalayer, and a datalayerDAO 
        ThisActionBeanContext ctx = new ThisActionBeanContext(true);

        try{
            //.. 1. Fabricate fake data with 1 channel and 200 readings
            ChannelSet b = ChannelSet.generate(2000, 10);
            ChannelSet c = ChannelSet.generate(2000, 10);
            
            //.. 1.1 And add some real data
            Experiment realE = BesteExperiment.getExperiment("input/bestemusic/bestemusic15.csv");
            ctx.addDataLayer("reale", new TriDAO(realE));
            ChannelSet cs = BesteExperiment.getChannelSet(13);
            ctx.addDataLayer("realcs", new BiDAO(cs));
            
            //.. 2. Associate correct number of markers with that data
            Markers m = Markers.generate(1, 10); // 10 * 20 = 200
            b.addMarkers(m);
            b.id = "b";
            c.id = "c";
            c.addMarkers(m);
            BiDAO bDAO = new BiDAO(b);
            BiDAO cDAO = new BiDAO(c);

            //.. 3. Add these to the session context
            ctx.addDataLayer("b", bDAO);
            ctx.addDataLayer("c", cDAO);

            //.. 4. Initialize a technique, select the channelset b, and make it an experimetn
            TechniqueSet ts = TechniqueSet.generate();
            ctx.setCurrentName("b");
            JSONObject response = ip.parseInput("split(condition)", ctx);
            ctx.setCurrentName("bcondition");
            TriDAO tDAO = (TriDAO) ctx.getCurrentDataLayer();
            
            //.. 5. associate technique with experiment
            TechniqueDAO wc = new TechniqueDAO(ts.getClassifier());
            tDAO.addConnection(wc);
            tDAO.addConnection(new TechniqueDAO(ts.getFeatureSet()));
            tDAO.addConnection(new TechniqueDAO(ts.getAttributeSelection()));
            
            int TEST =8;
                
            
            String test = "LOAD";
            if (test.equals("LOAD")) {
                String foldername = "GRProcessed";
                response = ip.parseInput("load("+foldername, ctx);
            }
            
            
            if (TEST == 7) {
                ChannelSet a1 = ChannelSet.generate(275000, 1000);
                ctx.addDataLayer("a1", new BiDAO(a1));
                response = ip.parseInput("delete", ctx);
                a1 =null;
                ChannelSet a2 = ChannelSet.generate(200000, 1000);
            }
            
            
            
            if (TEST ==0) 
                 response= ip.parseInput("removeallbut(a,b)", ctx);
            
            if (TEST ==1) {
                response = ip.parseInput("intercept(bestemusic07.csv, bajs, bajs)", ctx);
            }
            
            if (TEST ==2) {
                response = ip.parseInput("save(", ctx);
            }
            
            if (TEST ==3) {
                response = ip.parseInput("label(", ctx);
            }
            if (TEST ==4) {
                response = ip.parseInput("train", ctx); //.. After this, the associated weka classifier is trained
                
                //.. Having trained, now test
                ctx.setCurrentName("b");
                bDAO = (BiDAO) ctx.getCurrentDataLayer();
                bDAO.addConnection(wc);
                 
                response = ip.parseInput("classify", ctx);
                Predictions p = ctx.getPerformances().predictionSets.get("b");
                System.out.println(p.getPctCorrect());
                JSONObject jo = bDAO.getPerformanceJSON(ctx.getPerformances());
                System.out.println(jo.get("predictions"));
                System.out.println(jo.get("classes"));
            }
            if (TEST ==5) { //.. Test multilayer appending
                //.. First select all layers  
                ctx.setCurrentName("b");  
                bDAO = (BiDAO) ctx.getCurrentDataLayer();  
              //  bDAO.dataLayer.printStream();
                
                ctx.setCurrentName("c");
                bDAO = (BiDAO) ctx.getCurrentDataLayer();
               // bDAO.dataLayer.printStream();
                
                ctx.setCurrentName("b:c");
                response = ip.parseInput("append", ctx);
                System.out.println(response.get("content"));
                ctx.setCurrentName("mergedb-c");
                bDAO = (BiDAO) ctx.getCurrentDataLayer();
                bDAO.dataLayer.printStream();
            }
            
            if (TEST ==6) {
                ctx.setCurrentName("realcs");
                response = ip.parseInput("glassroutes", ctx);
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
