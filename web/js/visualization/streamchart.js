/* StreamChart updates moving linegraph in realtime based on changing values in a dtabase 
 */

var StreamChart = function (){
    this.added=1;
    
    this.displayChart = function(JSONobj, chart, data, channelsToShow, vocals){
        
        /** What it wants:
        *  - An object with the following:
        *  .values = each channels value at this timestamp
        *  .start = the first index to displayed on
        *  .end = the last index shown
        *  .maxTime = the last index shown adjusted for seconds
         */
        var channelVals = JSONobj.data.data; //.. the data contained now should be what's added
        var numUpdates = channelVals.values.length;
        var addedThisUpdate = 0;
        var sum = 0; //.. sum of values for all channels added
       
        for (var i=0; i < numUpdates; i++) {
            var updateData = [];
            
            if (channelsToShow != null) {
                var toShow = new Array();
                
                for (var j in channelsToShow) {
                    
                    var val = channelVals.values[i][channelsToShow[j]];
                    toShow.push(val);
                    
                    addedThisUpdate++;
                    sum += val[0] ;
                }
                updateData.values = toShow;
                
            }
            
            else updateData.values = channelVals.values[i];
            updateData.maxTime = channelVals.end + this.added;//channelVals.maxTime;
            updateData.start = this.added;
            updateData.end = channelVals.end + this.added;
            updateData.step =1;
            updateData.names = channelVals.names;
            chart.slideData(updateData);
            this.added++;
            
            
        }
        
        // TODO: make this only happen upon request
        // We update with the mean of the values we pushed in the visualization
        // set to true for adaptive music
       if (vocals) {
        if (addedThisUpdate > 0) {
           var average = sum / addedThisUpdate;
           if (!(audioArea.streaming)) {
               audioArea.createVoice(average);
           }
           else audioArea.updateAudio(average);
        }
       }
            
    }
    
   
}
