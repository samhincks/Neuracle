/**
 * Tracks is a sequence of songs with multiple versions in order they will be played
 * **/
function AdaptiveChords(tracks, trackLength) {
    
    this.iteration = 0; // At the beginning, we only play track 0
    
    this.goHard = true;
    
    this.play = function() {
     
        var self = this;
        self.advance();
        window.setInterval(function () {
            self.advance();
        }, 27900);
        console.log('set interval')
    }
    
    // todo: this should make you either go hard or go soft
    this.setState = function() {
        
    }
    
    this.advance = function() {
        for (var i = 0; i < this.iteration+1; i++) {
           tracks[i].play(this.goHard);
        }
        this.iteration++;
    }
    
}