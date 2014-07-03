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
public class InputParser {
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
                System.out.println("Just got currentdatalayer" + currentDataLayer.id);
            } else {
                System.err.println("No loaded datalayers");
            }

            //.. retreive the techinique that's currently selected
            TechniqueDAO techDAO = ctx.getCurrentTechnique();
            if (techDAO != null) {
                currentTechnique = techDAO.technique;
            }
            
            //String command = input.split("(")[0];
            String [] parameters = this.getParameters(input);
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
            e.printStackTrace();
            jsonObj.put("error", e.getMessage());
            return jsonObj;
        }
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
        ThisActionBeanContext ctx = new ThisActionBeanContext();

        //.. put experiment in tridao
        Experiment e = Experiment.generate(6, 1, 20);
        TriDAO tDAO = new TriDAO(e);
        
        //.. set the ctx's current dao. 
//        ctx.addDataLayer("e", tDAO);
        try{
            JSONObject response = new JSONObject();
            int TEST =3;
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
            
            
            System.out.println(response.get("content"));
            System.out.println(response.get("action"));
            System.out.println(response.get("error"));

        }
        catch (Exception ex) {
            
        }

    }
    
    
    public boolean saveDataLayer() throws Exception{
        if (ctx.dataLayersDAO.getDataLayers().size() > 0) {
            for (DataLayer dl :ctx.dataLayersDAO.getDataLayers()){
                   System.out.println("thats  " + dl.id);
                   JSONObject obj = new JSONObject();
                   String filename =  dl.getId();
                   DataLayerDAO dlGiver = ctx.dataLayersDAO.get(filename); 
            
                    //.. select the file
                    filename = filename.split("-")[0]+".csv";
                    ChannelSet channelSet = (ChannelSet) dlGiver.dataLayer;
                    ArrayList<Channel> rawValues =  channelSet.streams;
                    
                    //.. connect to SQL
                    MySqlDAO mydao=new MySqlDAO();
                    String uuIdStr= UUID.randomUUID().toString();
                    mydao.connSQL();
                    
                    //.. get the id of the user, a number
                    String userId = (String) ctx.getRequest().getSession().getAttribute("userId");
                   
                    //.. Retrieve the datalayer that is associated with this user - but this assumes that there is only ONE
                    ResultSet datalayerData = mydao.selectSQL("select * from datalayer where user_id='"+userId+"' and file_name like '" + filename.split(".cvs")[0]+"%'");
                    while(datalayerData.next()) {
                        mydao.deleteSQL("delete from datalayer where id ='"+datalayerData.getString(1)+"'");
                        mydao.deleteSQL("delete from datalayer where parent_id ='"+datalayerData.getString(1)+"'");
                        mydao.deleteSQL("delete from datalayer_id where parent_id ='"+datalayerData.getString(1)+"'");
                    }
                    datalayerData.close();
                    boolean num = mydao.insertSQL("insert into datalayer(id,user_id,parent_id,data,file_name) VALUES ('"+uuIdStr+"','"+userId+"','"+null+"','"+null+"', '"+filename+"')");
                    mydao.deconnSQL();

                    for(Channel rowValue : rawValues){
                        StringBuffer flotArrays = new StringBuffer();
                        for (int l = 0; l < rowValue.numPoints; l++){
                         flotArrays.append(","+rowValue.getPointOrNull(l));
                        }
                        mydao.connSQL();
                        Boolean result=mydao.insertSQL("insert into datalayer(id,user_id,parent_id,data,file_name) VALUES ('"+UUID.randomUUID().toString()+"','"+null+"','"+uuIdStr+"','"+flotArrays.toString().substring(1)+"', '"+rowValue.getId()+"')");
                        mydao.deconnSQL();
                  } 

                    //.. Having built a channel structure and label structure, label the channel
                    //... structure according to the label structure
                    for (Markers markers : channelSet.markers) {
                        Labels labels = markers.saveLabels;
                        StringBuffer str = new StringBuffer();
                        for (Label label : labels.channelLabels) {
                            str.append(","+label.value);
                        }
                        mydao.connSQL();
                        mydao.insertSQL("insert into label(id,datalayer_id,labelName,channelLabels) VALUES ('"+ UUID.randomUUID().toString()+"','"+uuIdStr+"','"+labels.labelName+"','"+str.toString().substring(1)+"')");
                        mydao.deconnSQL();
                    }
            }
        } else {
            return false;
        }
        return true;
    }

}
