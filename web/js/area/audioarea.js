

function AudioArea(context) {
    var on = true; // toggle synth on or off
    var playSong = true;
    var audioChannel = 2;
    var midFrequency = 440;
    var standardFrequency = 220; // TODO change this since sound is not linear
    
    var synth = new PolySynth(context);
    var song = new AdaptiveSong(context);
    var time = new Date().toLocaleTimeString();

    var masterGain_ = new GainNode(context, {gain: 0.5});
    synth.output.connect(masterGain_).connect(context.destination);
   
    this.streaming = false;
    this.window = []; // most recent buffer of data
    
    //x.connect(context.destination);
    //x.start();
    
    // Called when a dataset is clicked
    this.initializeWithData = function(json) {
        if (!on) return;
        var itr =0;
        this.MIN = 9999999;
        this.MAX = -99999999; // initialize at extreme values
        this.total = 0;
        
        
        // (We only examine those values specifed in channelsToShow variable
        for (var i = 0; i < json.data.values.length; i++ ){
            var channel = json.data.values[i];
            for (var j = 0; j < channel.length; j++) {
                var val = channel[j];
                this.total += val;
                itr++;
                
                if (val > this.MAX)
                    this.MAX = val;
                if (val < this.MIN)
                    this.MIN = val;                
            }
        }
        
        this.mean = this.total / itr;
        var sumOfSq = 0;
        
        for (var i = 0; i < json.data.values.length; i++ ){
            var channel = json.data.values[i];
            for (var j = 0; j < channel.length; j++) {
                var val = channel[j];
                sumOfSq += Math.pow(val - this.mean, 2);
            }
        }
        this.std = Math.sqrt(sumOfSq / itr);
        
        
         /// This can be uncommented for cross-app communication
        // this is listenedto in Documents/samhincks-old-websites
        /*$.ajax({
            url: 'http://localhost:5000/set-fnirs-data', 
            type: 'POST', 
            contentType: 'application/json', 
           // success: this.parseResponse.bind(this), 
            data: JSON.stringify({
                command:'init',
                mean:this.mean,
                std: this.std,
                min: this.MIN,
                max: this.MAX
            })}
        ) */
        
    }
    
    this.setMinMaxStd = function(data) {
        var tempMax = -99999;
        var tempMin = 99999;
        var total = 0;
        for (var i =0; i < data.length; i++) {
            var d = data[i];
            if (d> tempMax) {
                tempMax = d;
                this.MAX = tempMax;
            }
             if (d > tempMin) {
                tempMin = d;
                this.MIN = tempMin;
            }
            total += d;
        }
        
        this.mean = total / data.length;
        var sumOfSq = 0;
        
        for (var i = 0; i < data; i++ ){
            var val = data[i];
            sumOfSq += Math.pow(val - this.mean, 2);
            
        }
        this.std = Math.sqrt(sumOfSq / data.length);
        
    }
    
    
   // Called when stream is entered.
   this.createVoice = function(hbo) {
       if (!on) return
       // Max and minimum frequency depend on the data that has been seen so far.
       
       if (playSong) {
           song.playSong();
       }

       else {
           synth.playVoice("A4", this.getFrequencyFromValue(hbo)); // A4 is just an ID
       }
       this.streaming = true;
       
   }
    
   this.updateAudio = function(hbo) {
       if (!on) return;
       //console.log('ramping to ' + this.getFrequencyFromValue(hbo));
       var time = new Date();
       var newTime = time.toLocaleTimeString();
       var s = time.getSeconds();
       this.window.push(hbo);
       
       var standardScore = (hbo - this.mean) / this.std;

       if (playSong) {
           //song.updateSong(standardScore, newTime);
           
           song.updateWithHbo(hbo);

       }
       
     
       else 
            synth.rampNoteToFrequencyAtTime("A4",this.getFrequencyFromValue(hbo), STREAMINTERVAL / 1000);
             
       
       /// This can be uncommented for cross-app communication
       /*
       $.ajax({
            url: 'http://localhost:5000/set-fnirs-data', 
            type: 'POST', 
            contentType: 'application/json', 
           // success: this.parseResponse.bind(this), 
            data: JSON.stringify({
                command:'update',
                standardScore: standardScore
            })}
        ) */
       
   }
   
   this.getFrequencyFromValue = function(hbo) {
       // this.MIN = minFrequency; this.max = maxFrequency
       var standardScore = (hbo - this.mean) / this.std;
       
       return midFrequency +  (standardScore * standardFrequency);
      
   }
}