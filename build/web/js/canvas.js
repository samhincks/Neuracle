
function Canvas() {
    var canvasTag = "#content"; 
    
    this.appendDataLayer = function(elementTag) {
         $(canvasTag).append(elementTag);
    }
}
