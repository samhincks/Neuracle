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
    public String id ="";
    public String parameters ="";
    public String debug ="";
    public String retMessage =""; //.. populated when the command is executed
    public String action =""; //.. populated when the command is executed

    public JSONObject getJSONObject() throws Exception{
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("content", retMessage);
        jsonObj.put("action", action);
        
        return jsonObj;
    }
}
