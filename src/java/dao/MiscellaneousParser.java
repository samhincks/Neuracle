/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dao;

import dao.datalayers.DataLayersDAO;
import java.util.ArrayList;
import java.util.Hashtable;
import org.json.JSONObject;
import stripes.ext.ThisActionBeanContext;
import timeseriestufts.kth.streams.DataLayer;

/**
 *
 * @author samhincks
 */
public class MiscellaneousParser {
    public Hashtable<String,Command> commands;
    public ThisActionBeanContext ctx;
    public DataLayer currentDataLayer; 
    public MiscellaneousParser() {
        commands = new Hashtable();
        /**--  Every command this Parser handles should be added to commands
         *     with a corresponding function for execution in the execute function--**/
        
        //-- getdatalayers
    }
    
    public JSONObject execute(String command, String [] parameters, ThisActionBeanContext ctx, DataLayer currentDataLayer ) throws Exception{
        this.ctx = ctx;
        this.currentDataLayer = currentDataLayer;
        Command c = null; 
        if (command.startsWith("ls") || command.startsWith("getdatalayers")){
            c = commands.get("ls");
            c.retMessage = ls(parameters);
        }
        if (c ==null) return null;
        return c.getJSONObject();
    }
    
    private String ls(String [] parameters) throws Exception {
        String retString = "";
        DataLayersDAO dlDAOs = ctx.getDataLayers();
        for (int i = 0; i < dlDAOs.getDataLayers().size(); i++) {
            DataLayer dl = dlDAOs.getDataLayers().get(i);
            retString += dl.getId() + " with " + dl.getCount() + " pts " + " and mean of " + dl.getMean();
            if (i != dlDAOs.getDataLayers().size() - 1) {
                retString += "::";
            }
        }

        return retString;
    }
    
}
