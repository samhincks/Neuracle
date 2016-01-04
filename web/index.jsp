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
        <title>Neuracle</title>
        <link type="text/css" rel="stylesheet" href="css/jquery-ui.css" /> 
        <link type="text/css" rel="stylesheet" href="css/style.css" />
        <link type="text/css" rel="stylesheet" href="css/d3Style.css" />
        <link type="text/css" rel="stylesheet" href="css/consoleStyle.css" /> 
        <link type="text/css" rel="stylesheet" href="css/dlStyle.css" />
        <link type="text/css" rel="stylesheet" href="css/nback.css" />
    </head>
    
    <body id ="doc">   
        <div id ="timemine"> 
             <!--Default location for selectable data layers-->
             <div id ="topLeft" class ="component">
                <!--Variables we want to set in the DataLayer Action Bean -->
                <s:form beanclass ="stripes.action.DataLayerActionBean" id = "content" class ="unselectable"> 
                   <s:text name = "giver" id = "giver" style ="visibility:hidden"/> 
                   <s:text name = "receiver" id = "receiver" style ="visibility:hidden"/>     
                   <s:text name = "stats" id = "stats" style ="visibility:hidden"/> 
                   <s:text name = "debug" id = "debug" style ="visibility:hidden"/> 
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
                
                <div id="nhead">
                    <h1 id="nvalue">2</h1>
                    <div id="left">left=visual-match</div>
                    <div id="right">right=audio-match</div>
                </div>
               
                
                <table  class = "ntable" id ="nback">
                    <tr>
                        <td class = "ncol"><div id="uno" class="off"></div></td>
                        <td class = "ncol"><div id="dos" class="off"></div></td>
                        <td class = "ncol"><div id="tres" class="off"></div></td>
                    </tr>
                    <tr>
                        <td class = "ncol"><div id="cuatro" class="off"></div></td>
                        <td class = "ncol"><div id="cinco" class="off"><img src="images/plus.png" alt=""></div></td>
                        <td class = "ncol"><div id="seis" class="off"></div></td>
                    </tr>
                    <tr>
                        <td class = "ncol"><div id="siete" class="off"></div></td>
                        <td class = "ncol"><div id="ocho" class="off"></div></td>
                        <td class = "ncol"><div id="nueve" class="off"></div></td>
                    </tr>
                </table> 
                <div class="btn"><button id="begin" class="pure-button">Begin</button></div>
            </div>
            
            <!--Where we place our the description of selection-->
            <div id ="farBottomRight" class ="component ">      
                    
                <table id ="classifier" BORDER="5"    WIDTH="50%"   CELLPADDING="4" CELLSPACING="3">
                    <tr>
                        <td>Window</td>
                        <td>Threshold</td>
                        <td>Channel</td>
                        <td>Slope</td>
                        <td>Stdev</td>
                    </tr>
                    <tr>
                        <td>
                            <input type="number" id = "readingsBack" value="20"> 
                        </td>
                        <td>
                            <input type="number" id = "threshold" value="1"> 
                        </td>

                        <td> <select id ="classifierchannel" name="channel">
                            <option value="0">0</option>
                            <option value="1">1</option>
                            <option value="2">2</option>
                            <option value="3">3</option>
                            <option value="4">4</option>
                            <option value="5">5</option>
                            <option value="6">6</option>
                            <option value="7">7</option> 
                            <option value="8">8</option>
                            <option value="9">9</option>
                            <option value="10">10</option>
                            <option value="11">11</option>
                            <option value="12">12</option>
                            <option value="13">13</option>
                            <option value="14">14</option>
                            <option value="15">15</option>
                            </select>
                        </td>
                        <td> 
                            <div id ="currSlope">000</div>
                        </td>

                        <td> 
                            <div id ="currStdev">000</div>
                        </td>
                    </tr>
                    
                    <tr>
                        <td> </td> <td> </td>
                        <td> <select id ="classifierchannel2" name="channel">
                                <option value="0">0</option>
                                <option selected ="selected" value="1">1</option>
                                <option value="2">2</option>
                                <option value="3">3</option>
                                <option value="4">4</option>
                                <option value="5">5</option>
                                <option value="6">6</option>
                                <option value="7">7</option> 
                                <option value="8">8</option>
                                <option value="9">9</option>
                                <option value="10">10</option>
                                <option value="11">11</option>
                                <option value="12">12</option>
                                <option value="13">13</option>
                                <option value="14">14</option>
                                <option value="15">15</option>
                            </select>
                        </td>
                        <td> 
                            <div id ="currSlope2">000</div>
                        </td>

                        <td> 
                            <div id ="currStdev2">000</div>
                        </td>
                    </tr>
                    
                    <tr>
                        <td> </td>
                        <td> </td>
                        <td> </td>
                        
                        <td> 
                            <div id ="currSlope3">000</div>
                        </td>

                        <td> 
                            <div id ="currStdev3">000</div>
                        </td>
                    </tr>
                    
                </table> 

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
            </div>
           
           
            <!--Bottom right, where we put the console  -->
            <div id ="bottomRight" class ="component bottomRightUnzoomed">
                <!--The console --> 
                <div id = "console"> 
                    <div id = "pastmessages"> </div>     
                    <div id = "usrmessage" class = "usermessage">
                          <span class = "prompt"> > </span> 
                          <span class = "cursor"> </span> 
                          <input id = "userinput" ></input> 
                  </div> 
                </div>  
                 <s:form beanclass ="stripes.action.ConsoleActionBean" id = "consoleForm"> 
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
        <script type="text/javascript" src="js/lib/d3tip.js"></script> 
        <script type="text/javascript" src="js/lib/howler.min.js"></script> 
        <script type="text/javascript" src="js/lib/simple-statistics.min.js"></script> 


         

        <!--VISUALIZATIONS-->
        <script type="text/javascript" src="js/visualization/LineChart.js"></script> 
        <script type="text/javascript" src="js/visualization/BarChart.js"></script> 
        <script type="text/javascript" src="js/visualization/FreqBarChart.js"></script> 
        <script type="text/javascript" src="js/visualization/movingLinegraph.js"></script> 
        <script type="text/javascript" src="js/visualization/streamchart.js"></script> 
        <script type="text/javascript" src="js/visualization/CorrelationMatrix.js"></script> 
        <script type="text/javascript" src="js/visualization/PredictionChart.js"></script> 
        <script type="text/javascript" src="js/visualization/ClassificationChart.js"></script> 



        <!--AREAS-->
        <script type="text/javascript" src="js/area/chartarea.js"></script>
        <script type="text/javascript" src="js/area/datalayerarea.js"></script> 
        <script type="text/javascript" src="js/area/objects/plumb.js"></script> 
        <script type="text/javascript" src="js/area/objects/plumbtechniques.js"></script> 
        <script type="text/javascript" src="js/area/objects/datalayers.js"></script> 
        <script type="text/javascript" src="js/area/objects/techniques.js"></script> 
        <script type="text/javascript" src="js/area/consolearea.js"></script> 
        <script type="text/javascript" src="js/area/objects/labeler.js"></script> 
        <script type="text/javascript" src="js/area/objects/outputparser.js"></script> 
        <script type="text/javascript" src="js/area/objects/evaluator.js"></script> 
        <script type="text/javascript" src="js/area/objects/nback.js"></script>
        <script type="text/javascript" src="js/area/objects/classifier.js"></script> 


        <!--INTERFACE -->
        <script type="text/javascript" src="js/javainterface.js"></script> 
        <script type="text/javascript" src="js/events.js"></script> 
        
        <audio id="0A" src="audio/A0.wav" preload="auto"></audio>
        <audio id="0B" src="audio/A1.wav" preload="auto"></audio>
        <audio id="0C" src="audio/A2.wav" preload="auto"></audio>
        <audio id="1A" src="audio/B0.wav" preload="auto"></audio>
        <audio id="1B" src="audio/B1.wav" preload="auto"></audio>
        <audio id="1C" src="audio/B2.wav" preload="auto"></audio>
        <audio id="2A" src="audio/C0.wav" preload="auto"></audio>
        <audio id="2B" src="audio/B1.wav" preload="auto"></audio>
        <audio id="2C" src="audio/C2.wav" preload="auto"></audio>
        <audio id="3A" src="audio/D0.wav" preload="auto"></audio>
        <audio id="3B" src="audio/D1.wav" preload="auto"></audio>
        <audio id="3C" src="audio/D2.wav" preload="auto"></audio>


         
    </body>
    
</html> 
