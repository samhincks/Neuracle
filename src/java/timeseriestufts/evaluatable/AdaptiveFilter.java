package timeseriestufts.evaluatable;

/**
 *
 * @author shincks
 */


public class AdaptiveFilter extends Technique{
    public FilterType filterType;
    public static enum FilterType {LMS}; 
    public AdaptiveFilter ( FilterType filterType) {
        
    }
}
