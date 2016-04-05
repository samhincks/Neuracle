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
        <link type="text/css" rel="stylesheet" href="css/nback.css" />

        <link type="text/css" rel="stylesheet" href="css/jquery-ui.css" /> 
        <link type="text/css" rel="stylesheet" href="css/style.css" />
        <link type="text/css" rel="stylesheet" href="css/d3Style.css" />
        <link type="text/css" rel="stylesheet" href="css/consoleStyle.css" /> 
        <link type="text/css" rel="stylesheet" href="css/dlStyle.css" />
        <link type="text/css" rel="stylesheet" href="css/turnbook.css" />

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
                  <!---  <div id="flipbook">
                        <div class="hard"> Turn.js </div> 
                        <div class="hard"></div>
                        <div style="background-image:url(https://raw.github.com/blasten/turn.js/master/demos/magazine/pages/01.jpg)"></div>
                        <div style="background-image:url(https://raw.github.com/blasten/turn.js/master/demos/magazine/pages/02.jpg)"></div>
                        <div style="background-image:url(https://raw.github.com/blasten/turn.js/master/demos/magazine/pages/03.jpg)"></div>
                        <div style="background-image:url(https://raw.github.com/blasten/turn.js/master/demos/magazine/pages/04.jpg)"></div>
                        <div style="background-image:url(https://raw.github.com/blasten/turn.js/master/demos/magazine/pages/05.jpg)"></div>
                        <div style="background-image:url(https://raw.github.com/blasten/turn.js/master/demos/magazine/pages/06.jpg)"></div>
                        <div style="background-image:url(https://raw.github.com/blasten/turn.js/master/demos/magazine/pages/07.jpg)"></div>
                        <div style="background-image:url(https://raw.github.com/blasten/turn.js/master/demos/magazine/pages/08.jpg)"></div>
                        <div style="background-image:url(https://raw.github.com/blasten/turn.js/master/demos/magazine/pages/09.jpg)"></div>
                        <div style="background-image:url(https://raw.github.com/blasten/turn.js/master/demos/magazine/pages/10.jpg)"></div>
                        <div style="background-image:url(https://raw.github.com/blasten/turn.js/master/demos/magazine/pages/11.jpg)"></div>
                        <div style="background-image:url(https://raw.github.com/blasten/turn.js/master/demos/magazine/pages/12.jpg)"></div>

                        <div class="hard"></div>
                        <div class="hard"></div>
                    </div> -->
                <!--
                  <div class="t">
                      <div class="tc rel">
                          <div class="book" id="book">
                              <div class="hard">Pride and Prejudice</div>
                              <div class="page">   It is a truth universally acknowledged, that a single man in possession of a good fortune, must be in want of a wife.

   However little known the feelings or views of such a man may be on his first entering a neighbourhood, this truth is so well fixed in the minds of the surrounding families that he is considered as the rightful property of some one or other of their daughters.

   "My dear Mr. Bennet," said his lady to him one day, "have you heard that Netherfield Park is let at last?"

   Mr. Bennet replied that he had not.

   "But it is," returned she; "for Mrs. Long has just been here, and she told me all about it."
   Mr. Bennet made no answer.

   "Do not you want to know who has taken it?" cried his wife impatiently.

   "You want to tell me, and I have no objection to hearing it."

   This was invitation enough.


</div>
                              
                              <div class="page">"Is that his design in settling here?"

   "Design! nonsense, how can you talk so! But it is very likely that he may fall in love with one of them, and therefore you must visit him as soon as he comes."

   "I see no occasion for that. You and the girls may go, or you may send them by themselves, which perhaps will be still better, for as you are as handsome as any of them, Mr. Bingley might like you the best of the party."

   "My dear, you flatter me. I certainly have had my share of beauty, but I do not pretend to be any thing extraordinary now. When a woman has five grown-up daughters she ought to give over thinking of her own beauty."

   "In such cases a woman has not often much beauty to think of."

   "But, my dear, you must indeed go and see Mr. Bingley when he comes into the neighbourhood."

   "It is more than I engage for, I assure you."</div>
                              <div class="page">"But consider your daughters. Only think what an establishment it would be for one of them. Sir William and Lady Lucas are determined to go, merely on that account, for in general, you know, they visit no new-comers. Indeed you must go, for it will be impossible for us to visit him if you do not."

   "You are over-scrupulous surely. I dare say Mr. Bingley will be very glad to see you; and I will send a few lines by you to assure him of my hearty consent to his marrying whichever he chuses of the girls: though I must throw in a good word for my little Lizzy."

   "I desire you will do no such thing. Lizzy is not a bit better than the others; and I am sure she is not half so handsome as Jane, nor half so good-humoured as Lydia. But you are always giving her the preference."

   "They have none of them much to recommend them," replied he; "they are all silly and ignorant, like other girls; but Lizzy has something more of quickness than her sisters."</div>
                              <div class="page"> "Mr. Bennet, how can you abuse your own children in such a way! You take delight in vexing me. You have no compassion on my poor nerves."

   "You mistake me, my dear. I have a high respect for your nerves. They are my old friends. I have heard you mention them with consideration these twenty years at least."

   "Ah! you do not know what I suffer."

   "But I hope you will get over it, and live to see many young men of four thousand a year come into the neighbourhood."

   "It will be no use to us if twenty such should come, since you will not visit them."

   "Depend upon it, my dear, that when there are twenty, I will visit them all."</div>
                              <div class="page">Mr. Bennet was so odd a mixture of quick parts, sarcastic humour, reserve, and caprice, that the experience of three-and-twenty years had been insufficient to make his wife understand his character. Her mind was less difficult to develope. She was a woman of mean understanding, little information, and uncertain temper. When she was discontented she fancied herself nervous. The business of her life was to get her daughters married; its solace was visiting and news.

</div>
                              <div class="page"></div>
                              <div class="page"></div>
                              <div class="page"></div>
                              <div class="page"></div>
                              <div class="page"></div>
                              <div class="page"></div>
                              <div class="page"></div>
                              <div class="page"></div>

                          </div>
                      </div>
                  </div> -->

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
                    
                    <tr>
                        <td> </td>
                        <td> </td>
                        <td> </td>

                        <td> 
                            <div id ="currSlope4">000</div>
                        </td>

                        <td> 
                            <div id ="currStdev4">000</div>
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
        <script type="text/javascript" src="js/lib/turn.min.js"></script> 



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
        <script type="text/javascript" src="js/area/objects/journal.js"></script> 



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


         
<script type="text/javascript">
	/*
 * Turn.js responsive book
 */

/*globals window, document, $*/

(function () {
    'use strict';

    var module = {
        ratio: 1.38,
        init: function (id) {
            var me = this;

            // if older browser then don't run javascript
            if (document.addEventListener) {
                this.el = document.getElementById(id);
                this.resize();
                this.plugins();

                // on window resize, update the plugin size
                window.addEventListener('resize', function (e) {
                    var size = me.resize();
                    $(me.el).turn('size', size.width, size.height);
                });
            }
        },
        resize: function () {
            // reset the width and height to the css defaults
            this.el.style.width = '';
            this.el.style.height = '';

            var width = this.el.clientWidth,
                height = Math.round(width / this.ratio),
                padded = Math.round(document.body.clientHeight * 0.9);

            // if the height is too big for the window, constrain it
            if (height > padded) {
                height = padded;
                width = Math.round(height * this.ratio);
            }

            // set the width and height matching the aspect ratio
            this.el.style.width = width + 'px';
            this.el.style.height = height + 'px';

            return {
                width: width,
                height: height
            };
        },
        plugins: function () {
            // run the plugin
            $(this.el).turn({
                gradients: true,
                acceleration: true
            });
            // hide the body overflow
            document.body.className = 'hide-overflow';
        }
    };

    module.init('book');
}());
</script>
    </body>
    
</html> 
