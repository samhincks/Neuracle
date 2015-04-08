var brown ="#A6611A";
var beige = "#DFC27D";
var teal = "#80CDC1";
var emerald = "#018571";


/**Collection of available data layers*/
function DataLayers() {
    this.dls =  new Array(); //.. all available datalayers
    var selected = new Array(); //.. a single datalayer that is selected (has a blue square around it)

    //.. get this Datalayer by id; return null if it doesn't exist'
    this.getDLById = function(id) {
        for(var i =0; i< this.dls.length; i++) {
            if (this.dls[i].id == id)
                return this.dls[i];
        }
        return null;
    } 
    
    //.. add DataLayer if it does not exist
    this.addDL= function(dl){
        if(!this.getDLById(dl.id)) { //.. if we dont have it
            this.dls[this.dls.length] = dl;
        }
        this.selectLayer(dl.id);
    }
    
    //.. Removes circle unless it intersects
    this.unselectAll = function() {
        for (var i = 0; i < this.dls.length; i++) {
            var id = $("#" + this.dls[i].id)
            if((this.dls[i].intersected) ==0) {//.. remove circle if its not currently intersecting something
                id.css("border-color", "black");
            }
            else if ((this.dls[i].intersected) >=3) {
                id.css("border-color","green");
            }
            else 
                id.css("border-color", "crimson");

                
            
        }
    }
    
    //.. add the selected to class to target element; remove from all others
    this.selectLayer = function(layerId) {
        //.. remove all other 
        for(var i =0; i< this.dls.length; i++) {
            var dl = this.dls[i]; 
            var curId =  dl.id;
             $("#"+curId).removeClass("datalayerSelected")
             $("#"+curId).removeClass("surfaceElementMultiSelected")
             $("#"+curId).removeClass("surfaceElementSelected")
             
             if(layerId == curId) {
                 if (dl.type == "2D") 
                    $("#" + layerId).addClass("surfaceElementSelected");
                 else 
                    $("#" + layerId).addClass("datalayerSelected");
                
             }
        }
        
        $("#giver").val(layerId);
        
        //.. clear selected array and add this single element
        selected = new Array();
        selected.push(layerId);
    }
    
    //..If we are selecting multiple layers. Do not remove existing selections,
    //... Set val as a current givers + new one. So "oldLayer+secondOldest+newestLayer"
    this.multiSelectLayer = function(layerId) {
        $("#"+layerId).addClass("surfaceElementMultiSelected");

        //.. change the color scheme of a the existing selection if tehre is just one
        if (selected.length ==1) {
          $("#"+selected[0]).removeClass("surfaceElementSelected")
          $("#"+selected[0]).addClass("surfaceElementMultiSelected");
        }
        
        if(selected.indexOf(layerId) == -1) 
            selected.push(layerId);
        
        //.. make a String of of all selected data layers which will be sent to java
        var selectionIds =""; 
        for (var i in selected) {
            selectionIds += selected[i];
            if (i != selected.length-1)
                selectionIds += ":" //.. the only illegal character on both windows and mac for a file
        }
        $("#giver").val(selectionIds);
    }
    
}

/*type = 2D/3D
* id = unique key for it
* parent = the id that derived it if any
* numChannels = number of channels
 **/
var globalFreqId;
function DataLayer(jsonDL) {
    this.id = jsonDL.id;
    this.parent =jsonDL.parent;
    this.numChannels = jsonDL.numchannels;
    this.numPoints = jsonDL.numPoints;
    this.type = jsonDL.type;
   
    
    this.freqButton = "freq"+this.id; //.. For some reason it wont respond if I give it a unique id
    this.elementTag = '<div id = "'+this.id+'" class = " dropChannel surfaceElement"> <div title = "F" id = "'+this.freqButton+'"> </div></div>';
    this.sqScale = d3.scale.linear().domain([0, 625000]).range([45, 90]);
    this.intersected = 0; //.. increase if anything is intersected

    /**Display other graphing possibilities inside the container, when we hover over**/
    this.displaySubGraphs = function() {
        $("#"+this.id).attr("title", this.id + " has " + this.numChannels + " channels");
        globalFreqId = $("#" + this.freqButton);
        globalFreqId.attr("title", "F"); //.. this line of code doesnt work

        //.. For a channelset, display frequency and correlation views
        if (this.type =="2D") {
           
            //.. Give it the title attribute, so that the tooltip function applies
            globalFreqId.tooltip({
                content: "F",
                tooltipClass: "freq",
                hide: {duration: 5000},
                position: {my: 'right bottom+15', at: 'left center', collision: 'flipfit'},
                open: function(event, ui) {

                    console.log("opening");
                    $(ui.tooltip).dblclick(function(e) {
                        javaInterface.postToDataLayer("frequency");
                    });
                }
            });
            
            $("#" + this.id).tooltip({
                content: "C",
                tooltipClass: "corr",
                hide: {duration: 5000},
                position: {my: 'left bottom-15', at: 'left center', collision: 'flipfit'},
                open: function(event, ui) {

                    $(ui.tooltip).dblclick(function(e) {
                        javaInterface.postToDataLayer("correlation");
                    });
                    
                    globalFreqId.tooltip('open'); //.. doesnt trigger conventional open
                },
                close: function(event, ui) {
                    globalFreqId.tooltip('close'); //.. doesnt trigger conventional open
                }
            });
           
            
        }
    }
    
    
    
    
   //..  Draw lines inside it as art, and lines connecting
   //... it to elements it may have been derived from
   //. Errors will occur if it has not been appended to the canvas
    this.drawArt = function() {
      /*  //.. draw out the lines inside it
        if (jsonDL.type =="2D") {
           plumb.draw2DLinesWithinBox(this.id, this.numChannels);
        }
        else {//.. if its a 3D datalayer
            try{
               plumb.draw3DLinesWithinBox(this.id,jsonDL.numlabels)
            }
            catch (error) {console.log("Does the id have an illegal character in it?");};
           //.. add an additional class "experiment"
           $("#"+this.id).addClass("experiment");
           //plumbTechniques.setExperimentEPs();
        }*/ 
        if (jsonDL.type == "2D")
            if(this.numChannels <17)
                $("#" + this.id).addClass("chanset");
            else
                $("#" + this.id).addClass("chanset2");
        else { //.. 3D
            if (jsonDL.numlabels ==1) $("#"+this.id).addClass("experiment1");
            if (jsonDL.numlabels ==2) $("#"+this.id).addClass("experiment2");
            if (jsonDL.numlabels ==3) $("#"+this.id).addClass("experiment3");
            if (jsonDL.numlabels ==4) $("#"+this.id).addClass("experiment4");
            else $("#" + this.id).addClass("experiment2");
        }
        var scaledSize = this.sqScale(jsonDL.numpoints);
        $("#" + this.id).width(scaledSize).height(scaledSize /1.7);

        //.. if this datalayer was derived from another layer, draw a line between them 
        if (this.parent != null && this.parent != "Motherless") {
            plumb.setDerivedConnection(this.id, this.parent);
          }
        else {
            jsPlumb.draggable($(".dropChannel"));
        }
    }
   
    
    this.getPosition = function() {
       return $("#"+this.id).offset();
    }
    
    this.getSize = function() {
        return {height : $("#"+this.id).height(), width : $("#"+this.id).width() };
    }
    
    
    /*Position an element relative to the top-left corner*/
    this.setPos = function(x,y) {
        $("#"+this.id).offset({left:x, top:y});
    }
    
    this.getRect = function() {
        var pos = this.getPosition();
        var size = this.getSize();
        
        return {left : pos.left, 
            top : pos.top, 
            right : pos.left + size.width, 
            bottom : pos.top + size.height}; 
    }
}

//.. This isn't really used anymore
function ChannelSurface() {
    var dropperId =""; //.. set when handleDropEvent is fired
    var draggerId =""; //.. set when  channel is cliecked

    /**This is fired when one channel is dropped on another channel             * 
     */
     this.handleDropEvent = function(event, ui ) {
        //.. Get the id of the dropper and dragger and set their values in the html form
        var draggable = ui.draggable;
        dropperId = $(this).attr( 'id' );
        draggerId = draggable.attr('id'); 
        $('#receiver').val(dropperId);
        $('#giver').val(draggerId);
        
        //.. retrieve the corresponding form, and go to its actionbean with parameters
        javaInterface.postToDataLayerAndMerge();
    }
    
    this.getDropperId = function() {
        return dropperId;
    }
    this.getDraggerId = function() {
        return draggerId;
    }
                     
}

