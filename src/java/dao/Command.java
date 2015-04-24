  /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dao;

import org.json.JSONObject;  

/**    
 *  
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
 
    public Command(String id) {
        this.id = id;
    }  
      
    public JSONObject getJSONObject(boolean showTut) throws Exception{
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
        return jsonObj;
    }
}
