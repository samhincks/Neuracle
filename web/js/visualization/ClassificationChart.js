

function ClassificationChart() {
    /**PUBLIC VARIABLES**/
    var width = 700, 
        height = 400,
        margin = {top:30, left: 60, right: 30, bottom: 20 };
 
    var key = 0; //.. the set of elements in view. If its an array it's acceptable to use index position
    var selection;// = d3.select("body").append("svg:svg");
    var color = d3.scale.category10();
    
    
    /**PRIVATE VARIABLES*/
    //.. computed from data and configuration
    var svg,
        y,x,//.. the actual chart container
        line;//.. a function for computing 

    var min, max, numPoints;
    var data; //.. Each row an object with arbitrary number of channels at a common timestamp
    
    
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
      
        //.. calculate desired scales
        y= d3.scale.linear()
            .domain([0,1]) //.. doubly nested since a 2d array (too expensive?)
            .range([height - margin.top, 0 + margin.bottom/3]); //.. these need to be flipped because svg's are anchored at topleft
        x= d3.scale.linear()
            .domain([0, this.getLongest()])
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
                .x(function(d, i) {
                    return x(i)
                })
                .y(function(d, i) {
                    return y(d);
                });
        
        for (var i=0; i < data.values.length; i++ ) {
             svg.append("path")
                .datum(this.normalize(data.values[i])) //.. by doing datum we reserve the right to alternate which dimension of data is shown
                .attr("class", "classificationline")
                .attr("d", line)
                .attr("id", "instance"+i)
                .on("mousedown", function(d,i) {
                    var lines = d3.selectAll(".classificationline"); 
                    if(lines.style("opacity") > 0.5) lines.style("opacity", 0.4);
                    else lines.style("opacity", 1)})
                .style("stroke", function(d) {
                    return "lightgrey"; //.. assume all have same condition
                 });
                        
        }
       // d3.selectAll(".confidenceline").style("opacity", 0.1);
        numPoints = data.values[0].length;
        
        for (var i = 0; i < data.markers.length; i++) {
            this.showMarker(data.markers[i]);
        }
        
        if(data.classifiers != null) {for (var i = 0; i < data.classifiers.length; i++) {
            var binary = data.classifiers[i].binaryClass;
            //.. only show confidence of X if its a binary class
            if (binary != null) {
                var confidenceArray = extractConfidenceArray(data.classifiers[i]); 
                svg.append("path")
                        .datum(confidenceArray) //.. by doing datum we reserve the right to alternate which dimension of data is shown
                        .attr("class", "confidenceline")
                        .attr("d", line)
                        .style("stroke", function(d) {
                            return color(binary); //.. assume all have same condition
                        })
                        .on("mousedown", function(d, i) {
                            var lines = d3.selectAll(".confidenceline");
                            if (lines.style("opacity") > 0.5)
                                lines.style("opacity", 0.4);
                            else
                                lines.style("opacity", 1)
                        })
                        .style(("stroke-dasharray", ("3, 3")) );
            }
            var classificationArray = this.extractClassificationArray(data.classifiers[i]);
            this.showMarker(classificationArray);
        }}     
        d3.selectAll(".confidenceline").style("opacity", 0.3);
        
        var x2;
        if (maxTime < 0)
            x2 = x;
        else
            x2 = d3.scale.linear()
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
    }
    this.last = 0;
    this.markersAdded =0;
    
    /** Given an array of predictions, create a line that corresponds to the confidence 
     * of a certain class, and creates an array for a visualizable line
    **/
    this.extractConfidenceArray = function(classifier) {
        var every = classifier.every;
        //.. we have a classification every X. every is the amount we need to pad each time
        var numClassifications = numPoints /every; //.. should be equal to # classifications we have
        var confidences = new Array(); //.. should be # points long

        for (var i =0; i < numClassifications; i++) {
            if (i < classifier.classifications.length) {
                var classification = classifier.classifications[i];
                for (var k= 0; k<every; k++){
                    confidences.push(classification.confidence);
                }
            }
            else console.log("missing " + i);
        }
        return confidences; 
    }
   
    this.extractClassificationArray = function(classifier) {
        var markers = new Object();
        markers.name = classifier.name;
        markers.length = classifier.length;
        markers.data = new Array(); //.. create a new marker of appropriate length everytime the classification switches 
        var last = new Object();
        last.value= classifier.classifications[0].value;
        last.length = classifier.every;
        last.start =0;
        var position =last.length;
        for (var i =1; i< classifier.classifications.length; i++) {
            var thisC =classifier.classifications[i];
            if (last.value != thisC.value) {
                markers.data.push(last);
                last = new Object();
                last.name = classifier.name;
                last.value = thisC.value;
                last.start = position;
                last.length = classifier.every
            }
            else last.length += classifier.every;
            position+= classifier.every;
        }
        markers.data.push(last);
        return markers;
    }
    
    this.name;
    this.showMarker = function(m) {
        var effectiveWidth  = width - margin.left- margin.right;
        var x3 = d3.scale.linear()
                .domain([0, this.getLongest()])
                .range([0, effectiveWidth]);  
        this.name = m.name;
        var self = this;
        var tip = d3.tip()
                .attr('class', 'd3-tip')
                .offset([0, 0])
                .html(function(d) {
                    return "<strong> " + d.name + "</strong><span style='color:lightcrimson'> : " + d.value + "</span>";
                })
        svg.call(tip);
        
        var rect = svg.selectAll("div");  
        rect.data(m.data).enter().append("rect") //.. when this is called the second time I can only get 10, probably because of transitions
           .attr("y", function(d, i) {return y(markersAdded)}) //.. so that they dont overlap
           .attr("x", function (d, i) { return x(d.start);})
           .attr("width", function (d,i) { return x3(d.length);}) //.. why does x(0) work... How confusing
           .attr("height", function(d) {return 10})
           .style("fill", function(d) { return color(d.value);})
          // .attr("opacity", function(d) {return (d.confidence)})
          // .attr("id", function(d,i) {return recClass +i})
            .on("mouseover", tip.show)
            //.on("mousedown", function(d) {d3.selectAll(".confidenceline").style("opacity", 10);})
            .on("mouseout", tip.hide);
        markersAdded+=0.05;
    }
       
    this.normalize = function(arr) {
        var min = d3.min(arr);//, function(d) { return data[] };
        var max = d3.max(arr);
        var sc = d3.scale.linear()
                .domain([min, max]) //.. doubly nested since a 2d array (too expensive?)
                .range([0,1]); //.. these need to be flipped because svg's are anchored at topleft
       
        var normalized = new Array();
        for (var i =0 ; i < arr.length; i++) {
           normalized.push(sc(arr[i]));
        }
        return normalized;
    }
    
     //.. returns the length of the longest array
    this.getLongest = function () {
        return d3.max(data.values, function(d) { return d.length;});
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
    
    chart.data = function(_) {
        if (!arguments.length)
            return data;
        data = _;
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
        if (!arguments.length)
            return maxTime;
        maxTime = _;
        return chart;
    }
    
    
    /*Return an object that matches what actual data might look like*/
    chart.getTestData = function() {
        var obj = new Object();
        obj.data = new Object();
        obj.data.maxtime = 3000;
        obj.data.values = new Array();

        var chan = new Array();
        for (var i = 0; i < 100; i++) {
            var diff = Math.random() *1.0;
            chan.push(diff);
        }
        obj.data.values.push(chan);
        chan = new Array();
        for (var i = 0; i < 100; i++) {
            var diff = Math.random() * 100.0;
            chan.push(diff);
        }        
        obj.data.values.push(chan);
       
        obj.data.markers = new Array();
        var s = new Object();
        s.name = "condition"
        s.data =new Array();
        for (var i=0; i < 10; i++) {
            var marker = new Object();
            marker.length =10;
            marker.name = "condition";
            marker.start = i *marker.length;
            if (i% 2 ==0) marker.value = "easy";
            else marker.value ="hard";
            s.data.push(marker);
        }
        obj.data.markers.push(s);
        obj.data.classifiers = new Array();
        
        var classifier = new Object();
        classifier.binaryClass ="hard";
        classifier.name = "smo";
        classifier.length = 10;
        classifier.every =2;
        classifier.classifications =new Array();

        for (var i = 0; i < 50; i++) {
            var classification = new Object();
            classification.confidence = Math.random();
            if (classification.confidence  > 0.5)
                classification.value = "hard";
            else
                classification.value = "easy";
            classifier.classifications.push(classification);
        }
        
        obj.data.classifiers.push(classifier);

        return obj;
    }
    
    return chart; //.. return a function which has configuration properties width and height
}

test();
function test() {
    var chart = ClassificationChart();
    var obj = chart.getTestData();
    var selection = "#topRight";
    console.log(obj);
    //.. add a menu for selecting channel 
    /*$("#channelSelection").remove();
    $(selection).append("<select id = channelSelection> </select>");
    //.. add each channel as a value to the select menu
    for (var i = 0; i < obj.data.markers.length; i++) {
        $('#channelSelection')
                .append($('<option>', {value: i})
                        .text(obj.data.markers[i].name));
    }*/
   chart.maxTime(obj.data.maxtime).data(obj.data)(selection);

}

    


