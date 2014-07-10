/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function DescriptionArea(id) {
    var selection = id;
    this.displayedDL = ""; //.. set to currently displayed datalayer


    /** A lightweight stream of a datalayer. Inside a little information box, display a table
     * describing the channel.
     * Object must have .id , .channels , and .points
     **/
    this.displayDescription = function(JSONobj) {
        //.. remove existing selection and append
        $(selection).children().remove();
        $(selection).append("<div id = descriptionBox class = infoBox> </div>");
        var description = $("#descriptionBox");

        //.. set the text
        var text = "ID: " + JSONobj.id + "  /  CHANNELS: " + JSONobj.channels + "  / POINTS: " + JSONobj.points;
        description.text(text);

        this.displayedDL = JSONobj.id;
    }

    /** A lightweight stream of a technique
     **/
    this.displayTechniqueDescription = function(JSONobj) {
        //.. remove existing selection and append
        $(selection).children().remove();
        $(selection).append("<div id = descriptionBox class = infoBox> </div>");
        var description = $("#descriptionBox");

        //.. set the text
        var text = "ID: " + JSONobj.id + "  /  TYPE: " + JSONobj.type;
        if (JSONobj.value != null)
            text += "  /  VALUE: " + JSONobj.value;
        description.text(text);

        this.displayedDL = JSONobj.id;
    }
}
