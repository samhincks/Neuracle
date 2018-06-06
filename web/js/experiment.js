function Experiment() {
    
    var audio = []; // This is an array of Songs. The experiment shuffles between songs
    
    this.iterations = 0;
    this.qNum = 0;
    this.numQuestions = 3;
    var prefix = "songs/";
    var suffix = ".mp3";
    /*      this.songs = ['peco-theme', 'chaotic-fantasies', 'demogorgan', 'carlsson',
        'eerier-things', 'elentrith', 'bengal-tiger', 'death-of-a-pale-man',
        'ocean-yawning', 'entropic-bci']; */
   // this.songs = ['peco-theme', 'demogorgan', 'carlsson'];
    
    this.songs = ['peco-theme', 'chaotic-fantasies', 'mean-in-december', 'ocean-yawning'];

    //.. Dark Mix with ben on 5/4/2018
   /* this.songs = ['bengal-tiger', 'bit-from-it', 'death-of-a-pale-man', 'crying-in-fanguar',
        'cold-to-the-touch', 'mean-in-december', 'wreckage', 'ocean-yawning']; */ 
        
    //this.songs = ['whip', 'whip'];
    this.nextSong = this.songs[this.iterations];
    this.awatingFeedback = false;
    this.playingAudio = false;
    this.returnsFromDMNThisTrial = 0;
    var restLength = 3000;
    
    this.initiateAudio = function() {
        //. 1.. Start some audio sequence, lasting the duration of some audio clip
        //..2. Send a label to the back end so that the song gets labeled with the name of that song
        //..3  Initiate methods for sublabeling the dataset, with associated conditions
        console.log("initiating audio");
        
        this.verifySongs(this.songs);
        
        this.playSong(this.songs[0]);
        
        
       // var track = new Track('peco', 'songs/peco-theme.mp3', 'songs/ocean-yawning.mp3');
       // track.play();
    }
    
    this.verifySongs = function(songs) {
        for (var i = 0; i < songs.length; i++) {

            var instrumental = new Howl({
                urls: [prefix + songs[i] + suffix]
            });        
        }
        instrumental.play();
        instrumental.stop();
    }
    
    this.playSong = function(song) {
        var self = this;
        
        var message = "label(" + "realtime1" + "," + "condition" + ","+ song+")";
        $("#consoleInput").val(message);
        javaInterface.postToConsole();
        
        var instrumental = new Howl({
                urls: [prefix+song+suffix],
                onend: function (d) { experiment.solicitFeedback();}
        });
        instrumental.volume(1);
        instrumental.play();
        this.playingAudio = true;
        this.returnsFromDMNThisTrial = 0;
        
    }
        
    this.rest = function() {
        this.iterations++;
        this.playingAudio = false;

        if (this.iterations >= this.songs.length) {
            consoleArea.displayMessage("Thank you for participating! Please alert experimenter that the experiment is over.", "systemmess", "greenline");
            return;
        }
        this.nextSong = this.songs[this.iterations];
        var message = "label(" + "realtime1" + "," + "condition" + ","+ "rest)";
        $("#consoleInput").val(message);
        javaInterface.postToConsole();
       
       
        setTimeout(function() {experiment.playSong(experiment.nextSong)}, restLength);
    }
    
    //..  Query user's reported mental load, then go on. 
    this.solicitFeedback = function() {
        this.awaitingFeedback = true;
        this.playingAudio = false;
        if (this.qNum == 0) {
            var awarenesses = "awa-zero";
            if (this.returnsFromDMNThisTrial == 1)
                awarenesses = "awa-one"
            
            else if (this.returnsFromDMNThisTrial < 5)
                awarenesses = "awa-two-to-four";
            
            else
                awarenesses = "awa-five-plus";
            
            console.log(awarenesses);
            
            var message = "label(" + "realtime1" + "," + "awareness" + ","+ awarenesses +")";
            $("#consoleInput").val(message);
            javaInterface.postToConsole();
            
            console.log(awarenesses);
            
            
            var message = "label(" + "realtime1" + "," + "condition" + ","+ "feedback)";
            $("#consoleInput").val(message);
            javaInterface.postToConsole();
            
            
        }
        setTimeout(function(){experiment.askQuestion()}, 1000);
        
    }
    this.askQuestion = function() {
        if (this.qNum == 0) {
            consoleArea.displayMessage("How focused were you on the music?", "systemmess", "blueline");
            consoleArea.displayMessage("Enter 1 for 'Mainly focused on my thoughts' and 2 for 'Mainly focused on the music'", "systemmess", "greenline");

        }

        if (this.qNum == 1) {
            consoleArea.displayMessage("Were your thoughts and sensations automatic or deliberate?  ", "systemmess", "blueline");
            consoleArea.displayMessage("1 = 'Thoughts and sensations emerged automatically, controlling me' and 2 = 'I controlled the partitioning of my attention between thoughts and sensations')", "systemmess", "greenline");

        }

        if (this.qNum == 2) {
            consoleArea.displayMessage("Did you like the song?", "systemmess", "blueline");
            consoleArea.displayMessage("1 = 'Not my taste', 2 = 'I liked it', 3 = 'I loved it'", "systemmess", "greenline");
        }
    }
    
     //.. from console area  - parse the feedback, and send it to the backend
    this.parseFeedback = function(input) {
        
        this.awaitingFeedback =true;
        
        var conValue = null;
        //.. if its valid input, send a message to the back end saying -- whatever was the label,
        //... add a new label feedback1, if it doesnt exist, and make so that it has this value for as many back as the last trial
        if (input == "1") {
           conValue = "one";
        }
        
        else if (input == "2") {
            conValue = "two";
        }
        
        else if (input == "3") {
            conValue = "three"; 
        }
        
        else if (input == "4") {
            conValue = "four"; 

        }
        else if (input == "5") {
            conValue = "five";
        }

        else {
            consoleArea.displayMessage("Illegal input", "systemmess", "redline");
            this.solicitFeedback();            
        }
        
        //.. if its a sensible response ship it off to the back-end
        if (conValue != null) {
            var mess;
            if (this.qNum == 0) {
                var origin = "music";
                if (conValue == "one") {
                    origin = "thoughts";
                }
                mess  = "retrolabel(origin"+",condition,"+origin+",0,realtime1)";
            }
            if (this.qNum == 1) {
                var control = "top-down";
                if (conValue == "one") {
                    control = "bottom-up";
                }
                mess  = "retrolabel(control"+",condition,"+control+",0,realtime1)";
            }
            
            if (this.qNum == 2) {
                mess  = "retrolabel(rating"+",condition,"+conValue+",0,realtime1)";
            }
             
            $("#consoleInput").val(mess);
            javaInterface.postToConsole();
        }
        
        //.. if theres a feedback2, then collect that
        if (this.qNum < this.numQuestions-1){
           this.qNum++;
           this.solicitFeedback();             
        }
        
        //.. otherwise continue with normal labeling procedure
        else{
            this.awaitingFeedback = false;
            this.qNum =0;
            this.rest();
        }
            
    }
    
    function endAudio() {
        //.  1. Send label [questions] to condition of back-end.
        //.. 2. Initiate askQuestions
    }
    
    function initiateNBack() {
        // After experiment, we should initiate an easy n-back, with reaction time
        // as principal measure. If the user has "less default mode network activity"
        // as a result of the meditation, then presumable they will have quicker reaction
        // times in the n-back
    }
    
    function askQuestions () {
        //.. 1. Display questions in the console about the experience users just had,
        // and make so that the answers are labeled. In the CSV.
        // Questions include:
        // - Were you more focused on the sound (type sounds) or your thoughts (thoughts)
        // - Did you think about other matters than the sound during the experiment?
        // - What was the audio about? (please type one sentence)
        // - Were you thinking about more than the three categories of thought?
        // - Was your thinking more "chaotic and meandering) or cohesive, clear, and sustained.
        // - On a scale from [0 = focus on song] to [5=daydreaming] 
        // The last question is, are you ready to start the next trial (Y/N)?
        
        // When user hits Y, then the next audio begins.
        
    }
}