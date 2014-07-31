/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var StreamChart = function (){
    var added=0;
    this.displayChart = function(JSONobj, chart, data){
       /** What it wants:
        *  - An object with the following:
        *  .values = each channels value at this timestamp
        *  .start = the first index to displayed on
        *  .end = the last index shown
        *  .maxTime = the last index shown adjusted for seconds
         */
        var channelVals = JSONobj.data.data; //.. the data contained now should be what's added
        for (var i=0; i < channelVals.values.length; i++) {
            var updateData;
            updateData.values = channelVals.values[i];
            updateData.maxTime = channelVals.maxTime;
            updateData.start = added;
            updateData.end = channelVals.end + added;
            console.log(updateData);
            chart.slideData(updateData);
            added++;
        }
    }
   /* else {
        chart.drawLinePlot(JSONobj);
    }*/
    /*
    else {
        console.log(JSONobj.data);
       // data = {"start":1,"end":10000,"step":100,"names":["l"], "values":[[15820.0101840488, 15899.7253668067, 16047.4476816121, 16225.0631734631, 16321.0429563369, 16477.289219996, 16372.5034462091, 16420.2024254868, 16499.3156905815, 16422.1844610347, 16419.7447928312, 16602.0198900243, 16795.2846238759, 16708.9466016093, 16709.8158889291, 16796.7377507963, 16814.8517758747, 16944.4126048633, 16959.6935058422, 17249.8381137218, 17589.8424377422, 17531.9557988989],[15820.0101840488, 15899.7253668067, 16047.4476816121, 16225.0631734631, 16321.0429563369, 16477.289219996, 16372.5034462091, 16420.2024254868, 16499.3156905815, 16422.1844610347, 16419.7447928312, 16602.0198900243, 16795.2846238759, 16708.9466016093, 16709.8158889291, 16796.7377507963, 16814.8517758747, 16944.4126048633, 16959.6935058422, 17249.8381137218, 17589.8424377422, 17531.9557988989],[15820.0101840488, 15899.7253668067, 16047.4476816121, 16225.0631734631, 16321.0429563369, 16477.289219996, 16372.5034462091, 16420.2024254868, 16499.3156905815, 16422.1844610347, 16419.7447928312, 16602.0198900243, 16795.2846238759, 16708.9466016093, 16709.8158889291, 16796.7377507963, 16814.8517758747, 16944.4126048633, 16959.6935058422, 17249.8381137218, 17589.8424377422, 17531.9557988989],[15820.0101840488, 15899.7253668067, 16047.4476816121, 16225.0631734631, 16321.0429563369, 16477.289219996, 16372.5034462091, 16420.2024254868, 16499.3156905815, 16422.1844610347, 16419.7447928312, 16602.0198900243, 16795.2846238759, 16708.9466016093, 16709.8158889291, 16796.7377507963, 16814.8517758747, 16944.4126048633, 16959.6935058422, 17249.8381137218, 17589.8424377422, 17531.9557988989]]};
         dataA = {"start":10,"end":10,"step":1,"names":["l"],"values":[[1],[2],[3],[4]]};

//
        // add presentation logic for 'data' object using optional data arguments
        data["colors"] = ["green", "blue"];
        data["scale"] = "pow";



        setInterval(function() {

            // for each data series ...
            var newData = [];
            data.values.forEach(function(dataSeries, index) {
                // take the first value and move it to the end
                // and capture the value we're moving so we can send it to the graph as an update
                var v = dataSeries.shift();
                dataSeries.push(v);
                // put this value in newData as an array with 1 value
                newData[index] = [v];
            })

            // we will reuse dataA each time
            dataA.values = newData;
            // increment time 1 step
            dataA.start = dataA.start + dataA.step;
            dataA.end = dataA.end + dataA.step;

            l1.slideData(dataA);
        }, 100);
        */ 

}
