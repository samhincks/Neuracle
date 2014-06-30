/* Methods for calling and returning from Java calls.
 * Each return method has xhr, which can be evaluated to a JSONobject
 */
function JavaInterface() {
    
    /**Post a user's messge console. */
    this.postToConsole = function() {
        //.. append the machine's response
        var form = $('#consoleForm');  //.. it seems capped at around 100 million characters.
        $.post(address+"/Console.action", form.serialize(), this.returnFromConsole);
    }
    
    /**Having parsed the input and considered a response, post it to console*/
    this.returnFromConsole = function(xhr) {
        var JSONobj = eval('('+ xhr +')'); 
        console.log(JSONobj.content);
        if (JSONobj.error != null)
            consoleArea.displayMessage(JSONobj.error, "systemmess", "redline");
        else if(JSONobj.content != "")
            consoleArea.displayMessage(JSONobj.content, "systemmess", "blueline");
          
        //.. is there some action to complete here? A new dataset to reload
        if(JSONobj.action != null) {
             if (JSONobj.action.id == "reload") {
                javaInterface.postToDataLayers(); //.. just get the names of the datalayers
             }
             
             if(JSONobj.action.id == "reloadT") {
                 console.log("reloading techniques");
                 javaInterface.postToTechniques();
             }

             //.. NO longer in use??
             //.. if this was a highlighting command that required back-end computation 
             if(JSONobj.action.id == "sax") { 
                 chart.drawLinePlot(JSONobj.action.data);
             }
             if(JSONobj.action.id == "csrefresh") {
                 chartArea.displayChart(JSONobj.action);
             }
        }
    }
    
    /**Retrieve all the datalayers and their names*/
    this.postToDataLayers = function() {
        //.. Dev mode: get technqiues first because.. ? I think we want some techniques to be available
        this.postToTechniques(); 
        $.post(address+"DataLayers.action", null, this.returnFromDataLayers);
    }
   
   /**Having retrieved the datalayers, print them to console.
    *Note, we will have a refresh error if the motherless layers don't appear first
    *  */
    this.returnFromDataLayers = function(xhr) {
        var JSONobj = eval('('+ xhr +')'); 
        if (JSONobj.error != null)
            consoleArea.displayMessage(JSONobj.error, "systemmess", "redline");
       
        //.. no error, then render the object datalayers
        else {
            console.log("l : " +JSONobj.datalayers.length);
            datalayerArea.addDatalayers(JSONobj.datalayers);
            reinit(); //.. relaod the drag/drop properties
        }
    }
     /**Retrieve all techniques and their names*/
    this.postToTechniques = function() {
       $.post(address+"/Techniques.action", null, this.returnFromTechniques);
    }
    
    /*Return from techniques*/
    this.returnFromTechniques = function(xhr) {
        var JSONobj = eval('('+ xhr +')'); 
        if (JSONobj.error != null)
            consoleArea.displayMessage(JSONobj.error, "systemmess", "redline");
        
        //.. no error, then render the object datalayers
        else {
            datalayerArea.addTechniques(JSONobj.techniques);
            reinit();

        }
    }
    
    /**Having selected an element, get a stream of its data to display it. */
    this.postToDataLayer = function() {
        $('#merge').val(false); //.. so that we don't call a method that merges datalayers'
        $('#stats').val(false); 
        var form = $('#content'); 
        $.post(address+"DataLayer.action", form.serialize(), this.returnFromDataLayer);
    }
    
    
    /** Stream the JSON object that's been generated in the java backend*/
    this.returnFromDataLayer = function(xhr) {
        //$(".topRight").empty(); //.. clear any existing graph
        var JSONobj = eval('('+ xhr +')');  

        if (JSONobj.error != null)
            consoleArea.displayMessage(JSONobj.error, "systemmess", "redline");
        
        else { //.. no error
            chartArea.displayChart(JSONobj);
        }
    }
    
    /**Given the state of our technique connections as known by plumb, 
    push this information to the relevant bean**/ 
    this.postConnectionsToTechnique= function(){
        //1.. Get connections that pertain to techniques as known by plumb
        var c = plumbTechniques.getTechniqueConnections();
        var postArray = new Array();
        
        //2.. Put these connections in the format Java wants them 
        for (var i in c) {
            var l = c[i];
            var stString = l.sourceId +":"+l.targetId;
            postArray[i] = stString;
        }
        
        //.. also get intersections by overlap as detected in datalayerarea
        var overlaps = datalayerArea.getIntersectedTechniques();
        for (var i in overlaps){
            var ol = overlaps[i];
            console.log(ol);
            var conn = ol.sourceId +":"+ol.targetId;
            postArray.push(conn);
        }
        
        
        //3.Save them to TechniqueForm; wait for the console to pass them
        $("#connections").val(postArray);
    }
    
    /* Post to DataLayersStats ActionBean, retrieve a JSON object describing classification-stats.
     * Should only work if the selected target is an experiment
     */
    this.getDataLayerStats = function() {
        //.. Do nothing if it is already being display
        if ($("#giver").val() == chartArea.displayedDL) return;
       //... otherwise, post as a stats request
        var form = $('#content'); 
        $("#stats").val(true);
        $("#merge").val(false);
        $.post(address+"DataLayer.action", form.serialize(), this.returnFromDataLayerStats);
    }
    
    /*Display descriptions and statistics for a datalayer 
     **/
    this.returnFromDataLayerStats = function(xhr) {
        var JSONobj = eval('('+ xhr +')'); 
        if (JSONobj.error != null) //.. we give errors here but not for technique because of teh way we retrieve Dataset performances
            consoleArea.displayMessage(JSONobj.error, "systemmess", "redline");
        
        else if (JSONobj.description != null) {
             chartArea.displayDescription(JSONobj.description);
        }
       
       //.. otherwise no error, so we display the graph
        if (JSONobj.performance != null) {
            chartArea.displayPerformance(JSONobj.performance);
        }
    }
   
   /* Post to Techniques ActionBean, retrieve a JSON object describing classification-stats.
     */
    this.getTechniqueStats = function() {
       var form = $('#techniques'); 
       $("#techniqueStats").val(datalayerArea.techniques.getSelected());
       $.post(address+"Technique.action", form.serialize(), this.returnFromTechniqueStats);
    }
    
    /**With a JSONObj, holding the summed performance of this technique, create a bar chart
     * with an expected value and an actual value*/
     this.returnFromTechniqueStats = function(xhr) {
         var JSONobj = eval('('+ xhr +')'); 
         
         if(JSONobj.description != null)
             chartArea.displayTechniqueDescription(JSONobj.description);
         
         if(JSONobj.performance != null)
            chartArea.displayPerformance(JSONobj.performance);
         
    }
    
}
