package stripes.action;

import dao.datalayers.UserDAO;
import java.io.StringReader;
import java.sql.SQLException;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;
import org.json.JSONException;
import org.json.JSONObject;
import static stripes.action.BaseActionBean.INDEX;

/**
 *
 * @author 
 */
public class UserActionBean extends BaseActionBean {
         @ValidateNestedProperties({  
            @Validate(field ="userName",required=true,on = "loginUser"),  
            @Validate(field ="password",required=true,on = "loginUser")
          })  
   
    public Resolution loginUser() throws JSONException {
        String str = new String();
        try {
            if(ctx.userDAO == null)
            ctx.userDAO = new UserDAO();
            super.setUserName((String)ctx.getRequest().getSession().getAttribute("userName"));
            super.setPassword((String)ctx.getRequest().getSession().getAttribute("password"));
            String userId = ctx.userDAO.login(getUserName(), getPassword());
            if (userId != null ) {
                ctx.userDAO.setUserId(userId);
                ctx.getRequest().getSession().setAttribute("userId", userId);
                return new ForwardResolution(INDEX);
            } else {
                str  = "Account number or password is wrong!<a href='#'>asdf</a>";
                return new StreamingResolution("text", new StringReader(str));
            }
        } catch(Exception e) {
            e.printStackTrace();
            return new StreamingResolution("text", new StringReader(e.getClass() + " : "+ e.getMessage()));
        }
    }

}
