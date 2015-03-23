function CorrelationMatrix() {
    var data; //.. a 2D array of 'nearness' correlations 

    /**CONFIGURATION VARIABLES**/
    //.. width, height, margin. If these are customizable we need settors 
    var width = 400,
            height = 400,
            margin = {top: 20, left: 30, right: 30, bottom: 20};
    
    var selection;
   
    function chart(s) {
        console.log(s);
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
        
        
        var rectWidth = width/ data.length;
        //.. We have this doubly nest dataset. [0][0] is in the bottom corner. [51][51] is topright corner
        //.. paint a box at this position, colored by correlation value 
        var rect = svg.selectAll("rect");  

        for (var k =0; k < data.length; k++) {
            var correlations = data[k];
            var max = d3.max(data[k]);
            var normalize = d3.scale.linear().domain([0, max]).range([1, 0]);
            rect.data(correlations).enter().append("rect")
               .attr("y", function(d, i) {return  i*rectWidth;})
               .attr("x", function (d, i) {return k*rectWidth;})
               .attr("width", rectWidth) //. scale based on how many elements
               .attr("height", function(d) {return rectWidth})
               .attr("class", "corSquare")
               .attr("opacity", function(d) {return normalize(d)});
        }
    }
    
    chart.data = function(arr) {
        data = arr;
        return chart; //.. for chaining
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
            s.push(100 *Math.random());
        }
        d.push(s);
    }
    var max = d3.max(d[0]);
    var normalize = d3.scale.linear().domain([0, max]).range([1, 0]);
    console.log(max + " , " + normalize(3));
    chart.data(d)(selection);
   
}