

/**
 * 
 * @returns {HeatMap.chart}
 */
function HeatMap() {
    /**CONFIGURATION VARIABLES**/
    //.. width, height, margin. If these are customizable we need settors 
    var width = 700,
            height = 500,
            margin = {top: 20, left: 30, right: 30, bottom: 20};
    
    var maxX;
    var maxY;
    var data; 
    
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
                .attr("class", "predChart")
                .attr("width", width)
                .attr("height", height);
        
        
        //.. calculate desired scales
        var y = d3.scale.linear()
                .domain([0, maxY]) //.. doubly nested since a 2d array (too expensive?)
                .range([height - margin.top, 0 + margin.bottom]); //.. these need to be flipped because svg's are anchored at topleft
        var x = d3.scale.linear()
                .domain([0, maxX])
                .range([0 + margin.left, width - margin.right]);  
        
        //.. Add a path element to our visualization. USe D3's line'
        var line = d3.svg.line()
            .interpolate("basis") //.. makes jagged smooth
            .x(function(d, i) {
                return x(d.x)
            })
            .y(function(d,i) {
                return y(d.y);
            });
                
       /* svg.append("path")
            .datum(data) //.. by doing datum we reserve the right to alternate which dimension of data is shown
            .attr("class", "d3line")
            .attr("d", line)
            .style("stroke", function(d) {
                return "blue";
            });  */
            
            
        svg.append("linearGradient")
                .attr("id", "temperature-gradient")
                .attr("gradientUnits", "userSpaceOnUse")
                .attr("x1", 0).attr("y1", y(50))
                .attr("x2", 0).attr("y2", y(60))
                .selectAll("stop")
                .data([
                    {offset: "0%", color: "black"},
                    {offset: "50%", color: "black"},
                    {offset: "50%", color: "red"},
                    {offset: "100%", color: "red"}
                ])
                .enter().append("stop")
                .attr("offset", function(d) {
                    return d.offset;
                })
                .attr("stop-color", function(d) {
                    return d.color;
                });
         
        svg.append("path")
                .datum(data)
                .attr("class", "d3line")
                .attr("d", line) ;
                /*.style("stroke", function(d) {
                    return "blue"; }); */
            
    }
    
    chart.data = function(d) {
        data = d;
    }
    chart.maxX = function(x) {
        maxX = x;
    }
    
    chart.maxY = function(y) {
        maxY = y;
    }
    
    return chart;

}

function testData() {
    var maxX = 300;
    var data = new Array();
    
    //.. a diagonal line with random workload
    for (var i =0; i < maxX; i++) {
        var pos = new Object();
        pos.x = i;
        pos.y = i;
        pos.opacity = Math.random();
        data.push(pos);
    }
    return data;
}
//testHM();


/* Check out reading from TSV 
 */
function testHM() {
    var hm = HeatMap();
    hm.data(testData());
    hm.maxX(300);
    hm.maxY(300);
    hm();

}