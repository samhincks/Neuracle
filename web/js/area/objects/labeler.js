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
        this.trialLength  = parseInt(trialLength * 1000);
        this.restLength = parseInt(restLength * 1000);
        this.iterations =0;
        this.trialsToDo = parseInt(trialsOfEach * conditions.length);
        this.conditions= conditions;
        this.conditionName = conditionName;
        this.fileName = filename;
        
        consoleArea.displayMessage("Initiating labeling protocol for a total of " +this.trialsToDo 
                +" trials on " +this.fileName + " for " + trialLength + "s with rest of " + restLength + "s", "systemmess", "blackline");
        self = this;
        this.labelCondition();
    }
    
    /**Alternate conditions and rest, ping the server each time the label switches, 
     * which alternates how data will be labeled**/
    this.labelCondition = function() {
        var curCondition = self.conditions[self.iterations%self.conditions.length];
        if (curCondition == "easy" || curCondition == "hard") 
            curCondition += "%" + (self.trialLength);
           
        var message = "label(" + self.fileName + "," + self.conditionName + ","+curCondition+")";
        $("#consoleInput").val(message);
        //consoleArea.displayMessage("Prepare for " + curCondition, "systemmess", "greenline");
        javaInterface.postToConsole();
        
        self.iterations++;
        if (self.iterations < self.trialsToDo) 
            setTimeout(self.labelRest, self.trialLength);
        else{
            setTimeout(self.labelJunkAndEnd, self.trialLength);
            consoleArea.displayMessage("Last trial!", "systemmess", "greenline");
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
