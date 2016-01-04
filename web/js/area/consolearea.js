/*Author: Sam Hincks
 * Class for controlling the console. 
    *Add messages to '#pastmessages'
 *'  #console refers to whole area
 *   #userInput refers to where we type  **/

function ConsoleArea() {
   this.streaming = false; //.. set to true if we are streaming
   this.streamInterval; 
   this.pings =[] //.. a hash of commands we are pinging at the server 
   duplicatesAdded =0;//.. keep track of how many of the same command, eg classifylast we have created
   var labeler = new Labeler();
   this.messageStack = []; //.. save all the users old messages. Retrieve with arrows. Delete if erroneous
   this.commands; //.. initialize this when we start. In other words, ping server
   
   
   /**Display a message to the user;
    *primaryClass = system versus userMessage
    *secondaryClass = redLine, greenLine, orangeLine*/
   this.displayMessage = function(message, primaryClass, secondaryClass) {
        if (arguments.length ==1) {primaryClass = "systemmess"; secondaryClass = "blueline";}
        if (message ==null)return;

       ///.. ;; denotes splitting into new message
        var splitByNewMessage = message.split(";;");
        for (var i = 0; i < splitByNewMessage.length; i++) {
            var subMessage = splitByNewMessage[i];
                
            //.. :: denotes splitting by line break
            var splitByNewLine = subMessage.split("::");
            for (var j = 0; j < splitByNewLine.length; j++) {
                var subSubMessage = splitByNewLine[j];
                var d = $("<div></div>").text(subSubMessage);
                d.addClass(primaryClass);
                d.addClass(secondaryClass);
                $("#pastmessages").append(d);
            }
            
            //.. if its not the last one
            if (i!= splitByNewMessage.length-1) 
                this.newLine();
        }
        //.. Pause, then scroll to bottom
        setTimeout(function() {
            consoleArea.scrollToBottom(); //.. must refer explicitly object
        }, 20);
    };
    
    /**Parse the user's input*/
    this.parseUserMessage = function(userText) {
        this.displayMessage("> " +userText, "usermessage", "");
        
        if (this.streaming && !(userText.startsWith("streamlabel") || userText.startsWith("slopes")|| userText.startsWith("repeat")  || userText.startsWith("nback") || userText.startsWith("interceptlab") )) 
            this.parseLocally("clearstream");
        
        if(!this.parseLocally(userText)) {   
            //.. save the text to the imaginary form for Stripes purposes
            $("#consoleInput").val(userText);
            
            //.. now get the server's response'
            javaInterface.postToConsole();
         }
        
       if (this.messageStack[this.messageStack.length-1] != userText)
           this.messageStack.push(userText);
       
        //.. remove the text
       $("#userinput").val("");  
       this.upped =0; 
       this.scrollToBottom();
    }
    
    
    /**Return false if we don't have support for routing this message in javascript. 
     *Otherwise, decide how to handle the user's input in javascript */ 
    this.parseLocally = function(userText) {
        userText = userText.trim();
        //.. if this is the end of a streamlabel query
        if (labeler.awaitingFeedback) {
            labeler.parseFeedback(userText);
            return true;
        }
        //.. redirect all input to seeing if theyre getting the right nback response
        if (nbackEvaluator.active && !(userText.startsWith("interrupt"))) {
            nbackEvaluator.guess(userText);
            return true;
        }
        if(userText.startsWith("view.")){
            this.parseViewMessage(userText);
            return true; //.. parse it locally only
        }
         
         //.. if this is an evaluation command first save the connections to the context
        if(userText.startsWith("evaluate") || userText.startsWith("train") 
                 ||userText.startsWith("classify") ) {
             javaInterface.postConnectionsToTechnique();
             return false;//.. return false as we still want to go to java
         }
        
        //.. for realtime visualization, update stream repeatedly, and terminate it when I do another command
        else if (userText.startsWith("stream(") || userText == "stream") {
            if (this.streaming){
                this.displayMessage("A streaming procedure is already being run; terminate it with clearstream()", "systemmes", "redline");
                return true;
            }
            
            this.streaming = true;
             //.. callback that periodically issues a request to update; until what;
            this.streamInterval= setInterval(function() {
                $("#consoleInput").val(userText +"(" + datalayerArea.datalayers.lastSelectedId);
                javaInterface.postToConsole();
            }, 100); //.. less than 50 and there are errors
            return false;
         }
         
         
         //.. For periodically updating what the current label is of a synchronized
         //... dataset
         else if(userText.startsWith("streamlabel")) {
            //.. get the two possible set of parameters
            var mes = userText.split("("); //.. will be parameters (100,200)
            var params = new Array();
            if (mes.length >1) params  = mes[1].split(",");
            
            //.. full set
            if (params.length ==0) {
               params = new Array();
               params[0] = "easy%rest"
               params[1] = "30%1%5";
               params[2] = "realtime1";
               params[3] = "condition";
            }
            
            if (params.length ==2) {
               params[2] = "realtime1";
               params[3] = "condition";            
            }   
            
           //.. extract parameters for the labeler
            var filename = params[2];
            var conditionName = params[3];
            var conditions = params[0].split("%"); //.. error if trailing %  
            var timing = params[1].split("%");
            var trialLength = parseInt(timing[0]);
            var trialsOfEach = parseInt(timing[1]);
            var restLength = parseInt(timing[2]);
            labeler = new Labeler();
            
            //.. a little hacky: if we dont want feedback, then we do streamlabel2, but nobody else needs to know that
            var feedback = false;
            if(userText.startsWith("streamlabel2")) feedback =true;
            
            labeler.initiateLabeling(filename,conditionName,conditions,trialLength,trialsOfEach,restLength,feedback); 
            return true;
         }
           
         else if(userText.startsWith("clearstream")){
             this.streaming = false;
             clearInterval(this.streamInterval); 
             return true;
         }
         
         else if (userText.startsWith("anticorrelated")){
             var smallest = classifier.getSmallestCorrelation();
             consoleArea.displayMessage("Smallest correlation is " +  smallest[0] + " between "+ smallest[1] + " and " + smallest[2]);
             return true;
         }
         else if (userText.startsWith("journal")) {
            consoleArea.displayMessage("Journaling... ");
             $("#usrmessage").append("<textarea id = journalinput></textarea>");
             $("#userinput").remove();
             $("#bottomRight").removeClass("bottomRightUnZoomed")
             $("#bottomRight").addClass("bottomRightZoomed")
             //.. next make so that it cant be resized, and so that it is the right size, has the right font
             //.. and suggests interaction, like the words you are writing, especialyl at the start have a special flair to them 
            //.. and make so that it takes up the whole left half of the screen 
            return true;
         }
         
         //.. get the slopes of all channels; in additoin commence streaming if its not 
         else if (userText.startsWith("slopes")) {
             if (this.streaming == false)  {
                 this.parseLocally("stream");
             }
             var slope = classifier.getSlope(classifier.channel);
             var val = Math.round(slope[0] * 100) / 100;
             var dev =  Math.round(slope[1] * 100) / 100;
             $("#currSlope").text(val);
             $("#currStdev").text(dev);
             
            slope = classifier.getSlope(classifier.channel2);
            val = Math.round(slope[0] * 100) / 100;
            dev = Math.round(slope[1] * 100) / 100;
            $("#currSlope2").text(val);
            $("#currStdev2").text(dev);
             
            slope = classifier.getCorrelationKBack(classifier.channel, classifier.channel2);
            val = Math.round(slope[0] * 100) / 100;
            dev = Math.round(slope[1] * 100) / 100;
            $("#currSlope3").text(val);
            $("#currStdev3").text(dev);
             
             //if (dev > 1) alert("bajs");
             return true;
         }
         
         //.. repeat(classifyLast(), 200) shoots the command classifyLast to the server every 200ms
         else if (userText.startsWith("repeat:")) {
            var mes = userText.split(":"); //.. will be parameters (100,200)
            var params = mes[1].split(";");
            var command = params[0];
            command = command.replace(")","")
            var commandName = command.split("(")[0];
            var delay = 1000;
            
            //.. if delay value is specified, set it
            if (params.length >1){
                delay = params[1];
                delay = delay.replace(")","");
                if (delay < 20) {
                    consoleArea.displayMessage("Delay must be greater than 20");
                    return true;
                }
            }
            
            //.. for this one, we must intersect layer with techniques
            if (command.startsWith("classifylast")) javaInterface.postConnectionsToTechnique();

            //.. set an interval to repeat, and store it so that we can delete
            if (this.pings[commandName] != null){ 
                commandName = commandName +duplicatesAdded;
                duplicatesAdded++;
            }
            
            var self = this;
            this.pings[commandName] =  setInterval(function() {
                if (command.startsWith("slopes")) {
                    self.parseLocally(command);
                }
                else {
                    $("#consoleInput").val(command);
                    javaInterface.postToConsole();
                }
               
            }, delay);
            consoleArea.displayMessage("Pinging " + command  +  " every " + delay);
            
            return true;
         }
         //.. terminate specified pinging or all
         else if (userText.startsWith("stop")) {
            var mes = userText.split("("); //.. will be parameters (100,200)
            if (mes.length >1) {
                var params = mes[1].split(",");
                var command = params[0];
                command = command.replace(")", "");
                clearInterval(this.pings[command]);
            }
            else {
                for (var i in this.pings) {
                    clearInterval(this.pings[i]);
                }
            }
            return true;
         }
         return false;
    }
    
    /**Parse a message that will never be sent to Java; it only affects something in the view*/
    this.parseViewMessage = function(message) {
        if(message.indexOf("inter") != -1)
            datalayerArea.getIntersectedTechniques(datalayerArea.datalayers.dls[0].id);
        else if (message.indexOf("flatten")!= -1)
          chart.setDataToOne();
       
        else if (message.indexOf("show") != -1){
            message = message.replace(")","")
            var mes = message.split("("); //.. will be parameters (100,200)
            var params = mes[1].split(",");
            chartArea.show2DIndexes(params);
        }
       
        //.. the time it takes for a d3Chart transition to start and last
        else if (message.indexOf("transition")!= -1){
            var mes = message.split("("); //.. will be parameters (100,200)
            var params = mes[1].split(",");
            chartArea.transitionLength =params[0];
            chartArea.transitionTime = params[1]; //.. this is controlled from outside the chart object
        }
      
       else if (message.indexOf("undo")!= -1)
          chart.reset();
    }
    
    /**Append a new line*/
    this.newLine = function() {
        $("#pastmessages").append("</br>");
    };
    
    /**Scroll to the bottom of the console*/
    this.scrollToBottom = function() {
        //.. scroll to end of input//.. refocus on our input element
        $("#console").each( function() {
            var scrollHeight = Math.max(this.scrollHeight, this.clientHeight);
            this.scrollTop = scrollHeight - this.clientHeight;
        }); 
        $("input").focus();

    };
      
      /**Standard introductory message*/
     this.introduce = function() {
        var message = "Welcome - This is an interface for evaluating trial-based timeseries datasets, and for training "
        + " machine learning algorithms to broadcast meaningful classifications across a port in realtime;; "
        + " Please type selfcalibrate in the console and hit enter...";
         
        this.displayMessage(message, "systemmess", "secondline");
    };
    
    this.displayJSONObj = function(object) {
        var div = $("<div></div>");
        for (var i in object) {
            var attr = object[i];
            var bdo = this.getWord(i, "greyline");
            div.append(bdo);
            bdo = this.getWord(attr, "");
            div.append(bdo);
            div.append("</br></br>");
        }
        div.addClass("systemmess");
        $("#pastmessages").append(div);
        this.scrollToBottom();
    }

    
    /** A method for neatly presenting the information gain of attribtues to users **/
    this.displayAttributes = function(attributes) {
        var div = $("<div></div>");
        attributes.sort(function (a,b){;return b.value - a.value;});
        var addedUpTo = attributes.length -1;
        for (var i =0; i < attributes.length; i++) {
            var attr = attributes[i];
            if (attr.value >0) {
                var bdo = this.getWord(attr.label, "greenline");
                div.append(bdo);
                bdo = this.getWord(attr.value, "");
                div.append(bdo);
                div.append("</br></br>");
            }
            else{
                addedUpTo = i;
                break;
            }
        }
        if (addedUpTo < attributes.length-1){
            var rest ="";
            for (var i =addedUpTo; i< attributes.length;i++) {
                var attr = attributes[i];
                rest = rest + attr.label +", ";
            }
            var bdo = this.getWord(rest, "greenline");
            div.append(bdo);
            bdo = this.getWord(0, "");
            div.append(bdo);
            div.append("</br></br>");
        }
        div.addClass("systemmess");
        $("#pastmessages").append(div);
    }
    
    /* So that we know locally what the available commands are, as encoded in the java server */
    this.setCommands = function(commands, display) {
       var div = $("<div></div>");
       var cmdArray = commands.commands;
       this.commands = cmdArray;
       if(display) {
            for (var i =0; i < cmdArray.length; i++) {
                var d = this.addCommand(cmdArray[i]);
                div.append(d);
            }
            div.addClass("systemmess");
             $("#pastmessages").append(div);
         }
    }
    
    /*Display one command neatly in the console*/
    this.addCommand = function(command) {
        var d = $("<div></div>");
        //.. 1 ID
        var bdo = this.getWord(command.id+":", "redline");
        d.append(bdo);
        
        //.. 2. Description
        bdo = this.getWord(command.documentation,"");
        d.append(bdo);
        
        //.. 3. Parameters, if any
        if(command.parameters){
            d.append("</br>");
            bdo = this.getWord(command.parameters, "greyline");
            d.append(bdo);
        }

        d.append("</br></br>");
        return d;
    }
   
    /** Returns a bdo tag of the word int the specified color (a class"**/
    this.getWord = function(word, colorClass) {
        var bdo = $("<bdo></bdo>").text(" ");
        bdo.addClass(colorClass);
        bdo.append(word);
        return bdo;
    }
    /**Return a fat, manipulable div, packed with bdos that have an id set, to location + id**/
    this.getCharacters = function(message, strikeThrough) {
        var d = $("<div></div>");

        if(!strikeThrough)
            bdo = $("<bdo></bdo>").text(" ");
        else
            bdo = $("<del></del>").text(" ");

        bdo.append(message);
        //.. Iterate through the characters of message
        for (var i =0; i <message.length; i++) {
            var ch = message[i];
            var bdo2; 
            if(!strikeThrough)
                bdo2 = $("<bdo></bdo>").text(ch);
            else
                bdo2 = $("<del></del>").text(ch);

            bdo.append(bdo2);
        }

        d.append(bdo);
        return d;
    }
    
    this.upped = 0;
    this.getLastUp = function() {
        if (this.upped < this.messageStack.length)
            this.upped++;
        return this.messageStack[this.messageStack.length-this.upped];
    }
    
    this.getLastDown = function() {
        if (this.upped > 0 )
            this.upped--;
        return this.messageStack[this.messageStack.length - this.upped];
    }

    /**Return command that matches, print it too, giving info**/
    this.search = function(text) {
        var found = null;
        var div = $("<div></div");
        var added =0;
        for (var i =this.commands.length-1; i >=0; i--) {
            var cmd = this.commands[i]; 
            if(cmd.id.startsWith(text)){
                if(added !=0) div.append("</br>");
                found = cmd.id;
                div.append(this.addCommand(cmd));
                added++;
            }
        }
        div.addClass("systemmess");
        if (found) {
            $("#pastmessages").append(div);
            this.scrollToBottom();
        }

        return found;
    }
    
    
}

 
    
      