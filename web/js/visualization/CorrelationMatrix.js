/**
 CorrelationMatrix: a set of variously opaque rectangles, showcasing correlation
 between channels. High opacity -> low distance between channel
  **/

function CorrelationMatrix() {
    var data; //.. a 2D array of 'nearness' correlations 

    /**CONFIGURATION VARIABLES**/
    //.. width, height, margin. If these are customizable we need settors 
    var width = 700,
            height = 400,
            margin = {top: 10, left: 30, right: 30, bottom: 20};
    
    var selection;
   
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
                .attr("class", "chart")
                .attr("width", width)
                .attr("height", height);
        
        
        var tip = d3.tip()
                .attr('class', 'd3-tip')
                .offset([-10, 0])
                .html(function(d,i) {
                    return "<strong> (" + d.i + "," + d.j+ "):</strong> <span style='color:white'> " + d.data + "</span>";
                })
        svg.call(tip);
        
        //..create dummy array indexes as our domain
        var indexes = new Array();
        for (var i =0; i < data.length;i++) {
            indexes[i] = i;
        }
        
        var x = d3.scale.ordinal()
                .domain(indexes)
                .rangePoints([margin.left, width -margin.left]);
        var rectWidth = x(1) - x(0);
        var rectMarg = rectWidth/2;
        var y = d3.scale.ordinal()
                .domain(indexes)
                .rangePoints([margin.top, height-margin.bottom-(rectMarg)]);
        
        //.. a square is whatever can fit between two values
        var rectHeight = y(1) - y(0);

        var rect = svg.selectAll("rect");  
        for (var k =0; k < data.length; k++) {
            var correlations = data[k];
            var max = d3.max(data[k], function(d) {return d.data});
            var normalize = d3.scale.linear().domain([0, max]).range([1, 0]);
            rect.data(correlations).enter().append("rect")
               .attr("y", function(d,i){return  y(i);})
               .attr("x",function(d, i) { return x(k);})
               .attr("width", rectWidth) //. scale based on how many elements
               .attr("height", rectHeight)
               .attr("class", "corSquare")
               .attr("opacity", function(d) {return normalize(d.data)})
               .on("mouseover", tip.show)
               .on("mouseout", tip.hide);
        }
        
        
        
        //.. Add an x axis
        var xAxis = d3.svg.axis()
                .scale(x) 
                .orient("bottom");
        
        var yAxis = d3.svg.axis()
                .scale(y)
                .orient("left")
                .ticks(data.length);
        
        svg.append("g")
                .attr("class", "y axis")
                .attr("transform", "translate(" + (margin.left - 5) + "," +rectMarg+")")
                .call(yAxis);
        
        svg.append("g")
                .attr("class", "x axis")
                .attr("transform", "translate("+rectMarg+"," + (height - margin.bottom+10) + ")") //.. 0,0 refers to 0,height
                .call(xAxis);
    }
    
    chart.data = function(arr) {
        data = arr;
        return chart; //.. for chaining
    }
    chart.width = function(w) {
        width = w;
        return chart;
    }
    chart.height = function(h) {
        height = h;
        return chart;
    }
    
    return chart;
}

//testCM();
function testCM() { 
    var chart = CorrelationMatrix();
    //.. add chart
    var selection = "#topRight";
    
    var d = new Array();
    for (var i = 0; i < 52; i++) {
        var s = new Array();
        for (var j =0; j<52; j++) {
            var obj = new Object();
            obj.i = i;
            obj.j = j;
            obj.data = 100 *Math.random()
            s.push(obj);
        }
        d.push(s);
    }
    var max = d3.max(d[0]);
    var normalize = d3.scale.linear().domain([0, max]).range([1, 0]);
    console.log(max + " , " + normalize(3));
    chart.data(d)(selection);
   
}