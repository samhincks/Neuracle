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

    public Command(String id) {
        this.id = id;
    }
    
    public JSONObject getJSONObject() throws Exception{
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("content", retMessage);
        if(action!= null) {
            JSONObject jsObj = new JSONObject();
            jsObj.put("id", action);
            jsonObj.put("action", jsObj);
        }
        if(data!= null) jsonObj.put("data",data);
        return jsonObj;
    }
}
