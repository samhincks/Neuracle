/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package filereader;

import java.util.ArrayList;

/**
 * A collection of labels for run-time calculation of a channel set's points.
 * Note a Labels object does not refer to a condition. It's meant to mirror data
 * points. It does not make to keep this object in scope once we have labeled all our points.
 */
public class Labels {
    public ArrayList<Label> channelLabels = new ArrayList();
    public String labelName;   
    private String wekaString;
    private ArrayList<String> uniqueLabels;

    public Labels(String labelName) {
        this.labelName = labelName.trim();
        this.labelName = this.labelName.toLowerCase();
    }
    
    public void append(Labels l2) throws Exception {
        for(Label l : l2.channelLabels) {
            this.addLabel(l);
        }
    }
    
    public Labels getCopy() {
        Labels labels = new Labels(this.labelName);
        for (Label l : channelLabels) {
            labels.addLabel(l);
        }
        return labels;
    }

    
    /**Iterate through all the labels and discover the unique label
     */
    public ArrayList<String> getAvailableLabels() {
        if (uniqueLabels != null) return uniqueLabels;
        
        ///.. initialize then add each label just once
        uniqueLabels = new ArrayList();
        for(Label l : channelLabels) {
            if (!uniqueLabels.contains(l.value))
                uniqueLabels.add(l.value);
        }
        
        return uniqueLabels;
        
    }
    
    /**Return string structure as Weka wants it, ie 1,2,3*/
    public String getWekaString(){
        if (wekaString != null) return wekaString;
        
        ArrayList<String> allLabels = getAvailableLabels();
        String retString ="";
        for(int i=0; i< allLabels.size(); i++) {
            retString += allLabels.get(i);
            if (i!=allLabels.size()-1)
                retString+=",";
        }
        return retString;
    }

    public void addLabel(Label label) {
        channelLabels.add(label);
    }

}
