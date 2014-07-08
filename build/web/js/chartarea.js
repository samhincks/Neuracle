/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
function ChartArea(id) {
   var selection = id;
   var chart = new Chart();
   var streamChart; 
   this.displayedDL=""; //.. set to currently displayed datalayer
   this.timeToTransition = 200;
   this.transitionLength = 300;
   var added =0;
   
   /*Given a jsonObj packaged as a 2D or 3D datalayer display it in the graph
    *Experiment JSONObj =  A collection of instances = a collection of rows =
    *       an object with a collection of channels, time, and condition. channels indexed by index.
    */
   this.displayChart = function (JSONobj) {
        if(JSONobj.type == "experiment"){
            var channels = JSONobj.instances[0][0].channels;

            //.. add a menu for selecting channel 
            $("#channelSelection").remove();
            $(selection).append("<select id = channelSelection> </select>" );
            
            //.. add each channel as a value to the select menu
            for (var i =0; i < channels.length; i++) {
                $('#channelSelection')
                    .append($('<option>', { value : i })
                    .text(i)); 
            }
            var d3Chart = LineChart();
            
            //.. add event listener for this menu
             var menu = d3.select("#channelSelection")
                  .on("change", function() {
                      var channel = menu.property("value");
                      d3Chart.key(channel);
                      d3Chart(selection);
                      setTimeout(function() {d3Chart.transitionToAverage()},this.timeToTransition);
                      
                      //... should be a copy of what's below
                      setTimeout(function() {d3Chart.transitionScale(this.transitionLength*3.0)}, (this.timeToTransition*2.0)+this.transitionLength);

            });
            
            //.. build the line chart with default width and height and key
            var width = $(selection).width();
            var height = $(selection).height();
            d3Chart.channels(function(d){return d.channels;}).key(11).width(width).height(height); //.. so we show the first channel
            
            //.. the total duration of the area chart swallowing a line, scale to number of instances
            d3Chart.transitionLength(this.transitionLength / JSONobj.instances.length);//..set transition length

            //.. for each instnace (which all begin at 0)
            for (var i=0; i<JSONobj.instances.length; i++) {
                var instance = JSONobj.instances[i]; //.. each instance is an array of rows at diff timestamps

                //.. for each row
                for(var k =0; k < instance.length; k++) {
                    //.. for now show just one channel
                    d3Chart.addRow(instance[k],i);
                }
            }
            //.. instantiate chart, then make it automatically transition to average
            d3Chart(selection);
            setTimeout(function() {d3Chart.transitionToAverage()},this.timeToTransition);
            setTimeout(function() {d3Chart.transitionScale(this.transitionLength*3.0)}, (this.timeToTransition*2.0)+this.transitionLength);
        }
        
        else if(JSONobj.type == "channelset") {
            d3.selectAll('.line-graph').remove(); //.. remove if it exists already
            
            data = JSONobj.data; 
            streamChart = new LineGraph({containerId: 'topRight', data: data});
        }
        
        else if (JSONobj.id == "csrefresh") {
            dataA = JSONobj.data.data; //.. the data contained now should be what's added
            dataA.start = data.start + added;
            dataA.end = data.end + added;
            added += dataA.values[0].length;//dataA.data.length;

            console.log(dataA.start + " , " + dataA.end + " , " + added);
           
           // for each data series ...
            var newData = [];
            data.values.forEach(function(dataSeries, index) {
                // take the first value and move it to the end
                // and capture the value we're moving so we can send it to the graph as an update
                var v = dataSeries.shift();
                dataSeries.push(v);
                // put this value in newData as an array with 1 value
                newData[index] = [v];
            })

            // we will reuse dataA each time
            //dataA.values = newData;
            // increment time 1 step
           // dataA.start = dataA.start + dataA.step;
          //  dataA.end = dataA.end + dataA.step;

            streamChart.slideData(dataA);
        }
       /* else {
            chart.drawLinePlot(JSONobj);
        }*/
        /*
        else {
            console.log(JSONobj.data);
           // data = {"start":1,"end":10000,"step":100,"names":["l"], "values":[[15820.0101840488, 15899.7253668067, 16047.4476816121, 16225.0631734631, 16321.0429563369, 16477.289219996, 16372.5034462091, 16420.2024254868, 16499.3156905815, 16422.1844610347, 16419.7447928312, 16602.0198900243, 16795.2846238759, 16708.9466016093, 16709.8158889291, 16796.7377507963, 16814.8517758747, 16944.4126048633, 16959.6935058422, 17249.8381137218, 17589.8424377422, 17531.9557988989],[15820.0101840488, 15899.7253668067, 16047.4476816121, 16225.0631734631, 16321.0429563369, 16477.289219996, 16372.5034462091, 16420.2024254868, 16499.3156905815, 16422.1844610347, 16419.7447928312, 16602.0198900243, 16795.2846238759, 16708.9466016093, 16709.8158889291, 16796.7377507963, 16814.8517758747, 16944.4126048633, 16959.6935058422, 17249.8381137218, 17589.8424377422, 17531.9557988989],[15820.0101840488, 15899.7253668067, 16047.4476816121, 16225.0631734631, 16321.0429563369, 16477.289219996, 16372.5034462091, 16420.2024254868, 16499.3156905815, 16422.1844610347, 16419.7447928312, 16602.0198900243, 16795.2846238759, 16708.9466016093, 16709.8158889291, 16796.7377507963, 16814.8517758747, 16944.4126048633, 16959.6935058422, 17249.8381137218, 17589.8424377422, 17531.9557988989],[15820.0101840488, 15899.7253668067, 16047.4476816121, 16225.0631734631, 16321.0429563369, 16477.289219996, 16372.5034462091, 16420.2024254868, 16499.3156905815, 16422.1844610347, 16419.7447928312, 16602.0198900243, 16795.2846238759, 16708.9466016093, 16709.8158889291, 16796.7377507963, 16814.8517758747, 16944.4126048633, 16959.6935058422, 17249.8381137218, 17589.8424377422, 17531.9557988989]]};
             dataA = {"start":10,"end":10,"step":1,"names":["l"],"values":[[1],[2],[3],[4]]};

//
            // add presentation logic for 'data' object using optional data arguments
            data["colors"] = ["green", "blue"];
            data["scale"] = "pow";
            
            
           
            setInterval(function() {
               
                // for each data series ...
                var newData = [];
                data.values.forEach(function(dataSeries, index) {
                    // take the first value and move it to the end
                    // and capture the value we're moving so we can send it to the graph as an update
                    var v = dataSeries.shift();
                    dataSeries.push(v);
                    // put this value in newData as an array with 1 value
                    newData[index] = [v];
                })

                // we will reuse dataA each time
                dataA.values = newData;
                // increment time 1 step
                dataA.start = dataA.start + dataA.step;
                dataA.end = dataA.end + dataA.step;

                l1.slideData(dataA);
            }, 100);
            */ 
        
        
        this.displayedDL = JSONobj.id;
    }
    
   
    
    
    /** A lightweight stream of a datalayer. Inside a little information box, display a table
     * describing the channel.
     * Object must have .id , .channels , and .points
     **/
    this.displayDescription = function (JSONobj) {
        //.. remove existing selection and append
        $(selection).children().remove();
        $(selection).append("<div id = descriptionBox class = infoBox> </div>" );
        var description = $("#descriptionBox");
        
        //.. set the text
        var text = "ID: " + JSONobj.id + "  /  CHANNELS: " + JSONobj.channels +"  / POINTS: " + JSONobj.points;
        description.text(text);
        
        this.displayedDL = JSONobj.id;
    }
    
     /** A lightweight stream of a technique
     **/
    this.displayTechniqueDescription = function (JSONobj) {
        //.. remove existing selection and append
        $(selection).children().remove();
        $(selection).append("<div id = descriptionBox class = infoBox> </div>" );
        var description = $("#descriptionBox");
        
        //.. set the text
        var text = "ID: " + JSONobj.id + "  /  TYPE: " + JSONobj.type ;
        if (JSONobj.value != null) text +="  /  VALUE: " +JSONobj.value;
        description.text(text);
        
        this.displayedDL = JSONobj.id;
    }
    
    /**Can be displayed in conjunction with a description. Display a bar chart describing performance
     **/
    this.displayPerformance = function (JSONobj) {
        //.. remove any existing charts
         d3.select(".chart").remove();
         
         //.. instanstiate new home-made D3 chart with width and height to cover selection
         var d3Chart = BarChart();
         var width = $(selection).width();
         var height = $(selection).height();
         d3Chart.addBar(JSONobj);
         d3Chart.minY(0).width(width).height(height-5).maxY(1).key(function(d) {return d.expected;});
         d3Chart(selection); 
         
         ///.. gradually transition from measure of expected performance to actual performance
         setTimeout(function() {d3Chart.transition(function (d) {return d.value}, 1000)},1000);  
    }
}

