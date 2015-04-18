/* Functions for drawing lines between endpoints. Used for showcasing inheritence between
 * objects. TODO
 */
function Plumb() {
    var derivedEndpoint; //.. the means through which two elements are connected
    var techniqueEndpoint;
    var lineEndPoint;
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
    
    
    
    /*Add an endpoint without any connections which can be used to drag to others
     **/
    this.addEndPoint = function(container) {
        jsPlumb.addEndpoint(container, lineEndPoint);
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
       var ex = jsPlumb.connect({source:e1, target:e2, paintStyle:{ dashstyle:"2 4", strokeStyle:"#465", lineWidth:2 } });
       this.connections.push(ex);
    }
    this.removeConnection = function(id) {
        for (var c in this.connections) {
           var con = this.connections[c];
           if (con.sourceId== id || con.targetId == id) {
                jsPlumb.detachAllConnections(con.targetId)
                jsPlumb.detachAllConnections(con.sourceId)
            }
        }
    }
    
    this.connections = new Array();
}
