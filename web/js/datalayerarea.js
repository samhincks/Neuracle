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
            var dlRect = dl.getRect();
            
            //.. Select each technique, and check for intersection with the datalayer
            for (var i = 0 ; i < techRects.length; i++) {
                var intersect = intersectRect(techRects[i], dlRect);
                if (intersect) 
                    retArray.push({sourceId : this.techniques.techniques[i].id, targetId : dl.id});
            }
       }
       return retArray;
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
    
    /**Datalayers is an array of JSONObjs streamed from Java. 
     *Add the ones that don't already exist
     */
    this.addDatalayers  = function(layers) {
        //... Add the datalayers that may have been created       
        for (var i =0; i< layers.length; i++) { 
            var dl = layers[i];

            //.. the datalayer that should be reloaded
            var idName = dl.id;
            
           //.. if this is a brand new datalayer, make it and determine its default position
            if (this.datalayers.getDLById(idName) ==null) { //.. if we haven't added this dl yet'
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
                   $("#"+newLayer.id).offset({left:leftMost, top:fromTop});
                   leftMost += newLayer.getSize().width*1.3;
                   var containerWidth = $("#topLeft").width();
                   
                   //.. if it gets too far left, then jump down a few spaces
                   if (leftMost > containerWidth-60) {
                       leftMost = 30;
                       fromTop += 150;
                   }
                }
                
                newLayer.drawArt();
            }
        } 
       
    }
     
    /*Add element to selection. Eventually check bounds*/
     this.addDLToCanvas = function(element) {
         
    }
    /**techniques is an array of JSONObjs streamed from Java. Add the ones that don't already exist
     **/
    this.addTechniques = function(techniques) {

        //... Add the techniques that may have been created       
        for (var i =0; i< techniques.length; i++) { 
            var t = techniques[i];
            console.log(t);
            console.log(t.type + " , " + t.trained);

            //.. the technique that should be reloaded
            var idName = t.id;
            $("#" + idName).width(12.0);
            if (this.techniques.getTechniqueById(idName) ==null) { //.. if we haven't added this dl yet'
                var newTechnique = new Technique(t);
                addTechToCanvas(newTechnique);
                newTechnique.initializePlumb();
                this.techniques.addTechnique(newTechnique);
            }
            
            else if (t.type =="Classifier" && t.trained>0) {
                var curWidth = $("#"+idName).width();
                var curHeight = $("#" + idName).height();
                $("#"+idName).width(curWidth*1.3);
                $("#" +idName).height(curHeight * 1.3);
            }

        }
    }

    var intersectRect = function(r1, r2) {
        return !(r2.left > r1.right || 
                 r2.right < r1.left || 
                 r2.top > r1.bottom ||
                 r2.bottom < r1.top);
      }
}

