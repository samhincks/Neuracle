<%-- 
    Document   : index
    Created on : Aug 2, 2012, 10:33:23 AM
    Author     : Sam Hincks

    This is the main page of the file. 
--%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
 
    <!-- - STRIPES, the interfacing language to Java -->
<%@ include file="/jsp/lib/taglibs.jsp"%>
<%@ taglib prefix="s"  uri="http://stripes.sourceforge.net/stripes.tld" %>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="content-type" content="text/html; charset=UTF8">
        <title>Time Mine</title>
        <!--<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>  If we dont do remote, pep doesnt work-->

       <link type="text/css" rel="stylesheet" href="css/jquery-ui.css" /> 
        <link type="text/css" rel="stylesheet" href="css/style.css" />
        <link type="text/css" rel="stylesheet" href="css/d3Style.css" />
       <link type="text/css" rel="stylesheet" href="css/consoleStyle.css" /> 
       <link type="text/css" rel="stylesheet" href="css/dlStyle.css" />

    </head>
    
    <body class ="unselectable" id ="doc">   
               
        
        <div id ="timemine"> 
             <!--Default location for selectable data layers-->
             <div id ="topLeft" class ="component">
                <!--Variables we want to set in the DataLayer Action Bean -->
                <s:form beanclass ="stripes.action.DataLayerActionBean" id = "content" class ="unselectable"> 
                   <s:text name = "giver" id = "giver" style ="visibility:hidden"/> 
                   <s:text name = "receiver" id = "receiver" style ="visibility:hidden"/>     
                   <s:text name = "stats" id = "stats" style ="visibility:hidden"/> 
                   <s:text name = "frequency" id = "frequency" style ="visibility:hidden"/> 
                   <s:text name = "correlation" id = "correlation" style ="visibility:hidden"/> 
                   <s:text name = "prediction" id = "prediction" style ="visibility:hidden"/> 

                </s:form>
                
               <s:form beanclass ="stripes.action.TechniqueActionBean" id = "techniques" class ="unselectable"> 
                   <s:text name = "techniqueStats" id = "techniqueStats" style ="visibility:hidden"/> 
               </s:form>
                               
             </div>

            <!--Where we place our Chart-->
            <div id ="topRight" class ="component trunzoomed">         
            </div>
            
            <!--Where we place our the description of selection-->
            <div id ="farBottomRight" class ="component">         

            </div>
            
            <!--For communicating File variables to Stripes -->             
            <div id ="bottomLeft" class ="component">
               <s:form beanclass ="stripes.action.DataLayerActionBean" id = "submitform" class ="unselectable"> 
                   <div id ="fileUpload">
                        <s:file name="newAttachment" style="background-color:#b0c4de;"/> 
                        </br>
                        <s:submit name = "registerFile" value = "submitFile" title = "Enter a file, then hit submit" /> 
                        </br>
                        </br>
                        </br>
                        <s:errors/>
                    </div>
                </s:form> 
                
                <!--Used when I was experimenting with with passing data as a string. finally deemed a bad idea-->
              <!-- <input type="file" id ="asynchFile" name="asynchFile" enctype="multipart/form-data" /> -->
                
                
            </div>
           
           
            <!--Bottom right, where we put the console  -->
            <div id ="bottomRight" class ="component">
                <!--The console --> 
                <div id = "console"> 
                    <div id = "pastmessages"> </div>     
                    <div class = "usermessage">
                          <span class = "prompt"> > </span> 
                          <span class = "cursor"> </span> 
                          <input id = "userinput" ></input> 
                  </div> 
                   <!-- <A HREF="realtime.jsp"><b> Realtime Data</b> -->
                </div>  
                 <s:form beanclass ="stripes.action.ConsoleActionBean" id = "consoleForm" class ="unselectable"> 
                       <!--A message to the console--> 
                       <s:text name = "consoleInput" id = "consoleInput" style ="visibility:hidden"/> 
                       <s:text name = "connections" id = "connections" style = "visibility:hidden"/>
                       <s:text name = "technique" id = "technique" style ="visibility:hidden"/> 
                 </s:form>
                        
            </div>
            
        
        <!--External JSLibraries-->
        <script type="text/javascript" src="js/lib/jquery2.min.js"></script>
        <script type="text/javascript" src="js/lib/jquery-ui-1.11.min.js"></script> 
        <script type="text/javascript" src="js/lib/jquery.jsPlumb-1.3.3-all.js"></script> 
         <script type="text/javascript" src="js/lib/jquery.pep.js"></script> 
         <script type="text/javascript" src="js/lib/d3.min.js"></script>  
         

        <!--VISUALIZATIONS-->
        <script type="text/javascript" src="js/visualization/d3tip.js"></script> 
        <script type="text/javascript" src="js/visualization/LineChart.js"></script> 
        <script type="text/javascript" src="js/visualization/BarChart.js"></script> 
        <script type="text/javascript" src="js/visualization/FreqBarChart.js"></script> 
        <script type="text/javascript" src="js/visualization/movingLinegraph.js"></script> 
        <script type="text/javascript" src="js/visualization/streamchart.js"></script> 
        <script type="text/javascript" src="js/visualization/CorrelationMatrix.js"></script> 
        <script type="text/javascript" src="js/visualization/PredictionChart.js"></script> 


        <!--AREAS-->
        <script type="text/javascript" src="js/area/chartarea.js"></script>
        <script type="text/javascript" src="js/area/datalayerarea.js"></script> 
        <script type="text/javascript" src="js/area/objects/plumb.js"></script> 
        <script type="text/javascript" src="js/area/objects/plumbtechniques.js"></script> 
        <script type="text/javascript" src="js/area/objects/datalayers.js"></script> 
        <script type="text/javascript" src="js/area/objects/techniques.js"></script> 
        <script type="text/javascript" src="js/area/consolearea.js"></script> 
        <script type="text/javascript" src="js/area/objects/labeler.js"></script> 
        
        <!--INTERFACE -->
        <script type="text/javascript" src="js/javainterface.js"></script> 
        <script type="text/javascript" src="js/events.js"></script> 
        

         
    </body>
    
</html> 
