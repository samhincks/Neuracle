<%-- 
    Document   : realtime
    Created on : Mar 31, 2014, 7:32:20 PM
    Author     : Enhao
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
  <head>
      
      
      <title>Realtime showing</title>
      <script type="text/javascript"
              src="js/prototype.js"></script>
      <script type="text/javascript" xml:space="preserve">
         
          function invoke(form, event, container) {
              if (!form.onsubmit) { form.onsubmit = function() { return false } };
              var params = Form.serialize(form, {submit:event});
              new Ajax.Updater(container, form.action, {method:'post', parameters:params});
              
          }
      </script>
    
   
 
  </head>
  <body>
    <h1>Showing the data(Demo)</h1>

    <p>
        This is used for showing the real time data          
    </p>
        
        <stripes:form action ="/Realtime.action">   
        <table>         
            <tr>
                <td colspan="2">
                    <stripes:submit name="showing" value="Showing" style="background-color:#b0c4de;"
                                    onclick="invoke(this.form, this.name, 'result');"/>                    
                </td>
            </tr>
            <tr>
                <td><b>Real time fNIR DATA:<br></b></td>
            </tr>
            <tr>
                 <td id="result"></td>
            </tr>
        </table>
    </stripes:form>
        
    
  </body>
</html>
