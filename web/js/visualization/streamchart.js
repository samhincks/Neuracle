/* StreamChart updates moving linegraph in realtime based on changing values in a dtabase 
 */

var StreamChart = function (){
     this.added=1;
    this.displayChart = function(JSONobj, chart, data, channelsToShow){
        /** What it wants:
        *  - An object with the following:
        *  .values = each channels value at this timestamp
        *  .start = the first index to displayed on
        *  .end = the last index shown
        *  .maxTime = the last index shown adjusted for seconds
         */
        var channelVals = JSONobj.data.data; //.. the data contained now should be what's added
        var numUpdates = channelVals.values.length;
            
        for (var i=0; i < numUpdates; i++) {
            
            var updateData = [];
            if (channelsToShow != null) {
                var toShow = new Array();
                for (var j in channelsToShow) {
                    toShow.push(channelVals.values[i][channelsToShow[j]]);
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
    }
}
