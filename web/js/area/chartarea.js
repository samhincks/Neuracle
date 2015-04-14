/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
function ChartArea(id, descArea) {
   var selection = id;
   //var chart = new Chart();
   var descriptionArea = descArea;
   var streamChart; 
   this.displayedDL=""; //.. set to currently displayed datalayer
   this.timeToTransition = 200;
   this.transitionLength = 300;
   this.singleTransition; //.. total transition / each line
   var added =0;
   var lastGraph = "none";
   this.lastJSON; //.. we might wnat to redraw the graph, for instance if we resize the graph
   var border =20;
   var sc = new StreamChart();
   
   /*Given a jsonObj packaged as a 2D or 3D datalayer display it in the graph
    *Experiment JSONObj =  A collection of instances = a collection of rows =
    *       an object with a collection of channels, time, and condition. channels indexed by index.
    */
   this.displayChart = function (JSONobj) {
        this.lastJSON = JSONobj; //.. remember what is written for zoom
        lastGraph = JSONobj.type;
        
        if (JSONobj.predictions != null) {
            this.displayPredictions(JSONobj);
        }
        
        if(JSONobj.type == "experiment"){
            this.displayExperiment(JSONobj);
        }
        
        else if(JSONobj.type == "channelset") {
            this.displayChannelSet(JSONobj);
        }
        
        else if(JSONobj.type == "correlation") {
            this.displayCorrelation(JSONobj);
        }

        else if (JSONobj.id == "csrefresh") {
             sc.displayChart(JSONobj, streamChart, data);
        }
        this.displayedDL = JSONobj.id;
        descriptionArea.displayedDL = this.displayedDL;
    }
    this.displayCorrelation = function(JSONobj) {
        $(selection).children().remove();
        var width = $(selection).width() - border;
        var height = $(selection).height() - border;
        var corChart = CorrelationMatrix();
        corChart.data(JSONobj.data).width(width).height(height)(selection);
    }
    
    this.displayChannelSet = function(JSONobj) {
        $(selection).children().remove();
        d3.selectAll('.line-graph').remove(); //.. remove if it exists already
        data = JSONobj.data;
        var actualMaxPoints = JSONobj.actualNumPoints;
        var readingsPerSec = JSONobj.readingsPerSec;
        var maxInSeconds = (actualMaxPoints / readingsPerSec);
        data["maxTime"] = maxInSeconds;
        streamChart = new LineGraph({containerId: 'topRight', data: data});
    }
    this.displayExperiment = function(JSONobj) {
        $(selection).children().remove();
        var channels = JSONobj.channelnames; ///JSONobj.instances[0][0].channels;

        var actualMaxPoints = JSONobj.actualNumPoints;
        var readingsPerSec = JSONobj.readingsPerSec;
        var maxInSeconds = (actualMaxPoints / readingsPerSec)

        //.. add a menu for selecting channel 
        $("#channelSelection").remove();
        $(selection).append("<select id = channelSelection> </select>");
        //.. add each channel as a value to the select menu
        for (var i = 0; i < channels.length; i++) {
            $('#channelSelection')
                    .append($('<option>', {value: i})
                            .text(channels[i]));
        }
        var d3Chart = LineChart();
        var self = this;

        //.. add event listener for this menu
        var menu = d3.select("#channelSelection")
                .on("change", function() {
                    var channel = menu.property("value");
                    d3Chart.key(channel);
                    d3Chart(selection);

                });

        //.. build the line chart with default width and height and key
        var width = $(selection).width() - border;
        var height = $(selection).height() - border;
        d3Chart.channels(function(d) {
            return d.channels;
        }).key(0).width(width).height(height).maxTime(maxInSeconds); //.. so we show the first channel

        //.. the total duration of the area chart swallowing a line, scale to number of instances
        this.singleTransition = this.transitionLength / JSONobj.instances.length;
        d3Chart.singleTransitionLength(this.singleTransition);//..set transition length
        d3Chart.transitionLength(this.transitionLength);

        //.. for each instnace (which all begin at 0)
        for (var i = 0; i < JSONobj.instances.length; i++) {
            var instance = JSONobj.instances[i]; //.. each instance is an array of rows at diff timestamps

            //.. for each row
            for (var k = 0; k < instance.length; k++) {
                //.. for now show just one channel
                d3Chart.addRow(instance[k], i);
            }
        }

        //.. instantiate chart, then make it automatically transition to average
        d3Chart(selection);

        //.. when user clicks space the chart transitions
        window.onkeyup = function(e) {
            var key = e.keyCode ? e.keyCode : e.which;
            if (lastGraph == "experiment") {
                if (key == 16) {
                    if (!(d3Chart.hasTransitioned)) {
                        d3Chart.transitionToAverage();
                        setTimeout(function() {
                            d3Chart.transitionScale()
                        }, (self.timeToTransition + self.transitionLength));
                    }
                }
            }
        }
    }
    

    /*This cant be plain old line, becaues then it wont work for multiple classes. But we could
     * alter the opacity. We could draw little dots that were color based on prediction. They would
     * be green dark if confident and correct. And light red if confident and wrong. Hovering over
     * them would reveal the actual confidence. You would receive, at a glance, how often predictions
     * were made, then perhaps you could hover over to see how far back that prediction applied*/
    this.displayPredictions =function(JSONobj){
        $(selection).children().remove();
        var width = $(selection).width() - border;
        var height = $(selection).height() - border;
        var chart = PredictionChart();
        var everyK = JSONobj.every;
        var length = JSONobj.length;
        var data = JSONobj.predictions;
        var classes = JSONobj.classes.values;
        chart.data(data).width(width).height(height).instance(everyK, length).classes(classes)(selection);
    }
    
    /** A lightweight stream of a datalayer. Inside a little information box, display a table
     * describing the channel.
     * Object must have .id , .channels , and .points
     **/
    this.displayDescription = function (JSONobj) {
        //.. remove existing selection and append
        //$(selection).children().remove();
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
    
   
    
    this.displayFrequency = function(JSONobj, JSONdescription) {
        //.. remove any existing charts
        d3.select(".chart").remove();
        //.. instanstiate new home-made D3 chart with width and height to cover selection
        var d3Chart = FreqBarChart();
        var width = $(selection).width();
        var height = $(selection).height();
        
        //... TODO . Make so that each is a different frequency. 
        for (var i=0; i< JSONobj.length; i++){
            d3Chart.addBar(JSONobj[i]);
        }
        d3Chart
            .minY(0).width(width).height(height - 5).maxY(JSONdescription.max)
            .key(function(d) { return d.expected;})
            .numConditions(JSONdescription.numConditions)
            .frequencies(JSONdescription.frequenciesX);
        
        d3Chart(selection);
        
        
        ///.. gradually transition from measure of expected performance to actual performance
        setTimeout(function() {
            d3Chart.transition(function(d) {
                return d.value
            }, 1000)
        }, 1000); 
    }
    
     this.testCharts = function() {
        var test = "prediction";
        if (test == "prediction") {
            var chart = PredictionChart();
            var obj = chart.getTestData();
            this.displayChart(obj);
        }
            
    }
    //this.testCharts();

    
}

