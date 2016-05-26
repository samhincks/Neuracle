/** With ALL the data known about some object, compute the probability of various features.
 * I could even give this guy visual representation on the screen, and I could calibrate it
 **/
function Classifier() {
    var data; //.. 2D array of raw data
    var channelsToShow;
    var slopes; //.. 2D array of slope data at time point X. Wary: This shit is NOT thread safe as written. JAVASCRIPT IS JUST ONE THREAD, even though it feels like many with asynchronous server requests
    var NUMCHANNELS =16;
    this.readingsBack = 60;
    this.threshold = 1;
    this.channel = 0;
    this.channel2 =1;
    this.correlations =[];
    this.scaledCorrelations =[];
    
    //.. this is a bit different, btu I'm going to associate HRV and HR with this guy too. I'm going to have so that it just gets recorded here
    this.heartrate =0;
    this.heartratevariability=0;
    
    this.resetCorrelations = function() {
        this.correlations = new Array();
        this.scaledCorrelations = new Array();
    }
    this.initialize = function() {
        data = new Array();
        slopes = new Array();
        for (var i = 0; i < NUMCHANNELS; i++) {
            data.push(new Array());
            slopes.push(new Array());
        }
    }
    
    //.. returns the indexes of the 2 most anti-correlated channels, and their correlation coefficient
    this.getSmallestCorrelation = function () {
        var smallest =2; //.. since corr between -1 and 1
        var smallestI;
        var smallestJ;
        for (var i=0 ;i < 16; i++) {
            for (var j=0; j < 16; j++) {
                if (this.isHBO(i) && this.isHBO(j)){
                    var corr =  ss.sampleCorrelation(data[i], data[j]);
                    if (corr < smallest) {
                        smallest = corr;
                        smallestI = i;
                        smallestJ = j;
                    }
                }
            }
        }
        return [smallest, smallestI, smallestJ];
    }
    this.isHBO = function (i) {
        if (i == 1 || i ==2 || i==3  || i==9 || i==10 || i==11) return true;
        return false;
    }
    
    //.. get correlation at current slice 
    this.getCorrelationKBack = function (i,j) {
        var x = data[j].slice(data[j].length - this.readingsBack, data[j].length - 1);
        var y = data[i].slice(data[i].length - this.readingsBack, data[i].length - 1);
        
        var corr = ss.sampleCorrelation(x, y);
        var scale = d3.scale.linear().domain([d3.min(this.correlations), d3.max(this.correlations)]).range([-1, 1]);
       // var scaledCorr = scale(corr);
        var avg = ss.mean(this.correlations);
        var dev = ss.standardDeviation(this.correlations);

        var diff = corr - avg;
        var deviationsAway = diff / dev;
        
        this.correlations.push(corr);
        //this.scaledCorrelations.push(scaledCorr);
        
        //console.log(corr);
        //console.log(ss.sampleCovariance([0,1,2,3,4,5],[1,2,3,4,5,3]));
        //console.log(corr, scaledCorr, x.length, y.length, x[0], y[0]);
        //console.log(x, y);

        return [corr, deviationsAway];
    }
    
    this.addData = function(JSONobj) {
        var channelVals = JSONobj.data.data; //.. the data contained now should be what's added
        var numUpdates = channelVals.values.length;
       
        for (var i = 0; i < numUpdates; i++) {
            //.. push all the data to be saved 
            for (var j =0; j < NUMCHANNELS; j++) {
                var val = channelVals.values[i][j][0];
                data[j].push(val);
                if (data[j].length > this.readingsBack+1) {
                    var x = data[j].slice(data[j].length - this.readingsBack, data[j].length-1); //.. only last readings back values
                    slopes[j].push(getSlope(x));
                }
            }
        }
    }
    
    var getSlope = function(x) {
        var reg = new Array();
        //.. add each to tuple like array for copmuting linear regression
        for (var k = 0; k < x.length; k++) {
            var s = [x[k], k];
            reg.push(s);
        }
        var slope = ss.linearRegression(reg).m;
        return slope;
    }
    
    this.recomputeSlopes = function() {
        var every = 10;
        slopes = new Array();
        for (var j =0; j < data.length; j++) {
            slopes[j] = new Array();
            var vals = data[j];
            for (var i=0; i< vals.length; i+=every) {
                var x = data[j].slice(i, i+this.readingsBack); //.. only last readings back values
                slopes[j].push(getSlope(x));
            }
        }
    }
    
    /**Get slope / probability of it at specified channel. 
     **/
    this.getSlope = function(i) {
        //.. scale the slopes
        var scale = d3.scale.linear().domain([d3.min(slopes[i]), d3.max(slopes[i])]).range([-1, 1]);
        var vals = slopes[i].map(scale); //.. cant believe this is the first time I use this! 
        var val = vals[vals.length-1];
        
        var avg = ss.mean(vals);
        var dev = ss.standardDeviation(vals);
        
        var diff = val - avg;
        var deviationsAway = diff / dev;
        //console.log(avg,dev,diff,deviationsAway);

        return [val,Math.abs(deviationsAway)];
    }
 
}