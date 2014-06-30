/* Google Charts object; governed by a JSONObj.
 * 
 */

function Chart() {
     google.load('visualization', '1.0', {'packages':['corechart']}); //.. very important where this is placed
     var linePlot;
     var linePlotData;
     var numRows;
     var options;
     
     //.. Constants
     var TIME =0;
     var VAL = 1;
     var ANNO =2;
     var EMPH =3;
     var SCOPE =4;
     var COLSPERCHANNEL = 4; //.. but first channel has 5 columns (also has time) 

    // var container = document.createElement("lineplot_div");
    // console.log(container);
     //parent.appendChild(container);

     
     /**Draw a Line plot from the specifications of a JSONObj (defined in the DAOs)*/
     this.drawLinePlot = function (JSONObj) {
          options =  {
                title: 'Data', 
                'width':"100%", 
                'height':"100%", 
                'backgroundColor': 'ghostwhite',
                animation:{
                    duration: 1000,
                    easing: 'in'
                  }
            };
         this.buildTable(JSONObj.channels); //.. set data to the table built from this json obj
         this.drawPlot();
     }
     
     /**Set values to one, triggering an animation*/
     this.setDataToOne = function() {
         var tempData = linePlotData.clone();
         //.. set values to 0
         for (var i = 0; i<linePlotData.getNumberOfRows(); i++) {
             tempData.setCell(i, 1, 1); //..  set the value numeric value to 1 in first channel
         }
        
        //.. Draw it out
        linePlot.draw(tempData, options);
     }
     this.reset = function () {
        linePlot.draw(linePlotData, options);
     }
     
     /* Draw the plot, havining initialized the table/
      * Draw it inside chart area by appending a div called lineplot_div to it */
     this.drawPlot = function() {
         //.. select containing element and remove all children
        var chartContainer = $("#topRight");
        $(".chart").remove();
        
        //.. make a new element
        chartContainer.append("<div id = lineplot_div class = chart> </div>" );
       
       //.. make a new line chart visualization out of it
       linePlot = new google.visualization.LineChart(document.getElementById("lineplot_div"));
       linePlot.draw(linePlotData, options);
     }
     
     /**Build the Google-table which is used to populate the visualizition*/
     this.buildTable = function(channels) {
         linePlotData = new google.visualization.DataTable();
         var timestamps = this.setFirstRowAndTimestamps(channels[0]);
         this.setRemainingRows(channels, timestamps);
     }
     
     /**Setup basic structure of Table 
      * Timestamp, Value, SAX, Emphasis, Certainty, Scope
      **/
     this.setFirstRowAndTimestamps = function(firstChannel) {
        var firstData = firstChannel.data;
        this.setColumns(firstChannel.id, true);    

        //.. calculate how many readings this will give, and add that many rows
        numRows = firstData.length;
        linePlotData.addRows(numRows); //.. ho

        //.. this array holds the timestamps from the first array
        var timestamps = new Array();

        //.. Add each entry to the table. Since this is the first channel,
        //... also set the timestamp for successive channels
        for (var i =0; i <firstData.length; i++) {
            var point = firstData[i];
            linePlotData.setCell(i, 0, point.timestamp);
            linePlotData.setCell(i, 1, point.value);
            
            //.. set SAXChar if its not null
            if (point.SAXChar != null){
                var sax = String(point.SAXChar);
                linePlotData.setCell(i, 2, sax); //.. Set certainty to true
            }
                
            //.. set emphasis to true if its not null
            if (point.scope != null) {
                linePlotData.setCell(i, 3, true);
            }
            
            if(point.conditionIndex != null) {
                if (point.conditionIndex %2 ==1) {
                    linePlotData.setCell(i, 4, false)
                }
            }
               
            timestamps[i] = point.timestamp; 
        }
        return timestamps;
     }
     
     /**Set up the columns for a given channel*/
     this.setColumns = function(channelId, isFirst) {
        //... The First column represents the X AXIS, the values placed in x axis
        if (isFirst)
            linePlotData.addColumn('number', 'X');
        linePlotData.addColumn('number', channelId);
            
        //.. Add annotiation index to hold SAX string, and emphasis column for neighbors
        linePlotData.addColumn({
            type:'string',
            role:'annotation'
        });     
        
        linePlotData.addColumn({
            type:'boolean',
            role:'emphasis'
        });
        
        linePlotData.addColumn({
            type:'boolean',
            role:'scope'
        });   
     }
     
     /**Add the remaining channels to the data-set. Handle complexities like some channels
      *being offset in time */
     this.setRemainingRows = function(channels, timestamps) {
         for (var j = 1; j < channels.length; j++) {
            this.setColumns(channels[j].id, false);
            var data = channels[j].data;                   

            //.. Increment xIndex, where we write our cell, to where this channels first timestamp is                    
            var xIndex =0;
            while (xIndex < timestamps.length && data[0].timestamp != timestamps[xIndex] ) {
                xIndex = xIndex +1;
            } 

            //.. If this channel starts giving new timestamps, we're gonna write to column0
            var settingTimestamp = false;

            //.. Set an (x,y) pair for each entry
            for (var i =0; i <data.length; i++) {
                var point = data[i]; 

                //.. if we've exceeded the amount already written rows, add new ones
                if ( xIndex == numRows) {
                    settingTimestamp = true;
                    var entriesLeft = data.length-i;
                    linePlotData.addRows(entriesLeft);
                    numRows = numRows + entriesLeft;
                }
                
                //.. Calculate where to place the columns
                var LASTCOL = linePlotData.getNumberOfColumns()-1; //.. max columns
                var VALINDEX = LASTCOL - COLSPERCHANNEL + VAL;
                var ANNOINDEX = VALINDEX +1;
                var EMPHINDEX = ANNOINDEX +1;
                var SCOPEINDEX = EMPHINDEX +1;
                
                //..write out the y=value
                linePlotData.setCell(xIndex, VALINDEX, point.value); 
               
               //.. add SAX annotation and emphasis values
                if (point.SAXChar !=null)
                    linePlotData.setCell(xIndex, ANNOINDEX, point.SAXChar); //.. SAX modifier 
                
                if (point.scope != null) {
                    linePlotData.setCell(xIndex, EMPHINDEX, true);
                }
              
                if(point.conditionIndex != null) {
                    if (point.conditionIndex %2 ==1) {
                        linePlotData.setCell(i, SCOPEINDEX, false)
                    }
                }
                
                //.. if this is a 'new cell' just for this channel
                if (settingTimestamp) {
                    linePlotData.setCell(xIndex, TIME, point.timestamp);
                }

                //.. increment position
                xIndex = xIndex +1;
            }
        } 
     }
    /*Draw a Line Chart, with a SemiSynchedChannel stored as a JSON Object
    ... Additionally, expect each point to have point.SAXChar (probably null)
    ... but provide an entirely new column for each channel-column just for this 
     potential annotation */ 
     this.drawLinePlot2 = function (JSONObj) {

        //.. Initalize title, width height
        var linePlotOptions;
        //.. if we have preset colors, initialize with those (PileDAO makes colors for 3D)
        if (JSONObj.colors)
            linePlotOptions = {
                title: 'Data', 
                'width':1200, 
                'height':500, 
                'backgroundColor': 'white', 
                'colors': JSONObj.colors
                };
        else
            linePlotOptions = {
                title: 'Data', 
                'width':1200, 
                'height':500, 
                'backgroundColor': 'beige'
            }

        //.. Draw object to lineplot_div element
        var linePlot = new google.visualization.LineChart(document.getElementById('lineplot_div'));
            
        // Create a new DataTable
        var linePlotData = new google.visualization.DataTable();

        //... The First column represents the X AXIS, the values placed in x axis
        linePlotData.addColumn('number', 'X');

        //.. Retrieve channels from JSON obj, open first channel, and add it to table
        var channels = JSONObj.channels;                 
        var firstData = channels[0].data;
        linePlotData.addColumn('number', channels[0].id);
            
        //.. Add annotiation index to hold SAX string, and emphasis column for neighbors
        linePlotData.addColumn({
            type:'string',
            role:'annotation'
        });     
        
        var emphasisColumn =3;
        var numEncodings = 3;
       
       
       linePlotData.addColumn({
            type:'boolean',
            role:'emphasis'
        });
        linePlotData.addColumn({
             type:'boolean',
             role:'certainty'
         });     
       
         
        //.. calculate how many readings this will give, and add that many rows
        var numRows = firstData.length;
        linePlotData.addRows(numRows); //.. ho

        //.. this array holds the timestamps from the first array
        var timestamps = new Array();

        //.. Add each entry to the table. Since this is the first channel,
        //... also set the timestamp for successive channels
        for (var i =0; i <firstData.length; i++) {
            var point = firstData[i];
            linePlotData.setCell(i, 0, point.timestamp);
            linePlotData.setCell(i, 1, point.value);
               
            //.. set SAXChar if its not null
            if (point.SAXChar != null){
                var sax = String(point.SAXChar);
                linePlotData.setCell(i, 2, sax); //.. Set certainty to true
            }
                
            //.. set emphasis to true if its not null
            if (point.scope != null) {
                linePlotData.setCell(i, 3, true);
            }
            
            if(point.conditionIndex != null) {
                var conditionIndex = point.conditionIndex % numEncodings;
                var columnToModify = emphasisColumn -1 + conditionIndex; //.. since its not zero-based
               //.. for each possible condition set a modified lineplot role
                if(conditionIndex !=0){
                                    console.log("setting" + columnToModify);
                   if(columnToModify ==emphasisColumn)
                      linePlotData.setCell(i, columnToModify, true);
                   else 
                        linePlotData.setCell(i, columnToModify, false);
                }
                
            }
               
            timestamps[i] = point.timestamp; 
        }
        // console.log("C");

        //.. Having added one channel, add the rest (if there are any)
        //... If there are more than 1 channel, JSON Obj gave us a 
        //... SemiSynchedChannel which is guaranteed to have the channel 
        //... with the earliest timestamp in position 0, and the latest timestamp
        //... in the last position (unless this is also the first)
        for (var j = 1; j < channels.length; j++) {
            //.. Add this channel to datatable
            var data = channels[j].data;                   
            linePlotData.addColumn('number', channels[j].id);

            //.. Add annotation and emphasis columns for SAX and nearest neighbors
            linePlotData.addColumn({
                type:'string',
                role:'annotation'
            });
            linePlotData.addColumn({
                type:'boolean',
                role:'emphasis'
            });

            //.. Increment xIndex, where we write our cell, to where this channels first timestamp is                    
            var xIndex =0;
            while (xIndex < timestamps.length && data[0].timestamp != timestamps[xIndex] ) {
                xIndex = xIndex +1;
            } 

            //.. If this channel starts giving new timestamps, we're gonna write to column0
            var settingTimestamp = false;

            //.. Set an (x,y) pair for each entry
            for (var i =0; i <data.length; i++) {
                var point = data[i]; 

                //.. if we've exceeded the amount already written rows, add new ones
                if ( xIndex == numRows) {
                    settingTimestamp = true;
                    var entriesLeft = data.length-i;
                    linePlotData.addRows(entriesLeft);
                    numRows = numRows + entriesLeft;
                }
                var colIndex = linePlotData.getNumberOfColumns()-1;
                
                //..write out the y=value
                linePlotData.setCell(xIndex, colIndex-2, point.value);                        
                    
                //.. add SAX annotation and emphasis values
                linePlotData.setCell(xIndex, colIndex -1, point.SAXChar); //.. SAX modifier 
                if (point.scope != null) {
                    linePlotData.setCell(xIndex, colIndex, true);
                }
                    
                //.. if this is a 'new cell' just for this channel
                if (settingTimestamp) {
                    linePlotData.setCell(xIndex, 0, point.timestamp);
                }

                //.. increment position
                xIndex = xIndex +1;
            }
        } 
        // console.log("D");
        //.. draw it all out!
        linePlot.draw(linePlotData, linePlotOptions);
    }
    
}
