/* A front-end labeling mechanism interacts with a realteim streaming server. */
function Labeler()
{
    this.times =0; //.. used for setInterval
    this.labelInterval; 
    this.trialLength; 
    this.restLength;
    this.iterations;
    this.filename;
    this.conditionName;
    this.conditions;
    this.trialsToDo;
    this.conditionName;
    this.fileName;
    var self;
    this.feedback = false;
    
    //.. used by console area to know if we're awaiting user feedback
    this.awaitingFeedback = false;
    this.num =1; //.. I hate myself a little because of this, but I cant figure out how to give function's input in setTimeout
    
   //.. constants
    this.FEEDBACKDELAY = 4000; //.. a little delay so that nback input doesnt become feedback input
    
    /**Initiate the labeling, and display helpful messages to the conesole**/
    this.initiateLabeling = function(filename,conditionName,conditions,
        trialLength, trialsOfEach,restLength,feedback) {
        this.trialLength  = parseInt(trialLength * 1000);
        this.restLength = parseInt(restLength * 1000);
        this.iterations =0;
        this.trialsToDo = parseInt(trialsOfEach * conditions.length);
        this.conditions= conditions;
        this.conditionName = conditionName;
        this.fileName = filename;
        this.feedback = feedback;
        consoleArea.displayMessage("Initiating labeling protocol for a total of " +this.trialsToDo 
                +" trials on " +this.fileName + " for " + trialLength + "s with rest of " + restLength + "s", "systemmess", "blackline");
        self = this;
        // streamlabel(visual-1, 10%1%1)
        
        var r= $('<input type="button" id ="startButton" value="click to begin n-back... "/>');
        $("#topRight").append(r);
        
        $("#startButton").on('click', function(){
            self.labelCondition(this.feedback);
        });
       
       // setTimeout(function() {
         //   self.labelCondition(this.feedback);
      //  }, self.restLength);
    }
    
    /**Alternate conditions and rest, ping the server each time the label switches, 
     * which alternates how data will be labeled
     * streamlabel(visual-1,30%1%10)
     * **/
    this.labelCondition = function() {
        var curCondition = self.conditions[self.iterations%self.conditions.length];
        if (curCondition == "easy" || curCondition == "hard") 
            curCondition += "%" + (self.trialLength);
        
        //.. send message to backend
        var message = "label(" + self.fileName + "," + self.conditionName + ","+curCondition+")";
        $("#consoleInput").val(message);
        javaInterface.postToConsole();
        self.iterations++;
        self.num =1;
        
        if(curCondition.startsWith("dual")) {
            var values = curCondition.split("-");
            if (values.length ==1)
                nback.begin(self.trialLength,1, true, true);
            nback.begin(self.trialLength, parseInt(values[1]), true, true);
        }
        
        if(curCondition.startsWith("visual")) {
        

            var values = curCondition.split("-");
            if (values.length ==1)
                nback.begin(self.trialLength, 1, false, true);

            nback.begin(self.trialLength, parseInt(values[1]), false, true);
        }
        
        if (curCondition.startsWith("audio")) {
            var values = curCondition.split("-");
            nback.begin(self.trialLength, parseInt(values[1]), true, false);
        }
        
        if (curCondition.startsWith("hgwells")) {
            turnbook.init();        
        }
        
        
        //.. initiate rest
        self.restOrJunk(self.trialLength);
    }
  
   //.. rest or junk, depending on iteration
   this.restOrJunk = function(delay) {
        if (self.iterations < self.trialsToDo)
            setTimeout(self.labelRest, delay);
        else {
            setTimeout(self.labelJunkAndEnd, delay);
            consoleArea.displayMessage("Last trial!", "systemmess", "greenline");
        }  
    }
    
    //..  Query user's reported mental load, then go on. 
    this.solicitFeedback = function() {
        self.awaitingFeedback = true;
        //.. we need to deactivate our nbackevaluator here too. Now we shouldnt need this
        //if (self.num ==1 &&nbackEvaluator != null) nbackEvaluator.deactivate();
        
        if (self.num == 1){
            consoleArea.displayMessage("How would you describe your own cognitive workload that trial? (1=low, 2=medium, 3=high)", "systemmess", "blueline");
        }
        if (self.num ==2) {
            consoleArea.displayMessage("How would you describe your mental engagement that trial? (1=focused, 2=distracted)", "systemmess", "blueline");
        }
        
    }
    
    //.. from console area  - parse the feedback, and send it to the backend
    this.parseFeedback = function(input) {
         this.awaitingFeedback =false;
        
        var conValue = null;
        //.. if its valid input, send a message to the back end saying -- whatever was the label,
        //... add a new label feedback1, if it doesnt exist, and make so that it has this value for as many back as the last trial
        if (input == "1") {
           conValue = "one";
        }
        else if (input =="2") {
            conValue = "two";
        }
        else if (self.num == 1 && input == "3") {
            conValue = "three"; 
       }
        else {
            if (self.num ==1){
                consoleArea.displayMessage("Please enter 1, 2 or 3", "systemmess", "redline");
                self.num =2;
                this.solicitFeedback();
            }
            if (self.num ==2) {
                consoleArea.displayMessage("Please enter 1, 2 or 3", "systemmess", "redline");
                self.num =1;
                this.labelRest();
            }
            return;
        }
        
        //.. if its a sensible response ship it off to the back-end
        if (conValue != null) {
            var mess  = "retrolabel(feedback"+self.num+",condition,"+conValue+",1,realtime1)";
            $("#consoleInput").val(mess);
            javaInterface.postToConsole();
        }
        
        //.. if theres a feedback2, then collect that
        if(self.num ==1){
            self.num =2;
            setTimeout(self.solicitFeedback, self.FEEDBACKDELAY/4);;
        }
        
        //.. otherwise continue with normal labeling procedure
        else if (self.num ==2){
            if (self.iterations < self.trialsToDo)
                setTimeout(self.labelCondition,self.FEEDBACKDELAY/4);
        }
            
    }
    
    this.labelJunkAndEnd = function() {
        var message = "label(" + self.fileName + "," + self.conditionName + ",junk)";
        $("#consoleInput").val(message);
        javaInterface.postToConsole();
        if (self.feedback) {
            setTimeout(self.solicitFeedback, self.FEEDBACKDELAY);
        }
        
        
    }
    
    this.labelRest = function() {
        var message = "label("+self.fileName +","+ self.conditionName+",rest)";
        $("#consoleInput").val(message);
        javaInterface.postToConsole();
        
        //.. no feedback between trials, so pause and continue with loop
        if (self.feedback == false) 
            setTimeout(self.labelCondition, self.restLength);
        else
            setTimeout(self.solicitFeedback, self.FEEDBACKDELAY);
    }
    
    
}
