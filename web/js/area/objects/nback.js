
// Define letters audio
function NBack(){
    $("#nback").hide();
    $("#nhead").hide();
    $('#begin').hide();

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


    // PREPARE BLOCK FUNCTION


    function prepareBlock(n, numBlocks) {
            
            // Empty block array

            var thisBlock = [];

            // Populate thisBlock with [0, 0] pairs
            //.. doesn't work with input to repare block function... 
            //.. n cannot be 2
            for(var i = 0; i < n + numBlocks; i++) {
                thisBlock.push([0, 0]);
            }

            // Get the length of the block
            var blockLength = thisBlock.length;

            // Create 4 visual targets in empty spots
            var visuals = 0;
            while(visuals < 4) {
                    var visTarg = Math.floor(Math.random() * blockLength);
                    if(thisBlock[visTarg + n]) {
                            if(thisBlock[visTarg][0] == 0 && thisBlock[visTarg][1] == 0 && thisBlock[visTarg + n][0] == 0 && thisBlock[visTarg + n][1] == 0) {
                                    thisBlock[visTarg][0] = 1 + Math.floor(Math.random() * 8);
                                    thisBlock[visTarg + n][0] = thisBlock[visTarg][0];
                                    visuals++;
                            }
                            else if(thisBlock[visTarg][0] !== 0 && thisBlock[visTarg][1] == 0 && thisBlock[visTarg + n][0] == 0 && thisBlock[visTarg + n][1] == 0) {
                                    thisBlock[visTarg + n][0] = thisBlock[visTarg][0];
                                    visuals++;
                            }
                            else if(thisBlock[visTarg][0] == 0 && thisBlock[visTarg][1] == 0 && thisBlock[visTarg + n][0] !== 0 && thisBlock[visTarg + n][1] == 0) {
                                    thisBlock[visTarg][0] = thisBlock[visTarg + n][0];
                                    visuals++;
                            }
                            else{
                                    continue;
                            }
                    }
                    else {
                            continue;
                            }
            }

            // Create 4 audio targets in empty spots

            var audios = 0;
            audioRuns = 0;
            while(audios < 4) {
                    var audTarg = Math.floor(Math.random() * blockLength);
                    audioRuns++;
                    if(thisBlock[audTarg + n]) {
                            if(thisBlock[audTarg][0] == 0 && thisBlock[audTarg][1] == 0 && thisBlock[audTarg + n][0] == 0 && thisBlock[audTarg + n][1] == 0) {
                                    thisBlock[audTarg][1] = 1 + Math.floor(Math.random() * 8);
                                    thisBlock[audTarg + n][1] = thisBlock[audTarg][1];
                                    audios++;
                            }
                            else if(thisBlock[audTarg][0] == 0 && thisBlock[audTarg][1] !== 0 && thisBlock[audTarg + n][0] == 0 && thisBlock[audTarg + n][1] == 0) {
                                    thisBlock[audTarg + n][1] = thisBlock[audTarg][1];
                                    audios++;
                            }
                            else if(thisBlock[audTarg][0] == 0 && thisBlock[audTarg][1] == 0 && thisBlock[audTarg + n][0] == 0 && thisBlock[audTarg + n][1] !== 0) {
                                    thisBlock[audTarg][1] = thisBlock[audTarg + n][1];
                                    audios++;
                            }
                            else {
                                    if(audioRuns>1000) {
                                            break;
                                    }
                                    else {
                                            continue;
                                    }
                            }
                    }
                    else {
                            continue;
                    }
            }

            // Create 2 dual targets in empty spots
            var doubles = 0;
            var visualRuns = 0;
            while(doubles < 2) {
                    var dualTarg = Math.floor(Math.random() * blockLength);
                    visualRuns++;
                    if(thisBlock[dualTarg + n]) {
                            if(thisBlock[dualTarg][0] == 0 && thisBlock[dualTarg][1] == 0 && thisBlock[dualTarg + n][0] == 0 && thisBlock[dualTarg + n][1] == 0) {
                                    thisBlock[dualTarg][0] = 1 + Math.floor(Math.random() * 8);
                                    thisBlock[dualTarg][1] = 1 + Math.floor(Math.random() * 8);
                                    thisBlock[dualTarg + n] = thisBlock[dualTarg];
                                    doubles++;
                            }
                            else {
                                    if(visualRuns>1000) {
                                            break;
                                    }
                                    else {
                                            continue;
                                    }
                            }
                    }
                    else {
                            continue;
                    }
            }

            // Fill other values with random, non-matching values
            for(var x = 0; x < blockLength; x++) {
                    if(thisBlock[x][0] == 0) {
                            thisBlock[x][0] = 1 + Math.floor(Math.random() * 8);
                            if(thisBlock[x - n] && thisBlock[x][0] === thisBlock [x - n][0] && thisBlock[x] !== thisBlock[x - n]) {
                                    if(thisBlock[x][0] < 8) {
                                            thisBlock[x][0] += 1;
                                    } else {
                                            thisBlock[x][0] -= 1;
                                    }
                            }
                            else if(thisBlock[x + n] && thisBlock[x][0] === thisBlock [x + n][0] && thisBlock[x] !== thisBlock[x + n]) {
                                    if(thisBlock[x][0] < 8) {
                                            thisBlock[x][0] += 1;
                                    } else {
                                            thisBlock[x][0] -= 1;
                                    }
                            }
                    }
                    if(thisBlock[x][1] == 0) {
                            thisBlock[x][1] = 1 + Math.floor(Math.random() * 8);
                            if(thisBlock[x - n] && thisBlock[x][1] === thisBlock [x - n][1] && thisBlock[x] !== thisBlock[x - n]) {
                                    if(thisBlock[x][1] < 8) {
                                            thisBlock[x][1] += 1;
                                    } else {
                                            thisBlock[x][1] -= 1;
                                    }
                            }
                            else if(thisBlock[x + n] && thisBlock[x][1] === thisBlock [x + n][1] && thisBlock[x] !== thisBlock[x + n]) {
                                    if(thisBlock[x][1] < 8) {
                                            thisBlock[x][1] += 1;
                                    } else {
                                            thisBlock[x][1] -= 1;
                                    }
                            }
                    }
            }

            return thisBlock;

    };
    // END PREPARE BLOCK FUNCTION

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

    var userScore = [0, 0, 0, 0]; // Visual correct, audio correct, visual mistakes, audio mistakes

    function prepareBlockSam(numBlocks) {
        var thisBlock = [];
        //.. populate with random values
        for(var i = 0; i < numBlocks; i++) { //.. [visual][audio]
            thisBlock.push([Math.floor(Math.random() * 8), Math.floor(Math.random() * 8)]);
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

    function playBlock(numBlocks) {
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
            function playValue() {
                    $('html').on('keydown', function(event) {
                        if(event.which == 37) {
                            hitsThisValue[0] = 1;
                            if ($("#left").hasClass("blueline")) return;
                            $('#left').toggleClass("blueline");
                            setTimeout(function(){$('#left').toggleClass("blueline")}, 100);
                            
                        } else if(event.which == 39) {
                            hitsThisValue[1] = 1;
                            if ($("#right").hasClass("blueline")) return;
                            $('#right').toggleClass("blueline");
                            setTimeout(function(){$('#right').toggleClass("blueline")}, 100);
                        }
                    });
                    if(++blockCounter < thisBlockLength) {
                            if(blockCounter > n && currentBlock[blockCounter]) {
                                    if(currentBlock[blockCounter - 1][0] == currentBlock[blockCounter - n - 1][0]) {
                                            //console.log('visual n back');
                                            if(hitsThisValue[0] > 0) {
                                                    userScore[0] += 1;
                                                    $('#left').toggleClass("green");
                                                    setTimeout(function(){$('#left').toggleClass("green")}, 100);

                                            }
                                            else {
                                                    userScore[2] += 1;
                                                    $('#left').toggleClass("red");
                                                    setTimeout(function(){$('#left').toggleClass("red")}, 100);

                                            }
                                    }
                                    else {
                                            if(hitsThisValue[0] > 0) {
                                                    userScore[2] += 1;
                                            }
                                    }
                                    if(currentBlock[blockCounter - 1][1] == currentBlock[blockCounter - n - 1][1]) {
                                          //  console.log('audio n back');
                                            if(hitsThisValue[1] > 0) {
                                                    userScore[1] += 1;
                                                    $('#right').toggleClass("green");
                                                    setTimeout(function(){$('#right').toggleClass("green")}, 100);

                                                    

                                            }
                                            else {
                                                    userScore[3] += 1;
                                                    $('#right').toggleClass("red");
                                                    setTimeout(function(){$('#right').toggleClass("red")}, 100);

                                            }
                                    }
                                    else {
                                            if(hitsThisValue[1] > 0) {
                                                    userScore[3] += 1;
                                            }
                                    }
                            }
                            if(currentBlock[blockCounter]) {
                                sqrMaker(currentBlock[blockCounter][0],'on');
                                letters(currentBlock[blockCounter][1]);
                            }
                           // console.log('this block: ' + currentBlock[blockCounter])
                            //console.log('keypresses: ' + hitsThisValue);
                            //console.log('current score: ' + userScore);
                            setTimeout(playValue, 3000);
                            hitsThisValue = [0, 0];
                    }
                    //.. user is done
                    else {
                            consoleArea.displayMessage("You got " + userScore[0] +" of " +numBlocks + " visual cues " + 
                                    " and " + userScore[1]+ "of " + numBlocks +" audio cues");                            
                            userScore = [0, 0, 0, 0];
                    }
            }
    }
    
    // When the button is clicked, run a block

    var blockRunning = false;

    // VALUE OF N

    var n = 1;
    var PCTMATCHING = 0.4;
    
    this.begin = function (time, _n) {
         n = _n;
         $("#nback").show();
         $("#nhead").show();
         $('#begin').show();
         $("#nvalue").text("n = " + n);


         console.log(time);
        var numBlocks =  (time / 3000) - n;
        if (blockRunning === false) {
            console.log("asking for " + numBlocks);
            playBlock(numBlocks);
        }
        blockRunning = true;
        setTimeout(function () {  
            blockRunning = false;
            $("#nback").hide();
            $("#nhead").hide();
            $('#begin').hide();
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
