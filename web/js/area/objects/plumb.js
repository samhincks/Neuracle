/* Functions for drawing lines between endpoints
 */

function Plumb() {
    var derivedEndpoint; //.. the means through which two elements are connected
    var techniqueEndpoint;
    var lineEndPoint;
    var bigEndPoint;
    var eSlotEndpoint;
    this.colors = ["red", "blue", "green"];
    this.opacityClasses = ['o10','o9','o8','o7','o6','o5','o4','o3','o2','o1'];//.. very annoying that we must do this but theres a bug in the other methods
    
    //.. Load default settings
    this.loadPlumb = function() {
        jsPlumb.setRenderMode(jsPlumb.SVG);
        jsPlumb.Defaults.Anchors = ["TopCenter", "TopCenter"];
        derivedEndpoint = {       
            endpoint : ["Dot", { radius:1 }],
            isSource:false,
            maxConnections:1,
            isTarget:true,
            dropOptions:{
                tolerance:"touch",
                hoverClass:"dropHover"
            }
        };
      
        lineEndPoint = { endpoint: ["Dot", { radius:1 }], 
            curviness: 200};
        
        eSlotEndpoint = {
            endpoint : ["Dot", { radius:5 }],
            isTarget: true,
            isSource : false,
            curviness: 200,
            maxConnections : 10,
            connectorStyle:{ strokeStyle:"black", lineWidth:2 },
            scope: "technique"
        };
        
        techniqueEndpoint = {
            endpoint : ["Dot", { radius:5 }],
            isSource : true,
            isTarget : false,
            maxConnections : 10,
            connectorStyle:{ strokeStyle:"black", lineWidth:2 },
            curviness: 200,
            scope: "technique"
        };
    
        jsPlumb.Defaults.DragOptions = {
            zIndex:20
        };
        jsPlumb.Defaults.Connector = [ "Bezier", {
            curviness: 11 //90
        } ]; 
    
        var content = $("#content");
        this.addEndPoint(content);
    }
    
    this.drawMLBrain = function(circle ) {
        //this.draw2DLinesWithinBox(circle, 1);
        circle = $("#"+circle);
       var numPoints =4;
       var width = circle.width();
       var incX = 2*(width / (numPoints-1));
       var lastX =0;
       var lastY = 0;
       var up = true;
       
       var lastPoint = null;//getMidLeftPoint(lastY);
       circle.append(lastPoint);
     
      //.. connect each neighboring point
       for(var i=0; i< numPoints; i++) { 
            var thisPoint = this.getBrainPoint(lastX,lastY, up);
            circle.append(thisPoint);
           
           //.. draw connection between it and last point
            if (lastPoint!= null) this.setLineConnection(lastPoint,thisPoint, "grey");
           
           //.. increment point
           lastX += incX;
           lastPoint = thisPoint;
           up = false;
       } 
    }
    
    var getMidLeftPoint = function(radius) {
        var retElement = document.createElement("div");
        $(retElement).css({"position":"absolute"});
        var x = x + radius; //.. since starting point is 
        var y = y +radius;
        $(retElement).css({"left": x, "top": y})
        return retElement;
    }
    
    /**Get point along 2D line*/
    this.getBrainPoint = function(newX, startY, up) {
       var retElement = document.createElement("div");
       $(retElement).addClass("line");
       var yOffset;
       if (up)
           yOffset = startY + Math.floor((Math.random()*20));
       else{
           yOffset = startY - Math.floor((Math.random()*10));
       }
       $(retElement).css({"float":"left", "top":yOffset+"px", "left": newX});
       
       lastVal = yOffset;
       return retElement;
    }

    
    /**Draw a line on the periphery of a circular container to make it look like a brain*/
    this.drawMLBrain2 = function(circle) {
        circle = $("#"+circle);
        //.. we need to know the circle's center and radius
        var radius = circle.width()/2;
        var position = circle.position();
        var cx = position.left;
        var cy = position.top;
        console.log("x: " +cx + " y : " + cy );
        
//        var firstX =  radius*Math.cos(Math.PI); 
//        var firstY =  radius*Math.sin(Math.PI);
//        var secondX =  radius*Math.cos(10*Math.PI/9); 
//        var secondY =  radius*Math.sin(10*Math.PI/9);
//        
//        var thirdX =  radius*Math.cos(10*Math.PI/8); 
//        var thirdY =  radius*Math.sin(10*Math.PI/8); 
//        
//        var element = getPointWCoords (firstX,firstY,radius);
//        var element2 = getPointWCoords (secondX,secondY,radius);
//        var element3 = getPointWCoords (thirdX,thirdY,radius);
//
//        circle.append(element);
//        circle.append(element2);
//        circle.append(element3);
        
        //setBrainLine(element, element2, "grey");
      //  setBrainLine(element2, element3, "grey");

        var lastPoint = null;
        var numerator = 30;
        var denomon = 30;
       //.. connect each neighboring point
       for(var i=0; i< 7; i++) { 
            var trigConst = numerator / denomon;
            var firstX =  radius*Math.cos(trigConst*Math.PI); 
            var firstY =  radius*Math.sin(trigConst*Math.PI);
            var element = getPointWCoords (firstX,firstY,radius);
            circle.append(element);
          
          //.. draw connection between it and last point
            if (lastPoint!= null) setBrainLine(lastPoint, element, "grey");
           
           //.. increment point
           denomon--;
           denomon--;

           lastPoint = element;
       }  
    }
    
    var getPointWCoords = function(x,y, radius){
        var retElement = document.createElement("div");
        $(retElement).css({"position":"absolute"});
        x = x + radius; //.. since starting point is 
        y = y +radius;
        $(retElement).css({"left": x, "top": y})
        return retElement;

    }
    var setBrainLine = function(idA, idB, color) {
       var e1 = jsPlumb.addEndpoint(idA, lineEndPoint);
       var e2 = jsPlumb.addEndpoint(idB, lineEndPoint);
       jsPlumb.connect({ source:e1, 
           target:e2,
           connector:["Bezier",  {curviness: 15}],
           paintStyle:{
               strokeStyle:color, lineWidth:2
            }} );
    
    }
    
    
    /*Add an endpoint without any connections which can be used to drag to others
     **/
    this.addEndPoint = function(container) {
        var e1 = jsPlumb.addEndpoint(container, lineEndPoint);

    }
    
    /**Draw lines from the bottom left corner right-slanted towards the top.
     *Then draw a series of horizontal parallel lines with lighter opacity
     * originating from the first line in order to simulate three dimensions.
     * CAREFUL: We get really wierd behavior when we draw a point outside the containing element
     *   */
    this.draw3DLinesWithinBox = function (container, numLines) {
        //.. 1) Draw line rightward from bottom right corner
        container =  $("#"+container); //. select the container
        
        //.. the bottom point of 3Dline
       var blPoint = this.getPointByPct(container, 0.01, 0.01);
       container.append(blPoint);
       
        //.. the top point of 3Dlne
       // var deepPoint = this.getPointByPct(container, 1.1, 1.2)
       //.. SUPER mysterious: values of endpoints determiens whether or not it works
       //... probably related to the fact that if we draw outside of an element it screws id
        var deepPoint = this.getPointByPct(container, 0.9, 0.9) //.. mysterious; depending on these it works or not
        container.append(deepPoint);
        this.drawStraightLine(blPoint, deepPoint);
        
        //.. 2) Draw parallel lines at deepening points along this line, with receding opacity
        var start =0.1;
       
       //.. draw points along a  line
        for (var i =0; i< numLines; i++) {
            var pos = start + i/numLines;
            
            //.. get left and right point and append
            var left = this.getPointByPct(container, pos-0.01, pos);
            container.append(left);
            var right= this.getPointByPct(container, 1.09,pos);
            container.append(right);
            
            var opacity;
            //.. make deeper lines more transparent
            if (i< this.opacityClasses.length)
                 opacity = this.opacityClasses[i];
            else
                opacity = this.opacityClasses[this.opacityClasses.length-1];
            
            this.drawStraightOpacityLine(left,right,opacity, this.colors[i%this.colors.length]);
        }
    }
    
    /*Get a point inside the container at coordinates specified by percentages
     *pctR = how far from right. .99 would practically be to the right
     *pctH = how far from the bottom. .99 would be at the top
     *buggy.. it seems 1.2 gets you to the top*/
    this.getPointByPct = function(container, pctW, pctH) {
       var width = container.width();
       var height = container.height(); //.. wierd scaling issue
       var left = width *pctW;
       var bottom = height * pctH;
       
       //.. create element and return it
       var retElement = document.createElement("div");
       $(retElement).addClass("point");
       $(retElement).css({"left":left, "bottom":bottom});
       return retElement;
    }
 
    //.. draw a straight line, and try to simulate 3D
    this.drawStraightOpacityLine = function(idA, idB,oClass, color) {
       var e1 = jsPlumb.addEndpoint(idA, lineEndPoint);
       var e2 = jsPlumb.addEndpoint(idB, lineEndPoint);
       jsPlumb.connect({ 
           source:e1, 
           target:e2, 
           cssClass:oClass,
           connector:["Bezier",  {curviness: 25}],
           paintStyle:{
               strokeStyle: color, 
               lineWidth:1,
               dashStyle: "2 2"
                
           }} );
    }
    
    //.. draw a straight line, and try to simulate 3D
    this.drawStraightLine = function(idA, idB) {
       var e1 = jsPlumb.addEndpoint(idA, lineEndPoint);
       var e2 = jsPlumb.addEndpoint(idB, lineEndPoint);
       jsPlumb.connect({ 
           source:e1, 
           target:e2, 
           connector:"Straight",
           paintStyle:{
               strokeStyle:"black",
               lineWidth:2,
               gradient:{ stops:[[0, 'black'],[1, 'white']] }
           }} );
    }
    
    
    /*Draw lines from left to right inside specified box*/
    this.draw2DLinesWithinBox =function(container, numLines) {
        var MAXLINES = 5;
        var height = $("#"+container).height();

       //.. draw normally; one for each
        if (numLines < MAXLINES) {
            var incY = height / numLines;
        
            for (var i=0; i < numLines; i++) {
                var startY = 10 + incY*i;
                this.makeLineWithinBox(container,startY, this.colors[i % this.colors.length],false);
            }
        }
        
        //.. to avoid hideous art, draw only 5 with some dotted between 
        else {
             numLines = MAXLINES;
             var incY = height / numLines;
             
             var startY = 10;
             this.makeLineWithinBox(container,startY, this.colors[0],false);
             startY+= incY;
             this.makeLineWithinBox(container,startY, this.colors[1],false);
             startY +=incY;
             this.makeLineWithinBox(container, startY, "grey",true);
             
             startY +=incY;
             this.makeLineWithinBox(container, startY, "grey",true);
             
             startY+= incY;
             this.makeLineWithinBox(container,startY, this.colors[2],false);
        }
       

    } 
    
    /**Draw a line from left to right in a box. 
     *container = containing Element
     *startY = offset from top 
     **/
    this.makeLineWithinBox = function(container, startY, color, dashed) {
       var numPoints =5;
       var width = $("#"+container).width();
       var incX = width / (numPoints-1);
       var lastPoint = null; //.. set as null to start
       var lastX =0;
       
       //.. connect each neighboring point
       for(var i=0; i< numPoints; i++) { 
            var thisPoint = this.getPoint(lastX, startY);
            $("#"+container).append(thisPoint);
           //.. draw connection between it and last point
            if (lastPoint!= null) {
                if (!dashed)
                    this.setLineConnection(lastPoint,thisPoint, color);
                else
                    this.setDashedLineConnection(lastPoint,thisPoint, color)
            }
           //.. increment point
           lastX += incX;
           lastPoint = thisPoint;
       } 
    }
    
    /**Get point along 2D line*/
    this.getPoint = function(newX, startY) {
       var retElement = document.createElement("div");
       $(retElement).addClass("line");
       var yOffset = startY + Math.floor((Math.random()*10)-5);
       $(retElement).css({"float":"left", "top":yOffset+"px", "left": newX});
       lastVal = yOffset;
       return retElement;
    }
    
    //.. draw a line between two points with specified color
    this.setLineConnection = function(idA, idB, color) {
       var e1 = jsPlumb.addEndpoint(idA, lineEndPoint);
       var e2 = jsPlumb.addEndpoint(idB, lineEndPoint);
       jsPlumb.connect({ source:e1, target:e2, paintStyle:{
               strokeStyle:color, lineWidth:2
            }} );
    
    }
    
    //.. draw a line between two points with specified color
    this.setDashedLineConnection = function(idA, idB, color) {
       var e1 = jsPlumb.addEndpoint(idA, lineEndPoint);
       var e2 = jsPlumb.addEndpoint(idB, lineEndPoint);
       jsPlumb.connect({ source:e1, target:e2, paintStyle:{dashstyle:"2 2",
               strokeStyle:color, lineWidth:2
            }} );
    
    }
    /*Set a Dervied connection between a and b, ie b is a datalayer derived from a*/
    this.setDerivedConnection = function(idA, idB) {
       var e1 = jsPlumb.addEndpoint(idA,{ anchor:"Center" }, derivedEndpoint);
       var e2 = jsPlumb.addEndpoint(idB,{ anchor:"Center" }, derivedEndpoint);
       jsPlumb.connect({ source:e1, target:e2, paintStyle:{ dashstyle:"2 4", strokeStyle:"#465", lineWidth:2 } });
    }
}
