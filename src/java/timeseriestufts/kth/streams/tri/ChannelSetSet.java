/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeseriestufts.kth.streams.tri;

import java.util.ArrayList;
import timeseriestufts.kth.streams.bi.ChannelSet;

/** A collection of channelSets
 *
 * @author samhincks
 */
public class ChannelSetSet extends TridimensionalLayer<ChannelSet> {

    
    public ChannelSetSet() {
        matrixes = new ArrayList();
    }
    public void addChannelSet(ChannelSet cs) {
        super.matrixes.add(cs);
    }
    @Override
    public void printStream() {
        for (ChannelSet cs : matrixes) {
            System.out.println(cs.id);
            cs.printStream();
        }
    }
    
}
