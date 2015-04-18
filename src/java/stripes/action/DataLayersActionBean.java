/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stripes.action;

import dao.datalayers.MySqlDAO;
import java.io.StringReader;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import org.json.JSONArray;
import org.json.JSONObject;
import timeseriestufts.kth.streams.DataLayer;
import timeseriestufts.kth.streams.bi.BidimensionalLayer;
import timeseriestufts.kth.streams.tri.Experiment;
import timeseriestufts.kth.streams.tri.TridimensionalLayer;

/**
 *
 * @author samhincks
 */
public class DataLayersActionBean extends BaseActionBean{ 
    
    @DefaultHandler
    public Resolution getDataLayerArray() throws Exception{
        try{
            JSONObject jsonObj = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            //.. Add each datalayer to the arry
            super.setUserName((String)ctx.getRequest().getSession().getAttribute("userName"));
            super.setPassword((String)ctx.getRequest().getSession().getAttribute("password"));
//            ctx.dataLayersDAO.addDataByDB(ctx);
            for (DataLayer dl :ctx.dataLayersDAO.getDataLayers()){
               JSONObject obj = new JSONObject();   
               obj.put("id", dl.getId());
               obj.put("parent", dl.getParent()); //.. get the name of the layer it was derived from
               obj.put("numchannels", dl.getChannelCount());
               obj.put("numpoints", dl.getCount());
               if (ctx.getPerformances().predictionSets.containsKey(dl.getId()))
                   obj.put("performance", "true");
               else obj.put("performance", "false");
               
               //.. depending on type, place the layers type
               if (dl instanceof BidimensionalLayer)
                  obj.put("type", "2D");

               else if (dl instanceof TridimensionalLayer){
                   Experiment exp = (Experiment)dl;
                   obj.put("type", "3D");
                   obj.put("numinstances", exp.getLayerSize());
                   obj.put("numlabels", exp.classification.values.size());
               }
              jsonArray.put(obj);
            }
            jsonObj.put("datalayers", jsonArray);
           return new StreamingResolution("text", new StringReader(jsonObj.toString()));
        }
        catch(Exception e) {
            e.printStackTrace();
            System.err.println("Returning since  " + e.getMessage());
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("error", e.getMessage());
            return new StreamingResolution("text", new StringReader(jsonObj.toString()));
        }   

    }
    
}
