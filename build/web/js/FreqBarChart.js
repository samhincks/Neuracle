/* 
 * By Default, a BarChart that moves from 0 to the values listed by some key: 
 *  key, subValueKey, expectedKey, and labelKey all have a default functions but can be overriden if dataset has different
 *    accessors for these values. Only key is strictly necessary, setting keys for the remaining accessors opens
 *    more advanced functionality. 
 * 
 * ZOOM IN: When a bar with underlying data is clicked zoom in on that bar, and reveal underlying data-dimension.
 *  - .on mousedown call splitTransition 
 * 
 * TODO:
 *  Make so that x axis is a nominal piece of the data: THIS IS DIFFICULT. WE would need to find an xscale taht operates on nominal values
 *  Deal with removing chart if it already exists (otherwise we keep painting over it)  
 *  DEAL with keys - we don't have settors/gettors and we don't use the private variables in the code at all points
 *  Document, then test all the ways that I can interact with bar chart
 *   */

function FreqBarChart() {
    var data;
    
    /**CONFIGURATION VARIABLES**/
    //.. width, height, margin. If these are customizable we need settors 
    var margin = {top:20, left: 30, right: 30, bottom: 20 },
        width = 700 - margin.left -margin.right, 
        height = 400 - margin.top - margin.bottom,
        padding = 10,
        barSpaceFraction =3, //.. what proportion, 1/X, should the bars occupy
        max =1,
        min = 0,
        barClass = "d3BarRect",
        transitionLength = 1000;
        
    //.. CHART variables   
    var selection = "body", //.. container
        svg,  //.. accessor for the cahrt itself
        id; //.. id of the chart
   
    
    //.. KEYS: functions for extracting different data-componetnts from objects     
    var key = function (d) {return d.value}; //.. function returning where the value is held
    var subValueKey = function (d) {return d.subValues}; //.. if a bar has a set of subvalues (which you can zoom into
    var expectedKey = function(d) {return d.expected}; //.. if there is some expceted value to compare to
    var labelKey = function (d) {return d.label}; //.. function returning the label associated with a bar
   
    //.. FUNCTIONS for extractin the data
    var computeY =  function(d) {return  y(key(d));} //. straight forward 
    var computeHeight = function(d) {return  -1*y(key(d));} //.. height must be negative
    var computeX = function (i, barWidth) {if (data.length ==1) return x(0.5) - barWidth/2; else return padding +x(i);}; //.. determine x position based on index
    var barWidth =20; //.. but modify based on data 
    var x, //.. compute x position from data
        y; //.. compute y position from data
       
    //.. MISCELLANEOUS PRIVATE VARIABLES
    var zoomed = false, //.. but we have a new BAR CHART!!!
        zoomedId = "zoomedBarChart"; //.. the name if its zoomed in bar chart
    
    /* ALL parameters optional
      * s = selection to put it in 
      * id = css identifier
      * zoomedChart= true/false. is this a zoomed in version?
     */
    function chart(s, _id, zoomedChart) {
       //.. Throw errors if there's no data, data is not accessible, min-max not in tune with values
       if (data == null) throw ("No data has been added yet");
       if (key(data[0]) == null){ throw("Specify proper keys and channel functions for accessing data")};
       if (d3.max(data, key) > max || d3.min(data, key) < min) throw("Maximum or minimum value in dataset more or less than specified max or min and therefore not visible in chart")

      //.. if this chart has been added before remove it (TODO: rename chart)
       if (arguments.length >0){
            svg = d3.select(s).append("svg:svg");
            selection = s;
       }

       //.. if there is only one parameter, then set id by default
       if (arguments.length >= 2) 
           id = _id;
       else
           id = "barChart";

       //.. if there are atleast three parameters, the third is a boolean saying it is zoomed
       if (arguments.length >= 3)
           zoomed =zoomedChart;
       
       //.. x axis: a set of ticks from 0 to data.length
       x = d3.scale.linear()
            .domain([0, data.length])
            .range([0, width]);
  
       y = d3.scale.linear()
          .domain([min, max])
          .rangeRound([0, -height]);

       //.. Since width specifications were subtracted in the constructor, add them now
       //.. Then translate the calls to 0,0 to take into acount the margins
       svg = svg
           .attr("class", "chart")
           .attr("id", id)
           .attr("width", width+ margin.left + margin.right)
           .attr("height", height+ margin.top + margin.bottom)
         .append("g")
           .attr("transform", "translate(" + margin.left + "," + (height + margin.bottom) + ")");
       
      //.. select all existing - there should be none
       var rect = svg.selectAll("rect");
       
       //.. compute how big each bar should be
       barWidth = (width / barSpaceFraction) / data.length;

       //.. enter data initially as 0,so we get a zoom up effect
       rect.data(data).enter().append("rect")
           .attr("y", function(d) {return  y(0.0);})
           .attr("x", function (d, i) {return computeX(i, barWidth)})
           .attr("width", barWidth) //. scale based on how many elements
           .attr("height", function(d) {return -1* y(0.0);})
           .attr("class", barClass)
           .attr("id", function(d,i) {return barClass +i})
           .style("fill", "red")
           .style("opacity", 0.1)
           .on("mousedown", function (d, i) {chart.zoomTransition(i, subValueKey, transitionLength)})
           .on("mouseover",  function (d, i) {hoverRect(i)})
           .on("mouseout", function (d, i) {unHoverRect(d, i)});

       //.. zoom up to actual values
       chart.transition(key, transitionLength);

       // ------ X AXIS--------- 
        var xAxis = d3.svg.axis()
            .scale(x) //.. use same scaling function as for x
            .orient("bottom")
            .ticks(0); //.. set to 5 if we wanna show actual indexes
            
        var axisG = svg.append("g")
            .attr("class", "x axis")
            .call(xAxis); 
       
       //.. append labels 
       axisG.selectAll("text")
          .data(data).enter().append("text")
          .text(function (d) {if (labelKey(d) != null) return labelKey(d).substring(0,12);})
          .attr("x", function(d,i) {return computeX(i, barWidth) +(barWidth/10)})
          .attr("y", margin.bottom/2);
       //---------------------------  

        //.. Add a y axis
        var yAxis = d3.svg.axis()
           .scale(y)
           .orient("left")
           .ticks(20);
       
        svg.append("g")
           .attr("class", "y axis")
           .call(yAxis);

        //.. draw lines where 
        if(expectedKey(data[0]) != null) //.. if data has the expceted key 
            chart.drawLines(expectedKey);

    }
  
    
    /**Transition to values listed under specified access key.
     * Error if data does not contain the specified key*/
    chart.transition = function(accessKey, length) {
        if (arguments.length) key = accessKey;
        if (!arguments.length>1) length = 750;
        try {computeY(data[0])} catch(e) {throw error("Data does not have values specified by key")};
        
        var t0 = svg.transition().duration(length);
        var rects = t0.selectAll("."+barClass);
        
        rects
           .attr("y", function(d) { return computeY(d);})
           .attr("height", function(d) {return computeHeight(d);})
           .attr("width", barWidth)
           .attr("x" , function (d, i) {return computeX(i, barWidth)})
           .style("fill", function(d) { if (betterThanChance(d)) return "green"; else return "red"});
       
        if(data[0]["expected"] != "undefined") //.. if data has the 
           rects.style("opacity", function(d) {return opacityRelativeToChance(d)}); //.. if worse than chance make MORE RED
       
        return chart;
    }
    
    
    
    /*Make more opaque for performances better than chance and less opaque for performance
     *worse than chance. Make the opacity relative to what the chance-rate is
     **/
    var opacityRelativeToChance = function(d) {
       if (betterThanChance(d)) {
           var distToMax = max - key(d);
           var betThanChance = key(d) - d.expected;
           if (distToMax == 0) return 1;
           return 0.1 +(betThanChance / distToMax);
       }   
       
       else {
            return 0.1+ d.expected - key(d);
       }
    }
    
    //.. return true if better than chance
    var betterThanChance  = function(d) {
        return (d.value > d.expected) ?  true :  false;
    }
    
    /* On mouseover, give feedback that suggests its going to be clicked
     */
    var hoverRect = function(index) {
        var id = "#"+barClass+index; //.. 
        var rect = svg.select(id);
        rect.style("fill","steelblue");
        
        //.. append  a text element displaying the value on hover
        svg.selectAll(".percentageLabel")
            .data(data)
            .enter()
            .append("text")
               .text(function(d) {return Math.round(key(d)*1000)/1000;}) //.. round to 3 decimal plcces
               .attr("x" , function (d, i) {return computeX(i, barWidth) + (barWidth /1.8) }) //.. place in middle
               .attr("y", function(d) { return computeY(d);})
               .attr("class", "percentageLabel");

               
    }
    
     /* On mouseaway, return to original color */
    var unHoverRect = function(data,index) {
        var id = "#"+barClass+index; //.. 
        var rect = svg.select(id);
        
        svg.selectAll(".percentageLabel").remove();
        //.. return 
        rect.style("fill", function(d) { if (betterThanChance(d)) return "green"; else return "red"});
    }
    
    /** Draw a line at particular point at each bar specified by access key
     * changes current key
     **/
    chart.drawLines = function(accessKey) {
        if (arguments.length) key = accessKey;
        var lineExtra = barWidth / 5;
        for (var i in data) {
            var bar = data[i];
            var yPos = computeY(bar);
            var xPos = computeX(i, barWidth);
            var line = svg.append("svg:line")
                .attr("x1", computeX(i, barWidth)-lineExtra)
                .attr("y1", yPos)
                .attr("x2", xPos+barWidth+lineExtra)
                .attr("y2", yPos)
                .attr("class", "expectedLine")
                .style("stroke-dasharray",("10,10"));
        }
    }
    
    /************ZOOM IN ON BAR*********************************************** 
     *-When a bar is clicked (or whatever command), split into bars that represent some underlying set of data
     *components that average the bar, for example multiple classification accuracies. 
     *-Currently, there can only be one underlying depth. if something is clicked at lower depth, you zoom back.
     *
     *index is wehre the in the array the bar is, which is the distinguishing feature of its id
     **/
    chart.zoomTransition = function(index, accessKey, length) {
        if (!arguments.length>2) length = 750; //.. if no transition length specified default at 750
       
       //.. if its a zoomed in version, go back. (max depth =1)
       if (zoomed) {
            chart.unZoomTransition(index, accessKey, length);
            return; //.. don't zoom in.. return
       }
        console.log(data);

       if (accessKey(data[0]) == null) return; //.. disabled if we cannot access subvalues
       
       var id = "#"+barClass+index; //.. 
       var rect = svg.select(id);
        
        //.. make it seem like zooming in on the bar, filling the screen and fading to background color
        rect.transition().duration(length)
            .style("fill", "whitesmoke")
            .attr("width", width)
            .attr("height", height)
            .attr("y", -1*height)
            .attr("x", 0);
        
        var subVals = accessKey(data[index]);
        console.log(subVals.length);
        setTimeout(function() {drawNewChart(subVals); chart.transition(key);},length);
    }
  
    /**ZOOM-helper function. Draw a new chart above the old one*/
    var drawNewChart = function(subVals) {
        var chart = BarChart();
        
        ///.. set sunbVals as dataset for this new array
        for (var i in subVals) {
            console.log(subVals[i]);
            chart.addBar(subVals[i]);
        }
        
        console.log(subVals[0].expected);
        chart.key(function(d) {return d.expected;});
        
        //.. give it a special Id -- zoomedId -- which is always the name when we zoom
        chart(selection, zoomedId, true); //.. set true so that we know this is a zoomed in version and we can change behavior next time zoom is pressed
       
        //.. transition to function where we hold value
        setTimeout(function() {chart.transition(function (d) {return d.value}, 1000)},1000);
    } 
    
    
    /**Having zoomed in, go back to first outer layer with a transition
     **/
    chart.unZoomTransition= function(index, accessKey, length){
         if (!arguments.length>2) length = 750; //.. if no transition length specified default at 750
         var selection = d3.select("#"+id);
         
         selection.transition().duration(length).attr("width", 0).remove();
    }
    
    
    /*Add a bar, which is a tuple: obj.expected and obj.value
     */
    chart.addBar =function (barObj) {
        if (data == null) data = new Array();
        data.push(barObj);
        return chart;
    }
    
    chart.key = function(_){
        if (!arguments.length) return key; //.. this notation gives a get for free
        key = _;
        return chart;
    }
    
    //.. the maximum value shown in the y axis
    chart.maxY = function(_) {
        if (!arguments.length) return max;
        max = _;
        return chart;
    }
    
    //.. the minimum value shown in the y axis
    chart.minY = function(_) {
        if (!arguments.length) return min;
        min = _;
        return chart;
    }
    
    //.. set the width of the chart
    chart.width = function(_) {
        if (!arguments.length) return width;
        width = _;
        width = width -margin.left - margin.right; //.. since we will add these later
        return chart;
    };
    
    //.. set the height of the chart
    chart.height = function(_) {
        if (!arguments.length) return height;
        height = _;
        height = height - margin.top - margin.bottom;
        return chart;
    };
    
    //.. set the margins
    chart.margin = function(_) {
        if (!arguments.length) return margin;
       
        //.. recalculate original width request
        width = width + margin.left +margin.right, 
        height = height + margin.top + margin.bottom;
        margin =_;
        
        //.. subtract new values
        width = width + margin.left - margin.right, 
        height = height + margin.top - margin.bottom;

        return chart;
    }
    
    return chart;
}

//test();
function test() {
    var chart = BarChart();
    chart.addBar(getRandomData(true));
    chart.addBar(getRandomData(true));
    chart.addBar(getRandomData(true));
    chart.addBar(getRandomData(true));
    chart.addBar(getRandomData(true));
    chart.addBar(getRandomData(true));
    
    chart.minY(0).width(400).height(400).maxY(1).key(function(d) {return d.expected;});
    //chart("#topRight");
    
    chart("body");
    setTimeout(function() {chart.transition(function (d) {return d.value}, 1000)},1000);

}

function getRandomData(subVals) {
    if (!arguments.length)
        return {value :  Math.random(),  expected : Math.random()};
    else
        return {value : Math.random(), label : "bar" + Math.random(), expected : Math.random(), subValues : [getRandomData(),getRandomData(), getRandomData()]};
}