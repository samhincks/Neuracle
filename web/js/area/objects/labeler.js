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
    
    /**Initiate the labeling, and display helpful messages to the conesole
     **/
    this.initiateLabeling = function(filename,conditionName,conditions,
        trialLength, trialsOfEach,restLength) {
            console.log(trialLength);
        this.trialLength  = parseInt(trialLength * 1000);
        this.restLength = parseInt(restLength * 1000);
        this.iterations =0;
        this.trialsToDo = parseInt(trialsOfEach * conditions.length);
        this.conditions= conditions;
        this.conditionName = conditionName;
        this.fileName = filename;
        
        consoleArea.displayMessage("Initiating labeling protocol for a total of " +this.trialsToDo 
                +"trials on " +this.fileName + " for " + trialLength + "s with rest of " + restLength + "s", "systemmes", "blackline");
        self = this;
        this.labelCondition();
    }
    
    /**Alternate conditions and rest, ping the server each time the label switches, 
     * which alternates how data will be labeled**/
    this.labelCondition = function() {
        var message = "label(" + self.fileName + "," + self.conditionName + ","+self.conditions[self.iterations%self.conditions.length]+")";
        $("#consoleInput").val(message);
        javaInterface.postToConsole();
        
        self.iterations++;
        if (self.iterations < self.trialsToDo) 
            setTimeout(self.labelRest, self.trialLength);
        else{
            setTimeout(self.labelJunkAndEnd, self.trialLength);
            consoleArea.displayMessage("Experiment Complete", "systemmes", "greenline");
        }
    }
    
    this.labelJunkAndEnd = function() {
        var message = "label(" + self.fileName + "," + self.conditionName + ",junk)";
        $("#consoleInput").val(message);
        javaInterface.postToConsole();
    }
    
    this.labelRest = function() {
        var message = "label("+self.fileName +","+ self.conditionName+",rest)";
        $("#consoleInput").val(message);
        javaInterface.postToConsole();
        setTimeout(self.labelCondition, self.restLength);
    }
    
    
}
