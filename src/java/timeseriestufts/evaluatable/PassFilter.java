/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeseriestufts.evaluatable;

/**
 *
 * @author samhincks
 */
public class PassFilter extends Technique{
    public static enum FilterType {HighPass, LowPass, BandPass, bwBandPass, None};
    public FilterType filterType;
    public double highPass;
    public double lowPass;
    public double pass;
    public int order; 
    
    public PassFilter() {
        this.filterType = FilterType.None;
        this.id = "NoFilter";
    }
    public PassFilter (FilterType ft, double pass) {
        this.filterType = ft;
        this.pass = pass;
        this.id = this.filterType.toString() + pass;
    }
    
    public PassFilter (double lowPass, double highPass){
        this.filterType = FilterType.BandPass;
        this.lowPass = lowPass;
        this.highPass = highPass;
        this.id = filterType.toString() +lowPass + "-"+highPass;
    }
    
    public PassFilter (int order, double lowPass, double highPass){
        this.filterType = FilterType.bwBandPass;
        this.lowPass = lowPass;
        this.highPass = highPass;
        this.order = order;
        this.id = filterType.toString() +lowPass + "-"+highPass;
    }
}
