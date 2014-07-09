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
var address = "http://localhost:8080/SensorMining/"; //.. the address of the back-end

/* When document is loaded, do init, ie set listeners etc. 
 */
$( init );
function init() { 
    //.. Focus on input, so that it is the default location for cursor
    $("input").focus();
    
    //.. Tell consoleArea to display a welcome message. load a file 
    consoleArea.introduce();
    
    //.. Retrieve any data layers that might still be loaded in the system
    javaInterface.postToDataLayers();
    
    //.. Initialize our somewhat broken plumb library (this is what we used before d3 to draw lines)
    plumb.loadPlumb();

    //.. When we press enter send a message back to the server
    $(document).keypress(function(e) {
        if(e.which == 13) {
            var userText = $("#userinput").val();
            consoleArea.parseUserMessage(userText);
        } 
    });
    
    //.. Add startswith operation to String
    if (typeof String.prototype.startsWith != 'function') {
        // see below for better implementation!
        String.prototype.startsWith = function (str){
            return this.indexOf(str) == 0;
        };
    }
  
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
    $('.dropChannel').mouseenter(function(e) {
        $('#giver').val(e.currentTarget.id); //.. e.target.id gives you a bug sometimes 
        
     });

    /** Similarly when we press a technique, we need to know the name of what is being prssed
     ***/
    $('.technique').mousedown(function(e) {
         datalayerArea.techniques.selectTechnique(e.currentTarget.id);
         $("#technique").val(e.currentTarget.id);
     });
     
     //.. for multi-selection: set shiftpressed to true
    $("body").keydown(function (e) { 
        if (e.which == 16)
            shiftPressed =true;
    });  
    
    //.. and set it back up if we are releasing the key
    $("body").keyup(function (e) { 
        if (e.which == 16)
            shiftPressed =false;
    });  
    var shiftPressed = false; //.. for multi-selection
    
     //.. if a channel set is clicked
    $(".dropChannel").mousedown(function(e){
        if(!shiftPressed)
            datalayerArea.datalayers.selectLayer(e.currentTarget.id);
        else
            datalayerArea.datalayers.multiSelectLayer(e.currentTarget.id)
        javaInterface.getDataLayerStats();
    });
    
    //.. if a channel set is dbl-clicked
    $(".dropChannel").dblclick(function (e) { //.. change to .experiment
        datalayerArea.datalayers.selectLayer(e.currentTarget.id);
        javaInterface.postToDataLayer();
    });
    
    //.. if a technique is click (do essentially same as if channel is
    $(".technique").mousedown(function (e) {
        datalayerArea.techniques.selectTechnique(e.currentTarget.id);
        javaInterface.getTechniqueStats();
    });
    
    
    
}
/**THIS USED TO BE IN REINIT() but it was too broken so we have to exclude it*/
  //.. Pep the element, applying kinetic drag
    /*$('.dropChannel').pep({                    
        constrainToParent: true
    });  */

/*
    //.. Make so that buttons are droppable elements
    $('.dropChannel').droppable( {
        drop: channelSurface.handleDropEvent
    }); 
                   // $('.technique').pep({constrainToParent: true });

      */          
    /* DRAG now contained within plumb
    //.. Make so that buttons are draggable elements, set containments, etc
    //.. I think this overrides pep
    $('.dropChannel').draggable({
        cancel:true,
        cursor: 'move'
    }); */ 
