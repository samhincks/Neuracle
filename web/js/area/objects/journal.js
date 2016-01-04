function Journal() {
    var curText = "";
   
    //.. save start time for synchronization
    var start;
    
    var queryInterval = 1000;
    
    var self = this;
    this.updateText = function(newText) {
        //.. first text received: start a timer
        if(curText.length == 0) {
            var d = new Date();
            start = d.getTime();   
        }
        
        curText += newText;
        
        //.. save time in array, and as an id for the character 
        var elapsed = this.getTimeInTen();
        var id= "ch" + elapsed;
        
        //.. append new text
        var d = $("<bdo class = '"+id+"'></bdo>").text(newText);
        
        //d.addClass("systemmess");
        $("#journalblock").append(d);
    }
    
    this.getTimeInTen = function() {
        var d = new Date();
        var n = d.getTime();
        var elapsed = n - start;
        var nearestTen = Math.round(elapsed / 10000);
        return nearestTen;
    }
    
    this.colorText = function () {
        //.. get value of interest from var slope = classifier.getSlope(classifier.channel);
        var slope1 = classifier.getSlope(classifier.channel2);
        var slope1val = Math.round(slope1[0] * 100) / 100;
        var slope1dev = Math.round(slope1[1] * 100) / 100;

        var slope2 = classifier.getSlope(classifier.channel2);
        var slope2val = Math.round(slope2[0] * 100) / 100;
        var slope2dev = Math.round(slope2[1] * 100) / 100;

        var correlation = classifier.getCorrelationKBack(classifier.channel, classifier.channel2);
        var corrval = Math.round(correlation[0] * 100) / 100;
        var corrdev = Math.round(correlation[1] * 100) / 100; 
        
        var elapsed = this.getTimeInTen();
        var curClass = ".ch" + elapsed;

        var scale = d3.scale.linear().domain([-4,4]).range([-128, 128]); //.. saying we can have a max standard deviation of 4
        //
        //.. heres where the magic comes in, we're going to dynamically create a color out of these three values
        //.. dlPFC - > add blue
        var highlight = false;
        var red = 128;
        var green = 128;
        var blue = 128; 

        if (Math.abs(slope1dev) > 0.5) { //.. increase or reduce the red
            red+= scale(slope1dev);
            highlight = true;
        }
        
        if (Math.abs(slope2dev) > 0.5) { //.. increase or reduce the blue
            blue+=scale(slope2dev);
            highlight = true;
        }
        
        var color = "rgb(" +red + "," +green +"," +blue+")";
        if(highlight)
            $(curClass).css("background-color",color);
    }
    
    //.. place journal div inside usermessage
    this.initialize = function() {
        //.. create new console like div
        $("#usrmessage").append("<div id = journal> </div>");
        $("#journal").append("<div id = journalwriting></div>");
        $("#journalwriting").append("<div id = journalblock> </div>")
        $("#journalwriting").append("<textarea id = journalinput> </textarea>")
        
        //.. create new listener which adds whatever is entered to the permanent, non-erasable writing store
        $("#journalinput").keydown(function (d) {
            var text = $("#journalinput").val();
            self.updateText(text);
            $("#journalinput").val(""); //.. clear the input
            
        });
        
        //.. query the state every ten seconds
        this.querySlope = setInterval(function () {
            self.colorText();
        }, queryInterval);
    }
    
    /**Return a fat, manipulable div, packed with bdos that have an id set, to location + id**/
    this.getCharacters = function (message, index, strikeThrough) {
        var d = $("<div></div>");
        var bdo;
        if (!strikeThrough)
            bdo = $("<bdo></bdo>").text(" ");
        else
            bdo = $("<del></del>").text(" ");

        //.. Iterate through the characters of message
        for (var i = 0; i < message.length; i++) {
            var ch = message[i];
            var bdo2;
            if (!strikeThrough)
                bdo2 = $("<bdo></bdo>").text(ch);
            else
                bdo2 = $("<del></del>").text(ch);

            bdo2.addClass(id + "-" + index + "-" + i + strikeThrough);
            bdo.append(bdo2);
        }

        var lineId = id + "-" + index + "-" + strikeThrough;
        d.addClass(lineId);
        d.append(bdo);
        return d;
    }
}