function AdaptiveSong(context) {
    this.context_ = context;
    
    // for starters, just play a song when this is hit
    
    console.log('playing song');
    this.masterGain_ = new GainNode(this.context_, {gain: 0.5});
 
    var instrumental = new Howl({
            urls: ['songs/ocean-yawning.mp3']
    });
    
    var vocals = new Howl({
            urls: ['songs/damned-whale.mp3']
    });
    
    this.vocalsVolume = 0;
    this.instrumentalVolume = 1;
    var lastTime = 0; 
    var scoreInLastSecond = 0;
    this.window = [];
    var calls =0;
    var FADEDURATION = 1; // should be shorter than frequency updateSong is called
    
    vocals.volume(this.vocalsVolume);
    instrumental.volume(this.instrumentalVolume);
    
    this.playSong = function () {             
        vocals.play();    
        instrumental.play();
    }
    
    this.updateWithHbo = function(hbo) {
        var t = new Date();
        var thisTime = t.getSeconds();
        
         
        // update statistics and break if its in the same second as last
        if (thisTime == lastTime) {
            calls++;
            scoreInLastSecond += hbo;  
            return;
        }
        else {
            // TODO THIS SHOULD USE SMOOTHING ABOVE
            if (calls > 0) {
                this.window.push(scoreInLastSecond);
                calls =0;
                scoreInLastSecond = 0;
            }
            
                // every five seconds
            if (thisTime % 5 == 0) {
                var slope = (this.window[this.window.length-1] - this.window[0]) / this.window.length;
                if (slope > 0) {
                    console.log('increasing vocals');
                    vocals.fade(this.vocalsVolume, 1, FADEDURATION);
                    instrumental.fade(this.instrumentalVolume, 0, FADEDURATION);
                    this.vocalsVolume = 1;
                    this.instrumentalVolume = 0;
                }
                else {
                    console.log('increase dry');
                    vocals.fade(this.vocalsVolume, 0, FADEDURATION);
                    instrumental.fade(this.instrumentalVolume, 1, FADEDURATION);
                    this.vocalsVolume = 0;
                    this.instrumentalVolume = 1;
                }
                this.window = [];
            }
        }
        lastTime = thisTime;
    }
    
    this.updateSong = function(standardScore, newTime) {
        
        //INVARIANT: this gets updated every second with mean
        
        var t = new Date();
        var thisTime = t.getSeconds();
        
        // update statistics and break;
        if (thisTime == lastTime) {
            calls++;
            scoreInLastSecond += standardScore;
            return;
        }
        
        // reset averaging and set the one to be changed to mean
        standardScore = scoreInLastSecond / calls;
        calls = 0;
        scoreInLastSecond =0;
        lastTime = thisTime;
        
        
        // if 1.0 or higher, then vocalsVolume is 1 and instrumental Volume is 0
        // If -1.0 or lower, then instrumental is 1 and vocals is 0
        // if 0, then each is 0.5
        
        var newVoc;
        var newIns;
        
        if (standardScore > 1 ){// fade(from, to, duration, [id])
            newVoc = 1;
            newIns = 0;
            vocals.fade(this.vocalsVolume, newVoc, FADEDURATION);
            instrumental.fade(this.instrumentalVolume, newIns, FADEDURATION);
        }
        
        if (standardScore < -1 ){// fade(from, to, duration, [id])
            newVoc = 0;
            newIns = 1;
            vocals.fade(this.vocalsVolume, newVoc, FADEDURATION);
            instrumental.fade(this.instrumentalVolume, newIns, FADEDURATION);
        }
        else {
            var newVoc = 0.5 + (standardScore / 2); // e.g 0.1 becomes 0.55
            var newIns = 0.5 - (standardScore / 2); // e.g. 0.1 becomes 0.45
            
            vocals.fade(this.vocalsVolume, newVoc, FADEDURATION);
            instrumental.fade(this.instrumentalVolume, newIns, FADEDURATION);
        }
        
        console.log(this.vocalsVolume);
        console.log(this.instrumentalVolume);
        this.vocalsVolume =newVoc;
        this.instrumentalVolume = newIns;
                
    }
    

}