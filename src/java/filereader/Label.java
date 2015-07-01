/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package filereader;

/**A label associated with a particular datapoint. 
 * @author samhincks
 */
public class Label {
    public String name;
    public String value;

    public Label(String name, String value, int index) {
        this.name = name.trim();
        this.name = this.name.toLowerCase(); 
        this.value = value.trim();
        this.value= this.value.toLowerCase();
    }
}
