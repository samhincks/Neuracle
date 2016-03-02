
// Define letters audio
function NBack(){
    $("#nback").hide();
    $("#nhead").hide();
    $('#begin').hide();
    
    var dualMode = false; //.. if dualMode is true, then left is audio. Otherwise left is yes

    var letb = new Howl({
            urls: ['audio/b.mp3', 'audio/b.ogg', 'audio/b.wav']
    });

    var letf = new Howl({
            urls: ['audio/f.mp3', 'audio/f.ogg', 'audio/f.wav']
    });

    var letk = new Howl({
            urls: ['audio/k.mp3', 'audio/k.ogg', 'audio/k.wav']
    });

    var letn = new Howl({
            urls: ['audio/n.mp3', 'audio/n.ogg', 'audio/n.wav']
    });

    var letp = new Howl({
            urls: ['audio/p.mp3', 'audio/p.ogg', 'audio/p.wav']
    });

    var letq = new Howl({
            urls: ['audio/q.mp3', 'audio/q.ogg', 'audio/q.wav']
    });

    var letr = new Howl({
            urls: ['audio/r.mp3', 'audio/r.ogg', 'audio/r.wav']
    });

    var lett = new Howl({
            urls: ['audio/t.mp3', 'audio/t.ogg', 'audio/t.wav']
    });


    // EVALUATE BLOCK FUNCTION

    function evaluateBlock(block) {
            var vTargCount = 0;
            var aTargCount = 0;
            for(var i=0; i<block.length; i++) {
                    if(block[i - n]) {
                            if(block[i][0] == block[i - n][0]) {
                                    vTargCount += 1;
                            }
                            if(block[i][1] == block[i - n][1]) {
                                    aTargCount += 1;
                            }
                    }
            }
            return [vTargCount, aTargCount];
    }

    // Function to light up specified square


    var sqrMaker = function(randSqr,toToggle) {
            switch(randSqr) {
                    case 1:
                            $('#uno').toggleClass(toToggle);
                            setTimeout(function(){$('#uno').toggleClass(toToggle)}, 500);
                            break;
                    case 2:
                            $('#dos').toggleClass(toToggle);
                            setTimeout(function(){$('#dos').toggleClass(toToggle)}, 500);
                            break;
                    case 3:
                            $('#tres').toggleClass(toToggle);
                            setTimeout(function(){$('#tres').toggleClass(toToggle)}, 500);
                            break;
                    case 4:
                            $('#cuatro').toggleClass(toToggle);
                            setTimeout(function(){$('#cuatro').toggleClass(toToggle)}, 500);
                            break;
                    case 5:
                            $('#seis').toggleClass(toToggle);
                            setTimeout(function(){$('#seis').toggleClass(toToggle)}, 500);
                            break;
                    case 6:
                            $('#siete').toggleClass(toToggle);
                            setTimeout(function(){$('#siete').toggleClass(toToggle)}, 500);
                            break;
                    case 7:
                            $('#ocho').toggleClass(toToggle);
                            setTimeout(function(){$('#ocho').toggleClass(toToggle)}, 500);
                            break;
                    case 8:
                            $('#nueve').toggleClass(toToggle);
                            setTimeout(function(){$('#nueve').toggleClass(toToggle)}, 500);
                            break;
            }
    };

    // Function to trigger specified consonant

    var letters = function(randLet) {
            switch(randLet) {
                    case 1:
                            letb.play();
                            break;
                    case 2:
                            letf.play();
                            break;
                    case 3:
                            letk.play();
                            break;
                    case 4:
                            letn.play();
                            break;
                    case 5:
                            letp.play();
                            break;
                    case 6:
                            letq.play();
                            break;
                    case 7:
                            letr.play();
                            break;
                    case 8:
                            lett.play();
                            break;
            }
    };

    // Global variable for user score

    var visualScores = [0, 0, 0, 0]; 
    var audioScores = [0, 0, 0, 0]; 
    var reactionTimes = [[],[]];
    var start = 0; //.. for timing
    function prepareBlockSam(numBlocks) {
        var thisBlock = [];
        //.. populate with random values
        for(var i = 0; i < numBlocks; i++) { //.. [visual][audio]
            thisBlock.push([Math.floor(Math.random() * 8)+1, Math.floor(Math.random() * 8)+1]);
        }
        
        thisBlock = forceMatches(numBlocks,thisBlock,0);    
        thisBlock = forceMatches(numBlocks, thisBlock,1);
        return thisBlock;
    }
    function forceMatches(numBlocks, thisBlock, index) {
        //.. then at ~40% of locations, selected randomly make so that visual match
        
        var numMatches = Math.floor((numBlocks) * PCTMATCHING);
        var matching =[];
        for (var i =0; i < numMatches; i++) {
            matching.push(Math.floor(Math.random()*numMatches));
        }
       
        for (var i=0; i < matching.length; i++) {
            var suggestedMatch = matching[i];
            if (suggestedMatch + n <  thisBlock.length)
                thisBlock[suggestedMatch + n][index] = thisBlock[suggestedMatch][index];
        }
        return thisBlock;
    }

    function playBlock(numBlocks, audio, visual) {
            reactionTimes = [[],[]];
            visualScores = [0, 0, 0, 0];
            audioScores = [0, 0, 0, 0];
            reactionTimes = [[], []];
             start = 0; //.. for timing
            var currentBlock = prepareBlockSam(numBlocks);
            var blockEval = evaluateBlock(currentBlock);
            var numMatches = Math.floor((numBlocks) * PCTMATCHING);

            var index =0;
            while(blockEval[0] != numMatches && blockEval[1] != numMatches) { 
                currentBlock = prepareBlockSam(numBlocks);
                blockEval = evaluateBlock(currentBlock);
                index++;
                if (index > 2) break;
            }
            
            var blockCounter = -1;
            var thisBlockLength = currentBlock.length;
            var hitsThisValue = [0, 0];
            playValue();
            var justPressed = false;

            
            function playValue() {
                    $("#nback").css("border-style", "solid");
                    $("#nback").css("border-color", "black");
                    $("#nback").css("border-width", "5px");
                    
                    //.. if the user didn't press and its not the first, then see if this was 
                    if (justPressed == false && blockCounter > n) {
                        var position = blockCounter;
                        var checkPosition = blockCounter-n;
                        if (visual){
                            if (currentBlock[position][0] == currentBlock[checkPosition][0]) {
                                $("#nback").css("border-style", "dashed");
                                visualScores[3] += 1; 
                            }

                            //.. right
                            else {
                                if (dualMode)
                                    visualScores[2] += 1; 
                                else  {
                                    //$("#nback").css("border-color", "red");
                                    $("#nback").css("border-style", "dashed");

                                    visualScores[3] += 1; 
                                }
                            }
                        }
                        if (audio) {
                            if (currentBlock[position][0] == currentBlock[checkPosition-1][0]) {
                                audioScores[3] += 1; 
                            }

                            //.. right
                            else {
                                audioScores[2] += 1; 
                            }                                
                        }
                   }
                    justPressed = false;
                    start = Date.now();
                    $('html').on('keydown', function(event) {
                        //.. in a dual nback left is audio, right is visual
                        if (dualMode) {
                            if(event.which == 37) {
                                hitsThisValue[0] = 1;
                                if (visual &&!(justPressed)) {
                                     justPressed = true;
                                     checkAccuracy("visual");
                                 }

                            } else if( audio && event.which == 39) {
                                hitsThisValue[1] = 1;
                                if (!(justPressed)){
                                    justPressed = true;
                                    checkAccuracy("audio");
                                }

                            }
                        }
                        else { //. otherwise audio 
                             if(event.which == 37) { //.. yes
                                hitsThisValue[0] = 1;
                                if (visual &&!(justPressed)) {
                                     justPressed = true;
                                     checkAccuracyTDCS("yes");
                                 }

                            }
                            else if(event.which == 39) {
                                hitsThisValue[1] = 1;
                                if (!(justPressed)){
                                    justPressed = true;
                                    checkAccuracyTDCS("no");
                                }

                            }
                        }
                    });
                    
                   if(++blockCounter < thisBlockLength) {
                           if(currentBlock[blockCounter]) {
                               if (visual) sqrMaker(currentBlock[blockCounter][0],'on');
                               if (audio) letters(currentBlock[blockCounter][1]);
                           }
                           setTimeout(playValue, 3000);
                           hitsThisValue = [0, 0];
                   }
                    //.. user is done
                    else {
                        if (visual) {
                            consoleArea.displayMessage("Visual scores: ")
                            var corr = pctCorrect(visualScores);
                            var ave = (corr[0] / corr[1]);
                            if (corr[1] == 0) ave = 0;

                            consoleArea.displayMessage("    Correct: " +  corr[0] +
                                    " / " + corr[1] + " = " + (corr[0] / corr[1])); 
                            var reactAve = getAverage(reactionTimes[0]);
                            
                            consoleArea.displayMessage("    Average reaction time = " +reactAve); 
                            setTimeout(function () { // streamlabel(visual-1,10%1%1)
                                var mess = "retrolabel(corr,condition," + getThreshold(ave) + ",0,realtime1)";

                                $("#consoleInput").val(mess);
                                javaInterface.postToConsole(); 
                                
                            }, 5000);
                            
                            setTimeout(function () { // streamlabel(visual-1,10%1%1)
                                var mess = "retrolabel(reaction,condition," + getNum(reactAve) + ",0,realtime1)";

                                $("#consoleInput").val(mess);
                                javaInterface.postToConsole();

                            }, 5000);
                            
                        }
                        if (audio) {
                            consoleArea.displayMessage("Audio scores: ")
                            var corr = pctCorrect(audioScores);
                            consoleArea.displayMessage("    Correct: " + corr[0] +
                                    " / " + corr[1] + " = " + (corr[0] / corr[1]));
                            consoleArea.displayMessage("    Average reaction time = " + getAverage(reactionTimes[1])); 
                        }
                        visualScores = [];
                        audioScores = [];
                        reactionTimes = [];
                    }
                }
                
                //.. given a number return a string
                function getThreshold(num) {
                    if (num <=1) {
                        var nums = ["zero","ten", "twenty", "thirty", "forty","fifty","sixty","seventy","eighty","ninety","hundred"];
                        return nums[Math.floor(num*10)];
                    }
                }
                function getNum(num) {
                    numStr = Math.floor(num) +"";
                    var nums = ["zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "hundred"];
                    var retStr = "";
                    for (var i =0; i < numStr.length; i++) {
                        retStr +=nums[numStr[i]]+"-"
                    }
                    return retStr;
                }
                //.. set as wrong if 
                function checkAccuracyTDCS(ans) {
                    $("#nback").css("border-style", "solid");
                    var elapsed = Date.now() - start;
                    var position = blockCounter;
                    var checkPosition = blockCounter - n;
                    if (checkPosition < 0)
                        return;
                    reactionTimes[0].push(elapsed);
                    
                    //.. answer = yes,
                    if (ans =="yes") {
                        if (currentBlock[position][0] == currentBlock[checkPosition][0]) {
                            $("#nback").css("border-color", "green");
                            visualScores[0] += 1;
                        }
                        else {
                            $("#nback").css("border-color", "red");
                            visualScores[1] += 1;
                        }
                    }
                    
                    //.. answer = no;
                    if (ans == "no") {
                        if (currentBlock[position][0] == currentBlock[checkPosition][0]) {
                            $("#nback").css("border-color", "red");
                            visualScores[1] += 1;
                        }
                        else {
                            $("#nback").css("border-color", "green");
                            visualScores[0] += 1;
                        }
                    }
                }

                function checkAccuracy(hit) {
                        $("#nback").css("border-style", "solid");
                        var elapsed = Date.now() -start;
                        var position = blockCounter;
                        var checkPosition = blockCounter -n;
                        if (checkPosition < 0) return;
                        if (hit == "visual") {
                            reactionTimes[0].push(elapsed);
                            if (currentBlock[position][0] == currentBlock[checkPosition][0]) {
                                $("#nback").css("border-color", "green");
                                visualScores[0] += 1;
                            }
                            else {
                                $("#nback").css("border-color", "red");
                                visualScores[1] += 1;
                            }
                        }
                        if (hit == "audio") {
                            reactionTimes[1].push(elapsed);
                            if (currentBlock[position][1] == currentBlock[checkPosition][1]) {
                                $("#nback").css("border-width", "10px");
                                audioScores[0] +=1;
                            }
                            else {
                                $("#nback").css("border-width", "1px");
                                audioScores[1] +=1; 
                            }
                        }
                        
                       
                   }
                   
                /** Compute percent correct from following array
                 * true positivies, true negatives, false positives, false negatives
                 * so 0 and 3, contribute to correct
                 * 1 and 2 contribute to false
                 **/
                function pctCorrect(arr) {
                    var total = arr[0] + arr[1] + arr[2] +arr[3];
                    var correct = arr[0] + arr[2];
                    return [correct, total]; 
                }
                function getAverage(arr) {
                    var sum =arr.reduce(add, 0);
                    function add(a, b) {
                        return a + b;
                    }
                    return sum / arr.length;
                }
               }
     
    //.. [viz-true-positives, audio-false-positives
    this.displayScore = function() {
        
    }
    
    // When the button is clicked, run a block

    var blockRunning = false;

    // VALUE OF N

    var n = 1;
    var PCTMATCHING = 0.4;
    this.appendBack = function() {
        
        $("#topRight").append(" <div id='nhead'>" +
                " <h1 id='nvalue'>2</h1>" +
                "<div id='left'>left=yes</div>" +
                "<div id='right'>right=no</div>" +
                "</div>");

        $("#topRight").append("<table  class = 'ntable' id='nback'>" +
                "   <tr>" +
                "<td class = 'ncol'><div id='uno' class='off'></div></td>" +
                "<td class = 'ncol'><div id='dos' class='off'></div></td>" +
                "<td class = 'ncol'><div id='tres' class='off'></div></td>" +
                "</tr>" +
                "<tr>" +
                "   <td class = 'ncol'><div id='cuatro' class='off'></div></td>" +
                "  <td class = 'ncol'><div id='cinco' class='off'><img src='images/plus.png' alt=''></div></td>" +
                " <td class = 'ncol'><div id='seis' class='off'></div></td>" +
                "</tr>" +
                "<tr>" +
                "   <td class = 'ncol'><div id='siete' class='off'></div></td>" +
                "  <td class = 'ncol'><div id='ocho' class='off'></div></td>" +
                " <td class = 'ncol'><div id='nueve' class='off'></div></td>" +
                "</tr>" +
                "</table> ");
    }
    //. streamlabel(visual-3, 10%1%1)
    this.begin = function (time, _n, audio, visual) {
        $("#topRight").empty();
        this.appendBack();
        //$("userinput").blur();

        //$("#nback").focus();
        n = _n;
        $("#nvalue").text("n = " + n);

        var numBlocks =  (time / 3000) - n;
        if (blockRunning === false) {
            playBlock(numBlocks, audio, visual);
        }
        
        console.log("now running: " + numBlocks + "at  n = " + n + " for " + time);
        
        blockRunning = true;
        setTimeout(function () {  
            blockRunning = false;
            $("#nback").hide();
            $("#nhead").hide();
        }, time-3000);
    }
    
}   

// Size the grid to the window on load

document.ready = function () {
    var loadwidth = $(window).height();
    loadwidth *= 0.50;
    $('table').css({'width': loadwidth + 'px'});
    $('table').css({'height': loadwidth + 'px'});
}

// Resize grid if window is resized

window.onresize = function () {
    var dynwidth = $(window).height();
    dynwidth *= 0.50;
    $('table').css({'width': dynwidth + 'px'});
    $('table').css({'height': dynwidth + 'px'});
}

// Show and hide instructions when prompted

$('#info').click(function () {
    $('#instruct').css({'display': 'block'});
});

$('#close').click(function () {
    $('#instruct').css({'display': 'none'});
});
$('#begin').click(function () {
    nback.begin(30000);
    });
$('#resultclose').click(function () {
    $('#resultswindow').css({'display': 'none'});
});
