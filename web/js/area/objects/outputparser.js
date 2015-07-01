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
         
        if (content.startsWith("Current")) {
            document.getElementById("0A").play();
        }
        consoleArea.displayMessage(content, "systemmess", "blueline");
    }
     
    
}        
