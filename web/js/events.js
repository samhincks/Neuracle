/**
 *The Main JS file. When a new page is loaded, or when a new file is added, this
 *file is evoked. The function init() is always called. The function reinit() is
 *called everytime we load a new datalayer: it is where we set the bulk of our listeners
 ***/
var nbackEvaluator = new Evaluator(-1);
var consoleArea = new ConsoleArea();
var chartArea = new ChartArea("#topRight");
var javaInterface = new JavaInterface();
var plumbTechniques = new PlumbTechniques();
var plumb = new Plumb();
var datalayerArea = new DatalayerArea("#content");
var outputParser = new OutputParser();
var address =  "http://localhost:8080/Neuracle/";
var nback = new NBack();


//var address = "http://sensormining.herokuapp.com/"; //.. the address of the back-end
/* When document is loaded, do init, ie set listeners etc. 
 */
$( init );
function init() { 
    console.log("intitalizes!")
    //nback.begin(30000);
    //.. Focus on input, so that it is the default location for cursor
    //    $("input").focus();
    
    //.. Tell consoleArea to display a welcome me ssage. load a file 
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
}


//.. 
function datalayerInit(datalayer) {
    if (arguments.length == 0) datalayer =".datalayer";
    $(datalayer).mouseenter(function(e) {
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

    var altKey = false;
    var shiftKey = false; //.. for multi-selection
    //.. if a channel set is clicked
    $(datalayer).mousedown(function(e) {
        /// $(".ui-tooltip").tooltip('close'); // doesnt work 
        if (!shiftKey)
            datalayerArea.datalayers.selectLayer(e.currentTarget.id);
        else
            datalayerArea.datalayers.multiSelectLayer(e.currentTarget.id)
        javaInterface.getDataLayerStats(); //.. I'm not happy with this, but for now we tell the back end who's being selected each time we select a layer
    });

    //.. if a channel set is dbl-clicked
    $(datalayer).dblclick(function(e) { //.. change to .experiment
        datalayerArea.datalayers.selectLayer(e.currentTarget.id);
        if (altKey) javaInterface.postToDataLayer("debug");
        else javaInterface.postToDataLayer();
    });

    //.. When I release a datalayer, show what techniques I intersect
    $(datalayer).mouseup(function(e) {
        datalayerArea.highlightIntersectedTechniques();
        datalayerArea.datalayers.unselectAll();
        datalayerArea.boundsCheck();
    });
}


function individualTechniqueInit(id, type) {
    //.. if a technique is click (do essentially same as if channel is
    $("#"+id).dblclick(function(e) {
        javaInterface.getTechniqueStats();
    });

    //.. if a technique is clicked (do essentially same as if channel is
    $("#"+id).mousedown(function(e) {
        datalayerArea.techniques.selectTechnique(e.currentTarget.id);
        $("#technique").val(e.currentTarget.id);
    });

    //.. When I release a datalayer, show what techniques I intersect
    $("#"+id).mouseup(function(e) {
        datalayerArea.highlightIntersectedTechniques();
        datalayerArea.datalayers.unselectAll();
        datalayerArea.boundsCheck();
    });
    
    $("#"+id).attr("title", type + ": " + id);
    $("#"+id).tooltip();
}

