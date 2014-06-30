
$(document).ready(function() {
    $("input").focus();
    introduce();
    $.post("http://localhost:8080/TimeMine/DataLayers.action", null, reloadDataLayers);

    //.. if you press enter, append a message to console with the user's message
    //... remove input and then give a response
    $(document).keypress(function(e) {
        if(e.which == 13) {
            
            //.. get text
            var userText = $("#userinput").val();
            //.. make an element out of it
            displayMessage("> " +userText, "usermessage", "");
            
            //.. remove the text
            $("#userinput").val("");            //.. append the machine's response
            $("#consoleInput").val(userText);
            console.log("userText : " + userText);
            var form = $('#consoleForm'); 
            $.post("http://localhost:8080/TimeMine/Console.action", form.serialize(), returnFromInput);
            
            scrollToBottom();
        } 
        
    });
    
  
     function scrollToBottom() {
            //.. scroll to end of input//.. refocus on our input element
        $("#console").each( function() 
        {
            // certain browsers have a bug such that scrollHeight is too small
            // when content does not fill the client area of the element
            var scrollHeight = Math.max(this.scrollHeight, this.clientHeight);
            this.scrollTop = scrollHeight - this.clientHeight;
        }); 
         
     }
     function returnFromInput(xhr) {
           var JSONobj = eval('('+ xhr +')'); 
           if (JSONobj.error != null)
              displayMessage(JSONobj.error, "saybmess", "firstline");
           else if(JSONobj.content != null)
              displayMessage(JSONobj.content, "saybmess", "thirdline");
          
           //.. is there some action to complete here? A new dataset to reload
           if(JSONobj.action != null && JSONobj.action == "reload") {
               $.post("http://localhost:8080/TimeMine/DataLayers.action", null, reloadDataLayers);

           }
     }
     
     
    function introduce() {
           var message = "Load a file."; 
           displayMessage(message, "saybmess", "secondline");
    }
   
    
    function displayMessage(message, primaryClass, secondaryClass) {
            ///.. ;; denotes splitting into new message
            var splitByNewMessage = message.split(";;");
            for (var i = 0; i < splitByNewMessage.length; i++) {
                var subMessage = splitByNewMessage[i];
                
                //.. :: denotes splitting by line break
                var splitByNewLine = subMessage.split("::");
                for (var j = 0; j < splitByNewLine.length; j++) {
                    var subSubMessage = splitByNewLine[j];
                     
                
                    var d = $("<div></div>").text(subSubMessage);
                    d.addClass(primaryClass);
                    d.addClass(secondaryClass);
                    $("#pastmessages").append(d);
                }
                //.. if its not the last one
                if (i!= splitByNewMessage.length-1) 
                   newLine();
                

            }
             setTimeout(function() { scrollToBottom();}, 20);
    }
       

    function newLine() {
       $("#pastmessages").append("</br>");
    }       

     
});


     /** Reload each datalayer*/
     function reloadDataLayers(xhr) {
          var JSONobj = eval('('+ xhr +')'); 

          for (var i =0; i< JSONobj.datalayers.length; i++) { 
            //.. the datalayer that should be reloaded
            var idName = JSONobj.datalayers[i].id;
            
            //.. 1) Remove it if it exists already
            $("#"+idName).remove();
            
            //.. 2) Append a new version
            var elementTag = '<div id = "'+idName+'" class = "dropChannel" > </div>';
            $("#content").append(elementTag);
          } 
           
          init();

     }
     function backFromGettingLayers(xhr) {
           var JSONobj = eval('('+ xhr +')'); 
           for(var i = 0; i<JSONobj.dataLayers.length; i++) {
               console.log(JSONobj.dataLayers[i]);
           }
     }






















