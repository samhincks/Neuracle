
/**Collection of available data layers*/
function DataLayers() {
    this.dls =  new Array(); //.. all available datalayers
    var selected = new Array(); //.. a single datalayer that is selected (has a blue square around it)
    this.lastSelectedId ="";
   
   
    
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
            else if ((this.dls[i].intersected) >=3) { //.. Make background green
                id.addClass("experimentIntersected");
                id.removeClass("experimentHalfIntersected");
            }
            else { //.. Make red, so that user knows they need to move it a little
                id.addClass("experimentHalfIntersected");
                id.removeClass("experimentIntersected");
            }
        }
    }    
    
    //.. add the selected to class to target element; remove from all others
    this.selectLayer = function(layerId) {
        this.lastSelectedId = layerId;
        //.. remove all other 
        for(var i =0; i< this.dls.length; i++) {  
            var dl = this.dls[i]; 
            var curId =  dl.id;
             $("#"+curId).removeClass("experimentSelected")
             $("#"+curId).removeClass("surfaceElementMultiSelected")
             $("#"+curId).removeClass("channelsetSelected")
             
             //.. And add selection css if we now have access toe the current one
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
        
        //.. Add it if it doesn't already exist
        if(selected.indexOf(layerId) == -1) 
            selected.push(layerId);
        
        //.. make a String of of all selected data layers which will be sent to java. 
        //.. Java will know its a multiselection by an id that has many colons
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
    this.bTooltip = "b"+this.id; 
    this.cTooltip = "c" + this.bTooltip; //.. hacked to make tooltips work
    this.elementTag = '<div id = "'+this.id+'" class = "datalayer surfaceElement"> <div title = "F" id = "'+this.bTooltip+'"> <div title = "N" id = "' + this.cTooltip+ '"</div></div></div>';
    this.sqScale = d3.scale.linear().domain([0, 625000]).range([45, 90]); //.. scale the size of the datalayer
    this.intersected = 0; //.. increase if anything is intersected
    
    this.hasPerformance = false;  //.. set to true if this object should display the P for its performance 

    
    /** Display tooltips by the datalayer. Somewhat of a hack, since we want three
     * tooltips for one datalayer, so we've made the corresponding tag for an element
     * a triple embedded div
     **/
    this.displayTooltips = function() {
        //.. Give the divs the title attribute, so that tooltips work
        $("#"+this.id).attr("title", this.id + " has " + this.numChannels + " channels");
        var cId = $("#" + this.cTooltip); //.. but when it adds a new datalayer it will refer to the latest one!
        cId.attr("title", "P"); //.. this line of code doesnt work
        var bId = $("#" + this.bTooltip);
        bId.attr("title", "N"); 
        
        //.. set these fresh variables since this context will be clobbered where we need them
        var type = this.type;
        var hasPerformance =this.hasPerformance; 
        var titleId = this.id.split("-")[0];
        
        //.. For a channelset, display frequency and correlation views
        //.. Give it the title attribute, so that the tooltip function applies
        bId.tooltip({
            content: "C",
            tooltipClass: "pred",
            hide: {duration: 1200},
            position: {my: 'right bottom+15', at: 'left center', collision: 'flipfit'},
            open: function(event, ui) {
                $(ui.tooltip).dblclick(function(e) {
                    javaInterface.postToDataLayer("correlation");
                });
                cId.tooltip('open'); //.. doesnt trigger conventional open
            },
            close: function(event, ui) {
                cId.tooltip('close'); //.. doesnt trigger conventional open
            }
        });

        cId.tooltip({
           content: "P",
           tooltipClass: "corr",
            hide: {duration: 1200},
            position: {my: 'left bottom', at: 'left center', collision: 'flipfit'},
            open: function(event, ui) {
                $(ui.tooltip).dblclick(function(e) {
                    javaInterface.postToDataLayer("prediction");
                });
            },

        });

        $("#" + this.id).tooltip({
            content: titleId,
            hide: false,
            open: function(event, ui) {
                $(ui.tooltip).dblclick(function(e) {
                    javaInterface.postToDataLayer();
                });
                if (type =="2D" || type == "3D"){ //.. set of chain of showing corr view if its a 2D
                    bId.tooltip('open'); //.. doesnt trigger conventional open
                }
            }, 
            close: function(event, ui) {
                bId.tooltip('close'); //.. doesnt trigger conventional open
            },
            position: {my: 'left bottom+70', at: 'center center', collision: 'flipfit'}

        });            
    }
    
    this.toggleCTooltip = function(on) {
         if (on == "false" )//. Add Performance as part of the obejct
            $("#cb" + this.id).tooltip({content: ""});
        else 
            $("#cb" + this.id).tooltip({content: "P"});
    }
    
   /** Add an appropriate image to the datalayer that conveys information about the datalayer**/
    this.drawArt = function() {
        //.. Assign appropriate image to the datalayer 
        if (jsonDL.type == "2D") {
            if(this.numChannels <17)
                $("#" + this.id).addClass("chanset");
            else
                $("#" + this.id).addClass("chanset2");
        }
        else { //.. 3D
            if (jsonDL.numlabels ==1) $("#"+this.id).addClass("experiment1");
            if (jsonDL.numlabels ==2) $("#"+this.id).addClass("experiment2");
            if (jsonDL.numlabels ==3) $("#"+this.id).addClass("experiment3");
            if (jsonDL.numlabels >=4) $("#"+this.id).addClass("experiment4");
        }
        
        //.. Set appropriate size (though we may want to change this if we run on a better server)
        var scaledSize = this.sqScale(jsonDL.numpoints);
        $("#" + this.id).width(scaledSize).height(scaledSize /1.7);

        //.. if this datalayer was derived from another layer, draw a line between them 
        if (this.parent != null && this.parent != "Motherless") 
            plumb.setDerivedConnection(this.id, this.parent);
        
        else 
            jsPlumb.draggable($(".surfaceElement"));
        
        
        //.. pulsing animation of streaming objects
        if(jsonDL.streaming != null) {
            var me = this;

            this.blinkIn = function() {
                $("#" + this.id).animate({
                    opacity: 0.25,
                    }, 3000, function() { me.blinkOut();});
            }

            this.blinkOut = function() {
                $("#" + this.id).animate({
                    opacity: 1,              
                    }, 3000, function() { me.blinkIn();});
            }
            this.blinkIn();
        }
    }
    
    
    this.slopes = [];
    
    function standardDeviation(values) {
        var avg = d3.mean(values);

        var squareDiffs = values.map(function (value) {
            var diff = value - avg;
            var sqrDiff = diff * diff;
            return sqrDiff;
        });

        var avgSquareDiff = d3.mean(squareDiffs);

        var stdDev = Math.sqrt(avgSquareDiff);
        return stdDev;
    }

 
    /** Compute how far how many standard deviatiosn we are from the mean 
     **/
    this.processStat = function(val) {
        val = val*1;
        this.slopes.push(val);
        var avg = d3.mean(this.slopes);
        var dev = standardDeviation(this.slopes);
        var quant = d3.quantile(this.slopes,0);
        var diff = val - avg;
        var deviationsAway = diff / dev;
       // console.log(this.slopes);
        //console.log(val);
        console.log(deviationsAway);
        return deviationsAway;
        
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
    
    /*Return a rect describe xy coordinates of the object*/
    this.getRect = function() {
        var pos = this.getPosition();
        var size = this.getSize();
        
        return {left : pos.left, 
            top : pos.top, 
            right : pos.left + size.width, 
            bottom : pos.top + size.height}; 
    }
}



