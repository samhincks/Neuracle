package filereader.experiments;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.math3.util.Pair;

public class Sort {
    
    public static void main(String[] args) throws Exception {
    	BufferedReader reader = new BufferedReader(new FileReader("input/sort.csv"));
    	Map<String, Pair> map = new TreeMap<String, Pair>();
    	String line = reader.readLine();//read header
    	while ((line = reader.readLine()) != null) {
            String [] values= line.split(",");
            String key = values[0]+"-"+values[1];
            Float val = Float.parseFloat(values[2]);
            Pair p =  new Pair(key,val);
            map.put(key, p);
    	}
        
    	reader.close();
    	FileWriter writer = new FileWriter("sorted_numbers.txt");
        
    	writer.write("i, j, prob\n");
        // not yet sorted
        List<Pair> vals = new ArrayList<Pair>(map.values());

        Collections.sort(vals, new Comparator<Pair>() {

            public int compare(Pair o1, Pair o2) {
                Float val = (Float) o1.getValue();
                Float val2 = (Float) o2.getValue();
                
                if (val > val2) 
                    return 1;
                else return -1;
            }
        }); 

        for (Pair p : vals) {
            System.out.println(p.getKey() + "\t" + p.getValue());
            writer.write(p.getKey()+","+p.getValue());
            writer.write("\n");
        }
            
    	
    	writer.close();
    }
    
    class Double {
        public Float val;
        public String id;
                
        public Double(String a, Float b) {
            this.val = b;
            this.id = a;
        }
    }
}