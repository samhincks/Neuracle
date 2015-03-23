/* Functions for drawing lines between endpoints
 */

function Point(jqueryTag, parent, x, y) {
    this.jQueryTag = jqueryTag; //.. access to the div element that contains it
    this.parent = parent; //.. div tag of parent
    this.x =x; //.. its position x, relative to its container. left:x. X positions from left
    this.y =y; //.. its position y, relative to container. top:y . Y px from top
   //.. the default endpoint
   this.endPoint = { endpoint: ["Dot", { radius:1 }], 
            curviness: 200}; //.. plumb description of endpoint
}

//.. A line between two elements
function Line(pointA, pointB) {
    this.pointA = pointA;
    this.pointB = pointB;
    this.color = "green";
    this.connector = [ "Bezier", { curviness: 11 } ]; 
    this.paintStyle = {strokeStyle:"green", lineWidth:2}
}

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
    }
    
    //.. Draw 
    this.drawMLBrain  = function(circle) {
         circle = $("#"+circle);
         var startingPoint = getMidLeftOfCircle(circle);
         var endingPoint = getMidRightOfCircle(circle);
         var points = generatePoints(circle, startingPoint, endingPoint, 5);
         drawLine(points);
    }
    
    var drawLine = function(points) {
        var thisPoint = points[0];
        for (var i=1; i< points.length; i++) {
            var newPoint = points[i];
            var line = new Line(thisPoint, newPoint );
            setConnection(line);
            thisPoint = newPoint;
        }
    }
    
    //.. draw a line between two points with specified color
    var setConnection = function(line) {
       var e1 = jsPlumb.addEndpoint(line.pointA.jQueryTag, line.pointA.endPoint);
       var e2 = jsPlumb.addEndpoint(line.pointB.jQueryTag, line.pointB.endPoint);
       jsPlumb.connect({ source:e1, target:e2, paintStyle: line.paintStyle} );
    }
    
    //.. generates -numPoints- points from left to right 
    var generatePoints = function(container, startingPoint, endingPoint, numPoints) {
         //.. distance to go on both axes
         var xDistance = endingPoint.x - startingPoint.x;
         var yDistance = endingPoint.y - startingPoint.y;

        //.. change between each point
         var xChange = xDistance / (numPoints-1);
         var yChange = yDistance / (numPoints-1);
         //.. initialize array and last point
         var retPoints = new Array();
         var lastPoint = startingPoint;
         retPoints[0] = startingPoint; 
         
         //.. add all points between start and end
         for (var i=1; i <numPoints-1; i++) {
             lastPoint = getPoint(container,lastPoint.x + xChange, lastPoint.y +yChange);
             retPoints[i] = lastPoint;
         }
         
         return retPoints;
    }
    
    //.. returns the point at the way right of the circle
    var getMidRightOfCircle = function(circle) {
        var radius = circle.width()/2;
        var x = radius*2; 
        var y = radius;
        return getPoint(circle,x,y);
    }
    
    //.. get point corresponding to the mid left corner of a circle
    var getMidLeftOfCircle = function(circle) {
        var radius = circle.width()/2;
        var x = 0; 
        var y = 0;
        return getPoint(circle,x,y);
    }
    
    //.. place element at specified coordinates, and append. Then return as a point
    //... Assume 0,0 is at topLeftCorner
    var getPoint = function(container,x,y) {
        var retElement = document.createElement("div");
        $(retElement).css({"position":"absolute"});
        $(retElement).css({"left": x, "top": y, "float" :"left"});
        container.append(retElement);
        return new Point(container, retElement, x,y);
    }
    
    this.drawMLBrain3 = function(circle) {
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
    
    /**Returns the current state of connections labeled as scope
     **/
    this.getTechniqueConnections = function() {
        var c = jsPlumb.getConnections("technique");
        //.. print them
        for (var i in c) {
            var l = c[i];
            console.log( i +" is sc " + l.sourceId + "   t" + l.targetId);
            if (l && l.length > 0) {
                    for (var j = 0; j < l.length; j++) {
                         console.log("s: " + l[j].sourceId + " .. t: " +l[j].targetId);
                    }
            }
        }
        return c;
    }
    
    
    this.setExperimentEPs= function() { 
       jsPlumb.addEndpoint($(".experiment") , { anchor:"TopLeft" }, eSlotEndpoint);

    }
    
    this.setTechniqueEPs = function() {
      //  jsPlumb.addEndpoint($(".classifierT") , { anchor:"Center" }, techniqueEndpoint);
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
        var height = $("#"+container).height();
        var incY = height / numLines;
        for (var i=0; i < numLines; i++) {
            var startY = 10 + incY*i;
            this.makeLineWithinBox(container,startY, this.colors[i % this.colors.length]);
        }
    } 
    
    /**Draw a line from left to right in a box. 
     *container = containing Element
     *startY = offset from top 
     **/
    this.makeLineWithinBox = function(container, startY, color) {
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
            if (lastPoint!= null) this.setLineConnection(lastPoint,thisPoint, color);
           
           //.. increment point
           lastX += incX;
           lastPoint = thisPoint;
       } 
    }
    
    /**Get point along 2D line*/
    this.getPoint = function(newX, startY) {
       var retElement = document.createElement("div");
       $(retElement).addClass("line");
       var yOffset = startY + Math.floor((Math.random()*20)-10);
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
    /*Set a Dervied connection between a and b, ie b is a datalayer derived from a*/
    this.setDerivedConnection = function(idA, idB) {
       var e1 = jsPlumb.addEndpoint(idA,{ anchor:"Center" }, derivedEndpoint);
       var e2 = jsPlumb.addEndpoint(idB,{ anchor:"Center" }, derivedEndpoint);
       jsPlumb.connect({ source:e1, target:e2, paintStyle:{ dashstyle:"2 4", strokeStyle:"#465", lineWidth:2 } });
    }
}
