 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import dao.techniques.TechniquesDAO;
import dao.techniques.TechniqueDAO;
import dao.datalayers.TriDAO; 
import dao.datalayers.BiDAO;
import dao.datalayers.DataLayerDAO;
import dao.datalayers.DataLayersDAO;
import dao.datalayers.MySqlDAO;
import dao.datalayers.QuadDAO;    
import dao.datalayers.UserDAO;
import filereader.Label;
import filereader.Labels;
import filereader.Markers;
import java.io.StringReader; 
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;  
import java.util.UUID;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.StreamingResolution;
import org.json.JSONException;
import org.json.JSONObject;
import realtimereceiver.Client;
import stripes.ext.ThisActionBeanContext;
import timeseriestufts.evaluatable.*;
import timeseriestufts.evaluatable.performances.Performances;
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
    
    
    private String getHelp() {
        return "Figure it out for yourself!";
    }
    
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
            } else {
                if (!ctx.test)System.err.println("No loaded datalayers");
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
            //.. put experiment in tridao
            ChannelSet b = ChannelSet.generate(1, 200);
            ChannelSet c = ChannelSet.generate(1, 200);
            
            Markers m = Markers.generate(10, 20);
            b.addMarkers(m);
            b.id = "b";
            c.id = "c";
            c.addMarkers(m);
            BiDAO bDAO = new BiDAO(b);
            BiDAO cDAO = new BiDAO(c);

            //.. set the ctx's current dao. 
            ctx.addDataLayer("b", bDAO);
            ctx.addDataLayer("c", cDAO);

            
            JSONObject response = new JSONObject();
            int TEST =5;
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
                TechniqueSet ts = TechniqueSet.generate();
                ctx.setCurrentName("b");
                response = ip.parseInput("split(name)",ctx);
                ctx.setCurrentName("bname");
                TriDAO tDAO = (TriDAO) ctx.getCurrentDataLayer();
                
                TechniqueDAO wc = new TechniqueDAO(ts.getClassifier());
                tDAO.addConnection(wc);
                tDAO.addConnection(new TechniqueDAO(ts.getFeatureSet()));
                tDAO.addConnection(new TechniqueDAO(ts.getAttributeSelection()));
                
                response = ip.parseInput("train", ctx);
                System.out.println(response);
                
                //.. Having trained, now test
                ctx.setCurrentName("c");
                bDAO = (BiDAO) ctx.getCurrentDataLayer();
                bDAO.addConnection(wc);
                 
                response = ip.parseInput("classify", ctx);
                System.out.println(response);
            }
           
            
            System.out.println(response.get("content"));
            System.out.println(response.get("action"));
            System.out.println(response.get("error"));

        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    

}
