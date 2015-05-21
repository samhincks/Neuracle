/* Sam Hincks; hacked from http://bl.ocks.org/mbostock/3903818#index.html
 * A LineChart that merges into an areachart
 *
 *Seemingly a powerful programming pattern; 
 *Instantiate: var chart = TSChart();
 *Configure & Reconfigure: chart.width(200);
 *Reconfigure: chart();
 *Transition to different dimension = chart.dimension(x).transition();
 *Show area chart = chart.transitionToAverage
 *
*/

function LineChart() {
    /**PUBLIC VARIABLES**/
    this.hasTransitioned = false;
    
    /**CONFIGURATION VARIABLES**/
    //.. width, height, margin. If these are customizable we need settors 
    var width = 700, 
        height = 400,
        margin = {top:30, left: 60, right: 30, bottom: 20 };

    var maxTime =-1;
    
    //.. if this is true we set min and max to be the minimimum or maximum found in the area chart
    //... otherwise it is the min and max in the raw data
    var setHeightByAreaChart = false; 
    
    var key = 0; //.. the set of elements in view. If its an array it's acceptable to use index position
    var channels = function(d) {return d;}; //.. function for retrieving the numeric data from input-data, by default we want row.ch1, row.ch2. But maybe we want to store data in an array
    var selection;// = d3.select("body").append("svg:svg");
    
    /**Palletton colors 
     * ["#0A0D46",  "#004624", "#653800"]
     * **/
    var color = d3.scale.category10();  /*d3.scale.ordinal()
        .domain([0, 1, 2])
        .range(["#6699CC", "#BDAEAC"]);*/ //.. Blue and brown, for Tufts publications
    color = d3.scale.ordinal()
            .range(["#31A354", "#3182BD", "#756BB1", "#636363", "#637939", "#7B4173"]);
    var max, min; //.. the maximum and minimum values of the dataset
    
    
    /**PRIVATE VARIABLES*/
    //.. computed from data and configuration
    var svg,
        y,x,//.. the actual chart container
        line;//.. a function for computing 
   
   /* Data is an array of instances, where each instance belongs to a particular condition and starts at 0
    * Each instance is a collection of rows, where each row has a unique timestamp.
    * Each row has a timestamp, a condition (which decides color), and some collection of channels. 
    */
    var data; //.. Each row an object with arbitrary number of channels at a common timestamp
    
    //... For the area chart that represents mean of lines of same class
    var area;
    var meanline;
    var lineTrans =2000;
    var chartTrans = 2000;
    var transitionLength =2000; //.. the length of a scale transition
    var drawn; //.. used in transition to average to store averages and standard deviations
    var xAxis;
    var yAxis;
    
    //..  The chart itself; sets everything after a fresh customization   
    //... input: selection, defaults to body
    function chart(s) {
        if (data == null) return;
 
        //.. if there is an optional parameter of selection
        if (arguments.length) {
            d3.selectAll('.chart').remove(); //.. remove if it exists already
            selection = d3.select(s).append("svg:svg");
        }
        else selection = d3.select("body").append("svg:svg");
        selection.attr("id", "areachart");
        
        try {channels(data[0][0])[key];}catch(error){ throw("Specify proper keys and channel functions for accessing data")};
        
        if (!(setHeightByAreaChart)){
            min = this.getMin(key);
            max = this.getMax(key);
        }
        
        //.. calculate desired scales
        y= d3.scale.linear()
            .domain([min,max]) //.. doubly nested since a 2d array (too expensive?)
            .range([height - margin.top, 0 + margin.bottom]); //.. these need to be flipped because svg's are anchored at topleft
        x= d3.scale.linear()
            .domain([0, this.getLongest(key)])
            .range([0 + margin.left, width - margin.right]);  
       
        //.. if we've already initialized this chart, we must clear it
        if (typeof(svg) == "object") {
            d3.select(".chart").remove();
        }
        //.. make our chart... does it make sense to reinstantiate chart each time? can we still do transition
        svg = selection
            .attr("class","chart")
            .attr("width",width)
            .attr("height", height);
        
        //.. Add a path element to our visualization. USe D3's line'
        line = d3.svg.line()
            .interpolate("basis") //.. makes jagged smooth
            .x(function(d,i) {
               return x(i)
             }) 
            .y(function(d) {
                return y(channels(d)[key]);
            });

        var tip = d3.tip()
                .attr('class', 'd3-tip')
                .offset([-10, 0])
                .html(function(d, i) {
                    return "<strong> " + d[0].condition + " : " +d[0].index+ "</strong>";
                })
        svg.call(tip);
        
        //... now I want to create a collection of path elements using datum hack
        //.. ERROR BENEATH
        for (var i=0; i < data.length; i++ ) {
             svg.append("path")
                .datum(data[i]) //.. by doing datum we reserve the right to alternate which dimension of data is shown
                .attr("class", "d3line")
                .attr("d", line)
                .attr("id", "instance"+i)
                .on("mouseover", tip.show)
                .on("mouseout", tip.hide)
                .style("stroke", function(d) {
                    return color(d[0].condition); //.. assume all have same condition
                 });
        }

       //.. the tick of the x axis, only set it if we max time has been set
        var x2;
        if (maxTime <0) x2=x;
        else x2 = d3.scale.linear()
                .domain([0, maxTime])
                .range([0 + margin.left, width - margin.right]);  
        
        //.. Add an x axis
        xAxis = d3.svg.axis()
            .scale(x2) //.. use same scaling function as for x
            .orient("bottom");
            
        svg.append("g")
            .attr("class", "x axis")
            .attr("transform", "translate(0," + (height - margin.bottom) + ")") //.. 0,0 refers to 0,height
            .call(xAxis);
        

       //.. Add a y axis
       yAxis = d3.svg.axis()
           .scale(y)
           .orient("left");
       
       svg.append("g")
           .attr("class", "y axis")
           .attr("transform", "translate(" + (margin.left-5) + ",0)")
           .call(yAxis);
        chart.hasTransitioned = false;

        return chart;
    }
    
    
    //.. Having swapped the key, transition to the new element
    chart.transition = function() {
        //.. handle case, where we've cluttered our screen with area chart and removed our lines
       // if (svg.selectAll(".avgArea")[0].length)
         //   chart();
        var t0 = svg.transition().duration(750);
        t0.selectAll(".line").attr("d", line);
        return chart;
    }   
    
    chart.singleTransitionLength = function(_) {
        chartTrans = _;
        lineTrans = _;
    }
    chart.transitionLength = function(_) {
        transitionLength = _;
    }
    /**-------------PUBLIC DATA FUNCTIONS----------**/
    //.. Returns the minimum value in all arrays at the specified key
    this.getMin = function(key) {
        return d3.min(data, 
            function(d) {
                return d3.min(d, function(d){
                    return channels(d)[key];})});
    }
    
    //.. returns the maximum value in all arrays at the specified key
    this.getMax = function(key) {
        return d3.max(data, 
            function(d) {
                return d3.max(d, function(d){
                    return channels(d)[key];})});
    }
    
    
    //.. returns the length of the longest array
    this.getLongest = function () {
        return d3.max(data, function(d) { return d.length;});
    }
   
    /***---------CHART CONFIGURATION***/
    //.. If we have updated what dimension of the data we are showing, call transition to move to this data
    //.. Set key to be the group-name of the elements we want to view
    chart.key = function(_){
        if (!arguments.length) return key; //.. this notation gives a get for free
        key = _;
        return chart;
    }
    
    //.. set a function for retrieving the channels from each row object
    chart.channels = function(_) {
          if (!arguments.length) return channels; //.. gettor method hack
          channels = _;
          return chart;
    }
    
    //.. set the width of the chart
    chart.width = function(_) {
        if (!arguments.length) return width;
        width = _;
        return chart;
    };
    
    //.. set the height of the chart
    chart.height = function(_) {
        if (!arguments.length) return height;
        height = _;
        return chart;
    };
    
    //.. set the margins
    chart.margin = function(_) {
        if (!arguments.length) return margin;
        margin =_;
        return chart;
    }
    
    chart.maxTime = function(_) {
        if (!arguments.length) return maxTime;
        maxTime = _;
        return chart;
    }
    
    
    
    /** Row is an object of the form:
     *  row.time = 0;
     *  row.ch1 = 0.5;
     *  row.ch2 =  0.3;
     *  row.ch3 = 0.6;
     *  Place it in 2D array where elements in the same array belong to the same instance
     **/
    chart.addRow = function(row, index) {
        if (data==null) data = new Array();
        if (index > data.length+1) {throw "Cannot add to that index yet";}//.. how do we throw exceptions ?
        if (data[index] == null) data[index] = new Array();
        data[index].push(row);
        return chart; //.. for chaining
    }
    
    /** One by one, remove an instance from each class and build a new area line view 
     **/
    chart.transitionToAverage = function(totalTime) {
        this.hasTransitioned = true;
         var lines = d3.selectAll(".d3line")[0];
         drawn = new Array(); //.. hash holding what has been drawn once
         
         if (arguments.length != 0) { //.. if total time is specified
              lineTrans = totalTime / lines.length;
              chartTrans = lineTrans *1;
         }
         //..for each line, transform it into the the std.dev area chart
         for (var i=0; i < lines.length; i++) {
             var oldLine = lines[i];
             var instance = data[i];
             var condition = instance[0].condition;
             var existingAvg = drawn[condition]; //.. retrieve past avg calulculations if any 

             //..  Recompute average and standard deviation of this and past lines of same condition
             drawn[condition] = existingAvg = getAverageOfTwoArrays(existingAvg, instance);
             mergeToArea(existingAvg, condition, instance, oldLine,i);
         }
         
         //.. figure out afterwards how we're going to deal with multiple conditions
        // console.log(drawn["low"]);

        //.. Once complete draw a line through the middle, at the average
         for (property in drawn) {
             drawMeanInMiddle(drawn[property], property );
         }
    }
    
    /**Alter the scale, so that it is fit to the area chart as opposed to the indiviudal lines
     * Since its in setTimeout, it cant take a parameter without a hack**/
    chart.transitionScale = function() {   
         var maxOfAC = getMaxOfAC("value");
         var minOfAC = getMinOfAC("value");
         y= d3.scale.linear()
            .domain([minOfAC,maxOfAC]) //.. doubly nested since a 2d array (too expensive?)
            .range([height - margin.top, 0 + margin.bottom]); //.. these need to be flipped because svg's are anchored at topleft
        
         //.. Add a y axis
         yAxis = d3.svg.axis()
           .scale(y)
           .orient("left");
   
         svg.select(".y")
                 .transition()
                 .duration(transitionLength)
                 .call(yAxis);
         
         //.. Next redraw all data, so that it transitions to this new scale
         for (var property in drawn){
             svg.selectAll("#area" + property)
                   .data([drawn[property]])
                   .transition()
                   .duration(transitionLength)
                   .attr("d", area);
           
            svg.selectAll("#line" + property)
                    .datum(drawn[property])
                    .transition()
                    .duration(transitionLength)
                    .attr("d", meanline);
          }
       
    }
    
    //.. returns the maximum value in area chart
    var getMaxOfAC = function(key) { 
        var all = [];
        for (var property in drawn) {
            all.push(drawn[property]);
        }
        
        return d3.max(all, 
            function(d) {
                return d3.max(d, function(d){
                    return (d[key] +(d.getStdDev()/2.0));})});
    }
    //.. returns the maximum value in area chart
    var getMinOfAC = function(key) {
        var all = [];
        for (var property in drawn) {
            all.push(drawn[property]);
        }

        return d3.min(all,
                function(d) {
                    return d3.min(d, function(d) {
                        return (d[key] - (d.getStdDev() / 2.0));
                    })
                });
    }
    
    /**Move individual line and summed-class-area chart to a location, representing the condition's 
     *average and standard deviation
     **/
    var mergeToArea = function(existingAvg, condition, instance, oldLine, index) {
         var numExamples = existingAvg[0].numExamples;
         fadeLine(oldLine, index);
         if (numExamples ==1) //.. its the first of this condition, then draw the first area chart
             drawAreaChart(existingAvg,condition);
         else //.. otehrwise update an existing chart
             updateAreaChart(existingAvg,condition, index);
    }
    
    var fadeLine = function(oldLine, index) {
         svg.transition().delay(lineTrans*index).duration(lineTrans).selectAll("#" +oldLine.id).style("opacity",0.9).remove();
    }

  
    
    /*Having drawn an area chart, update it with new data
     **/
    var updateAreaChart = function(avgArray, condition, index) {
       svg.selectAll("#area"+condition)
             .data([avgArray])
             .transition()
             .delay(chartTrans*index)
             .duration(chartTrans)
             .attr("d", area);
   }

  
    var drawMeanInMiddle = function(avgArray, condition) {
        avgArray.pop(); //.. LOOKS radicalyl better if we dont add teh last one
        meanline = d3.svg.line()
                .interpolate("basis")
                .x(function(d,i){return x(i);})
                .y(function(d){return y(d.value)});
       
        svg.append("path")
                .datum(avgArray) //.. by doing datum we reserve the right to alternate which dimension of data is shown
                .attr("class", "meanline")
                .attr("id", "line" + condition) //.. so that it rescales
                .attr("d", meanline)
                .style("stroke", function(d) {
                    return "black"; //.. assume all have same condition
                }); 
        
        
                           
    }
   
    /**Given an array of a collection of points of form {value: x, stdder: z},
     * draw an area chart with center at x, bottom at x- stdder and top at x +stdder*/
    var drawAreaChart = function(avgArray, condition) {
            //.. area is an svg object
            area = d3.svg.area()
                .interpolate("basis")
                .x(function(d,i) {return x(i); })
                .y0(function(d) { if (d.numExamples ==1) return y(d.value)-2; return y(d.value+(d.getStdDev()/2.0)) /*+areaHeight(d.getStdDev())*/; })
                .y1(function(d) {if (d.numExamples ==1) return y(d.value)+2; return y(d.value -(d.getStdDev()/2.0))  /*-areaHeight(d.getStdDev())*/; });
        
           var ent =svg.selectAll(".area")
              .data([avgArray])
              .enter();
           var path = ent.append("path")
               .attr("id", "area"+condition)
               .attr("class", "avgArea")
               .attr("d", area)
               .style("fill", color(condition)) ///color(condition)
               .style("opacity", 0.9); 
       
            
    }
    
    /* Returns an array of length of the largest that is the average at each of the points;
    ** a, but not b can be an uninitualized array (in which case we initialize it and give it b
    ** a is the average array; b is an instance
    ** a's points are quadruples: standard deviation, value, numExamples, stdDev*/ 
    var getAverageOfTwoArrays = function(a, b) {
        if (a == null) a = new Array();
        var longestLength = (a.length > b.length) ? a.length : b.length;
        var retArray = new Array(longestLength);
     
        //.. average each value; take into consideration that one array might be longer
        for (var i =0; i < longestLength; i++) {
            var point = {value: 0, sumOfSquares: 0, numExamples :0, getStdDev : function(){return Math.sqrt(this.sumOfSquares / this.numExamples)}};
            var oldPoint = a[i]; //.. has same form as point ( value, sumOfSquares, numExamples)
            var newPoint = b[i]; //.. has same form as a dataPoint. Access value by key

            //... If a has values and b has values
            if (i < a.length && i < b.length) {
                point.sumOfSquares = oldPoint.sumOfSquares+ Math.pow((channels(newPoint)[key] - oldPoint.value),2);
                point.value = (channels(newPoint)[key]+ oldPoint.value) /2;
                point.numExamples = oldPoint.numExamples +1;
            }
            
            //.. If this is the first value put into a 
            else if (i >= a.length) { 
                point = new Object();
                point.value = channels(b[i])[key];
                point.sumOfSquares = 0; //.. new point, so no standarddeviation
                point.numExamples =1;
            }
            
            else{ //.. only possibility: there's no more in b but there is in a
                point.value =oldPoint.value;
                point.sumOfSquares = oldPoint.sumOfSquares;
                point.numExamples = oldPoint.numExamples;
                //.. dont change standard deviation
            }
            retArray[i] = point;
        }
    
        return retArray;
    }

   
    
    return chart; //.. return a function which has configuration properties width and height
}


//.. Test/
//test();
function test() {
    var chart = LineChart();
    var menu = d3.select("#menu select")
    .on("change", function() {
        var channel = menu.property("value");
        chart.key(channel);
        chart.transition();
    });
   

    var s = new Array();
    //.. one instance
    for (var i=0;i <20; i++) {
        var row = getRandomRow3(i,"low");
        s[i] = row.channels[0];

        chart.addRow(row, 0);
    }
    
    //. two instances
    for (var i=0;i <20; i++) {
        var row = getRandomRow3(i, "low");
        s[i] = (row.channels[0] + s[i])/2;
        chart.addRow(row, 1);
    }
    
    /*
    for (var i=0;i <20; i++) {
        chart.addRow(getRandomRow2(i, "high"),2);
    }
    for (var i=0;i <20; i++) {
        chart.addRow(getRandomRow2(i, "high"),3);
    }
    
     for (var i=0;i <20; i++) {
        chart.addRow(getRandomRow2(i, "low"),4);
    }
    
     for (var i=0;i <20; i++) {
        chart.addRow(getRandomRow2(i, "high"),5);
    }
    
     
     for (var i=0;i <20; i++) {
        chart.addRow(getRandomRow2(i, "high"),6);
    }
    
     for (var i=0;i <20; i++) {
        chart.addRow(getRandomRow2(i, "high"),7);
    }
    
       for (var i=0;i <20; i++) {
        chart.addRow(getRandomRow2(i, "high"),8);
    }
    
     for (var i=0;i <20; i++) {
        chart.addRow(getRandomRow2(i, "low"),9);
    }
    
     
     for (var i=0;i <20; i++) {
        chart.addRow(getRandomRow2(i, "low"),10);
    }
    
     for (var i=0;i <20; i++) {
        chart.addRow(getRandomRow2(i, "low"),11);
    }
    
     for (var i=0;i <20; i++) {
        chart.addRow(getRandomRow2(i, "high"),12);
    }
    
    for (var i=0;i <20; i++) {
        chart.addRow(getRandomRow2(i, "high"),13);
    }*/ 

    chart.key(0).channels(function(d){return d.channels;}).width(900).height(400)("#topRight").transition();
    setTimeout(function() {chart.transitionToAverage(10)},10);
    setTimeout(function() {
        chart.transitionScale(10)
    }, 100);

    
}

    
function getRandomRow(i, condition) {
   var row = new Object();
   row.time =i;
   row.ch1 = Math.random()*2;
   row.ch2 = Math.random()*2;
   row.condition = condition;
   return row;
}

function getRandomRow2(i, condition) {
   var row = new Object();
   row.time =i;
   row.channels = [Math.random()*2, Math.random()*2];
   row.condition = condition;
   return row;
}
function getRandomRow3(i, condition) {
    var row = new Object();
    row.time = i;
    row.channels = [Math.random() * 100];
    row.condition = condition;
    return row;
}