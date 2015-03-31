/*Author: Sam Hincks
 * Class for controlling the console. 
 *Add messages to '#pastmessages'
 *'  #console refers to whole area
 *   #userInput refers to where we type
 *   
 **/

function ConsoleArea() {
   this.streaming = false;   //.. set to true if we are streaming
   this.streamInterval; 
   
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
       this.scrollToBottom();
    }
    
    
    /**Return false if we don't have support for routing this message in javascript. 
     *Otherwise, decide how to handle the user's input in javascript */ 
    this.parseLocally = function(userText) {
        userText = userText.trim();
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
         
         //.. if we made fs with wrong name
         if (userText.startsWith("makefs(")) {
             if (userText.indexOf(",") != -1) return true;
             return false;
         }
         
         if (userText.startsWith("streamsynch(")) {
             if (this.streaming) {
                this.displayMessage("A streaming procedure is already being run; terminate it with clearstream()", "systemmes", "redline");
                return true;
            }
             var mes = userText.split("(");
             var file = mes[1].split(")")[0];
             
             //.. callback that periodically issues a request to update; until what;
             this.streamInterval = setInterval(function() {
                this.streaming = true;
                $("#consoleInput").val("synchronize("+file+",none");
                javaInterface.postToConsole();
            }, 300);
            return true;
         }
         
        else if (userText.startsWith("stream(") || userText == "stream") {
            if (this.streaming){
                this.displayMessage("A streaming procedure is already being run; terminate it with clearstream()", "systemmes", "redline");
                return true;
            }

             //.. callback that periodically issues a request to update; until what;
            this.streamInterval= setInterval(function() {
                this.streaming = true;
                $("#consoleInput").val(userText);
                javaInterface.postToConsole();
            }, 50); //.. less than 50 and there are errors
            return false;
         }
         
         //.. For periodically updating what the current label is of a synchronized
         //... dataset
         else if(userText.startsWith("streamlabel(")) {
            if (!this.streaming){
                consoleArea.displayMessage("Must first apply a procedure for synchronizing the database using stream(dbname)", "systemmes", "redline");
                return true;
            }
             
            var mes = userText.split("("); //.. will be parameters (100,200)
            var params = mes[1].split(",");
            
            if (params.length != 4) {
                this.displayMessage("There ought to be 4 parameters: filename, conditionName, valA%valB%valC, [seconds]%[#trialsOfEach]%[secondsOfRest]", "systemmess", "redline" );
                return false;
            }
            
            var filename = params[0];
            var conditionName = params[1];
            var conditions = params[2].split("%");
            var timing = params[3].split("%");
            var trialLength = parseInt(timing[0]);
            var trialsOfEach = parseInt(timing[1]);
            var restLength = parseInt(timing[2]);
            labeler = new Labeler();
            console.log(timing + "," + trialLength + "," + trialsOfEach + "," +restLength);
            labeler.initiateLabeling(filename,conditionName,conditions,trialLength,trialsOfEach,restLength); 
            return false;
         }
         
         else if(userText.startsWith("clearstream")){
             this.streaming = false;
             clearInterval(this.streamInterval); 
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
        + " If you have no idea how to begin and no data to work with, simply type tutorial";
         
        this.displayMessage(message, "systemmess", "secondline");
    };
    
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

           // bdo2.addClass(id +"-"+index+"-"+i+strikeThrough);
            bdo.append(bdo2);
        }

        //var lineId = id+"-"+index+"-"+strikeThrough;
        //d.addClass(lineId);
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
    
     /* ------------------
      * NO LONGER IN USE! **/
     
     
     /** Next step is to change it into multiple concurrent filepassings. As we see, 
     * whereas the original file was 2 million; we were only able to pass about 500,000
     * we should figure out the absolute limit, and make it into many concurrent passings
     * with different signatures. 
     */
    this.passFile = function(filename, filedata) {
        //.. Display message acknologing file being sent
        var charsPerPass = 3;//500000.0;
        var numPasses = Math.ceil(filedata.length / charsPerPass);
        var start =0;
        filedata= filedata.replace("\n", "cr13");

        console.log(filedata);
        this.displayMessage("> " + filename +", " + filedata.length, "usermessage", "");
        //..  given the limit for how much we can pass at a time, partition into
        //.. segments and pass one at a time
        for (var i =0; i< numPasses; i++) {
            var segment = filedata.substring(start, start +charsPerPass);
            console.log(segment);
            start += charsPerPass;
             $("#consoleInput").val("filemessage%%%"+i+"%%%"+numPasses+"%%%"+filename +"%%%"+segment);
            javaInterface.postToConsole();
            for (var k =0; k < 4000000;k++) {
                var f = 2*2;
            }
        }
       
        this.scrollToBottom();
    }
}

 
    
      