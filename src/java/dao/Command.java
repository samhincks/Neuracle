package dao;

import org.json.JSONObject;  

/**        
 *  An object which mediates command information between server and client. 
 * @author samhincks  
 */                    
public class Command {                              
    public String id;                           
    public String parameters ="";        
    public String debug ="";
    public String retMessage =""; //.. populated when the command is executed
    public String documentation =""; //.. populated when the command is executed
    public String action = null;
    public JSONObject data =null;  
    public String tutorial = null;    
    public String selfcalibrate = null;  
       
    public Command(String id) {        
        this.id = id;             
    }              
      
    /** Returns the JSON representation of the command object
     * @param showTut , set to true if you want to add the tutorial data too
     * @return
     * @throws Exception 
     */
    public JSONObject getJSONObject(boolean showTut, boolean showSelf) throws Exception{
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("content", retMessage);
        jsonObj.put("id", id);
        jsonObj.put("parameters", parameters);
        jsonObj.put("documentation", documentation);
        
        if(action!= null) {
            JSONObject jsObj = new JSONObject();
            jsObj.put("id", action); 
            jsObj.put("data", data);
            jsonObj.put("action", jsObj);
        }  
        if (tutorial != null && showTut)
            jsonObj.put("tutorial", tutorial);
        
        if (selfcalibrate != null && showSelf){
            jsonObj.put("selfcalibrate", selfcalibrate);
        }
        return jsonObj;
    }
}
