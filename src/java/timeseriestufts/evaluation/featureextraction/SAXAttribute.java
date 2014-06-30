/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeseriestufts.evaluation.featureextraction;

import java.util.Arrays;
import timeseriestufts.kth.streams.uni.Channel;
import weka.core.Attribute;
import weka.core.FastVector;

/**
 *A SAX attribute describes a channel as a SAX-string of specified alphabet and length.
 * It will have alphabet^length possibilities so these must be kept to a minimum
 * 
 * @author samhincks
 */
public class SAXAttribute extends NominalAttribute {
    Channel channel; //.. the channel we are extracting from
    String window; //.. description of window for name
    int alphabet;  //.. size of alphabet we draw from
    int numLetters; //.. length of string
    public static final String [] abc = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
    public String [] thisABC;
    public SAXAttribute(Channel channel, String window, int alphabet, int numLetters) throws Exception{
        this.channel = channel;
        this.window = window;
        this.alphabet = alphabet;
        this.thisABC = Arrays.copyOfRange(this.abc, 0, this.alphabet);
        this.numLetters = numLetters;
        possibilities = computePossibilities(this.thisABC, numLetters);
        this.setName();
    }
    
    /**Given an alphabet and a length, return all permutations
     */
    public static String [] computePossibilities(String [] abc, int numLetters) throws Exception{
       int combinations = (int) Math.pow(abc.length, numLetters);
      
       //.. it is in nobody's interest to have more than 16 possibilites - you wouldn't see any examples of them!
       if(combinations > 42) throw new Exception("Cannot have more than 16 possibilities for nominal attribute. You have " + combinations);

        String [] possibilities = new String[combinations];
        for (int i = 0; i < possibilities.length; i++) {
            possibilities[i] = "";

        }

        int alternation = 1;
        //.. create each nominal value
        for (int i = 0; i < numLetters; i++) {
            int alphaPos = 0;
            //.. for each possible 
            for (int j = 0; j < combinations; j++) {
                possibilities[j] += abc[alphaPos % abc.length];

                //.. on the first layer want to alternate the most, and on the inner layers we alternate half that amount
                if (j % alternation == (alternation - 1)) {
                    alphaPos++;
                }
            }
            alternation = alternation * abc.length;
        }
        
        return possibilities;
    }
    
    /**Return the charachter that appears latest in the alphabet of word*/
    public static int getLatestLetter(String word) {
        int highestIndex =0;
        for(int i =0; i < word.length(); i++) {
            char letter = word.charAt(i);

            for (int j =0; j < SAXAttribute.abc.length; j++) {
                if (SAXAttribute.abc[j].equals(String.valueOf(letter))){
                    if (j > highestIndex){
                        highestIndex = j;
                     }
                }
            }
        }
        if (highestIndex <19)
            return highestIndex+1;
        else return 20;
    }
    
    public static String getBesteBest() {
       return "sax-baqplqrlj^sax-ahln^sax-eoimkm^sax-dnsbc^"
               + "sax-pabgd^sax-rpkrqnhkldm^sax-llaiosp^"
               + "sax-cfjacosp^sax-rqpbfasogf^sax-sed^"
               + "sax-qcoeocfib^sax-osbqehne^sax-egb^"
               + "sax-bhsnjj^sax-ihmffmaqpl^sax-iha^"
               + "sax-hc^sax-chpbh^sax-ak^sax-eeokm^sax-oqe";
    }
    
    public static String getRandomString(int length) {
        String retString ="";
        int max =19;
        for (int i = 0; i < length; i++) {
            int randomInt = (int) (Math.random() * max);
            retString += SAXAttribute.abc[randomInt];
        }
        return retString;
    }
    
   @Override
   public String setName() {     
        type = "NOMINAL";
        name = "CHANNEL-"+channel.id+"-SAX"+this.alphabet+"-"+this.numLetters+"-"+window+"-";        
        return name;
    }

    @Override
    public void extract() throws Exception {
        nomValue = channel.getSAXRepresentation(numLetters, alphabet);
    }
    
    public static void main(String [] args) {
        try{
            int TEST =1;
            if (TEST ==0){
                Channel c = Channel.generate(100);

                SAXAttribute s = new SAXAttribute(c, "whole", 3,3);
                s.extract();
                System.out.println(s.nomValue);
            }
            if (TEST ==1) {
                int l = SAXAttribute.getLatestLetter("aab");
                System.out.println(l);
            }
        }
        catch(Exception e) {e.printStackTrace();}
    }

    @Override
    public weka.core.Attribute getWekaAttribute() {
        FastVector f = new FastVector();
        for (String s : possibilities) f.addElement(s);
        return new weka.core.Attribute(name, f);
    }
}
