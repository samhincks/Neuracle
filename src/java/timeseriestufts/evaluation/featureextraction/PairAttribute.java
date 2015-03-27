/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package timeseriestufts.evaluation.featureextraction;

import timeseriestufts.evaluatable.FeatureDescription;
import timeseriestufts.kth.streams.uni.Channel;

/**
 *
 * @author samhincks
 */
public class PairAttribute extends Attribute {
    
    
    public Channel channel;
    public Channel channel2;
    public FeatureDescription.Statistic stat;
    public String window;
    public int lag =0;
    public int numLetters=0; //.. for SAX
    public int alphaLength=0; //.. num letters to use

    public PairAttribute(Channel channel, Channel channel2, int lag, FeatureDescription.Statistic stat, String window)  {
        this.channel = channel;
        this.channel2 = channel2;
        this.stat = stat;
        this.window = window;
        this.lag = lag;
        this.setName();
    }

    public PairAttribute(Channel a, Channel b, int numLetters, int alphaLength, FeatureDescription.Statistic stat, String window) {
        this.channel = a;
        this.channel2 = b;
        this.stat = stat;
        this.window = window;
        this.numLetters = numLetters;
        this.alphaLength = alphaLength;
        this.setName();
    }
    
    /**
     * The name of the attribute, must be unique for each. If two attributes
     * have the same name, then they should be identical
     */
    @Override
    public String setName()  {
        type = "NUMERIC";
        name = "CHANNEL-" + channel.id + "-"+channel2.id+ "-"+stat.stat + "-" 
                +(this.lag>0 ? this.lag + "l-": "") //.. conditionally display if its been set
                +(this.numLetters > 0 ? this.numLetters+ "n-" : "")
                +(this.alphaLength > 0 ? this.alphaLength+ "a-" : "")
                +"-"+ window + "-";
        return name;
    }

    /**
     * TODO: Implement timeIndex. Implement 1st, 2nd derivative Add standard
     * deviation, etc; get creative
     */
    @Override
    public void extract() throws Exception {
        switch (stat.stat) {
            case granger:
                numValue = channel.granger(channel2, lag);
                break;
            case saxpair:
                numValue = channel.getSAXDistanceTo(channel2, alphaLength, numLetters);
                break;
        }
      //  if(numValue <1)
        //    System.out.println(name + " : " + numValue);
        
        
    }
    @Override
    public weka.core.Attribute getWekaAttribute() {
        return new weka.core.Attribute(this.name);
    }
    
}
