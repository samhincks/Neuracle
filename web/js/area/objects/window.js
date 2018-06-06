/**
 *  A window of fNIRS data. The classifier has an array of windows. It wants
 *  to continuously add to the current window, but the addData function returns
 *  false (meaning it was unsuccesful) when the data is going in some new direction.
 *  A given window has a slope that is positive or negative, and a new window is created
 *  when that shifts. But we limit false positives by verifying that both Hb and HbO
 *  are anti-correlated in this shift.
 *  
 *  The number of readings back to determine whether to create a new window is an
 *  unknown which we optimize. I guess 1 to 3 seconds.
 **/

function Window () {
    var NUMCHANNELS =16;
    this.readingsBack = 30; // Arbitrary: ~3 seconds of data at 11 hz for determining new window or not
    
    var data = new Array(); // 2 dimensional array of data
    for (var i = 0; i < NUMCHANNELS; i++) {
        data.push(new Array());
    }
    
    var slopes = new Array(); // the slope of each channel at thsi window
    var hboChanIndex = 5; //  Double check these - but these are the two channels, which should have particular anti-correlation
    var hbChanIndex = 7; // 
    
    this.addData = function(channelVals, numUpdates) {
        
        // Note, we are adding to the data no matter what - and we may be creating a new window
        // in which case, the tail of this window and head of the new window will be duplicated. 
        for (var i = 0; i < numUpdates; i++) {
            
            //.. push all the data to be saved 
            for (var j = 0; j < NUMCHANNELS; j++) {
                var val = channelVals.values[i][j][0];
                data[j].push(val);
            }
        }
        
        // Now do a reality check - do we want a new window? 
        var hboChan = data[hboChanIndex].slice(data[hboChanIndex].length - this.readingsBack, data[hboChanIndex].length - 1);
        var hbChan =  data[hbChanIndex].slice(data[hbChanIndex].length - this.readingsBack, data[hbChanIndex].length - 1);
        
        var corr = ss.sampleCorrelation(hboChan, hbChan);
        console.log(corr);
        return true;
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
    
}