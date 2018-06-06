function Track(name, hardUrl, softUrl) {
 
    var soft = new Howl({
            urls: softUrl//['songs/ocean-yawning.mp3']
    });
    
    var hard = new Howl({
            urls: [hardUrl]//['songs/damned-whale.mp3']
    });
   
    this.played = [];
    
    this.play = function (playHard) {
        console.log("playing");
        if (playHard) hard.play();
        else soft.play();
        
        if (playHard) this.played.push(name+"hard");
        else this.played.push(name+"soft");
    }
    
    

}