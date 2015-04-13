/**
 *The Main JS file. When a new page is loaded, or when a new file is added, this
 *file is evoked. The function init() is always called. The function reinit() is
 *called everytime we load a new datalayer: it is where we set the bulk of our listeners
 ***/

var consoleArea = new ConsoleArea();
var descriptionArea = new DescriptionArea("#farBottomRight");
var chartArea = new ChartArea("#topRight", descriptionArea);
var javaInterface = new JavaInterface();
var channelSurface = new ChannelSurface();
var plumb = new Plumb();
var datalayerArea = new DatalayerArea("#content");
var plumbTechniques = new PlumbTechniques();
var address = "http://localhost:8080/SensorMining/";/*"http://sensormining.herokuapp.com/";*/ //.. the address of the back-end

/* When document is loaded, do init, ie set listeners etc. 
 */
$( init );
function init() { 
    //.. Focus on input, so that it is the default location for cursor
//    $("input").focus();
    
    //.. Tell consoleArea to display a welcome message. load a file 
    consoleArea.introduce();
    
    //.. Retrieve any data layers that might still be loaded in the system
    javaInterface.postToDataLayers();
    
    //.. get commands
    $("#consoleInput").val("getcommandsnodisplay");
    javaInterface.postToConsole();
    
    //.. Initialize our somewhat broken plumb library (this is what we used before d3 to draw lines)
    plumb.loadPlumb();

    //.. When we press enter send a message back to the server
    $(document).keypress(function(e) {
        if(e.which == 13) {
            var userText = $("#userinput").val();
            consoleArea.parseUserMessage(userText);
        }       
    });
    
  
    
    //..Wow, that was really challening figuring out that reinit kinda spawns two threads... 
    //... Be careful with that Sam. I think its the reason why double clicking calls the server twice
    $("body").keydown(function(e) { 
          //.. tab means search in console
        if (e.which == 9) {
            e.preventDefault();
            var target = consoleArea.search($("#userinput").val());
            if (target != null) 
                $("#userinput").val(target);           
        }
        if (e.which == 38) {
            e.preventDefault();
            var target = consoleArea.getLastUp();
            if (target != null)
                $("#userinput").val(target);
        }
        if (e.which == 40) {
            e.preventDefault();
            var target = consoleArea.getLastDown();
            if (target != null)
                $("#userinput").val(target);
        }
    });
    //.. Add startswith operation to String
    if (typeof String.prototype.startsWith != 'function') {
        // see below for better implementation!
        String.prototype.startsWith = function (str){
            return this.indexOf(str) == 0;
        };
    }
    
    var trZoomed = false;

    $("#topRight").dblclick(function(e) {
        if (!(trZoomed)) {
            $("#topRight").removeClass("trunzoomed");
            $("#topRight").addClass("trzoomed");
            trZoomed = true;
        }
        else {
            $("#topRight").addClass("trunzoomed");
            $("#topRight").removeClass("trzoomed");
            trZoomed = false;
        }
        chartArea.displayChart(chartArea.lastJSON);
    });
  
  /*
   document.getElementById('asynchFile').addEventListener('change', readFile, false);

   function readFile (evt) {
       var files = evt.target.files;
       var file = files[0];           
       var reader = new FileReader();
      
       reader.onload = function() {
         consoleArea.passFile(file.name, this.result);
       }
       reader.readAsText(file);
    }
    //.. No longer in huse
    */ 
}

/*Functions that must be loaded at start but also
 *reinitalized when we reload datalayers*/
function reinit() {
  /** dropchannel: this is the class for datalayers. As soon as our mouse enters
    *it, we want to change "givers" val (again an old name that no longer makes sense)
    * so that the backend knows what is currently being pressed
    **/
   
}

function datalayerInit() {
    $('.datalayer').mouseenter(function(e) {
        $('#giver').val(e.currentTarget.id); //.. e.target.id gives you a bug sometimes 
    });

    //.. for multi-selection: set shiftpressed to true
    $("body").keydown(function(e) {
        if (e.which == 16)
            shiftKey = true;
        if (e.which == 18) //.. alt
            altKey = true;

    });

    //.. and set it back up if we are releasing the key
    $("body").keyup(function(e) {
        if (e.which == 16)
            shiftKey = false;
        if (e.which == 18)
            altKey = false;
    });

    var shiftKey = false; //.. for multi-selection
    var altKey = false;

    //.. if a channel set is clicked
    $(".datalayer").mousedown(function(e) {
        if (!shiftKey)
            datalayerArea.datalayers.selectLayer(e.currentTarget.id);
        else
            datalayerArea.datalayers.multiSelectLayer(e.currentTarget.id)
        javaInterface.getDataLayerStats();
    });

    //.. if a channel set is dbl-clicked
    $(".datalayer").dblclick(function(e) { //.. change to .experiment
        datalayerArea.datalayers.selectLayer(e.currentTarget.id);
        if (altKey)
            javaInterface.postToDataLayer("frequency");
        else if (altKey)
            javaInterface.postToDataLayer("correlation");
        else
            javaInterface.postToDataLayer();
    });

    //.. When I release a datalayer, show what techniques I intersect
    $(".datalayer").mouseup(function(e) {
        datalayerArea.highlightIntersectedTechniques();
        datalayerArea.datalayers.unselectAll();
        datalayerArea.boundsCheck();
    });
    
   
   
}

function techniqueInit() { 
    //.. if a technique is click (do essentially same as if channel is
    $(".technique").dblclick(function(e) {
        javaInterface.getTechniqueStats();
    });

    //.. if a technique is click (do essentially same as if channel is
    $(".technique").mousedown(function(e) {
        datalayerArea.techniques.selectTechnique(e.currentTarget.id);
        $("#technique").val(e.currentTarget.id);
    });
    
     //.. When I release a datalayer, show what techniques I intersect
    $(".technique").mouseup(function(e) {
        datalayerArea.highlightIntersectedTechniques();
        datalayerArea.datalayers.unselectAll();
        datalayerArea.boundsCheck();
    });
    
    
}
