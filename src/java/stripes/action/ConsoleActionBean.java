/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stripes.action;

import dao.datalayers.DataLayerDAO;
import dao.InputParser;
import dao.datalayers.DataLayersDAO;
import dao.techniques.TechniqueDAO;
import dao.techniques.TechniquesDAO;
import java.io.StringReader;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import org.json.JSONException;
import org.json.JSONObject;

/**For handling input from the console
 * @author samhincks
 */
public class ConsoleActionBean extends DataLayerActionBean {
    
    private String consoleInput;
    public String getConsoleInput() { return consoleInput;}
    public void setConsoleInput(String consoleInput) {this.consoleInput = consoleInput;}
    
    private String technique;
    public String getTechnique() { return technique;}
    public void setTechnique(String technique) {this.technique = technique;}
        
    
    /**Parse the input from the console.
     */
    @DefaultHandler
    public Resolution parseInput() throws JSONException{
        InputParser inputParser = ctx.inputParser;
        //.. Parse the actual input
        JSONObject jsonObj = new JSONObject();
        try {
            //--- SET any additional parameters not parsed in text for certain commands
            if (this.getTechnique()!=null){
                ctx.setCurrentTechnique(technique);
            }
           if (consoleInput == null || consoleInput.equals("")) return new StreamingResolution("text", "Enter tab to see all available commands");

            //.. if this is an evaluation command, set the technique parameters
            if (connections != null && (consoleInput.startsWith("evaluate") 
                    || consoleInput.startsWith("train")
                    || consoleInput.startsWith("classify"))){
                setTechniqueParams();
            }
            System.out.println("parsing " + consoleInput);
                jsonObj = inputParser.parseInput(consoleInput);
            return new StreamingResolution("text", new StringReader(jsonObj.toString()));
        }
        catch(Exception e) {
            System.out.println("Caught an Exception");
            e.printStackTrace();
            jsonObj.put("error", e.getClass() + " : "+ e.getMessage());
            return new StreamingResolution("text", new StringReader(jsonObj.toString()));
        }
    }

    /**Parse the array of Strings that hold what techniques are connected to what datalayer*/
    private void setTechniqueParams() throws Exception{
        DataLayersDAO dls = ctx.getDataLayers();
        dls.resetTConnections();
        TechniquesDAO techs = ctx.getTechniques();
        String[]cs = connections.split(",");
       
        //.. For each connection, find the corresponding DataLayerDAO and TechniqueDAO. Link them
        for (String s : cs) {
            String [] pair = s.split(":");
            String technique = pair[0]; //.. the technique is the source
            String datalayer = pair[1]; //.. the datalayer is the target
            
            //.. retrieve the pointed-to DLDAO, and add  the technique
            DataLayerDAO dlDAO= dls.get(datalayer);
            TechniqueDAO tDAO = techs.get(technique);
            dlDAO.addConnection(tDAO);
        }
    }
    
    /**IMPLICIT variables parsed with an input. We set these prior to passing the input to 
     the aciton bean**/
    
    private String connections;
    public void setConnections(String connections) {this.connections = connections;}
     
}
