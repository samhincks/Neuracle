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
                id.removeClass("experimentHalfIntersected");
                id.removeClass("experimentIntersected");

            }
            else if ((this.dls[i].intersected) >=3) {
                id.addClass("experimentIntersected");
                id.removeClass("experimentHalfIntersected");

            }
            else {
                id.addClass("experimentHalfIntersected");
                id.removeClass("experimentIntersected");

                
            }
        }
    }
    
    //.. add the selected to class to target element; remove from all others
    this.selectLayer = function(layerId) {
        //.. remove all other 
        for(var i =0; i< this.dls.length; i++) {  
            var dl = this.dls[i]; 
            var curId =  dl.id;
             $("#"+curId).removeClass("experimentSelected")
             $("#"+curId).removeClass("surfaceElementMultiSelected")
             $("#"+curId).removeClass("channelsetSelected")
             
             if(layerId == curId) {
                 if (dl.type == "2D") 
                    $("#" + layerId).addClass("channelsetSelected");
                 else 
                    $("#" + layerId).addClass("experimentSelected");
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
          $("#"+selected[0]).removeClass("channelsetSelected")
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
function DataLayer(jsonDL) {
    this.id = jsonDL.id;
    this.parent =jsonDL.parent;
    this.numChannels = jsonDL.numchannels;
    this.numPoints = jsonDL.numPoints;
    this.type = jsonDL.type;
    this.freqButton = "freq"+this.id; //.. For some reason it wont respond if I give it a unique id
    this.nameButton = "name" + this.freqButton; 
    this.elementTag = '<div id = "'+this.id+'" class = "datalayer surfaceElement"> <div title = "F" id = "'+this.freqButton+'"> <div title = "N" id = "' + this.nameButton+ '"</div></div></div>';
    this.sqScale = d3.scale.linear().domain([0, 625000]).range([45, 90]);
    this.intersected = 0; //.. increase if anything is intersected

    /**Display other graphing possibilities inside the container, when we hover over.
     * CALL THIS AFTER YOU'VE ADDED THE ELEMENT TAG TO THE DOM, AND NO MORE
     * **/
    this.displaySubGraphs = function() {
        $("#"+this.id).attr("title", this.id + " has " + this.numChannels + " channels");
        var globalFreqId = $("#" + this.freqButton); //.. but when it adds a new datalayer it will refer to the latest one!
        globalFreqId.attr("title", "P"); //.. this line of code doesnt work
        var globalNameId = $("#" + this.nameButton);
        globalNameId.attr("title", "N"); 

        //.. For a channelset, display frequency and correlation views
            //.. Give it the title attribute, so that the tooltip function applies
            globalFreqId.tooltip({
                content: "P",
                tooltipClass: "freq",
                hide: {duration: 1200},
                position: {my: 'right bottom+15', at: 'left center', collision: 'flipfit'},
                open: function(event, ui) {
                    $(ui.tooltip).dblclick(function(e) {
                        javaInterface.postToDataLayer("prediction");
                    });
                    var nId = $("#name" + this.id);
                    nId.tooltip('open'); //.. doesnt trigger conventional open
                },
                close: function(event, ui) {
                    globalNameId.tooltip('close'); //.. doesnt trigger conventional open
                }
            });
            
            globalNameId.tooltip({
                content: this.id,
                hide: {duration: 1200},
                open: function(event, ui) {
                    $(ui.tooltip).dblclick(function(e) {
                        javaInterface.postToDataLayer();
                    });
                },
                position: {my: 'left bottom+90', at: 'center center', collision: 'flipfit'}
               
            });
            
            $("#" + this.id).tooltip({
                content: "C",
                tooltipClass: "corr",
                hide: {duration: 1200},
                position: {my: 'left bottom-15', at: 'left center', collision: 'flipfit'},
                open: function(event, ui) {
                    $(ui.tooltip).dblclick(function(e) {
                        javaInterface.postToDataLayer("correlation");
                    });
                    var fId = $("#freq"+this.id);
                    fId.tooltip('open'); //.. doesnt trigger conventional open
                },
                close: function(event, ui) {
                    globalFreqId.tooltip('close'); //.. doesnt trigger conventional open
                }
            });            
        
    }
    
    
    
    
   //..  Draw lines inside it as art, and lines connecting
   //... it to elements it may have been derived from
   //. Errors will occur if it has not been appended to the canvas
    this.drawArt = function() {
        //.. Assign appropriate image to the datalayer 
        if (jsonDL.type == "2D")
            if(this.numChannels <17)
                $("#" + this.id).addClass("chanset");
            else
                $("#" + this.id).addClass("chanset2");
        else { //.. 3D
            console.log(jsonDL.numlabels);
            if (jsonDL.numlabels ==1) $("#"+this.id).addClass("experiment1");
            if (jsonDL.numlabels ==2) $("#"+this.id).addClass("experiment2");
            if (jsonDL.numlabels ==3) $("#"+this.id).addClass("experiment3");
            if (jsonDL.numlabels >=4) $("#"+this.id).addClass("experiment4");
        }
        var scaledSize = this.sqScale(jsonDL.numpoints);
        $("#" + this.id).width(scaledSize).height(scaledSize /1.7);

        //.. if this datalayer was derived from another layer, draw a line between them 
        if (this.parent != null && this.parent != "Motherless") {
            plumb.setDerivedConnection(this.id, this.parent);
          }
        else {
            jsPlumb.draggable($(".surfaceElement"));
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
        y = y % 300;
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

