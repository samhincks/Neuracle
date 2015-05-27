

function ClassificationChart() {
    /**PUBLIC VARIABLES**/
    var width = 700, 
        height = 400,
        margin = {top:30, left: 60, right: 30, bottom: 20 };
 
    var key = 0; //.. the set of elements in view. If its an array it's acceptable to use index position
    var selection;// = d3.select("body").append("svg:svg");
    var color = d3.scale.category10();
    color = d3.scale.category20();
    color = d3.scale.category20b();
    //color = d3.scale.category20c();

    color = d3.scale.ordinal()
            .range(["#31A354","#3182BD","#756BB1", "#636363", "#637939", "#7B4173"]);


    
    /**PRIVATE VARIABLES*/
    //.. computed from data and configuration
    var svg,
        y,x,x3,//.. the actual chart container
        line;//.. a function for computing 

    var min, maxX, numPoints;
    var data; //.. Each row an object with arbitrary number of channels at a common timestamp
    
    var markerHeight = 10;
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
        
        maxX = chart.getLongest();

        x= d3.scale.linear()
            .domain([0, maxX])
            .range([0 + margin.left, width - margin.right]);  
    
        var effectiveWidth = width - margin.left - margin.right; //.. needs a different scale
        x3 = d3.scale.linear()
                .domain([0, maxX])
                .range([0, effectiveWidth]);  
       
        //.. if we've already initialized this chart, we must clear it
        if (typeof(svg) == "object") d3.select(".chart").remove();
        
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
        
        //.. append greyed out versions of the raw data
        for (var i=0; i < data.values.length; i++ ) {
            chart.showLine(i);
        }
        
        // d3.selectAll(".confidenceline").style("opacity", 0.1);
        numPoints = data.values[0].length;
        
        for (var i = 0; i < data.markers.length; i++) {
            var m = data.markers[i];
            chart.showMarker(data.markers[i], i);
        }
        
        //... for now I don't use this in the back-end, since the marker classifications is working so well
        //.. I could consider removing all classifier functionality, though I might want to put in confidences 
        if(data.classifiers != null) {for (var i = 0; i < data.classifiers.length; i++) {
            var binary = data.classifiers[i].binaryClass;
            /*
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
            
            //.. This shouldn't be necessary even! 
           // var classificationArray = this.extractClassificationArray(data.classifiers[i]);
            this.showMarker(classificationArray); */
        }}     
        d3.selectAll(".confidenceline").style("opacity", 0.3);
        
        var x2; //.. x2 is the scale for our x axis
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
    
    chart.showLine = function(index) {
        svg.append("svg:path")
                .attr("d", line(chart.normalize(data.values[index])))
                .attr("class", "classificationline")
                .style("stroke", function(d) {
                    return "lightgrey"; //.. assume all have same condition
                });

            
          //.. JOIN
        /*  svg.append("path")
                .datum(chart.normalize(chan)) //.. by doing datum we reserve the right to alternate which dimension of data is shown
                .attr("class", "classificationline")
                .attr("d", line)
                .attr("id", "instance"+index)
                .on("mousedown", function(d,i) {
                    var lines = d3.selectAll(".classificationline"); 
                    if(lines.style("opacity") > 0.5) lines.style("opacity", 0.4);
                    else lines.style("opacity", 1)})
                .style("stroke", function(d) {
                    return "lightgrey"; //.. assume all have same condition
                 }); */
    }
    chart.transitionLine = function(index) {
        svg.selectAll("path")
                .data([(data.values[index])])
                .attr("d", line)
                .transition()
                .ease("linear")
                .duration(3000);//.. doesnt work
    }
    
 
    /** For both adding and updating a marker array
     **/
    chart.showMarker = function(m, index) {
        //.. TIP
        var tip = d3.tip()
                .attr('class', 'd3-tip')
                .offset([0, 0])
                .html(function(d) {
                    return "<strong> " + d.name + "</strong><span style='color:lightcrimson'> : " + d.value + "</span>";
                })
        svg.call(tip);
        
        //.. JOIN
        var rect = svg.selectAll("div").data(m.data);
        
        //.. ENTER
        rect.enter().append("rect")
           .attr("y", function(d, i) {return y(index /20)}) //.. so that they dont overlap
           .attr("height", function(d) {return markerHeight})
           .style("fill", function(d) { return color(d.value);})
            .attr("x", function (d, i) {return x(d.start /*+ d.offset*/);}) ///.. toggle d.offset to change whether its how it gets classified
            .attr("width", function (d,i) {return x3(d.length)}) //.. width is like radius
            .attr("class", function(d,i) {return d.name+"-"+d.value +" rect";})
            //.on("mouseover", tip.show)
            .on("mousedown", function (d,i ) { //.. remember what pairs have been overlapped. 
                    var id = d.name+"-"+d.value;
                    consoleArea.displayMessage(d.value);
                    var rects = d3.selectAll("."+id);
                    if (rects.style("opacity") > 0.5){ //.. if its not selected
                        rects.style("opacity", 0.4);
                        if (firstSelection == null) firstSelection = id;
                        else if (secondSelection == null){
                            secondSelection = id;
                            chart.computeOverlap()
                        }
                        else {
                            d3.selectAll(".rect").style("opacity", 1);
                            firstSelection =null;
                            secondSelection = null;
                        }
                    }
                    else { //.. if it is selected
                        d3.selectAll(".rect").style("opacity", 1);
                        firstSelection =null;
                        secondSelection = null;
                    }})
            //.on("mouseout", tip.hide)
            .transition();
    }
    var firstSelection = null;
    var secondSelection = null;
    
    chart.computeOverlap = function() {
        var firstName = firstSelection.split("-")[0];
        var firstCondition = firstSelection.split("-")[1];
        var secondName = secondSelection.split("-")[0];
        var secondCondition = secondSelection.split("-")[1];
        var first, second;
        
        ///.. get markers
        for (var i = 0; i < data.markers.length; i++) {
            var m = data.markers[i];
            if (m.name==firstName) first = m;
            if (m.name == secondName) second = m;
        }
        
        //.. get labels extracted from the instance object
        var firstAligned = chart.getLabels(first); //.. a succession of labels corresponding to the instances
        var secondAligned = chart.getLabels(second);
        var correct =0;
        var total =0;
        //.. compute the situations in which the firstAligned is its condition and second condition is its condition
        for (var i in firstAligned) {
            if (firstAligned[i] == firstCondition) {
                if (secondCondition == secondAligned[i]) correct++;        
                total++;
            } 

        }
        var accuracy = (correct / total) *100;
        if (total == 0) accuracy = 0;
        consoleArea.displayMessage("When " + firstName + " had value " + firstCondition + ", " + secondName + " had value " + secondCondition + " " + accuracy  + "% of the time." );
    }
    /*Return an instance label for each point of every instance*/
    chart.getLabels = function(m) {
        var ret = new Array();
        for (var i in m.data) {
            var instance = m.data[i];
            for (var j =0; j <  instance.length; j++) {
                ret.push(instance.value);
            }
        }
        return ret;
    }
    
    /**Reset all values to be between 0 and 0**/
    chart.normalize = function(arr) {
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
    
    chart.appendValues = function(newData, index) {
        var chan = data.values[index];
        data.values[index] = chan.concat(chart.normalize(newData));
    }
    
    /**Add new marker to specified index of markers array **/
    chart.addMarker = function(index,m) { 
        data.markers[index].data.push(m);
    }
    
    /**Recompute the x scale**/
    chart.recomputeX= function (numValues) {
        maxX = maxX + numValues;

        //.. rescale for new data
        x = d3.scale.linear()
                .domain([0, maxX])
                .range([0 + margin.left, width - margin.right]);

        var effectiveWidth = width - margin.left - margin.right;
        x3 = d3.scale.linear()
                .domain([0, maxX])
                .range([0, effectiveWidth]);  
    }
    
    
    
    /**Change to a stream view where the length is fixed at size x, 
     * and we can visualize new classifications and markers**/
    chart.getSlicedData = function(maxX) {
        //.. slice out the last x readings from values and markers arrays
        for (var i = 0; i < data.values.length; i++) {
            data.values[i] = data.values[i].slice (data.values[i].length - maxX,data.values[i].length );
        }
        for (var i = 0; i < data.markers.length; i++) {
            var end =data.markers[i].data.length;
            var start = end -  maxX;
            data.markers[i].data[i] = data.markers[i].data.slice(start, end);
        }
        return data;
    }
    
    //.. returns the length of the longest array
    chart.getLongest = function () {
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
    
    chart.data = function(d, removeNumbers) {
        data = d;
        if (removeNumbers) {
            for (var i in data.markers) {
                var m = data.markers[i];
                for (var k in m.data) {
                   m.data[k].value = m.data[k].value.replace(/[0-9]/g, '');
                }
            }
        }
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
       /* chan = new Array();
        for (var i = 0; i < 100; i++) {
            var diff = Math.random() * 100.0;
            chan.push(diff);
        }        
        obj.data.values.push(chan); */ 
       
        obj.data.markers = new Array();
        var s = new Object();
        s.name = "condition"
        s.data =new Array();
        for (var i=0; i < 10; i++) {
            var marker = new Object();
            marker.length =10;
            marker.offset =0;
            marker.name = "condition";
            marker.start = i*marker.length;
            if (i% 2 ==0) marker.value = "easy";
            else marker.value ="hard";
            s.data.push(marker);
        }
        obj.data.markers.push(s);
        obj.data.classifiers = new Array();
        
        
        s = new Object();
        s.name = "condition2"
        s.data =new Array();
        for (var i=0; i < 5; i++) {
            var marker = new Object();
            marker.length =20;
            marker.offset =0;
            marker.name = "condition2";
            marker.start = i*marker.length;
            if (i% 2 ==0) marker.value = "medium";
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
   
   this.last =100;
   this.added =0;
   var self = this;
   
   /** A bit of a mess here since I got fed up with these transitions. Even though they would be so nice. Maybe need a new scenery*/
   setTimeout(function() {
        var marker = new Object();
        marker.length = 10;
        marker.offset = 0;
        marker.name = "condition";
        marker.start = self.last +marker.length; //*marker.length;
        self.last = marker.start;
        self.added++;
        if (self.added % 2 ==0)
            marker.value = "something";
        else marker.values = "somethingelse";
        chart.addMarker(0,marker);
        
        var chan = new Array();
        for (var i = 0; i < 10; i++) {
            var diff = Math.random() * 1.0;
            chan.push(diff);
        }
       // chart.appendValues(chan, 0);
        //chart.recomputeX(10);
        //chart.transitionLine(0);
        //chart.showMarker(obj.data.markers[0], 0);

    }, 1000);


}

    


