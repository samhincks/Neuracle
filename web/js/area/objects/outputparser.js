function OutputParser() {
    
    this.parseOutput = function(content) {
        if (content.startsWith("probabilities:")) {
            var probs = (content.split(":")[1]).split(",");
            for (var i in probs) {
                var s = probs[i].split("%");
                var slope = (parseFloat(s[0]) *1000000) + "";
                
                slope = slope.substr(0,4);
                var prob = parseFloat(s[1]) +"";
                prob = prob.substr(0,4);
                console.log(s, slope, prob)

                if (prob < 0.33) {
                    var name = i %4;
                    if (prob < 0.01) name +="C";
                    else if (prob < 0.05) name +="B";
                    else  name +="A";
                   // document.getElementById(name).play();
                    if (slope > 0)
                        consoleArea.displayMessage(i + ":" +slope+","+ prob, "systemmess", "greenline");
                    else
                        consoleArea.displayMessage(i + ":" + slope + "," + prob, "systemmess", "blueline");
                }
            }
            consoleArea.displayMessage("")
        }
        
        ///.. start a new condition, so deactivate any running nback evaluator
        if (content.startsWith("Starting")) {
            nbackEvaluator.deactivate();
            document.getElementById("0A").play();
            console.log(content);
            if(content.startsWith("Starting: junk")) {
               //  $("#consoleInput").val("selfcalibrate2");
               // javaInterface.postToConsole();
            }
        }
        
        //.. initializing a new label, play sound etc.
        if (content.startsWith("Initializing")) {
            document.getElementById("0A").play();
            
            //.. Not only starting a new label, but its the nback
            if (content.startsWith("Initializing nback")) {
                var seqNum = content.split("-")[1];
                var condition = content.split("Starting:")[1].trim(); //.. extract the condition
                nbackEvaluator = new Evaluator(seqNum, condition);
            }

        }
       
        consoleArea.displayMessage(content, "systemmess", "blueline");
    }
     
    
}        
