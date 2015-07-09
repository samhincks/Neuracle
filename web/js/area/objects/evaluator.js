function Evaluator(seqNum, con) { // streamlabel(easy,15%1%1)
    var back =0;
    var HARD = 2; //.. MAYBE WE SHOULD DO THIS AS SOME KIND OF INPUT
    var INTERRUPT = false; //.. Set to true if we want to start over when the user errs. Doesnt really work now
    var INTERRUPTPAUSE = 3000;
    
    //.. specify condition, default to 0
    if (arguments.length >=2) {
        if (con.startsWith("easy")) back =0;
        if (con.startsWith("hard")) back =HARD; 
    }
    //.. whether or not we're running
    this.active; 
    this.done = false;
    if (seqNum >-1) this.active = true;
    else this.active = false;
    
    //.. hardcoded and abbreviated versions of the sequences
    var a = [1,8,2,3,9,5,4,0,7,6,0,2,8,3,5,4,0,1,8,4,6,2,9,7,3,5,1,6];
    var b = [7,4,9,0,8,3,6,1,5,2,8,0,5,1,2,6,2,6,5,1,8,0,3,4,9,7,2,9];
    var c = [4,0,3,5,8,7,2,6,1,9,6,4,3,2,9,7,7,0,6,4,9,1,8,2,3,5,1,5];
    var d = [2,6,5,1,8,0,3,4,9,7,2,9,3,6,7,4,7,0,6,4,9,1,8,2,3,5,1,5];
    
    //.. save the sequeence
    this.sequence;
    if (seqNum == 0) this.sequence =a;
    if (seqNum == 1) this.sequence =b;
    if (seqNum == 2) this.sequence =c;
    if (seqNum == 3) this.sequence =d;
    
    //.. user guessesand positioning
    this.position =0;
    this.correct = 0;
    this.wrong = 0;
    this.userEntries;
    this.numInterrupted =0;
    this.guesses =[];
    var self = this;
    
    //.. for the interval and timing code
    this.inter;
    var DELAY = 1500; //.. (these values empirically set)
    var INTERVAL = 2460;
    this.predictedPosition =0;
    
    //.. hazard a guess, and show if its correct or not
    this.guess = function(g) {
        g= g*1; //.. coerce to integer
        this.guesses.push(g); //.. store for safe keeping
        
        //.. if correct
        if(g == this.sequence[this.predictedPosition-back-1]) {
            consoleArea.displayMessage(":)", "systemmess", "greenline");
            this.correct++;
        }
          
        
        //.. a little bit of hack because I'm nto a great programmer, only matters in interruption mode
        else if (this.numInterrupted>0) {
            if (g == this.sequence[this.position - back +this.numInterrupted]) {
                consoleArea.displayMessage(":)", "systemmess", "greenline");
                this.correct++;
            }
        }
        
        //.. if wrong
        else {
            consoleArea.displayMessage(":(", "systemmess", "redline");
            
            //.. if we want to start over when we make a mistake
            if (INTERRUPT){
                $("#consoleInput").val("interruptnback(" +INTERRUPTPAUSE+")");
                javaInterface.postToConsole();
                this.deactivate();
                setTimeout(start, INTERRUPTPAUSE+1000); //.. basically impossible to set this perfectly
                this.numInterrupted++;
            }
            this.wrong++;
        }
        this.position++;
    }
    
    //.. turn off any interval
    this.deactivate = function() {
        if (this.guesses.length >0){
            var total = this.correct + this.wrong;
            var accuracy = this.correct / total;
            consoleArea.displayMessage("Accuracy: " +accuracy, "systemmess", "blueline");
            
            //.. save the accuracy in this trial to the backend
            var label = "zero";
            if (accuracy > 0.99) label ="hundred";
            if (accuracy > 0.75) label = "eighty";
            if (accuracy > 0.5)label = "sixty";
            else label = "lessThanFifty";

            //.. ship it off
            var mess = "retrolabel(accuracy" + self.num + ",condition," + label + ",1,realtime1)";
            $("#consoleInput").val(mess);
            javaInterface.postToConsole();
        }
        this.active = false;
        clearInterval(this.inter);
    }
    
    
    if (this.active){
        //.. with some delay, say the first one
        setTimeout(start, DELAY);  
    }
    
    function start() {
        self.active = true;
        if (this.done) return;
        update();
        self.inter = setInterval(update, INTERVAL);
    }
    function update() {
       // console.log(self.sequence[self.predictedPosition]);
        self.predictedPosition++;
    }
     
    
}        
        
