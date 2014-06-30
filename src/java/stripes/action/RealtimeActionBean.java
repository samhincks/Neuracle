

package stripes.action;

/** This is use to deal with the button click in the realtime.jsp
 *  Right now it is only return some fixed string content. You can add more code
 * here to show different things, such as database data from Realtime fNIR data.
 * 
 *
 * @author Enhao
 */
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import java.io.StringReader;
import java.util.List;  
    



public class RealtimeActionBean implements ActionBean {
    private ActionBeanContext context;
   

    
    public ActionBeanContext getContext() { return context; }
    
    public void setContext(ActionBeanContext context) { this.context = context; }
    
    @DefaultHandler public Resolution showing() {
       
        String result = "[testfile; 2.0, 3.0, 2.3, 4.1; highworkload, lowarousal, angry]<br>"+
               "[testfile; 2.0, 3.0, 2.3, 4.1; highworkload, lowarousal, angry]<br>"+
               "[testfile; 2.0, 3.0, 2.3, 4.1; highworkload, lowarousal, angry]<br>"+
               "[testfile; 2.0, 3.0, 2.3, 4.1; highworkload, lowarousal, angry]<br>"+
               "[testfile; 2.0, 3.0, 2.3, 4.1; highworkload, lowarousal, angry]<br>"+
               "[testfile; 2.0, 3.0, 2.3, 4.1; highworkload, lowarousal, angry]<br>"+
               "[testfile; 2.0, 3.0, 2.3, 4.1; highworkload, lowarousal, angry]<br>"+
               "[testfile; 2.0, 3.0, 2.3, 4.1; highworkload, lowarousal, angry]<br>"+
               "[testfile; 2.0, 3.0, 2.3, 4.1; highworkload, lowarousal, angry]<br>"+
               "[testfile; 2.0, 3.0, 2.3, 4.1; highworkload, lowarousal, angry]<br>"+
               "[testfile; 2.0, 3.0, 2.3, 4.1; highworkload, lowarousal, angry]<br>"+
               "[testfile; 2.0, 3.0, 2.3, 4.1; highworkload, lowarousal, angry]<br>"+
               "[testfile; 2.0, 3.0, 2.3, 4.1; highworkload, lowarousal, angry]<br>"+
               "[testfile; 2.0, 3.0, 2.3, 4.1; highworkload, lowarousal, angry]<br>";
        
        return new StreamingResolution("text", new StringReader(result));
        
    }
 
}


   