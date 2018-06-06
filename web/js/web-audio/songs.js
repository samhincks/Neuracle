function Songs() {
    
    
    this.vivacitateMe = function() {
        var tracks = [];
        
        var t = new Track('melody', 'songs/vivacitate-me/melody-hard_1.wav', 'songs/vivacitate-me/melody-soft_1.wav');
        tracks.push(t);
        
        t = new Track('chords', 'songs/vivacitate-me/chords-hard_1.wav', 'songs/vivacitate-me/chords-soft_1.wav');
        tracks.push(t);
        
        t = new Track('drums', 'songs/vivacitate-me/drums-hard_1.wav', 'songs/vivacitate-me/drums-soft_1.wav');
        tracks.push(t);
        
        t = new Track('vocals', 'songs/vivacitate-me/vocals-hard_1.wav', 'songs/vivacitate-me/vocals-soft_1.wav');
        tracks.push(t);
        
        t = new Track('bass', 'songs/vivacitate-me/bass-hard_1.wav', 'songs/vivacitate-me/bass-soft_1.wav');
        tracks.push(t);
        
        t = new Track('voice', 'songs/vivacitate-me/voice-hard_1.wav', 'songs/vivacitate-me/voice-soft_1.wav');
        tracks.push(t);
        
        t = new Track('arp', 'songs/vivacitate-me/arp-hard_1.wav', 'songs/vivacitate-me/arp-soft_1.wav');
        tracks.push(t);
        
        activeSong = new AdaptiveChords(tracks, 28000);        
    }
    
    this.play = function() {
        activeSong.play();
    }
}