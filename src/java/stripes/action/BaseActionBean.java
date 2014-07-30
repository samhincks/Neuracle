/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stripes.action;

import net.sourceforge.stripes.action.*;
import stripes.ext.ThisActionBeanContext;

/**
 *
 * @author Samuel Hincks
 */ 
public class BaseActionBean implements ActionBean{    
    protected static final String INDEX = "index.jsp";    
    private String  userName;
    private String password;
    
    protected ThisActionBeanContext ctx; //. store stuff through a session
    @Override
    public void setContext(ActionBeanContext _ctx) { ctx = (ThisActionBeanContext)_ctx;}
    @Override
    public ThisActionBeanContext getContext() { return ctx;}
    
   
    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
