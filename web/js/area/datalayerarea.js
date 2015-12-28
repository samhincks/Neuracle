/**- Control where techniques and datalayers get laid out by default
 *  -Make sure they don't exist the canvas
 *  -Check for intersections between techniques and datalayers
**/
function DatalayerArea(selection) {
    this.techniques = new Techniques();
    this.datalayers = new DataLayers();
    
    var leftMost =30;//.. the position of the left most automatically placed element
    var fromTop =100;//.. the position of the first row of loaded objects
    
    /**When you hit evaluate(), check what techniques are intersecting the specified datalayer.
     *Return an array specifying all these techniques, a tuple for each connection with
     *a.sourceId (the technique), a.targetId the datalayer
     **/
    this.getIntersectedTechniques = function() {
         var retArray = new Array(); //.. an array of tuples with target and source id
         
         //.. build a rectangular box for each technique. (We don't want to repeat this for every data layer 
         var techniqueTags = $('.technique');
         var techRects = new Array();
         for (var i = 0 ; i < techniqueTags.length; i++) {
            var techEl = $(techniqueTags[i]);
            var techRect = makeRect(techEl);
            techRects[i] = techRect;
         }

         //..consider each datalayers possible intersection with techniques
         for (var j in this.datalayers.dls) {
            var dl = this.datalayers.dls[j];
            dl.intersected = 0;
            var dlRect = dl.getRect();
                //.. Select each technique, and check for intersection with the datalayer
                for (var i = 0 ; i < techRects.length; i++) {
                    var intersect = intersectRect(techRects[i], dlRect, 75, 75);
                    if (intersect) {
                        retArray.push({sourceId : this.techniques.techniques[i].id, targetId : dl.id, sourceType :this.techniques.techniques[i].type, datalayer : dl });
                        if (dl.type =="3D")
                             dl.intersected++;
                        else { //.. then wenwant the technique to change color only if its trained
                            this.techniques.techniques[i].intersected++;
                        }
                    }
                    else {
                        $(techniqueTags[i]).removeClass("classifierTrainedIntersected");
                    }
                }
       }
       return retArray;
    }
    
    /*Reset position if buoyant user moves datalayer outside container*/
    this.boundsCheck = function(){
        var techniqueTags = $('.surfaceElement');
        var container = $("#topLeft");
        var contRect = makeRect(container);
        
        contRect.left -= 30;
        contRect.right -= 10;
        contRect.top -= 30;
        contRect.bottom -= 10;
        var containerWidth = contRect.right - contRect.left;
        var containerHeight = contRect.bottom - contRect.top;
        for (var i = 0 ; i < techniqueTags.length; i++) {
            var techEl = $(techniqueTags[i]);
            var techRect = makeRect(techEl);
            
            if (!(intersectRect(techRect, contRect, containerHeight/2, containerWidth/2))) {
                techEl.css({top: (10), left: (20), position:'absolute'});
            }
        }
    }
    
    /**Given a jquery element with left,top, width, height defined, return
     *a rect with left, right, bottom, top. Broken OO, error with selecting techniques 
     *otherwise structure would mimic datalayer*/
    var makeRect = function (element) {
        return {left : element.position().left, 
            top : element.position().top, 
            right : element.position().left + element.width(), 
            bottom : element.position().top + element.height()};
    }
   
    
   var addTechToCanvas = function(element) {
         $(selection).append(element.elementTag);
   }
    
    /**If our datalayers object contains datalayers which we no longer want, then 
     * remove them**/
    this.removeDatalayers = function(layers) {
        //... Add the datalayers that may have been created  
        var arrayLength =this.datalayers.dls.length; 
        
        //.. iterate through all the layers
        for (var i = 0; i < arrayLength; i++) { 
            var dl = this.datalayers.dls[i];
            var exists = false;
            
            //.. if we have a local copy of it, flag it as existing
            for (var j in layers) {
                if (layers[j].id == dl.id) {
                    exists =true; break;
                }
            }
            
            //.. if it doesn't exist, remove it from the dom. 
            if (!exists) {
                //.. 1) remove from the DOM
                $("#"+dl.id).remove();
                
                //.. 2) remove from array, padding it without disturbing iteration through this loop
                this.datalayers.dls.splice(i, 1);
                arrayLength--;
                i--;
                
                //.. 3) Remove any plumb connectors that involve this id
                plumb.removeConnection(dl.id);
            }
        }
    }
    
    /**Datalayers is an array of JSONObjs streamed from Java. 
     *Add the ones that don't already exist */
    this.addDatalayers  = function(layers) {
        //... Add the datalayers that may have been created       
        for (var i =0; i< layers.length; i++) { 
            var dl = layers[i];

            //.. the datalayer that should be reloaded
            var idName = dl.id;
            var localDl = this.datalayers.getDLById(idName);
            
           //.. if this is a brand new datalayer, make it and determine its default position
            if (localDl ==null) { //.. if we haven't added this dl yet'
                //.. instantiate a new datalayer with the JSON specifications
                var newLayer = new DataLayer(dl);
                
                //.. add it to datalayers object
                this.datalayers.addDL(newLayer);
                
                //.. then append to canvas and determine its position
                $(selection).append(newLayer.elementTag);

                //.. IF it has a parent place directly beneath it
                if (!(newLayer.parent == "Motherless")) {
                    var parent = this.datalayers.getDLById(newLayer.parent);
                    var parentPos = parent.getPosition();
                    var yInc = parent.getSize().height+newLayer.getSize().height*2.5;//.. add a certain amount in y positioning
                    newLayer.setPos(parentPos.left,parentPos.top+yInc);
                }
                //.. move it rightward
                else{
                   $("#"+newLayer.id).offset({left:leftMost, top:(fromTop% $("#topLeft").height())});
                   leftMost += newLayer.getSize().width*1.3;
                   var containerWidth = $("#topLeft").width();
                   
                   //.. if it gets too far left, then jump down a few spaces
                   if (leftMost > containerWidth-60) {
                       leftMost = 30;
                       fromTop += 150 
                   }
                }
                                
                //.. add images and art 
                newLayer.drawArt();
                newLayer.displayTooltips();
                newLayer.toggleCTooltip(dl.performance);    
                
                //.. finally, instantitate the listeners
                 datalayerInit("#"+newLayer.id); //.. relaod the drag/drop properties

            }
            
            else { //.. Even if we're not redrwaing the layer, potentially add an extra button
                localDl.toggleCTooltip(dl.performance);
            }
            
        } 
    }
    
    /**Place techniques ontop of datalasyer they intersect **/
    this.highlightIntersectedTechniques = function(){
        var intersected = this.getIntersectedTechniques();
        var selected = $(".channelsetSelected");
        for (var i =0; i <intersected.length; i++) {
            var tech = this.techniques.getTechniqueById(intersected[i].sourceId);
            //var dl this.datalayers.
            //            
            //.. show its a valid selection ebtween trained classifier and channelset
            if (tech.trained > 0) {
                //.. Don't color green unless we're selecting the one we intersect
                if ($(selected[0]).attr("id") == intersected[i].targetId) 
                    $("#" + intersected[i].sourceId).addClass("classifierTrainedIntersected");
             }
        }
    }
   
    
    /**techniques is an array of JSONObjs streamed from Java. Add the ones that don't already exist  **/
    this.addTechniques = function(techniques) {
        //... Add the techniques that may have been created       
        for (var i =0; i< techniques.length; i++) { 
            var t = techniques[i];
            
            //.. the technique that should be reloaded
            var idName = t.id;
            $("#" + idName).width(12.0);
            if (this.techniques.getTechniqueById(idName) ==null) { //.. if we haven't added this dl yet'
                var newTechnique = new Technique(t);
                addTechToCanvas(newTechnique);
                newTechnique.initializePlumb();
                this.techniques.addTechnique(newTechnique);
                individualTechniqueInit(idName, t.type);
            }
            
            //.. Add a special shadow if its a trained usable classifier 
            else if (t.type =="Classifier" && t.trained>0) {
                this.techniques.getTechniqueById(idName).trained =true;
                $("#"+idName).addClass("classifierTrained");
            }

        }
    }
    

    /**R2 is datalayer, r1 is technique, for instance. Return true if they intersect
     * given an amount of afforded slack */
    var intersectRect = function(r1, r2, SLACKY, SLACKX) {
        //console.log(r1);
        var largeX = r2.right - ((r2.right - r2.left)/2);
        var largeY =  r2.bottom - ((r2.bottom - r2.top)/2); //.. a super rectangle drawn around the datalayer
        
        var smallX = r1.right - ((r1.right - r1.left)/2);
        var smallY = r1.bottom - ((r1.bottom - r1.top)/2);
        
        var maxY = largeY + SLACKY;
        var minY = largeY - SLACKY;
        
        var maxX = largeX + SLACKX;
        var minX = largeX - SLACKX;
        
         
        //.. case by case, 1. is it within Y grasp
        if (smallY < maxY && smallY > minY) {
            if (smallX < maxX && smallX > minX) {
                return true;
            }
        }
        return false;
      }
      
     
}

