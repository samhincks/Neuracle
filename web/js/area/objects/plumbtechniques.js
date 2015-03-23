
/**A helper class to plumb for providing the view of techniques connected to data
 *layers
 **/
function PlumbTechniques() {
   var cColor ="#E41A1C"; //"#8DD3C7";//"blue";
   var aColor = "#377EB8"; //"#80B1D3";//"green";
   var fColor = "black";//"4DAF4A";//"#BEBADA";//"purple";
   var sColor = "#984EA3";//"#FB8072";//"red";
    
   var eSlotEndpoint = {
            endpoint : ["Dot", { radius:10 }],
            isTarget: true,
            isSource : false,
            curviness: 200,
            maxConnections : 10,
            connectorStyle:{ strokeStyle:"black", lineWidth:1 },
            scope: "technique"
   };
   
   var classifierEndpoint = {
        endpoint : ["Dot", { radius:5, fillStyle:'red'}],
        isSource : true,
        isTarget : false,
        maxConnections : 1,
        connectorStyle:{ strokeStyle:cColor, lineWidth:1 },
        curviness: 200,
        paintStyle:{ fillStyle:cColor },
        scope: "classifier"
       
   }
   
   var attributeSelectionEndpoint = {
        endpoint : ["Dot", { radius:5, fillStyle:'red'}],
        isSource : true,
        isTarget : false,
        maxConnections : 1,
        connectorStyle:{ strokeStyle:aColor, lineWidth:1 },
        curviness: 200,
        paintStyle:{ fillStyle:aColor },
        scope: "attributeSelection"
       
   }
   
   var featureSetEndpoint = { 
        endpoint : ["Dot", { radius:5, fillStyle:'red'}],
        isSource : true,
        isTarget : false,
        maxConnections : 1,
        connectorStyle:{ strokeStyle:fColor, lineWidth:1 },
        curviness: 200,
        paintStyle:{ fillStyle:fColor },
        scope: "featureSet"
   
   }
   
   var settingsEndpoint = {
        endpoint : ["Dot", { radius:5, fillStyle:'red'}],
        isSource : true,
        isTarget : false,
        maxConnections : 1,
        connectorStyle:{ strokeStyle:sColor, lineWidth:1 },
        curviness: 200,
        paintStyle:{ fillStyle:sColor },
        scope: "settings"
       
   }
  
   /*-----
    *Endpoints for experiment; should precisely match above endpoints but be targets, not sources
    **/
   var cSlotEndpoint = {
        endpoint : ["Dot", { radius:5 }],
        isTarget: true,
        isSource : false,
        curviness: 200,
        maxConnections : 5,
        connectorStyle:{ strokeStyle:cColor, lineWidth:1 },
        paintStyle:{ fillStyle:cColor },
        scope: "classifier"
   }
   
   var aSlotEndpoint = {
        endpoint : ["Dot", { radius:5 }],
        isTarget: true,
        isSource : false,
        curviness: 200,
        maxConnections : 5,
        connectorStyle:{ strokeStyle:aColor, lineWidth:1 }, 
        paintStyle:{ fillStyle:aColor },       
        scope: "attributeSelection"
       
   }
   
   var fSlotEndpoint = {
        endpoint : ["Dot", { radius:5 }],
        isTarget: true,
        isSource : false,
        curviness: 200,
        maxConnections : 5,
        connectorStyle:{ strokeStyle:fColor, lineWidth:1 },
        paintStyle:{ fillStyle:fColor },   
        scope: "featureSet"
       
   }
   
   var sSlotEndpoint = {
        endpoint : ["Dot", { radius:5 }],
        isTarget: true,
        isSource : false,
        curviness: 200,
        maxConnections : 5,
        connectorStyle:{ strokeStyle:sColor, lineWidth:1 },
        paintStyle:{ fillStyle:sColor },   
        scope: "settings"
   }
   
   var techniqueEndpoint = {
        endpoint : ["Dot", { radius:5, fillStyle:'red'}],
        isSource : true,
        isTarget : false,
        maxConnections : 1,
        connectorStyle:{ strokeStyle:"black", lineWidth:1 },
        curviness: 200,
        paintStyle:{ fillStyle:"red" },
        scope: "technique"
   };
   /**Returns the current state of connections labeled as scope
     **/
    this.getTechniqueConnections = function() {
        var a = jsPlumb.getConnections("attributeSelection");
        var c = jsPlumb.getConnections("classifier");
        var f = jsPlumb.getConnections("featureSet");
        var s = jsPlumb.getConnections("settings");
        return a.concat(c,f,s);
    } 
    
    this.setExperimentEPs= function() { 
      jsPlumb.addEndpoint($(".experiment") , { anchor:"TopLeft" }, aSlotEndpoint);
      jsPlumb.addEndpoint($(".experiment") , { anchor:"TopRight" }, cSlotEndpoint);
      jsPlumb.addEndpoint($(".experiment") , { anchor:"BottomLeft" }, fSlotEndpoint);
      jsPlumb.addEndpoint($(".experiment") , { anchor:"BottomRight" }, sSlotEndpoint);
    }
    
    
    this.setClassifierEP = function(id) {
        jsPlumb.addEndpoint($("#"+id) , { anchor:"Center" }, classifierEndpoint);
    }
    this.setFeatureSetEP = function(id) {
        jsPlumb.addEndpoint($("#"+id) , { anchor:"Center" }, featureSetEndpoint);
    }
    this.setSettingsEP = function(id) {
        jsPlumb.addEndpoint($("#"+id) , { anchor:"Center" }, settingsEndpoint);
    }
    this.setAttributeSelectionEP = function(id) {
        jsPlumb.addEndpoint($("#"+id), { anchor:"Center" }, attributeSelectionEndpoint);
    }
}

