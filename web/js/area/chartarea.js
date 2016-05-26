/* Javascript logic for the area denoted as #topRight. This inititiates D3 charts
 * in the visualization folder, in response to JSONObjects from the server*/
function ChartArea(id, descArea) {
   var selection = id;
   var streamChart; 
   this.displayedDL=""; //.. set to currently displayed datalayer
   this.timeToTransition = 200;
   this.transitionLength = 300;
   this.singleTransition; //.. total transition / each line
   var lastGraph = "none";
   this.lastJSON; //.. we might wnat to redraw the graph, for instance if we resize the graph
   var border =20;
   var sc = new StreamChart();
   var channelsToShow = [1,2];//=[0,1]; //.. an array of 2D channels to show (streaming or not streaming), which we change by view.show()
   //.. changed in events when we alter channel selection by slope display
   this.setChannelsToShow= function(index, val) {
       channelsToShow[index] = val;
   }
   /*Given a jsonObj packaged as a 2D or 3D datalayer display it in the graph
    *Most JSONObj's returned are straightforward. Experiment is tricky: it's   A collection of instances =
    *    a collection of rows = an object with a collection of channels, time, and condition. channels indexed by index.
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
            if(JSONobj.data.classifiers==null)//this.displayClassificationSet(JSONobj); //. I like this one more for now
                this.displayChannelSet(JSONobj); //.. this one can be streamed
            else  this.displayClassificationSet(JSONobj);//.. this one displays conditions
        }
        
        else if(JSONobj.type == "correlation") {
           this.displayCorrelation(JSONobj);
        }

        else if (JSONobj.id == "csrefresh") { 
           classifier.addData(JSONobj);
           sc.displayChart(JSONobj, streamChart, data, channelsToShow);
        }
        this.displayedDL = JSONobj.id;
    }
    
    
    this.displayCorrelation = function(JSONobj) {
        $(selection).children().remove();
        var width = $(selection).width() - border;
        var height = $(selection).height() - border;
        var corChart = CorrelationMatrix();
        corChart.data(JSONobj.data).width(width).height(height)(selection);
    }
    
    this.displayClassificationSet = function(JSONobj) {
        $(selection).children().remove();
        d3.selectAll('.line-graph').remove(); //.. remove if it exists already
        data = JSONobj.data;
        var actualMaxPoints = JSONobj.actualNumPoints;
        var readingsPerSec = JSONobj.readingsPerSec;
        var maxInSeconds = (actualMaxPoints / readingsPerSec);
        data["maxTime"] = maxInSeconds;
        var width = $(selection).width() - border;
        var height = $(selection).height() - border;
        
        var chart = ClassificationChart();
        chart.maxTime(maxInSeconds).width(width).height(height).data(data,true)(selection);

        //this.writeMarkerVals(JSONobj.data.markerNames);
    }
    this.displayChannelSet = function(JSONobj) {
        $(selection).children().remove();
        d3.selectAll('.line-graph').remove(); //.. remove if it exists already
        data = JSONobj.data;
        
        //.. potentially, initialize a classifier too if we're interested in realtiem front end statistics
        classifier.initialize(channelsToShow); //.. watch out! now we reinitialie every time it presses
        
        //.. if we've called view.show, then restrict channels to show
        if (channelsToShow != null) {
            var toShow = new Array();
            for (var i in channelsToShow){ 
                toShow.push(data.values[channelsToShow[i]]);
                //console.log(channelsToShow[i], JSONobj);
            }
            data.values = toShow;
        }
        //console.log(data);
        
        var actualMaxPoints = JSONobj.actualNumPoints;
        var readingsPerSec = JSONobj.readingsPerSec;
        var maxInSeconds = (actualMaxPoints / readingsPerSec);
        data["maxTime"] = maxInSeconds;
        streamChart = new LineGraph({containerId: 'topRight', data: data});
        //this.writeMarkerVals(JSONobj.data.markerNames);
    }
    
    this.show2DIndexes = function(params) {
        channelsToShow = new Array();
        channelsToShow = params;
    }
    this.writeMarkerVals = function(JSONarr) {
        var msg = "";
        for (var k =0; k <JSONarr.length; k++) {
            var arr2 = JSONarr[k];
            for (var i =0; i < arr2.length; i++) {
                var ob = arr2[i];
                msg += ob.condition + " : " + ob.index + "  | ";
            }
            consoleArea.displayMessage(msg);
            msg ="";  
        }
        
    }
    
    this.displayExperiment = function(JSONobj) {
        $(selection).children().remove();
        var channels = JSONobj.channelnames; 

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
                    d3Chart(selection); });

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
    

    /*Display predictions in the prediction chart */
    this.displayPredictions =function(JSONobj){
        $(selection).children().remove();
        
        //.. extract parameters from the JSON obj
        var width = $(selection).width() - border;
        var height = $(selection).height() - border;
        var everyK = JSONobj.every;
        var length = JSONobj.length;
        var data = JSONobj.predictions;
        var classes = JSONobj.classes.values;
        
        //.. Instantiate the chart setting relevant parameterrs
        var chart = PredictionChart();

        chart.data(data).width(width).height(height).instance(everyK, length).classes(classes)(selection);
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
    
   
    /** Currently a hidden feature, but the system supports extracting a displaying the various
     * frequencies of an experiment
     **/
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
    
    //.. Many of the test functions are inteh visualization object
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

