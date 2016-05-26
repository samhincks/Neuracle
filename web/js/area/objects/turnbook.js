 /* A class for displaying a readible book in the console, synchronized with parallel audio
 * Adapted code by Sam Hincks
 */
function Turnbook() {
    var lags = []; //.. stored user data, lags between conditions 
    var initiated = false; //.. set to true one's we've called the init function
    var turns =0; //.. how many times the user has turned the page
    var pagesToClassification = 2; //.. when to start storign data (when we have enough to know standard deviations
    var wells; //.. the current book
    var start =0; //.. number of millisecond since 1970
    var double = true; //.. set to true if we use both pages
    var audioPage; //.. set to page where audio is at
    var busy = false;
    
    //.. When commmand is called, partitition into pages
    this.init = function() {
        //.. Add HTML element
        $("#topRight").append("<div class='book' id='book'>")
        $("#book").append("<div class='hard'>H.G Wells, The Food of the Gods</div>");        
        initiated = true;
        
        //.. copy pasted code to initialize book
        var module = {
            ratio: 1.38,
            init: function (id) {
                var me = this;

                // if older browser then don't run javascript
                if (document.addEventListener) {
                    this.el = document.getElementById(id);
                    this.resize();
                    this.plugins();

                    // on window resize, update the plugin size
                    window.addEventListener('resize', function (e) {
                        var size = me.resize();
                        $(me.el).turn('size', size.width, size.height);
                    });
                }
            },
            resize: function () {
                // reset the width and height to the css defaults
                this.el.style.width = '';
                this.el.style.height = '';

                var width = this.el.clientWidth,
                    height = Math.round(width / this.ratio),
                    padded = Math.round(document.body.clientHeight * 0.9);

                // if the height is too big for the window, constrain it
                if (height > padded) {
                    height = padded;
                    width = Math.round(height * this.ratio);
                }

                // set the width and height matching the aspect ratio
                this.el.style.width = width + 'px';
                this.el.style.height = height + 'px';

                return {
                    width: width,
                    height: height
                };
            },
            plugins: function () {
                // run the plugin
                $(this.el).turn({
                    gradients: true,
                    acceleration: true
                });
                // hide the body overflow
                document.body.className = 'hide-overflow';
            }
        };
        module.init('book');
       
        //.. Add the book Wells
        wells = new Wells(double);
        
        if (double){
            for (var i =0; i < wells.pages.length; i++) {
                var text = wells.pages[i];
                var element = $("<div />").html(text);
                $("#book").turn("addPage", element, i + 2); //. first is 2, then 3
            }
        }
        
        //.. we leave every other page blank
        else{
            for (var i =0; i < wells.pages.length; i++) {
                var text = wells.pages[i];
                var element = $("<div />").html(text);
                var pageNum = (i *2)+2; //.. first is 2, then 3 is blank, then 4 is a page
                $("#book").turn("addPage", element, pageNum );
                element = $("<div />").html("");
                $("#book").turn("addPage", element, pageNum +1 );

                
            }
        }
    }
        
    
    //.. Return the current page
    function getCurrentPage() {
       return $("#book").turn("page");
    }
    
    
    /** Return true if the user is not ahead of the audio**/
    function notAhead() {
        if (getCurrentPage() >= audioPage) return false;
        return true;
    }
    
    /** When the page is turned, turn page and send message to back end, as well 
     * as display lag**/
    $("body").keydown(function(e) {
        if (initiated ) {
             if (e.which == 39) {
                if (getCurrentPage() == 1) {
                    $("#book").turn("next");
                    initAudio();
                    audioPage =1;
                    return;
                }
                    
                else if (notAhead() && !busy) {
                    busy = true;
                    $("#book").turn("next");
                    busy = false;
                }
                else {
                    console.log("ahead");
                    return;
                }
             }
            
            initiated = true;
            //.. estimate lag between pages
            var lag = Math.abs(getLag());
            console.log("changed to " + getCurrentPage() + " with lag " + lag);
            lags.push(lag);
            
            //.. increment turns, but don't do it if they are spamming... 
            turns++;
            
            //.. post to console, making clear we changed page 
            var condition = "none"+turns; 
            var message = "label(realtime1,page,"+condition+")";
            $("#consoleInput").val(message);
            javaInterface.postToConsole();
            
            //.. Lags contains every lag computed so far. How large is this one relative to the others.
            var avg = ss.mean(lags);
            var dev = ss.standardDeviation(lags);
            var diff = lag - avg;
            var deviationsAway = diff / dev;
            
            //.. begin to classify where they are in reading exercise
            if (turns > pagesToClassification) {
                 //.. if deviations away > 1, then -> 
                setTimeout(function () { 
                    var lagState = "medium";
                    if (deviationsAway > 1) lagState = "slow";
                    if (deviationsAway < -1) lagState = "fast";
                        
                    var mess = "retrolabel(lag,page,"+lagState+",0,realtime1)";
                    $("#consoleInput").val(mess);
                    javaInterface.postToConsole();
                }, 2000);
            }
        }
    });
    
    
    /**Return the number of seconds that should have elapsed when a page is turned**/
    function getTime(pageIndex) {
        var p = Math.floor(pageIndex / 2 -1);  //.. +2 since its 2 on ech page and we start with cove
        return wells.times[p];
        
        
    }

    /** Return the lag between where we are and where we should be**/
    function getLag () {
        //.. when we should have arrived at a page 
        correctTime = getTime(getCurrentPage());
        var elapsed = getElapsed();
        return  correctTime - elapsed;
    }
    
    /** Return the relevant page for the given time **/
    function getPage(time) {
        var page = wells.times.indexOf(time) ;
        if (page == -1) return -1;
        return page *2 +2;        
    }
    
    /** If we are ~lag seconds after when a page should have been turned, 
     *  then turn the page **/
    var synchronizeIfBehind = function(lag) {
        var elapsed = getElapsed();
        var cur = getPage(elapsed-lag);
        if (cur != -1){ 
            $("#book").turn("page", cur);
        }
        
        //.. also set the page which we should be at for the calculation of lags
        cur = getPage(elapsed);
        if (cur != -1) {
            audioPage = cur;
            console.log(audioPage);
        }
    }
    
    //.. initialize audio, and set it to synchronize with the page at specific lag
    var initAudio = function(){
         start = new Date().getTime() / 1000;
         document.getElementById("hgwellsaudio").play();
        
         setInterval(function () {
            synchronizeIfBehind(10);
         }, 1000);
    }
    
    //.. Return number of seconds that have passed since the start
    var getElapsed = function( ) {
        var curTime =new Date().getTime() / 1000;
        var elapsed = Math.floor(curTime - start);
        return elapsed;
    }
}


    
   
