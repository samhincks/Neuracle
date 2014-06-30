/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stripes.action;

import dao.techniques.TechniqueDAO;
import java.io.StringReader;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import org.json.JSONObject;

/**
 *
 * @author samhincks
 */
public class TechniqueActionBean extends BaseActionBean{
    
    
    private String techniqueStats;
    public String getTechniqueStats() { return techniqueStats;}
    public void setTechniqueStats(String technique) {this.techniqueStats = technique;}
    
    
    @DefaultHandler
    /**Return a Streaming resolution of saved performance*/
    public Resolution getJSON() {   
         if (this.getTechniqueStats()!=null){
            ctx.setCurrentTechnique(techniqueStats);
         }
         
         JSONObject jsonObj = new JSONObject();
         try{
            TechniqueDAO tDAO = ctx.getCurrentTechnique();
            jsonObj = tDAO.getJSON();
            return new StreamingResolution("text", new StringReader(jsonObj.toString()));
         }
         
         //.. in practice this just doesn't happen.
         catch(Exception e) {
            System.err.println(e.getMessage());
             return new ForwardResolution(INDEX);
         }

    }
}
