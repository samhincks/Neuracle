 
   function getData(control) {
        $('#merge').val(false);
        console.log($("#giver").val());
       // var form = control.form; //.. retrieve the corresponding form
        //.. retrieve the corresponding form, and go to its actionbean with parameters
         var form = $('#content'); 
        //  $.post("<s:url beanclass='stripes.action.DataLayerActionBean' event='getJSON'/>", form.serialize(), drawLineChart); //.. when done, fire receive response
        $.post("http://localhost:8080/TimeMine/DataLayer.action", form.serialize(), drawLineChart);
    }
    
    // Load the Visualization API and the piechart package.
    google.load('visualization', '1.0', {'packages':['corechart']});
   
    //.. Draw a Line Chart, with a SemiSynchedChannel stored as a JSON Object
    //... Additionally, expect each point to have point.SAXChar (probably null)
    //... but provide an entirely new column for each channel-column just for this 
    //... potential annotation
    function drawLinePlotSAX(JSONObj) {
        //.. Initalize title, width height
        var linePlotOptions;

        //.. if we have preset colors, initialize with those (PileDAO makes colors for 3D)
        if (JSONObj.colors)
                linePlotOptions = {title: 'Data', 'width':1200, 'height':500, 
                'backgroundColor': 'beige', 'colors': JSONObj.colors};
        else
            linePlotOptions = {title: 'Data', 'width':1200, 'height':500, 
                'backgroundColor': 'beige'}

            //console.log("A");
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
           // console.log("B");
            
            //.. Add annotiation index to hold SAX string, and emphasis column for neighbors
            linePlotData.addColumn({type:'string',role:'annotation'});            
            linePlotData.addColumn({type:'boolean',role:'emphasis'});
         
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
                linePlotData.addColumn({type:'string',role:'annotation'});
                linePlotData.addColumn({type:'boolean',role:'emphasis'});

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

   

