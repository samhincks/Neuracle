/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


function PredictionChart() {
    var data; //.. a 2D array of 'nearness' correlations 

    /**CONFIGURATION VARIABLES**/
    //.. width, height, margin. If these are customizable we need settors 
    var width = 700,
        height = 500,
        margin = {top: 20, left: 30, right: 30, bottom: 20};
    
    var rectWidth =15; //.. this should be a constant
    var rectMargin; //.. computed when we know # data points
    var classMargin; //.. margin between classes on the y axis. Compute this when we add classes
    var selection;
    var everyK;
    var length;
    var classes;
    var recClass = "predRect";
    var calcY;
    var x; //.. same logic as calcY but I suck at programming
   
    function chart(s) {
        //.. if there is an optional parameter of selection
        if (arguments.length) {
            d3.selectAll('.chart').remove(); //.. remove if it exists already
            selection = d3.select(s).append("svg:svg");
        }
        else
            selection = d3.select("body").append("svg:svg");

        //.. make our chart... does it make sense to reinstantiate chart each time? can we still do transition
        svg = selection
                .attr("class", "predChart")
                .attr("width", width)
                .attr("height", height);
        
        var lastX = data.length * everyK;
        x = d3.scale.linear()
                .domain([0, lastX])
                .range([0, width ]); //... margin is weirdly broken??
        
        
        var y = d3.scale.ordinal()
                .domain(classes)
                .rangePoints([margin.top+30,height-margin.bottom-120]);
       
        var y2 = d3.scale.linear()
                .domain([0,1])
                .range([height-margin.bottom-120, margin.top+30]);
      
        
        //.. Add an x axis
        var xAxis = d3.svg.axis()
                .scale(x) //.. use same scaling function as for x
                .orient("bottom");

        svg.append("g")
                .attr("class", "x axis")
                .attr("transform", "translate(0," + (height - margin.bottom*2) + ")") //.. 0,0 refers to 0,height
                .call(xAxis);
        
        //.. Add a y axis
        var yAxis = d3.svg.axis()
                .scale(y)
                .orient("left")
                .ticks(data.length);

        calcY = function(d,i) {
            if (classes.length >2)
                return (y(d.guess) + ((i % 3 == 0) ? 0 : ((i % 3 == 1) ? 10 : - 10)));
            return ((d.guess == classes[0]) ? y2(d.confidence) : y2(1-d.confidence)); //.. invert prediction if its not first class
        }
        svg.append("g")
                .attr("class", "y axis pred")
                .call(yAxis);
        
        var tip = d3.tip()
                .attr('class', 'd3-tip')
                .offset([-10, 0])
                .html(function(d) {
                    return "<strong> " + d.guess+"</strong> <span style='color:lightcrimson'> : " + d.confidence + "</span>";
                })
        svg.call(tip);
        
        var rect = svg.selectAll("rect");  
        rect.data(data).enter().append("rect")
           .attr("y", function(d, i) {return calcY(d,i);}) //.. so that they dont overlap
           .attr("x", function (d, i) {return x(i*everyK);})
           .attr("width", x(length)) //. scale based on how many elements
           .attr("height", function(d) {return 10})
           .attr("class", "corSquare")
           .style("fill", function(d) { return ((d.guess == d.answer) ? "green" : "red")})
           .attr("opacity", function(d) {return (d.confidence)})
           .attr("id", function(d,i) {return recClass +i})
           .on("mouseover",  tip.show)
           .on("mouseout", tip.hide);
    }
    
    chart.data = function(arr) {
        data = arr;
        rectMargin = width / arr.length;
        if (rectMargin > rectWidth) rectWidth = rectMargin;
        return chart; //.. for chaining
    }
    
    chart.instance = function(readEvery, instanceLength) {
        everyK = readEvery; 
        length = instanceLength;
        return chart; //.. for chaining   
    }
    chart.width = function(w) {
        width = w;
        return chart;
    }
    chart.height = function(h) {
        height =h;
        return chart;
    }
    
    chart.classes = function(arr) {
        classes = arr;
        return chart; //.. for chaining
    }
    
    /*Return an object that matches what actual data might look like*/
    chart.getTestData = function() {
        var JSONobj = new Object();
        JSONobj.every =3;
        JSONobj.length =3;
        JSONobj.classes = new Object(); JSONobj.classes.values = ["a","b","c"];
        var d = new Array();
        for (var i = 0; i < 10; i++) {
            d.push({guess: "a", answer: "b", confidence: 0.99});
        }
        for (var i = 0; i < 10; i++) {
            d.push({guess: "b", answer: "b", confidence: 0.55});
        }

        for (var i = 0; i < 10; i++) {
            d.push({guess: "c", answer: "b", confidence: 0.93});
        } 
        JSONobj.predictions = d;
        return JSONobj;
    }
    return chart;

}

//testCM();
function testCM() { 
    var chart = PredictionChart();
    var everyK =3;
    var length = 3;
    
    var classes = ["a","b","c"];
    //.. add chart
    var selection = "#topRight";
    
    var d = new Array();
    for (var i = 0; i < 10; i++) {
        d.push({guess: "a", answer: "b", confidence: 0.99});
    } 
    for (var i = 0; i < 10; i++) {
        d.push({guess: "b", answer: "b", confidence: 0.55});
    } 
    
    for (var i = 0; i < 10; i++) {
        d.push({guess: "c", answer: "b", confidence: 0.93});
    } 
    var obj = chart.getTestData();
    chart.data(obj.predictions).instance(obj.every,obj.length).classes(obj.classes.values)(selection);
   
}