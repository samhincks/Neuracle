/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**Collection of available techniques*/
function Techniques() {
    this.techniques =  new Array(); //.. all available technique
    var selectedId; //.. set this when an id is selected
    
    //.. get this technique by id; return null if it doesn't exist'
    this.getTechniqueById = function(id) {
        for(var i =0; i< this.techniques.length; i++) {
            if (this.techniques[i].id == id)
                return this.techniques[i];
        }
        return null;
    } 
    
    //.. add Technique if it does not exist
    this.addTechnique= function(technique){
        if(!this.getTechniqueById(technique.id)) { //.. if we dont have it
            this.techniques[this.techniques.length] = technique;
        }
        this.selectTechnique(technique.id);
    }
    
    //.. add the selected to class to target element; remove from all others
    this.selectTechnique = function(id) {
        //.. remove all other 
        for(var i =0; i< this.techniques.length; i++) {
             $("#"+this.techniques[i].id).removeClass("surfaceElementSelected");
        }
        $("#"+id).addClass("surfaceElementSelected");
        selectedId = id; 

    }
    
    
    //.. return the selected id
    this.getSelected = function() {
        return selectedId;
    }
}


//.. Five Types: Settings, Filters, Selection, Features, ML
function Technique(jsonTech) {
    this.id = jsonTech.id;
    this.type = jsonTech.type;
    this.elementTag = '<div id = "'+this.id+'"class = "technique surfaceElement"> </div>';

    this.trained =false;
    this.initializePlumb = function() {
        if (this.type =="Classifier") {
            $("#"+this.id).addClass("classifierT");    
           // plumbTechniques.setClassifierEP(this.id);
        }
        
        else if(this.type =="FeatureSet") {
            $("#"+this.id).addClass("featureSetT");        
            //plumbTechniques.setFeatureSetEP(this.id);
        }
        
        else if(this.type =="Settings") {
            $(this.elementTag).addClass("settingsT");        
           // plumbTechniques.setSettingsEP(this.id);
        }
        
        else if(this.type =="AttributeSelection") {    
            $("#"+this.id).addClass("attributeSelectionT");    
            //plumbTechniques.setAttributeSelectionEP(this.id);
        }
        
        jsPlumb.draggable($("#"+this.id));
    }

    
}